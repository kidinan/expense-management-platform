# Multi-User Expense Management Platform

A clean, modern, and production-ready full-stack Expense Management Platform built with **Angular 19 (TypeScript)**, **Spring Boot 3 (Java 21)**, and **PostgreSQL**.

Designed with **clean code simplicity** (easy to understand and explain in a technical interview) and a **responsive, modern UI**.

---

## 🚀 Key Features

### 1. Authentication & Session Management
- **User Registration**: Register with full name, unique username, valid email, and secure password (hashed via BCrypt).
- **User Login**: Stateless authentication returning a signed JWT (JSON Web Token) with user identity.
- **Protected Dashboard & Routes**: Angular `authGuard` prevents unauthenticated access to dashboard and expense management pages.
- **Session Management**: Client-side storage of JWT, automatic injection of `Authorization: Bearer <token>` header via Angular HTTP Interceptor (`authInterceptor`), and automatic session timeout/redirect on `401 Unauthorized`.
- **Change Password**: Secure endpoint requiring current password verification before updating to a new password.
- **Logout**: Instantly terminates client session and clears stored tokens.

### 2. Expense Management
- **CRUD Operations**: Full Create, Read, Update, and Delete operations for user expenses.
- **Expense Attributes**:
  - Amount (validated positive number)
  - Date (custom date picker)
  - Description (clean search-indexed text)
  - Category (categorized with distinct visual badges & icons)
- **Categories**:
  - 🍔 Food
  - ✈️ Travel
  - 📄 Bills
  - 🛍️ Shopping
  - 🎬 Entertainment
  - 💊 Healthcare
  - 📚 Education
  - 🏷️ Other

### 3. Dashboard & Analytics
- **Total Expenses**: Lifetime spending across all categories.
- **Monthly Expenses**: Spending aggregated strictly for the current calendar month.
- **Expense Count**: Total number of expense entries.
- **Spending by Category**: Breakdown with amount, item count, and percentage distribution.
- **Visual Reporting**: Clean, lightweight visual charts:
  - Interactive category progress indicators with dedicated category colors.
  - Monthly spending trend bar chart covering the last 6 months.
  - Quick recent transactions feed with direct links.

### 4. Search, Filtering & Sorting
- **Real-time Search**: Search expenses by description text.
- **Category Filter**: Filter expenses by any specific category or "All Categories".
- **Date Range Filter**: Filter records by "From Date" and "To Date".
- **Sorting**: Toggle sorting by **Date** or **Amount** (Ascending / Descending).

### 5. Multi-User Server-Side Authorization
- **Strict Data Isolation**: Each authenticated user can only access, view, update, or delete **their own** expenses.
- **Server-Side Enforcement**: Every database query is strictly scoped to the authenticated user's ID (`user.id = :userId` and `findByIdAndUserId(id, userId)`).
- **Zero Cross-User Leakage**: Any attempt by User A to access or manipulate User B's expense ID returns an immediate HTTP `404 Not Found` / `403 Forbidden`.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | Angular 19 (Standalone Components, TypeScript, Signals) |
| **Styling** | Modern Responsive CSS (CSS Custom Properties, Mobile-First) |
| **Backend** | Java 21, Spring Boot 3.3.4 (Spring Web, Spring Security, Spring Data JPA, Validation) |
| **Security** | JSON Web Tokens (JJWT 0.12.6), BCrypt Password Hashing |
| **Database** | PostgreSQL 16 (production), H2 In-Memory (testing & zero-config dev) |
| **Containerization** | Docker & Docker Compose |

---

## 📁 Project Structure

```
├── docker-compose.yml          # PostgreSQL container definition
├── README.md                   # Project documentation & interview guide
├── backend/                    # Spring Boot 3 Application
│   ├── pom.xml                 # Maven dependencies
│   └── src/
│       ├── main/
│       │   ├── java/com/example/expensetracker/
│       │   │   ├── ExpenseTrackerApplication.java
│       │   │   ├── controller/   # AuthController, ExpenseController, DashboardController
│       │   │   ├── dto/          # Request & Response DTOs
│       │   │   ├── entity/       # User & Expense JPA entities
│       │   │   ├── exception/    # GlobalExceptionHandler & custom exceptions
│       │   │   ├── repository/   # UserRepository, ExpenseRepository
│       │   │   ├── security/     # SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
│       │   │   └── service/      # AuthService, ExpenseService, DashboardService
│       │   └── resources/
│       │       └── application.properties # PostgreSQL & JWT config
│       └── test/                 # Automated multi-user isolation integration tests
└── frontend/                   # Angular 19 Application
    ├── package.json
    ├── angular.json
    └── src/
        ├── app/
        │   ├── components/       # Navbar, Login, Register, Dashboard, ExpenseList, Modals
        │   ├── guards/           # authGuard
        │   ├── interceptors/     # authInterceptor (JWT Bearer attachment)
        │   ├── models/           # TypeScript interfaces & category constants
        │   ├── services/         # AuthService, ExpenseService
        │   ├── app.routes.ts     # Route definitions
        │   └── app.component.ts
        └── styles.css            # Clean, modern design system
```

---

## ⚡ Quick Start Guide

### Prerequisites
- **Java 21** & **Maven**
- **Node.js 20+** & **npm**
- **Docker** (optional, for PostgreSQL container)

---

### Step 1: Start the PostgreSQL Database

