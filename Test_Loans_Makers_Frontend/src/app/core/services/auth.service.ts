import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, retry, timeout } from 'rxjs';
import { LoginRequest, LoginResponse, User } from '../models/user.model';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`).pipe(
      timeout(15000),
      retry({ count: 2, delay: 1000 }),
    );
  }
}
