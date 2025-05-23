import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

import { MemeService } from '../../../core/services/meme.service';
import { CategoryService } from '../../../core/services/category.service';
import { AuthService } from '../../../core/services/auth.service';
import { Category } from '../../../core/models/category.model';
import { MemeRequest } from '../../../core/models/meme.model';

@Component({
  selector: 'app-meme-upload',
  templateUrl: './meme-upload.component.html',
  styleUrls: ['./meme-upload.component.scss']
})
export class MemeUploadComponent implements OnInit {
  uploadForm: FormGroup;
  categories: Category[] = [];
  selectedFiles: File[] = [];
  imagePreviews: { file: File, preview: string | ArrayBuffer }[] = [];
  loading: boolean = false;

  // For categories
  allCategories: string[] = [];

  // For multiple file upload mode
  isMultipleMode: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private memeService: MemeService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService
  ) {
    this.uploadForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(100)]]
      // Categories are handled separately through allCategories array
    });
  }

  ngOnInit(): void {
    // Check if user is authenticated
    if (!this.authService.isAuthenticated()) {
      this.toastr.error('You must be logged in to upload memes');
      this.router.navigate(['/auth/login']);
      return;
    }

    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe(
      categories => {
        this.categories = categories;
      },
      error => {
        this.toastr.error('Failed to load categories');
      }
    );
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      // Clear previous selections if not in multiple mode
      if (!this.isMultipleMode) {
        this.selectedFiles = [];
        this.imagePreviews = [];
      }

      // Process each file
      for (let i = 0; i < input.files.length; i++) {
        const file = input.files[i];

        // Check file type
        const fileType = file.type;
        if (fileType !== 'image/jpeg' && fileType !== 'image/png' && fileType !== 'image/gif') {
          this.toastr.error(`File ${file.name}: Only JPEG, PNG, and GIF files are allowed`);
          continue;
        }

        // Check file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
          this.toastr.error(`File ${file.name}: File size should not exceed 5MB`);
          continue;
        }

        // Add to selected files
        this.selectedFiles.push(file);

        // Create image preview
        const reader = new FileReader();
        reader.onload = (e) => {
          this.imagePreviews.push({
            file: file,
            preview: reader.result as string
          });

          // If single file mode and it's the first file, set the title to filename
          if (!this.isMultipleMode && this.imagePreviews.length === 1) {
            const fileName = file.name;
            const titleWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            this.uploadForm.get('title')?.setValue(titleWithoutExtension);
          }
        };
        reader.readAsDataURL(file);
      }
    }
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.imagePreviews.splice(index, 1);
  }

  toggleMode(): void {
    this.isMultipleMode = !this.isMultipleMode;

    // Clear selections when switching modes
    this.selectedFiles = [];
    this.imagePreviews = [];

    if (this.isMultipleMode) {
      // In multiple mode, title is not needed
      this.uploadForm.get('title')?.clearValidators();
    } else {
      // In single mode, title is required
      this.uploadForm.get('title')?.setValidators([Validators.required, Validators.maxLength(100)]);
    }
    this.uploadForm.get('title')?.updateValueAndValidity();
  }

  // For categories
  addCategory(event: any): void {
    const value = (event.value || '').trim();

    // Add category if it's not empty and not already in the list
    if (value && !this.allCategories.includes(value)) {
      this.allCategories.push(value);
    }

    // Clear the input value
    if (event.chipInput) {
      event.chipInput.value = '';
    }
  }

  // Add an existing category when clicked
  addExistingCategory(categoryName: string): void {
    if (!this.allCategories.includes(categoryName)) {
      this.allCategories.push(categoryName);
    }
  }

  removeCategory(category: string): void {
    const index = this.allCategories.indexOf(category);

    if (index >= 0) {
      this.allCategories.splice(index, 1);
    }
  }

  onSubmit(): void {
    // Validate form and files
    if (this.uploadForm.invalid || this.selectedFiles.length === 0) {
      this.toastr.error('Please fill in all required fields and select at least one image');
      return;
    }

    // Check authentication
    if (!this.authService.isAuthenticated()) {
      this.toastr.error('You must be logged in to upload memes');
      this.router.navigate(['/auth/login']);
      return;
    }

    console.log('Starting meme upload process');
    this.loading = true;

    if (this.isMultipleMode) {
      // Multiple file upload mode
      console.log('Uploading multiple memes with categories:', this.allCategories);
      this.memeService.uploadMultipleMemes(this.selectedFiles, this.allCategories).subscribe(
        memes => {
          console.log('Upload successful:', memes);
          this.toastr.success(`${memes.length} memes uploaded successfully`);
          this.router.navigate(['/memes']);
        },
        error => {
          console.error('Upload failed:', error);
          this.toastr.error('Failed to upload memes: ' + (error.message || 'Unknown error'));
          this.loading = false;
        }
      );
    } else {
      // Single file upload mode
      const memeRequest: MemeRequest = {
        title: this.uploadForm.value.title,
        categories: this.allCategories
      };

      console.log('Uploading single meme:', memeRequest);
      this.memeService.createMeme(memeRequest, this.selectedFiles[0]).subscribe(
        meme => {
          console.log('Upload successful:', meme);
          this.toastr.success('Meme uploaded successfully');
          this.router.navigate(['/memes', meme.id]);
        },
        error => {
          console.error('Upload failed:', error);
          this.toastr.error('Failed to upload meme: ' + (error.message || 'Unknown error'));
          this.loading = false;
        }
      );
    }
  }
}
