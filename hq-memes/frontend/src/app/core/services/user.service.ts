import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ProfileUpdateRequest, UserSummary } from '../models/user.model';
import { MessageResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  getCurrentUser(): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.apiUrl}/me`);
  }

  updateProfile(profileUpdateRequest: ProfileUpdateRequest): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${this.apiUrl}/me`, profileUpdateRequest);
  }

  updateProfilePicture(file: File): Observable<MessageResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<MessageResponse>(`${this.apiUrl}/me/profile-picture`, formData);
  }
}
