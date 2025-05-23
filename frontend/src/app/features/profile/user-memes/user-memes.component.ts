import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

import { MemeService } from '../../../core/services/meme.service';
import { AuthService } from '../../../core/services/auth.service';
import { Meme } from '../../../core/models/meme.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-memes',
  templateUrl: './user-memes.component.html',
  styleUrls: ['./user-memes.component.scss']
})
export class UserMemesComponent implements OnInit {
  currentUser: User | null = null;
  memes: Meme[] = [];
  loading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;

  constructor(
    private memeService: MemeService,
    private authService: AuthService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadUserMemes();
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
}
