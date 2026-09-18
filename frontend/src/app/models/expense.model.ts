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

export interface CategoryMeta {
  name: string;
  color: string;
  bgColor: string;
  textColor: string;
}

export const EXPENSE_CATEGORIES: CategoryMeta[] = [
  { name: 'Food', color: '#059669', bgColor: '#ecfdf5', textColor: '#065f46' },
  { name: 'Travel', color: '#0284c7', bgColor: '#f0f9ff', textColor: '#075985' },
  { name: 'Bills', color: '#dc2626', bgColor: '#fef2f2', textColor: '#991b1b' },
  { name: 'Shopping', color: '#7c3aed', bgColor: '#f5f3ff', textColor: '#5b21b6' },
  { name: 'Entertainment', color: '#d97706', bgColor: '#fffbeb', textColor: '#92400e' },
  { name: 'Healthcare', color: '#db2777', bgColor: '#fdf2f8', textColor: '#9d174d' },
  { name: 'Education', color: '#0d9488', bgColor: '#f0fdfa', textColor: '#115e59' },
  { name: 'Other', color: '#64748b', bgColor: '#f8fafc', textColor: '#334155' }
];
