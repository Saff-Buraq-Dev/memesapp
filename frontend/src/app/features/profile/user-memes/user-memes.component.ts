import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { MemeService } from '../../../core/services/meme.service';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryService } from '../../../core/services/category.service';
import { Meme } from '../../../core/models/meme.model';
import { User } from '../../../core/models/user.model';
import { Category } from '../../../core/models/category.model';
import { EditMemeModalComponent } from './edit-meme-modal.component';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-user-memes',
  templateUrl: './user-memes.component.html',
  styleUrls: ['./user-memes.component.scss']
})
export class UserMemesComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  memes: Meme[] = [];
  loading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;
  private userSubscription: Subscription = new Subscription();

  constructor(
    private memeService: MemeService,
    private authService: AuthService,
    private categoryService: CategoryService,
    private modalService: NgbModal,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    // Subscribe to current user changes and wait for user to be loaded
    this.userSubscription = this.authService.currentUser$
      .pipe(filter(user => user !== null)) // Only proceed when user is loaded
      .subscribe(user => {
        this.currentUser = user;
        this.loadUserMemes();
      });
  }

  ngOnDestroy(): void {
    this.userSubscription.unsubscribe();
  }

  loadUserMemes(): void {
    if (!this.currentUser) {
      return;
    }

    this.loading = true;
    this.memeService.getMemes(
      this.currentPage,
      10,
      'createdAt,desc',
      undefined,
      this.currentUser.username,
      undefined
    ).subscribe(
      response => {
        this.memes = response.content;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error => {
        this.toastr.error('Failed to load your memes');
        this.loading = false;
      }
    );
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadUserMemes();
  }

  getMemeImageUrl(memeUrl: string): string {
    return `${environment.uploadsUrl}/${memeUrl}`;
  }

  editMeme(meme: Meme): void {
    const modalRef = this.modalService.open(EditMemeModalComponent, {
      centered: true,
      size: 'lg'
    });

    modalRef.componentInstance.meme = meme;

    modalRef.result.then(
      (updatedMeme: Meme) => {
        // Update the meme in our local array
        const index = this.memes.findIndex(m => m.id === meme.id);
        if (index !== -1) {
          this.memes[index] = updatedMeme;
        }
      },
      (dismissed) => {
        // Modal was dismissed, no action needed
      }
    );
  }

  deleteMeme(meme: Meme): void {
    if (confirm(`Are you sure you want to delete "${meme.title}"? This action cannot be undone.`)) {
      this.memeService.deleteMeme(meme.id).subscribe(
        response => {
          // Remove the meme from our local array
          this.memes = this.memes.filter(m => m.id !== meme.id);
          this.toastr.success('Meme deleted successfully');

          // If this was the last meme on the page and we're not on page 0, go back a page
          if (this.memes.length === 0 && this.currentPage > 0) {
            this.currentPage--;
            this.loadUserMemes();
          }
        },
        error => {
          console.error('Error deleting meme:', error);
          this.toastr.error('Failed to delete meme');
        }
      );
    }
  }
}
