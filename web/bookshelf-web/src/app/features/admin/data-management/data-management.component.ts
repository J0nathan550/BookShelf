import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { statusKey } from '../../../core/utils/status.utils';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { StatisticsService } from '../../../core/services/statistics.service';
import { BookDto } from '../../../core/models/book.model';

@Component({
  selector: 'app-data-management',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatButtonModule, MatIconModule,
    MatDividerModule, MatSnackBarModule, MatProgressSpinnerModule, TranslateModule
  ],
  template: `
    <h2>{{ 'ADMIN.DATA' | translate }}</h2>

    <mat-card style="margin-bottom:16px">
      <mat-card-header>
        <mat-card-title>{{ 'ADMIN.BACKUP' | translate }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p style="font-size:14px; color:#616161">Export all system data as a JSON backup file.</p>
        <div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:8px">
          <button mat-raised-button color="primary" (click)="exportJSON()" [disabled]="working()">
            <mat-icon>download</mat-icon>
            {{ 'ADMIN.EXPORT_JSON' | translate }}
          </button>
          <button mat-raised-button (click)="exportCSV()" [disabled]="working()">
            <mat-icon>table_view</mat-icon>
            {{ 'ADMIN.EXPORT_CSV' | translate }}
          </button>
        </div>
      </mat-card-content>
    </mat-card>

    <mat-card>
      <mat-card-header>
        <mat-card-title>{{ 'ADMIN.IMPORT_JSON' | translate }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <p style="font-size:14px; color:#616161">Import a previously exported JSON backup.</p>
        <div style="display:flex; gap:8px; align-items:center; margin-top:8px; flex-wrap:wrap">
          <button mat-stroked-button (click)="fileInput.click()">
            <mat-icon>upload_file</mat-icon>
            {{ 'ADMIN.IMPORT_FILE' | translate }}
          </button>
          <input #fileInput type="file" accept=".json" style="display:none" (change)="onFileSelected($event)">
          @if (selectedFile()) {
            <span style="font-size:13px; color:#616161">{{ selectedFile()!.name }}</span>
            <button mat-raised-button color="warn" (click)="importJSON()" [disabled]="working()">
              @if (working()) { <mat-spinner diameter="18" style="display:inline-block"></mat-spinner> }
              @else { <mat-icon>publish</mat-icon> }
              {{ 'ADMIN.IMPORT_JSON' | translate }}
            </button>
          }
        </div>
        @if (importResult()) {
          <p [class]="importSuccess() ? 'success-message' : 'error-message'" style="margin-top:8px">{{ importResult() }}</p>
        }
      </mat-card-content>
    </mat-card>
  `
})
export class DataManagementComponent {
  working = signal(false);
  selectedFile = signal<File | null>(null);
  importResult = signal('');
  importSuccess = signal(false);

  constructor(
    private adminService: AdminService,
    private statsService: StatisticsService,
    private snack: MatSnackBar,
    private translate: TranslateService
  ) {}

  exportJSON(): void {
    this.working.set(true);
    Promise.all([
      this.adminService.getAllBooks().toPromise(),
      this.adminService.getUsers().toPromise(),
      this.statsService.getStatistics().toPromise()
    ]).then(([books, users, stats]) => {
      const data = {
        exportedAt: new Date().toISOString(),
        books,
        users,
        statistics: stats
      };
      this.downloadFile(JSON.stringify(data, null, 2), 'bookshelf-backup.json', 'application/json');
      this.working.set(false);
      this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
    }).catch(() => {
      this.working.set(false);
      this.snack.open(this.translate.instant('COMMON.ERROR'), '', { duration: 3000 });
    });
  }

  exportCSV(): void {
    this.working.set(true);
    this.adminService.getAllBooks().subscribe({
      next: books => {
        const csvQuote = (s: string) => `"${(s ?? '').replace(/"/g, '""')}"`;
        const header = [
          'ID', 'Title', 'Author', 'Genre', 'Format', 'Pages',
          'Date Added', 'Status', 'Approved', 'Lent'
        ].join(',');
        const rows = (books as BookDto[]).map(b =>
          [
            b.id,
            csvQuote(b.title),
            csvQuote(b.author),
            csvQuote(b.genre?.name ?? ''),
            csvQuote(b.format?.name ?? ''),
            b.pages ?? '',
            b.dateAdded ? new Date(b.dateAdded).toLocaleDateString() : '',
            csvQuote(this.translate.instant(statusKey(b.readingStatus?.status))),
            b.isApproved ? this.translate.instant('BOOKS.APPROVED') : this.translate.instant('BOOKS.PENDING_APPROVAL'),
            (b.lendingRecord && !b.lendingRecord.isReturned) ? this.translate.instant('STATUS.LENT_OUT') : ''
          ].join(',')
        );
        // UTF-8 BOM so Excel reads Cyrillic correctly
        this.downloadFile('﻿' + [header, ...rows].join('\n'), 'bookshelf-books.csv', 'text/csv;charset=utf-8');
        this.working.set(false);
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      },
      error: () => {
        this.working.set(false);
        this.snack.open(this.translate.instant('COMMON.ERROR'), '', { duration: 3000 });
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.selectedFile.set(input.files[0]);
  }

  importJSON(): void {
    if (!this.selectedFile()) return;
    if (!confirm(this.translate.instant('ADMIN.IMPORT_CONFIRM'))) return;
    this.working.set(true);
    this.importResult.set('');
    const reader = new FileReader();
    reader.onload = (e) => {
      let parsed: any;
      try {
        parsed = JSON.parse(e.target!.result as string);
      } catch {
        this.working.set(false);
        this.importSuccess.set(false);
        this.importResult.set('Invalid JSON file');
        return;
      }
      const payload = { users: parsed.users ?? [], books: parsed.books ?? [] };
      this.adminService.importData(payload).subscribe({
        next: (result) => {
          this.working.set(false);
          this.importSuccess.set(true);
          this.importResult.set(
            `${this.translate.instant('COMMON.SUCCESS')} — ${result.usersImported} users, ${result.booksImported} books and ${result.notesImported} notes imported.`
          );
          this.selectedFile.set(null);
        },
        error: () => {
          this.working.set(false);
          this.importSuccess.set(false);
          this.importResult.set(this.translate.instant('COMMON.ERROR'));
        }
      });
    };
    reader.readAsText(this.selectedFile()!);
  }

  private downloadFile(content: string, filename: string, type: string): void {
    const blob = new Blob([content], { type });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}
