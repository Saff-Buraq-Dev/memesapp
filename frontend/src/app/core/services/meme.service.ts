import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Meme, MemeRequest } from '../models/meme.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class MemeService {
  private apiUrl = `${environment.apiUrl}/memes`;

  constructor(private http: HttpClient) { }

  getMemes(
    page: number = 0,
    size: number = 10,
    sort: string = 'createdAt,desc',
    categories?: string[],
    username?: string,
    title?: string
  ): Observable<ApiResponse<Meme>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (categories && categories.length > 0) {
      categories.forEach(category => {
        params = params.append('categories', category);
      });
    }

    if (username) {
      params = params.set('username', username);
    }

    if (title) {
      params = params.set('title', title);
    }

    return this.http.get<ApiResponse<Meme>>(this.apiUrl, { params });
  }

  getMemeById(id: number): Observable<Meme> {
    return this.http.get<Meme>(`${this.apiUrl}/${id}`);
  }

  createMeme(memeRequest: MemeRequest, file: File): Observable<Meme> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('meme', new Blob([JSON.stringify(memeRequest)], { type: 'application/json' }));

    return this.http.post<Meme>(this.apiUrl, formData);
  }

  uploadMultipleMemes(files: File[], categories: string[]): Observable<Meme[]> {
    const formData = new FormData();

    // Append all files
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }

    // Append categories if any
    if (categories && categories.length > 0) {
      formData.append('categories', new Blob([JSON.stringify(categories)], { type: 'application/json' }));
    }

    return this.http.post<Meme[]>(`${this.apiUrl}/batch`, formData);
  }
}
