import { Component, OnInit } from '@angular/core';
import { MemeService } from '../../../core/services/meme.service';
import { CategoryService } from '../../../core/services/category.service';
import { VoteService } from '../../../core/services/vote.service';
import { AuthService } from '../../../core/services/auth.service';
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
export class MemeListComponent implements OnInit {
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
    private toastr: ToastrService,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    this.loadMemes();
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
      },
      error => {
        console.error('Error loading memes', error);
        this.loading = false;
      }
    );
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
        // Toggle the userVoted status
        meme.userVoted = !meme.userVoted;

        // Update the vote count
        if (meme.userVoted) {
          meme.voteCount++;

          // Add current user to voters list if not already there
          const currentUser = this.authService.getCurrentUser();
          if (currentUser && (!meme.voters || !meme.voters.find(v => v.id === currentUser.id))) {
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
          meme.voteCount--;

          // Remove current user from voters list
          const currentUser = this.authService.getCurrentUser();
          if (currentUser && meme.voters) {
            meme.voters = meme.voters.filter(v => v.id !== currentUser.id);
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

  openVotersModal(meme: Meme): void {
    const modalRef = this.modalService.open(VotersModalComponent, { centered: true });
    modalRef.componentInstance.voters = meme.voters;
  }
}
