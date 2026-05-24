import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

function passwordMatch(control: AbstractControl) {
  const pw = control.get('password')?.value;
  const cpw = control.get('confirmPassword')?.value;
  return pw === cpw ? null : { mismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, TranslateModule
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <h2 class="auth-title">{{ 'AUTH.REGISTER' | translate }}</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.FULL_NAME' | translate }}</mat-label>
            <input matInput formControlName="fullName" autocomplete="name">
            @if (form.get('fullName')?.hasError('required') && form.get('fullName')?.touched) {
              <mat-error>{{ 'AUTH.FULLNAME_REQUIRED' | translate }}</mat-error>
            }
          </mat-form-field>
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
            <input matInput formControlName="password" type="password" autocomplete="new-password">
            @if (form.get('password')?.hasError('minlength') && form.get('password')?.touched) {
              <mat-error>{{ 'AUTH.PASSWORD_MIN' | translate }}</mat-error>
            }
          </mat-form-field>
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.CONFIRM_PASSWORD' | translate }}</mat-label>
            <input matInput formControlName="confirmPassword" type="password" autocomplete="new-password">
            @if (form.hasError('mismatch') && form.get('confirmPassword')?.touched) {
              <mat-error>{{ 'AUTH.PASSWORDS_MISMATCH' | translate }}</mat-error>
            }
          </mat-form-field>
          @if (errorMsg) { <p class="error-message">{{ errorMsg }}</p> }
          @if (successMsg) { <p class="success-message">{{ successMsg }}</p> }
          <button mat-raised-button color="primary" class="full-width" type="submit" [disabled]="loading" style="margin-top:16px">
            @if (loading) { <mat-spinner diameter="20" style="display:inline-block"></mat-spinner> }
            @else { {{ 'AUTH.REGISTER' | translate }} }
          </button>
        </form>
        <div style="margin-top:16px; text-align:center; font-size:14px">
          {{ 'AUTH.HAVE_ACCOUNT' | translate }}
          <a routerLink="/auth/login"> {{ 'AUTH.LOGIN' | translate }}</a>
        </div>
      </mat-card>
    </div>
  `
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordMatch });
  errorMsg = '';
  successMsg = '';
  loading = false;


  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    const { email, password, fullName } = this.form.value;
    this.auth.register({ email: email!, password: password!, fullName: fullName! }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          this.router.navigate(['/auth/verify-email', res.userId]);
        } else {
          this.errorMsg = res.message ?? 'Registration failed';
        }
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message ?? 'Registration failed';
      }
    });
  }
}
