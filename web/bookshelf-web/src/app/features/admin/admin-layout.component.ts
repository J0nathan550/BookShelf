import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatListModule, MatIconModule, TranslateModule
  ],
  template: `
    <mat-sidenav-container class="admin-layout" style="height:calc(100vh - 64px)">
      <mat-sidenav mode="side" opened style="width:220px">
        <mat-nav-list>
          <a mat-list-item routerLink="dashboard" routerLinkActive="active-link">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>{{ 'ADMIN.DASHBOARD' | translate }}</span>
          </a>
          <a mat-list-item routerLink="users" routerLinkActive="active-link">
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>{{ 'ADMIN.USERS' | translate }}</span>
          </a>
          <a mat-list-item routerLink="books" routerLinkActive="active-link">
            <mat-icon matListItemIcon>library_books</mat-icon>
            <span matListItemTitle>{{ 'ADMIN.BOOKS' | translate }}</span>
          </a>
          <a mat-list-item routerLink="data" routerLinkActive="active-link">
            <mat-icon matListItemIcon>storage</mat-icon>
            <span matListItemTitle>{{ 'ADMIN.DATA' | translate }}</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content style="padding:24px; overflow:auto">
        <router-outlet />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`.active-link { background: rgba(63,81,181,0.12); }`]
})
export class AdminLayoutComponent {}
