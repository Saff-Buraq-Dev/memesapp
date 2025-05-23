import { Component, OnInit } from '@angular/core';
import { AuthService } from './core/services/auth.service';
import { WebSocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'MemeVote';

  constructor(
    private authService: AuthService,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.webSocketService.connect();
    }

    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.webSocketService.connect();
      } else {
        this.webSocketService.disconnect();
      }
    });
  }
}
