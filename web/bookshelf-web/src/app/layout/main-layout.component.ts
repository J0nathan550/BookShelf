import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule,
    MatSidenavModule, MatListModule, TranslateModule
  ],
  template: `
    <mat-toolbar color="primary" class="app-toolbar">
      <a routerLink="/books" style="color:inherit; text-decoration:none; font-size:20px; font-weight:500; letter-spacing:0.5px">
        {{ 'APP_TITLE' | translate }}
      </a>
      <span class="spacer"></span>
      <nav class="nav-links">
        <a mat-button routerLink="/books" routerLinkActive="active-link">
          <mat-icon>library_books</mat-icon>
          {{ 'NAV.LIBRARY' | translate }}
        </a>
        <a mat-button routerLink="/statistics" routerLinkActive="active-link">
          <mat-icon>bar_chart</mat-icon>
          {{ 'NAV.STATISTICS' | translate }}
        </a>
        @if (auth.isAdmin()) {
          <a mat-button routerLink="/admin" routerLinkActive="active-link">
            <mat-icon>admin_panel_settings</mat-icon>
            {{ 'NAV.ADMIN' | translate }}
          </a>
        }
      </nav>
      <button mat-icon-button [matMenuTriggerFor]="userMenu">
        <mat-icon>account_circle</mat-icon>
      </button>
      <mat-menu #userMenu="matMenu">
        <button mat-menu-item routerLink="/account">
          <mat-icon>person</mat-icon>
          <span>{{ 'NAV.ACCOUNT' | translate }}</span>
        </button>
        <button mat-menu-item [matMenuTriggerFor]="langMenu">
          <mat-icon>language</mat-icon>
          <span>{{ 'COMMON.LANGUAGE' | translate }}</span>
        </button>
        <button mat-menu-item (click)="auth.logout()">
          <mat-icon>logout</mat-icon>
          <span>{{ 'NAV.LOGOUT' | translate }}</span>
        </button>
      </mat-menu>
      <mat-menu #langMenu="matMenu">
        <button mat-menu-item (click)="setLang('en')">English</button>
        <button mat-menu-item (click)="setLang('uk')">Українська</button>
      </mat-menu>
    </mat-toolbar>
    <div class="main-content">
      <router-outlet />
    </div>
  `,
  styles: [`
    .app-toolbar { position: sticky; top: 0; z-index: 100; }
    .nav-links { display: flex; align-items: center; gap: 4px; }
    .nav-links a { display: flex; align-items: center; gap: 4px; }
    .active-link { background: rgba(255,255,255,0.15); border-radius: 4px; }
    .main-content { min-height: calc(100vh - 64px); }
    @media (max-width: 600px) {
      .nav-links a span { display: none; }
    }
  `]
})
export class MainLayoutComponent {
  auth = inject(AuthService);

  constructor(private translate: TranslateService) {}

  setLang(lang: string): void {
    this.translate.use(lang);
    localStorage.setItem('lang', lang);
  }
}
