import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse, LoginDto, RegisterDto, VerifyEmailCodeDto,
  ResendVerificationCodeDto, ForgotPasswordDto, ResetPasswordCodeDto, CurrentUser
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;
  currentUser = signal<CurrentUser | null>(this.loadStoredUser());

  constructor(private http: HttpClient, private router: Router) {}

  private loadStoredUser(): CurrentUser | null {
    const stored = localStorage.getItem('currentUser');
    return stored ? JSON.parse(stored) : null;
  }

  login(dto: LoginDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, dto).pipe(
      tap(res => {
        if (res.success && res.token) {
          localStorage.setItem('token', res.token);
          const user: CurrentUser = {
            userId: res.userId!,
            email: res.email!,
            fullName: res.fullName!,
            roles: res.roles ?? []
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUser.set(user);
        }
      })
    );
  }

  register(dto: RegisterDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, dto);
  }

  verifyEmailCode(dto: VerifyEmailCodeDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/verify-email-code`, dto);
  }

  resendVerificationCode(dto: ResendVerificationCodeDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/resend-verification-code`, dto);
  }

  forgotPassword(dto: ForgotPasswordDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/forgot-password`, dto);
  }

  resetPasswordWithCode(dto: ResetPasswordCodeDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/reset-password-code`, dto);
  }

  getCurrentUser(): Observable<AuthResponse> {
    return this.http.get<AuthResponse>(`${this.baseUrl}/current-user`).pipe(
      tap(res => {
        if (res.success) {
          const user: CurrentUser = {
            userId: res.userId!,
            email: res.email!,
            fullName: res.fullName!,
            roles: res.roles ?? []
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUser.set(user);
        }
      })
    );
  }

  logout(): void {
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe();
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.currentUser()?.roles?.includes('Admin') ?? false;
  }
}
