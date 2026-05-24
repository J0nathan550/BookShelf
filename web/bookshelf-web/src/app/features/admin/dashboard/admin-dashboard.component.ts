import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { AdminDashboardDto } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule, MatTableModule, TranslateModule],
  template: `
    <h2>{{ 'ADMIN.DASHBOARD' | translate }}</h2>
    @if (loading()) {
      <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
    } @else if (data()) {
      <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(160px, 1fr)); gap:16px; margin-bottom:24px">
        <mat-card class="stat-card">
          <div class="stat-value">{{ data()!.totalUsers }}</div>
          <div class="stat-label">{{ 'ADMIN.TOTAL_USERS' | translate }}</div>
        </mat-card>
        <mat-card class="stat-card">
          <div class="stat-value">{{ data()!.totalBooks }}</div>
          <div class="stat-label">{{ 'ADMIN.TOTAL_BOOKS' | translate }}</div>
        </mat-card>
        <mat-card class="stat-card">
          <div class="stat-value">{{ data()!.pendingBooksCount }}</div>
          <div class="stat-label">{{ 'ADMIN.PENDING_BOOKS' | translate }}</div>
        </mat-card>
      </div>

      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ 'ADMIN.RECENT_REGISTRATIONS' | translate }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="data()!.recentRegistrations" style="width:100%">
            <ng-container matColumnDef="fullName">
              <th mat-header-cell *matHeaderCellDef>{{ 'ADMIN.USER_NAME' | translate }}</th>
              <td mat-cell *matCellDef="let u">{{ u.fullName }}</td>
            </ng-container>
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>{{ 'ADMIN.USER_EMAIL' | translate }}</th>
              <td mat-cell *matCellDef="let u">{{ u.email }}</td>
            </ng-container>
            <ng-container matColumnDef="registrationDate">
              <th mat-header-cell *matHeaderCellDef>{{ 'ADMIN.USER_REGISTERED' | translate }}</th>
              <td mat-cell *matCellDef="let u">{{ u.registrationDate | date:'mediumDate' }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="['fullName','email','registrationDate']"></tr>
            <tr mat-row *matRowDef="let r; columns: ['fullName','email','registrationDate']"></tr>
          </table>
        </mat-card-content>
      </mat-card>
    }
  `
})
export class AdminDashboardComponent implements OnInit {
  data = signal<AdminDashboardDto | null>(null);
  loading = signal(true);

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
