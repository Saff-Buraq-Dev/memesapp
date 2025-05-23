import { UserSummary } from './user.model';

export interface Comment {
  id: number;
  text: string;
  createdAt: Date;
  user: UserSummary;
  memeId: number;
}

export interface CommentRequest {
  text: string;
}
