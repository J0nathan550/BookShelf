import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { statusKey } from '../../../core/utils/status.utils';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule } from '@ngx-translate/core';
import { BookService } from '../../../core/services/book.service';
import { BookDto } from '../../../core/models/book.model';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

type FilterType = 'all' | 'available' | 'want' | 'reading' | 'finished' | 'lent';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatChipsModule, MatProgressSpinnerModule,
    MatTabsModule, MatSelectModule, MatTooltipModule, TranslateModule
  ],
  template: `
    <div class="page-container">
      <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; flex-wrap:wrap; gap:8px">
        <h1 style="margin:0">{{ 'NAV.LIBRARY' | translate }}</h1>
        <button mat-raised-button color="primary" routerLink="/books/new">
          <mat-icon>add</mat-icon>
          {{ 'BOOKS.ADD_BOOK' | translate }}
        </button>
      </div>

      <mat-form-field style="width:100%; margin-bottom:8px">
        <mat-label>{{ 'BOOKS.SEARCH_PLACEHOLDER' | translate }}</mat-label>
        <input matInput [(ngModel)]="searchTerm" (ngModelChange)="onSearch($event)">
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>

      <div class="filter-chips-row">
        @for (f of filters; track f.key) {
          <button mat-stroked-button [color]="activeFilter() === f.key ? 'primary' : ''"
            (click)="setFilter(f.key)"
            [style.font-weight]="activeFilter() === f.key ? '700' : 'normal'">
            {{ f.label | translate }}
          </button>
        }
      </div>

      <mat-tab-group (selectedIndexChange)="onTabChange($event)">
        <mat-tab [label]="'BOOKS.LIBRARY_TAB' | translate">
          @if (loading()) {
            <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
          } @else if (filteredBooks().length === 0) {
            <p style="text-align:center; color:#9e9e9e; padding:40px">{{ 'BOOKS.NO_BOOKS' | translate }}</p>
          } @else {
            <div class="book-grid" style="margin-top:16px">
              @for (book of filteredBooks(); track book.id) {
                <mat-card class="book-card" [routerLink]="['/books', book.id]">
                  <div class="book-cover">
                    @if (book.coverImageUrl) {
                      <img [src]="resolveCoverUrl(book.coverImageUrl)" [alt]="book.title" style="width:100%; height:100%; object-fit:cover">
                    } @else {
                      <mat-icon style="font-size:64px; width:64px; height:64px; color:#bdbdbd">menu_book</mat-icon>
                    }
                  </div>
                  <mat-card-content style="padding:8px">
                    <div style="display:flex; gap:4px; flex-wrap:wrap; margin-bottom:4px">
                      @if (!book.isApproved) {
                        <span class="pending-badge">{{ 'BOOKS.PENDING_APPROVAL' | translate }}</span>
                      }
                      @if (book.lendingRecord && !book.lendingRecord.isReturned) {
                        <span class="lent-badge">{{ 'BOOKS.LENT_BADGE' | translate }}</span>
                      }
                    </div>
                    <div class="book-title" [matTooltip]="book.title">{{ book.title }}</div>
                    <div class="book-author">{{ book.author }}</div>
                    <div style="font-size:11px; color:#3949ab; margin-top:2px">{{ getStatusKey(book.readingStatus?.status) | translate }}</div>
                  </mat-card-content>
                </mat-card>
              }
            </div>
          }
        </mat-tab>
        @if (pendingBooks().length > 0 || auth.isAdmin()) {
          <mat-tab [label]="'BOOKS.PENDING_TAB' | translate">
            @if (loading()) {
              <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
            } @else if (pendingBooks().length === 0) {
              <p style="text-align:center; color:#9e9e9e; padding:40px">{{ 'BOOKS.NO_BOOKS' | translate }}</p>
            } @else {
              <div class="book-grid" style="margin-top:16px">
                @for (book of pendingBooks(); track book.id) {
                  <mat-card class="book-card" [routerLink]="['/books', book.id]">
                    <div class="book-cover">
                      @if (book.coverImageUrl) {
                        <img [src]="resolveCoverUrl(book.coverImageUrl)" [alt]="book.title" style="width:100%; height:100%; object-fit:cover">
                      } @else {
                        <mat-icon style="font-size:64px; width:64px; height:64px; color:#bdbdbd">menu_book</mat-icon>
                      }
                    </div>
                    <mat-card-content style="padding:8px">
                      <span class="pending-badge">{{ 'BOOKS.PENDING_APPROVAL' | translate }}</span>
                      <div class="book-title" style="margin-top:4px">{{ book.title }}</div>
                      <div class="book-author">{{ book.author }}</div>
                      @if (book.submittedByName) {
                        <div style="font-size:11px; color:#616161">{{ 'BOOKS.SUBMITTER' | translate }}: {{ book.submittedByName }}</div>
                      }
                    </mat-card-content>
                  </mat-card>
                }
              </div>
            }
          </mat-tab>
        }
      </mat-tab-group>
    </div>
  `
})
export class BookListComponent implements OnInit {
  books = signal<BookDto[]>([]);
  pendingBooks = signal<BookDto[]>([]);
  loading = signal(false);
  activeFilter = signal<FilterType>('all');
  searchTerm = '';
  baseUrl = environment.apiUrl.replace('/api', '');

  filters: { key: FilterType; label: string }[] = [
    { key: 'all', label: 'BOOKS.FILTER_ALL' },
    { key: 'available', label: 'BOOKS.FILTER_AVAILABLE' },
    { key: 'want', label: 'BOOKS.FILTER_WANT' },
    { key: 'reading', label: 'BOOKS.FILTER_READING' },
    { key: 'finished', label: 'BOOKS.FILTER_FINISHED' },
    { key: 'lent', label: 'BOOKS.FILTER_LENT' }
  ];

  filteredBooks = computed(() => {
    const all = this.books();
    const f = this.activeFilter();
    switch (f) {
      case 'available': return all.filter(b => !b.lendingRecord || b.lendingRecord.isReturned);
      case 'want': return all.filter(b => b.readingStatus?.status === 'Want to Read');
      case 'reading': return all.filter(b => b.readingStatus?.status === 'Currently Reading');
      case 'finished': return all.filter(b => b.readingStatus?.status === 'Finished');
      case 'lent': return all.filter(b => b.lendingRecord && !b.lendingRecord.isReturned);
      default: return all;
    }
  });

  constructor(public auth: AuthService, private bookService: BookService) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading.set(true);
    this.bookService.getBooks().subscribe({
      next: books => {
        this.books.set(books);
        this.pendingBooks.set(books.filter(b => !b.isApproved));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onSearch(term: string): void {
    if (!term.trim()) { this.loadBooks(); return; }
    this.loading.set(true);
    this.bookService.searchBooks(term).subscribe({
      next: books => {
        this.books.set(books);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  setFilter(f: FilterType): void {
    this.activeFilter.set(f);
  }

  onTabChange(idx: number): void {}

  getStatusKey = statusKey;

  resolveCoverUrl(url: string): string {
    if (url.startsWith('http')) return url;
    return `${this.baseUrl}${url}`;
  }
}
