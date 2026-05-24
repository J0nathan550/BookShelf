import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AdminService } from '../../../core/services/admin.service';
import { BookDto } from '../../../core/models/book.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-books',
  standalone: true,
  imports: [
    CommonModule, MatTabsModule, MatCardModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatSnackBarModule, TranslateModule
  ],
  template: `
    <h2>{{ 'ADMIN.BOOKS' | translate }}</h2>
    <mat-tab-group>
      <mat-tab [label]="'ADMIN.PENDING_BOOKS_TAB' | translate">
        @if (loadingPending()) {
          <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
        } @else if (pendingBooks().length === 0) {
          <p style="color:#9e9e9e; padding:20px 0">No pending books.</p>
        } @else {
          <div style="margin-top:16px">
            @for (book of pendingBooks(); track book.id) {
              <mat-card style="margin-bottom:12px; display:flex; gap:16px; flex-wrap:wrap">
                <div style="width:80px; height:110px; background:#e0e0e0; flex-shrink:0; overflow:hidden; border-radius:4px">
                  @if (book.coverImageUrl) {
                    <img [src]="resolveCoverUrl(book.coverImageUrl)" style="width:100%; height:100%; object-fit:cover">
                  } @else {
                    <div style="width:100%;height:100%;display:flex;align-items:center;justify-content:center">
                      <mat-icon style="color:#bdbdbd">menu_book</mat-icon>
                    </div>
                  }
                </div>
                <div style="flex:1">
                  <strong>{{ book.title }}</strong> — {{ book.author }}
                  @if (book.submittedByName) {
                    <div style="font-size:12px; color:#616161; margin-top:4px">{{ 'BOOKS.SUBMITTER' | translate }}: {{ book.submittedByName }}</div>
                  }
                  <div style="margin-top:8px; display:flex; gap:8px">
                    <button mat-raised-button color="primary" (click)="approve(book)">
                      <mat-icon>check</mat-icon>{{ 'ADMIN.APPROVE_BOOK' | translate }}
                    </button>
                    <button mat-stroked-button color="warn" (click)="reject(book)">
                      <mat-icon>close</mat-icon>{{ 'ADMIN.REJECT_BOOK' | translate }}
                    </button>
                  </div>
                </div>
              </mat-card>
            }
          </div>
        }
      </mat-tab>
      <mat-tab [label]="'ADMIN.ALL_BOOKS' | translate">
        @if (loadingAll()) {
          <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
        } @else {
          <div style="margin-top:16px">
            @for (book of allBooks(); track book.id) {
              <mat-card style="margin-bottom:8px; padding:8px 16px; display:flex; gap:16px; align-items:center; flex-wrap:wrap">
                <div style="flex:1; min-width:200px">
                  <strong>{{ book.title }}</strong> — {{ book.author }}
                  @if (book.submittedByName) {
                    <span style="font-size:12px; color:#616161; margin-left:8px">{{ book.submittedByName }}</span>
                  }
                </div>
                @if (book.isApproved) {
                  <span class="approved-badge">{{ 'BOOKS.APPROVED' | translate }}</span>
                } @else {
                  <span class="pending-badge">{{ 'BOOKS.PENDING_APPROVAL' | translate }}</span>
                }
              </mat-card>
            }
          </div>
        }
      </mat-tab>
    </mat-tab-group>
  `
})
export class AdminBooksComponent implements OnInit {
  pendingBooks = signal<BookDto[]>([]);
  allBooks = signal<BookDto[]>([]);
  loadingPending = signal(true);
  loadingAll = signal(true);
  baseUrl = environment.apiUrl.replace('/api', '');

  constructor(
    private adminService: AdminService,
    private snack: MatSnackBar,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.adminService.getPendingBooks().subscribe({
      next: books => { this.pendingBooks.set(books); this.loadingPending.set(false); },
      error: () => this.loadingPending.set(false)
    });
    this.adminService.getAllBooks().subscribe({
      next: books => { this.allBooks.set(books); this.loadingAll.set(false); },
      error: () => this.loadingAll.set(false)
    });
  }

  approve(book: BookDto): void {
    this.adminService.approveBook(book.id).subscribe({
      next: () => {
        this.pendingBooks.update(list => list.filter(b => b.id !== book.id));
        this.allBooks.update(list => list.map(b => b.id === book.id ? { ...b, isApproved: true } : b));
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      }
    });
  }

  reject(book: BookDto): void {
    if (!confirm(this.translate.instant('BOOKS.CONFIRM_DELETE'))) return;
    this.adminService.rejectBook(book.id).subscribe({
      next: () => {
        this.pendingBooks.update(list => list.filter(b => b.id !== book.id));
        this.allBooks.update(list => list.filter(b => b.id !== book.id));
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      }
    });
  }

  resolveCoverUrl(url: string): string {
    if (url.startsWith('http')) return url;
    return `${this.baseUrl}${url}`;
  }
}
