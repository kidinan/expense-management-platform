export interface Expense {
  id: number;
  amount: number;
  date: string;
  description: string;
  category: string;
  createdAt: string;
}

export interface ExpenseRequest {
  amount: number;
  date: string;
  description: string;
  category: string;
}

export const EXPENSE_CATEGORIES = [
  { name: 'Food', color: '#10b981', icon: '🍔' },
  { name: 'Travel', color: '#3b82f6', icon: '✈️' },
  { name: 'Bills', color: '#ef4444', icon: '📄' },
  { name: 'Shopping', color: '#8b5cf6', icon: '🛍️' },
  { name: 'Entertainment', color: '#f59e0b', icon: '🎬' },
  { name: 'Healthcare', color: '#ec4899', icon: '💊' },
  { name: 'Education', color: '#06b6d4', icon: '📚' },
  { name: 'Other', color: '#6b7280', icon: '🏷️' }
];
