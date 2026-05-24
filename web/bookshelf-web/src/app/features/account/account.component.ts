import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatCardModule, MatButtonModule, MatIconModule,
    MatProgressSpinnerModule, TranslateModule
  ],
  template: `
    <div class="page-container" style="max-width:500px">
      <h1>{{ 'ACCOUNT.TITLE' | translate }}</h1>
      @if (loading()) {
        <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
      } @else {
        <mat-card>
          <mat-card-header>
            <mat-icon mat-card-avatar style="font-size:40px; width:40px; height:40px; color:#3949ab">account_circle</mat-icon>
            <mat-card-title>{{ user()?.fullName }}</mat-card-title>
            <mat-card-subtitle>{{ user()?.email }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content style="padding-top:16px">
            @if (user()?.roles?.length) {
              <p style="margin:0; font-size:13px; color:#616161">
                <strong>Roles:</strong> {{ user()!.roles.join(', ') }}
              </p>
            }
          </mat-card-content>
          <mat-card-actions>
            <button mat-stroked-button color="primary"
              [routerLink]="['/auth/forgot-password']"
              [queryParams]="{ email: user()?.email }">
              <mat-icon>lock_reset</mat-icon>
              {{ 'ACCOUNT.RESET_PASSWORD' | translate }}
            </button>
            <button mat-raised-button color="warn" (click)="auth.logout()" style="margin-left:8px">
              <mat-icon>logout</mat-icon>
              {{ 'ACCOUNT.SIGN_OUT' | translate }}
            </button>
          </mat-card-actions>
        </mat-card>
      }
    </div>
  `
})
export class AccountComponent implements OnInit {
  auth = inject(AuthService);
  user = this.auth.currentUser;
  loading = signal(false);

  ngOnInit(): void {
    this.loading.set(true);
    this.auth.getCurrentUser().subscribe({
      next: () => this.loading.set(false),
      error: () => this.loading.set(false)
    });
  }
}
