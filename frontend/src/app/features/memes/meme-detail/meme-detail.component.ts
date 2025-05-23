import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { MemeService } from '../../../core/services/meme.service';
import { CommentService } from '../../../core/services/comment.service';
import { VoteService } from '../../../core/services/vote.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { AuthService } from '../../../core/services/auth.service';

import { Meme } from '../../../core/models/meme.model';
import { Comment, CommentRequest } from '../../../core/models/comment.model';
import { Voter } from '../../../core/models/voter.model';
import { WebSocketEvent, VoteUpdate } from '../../../core/models/websocket.model';
import { VotersModalComponent } from '../voters-modal/voters-modal.component';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-meme-detail',
  templateUrl: './meme-detail.component.html',
  styleUrls: ['./meme-detail.component.scss']
})
export class MemeDetailComponent implements OnInit, OnDestroy {
  memeId: number = 0;
  meme: Meme | null = null;
  comments: Comment[] = [];
  commentForm: FormGroup;
  loading: boolean = false;
  commentLoading: boolean = false;
  isAuthenticated: boolean = false;
  uploadsUrl: string = environment.uploadsUrl;
  private subscriptions: Subscription[] = [];

  constructor(
    private route: ActivatedRoute,
    private memeService: MemeService,
    private commentService: CommentService,
    private voteService: VoteService,
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private toastr: ToastrService,
    private modalService: NgbModal
  ) {
    this.commentForm = this.formBuilder.group({
      text: ['', [Validators.required, Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.memeId = +this.route.snapshot.paramMap.get('id')!;

    // Connect to WebSocket
    this.webSocketService.connect();

    // Subscribe to connection status
    this.subscriptions.push(
      this.webSocketService.connected$.subscribe(connected => {
        console.log('WebSocket connection status:', connected);
        if (connected) {
          this.setupWebSocketSubscriptions();
        }
      })
    );

    this.loadMeme();
    this.loadComments();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadMeme(): void {
    this.loading = true;
    this.memeService.getMemeById(this.memeId).subscribe(
      meme => {
        this.meme = meme;
        this.loading = false;
      },
      error => {
        this.toastr.error('Failed to load meme');
        this.loading = false;
      }
    );
  }

  loadComments(): void {
    this.commentLoading = true;
    this.commentService.getRecentCommentsByMemeId(this.memeId).subscribe(
      comments => {
        this.comments = comments;
        this.commentLoading = false;
      },
      error => {
        this.toastr.error('Failed to load comments');
        this.commentLoading = false;
      }
    );
  }

  setupWebSocketSubscriptions(): void {
    console.log('Setting up WebSocket subscriptions for meme', this.memeId);

    // Subscribe to vote updates
    this.webSocketService.subscribeToMemeVotes(this.memeId, (event: WebSocketEvent<VoteUpdate>) => {
      console.log('Received vote update:', event);
      if (event.type === 'VOTE_UPDATED' && this.meme) {
        console.log('Updating meme vote count:', event.payload.voteCount);
        this.meme.voteCount = event.payload.voteCount;
        this.meme.userVoted = event.payload.userVoted;
      }
    });

    // Subscribe to comment updates
    this.webSocketService.subscribeToMemeComments(this.memeId, (event: WebSocketEvent<Comment>) => {
      console.log('Received comment update:', event);
      if (event.type === 'NEW_COMMENT') {
        console.log('Adding new comment to list');
        this.comments.unshift(event.payload);
      }
    });
  }

  toggleVote(): void {
    if (!this.isAuthenticated) {
      this.toastr.warning('Please login to vote');
      return;
    }

    this.voteService.toggleVote(this.memeId).subscribe(
      response => {
        console.log('Vote toggled successfully, waiting for WebSocket update');
        // WebSocket will handle the UI update
        this.toastr.success(response.message);
      },
      error => {
        console.error('Failed to toggle vote:', error);
        this.toastr.error('Failed to vote');
      }
    );
  }

  getVoterImageUrl(voter: Voter): string {
    if (!voter.profilePicture || voter.profilePicture === 'default-avatar.png') {
      return 'assets/images/default-avatar.png';
    }
    return `${this.uploadsUrl}/${voter.profilePicture}`;
  }

  getUserImageUrl(profilePicture: string | null | undefined): string {
    if (!profilePicture) {
      return 'assets/images/default-avatar.png';
    }
    return `${this.uploadsUrl}/${profilePicture}`;
  }

  openVotersModal(meme: Meme): void {
    const modalRef = this.modalService.open(VotersModalComponent, { centered: true });
    modalRef.componentInstance.voters = meme.voters;
  }

  submitComment(): void {
    if (this.commentForm.invalid) {
      return;
    }

    const commentRequest: CommentRequest = {
      text: this.commentForm.value.text
    };

    this.commentService.addComment(this.memeId, commentRequest).subscribe(
      comment => {
        console.log('Comment added successfully, waiting for WebSocket update');
        this.commentForm.reset();
        // WebSocket will handle adding the comment to the list
        this.toastr.success('Comment added successfully');
      },
      error => {
        console.error('Failed to add comment:', error);
        this.toastr.error('Failed to add comment');
      }
    );
  }
}
