export interface WebSocketEvent<T> {
  type: string;
  payload: T;
}

export interface VoteUpdate {
  memeId: number;
  voteCount: number;
  userVoted: boolean;
}
