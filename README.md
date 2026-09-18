# Multi-User Expense Management Platform

A clean, modern, and simple full-stack Expense Management Platform built with **Angular 19 (TypeScript)**, **Spring Boot 3 (Java 21)**, and **PostgreSQL**.

Designed with **clean code simplicity** (easy to understand and explain in a technical interview) and a **responsive, modern UI**.

---

## 🚀 Core Features

### 1. Authentication
- **Register**: Create an account with full name, username, email, and password.
- **Login**: Authenticate and receive a secure JWT token.
- **Logout**: Clears session and redirects to the login screen.
- **Protected Dashboard**: Angular route guard prevents unauthenticated access.
- **Session Management**: JWT token stored client-side, automatically attached to HTTP requests via interceptor, and auto-redirects on expiration.
- **Change Password**: Verify current password and update to a new password.

### 2. Expense Management
- **CRUD Operations**: Create, edit, delete, and view expenses.
- **Attributes**: Amount, date, description, and category.
- **Categories**: Food, Travel, Bills, Shopping, Entertainment, Healthcare, Education, Other.

### 3. Dashboard
- **Total Expenses**: Lifetime total spending.
- **Monthly Expenses**: Spending for the current month.
- **Expense Count**: Total number of expenses.
- **Spending by Category**: Breakdown with amount, item count, and percentage distribution.
- **Basic Chart / Reporting**: Category progress bars with category colors and a monthly spending trend bar chart.

### 4. Search & Filtering
- **Search Expenses**: Search by description keyword.
- **Filter by Category**: Filter by any category or view all.
- **Filter by Date Range**: Specify start and end dates.
- **Sort**: Sort by date or amount (ascending or descending).

### 5. Authorization
- **Users can only access their own expenses**.
- **Server-side authorization**: Every query is strictly filtered by the authenticated user's ID. Accessing another user's expense returns HTTP 404 / 403.

---

## 🛠️ Technical Stack

- **Frontend**: Angular 19 (TypeScript), Standalone Components, Responsive CSS
- **Backend**: Java 21, Spring Boot 3 (Spring Web, Spring Security, Spring Data JPA, Validation)
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Token) + BCrypt password hashing

---

## 📁 Project Structure

```
├── README.md
├── backend/                    # Spring Boot Application
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/example/expensetracker/
│       │   │   ├── ExpenseTrackerApplication.java
│       │   │   ├── controller/   # AuthController, ExpenseController, DashboardController
│       │   │   ├── dto/          # Request & Response DTOs
│       │   │   ├── entity/       # User & Expense entities
│       │   │   ├── exception/    # GlobalExceptionHandler
│       │   │   ├── repository/   # UserRepository, ExpenseRepository
│       │   │   ├── security/     # SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
│       │   │   └── service/      # AuthService, ExpenseService, DashboardService
│       │   └── resources/
│       │       └── application.properties # PostgreSQL configuration
│       └── test/                 # Multi-user isolation integration tests
└── frontend/                   # Angular Application
    ├── package.json
    ├── angular.json
    └── src/
        ├── app/
        │   ├── components/       # Navbar, Login, Register, Dashboard, ExpenseList, Modals
        │   ├── guards/           # authGuard
        │   ├── interceptors/     # authInterceptor (JWT Bearer attachment)
        │   ├── models/           # TypeScript interfaces & categories
        │   ├── services/         # AuthService, ExpenseService
        │   ├── app.routes.ts     # Routes
        │   └── app.component.ts
        └── styles.css            # Responsive CSS
```

---

## ⚡ How to Run

### 1. Database Setup (PostgreSQL)

Make sure PostgreSQL is running on standard port `5432` and create the database:
```sql
CREATE DATABASE expensedb;
```

*(Database connection details can be configured in `backend/src/main/resources/application.properties`)*:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expensedb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

### 2. Run the Spring Boot Backend

```bash
cd backend
mvn spring-boot:run
```
The REST API will start at `http://localhost:8080`.

To run the automated tests:
```bash
mvn test
```

---

### 3. Run the Angular Frontend

```bash
cd frontend
npm install
npm start
```
Open `http://localhost:4200` in your browser.

---

## 📡 REST API Endpoints

### Authentication (`/api/auth`)
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and obtain JWT
- `POST /api/auth/change-password` - Update password (authenticated)
- `GET /api/auth/me` - Current user profile (authenticated)

### Expenses (`/api/expenses`)
- `GET /api/expenses` - Paginated user expenses (supports `page`, `size`, `category`, `search`, `startDate`, `endDate`, `sortBy`, `sortDirection`)
- `GET /api/expenses/recent?limit=6` - Quick list of latest expenses for dashboard preview
- `POST /api/expenses` - Create a new expense
- `GET /api/expenses/{id}` - View an expense (user-owned only)
- `PUT /api/expenses/{id}` - Update an expense (user-owned only)
- `DELETE /api/expenses/{id}` - Delete an expense (user-owned only)

### Dashboard (`/api/dashboard`)
- `GET /api/dashboard/stats` - Total expenses, monthly expenses, count, category breakdown, monthly trend

---

## 💡 Code Interview Talking Points

- **Multi-user Data Isolation**: In `ExpenseRepository`, queries filter strictly by `user.id = :userId`, which is extracted from the authenticated JWT principal on the server. No user can view or modify another user's records.
- **Stateless JWT Flow**: Spring Security's `JwtAuthenticationFilter` checks every incoming request's `Authorization` header, validates the signature, and loads the user into `SecurityContextHolder`.
- **Angular Architecture**: Angular 19 Standalone Components, functional `authGuard` for routing protection, and `authInterceptor` for automatic JWT Bearer token attachment and handling `401 Unauthorized`.
- **Server-Side Validation**: Jakarta Validation (`@NotBlank`, `@Positive`, `@Email`, `@Size`) with standardized JSON errors via `@RestControllerAdvice`.

---

## 📤 Sharing on GitHub

To push this repository to GitHub and share with **yirgu15@gmail.com**:

```bash
git remote add origin https://github.com/<your-username>/expense-management-platform.git
git branch -M main
git push -u origin main
```

In GitHub:
1. Navigate to your repository.
2. Go to **Settings** &rarr; **Collaborators** &rarr; **Add people**.
3. Enter **`yirgu15@gmail.com`** and send the invite.
