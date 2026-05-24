import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TranslateModule } from '@ngx-translate/core';
import { StatisticsService } from '../../core/services/statistics.service';
import { StatisticsDto } from '../../core/models/statistics.model';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule, MatTableModule, TranslateModule],
  template: `
    <div class="page-container">
      <h1>{{ 'STATS.TITLE' | translate }}</h1>
      @if (loading()) {
        <div style="text-align:center; padding:40px"><mat-spinner diameter="40"></mat-spinner></div>
      } @else if (stats()) {
        <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(160px, 1fr)); gap:16px; margin-bottom:24px">
          <mat-card class="stat-card">
            <div class="stat-value">{{ stats()!.totalBooks }}</div>
            <div class="stat-label">{{ 'STATS.TOTAL_BOOKS' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ stats()!.wantToRead }}</div>
            <div class="stat-label">{{ 'STATS.WANT_TO_READ' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ stats()!.currentlyReading }}</div>
            <div class="stat-label">{{ 'STATS.CURRENTLY_READING' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ stats()!.finished }}</div>
            <div class="stat-label">{{ 'STATS.FINISHED' | translate }}</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ stats()!.booksReadThisYear }}</div>
            <div class="stat-label">{{ 'STATS.THIS_YEAR' | translate }}</div>
          </mat-card>
        </div>

        <mat-card>
          <mat-card-header>
            <mat-card-title>{{ 'STATS.GENRE_DISTRIBUTION' | translate }}</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="genreRows()" class="genre-table full-width">
              <ng-container matColumnDef="genre">
                <th mat-header-cell *matHeaderCellDef>{{ 'STATS.GENRE' | translate }}</th>
                <td mat-cell *matCellDef="let row">{{ row.genre }}</td>
              </ng-container>
              <ng-container matColumnDef="count">
                <th mat-header-cell *matHeaderCellDef>{{ 'STATS.COUNT' | translate }}</th>
                <td mat-cell *matCellDef="let row">{{ row.count }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="['genre','count']"></tr>
              <tr mat-row *matRowDef="let row; columns: ['genre','count']"></tr>
            </table>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `
})
export class StatisticsComponent implements OnInit {
  stats = signal<StatisticsDto | null>(null);
  loading = signal(true);

  genreRows = signal<{ genre: string; count: number }[]>([]);

  constructor(private statsService: StatisticsService) {}

  ngOnInit(): void {
    this.statsService.getStatistics().subscribe({
      next: s => {
        this.stats.set(s);
        this.genreRows.set(
          Object.entries(s.genreDistribution ?? {})
            .map(([genre, count]) => ({ genre, count }))
            .sort((a, b) => b.count - a.count)
        );
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
