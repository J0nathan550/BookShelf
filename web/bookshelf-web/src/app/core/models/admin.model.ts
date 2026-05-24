export interface AdminUserDto {
  id: string;
  email: string;
  fullName: string;
  isActive: boolean;
  registrationDate: string;
  bookCount?: number;
}

export interface AdminDashboardDto {
  totalUsers: number;
  totalBooks: number;
  pendingBooksCount: number;
  recentRegistrations: AdminUserDto[];
}
