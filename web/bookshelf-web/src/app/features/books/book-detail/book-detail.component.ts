import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BookService } from '../../../core/services/book.service';
import { AdminService } from '../../../core/services/admin.service';
import { AuthService } from '../../../core/services/auth.service';
import { BookDto, BookNoteDto } from '../../../core/models/book.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatDatepickerModule, MatNativeDateModule, MatProgressSpinnerModule,
    MatDividerModule, MatSnackBarModule, TranslateModule
  ],
  template: `
    <div class="page-container">
      <button mat-button routerLink="/books" style="margin-bottom:8px">
        <mat-icon>arrow_back</mat-icon>{{ 'COMMON.BACK' | translate }}
      </button>

      @if (loading()) {
        <div style="text-align:center; padding:60px"><mat-spinner diameter="50"></mat-spinner></div>
      } @else if (book()) {
        <div style="display:flex; gap:24px; flex-wrap:wrap">
          <!-- Cover -->
          <div style="min-width:200px; max-width:240px">
            @if (book()!.coverImageUrl) {
              <img [src]="resolveCoverUrl(book()!.coverImageUrl!)" [alt]="book()!.title"
                style="width:100%; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,0.2)">
            } @else {
              <div style="width:100%; height:300px; background:#e0e0e0; display:flex; align-items:center; justify-content:center; border-radius:8px">
                <mat-icon style="font-size:80px; width:80px; height:80px; color:#bdbdbd">menu_book</mat-icon>
              </div>
            }
            @if (book()!.isApproved) {
              <span class="approved-badge" style="display:block; text-align:center; margin-top:8px">{{ 'BOOKS.APPROVED' | translate }}</span>
            } @else {
              <span class="pending-badge" style="display:block; text-align:center; margin-top:8px">{{ 'BOOKS.PENDING_APPROVAL' | translate }}</span>
            }
            @if (auth.isAdmin() && !book()!.isApproved) {
              <button mat-raised-button color="primary" style="width:100%; margin-top:8px" (click)="approveBook()">
                {{ 'ADMIN.APPROVE_BOOK' | translate }}
              </button>
            }
          </div>

          <!-- Details -->
          <div style="flex:1; min-width:280px">
            <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:8px">
              <div>
                <h1 style="margin:0; font-size:24px">{{ book()!.title }}</h1>
                <p style="margin:4px 0; color:#616161">{{ book()!.author }}</p>
              </div>
              <div style="display:flex; gap:8px">
                <button mat-icon-button [routerLink]="['/books', book()!.id, 'edit']" color="primary">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button color="warn" (click)="deleteBook()">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </div>

            <mat-divider style="margin:12px 0"></mat-divider>
            <div style="display:flex; flex-wrap:wrap; gap:16px; font-size:14px; color:#616161">
              @if (book()!.genre) { <span><strong>{{ 'BOOKS.GENRE' | translate }}:</strong> {{ book()!.genre!.name }}</span> }
              @if (book()!.format) { <span><strong>{{ 'BOOKS.FORMAT' | translate }}:</strong> {{ book()!.format!.name }}</span> }
              @if (book()!.pages) { <span><strong>{{ 'BOOKS.PAGES' | translate }}:</strong> {{ book()!.pages }}</span> }
              <span><strong>{{ 'BOOKS.DATE_ADDED' | translate }}:</strong> {{ book()!.dateAdded | date:'mediumDate' }}</span>
            </div>

            <!-- Lending state (read-only) -->
            @if (book()!.lendingRecord && !book()!.lendingRecord!.isReturned) {
              <mat-card style="margin-top:16px; background:#fff3e0">
                <mat-card-content>
                  <span class="lent-badge">{{ 'BOOKS.LENT_BADGE' | translate }}</span>
                  <p style="margin:4px 0"><strong>{{ 'BOOKS.LENT_TO' | translate }}:</strong> {{ book()!.lendingRecord!.borrowerName }}</p>
                  <p style="margin:4px 0"><strong>{{ 'BOOKS.LENT_DATE' | translate }}:</strong> {{ book()!.lendingRecord!.lendingDate | date:'mediumDate' }}</p>
                </mat-card-content>
              </mat-card>
            }

            <!-- Reading Status -->
            <mat-card style="margin-top:16px">
              <mat-card-header><mat-card-title>{{ 'BOOKS.STATUS' | translate }}</mat-card-title></mat-card-header>
              <mat-card-content>
                <form [formGroup]="statusForm" (ngSubmit)="saveStatus()">
                  <mat-form-field style="width:100%">
                    <mat-label>{{ 'BOOKS.STATUS' | translate }}</mat-label>
                    <mat-select formControlName="status">
                      <mat-option value="Available" disabled>{{ 'STATUS.AVAILABLE' | translate }}</mat-option>
                      <mat-option value="Lent Out" disabled>{{ 'STATUS.LENT_OUT' | translate }}</mat-option>
                      <mat-option value="Want to Read">{{ 'STATUS.WANT_TO_READ' | translate }}</mat-option>
                      <mat-option value="Currently Reading">{{ 'STATUS.CURRENTLY_READING' | translate }}</mat-option>
                      <mat-option value="Finished">{{ 'STATUS.FINISHED' | translate }}</mat-option>
                    </mat-select>
                  </mat-form-field>
                  @if (statusForm.value.status === 'Finished') {
                    <mat-form-field style="width:100%">
                      <mat-label>{{ 'BOOKS.RATING' | translate }} (1-5)</mat-label>
                      <input matInput type="number" formControlName="rating" min="1" max="5">
                    </mat-form-field>
                    <mat-form-field style="width:100%">
                      <mat-label>{{ 'BOOKS.COMPLETION_DATE' | translate }}</mat-label>
                      <input matInput [matDatepicker]="picker" formControlName="completionDate">
                      <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                      <mat-datepicker #picker></mat-datepicker>
                    </mat-form-field>
                  }
                  <button mat-raised-button color="primary" type="submit" [disabled]="savingStatus()">
                    {{ 'BOOKS.SAVE' | translate }}
                  </button>
                </form>
                @if (book()!.averageRating != null) {
                  <p style="margin-top:8px; font-size:13px; color:#616161">
                    {{ 'BOOKS.AVERAGE_RATING' | translate }}: <strong>{{ book()!.averageRating | number:'1.1-1' }}/5</strong>
                  </p>
                }
              </mat-card-content>
            </mat-card>

            <!-- Notes -->
            <mat-card style="margin-top:16px">
              <mat-card-header><mat-card-title>{{ 'BOOKS.NOTES' | translate }}</mat-card-title></mat-card-header>
              <mat-card-content>
                @for (note of book()!.notes ?? []; track note.id) {
                  <div style="border:1px solid #e0e0e0; border-radius:4px; padding:8px; margin-bottom:8px">
                    @if (editingNoteId() === note.id) {
                      <mat-form-field style="width:100%">
                        <textarea matInput [(ngModel)]="editNoteText" rows="3"></textarea>
                      </mat-form-field>
                      <button mat-button color="primary" (click)="saveNote(note.id)">{{ 'BOOKS.SAVE' | translate }}</button>
                      <button mat-button (click)="cancelEditNote()">{{ 'BOOKS.CANCEL' | translate }}</button>
                    } @else {
                      <p style="margin:0 0 4px">{{ note.noteText }}</p>
                      @if (auth.isAdmin() && note.authorName) {
                        <span style="font-size:11px; color:#3949ab">{{ note.authorName }} · {{ note.createdDate | date:'short' }}</span>
                      } @else {
                        <span style="font-size:11px; color:#9e9e9e">{{ note.createdDate | date:'short' }}</span>
                      }
                      <div style="margin-top:4px">
                        <button mat-icon-button (click)="startEditNote(note)"><mat-icon>edit</mat-icon></button>
                        <button mat-icon-button color="warn" (click)="deleteNote(note.id)"><mat-icon>delete</mat-icon></button>
                      </div>
                    }
                  </div>
                }
                <mat-form-field style="width:100%; margin-top:8px">
                  <mat-label>{{ 'BOOKS.ADD_NOTE' | translate }}</mat-label>
                  <textarea matInput [(ngModel)]="newNoteText" rows="2"></textarea>
                </mat-form-field>
                <button mat-stroked-button color="primary" (click)="addNote()" [disabled]="!newNoteText.trim()">
                  <mat-icon>add</mat-icon>{{ 'BOOKS.ADD_NOTE' | translate }}
                </button>
              </mat-card-content>
            </mat-card>

            <!-- Admin: lending history -->
            @if (auth.isAdmin() && (book()!.lendingHistory?.length ?? 0) > 0) {
              <mat-card style="margin-top:16px">
                <mat-card-header><mat-card-title>Lending History</mat-card-title></mat-card-header>
                <mat-card-content>
                  @for (rec of book()!.lendingHistory; track rec.id) {
                    <div style="font-size:13px; padding:4px 0; border-bottom:1px solid #f0f0f0">
                      <strong>{{ rec.borrowerName }}</strong>
                      — {{ rec.lendingDate | date:'shortDate' }}
                      @if (rec.returnDate) { → {{ rec.returnDate | date:'shortDate' }} }
                      @if (!rec.isReturned) { <span class="lent-badge" style="font-size:10px">Active</span> }
                    </div>
                  }
                </mat-card-content>
              </mat-card>
            }
          </div>
        </div>
      }
    </div>
  `
})
export class BookDetailComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private bookService = inject(BookService);
  private adminService = inject(AdminService);
  public auth = inject(AuthService);
  private snack = inject(MatSnackBar);
  private translate = inject(TranslateService);

  book = signal<BookDto | null>(null);
  loading = signal(true);
  savingStatus = signal(false);
  editingNoteId = signal<number | null>(null);
  editNoteText = '';
  newNoteText = '';
  baseUrl = environment.apiUrl.replace('/api', '');

  statusForm = this.fb.group({
    status: [''],
    rating: [null as number | null],
    completionDate: [null as Date | null]
  });

  private bookId = 0;

  ngOnInit(): void {
    this.bookId = Number(this.route.snapshot.params['id']);
    this.loadBook();

    this.statusForm.get('status')!.valueChanges.subscribe(status => {
      if (status === 'Finished' && !this.statusForm.value.completionDate) {
        this.statusForm.patchValue({ completionDate: new Date() }, { emitEvent: false });
      }
    });
  }

  private loadBook(): void {
    this.bookService.getBook(this.bookId).subscribe({
      next: book => {
        this.book.set(book);
        this.loading.set(false);
        this.statusForm.patchValue({
          status: book.readingStatus?.status ?? '',
          rating: book.readingStatus?.rating ?? null,
          completionDate: book.readingStatus?.completionDate ? new Date(book.readingStatus.completionDate) : null
        });
      },
      error: () => this.loading.set(false)
    });
  }

  saveStatus(): void {
    const b = this.book();
    if (!b) return;
    this.savingStatus.set(true);
    const { status, rating, completionDate } = this.statusForm.value;
    const isFinished = status === 'Finished';
    this.bookService.setReadingStatus(b.id, {
      status: status!,
      rating: isFinished ? (rating ?? undefined) : undefined,
      completionDate: isFinished && completionDate ? (completionDate as Date).toISOString() : undefined
    }).subscribe({
      next: () => {
        this.savingStatus.set(false);
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
        this.loadBook();
      },
      error: () => this.savingStatus.set(false)
    });
  }

  deleteBook(): void {
    if (!confirm(this.translate.instant('BOOKS.CONFIRM_DELETE'))) return;
    this.bookService.deleteBook(this.book()!.id).subscribe({
      next: () => this.router.navigate(['/books']),
      error: () => {}
    });
  }

  addNote(): void {
    if (!this.newNoteText.trim()) return;
    this.bookService.addNote(this.book()!.id, { noteText: this.newNoteText }).subscribe({
      next: () => {
        this.newNoteText = '';
        this.loadBook();
      }
    });
  }

  startEditNote(note: BookNoteDto): void {
    this.editingNoteId.set(note.id);
    this.editNoteText = note.noteText;
  }

  cancelEditNote(): void {
    this.editingNoteId.set(null);
    this.editNoteText = '';
  }

  saveNote(noteId: number): void {
    this.bookService.updateNote(noteId, { noteText: this.editNoteText }).subscribe({
      next: () => {
        this.cancelEditNote();
        this.loadBook();
      }
    });
  }

  deleteNote(noteId: number): void {
    if (!confirm(this.translate.instant('BOOKS.DELETE_NOTE'))) return;
    this.bookService.deleteNote(noteId).subscribe({
      next: () => this.loadBook()
    });
  }

  approveBook(): void {
    const b = this.book();
    if (!b) return;
    this.adminService.approveBook(b.id).subscribe({
      next: () => {
        this.book.update(bk => bk ? { ...bk, isApproved: true } : bk);
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
      }
    });
  }

  resolveCoverUrl(url: string): string {
    if (url.startsWith('http')) return url;
    return `${this.baseUrl}${url}`;
  }
}
