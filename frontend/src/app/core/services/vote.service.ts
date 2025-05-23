import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { MessageResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class VoteService {
  private apiUrl = `${environment.apiUrl}/memes`;

  constructor(private http: HttpClient) { }

  toggleVote(memeId: number): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/${memeId}/votes`, {});
  }
}
