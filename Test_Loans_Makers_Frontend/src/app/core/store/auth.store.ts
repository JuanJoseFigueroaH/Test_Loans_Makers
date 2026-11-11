import { computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { User } from '../models/user.model';

export interface AuthState {
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

const storedToken = sessionStorage.getItem('auth_token');
const storedUser = sessionStorage.getItem('auth_user');

const initialState: AuthState = {
  user: storedUser ? JSON.parse(storedUser) : null,
  token: storedToken,
  loading: false,
  error: null,
};

export const AuthStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),
  withComputed((store) => ({
    isAuthenticated: computed(() => store.token() !== null && store.user() !== null),
    isAdmin: computed(() => store.user()?.role === 'ROLE_ADMIN'),
    isUser: computed(() => store.user()?.role === 'ROLE_USER'),
    userEmail: computed(() => store.user()?.email ?? ''),
    userRole: computed(() => store.user()?.role ?? ''),
  })),
  withMethods((store) => ({
    setLoading(): void {
      patchState(store, { loading: true, error: null });
    },
    setAuthenticated(user: User, token: string): void {
      sessionStorage.setItem('auth_token', token);
      sessionStorage.setItem('auth_user', JSON.stringify(user));
      patchState(store, { user, token, loading: false, error: null });
    },
    setError(error: string): void {
      patchState(store, { loading: false, error });
    },
    logout(): void {
      sessionStorage.removeItem('auth_token');
      sessionStorage.removeItem('auth_user');
      patchState(store, { user: null, token: null, loading: false, error: null });
    },
    clearError(): void {
      patchState(store, { error: null });
    },
  }))
);
