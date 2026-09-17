import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Expense, ExpenseRequest } from '../models/expense.model';
import { DashboardStats } from '../models/dashboard.model';

export interface ExpenseFilter {
  category?: string;
  search?: string;
  startDate?: string;
  endDate?: string;
  sortBy?: string;
  sortDirection?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  private readonly apiUrl = 'http://localhost:8080/api/expenses';
  private readonly dashboardUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  getExpenses(filters?: ExpenseFilter): Observable<Expense[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.category) params = params.set('category', filters.category);
      if (filters.search) params = params.set('search', filters.search);
      if (filters.startDate) params = params.set('startDate', filters.startDate);
      if (filters.endDate) params = params.set('endDate', filters.endDate);
      if (filters.sortBy) params = params.set('sortBy', filters.sortBy);
      if (filters.sortDirection) params = params.set('sortDirection', filters.sortDirection);
    }
    return this.http.get<Expense[]>(this.apiUrl, { params });
  }

  getExpense(id: number): Observable<Expense> {
    return this.http.get<Expense>(`${this.apiUrl}/${id}`);
  }

  createExpense(expense: ExpenseRequest): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, expense);
  }

  updateExpense(id: number, expense: ExpenseRequest): Observable<Expense> {
    return this.http.put<Expense>(`${this.apiUrl}/${id}`, expense);
  }

  deleteExpense(id: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.dashboardUrl}/stats`);
  }
}
