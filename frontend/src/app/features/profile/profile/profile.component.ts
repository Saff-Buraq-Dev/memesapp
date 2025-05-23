import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';

import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { User, ProfileUpdateRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  currentUser: User | null = null;
  profileForm: FormGroup;
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  loading: boolean = false;
  uploadLoading: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private toastr: ToastrService
  ) {
    this.profileForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]]
    });
  }

  ngOnInit(): void {
    this.loadCurrentUser();
  }

  loadCurrentUser(): void {
    this.currentUser = this.authService.getCurrentUser();
    if (this.currentUser) {
      this.profileForm.patchValue({
        username: this.currentUser.username
      });
      
      if (this.currentUser.profilePicture) {
        this.imagePreview = `http://localhost:8080/uploads/${this.currentUser.profilePicture}`;
      }
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
      
      // Check file type
      const fileType = this.selectedFile.type;
      if (fileType !== 'image/jpeg' && fileType !== 'image/png' && fileType !== 'image/gif') {
        this.toastr.error('Only JPEG, PNG, and GIF files are allowed');
        this.selectedFile = null;
        return;
      }
      
      // Check file size (max 2MB)
      if (this.selectedFile.size > 2 * 1024 * 1024) {
        this.toastr.error('File size should not exceed 2MB');
        this.selectedFile = null;
        return;
      }
      
      // Create image preview
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  uploadProfilePicture(): void {
    if (!this.selectedFile) {
      return;
    }

    this.uploadLoading = true;
    this.userService.updateProfilePicture(this.selectedFile).subscribe(
      response => {
        this.toastr.success(response.message || 'Profile picture updated successfully');
        this.uploadLoading = false;
        this.selectedFile = null;
        
        // Refresh user data
        this.userService.getCurrentUser().subscribe(
          user => {
            if (this.currentUser) {
              this.currentUser.profilePicture = user.profilePicture;
            }
          }
        );
      },
      error => {
        this.toastr.error(error.error?.message || 'Failed to update profile picture');
        this.uploadLoading = false;
      }
    );
  }

  updateProfile(): void {
    if (this.profileForm.invalid) {
      return;
    }

    this.loading = true;
    const profileUpdateRequest: ProfileUpdateRequest = {
      username: this.profileForm.value.username
    };

    this.userService.updateProfile(profileUpdateRequest).subscribe(
      response => {
        this.toastr.success(response.message || 'Profile updated successfully');
        this.loading = false;
        
        // Refresh user data
        this.userService.getCurrentUser().subscribe(
          user => {
            if (this.currentUser) {
              this.currentUser.username = user.username;
            }
          }
        );
      },
      error => {
        this.toastr.error(error.error?.message || 'Failed to update profile');
        this.loading = false;
      }
    );
  }
}
