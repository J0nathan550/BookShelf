import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

function passwordMatch(control: AbstractControl) {
  const pw = control.get('newPassword')?.value;
  const cpw = control.get('confirmPassword')?.value;
  return pw === cpw ? null : { mismatch: true };
}

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, TranslateModule
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <h2 class="auth-title">{{ 'AUTH.RESET_PASSWORD' | translate }}</h2>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.CODE' | translate }}</mat-label>
            <input matInput formControlName="code" maxlength="6">
          </mat-form-field>
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.NEW_PASSWORD' | translate }}</mat-label>
            <input matInput formControlName="newPassword" type="password" autocomplete="new-password">
            @if (form.get('newPassword')?.hasError('minlength') && form.get('newPassword')?.touched) {
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
          <button mat-raised-button color="primary" class="full-width" type="submit" [disabled]="loading" style="margin-top:8px">
            @if (loading) { <mat-spinner diameter="20" style="display:inline-block"></mat-spinner> }
            @else { {{ 'AUTH.RESET_PASSWORD' | translate }} }
          </button>
        </form>
        <div style="margin-top:16px; text-align:center; font-size:14px">
          <a routerLink="/auth/login">{{ 'AUTH.LOGIN' | translate }}</a>
        </div>
      </mat-card>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  form = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordMatch });
  email = '';
  errorMsg = '';
  successMsg = '';
  loading = false;


  ngOnInit(): void {
    this.email = this.route.snapshot.queryParams['email'] ?? '';
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    const { code, newPassword } = this.form.value;
    this.auth.resetPasswordWithCode({ email: this.email, code: code!, newPassword: newPassword! }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          this.successMsg = res.message ?? 'Password reset';
          setTimeout(() => this.router.navigate(['/auth/login']), 1500);
        } else {
          this.errorMsg = res.message ?? 'Reset failed';
        }
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message ?? 'Reset failed';
      }
    });
  }
}
