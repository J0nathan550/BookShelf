export interface AuthResponse {
  success: boolean;
  message?: string;
  token?: string;
  userId?: string;
  email?: string;
  fullName?: string;
  roles?: string[];
}

export interface LoginDto {
  email: string;
  password: string;
  rememberMe: boolean;
}

export interface RegisterDto {
  email: string;
  password: string;
  fullName: string;
}

export interface VerifyEmailCodeDto {
  userId: string;
  code: string;
}

export interface ResendVerificationCodeDto {
  userId: string;
}

export interface ForgotPasswordDto {
  email: string;
}

export interface ResetPasswordCodeDto {
  email: string;
  code: string;
  newPassword: string;
}

export interface CurrentUser {
  userId: string;
  email: string;
  fullName: string;
  roles: string[];
}
