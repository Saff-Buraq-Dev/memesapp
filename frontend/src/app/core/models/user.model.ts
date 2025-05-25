export interface User {
  id: number;
  username: string;
  email: string;
  profilePicture?: string;
}

export interface UserSummary {
  id: number;
  username: string;
  email: string;
  profilePicture?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  profilePicture?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
}

export interface ProfileUpdateRequest {
  username?: string;
}
