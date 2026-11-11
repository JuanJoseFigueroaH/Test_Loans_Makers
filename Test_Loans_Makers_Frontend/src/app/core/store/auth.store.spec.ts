import { TestBed } from '@angular/core/testing';
import { AuthStore } from './auth.store';
import { User } from '../models/user.model';

describe('AuthStore', () => {
  let store: InstanceType<typeof AuthStore>;

  const mockUser: User = {
    id: '1',
    email: 'test@test.com',
    role: 'ROLE_USER',
    enabled: true,
    createdAt: '2024-01-01T00:00:00Z',
  };

  const mockAdmin: User = {
    id: '2',
    email: 'admin@test.com',
    role: 'ROLE_ADMIN',
    enabled: true,
    createdAt: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [AuthStore],
    });
    store = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  describe('initial state', () => {
    it('should have null user', () => {
      expect(store.user()).toBeNull();
    });

    it('should have null token', () => {
      expect(store.token()).toBeNull();
    });

    it('should not be loading', () => {
      expect(store.loading()).toBeFalse();
    });

    it('should have null error', () => {
      expect(store.error()).toBeNull();
    });

    it('should not be authenticated', () => {
      expect(store.isAuthenticated()).toBeFalse();
    });

    it('should not be admin', () => {
      expect(store.isAdmin()).toBeFalse();
    });

    it('should not be user', () => {
      expect(store.isUser()).toBeFalse();
    });

    it('should have empty email', () => {
      expect(store.userEmail()).toBe('');
    });

    it('should have empty role', () => {
      expect(store.userRole()).toBe('');
    });
  });

  describe('setLoading', () => {
    it('should set loading to true and clear error', () => {
      store.setError('previous error');
      store.setLoading();

      expect(store.loading()).toBeTrue();
      expect(store.error()).toBeNull();
    });
  });

  describe('setAuthenticated', () => {
    it('should set user and token', () => {
      store.setAuthenticated(mockUser, 'jwt-token');

      expect(store.user()).toEqual(mockUser);
      expect(store.token()).toBe('jwt-token');
      expect(store.loading()).toBeFalse();
      expect(store.error()).toBeNull();
    });

    it('should mark as authenticated', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      expect(store.isAuthenticated()).toBeTrue();
    });

    it('should detect ROLE_USER', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      expect(store.isUser()).toBeTrue();
      expect(store.isAdmin()).toBeFalse();
    });

    it('should detect ROLE_ADMIN', () => {
      store.setAuthenticated(mockAdmin, 'jwt-token');
      expect(store.isAdmin()).toBeTrue();
      expect(store.isUser()).toBeFalse();
    });

    it('should provide user email', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      expect(store.userEmail()).toBe('test@test.com');
    });

    it('should provide user role', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      expect(store.userRole()).toBe('ROLE_USER');
    });
  });

  describe('setError', () => {
    it('should set error and stop loading', () => {
      store.setLoading();
      store.setError('Login failed');

      expect(store.error()).toBe('Login failed');
      expect(store.loading()).toBeFalse();
    });
  });

  describe('logout', () => {
    it('should reset all state', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      store.logout();

      expect(store.user()).toBeNull();
      expect(store.token()).toBeNull();
      expect(store.isAuthenticated()).toBeFalse();
      expect(store.loading()).toBeFalse();
      expect(store.error()).toBeNull();
    });
  });

  describe('clearError', () => {
    it('should clear error without affecting other state', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      store.setError('some error');
      store.clearError();

      expect(store.error()).toBeNull();
      expect(store.isAuthenticated()).toBeTrue();
    });
  });

  describe('sessionStorage persistence', () => {
    it('should persist token and user to sessionStorage on setAuthenticated', () => {
      store.setAuthenticated(mockUser, 'jwt-token');

      expect(sessionStorage.getItem('auth_token')).toBe('jwt-token');
      expect(JSON.parse(sessionStorage.getItem('auth_user')!)).toEqual(mockUser);
    });

    it('should clear sessionStorage on logout', () => {
      store.setAuthenticated(mockUser, 'jwt-token');
      store.logout();

      expect(sessionStorage.getItem('auth_token')).toBeNull();
      expect(sessionStorage.getItem('auth_user')).toBeNull();
    });
  });
});
