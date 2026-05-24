import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatCheckboxModule, MatProgressSpinnerModule,
    TranslateModule
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <h2 class="auth-title">{{ 'APP_TITLE' | translate }}</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.EMAIL' | translate }}</mat-label>
            <input matInput formControlName="email" type="email" autocomplete="email">
            @if (form.get('email')?.hasError('required') && form.get('email')?.touched) {
              <mat-error>{{ 'AUTH.EMAIL_REQUIRED' | translate }}</mat-error>
            }
            @if (form.get('email')?.hasError('email') && form.get('email')?.touched) {
              <mat-error>{{ 'AUTH.EMAIL_INVALID' | translate }}</mat-error>
            }
          </mat-form-field>
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.PASSWORD' | translate }}</mat-label>
            <input matInput formControlName="password" type="password" autocomplete="current-password">
            @if (form.get('password')?.hasError('required') && form.get('password')?.touched) {
              <mat-error>{{ 'AUTH.PASSWORD_REQUIRED' | translate }}</mat-error>
            }
          </mat-form-field>
          <mat-checkbox formControlName="rememberMe">{{ 'AUTH.REMEMBER_ME' | translate }}</mat-checkbox>
          @if (errorMsg) {
            <p class="error-message">{{ errorMsg }}</p>
          }
          <button mat-raised-button color="primary" class="full-width" type="submit" [disabled]="loading" style="margin-top:16px">
            @if (loading) { <mat-spinner diameter="20" style="display:inline-block"></mat-spinner> }
            @else { {{ 'AUTH.LOGIN' | translate }} }
          </button>
        </form>
        <div style="margin-top:16px; text-align:center; font-size:14px">
          <a routerLink="/auth/forgot-password">{{ 'AUTH.FORGOT_PASSWORD' | translate }}</a>
        </div>
        <div style="margin-top:8px; text-align:center; font-size:14px">
          {{ 'AUTH.NO_ACCOUNT' | translate }}
          <a routerLink="/auth/register"> {{ 'AUTH.REGISTER' | translate }}</a>
        </div>
      </mat-card>
    </div>
  `
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    rememberMe: [false]
  });
  errorMsg = '';
  loading = false;

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    const { email, password, rememberMe } = this.form.value;
    this.auth.login({ email: email!, password: password!, rememberMe: !!rememberMe }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          this.router.navigate(['/books']);
        } else {
          this.errorMsg = res.message ?? 'Login failed';
          if (res.userId) this.router.navigate(['/auth/verify-email', res.userId]);
        }
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message ?? 'Login failed';
        if (err.error?.userId) this.router.navigate(['/auth/verify-email', err.error.userId]);
      }
    });
  }
}
