import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, TranslateModule
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <h2 class="auth-title">{{ 'AUTH.FORGOT_PASSWORD' | translate }}</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.EMAIL' | translate }}</mat-label>
            <input matInput formControlName="email" type="email" autocomplete="email">
            @if (form.get('email')?.hasError('required') && form.get('email')?.touched) {
              <mat-error>{{ 'AUTH.EMAIL_REQUIRED' | translate }}</mat-error>
            }
          </mat-form-field>
          @if (errorMsg) { <p class="error-message">{{ errorMsg }}</p> }
          @if (successMsg) { <p class="success-message">{{ successMsg }}</p> }
          <button mat-raised-button color="primary" class="full-width" type="submit" [disabled]="loading" style="margin-top:8px">
            @if (loading) { <mat-spinner diameter="20" style="display:inline-block"></mat-spinner> }
            @else { {{ 'AUTH.SEND_CODE' | translate }} }
          </button>
        </form>
        <div style="margin-top:16px; text-align:center; font-size:14px">
          <a routerLink="/auth/login">{{ 'AUTH.LOGIN' | translate }}</a>
        </div>
      </mat-card>
    </div>
  `
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
  errorMsg = '';
  successMsg = '';
  loading = false;

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    this.auth.forgotPassword({ email: this.form.value.email! }).subscribe({
      next: res => {
        this.loading = false;
        this.successMsg = res.message ?? 'Code sent if email exists';
        setTimeout(() => this.router.navigate(['/auth/reset-password'], { queryParams: { email: this.form.value.email } }), 1500);
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Failed to send code';
      }
    });
  }
}
