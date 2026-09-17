import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ExpenseService } from '../../services/expense.service';
import { AuthService } from '../../services/auth.service';
import { DashboardStats } from '../../models/dashboard.model';
import { Expense, EXPENSE_CATEGORIES } from '../../models/expense.model';
import { ExpenseModalComponent } from '../expense-modal/expense-modal.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ExpenseModalComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private expenseService = inject(ExpenseService);
  authService = inject(AuthService);

  stats: DashboardStats | null = null;
  recentExpenses: Expense[] = [];
  categories = EXPENSE_CATEGORIES;
  isLoading = true;

  showExpenseModal = false;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.expenseService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      },
      error: (err) => console.error('Failed to load stats', err)
    });

    this.expenseService.getExpenses({ sortBy: 'date', sortDirection: 'desc' }).subscribe({
      next: (expenses) => {
        this.recentExpenses = expenses.slice(0, 5);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load recent expenses', err);
        this.isLoading = false;
      }
    });
  }

  openAddModal(): void {
    this.showExpenseModal = true;
  }

  onExpenseSaved(): void {
    this.showExpenseModal = false;
    this.loadDashboardData();
  }

  getCategoryColor(categoryName: string): string {
    const found = this.categories.find(c => c.name.toLowerCase() === categoryName.toLowerCase());
    return found ? found.color : '#6b7280';
  }

  getCategoryIcon(categoryName: string): string {
    const found = this.categories.find(c => c.name.toLowerCase() === categoryName.toLowerCase());
    return found ? found.icon : '🏷️';
  }

  getMaxMonthlyAmount(): number {
    if (!this.stats || !this.stats.monthlyTrend.length) return 1;
    const max = Math.max(...this.stats.monthlyTrend.map(m => m.amount));
    return max > 0 ? max : 1;
  }
}
