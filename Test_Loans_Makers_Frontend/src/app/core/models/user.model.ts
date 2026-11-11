export interface User {
  id: string;
  email: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  code?: string;
  errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

export type UserRole = 'ROLE_USER' | 'ROLE_ADMIN';
