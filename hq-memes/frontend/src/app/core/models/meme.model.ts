import { UserSummary } from './user.model';
import { Category } from './category.model';
import { Voter } from './voter.model';

export interface Meme {
  id: number;
  title: string;
  url: string;
  createdAt: Date;
  user: UserSummary;
  categories: Category[];
  voteCount: number;
  userVoted: boolean;
  voters?: Voter[];
}

export interface MemeRequest {
  title: string;
  categories: string[];
}
