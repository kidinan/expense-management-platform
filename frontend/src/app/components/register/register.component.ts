import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  fullName = '';
  username = '';
  email = '';
  password = '';
  confirmPassword = '';

  errorMessage = '';
  isLoading = false;

  onSubmit(): void {
    if (!this.fullName || !this.username || !this.email || !this.password) {
      this.errorMessage = 'Please fill out all fields.';
      return;
    }

    if (this.password.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register({
      fullName: this.fullName,
      username: this.username,
      email: this.email,
      password: this.password
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        if (err.error?.errors) {
          const firstKey = Object.keys(err.error.errors)[0];
          this.errorMessage = err.error.errors[firstKey];
        } else {
          this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        }
      }
    });
  }
}
