import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-card">
      <h2>{{ mode === 'login' ? 'Log in' : 'Register' }}</h2>

      <input [(ngModel)]="email" placeholder="Email" type="email" />
      <input [(ngModel)]="password" placeholder="Password" type="password" />
      <input *ngIf="mode === 'register'" [(ngModel)]="displayName" placeholder="Display name" />

      <button (click)="submit()">{{ mode === 'login' ? 'Log in' : 'Register' }}</button>

      <p class="toggle" (click)="toggleMode()">
        {{ mode === 'login' ? 'Need an account? Register' : 'Have an account? Log in' }}
      </p>

      <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
    </div>
  `,
  styles: [
    `
      .login-card { max-width: 320px; margin: 4rem auto; display: flex; flex-direction: column; gap: 0.75rem; }
      input, button { padding: 0.5rem; font-size: 1rem; }
      .toggle { cursor: pointer; color: #2563eb; text-align: center; }
      .error { color: #dc2626; }
    `,
  ],
})
export class LoginComponent {
  mode: 'login' | 'register' = 'login';
  email = '';
  password = '';
  displayName = '';
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  toggleMode(): void {
    this.mode = this.mode === 'login' ? 'register' : 'login';
    this.errorMessage = '';
  }

  submit(): void {
    const call =
      this.mode === 'login'
        ? this.authService.login(this.email, this.password)
        : this.authService.register(this.email, this.password, this.displayName);

    call.subscribe({
      next: () => this.router.navigate(['/tasks']),
      error: () => (this.errorMessage = 'Something went wrong. Check your details and try again.'),
    });
  }
}
