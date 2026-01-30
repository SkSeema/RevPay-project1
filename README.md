# RevPay Console-Based Financial Application

RevPay is a console-based Digital Wallet and Payment Management System developed using Java and JDBC.  
The application supports user registration, wallet operations, money transfer, invoices, loans, notifications, and business analytics.

The project follows a layered architecture and uses Log4j 2 for logging and MySQL as the backend database.

---

## Project Architecture

The application follows a layered architecture to ensure separation of concerns and maintainability.

### 1. Main Layer

- Entry point of the application (Main.java)
- Handles console-based user interaction
- Controls application flow

### 2. Controller Layer

Handles user input and delegates requests to services.

Controllers include:
- UserController
- WalletController
- SendMoneyController
- MoneyRequestController
- InvoiceController
- InvoiceItemController
- LoanController
- NotificationController
- BusinessAnalyticsController

### 3. Service Layer

Contains business logic and acts as a bridge between Controller and DAO layers.

Services include:
- UserService
- WalletService
- SendMoneyService
- MoneyRequestService
- InvoiceService
- InvoiceItemService
- LoanService
- NotificationService
- TransactionService
- BusinessAnalyticsService

### 4. DAO Layer

Handles all database operations using JDBC and PreparedStatement.

DAOs include:
- UserDAO
- WalletDAO
- SendMoneyDAO
- MoneyRequestDAO
- InvoiceDAO
- InvoiceItemDAO
- LoanDAO
- NotificationDAO
- TransactionDAO
- BusinessDetailsDAO

### 5. Model Layer

Contains POJO (Plain Old Java Object) classes.

Core entities:
- User
- Wallet
- Transaction
- SendMoney
- MoneyRequest
- Invoice
- InvoiceItem
- Loan
- Notification
- BusinessDetails

### 6. Utility and Security Layer

- Database connection handling
- Password hashing
- OTP generation
- Encryption utilities

---

## Database Design

- Database: MySQL
- SQL scripts provided in the sql folder
- Normalized schema with foreign key constraints

---

## Logging

Logging is implemented using Log4j 2.

Logs include:
- User actions
- Transactions
- Errors and exceptions

Output:
- Console
- Log files

---

## Testing

- Unit testing using JUnit 5
- Mock testing using Mockito
- Service layer tested independently from database

---

## Technology Stack

- Java
- JDBC
- MySQL
- Maven
- Log4j 2
- JUnit 5
- Mockito

---

## How to Run

1. Create MySQL database  
   revpay

2. Execute SQL scripts from the sql folder

3. Update database credentials in DBConnection.java

4. Run Main.java

---


