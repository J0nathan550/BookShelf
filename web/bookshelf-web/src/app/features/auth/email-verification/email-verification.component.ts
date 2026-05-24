import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-email-verification',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule, TranslateModule
  ],
  template: `
    <div class="auth-container">
      <mat-card class="auth-card">
        <h2 class="auth-title">{{ 'AUTH.VERIFY_EMAIL' | translate }}</h2>
        <p style="text-align:center;color:#616161;margin-bottom:20px">{{ 'AUTH.VERIFICATION_HINT' | translate }}</p>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <mat-form-field class="full-width">
            <mat-label>{{ 'AUTH.CODE' | translate }}</mat-label>
            <input matInput formControlName="code" maxlength="6" autocomplete="one-time-code">
            @if (form.get('code')?.hasError('required') && form.get('code')?.touched) {
              <mat-error>{{ 'AUTH.CODE_REQUIRED' | translate }}</mat-error>
            }
          </mat-form-field>
          @if (errorMsg) { <p class="error-message">{{ errorMsg }}</p> }
          @if (successMsg) { <p class="success-message">{{ successMsg }}</p> }
          <button mat-raised-button color="primary" class="full-width" type="submit" [disabled]="loading" style="margin-top:8px">
            @if (loading) { <mat-spinner diameter="20" style="display:inline-block"></mat-spinner> }
            @else { {{ 'AUTH.SUBMIT' | translate }} }
          </button>
          <button mat-stroked-button class="full-width" type="button" (click)="resend()" [disabled]="loading" style="margin-top:8px">
            {{ 'AUTH.RESEND_CODE' | translate }}
          </button>
        </form>
        <div style="margin-top:16px; text-align:center; font-size:14px">
          <a routerLink="/auth/login">{{ 'AUTH.LOGIN' | translate }}</a>
        </div>
      </mat-card>
    </div>
  `
})
export class EmailVerificationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  form = this.fb.group({ code: ['', [Validators.required, Validators.minLength(6)]] });
  userId = '';
  errorMsg = '';
  successMsg = '';
  loading = false;

  ngOnInit(): void {
    this.userId = this.route.snapshot.params['userId'];
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading = true;
    this.errorMsg = '';
    this.auth.verifyEmailCode({ userId: this.userId, code: this.form.value.code! }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          this.router.navigate(['/auth/login']);
        } else {
          this.errorMsg = res.message ?? 'Verification failed';
        }
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message ?? 'Verification failed';
      }
    });
  }

  resend(): void {
    this.loading = true;
    this.auth.resendVerificationCode({ userId: this.userId }).subscribe({
      next: res => {
        this.loading = false;
        this.successMsg = res.message ?? 'Code sent';
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Failed to resend code';
      }
    });
  }
}
