import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Expense, EXPENSE_CATEGORIES, CategoryMeta } from '../../models/expense.model';
import { ExpenseService, ExpenseFilter } from '../../services/expense.service';
import { ExpenseModalComponent } from '../expense-modal/expense-modal.component';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ExpenseModalComponent],
  templateUrl: './expense-list.component.html',
  styleUrls: ['./expense-list.component.css']
})
export class ExpenseListComponent implements OnInit {
  private expenseService = inject(ExpenseService);

  expenses: Expense[] = [];
  categories = EXPENSE_CATEGORIES;
  isLoading = false;

  // Filters
  searchTerm = '';
  selectedCategory = '';
  startDate = '';
  endDate = '';
  sortBy = 'date';
  sortDirection = 'desc';

  // Modals state
  showExpenseModal = false;
  selectedExpenseForEdit: Expense | null = null;
  expenseToDelete: Expense | null = null;
  isDeleting = false;

  ngOnInit(): void {
    this.loadExpenses();
  }

  loadExpenses(): void {
    this.isLoading = true;
    const filter: ExpenseFilter = {
      search: this.searchTerm || undefined,
      category: this.selectedCategory || undefined,
      startDate: this.startDate || undefined,
      endDate: this.endDate || undefined,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection
    };

    this.expenseService.getExpenses(filter).subscribe({
      next: (data) => {
        this.expenses = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching expenses', err);
        this.isLoading = false;
      }
    });
  }

  onSearchChange(): void {
    this.loadExpenses();
  }

  onFilterChange(): void {
    this.loadExpenses();
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.selectedCategory = '';
    this.startDate = '';
    this.endDate = '';
    this.sortBy = 'date';
    this.sortDirection = 'desc';
    this.loadExpenses();
  }

  get hasActiveFilters(): boolean {
    return !!(this.searchTerm || this.selectedCategory || this.startDate || this.endDate);
  }

  toggleSort(field: string): void {
    if (this.sortBy === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = field;
      this.sortDirection = 'desc';
    }
    this.loadExpenses();
  }

  openAddModal(): void {
    this.selectedExpenseForEdit = null;
    this.showExpenseModal = true;
  }

  openEditModal(expense: Expense): void {
    this.selectedExpenseForEdit = expense;
    this.showExpenseModal = true;
  }

  onExpenseSaved(): void {
    this.showExpenseModal = false;
    this.selectedExpenseForEdit = null;
    this.loadExpenses();
  }

  confirmDelete(expense: Expense): void {
    this.expenseToDelete = expense;
  }

  cancelDelete(): void {
    this.expenseToDelete = null;
  }

  executeDelete(): void {
    if (!this.expenseToDelete) return;
    this.isDeleting = true;

    this.expenseService.deleteExpense(this.expenseToDelete.id).subscribe({
      next: () => {
        this.isDeleting = false;
        this.expenseToDelete = null;
        this.loadExpenses();
      },
      error: (err) => {
        this.isDeleting = false;
        alert(err.error?.message || 'Failed to delete expense.');
      }
    });
  }

  getCategoryMeta(categoryName: string): CategoryMeta {
    const found = this.categories.find(c => c.name.toLowerCase() === categoryName.toLowerCase());
    return found || { name: categoryName, color: '#64748b', bgColor: '#f8fafc', textColor: '#334155' };
  }
}
