import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BookService } from '../../../core/services/book.service';
import { GenreDto, FormatDto } from '../../../core/models/book.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatSelectModule, MatIconModule,
    MatProgressSpinnerModule, MatSnackBarModule, TranslateModule
  ],
  template: `
    <div class="page-container" style="max-width:600px">
      <button mat-button routerLink="/books" style="margin-bottom:8px">
        <mat-icon>arrow_back</mat-icon>{{ 'COMMON.BACK' | translate }}
      </button>
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ (isEdit ? 'BOOKS.EDIT_BOOK' : 'BOOKS.ADD_BOOK') | translate }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()" style="display:flex; flex-direction:column; gap:4px; margin-top:16px">
            <mat-form-field class="full-width">
              <mat-label>{{ 'BOOKS.TITLE' | translate }}</mat-label>
              <input matInput formControlName="title">
              @if (form.get('title')?.hasError('required') && form.get('title')?.touched) {
                <mat-error>{{ 'BOOKS.TITLE_REQUIRED' | translate }}</mat-error>
              }
            </mat-form-field>
            <mat-form-field class="full-width">
              <mat-label>{{ 'BOOKS.AUTHOR' | translate }}</mat-label>
              <input matInput formControlName="author">
              @if (form.get('author')?.hasError('required') && form.get('author')?.touched) {
                <mat-error>{{ 'BOOKS.AUTHOR_REQUIRED' | translate }}</mat-error>
              }
            </mat-form-field>
            <mat-form-field class="full-width">
              <mat-label>{{ 'BOOKS.GENRE' | translate }}</mat-label>
              <mat-select formControlName="genreId">
                <mat-option [value]="null">—</mat-option>
                @for (g of genres(); track g.id) {
                  <mat-option [value]="g.id">{{ g.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field class="full-width">
              <mat-label>{{ 'BOOKS.FORMAT' | translate }}</mat-label>
              <mat-select formControlName="formatId">
                <mat-option [value]="null">—</mat-option>
                @for (f of formats(); track f.id) {
                  <mat-option [value]="f.id">{{ f.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
            <mat-form-field class="full-width">
              <mat-label>{{ 'BOOKS.PAGES' | translate }}</mat-label>
              <input matInput type="number" formControlName="pages" min="1">
              @if (form.get('pages')?.hasError('required') && form.get('pages')?.touched) {
                <mat-error>{{ 'BOOKS.PAGES_REQUIRED' | translate }}</mat-error>
              }
            </mat-form-field>
            <mat-form-field class="full-width">
              <mat-label>{{ 'BOOKS.COVER_URL' | translate }}</mat-label>
              <input matInput formControlName="coverImageUrl" placeholder="https://...">
            </mat-form-field>

            <!-- Cover upload -->
            <div style="margin-bottom:8px">
              <button mat-stroked-button type="button" (click)="fileInput.click()">
                <mat-icon>upload</mat-icon>{{ 'BOOKS.UPLOAD_COVER' | translate }}
              </button>
              <input #fileInput type="file" accept="image/*" style="display:none" (change)="onFileChange($event)">
              @if (selectedFile()) {
                <span style="margin-left:8px; font-size:13px; color:#616161">{{ selectedFile()!.name }}</span>
              }
            </div>

            <!-- ISBN lookup -->
            <div style="display:flex; gap:8px; align-items:center; margin-bottom:8px">
              <mat-form-field style="flex:1">
                <mat-label>ISBN</mat-label>
                <input matInput [(ngModel)]="isbnInput" [ngModelOptions]="{standalone:true}">
              </mat-form-field>
              <button mat-stroked-button type="button" (click)="lookupIsbn()">
                <mat-icon>search</mat-icon>Lookup
              </button>
            </div>

            @if (errorMsg) { <p class="error-message">{{ errorMsg }}</p> }
            <div style="display:flex; gap:8px; margin-top:8px">
              <button mat-raised-button color="primary" type="submit" [disabled]="saving()">
                @if (saving()) { <mat-spinner diameter="20" style="display:inline-block"></mat-spinner> }
                @else { {{ 'BOOKS.SAVE' | translate }} }
              </button>
              <button mat-stroked-button type="button" routerLink="/books">{{ 'BOOKS.CANCEL' | translate }}</button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class BookFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private bookService = inject(BookService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snack = inject(MatSnackBar);
  private translate = inject(TranslateService);

  form = this.fb.group({
    title: ['', Validators.required],
    author: ['', Validators.required],
    genreId: [null as number | null],
    formatId: [null as number | null],
    pages: [null as number | null, Validators.required],
    coverImageUrl: ['' as string | null]
  });
  isEdit = false;
  bookId: number | null = null;
  genres = signal<GenreDto[]>([]);
  formats = signal<FormatDto[]>([]);
  saving = signal(false);
  selectedFile = signal<File | null>(null);
  isbnInput = '';
  errorMsg = '';
  baseUrl = environment.apiUrl.replace('/api', '');

  ngOnInit(): void {
    this.bookService.getGenres().subscribe(g => this.genres.set(g));
    this.bookService.getFormats().subscribe(f => this.formats.set(f));

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEdit = true;
      this.bookId = Number(id);
      this.bookService.getBook(this.bookId).subscribe(book => {
        this.form.patchValue({
          title: book.title,
          author: book.author,
          genreId: book.genre?.id ?? null,
          formatId: book.format?.id ?? null,
          pages: book.pages ?? null,
          coverImageUrl: book.coverImageUrl ?? ''
        });
      });
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile.set(input.files[0]);
      this.form.patchValue({ coverImageUrl: '' });
    }
  }

  lookupIsbn(): void {
    if (!this.isbnInput.trim()) return;
    this.bookService.lookupIsbn(this.isbnInput.trim()).subscribe({
      next: data => {
        if (data.title) this.form.patchValue({ title: data.title });
        if (data.author) this.form.patchValue({ author: data.author });
        if (data.pages) this.form.patchValue({ pages: data.pages });
        if (data.coverImageUrl) this.form.patchValue({ coverImageUrl: data.coverImageUrl });
      },
      error: () => this.errorMsg = 'ISBN not found'
    });
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.errorMsg = '';
    const val = this.form.value;
    const dto = {
      title: val.title!,
      author: val.author!,
      genreId: val.genreId ?? undefined,
      formatId: val.formatId ?? undefined,
      pages: val.pages ?? undefined,
      coverImageUrl: val.coverImageUrl || undefined
    };

    if (this.isEdit && this.bookId) {
      this.bookService.updateBook(this.bookId, dto).subscribe({
        next: book => {
          if (this.selectedFile()) {
            this.uploadCover(book.id);
          } else {
            this.saving.set(false);
            this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
            this.router.navigate(['/books', book.id]);
          }
        },
        error: err => {
          this.saving.set(false);
          this.errorMsg = err.error?.message ?? 'Save failed';
        }
      });
    } else {
      this.bookService.createBook(dto).subscribe({
        next: book => {
          if (this.selectedFile()) {
            this.uploadCover(book.id);
          } else {
            this.saving.set(false);
            this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
            this.router.navigate(['/books', book.id]);
          }
        },
        error: err => {
          this.saving.set(false);
          this.errorMsg = err.error?.message ?? 'Save failed';
        }
      });
    }
  }

  private uploadCover(bookId: number): void {
    this.bookService.uploadCover(bookId, this.selectedFile()!).subscribe({
      next: () => {
        this.saving.set(false);
        this.snack.open(this.translate.instant('COMMON.SUCCESS'), '', { duration: 2000 });
        this.router.navigate(['/books', bookId]);
      },
      error: () => {
        this.saving.set(false);
        this.router.navigate(['/books', bookId]);
      }
    });
  }
}
