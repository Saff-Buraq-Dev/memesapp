import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Comment, CommentRequest } from '../models/comment.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${environment.apiUrl}/memes`;

  constructor(private http: HttpClient) { }

  getCommentsByMemeId(
    memeId: number,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<Comment>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'createdAt,desc');

    return this.http.get<ApiResponse<Comment>>(`${this.apiUrl}/${memeId}/comments`, { params });
  }

  getRecentCommentsByMemeId(memeId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/${memeId}/comments/recent`);
  }

  addComment(memeId: number, commentRequest: CommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/${memeId}/comments`, commentRequest);
  }
}
