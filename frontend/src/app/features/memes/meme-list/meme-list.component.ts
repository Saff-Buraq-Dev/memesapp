import { Component, OnInit, OnDestroy } from '@angular/core';
import { MemeService } from '../../../core/services/meme.service';
import { CategoryService } from '../../../core/services/category.service';
import { VoteService } from '../../../core/services/vote.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { ToastrService } from 'ngx-toastr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Meme } from '../../../core/models/meme.model';
import { Category } from '../../../core/models/category.model';
import { Voter } from '../../../core/models/voter.model';
import { VotersModalComponent } from '../voters-modal/voters-modal.component';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-meme-list',
  templateUrl: './meme-list.component.html',
  styleUrls: ['./meme-list.component.scss']
})
export class MemeListComponent implements OnInit, OnDestroy {
  memes: Meme[] = [];
  categories: Category[] = [];
  selectedCategories: string[] = [];
  searchTitle: string = '';
  loading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;
  sortOption: string = 'createdAt,desc';
  uploadsUrl: string = environment.uploadsUrl;

  constructor(
    private memeService: MemeService,
    private categoryService: CategoryService,
    private voteService: VoteService,
    private authService: AuthService,
    private webSocketService: WebSocketService,
    private toastr: ToastrService,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    this.loadMemes();
    this.setupWebSocketSubscriptions();
  }

  ngOnDestroy(): void {
    // WebSocket subscriptions are automatically cleaned up by the service
  }

  setupWebSocketSubscriptions(): void {
    // Subscribe to new memes
    this.webSocketService.subscribeToNewMemes((event) => {
      if (event.type === 'NEW_MEME') {
        // Add new meme to the beginning of the list if we're on the first page
        // and sorting by newest first
        if (this.currentPage === 0 && this.sortOption === 'createdAt,desc') {
          this.memes.unshift(event.payload);
          // Remove the last item to maintain page size
          if (this.memes.length > 10) {
            this.memes.pop();
          }
        }
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(
      categories => {
        this.categories = categories;
      }
    );
  }

  loadMemes(): void {
    this.loading = true;
    this.memeService.getMemes(
      this.currentPage,
      10,
      this.sortOption,
      this.selectedCategories.length > 0 ? this.selectedCategories : undefined,
      undefined,
      this.searchTitle ? this.searchTitle : undefined
    ).subscribe(
      response => {
        this.memes = response.content;
        this.totalPages = response.totalPages;
        this.loading = false;

        // Set up WebSocket subscriptions for each meme's votes
        this.setupMemeVoteSubscriptions();
      },
      error => {
        console.error('Error loading memes', error);
        this.loading = false;
      }
    );
  }

  setupMemeVoteSubscriptions(): void {
    // Subscribe to vote updates for each meme
    this.memes.forEach(meme => {
      this.webSocketService.subscribeToMemeVotes(meme.id, (event) => {
        if (event.type === 'VOTE_UPDATED') {
          // Find the meme in our list and update its vote count
          const memeIndex = this.memes.findIndex(m => m.id === event.payload.memeId);
          if (memeIndex !== -1) {
            this.memes[memeIndex].voteCount = event.payload.voteCount;
            // Note: We don't update userVoted here as that's specific to the current user
            // and should only be updated when the current user votes
          }
        }
      });
    });
  }

  onCategoryChange(category: string): void {
    const index = this.selectedCategories.indexOf(category);
    if (index === -1) {
      this.selectedCategories.push(category);
    } else {
      this.selectedCategories.splice(index, 1);
    }
    this.currentPage = 0;
    this.loadMemes();
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadMemes();
  }

  onSortChange(sortOption: string): void {
    this.sortOption = sortOption;
    this.currentPage = 0;
    this.loadMemes();
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadMemes();
  }

  toggleVote(meme: Meme): void {
    if (!this.authService.isAuthenticated()) {
      this.toastr.warning('Please log in to vote for memes');
      return;
    }

    this.voteService.toggleVote(meme.id).subscribe(
      response => {
        // Only update userVoted status for the current user
        // The vote count will be updated via WebSocket
        meme.userVoted = !meme.userVoted;

        // Update voters list for current user
        const currentUser = this.authService.getCurrentUser();
        if (currentUser) {
          if (meme.userVoted) {
            // Add current user to voters list if not already there
            if (!meme.voters || !meme.voters.find(v => v.id === currentUser.id)) {
              if (!meme.voters) {
                meme.voters = [];
              }
              meme.voters.push({
                id: currentUser.id,
                username: currentUser.username,
                profilePicture: currentUser.profilePicture || 'default-avatar.png'
              });
            }
          } else {
            // Remove current user from voters list
            if (meme.voters) {
              meme.voters = meme.voters.filter(v => v.id !== currentUser.id);
            }
          }
        }

        this.toastr.success(response.message);
      },
      error => {
        console.error('Error toggling vote', error);
        this.toastr.error('Failed to vote for this meme');
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

  getMemeImageUrl(memeUrl: string): string {
    return `${this.uploadsUrl}/${memeUrl}`;
  }

  openVotersModal(meme: Meme): void {
    const modalRef = this.modalService.open(VotersModalComponent, { centered: true });
    modalRef.componentInstance.voters = meme.voters;
  }
}
