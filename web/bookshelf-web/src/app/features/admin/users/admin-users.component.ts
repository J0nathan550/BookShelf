import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { AdminUserDto } from '../../../core/models/admin.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatSnackBarModule, TranslateModule
  ],
  template: `
    <h2>{{ 'ADMIN.USERS' | translate }}</h2>
    @if (loading()) {
      <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
    } @else {
      <table mat-table [dataSource]="users()" style="width:100%">
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
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>{{ 'ADMIN.USER_STATUS' | translate }}</th>
          <td mat-cell *matCellDef="let u">
            @if (u.isActive) {
              <span class="approved-badge">{{ 'ADMIN.ACTIVE' | translate }}</span>
            } @else {
              <span class="lent-badge">{{ 'ADMIN.DISABLED' | translate }}</span>
            }
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let u">
            @if (u.isActive) {
              <button mat-icon-button color="warn" (click)="disable(u)" [title]="'ADMIN.DISABLE_USER' | translate">
                <mat-icon>block</mat-icon>
              </button>
            } @else {
              <button mat-icon-button color="primary" (click)="enable(u)" [title]="'ADMIN.ENABLE_USER' | translate">
                <mat-icon>check_circle</mat-icon>
              </button>
            }
            <button mat-icon-button color="warn" (click)="deleteUser(u)" [title]="'ADMIN.DELETE_USER' | translate">
              <mat-icon>delete</mat-icon>
            </button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="cols"></tr>
        <tr mat-row *matRowDef="let r; columns: cols"></tr>
      </table>
    }
  `
})
export class AdminUsersComponent implements OnInit {
  users = signal<AdminUserDto[]>([]);
  loading = signal(true);
  cols = ['fullName', 'email', 'registrationDate', 'status', 'actions'];

  constructor(
    private adminService: AdminService,
    private snack: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.adminService.getUsers().subscribe({
      next: u => { this.users.set(u); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  disable(u: AdminUserDto): void {
    this.adminService.disableUser(u.id).subscribe({
      next: () => {
        this.users.update(list => list.map(x => x.id === u.id ? { ...x, isActive: false } : x));
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      }
    });
  }

  enable(u: AdminUserDto): void {
    this.adminService.enableUser(u.id).subscribe({
      next: () => {
        this.users.update(list => list.map(x => x.id === u.id ? { ...x, isActive: true } : x));
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      }
    });
  }

  deleteUser(u: AdminUserDto): void {
    if (!confirm(this.translate.instant('ADMIN.CONFIRM_DELETE_USER'))) return;
    this.adminService.deleteUser(u.id).subscribe({
      next: () => {
        this.users.update(list => list.filter(x => x.id !== u.id));
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      }
    });
  }
}
