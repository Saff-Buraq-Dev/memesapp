import { Component, OnInit } from '@angular/core';
import { MemeService } from '../../core/services/meme.service';
import { CategoryService } from '../../core/services/category.service';
import { Meme } from '../../core/models/meme.model';
import { Category } from '../../core/models/category.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  memes: Meme[] = [];
  categories: Category[] = [];
  selectedCategories: string[] = [];
  searchTitle: string = '';
  loading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;
  sortOption: string = 'createdAt,desc';

  constructor(
    private memeService: MemeService,
    private categoryService: CategoryService
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

  getMemeImageUrl(memeUrl: string): string {
    return `${environment.uploadsUrl}/${memeUrl}`;
  }
}
