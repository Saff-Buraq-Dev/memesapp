import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { LocalStorageService } from 'ngx-webstorage';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, SignupRequest, User } from '../models/user.model';
import { MessageResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private jwtHelper: JwtHelperService,
    private localStorage: LocalStorageService
  ) {
    this.loadCurrentUser();
  }

  login(loginRequest: LoginRequest): Observable<AuthResponse> {
    console.log('Attempting login with:', loginRequest.email);
    return this.http.post<AuthResponse>(`${this.apiUrl}/signin`, loginRequest)
      .pipe(
        tap(response => {
          console.log('Login successful, storing token and user data');
          this.localStorage.store('token', response.token);
          const user = {
            id: response.id,
            username: response.username,
            email: response.email,
            profilePicture: response.profilePicture
          };
          console.log('User data:', user);
          this.currentUserSubject.next(user);
        })
      );
  }

  register(signupRequest: SignupRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/signup`, signupRequest);
  }

  logout(): void {
    this.localStorage.clear('token');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    const token = this.localStorage.retrieve('token');
    return token && !this.jwtHelper.isTokenExpired(token);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  private loadCurrentUser(): void {
    const token = this.localStorage.retrieve('token');
    if (token && !this.jwtHelper.isTokenExpired(token)) {
      console.log('Found valid token, loading user data');
      const decodedToken = this.jwtHelper.decodeToken(token);
      console.log('Decoded token:', decodedToken);

      this.http.get<User>(`${environment.apiUrl}/users/me`).subscribe(
        user => {
          console.log('User data loaded successfully:', user);
          this.currentUserSubject.next(user);
        },
        error => {
          console.error('Failed to load user data:', error);
          this.localStorage.clear('token');
          this.currentUserSubject.next(null);
        }
      );
    } else if (token) {
      console.log('Token expired, clearing authentication');
      this.localStorage.clear('token');
      this.currentUserSubject.next(null);
    } else {
      console.log('No token found');
    }
  }
}
