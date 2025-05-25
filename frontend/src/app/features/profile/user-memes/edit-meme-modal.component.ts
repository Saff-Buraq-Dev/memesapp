import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';

import { MemeService } from '../../../core/services/meme.service';
import { CategoryService } from '../../../core/services/category.service';
import { Meme } from '../../../core/models/meme.model';
import { Category } from '../../../core/models/category.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-edit-meme-modal',
  templateUrl: './edit-meme-modal.component.html',
  styleUrls: ['./edit-meme-modal.component.scss']
})
export class EditMemeModalComponent implements OnInit {
  @Input() meme!: Meme;

  editForm: FormGroup;
  availableCategories: Category[] = [];
  selectedCategories: string[] = [];
  loading = false;

  constructor(
    public activeModal: NgbActiveModal,
    private formBuilder: FormBuilder,
    private memeService: MemeService,
    private categoryService: CategoryService,
    private toastr: ToastrService
  ) {
    this.editForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.minLength(1)]],
      newCategory: ['']
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.initializeForm();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(
      categories => {
        this.availableCategories = categories;
      },
      error => {
        console.error('Error loading categories:', error);
        this.toastr.error('Failed to load categories');
      }
    );
  }

  initializeForm(): void {
    // Set the title
    this.editForm.patchValue({
      title: this.meme.title
    });

    // Set the selected categories
    if (this.meme.categories) {
      this.selectedCategories = this.meme.categories.map(c => c.name);
    }
  }

  isSelectedCategory(categoryName: string): boolean {
    return this.selectedCategories.includes(categoryName);
  }

  onCategoryChange(categoryName: string, event: any): void {
    if (event.target.checked) {
      if (!this.selectedCategories.includes(categoryName)) {
        this.selectedCategories.push(categoryName);
      }
    } else {
      this.selectedCategories = this.selectedCategories.filter(c => c !== categoryName);
    }
  }

  removeCategory(categoryName: string): void {
    this.selectedCategories = this.selectedCategories.filter(c => c !== categoryName);
  }

  addNewCategory(): void {
    const newCategoryValue = this.editForm.get('newCategory')?.value;
    if (newCategoryValue && newCategoryValue.trim()) {
      const categoryName = newCategoryValue.trim();

      // Check if category already exists
      const existingCategory = this.availableCategories.find(c =>
        c.name.toLowerCase() === categoryName.toLowerCase()
      );

      if (existingCategory) {
        // Category exists, just select it
        if (!this.selectedCategories.includes(existingCategory.name)) {
          this.selectedCategories.push(existingCategory.name);
        }
        this.toastr.info(`Category "${existingCategory.name}" already exists and has been selected`);
      } else {
        // Create new category
        this.categoryService.createCategory({ name: categoryName }).subscribe(
          (newCategory: Category) => {
            // Add to available categories
            this.availableCategories.push(newCategory);
            // Select the new category
            this.selectedCategories.push(newCategory.name);
            this.toastr.success(`New category "${newCategory.name}" created and selected`);
          },
          (error: any) => {
            console.error('Error creating category:', error);
            this.toastr.error('Failed to create new category');
          }
        );
      }

      // Clear the input
      this.editForm.get('newCategory')?.setValue('');
    }
  }

  getMemeImageUrl(memeUrl: string): string {
    return `${environment.uploadsUrl}/${memeUrl}`;
  }

  onSubmit(): void {
    if (this.editForm.valid) {
      this.loading = true;

      const updateRequest = {
        title: this.editForm.get('title')?.value.trim(),
        categories: this.selectedCategories
      };

      this.memeService.updateMeme(this.meme.id, updateRequest).subscribe(
        updatedMeme => {
          this.loading = false;
          this.toastr.success('Meme updated successfully');
          this.activeModal.close(updatedMeme);
        },
        error => {
          this.loading = false;
          console.error('Error updating meme:', error);
          this.toastr.error('Failed to update meme');
        }
      );
    }
  }
}
