
```
# 📅 Payment Scheduler API

A robust REST API for scheduling and executing future bank transfers with fund reservation, JWT authentication, and role-based access control.

## 🚀 Technologies

- **Java 17**
- **Spring Boot 3**
- **Spring Security** with JWT
- **Spring Data JPA** with Hibernate
- **MySQL**
- **JUnit 5 & Mockito** (85% code coverage)
- **Swagger/OpenAPI 3** (API documentation)
- **Java Mail Sender** (SMTP)
- **Maven**

## ✨ Features

### 🔐 Security
- Stateless authentication with JWT
- Role-Based Access Control (RBAC)
- Password encryption with PBKDF2
- Protected endpoints requiring specific roles

### 💰 Transaction Management
- **Fund reservation system** – money is reserved at scheduling time, guaranteeing execution
- **Pessimistic locks** (`PESSIMISTIC_WRITE`) to prevent race conditions
- **Transaction isolation** ensuring data consistency
- Balance verification before execution

### ⏰ Scheduled Jobs
- Automatic verification 1 minute before scheduled payment
- Exact-time payment execution using `TaskScheduler`
- Job cancellation for pending transactions

### 📧 Notifications
- Email notifications for:
  - New scheduled payment confirmation
  - Payment execution success
  - Payment failure alerts

### 📚 API Documentation
- Full OpenAPI 3 documentation with Swagger UI
- Request/response examples
- Live API testing interface

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (Postman/Frontend)              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Spring Security + JWT                    │
│                  (Authentication & Authorization)           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      REST Controllers                       │
│              (Auth, Payment Scheduling, Account)            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Services                             │
│     (Business logic, Email, Transaction Management)         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Data JPA + Hibernate               │
│              (Repositories, Locks, Transactions)            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                            MySQL                            │
└─────────────────────────────────────────────────────────────┘
```

## 📋 API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/auth/register` | Create new user + bank account | Public |
| POST | `/auth/signin` | Authenticate and get JWT | Public |

### PIX Scheduling
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/scheduling` | Create scheduled payment | USER/ADMIN |
| GET | `/api/scheduling` | List all schedules | ADMIN |
| DELETE | `/api/scheduling/{id}` | Cancel schedule | ADMIN |
| GET | `/api/scheduling/today` | Get today's schedules | ADMIN |

## 🔄 How It Works

### Payment Flow

```
1. User schedules payment
   ↓
2. System verifies balance
   ↓
3. Funds are RESERVED (not debited yet)
   ↓
4. Email confirmation sent
   ↓
5. 1 minute before execution: verification runs
   ↓
6. At exact time: payment executes
   ↓
7. Balance debited, destination credited
   ↓
8. Success/failure email sent
```

### Fund Reservation Logic

```java
// Balance tracking
balance = 100.00          // Total balance
reservedBalance = 0.00    // Reserved for future payments
availableBalance = 100.00 // Balance - ReservedBalance

// After scheduling a $30 payment
balance = 100.00          // Unchanged
reservedBalance = 30.00   // Increased
availableBalance = 70.00  // Decreased

// After payment execution
balance = 70.00           // Debited
reservedBalance = 0.00    // Released
availableBalance = 70.00
```

## 🚦 Performance Optimizations

- **Async email sending** – Reduced response time from 8 seconds to 41ms
- **Pessimistic locks** – Prevent race conditions on concurrent requests
- **EAGER fetching** – Optimized relationship loading
- **Connection pooling** – Efficient database connection management

## 🧪 Testing

- **85% code coverage** with JUnit 5 and Mockito
- Unit tests for services and controllers
- Integration tests for repositories
- Security tests for role-based access

```bash
# Run tests
mvn clean test

# Generate coverage report
mvn jacoco:report
```

## 📦 Installation

### Prerequisites
- Java 17
- MySQL 8+
- Maven 3.8+

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/pay-scheduler-api.git
cd pay-scheduler-api
```

2. **Configure environment variables**
```bash
# Create .env file
JWT_SECRET=your-super-secret-key-min-32-chars
DB_URL=jdbc:mysql://localhost:3306/pay_scheduler
DB_USER=root
DB_PASS=yourpassword
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

3. **Build the project**
```bash
mvn clean package
```

4. **Log in to the database and add funds for testing purposes.**

5. **Run the application**
```bash
java -jar target/pay-scheduler-api.jar
```

## 📖 API Documentation

After running the application, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## 🛠️ Development

### Project Structure
```
src/
├── main/
│   ├── java/.../payschedulerapi/
│   │   ├── config/          # Security & Swagger config
│   │   ├── controller/      # REST endpoints
│   │   ├── dto/            # Data transfer objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # JWT & auth filters
│   │   └── service/        # Business logic
│   └── resources/
│       └── application.yml  # Configuration
└── test/                    # Unit tests
```

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is for educational purposes.

## 👨‍💻 Author

**Gabriel Wederson**  
- GitHub: [@GabrielWederson](https://github.com/GabrielWederson)

---

## 📊 Key Achievements

- ✅ **85% test coverage** ensuring reliability
- ✅ **41ms email sending** – 99.5% faster than sync approach
- ✅ **Race condition prevention** with pessimistic locks
- ✅ **Complete JWT security** with refresh tokens
- ✅ **Production-ready** with OpenAPI documentation

```
