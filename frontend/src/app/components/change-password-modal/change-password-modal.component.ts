import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-change-password-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password-modal.component.html',
  styleUrls: ['./change-password-modal.component.css']
})
export class ChangePasswordModalComponent {
  @Output() closed = new EventEmitter<void>();

  private authService = inject(AuthService);

  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  errorMessage = '';
  successMessage = '';
  isSubmitting = false;

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.currentPassword || !this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'All fields are required.';
      return;
    }

    if (this.newPassword.length < 6) {
      this.errorMessage = 'New password must be at least 6 characters.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'New passwords do not match.';
      return;
    }

    this.isSubmitting = true;
    this.authService.changePassword({
      currentPassword: this.currentPassword,
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Password changed successfully!';
        setTimeout(() => {
          this.close();
        }, 1200);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Failed to change password.';
      }
    });
  }

  close(): void {
    this.closed.emit();
  }
}
