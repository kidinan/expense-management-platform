export interface CategorySummary {
  category: string;
  totalAmount: number;
  count: number;
  percentage: number;
}

export interface MonthlySpending {
  month: string;
  amount: number;
}

export interface DashboardStats {
  totalExpenses: number;
  monthlyExpenses: number;
  expenseCount: number;
  spendingByCategory: CategorySummary[];
  monthlyTrend: MonthlySpending[];
}
