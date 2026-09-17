import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Expense, EXPENSE_CATEGORIES, ExpenseRequest } from '../../models/expense.model';
import { ExpenseService } from '../../services/expense.service';

@Component({
  selector: 'app-expense-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './expense-modal.component.html',
  styleUrls: ['./expense-modal.component.css']
})
export class ExpenseModalComponent implements OnInit {
  @Input() expenseToEdit: Expense | null = null;
  @Output() saved = new EventEmitter<Expense>();
  @Output() closed = new EventEmitter<void>();

  private expenseService = inject(ExpenseService);

  categories = EXPENSE_CATEGORIES;

  amount: number | null = null;
  date: string = '';
  description: string = '';
  category: string = 'Food';

  errorMessage: string = '';
  isSubmitting: boolean = false;

  ngOnInit(): void {
    if (this.expenseToEdit) {
      this.amount = this.expenseToEdit.amount;
      this.date = this.expenseToEdit.date;
      this.description = this.expenseToEdit.description;
      this.category = this.expenseToEdit.category;
    } else {
      // Default to today's date
      const today = new Date();
      this.date = today.toISOString().split('T')[0];
    }
  }

  onSubmit(): void {
    if (this.amount === null || this.amount <= 0) {
      this.errorMessage = 'Please enter a valid positive amount.';
      return;
    }

    if (!this.date) {
      this.errorMessage = 'Please select a date.';
      return;
    }

    if (!this.description.trim()) {
      this.errorMessage = 'Please enter a description.';
      return;
    }

    if (!this.category) {
      this.errorMessage = 'Please select a category.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const payload: ExpenseRequest = {
      amount: this.amount,
      date: this.date,
      description: this.description.trim(),
      category: this.category
    };

    if (this.expenseToEdit) {
      this.expenseService.updateExpense(this.expenseToEdit.id, payload).subscribe({
        next: (updated) => {
          this.isSubmitting = false;
          this.saved.emit(updated);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.errorMessage = err.error?.message || 'Failed to update expense.';
        }
      });
    } else {
      this.expenseService.createExpense(payload).subscribe({
        next: (created) => {
          this.isSubmitting = false;
          this.saved.emit(created);
        },
        error: (err) => {
          this.isSubmitting = false;
          this.errorMessage = err.error?.message || 'Failed to create expense.';
        }
      });
    }
  }

  close(): void {
    this.closed.emit();
  }
}
