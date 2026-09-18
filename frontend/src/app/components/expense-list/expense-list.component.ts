import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Expense, EXPENSE_CATEGORIES, CategoryMeta, PagedExpenseResponse } from '../../models/expense.model';
import { ExpenseService, ExpenseFilter } from '../../services/expense.service';
import { ToastService } from '../../services/toast.service';
import { ExpenseModalComponent } from '../expense-modal/expense-modal.component';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ExpenseModalComponent],
  templateUrl: './expense-list.component.html',
  styleUrls: ['./expense-list.component.css']
})
export class ExpenseListComponent implements OnInit, OnDestroy {
  private expenseService = inject(ExpenseService);
  private toastService = inject(ToastService);

  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  expenses: Expense[] = [];
  categories = EXPENSE_CATEGORIES;
  isLoading = false;

  // Pagination State
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  isLast = true;
  pageSizeOptions = [10, 25, 50];

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
  isExporting = false;

  ngOnInit(): void {
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm = term;
      this.currentPage = 0;
      this.loadExpenses();
    });

    this.loadExpenses();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  onSearchInput(value: string): void {
    this.searchSubject.next(value);
  }

  exportToCsv(): void {
    this.isExporting = true;
    this.expenseService.exportCsv({
      category: this.selectedCategory || undefined,
      search: this.searchTerm || undefined,
      startDate: this.startDate || undefined,
      endDate: this.endDate || undefined,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `expenses-${new Date().toISOString().slice(0, 10)}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.isExporting = false;
        this.toastService.success('Expenses exported to CSV successfully');
      },
      error: () => {
        this.isExporting = false;
        this.toastService.error('Failed to export expenses');
      }
    });
  }

  loadExpenses(): void {
    this.isLoading = true;
    const filter: ExpenseFilter = {
      search: this.searchTerm || undefined,
      category: this.selectedCategory || undefined,
      startDate: this.startDate || undefined,
      endDate: this.endDate || undefined,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection,
      page: this.currentPage,
      size: this.pageSize
    };

    this.expenseService.getExpenses(filter).subscribe({
      next: (response: PagedExpenseResponse) => {
        this.expenses = response.content;
        this.currentPage = response.page;
        this.pageSize = response.size;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLast = response.last;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching expenses', err);
        this.isLoading = false;
      }
    });
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadExpenses();
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
    this.selectedCategory = '';
    this.startDate = '';
    this.endDate = '';
    this.sortBy = 'date';
    this.sortDirection = 'desc';
    this.currentPage = 0;
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
    this.currentPage = 0;
    this.loadExpenses();
  }

  // Pagination actions
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadExpenses();
    }
  }

  nextPage(): void {
    if (!this.isLast && this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadExpenses();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadExpenses();
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 0;
    this.loadExpenses();
  }

  get startEntry(): number {
    if (this.totalElements === 0) return 0;
    return this.currentPage * this.pageSize + 1;
  }

  get endEntry(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
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
    const isEdit = !!this.selectedExpenseForEdit;
    this.showExpenseModal = false;
    this.selectedExpenseForEdit = null;
    this.loadExpenses();
    this.toastService.success(isEdit ? 'Expense updated successfully' : 'Expense created successfully');
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
        // If we deleted the only item on the current page and it's not the first page, go back 1 page
        if (this.expenses.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }
        this.loadExpenses();
        this.toastService.success('Expense deleted successfully');
      },
      error: (err) => {
        this.isDeleting = false;
        this.toastService.error(err.error?.message || 'Failed to delete expense.');
      }
    });
  }

  getCategoryMeta(categoryName: string): CategoryMeta {
    const found = this.categories.find(c => c.name.toLowerCase() === categoryName.toLowerCase());
    return found || { name: categoryName, color: '#64748b', bgColor: '#f8fafc', textColor: '#334155' };
  }
}
