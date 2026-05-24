import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { MainLayoutComponent } from './layout/main-layout.component';

export const routes: Routes = [
  { path: '', redirectTo: '/books', pathMatch: 'full' },
  {
    path: 'auth',
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
      { path: 'verify-email/:userId', loadComponent: () => import('./features/auth/email-verification/email-verification.component').then(m => m.EmailVerificationComponent) },
      { path: 'forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
      { path: 'reset-password', loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'books', loadComponent: () => import('./features/books/book-list/book-list.component').then(m => m.BookListComponent) },
      { path: 'books/new', loadComponent: () => import('./features/books/book-form/book-form.component').then(m => m.BookFormComponent) },
      { path: 'books/:id/edit', loadComponent: () => import('./features/books/book-form/book-form.component').then(m => m.BookFormComponent) },
      { path: 'books/:id', loadComponent: () => import('./features/books/book-detail/book-detail.component').then(m => m.BookDetailComponent) },
      { path: 'statistics', loadComponent: () => import('./features/statistics/statistics.component').then(m => m.StatisticsComponent) },
      { path: 'account', loadComponent: () => import('./features/account/account.component').then(m => m.AccountComponent) },
      {
        path: 'admin',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/admin/admin-layout.component').then(m => m.AdminLayoutComponent),
        children: [
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
          { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
          { path: 'users', loadComponent: () => import('./features/admin/users/admin-users.component').then(m => m.AdminUsersComponent) },
          { path: 'books', loadComponent: () => import('./features/admin/books/admin-books.component').then(m => m.AdminBooksComponent) },
          { path: 'data', loadComponent: () => import('./features/admin/data-management/data-management.component').then(m => m.DataManagementComponent) }
        ]
      }
    ]
  },
  { path: '**', redirectTo: '/books' }
];
