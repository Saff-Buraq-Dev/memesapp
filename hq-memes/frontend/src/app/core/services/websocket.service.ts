import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';

import { environment } from '../../../environments/environment';
import { WebSocketEvent, VoteUpdate } from '../models/websocket.model';
import { Comment } from '../models/comment.model';
import { Meme } from '../models/meme.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService implements OnDestroy {
  private client: Client;
  private connected = new BehaviorSubject<boolean>(false);
  public connected$ = this.connected.asObservable();
  private subscriptions: Map<string, StompSubscription> = new Map();
  private messageSubject: Subject<{ topic: string, message: WebSocketEvent<any> }> = new Subject();

  constructor() {
    // Initialize as disconnected
    this.connected.next(false);

    // Initialize STOMP client
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${environment.apiUrl.replace('/api', '')}/ws`),
      debug: function (str) {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = (frame) => {
      console.log('WebSocket Connected: ' + frame);
      this.connected.next(true);
    };

    this.client.onStompError = (frame) => {
      console.error('WebSocket Error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.client.onDisconnect = () => {
      console.log('WebSocket Disconnected');
      this.connected.next(false);
    };
  }

  connect(): void {
    if (!this.client.active) {
      console.log('Connecting to WebSocket...');
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.client.active) {
      console.log('Disconnecting from WebSocket...');
      this.client.deactivate();
      this.subscriptions.clear();
      this.connected.next(false);
    }
  }

  private subscribe<T>(topic: string): Observable<WebSocketEvent<T>> {
    if (!this.client.active) {
      this.connect();
    }

    if (!this.subscriptions.has(topic)) {
      console.log(`Subscribing to ${topic}`);

      const subscription = this.client.subscribe(topic, (message: IMessage) => {
        try {
          const parsedMessage: WebSocketEvent<T> = JSON.parse(message.body);
          console.log(`Received message from ${topic}:`, parsedMessage);
          this.messageSubject.next({ topic, message: parsedMessage });
        } catch (e) {
          console.error(`Error parsing message from ${topic}:`, e);
        }
      });

      this.subscriptions.set(topic, subscription);
    }

    return this.messageSubject.pipe(
      filter(msg => msg.topic === topic),
      map(msg => msg.message as WebSocketEvent<T>)
    );
  }

  subscribeToMemeVotes(memeId: number, callback: (event: WebSocketEvent<VoteUpdate>) => void): void {
    const topic = `/topic/memes/${memeId}/votes`;
    this.subscribe<VoteUpdate>(topic).subscribe(callback);
  }

  subscribeToMemeComments(memeId: number, callback: (event: WebSocketEvent<Comment>) => void): void {
    const topic = `/topic/memes/${memeId}/comments`;
    this.subscribe<Comment>(topic).subscribe(callback);
  }

  subscribeToNewMemes(callback: (event: WebSocketEvent<Meme>) => void): void {
    const topic = '/topic/memes/new';
    this.subscribe<Meme>(topic).subscribe(callback);
  }

  unsubscribe(topic: string): void {
    if (this.subscriptions.has(topic)) {
      console.log(`Unsubscribing from ${topic}`);
      const subscription = this.subscriptions.get(topic);
      subscription?.unsubscribe();
      this.subscriptions.delete(topic);
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