Using Docker Compose:
```bash
docker compose up -d
```
*(This starts PostgreSQL on port `5432` with database `expensedb`, user `postgres`, and password `postgres`)*.

> **Note**: An H2 in-memory profile is also included. If you want to run without PostgreSQL or Docker, run the backend with `--spring.profiles.active=test`.

---

### Step 2: Run the Spring Boot Backend

```bash
cd backend
mvn spring-boot:run
```
The REST API will be live at `http://localhost:8080`.

To run the automated tests (including multi-user isolation tests):
```bash
mvn test
```

---

### Step 3: Run the Angular Frontend

```bash
cd frontend
npm install
npm start
```
The application will be live at `http://localhost:4200`.

---

## 📡 REST API Documentation

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user account | No |
| `POST` | `/api/auth/login` | Login and receive JWT token | No |
| `POST` | `/api/auth/change-password` | Change current user password | Yes (Bearer) |
| `GET` | `/api/auth/me` | Get current user profile details | Yes (Bearer) |

#### Register Request Sample:
```json
POST /api/auth/register
{
  "fullName": "Alice Smith",
  "username": "alice",
  "email": "alice@example.com",
  "password": "password123"
}
```

#### Login Request Sample:
```json
POST /api/auth/login
{
  "username": "alice",
  "password": "password123"
}
```

---

### Expense Management (`/api/expenses`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/expenses` | Get all expenses with optional filters | Yes (Bearer) |
| `POST` | `/api/expenses` | Create a new expense | Yes (Bearer) |
| `GET` | `/api/expenses/{id}` | Get expense by ID (must belong to user) | Yes (Bearer) |
| `PUT` | `/api/expenses/{id}` | Update expense (must belong to user) | Yes (Bearer) |
| `DELETE` | `/api/expenses/{id}` | Delete expense (must belong to user) | Yes (Bearer) |

#### Query Parameters for `GET /api/expenses`:
- `category` (optional): e.g. `Food`, `Travel`
- `search` (optional): search term in description
- `startDate` (optional): `YYYY-MM-DD`
- `endDate` (optional): `YYYY-MM-DD`
- `sortBy` (optional): `date` (default) or `amount`
- `sortDirection` (optional): `desc` (default) or `asc`

#### Create Expense Request Sample:
```json
POST /api/expenses
Authorization: Bearer <your_jwt_token>
{
  "amount": 75.50,
  "date": "2026-09-17",
  "description": "Team lunch at cafe",
  "category": "Food"
}
```

---

### Dashboard (`/api/dashboard`)

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/dashboard/stats` | Returns total spend, monthly spend, count, category breakdown, & monthly trend | Yes (Bearer) |

---

## 💡 Code Interview Preparation: How to Explain This Project

When interviewing, interviewers usually focus on design choices, security, and clean separation of concerns. Here are concise answers to common questions:

### 1. How is multi-user data isolation enforced?
> **Answer**: "Data isolation is enforced strictly on the backend at the repository and service layers. In `ExpenseRepository`, methods like `findByIdAndUserId(Long id, Long userId)` and custom JPQL queries ensure that every database lookup or mutation includes `e.user.id = :userId`. The `userId` is never trusted from the client request body or query param; instead, it is extracted securely from the verified JWT token in `SecurityContextHolder` via `@AuthenticationPrincipal`."

### 2. How does the JWT authentication filter work?
> **Answer**: "The application uses stateless session management (`SessionCreationPolicy.STATELESS`). On every incoming HTTP request, `JwtAuthenticationFilter` intercepts the request, checks for the `Authorization: Bearer <token>` header, verifies the signature and expiration using JJWT, extracts the username/userId, loads the `UserPrincipal`, and populates Spring's `SecurityContextHolder`. If the token is absent or invalid on a protected route, Spring Security returns `401 Unauthorized`."

### 3. How does Angular handle authentication and route protection?
> **Answer**: "We utilize Angular 19 functional guards and interceptors:
> - `authGuard`: Registered on protected routes (`/dashboard`, `/expenses`). If no JWT exists in storage, the user is immediately redirected to `/login`.
> - `authInterceptor`: Automatically clones every outgoing HTTP request to attach `Authorization: Bearer <token>` and catches any `401 Unauthorized` responses to clear local storage and log the user out cleanly."

### 4. Why keep the architecture straightforward?
> **Answer**: "We avoided unnecessary complexity and external heavyweight libraries. The backend follows standard Spring Boot layered architecture (`Controller -> Service -> Repository -> Entity`), standard Jakarta Validation annotations (`@Positive`, `@NotBlank`), and standardized `@RestControllerAdvice` error responses. The frontend uses Angular Standalone Components with clean, responsive CSS custom properties, achieving an initial bundle size under 100 kB."

---

## 📤 Sharing on GitHub

To push this repository to GitHub and share with **yirgu15@gmail.com**:

1. Create a new repository on your GitHub account (e.g. `expense-management-platform`).
2. Add your GitHub remote and push:
   ```bash
   git remote add origin https://github.com/<your-username>/expense-management-platform.git
   git branch -M main
   git push -u origin main
   ```
3. Share with the interviewer:
   - Go to your repository on GitHub.
   - Click **Settings** &rarr; **Collaborators** &rarr; **Add people**.
   - Enter **`yirgu15@gmail.com`** and click **Add collaborator**.
