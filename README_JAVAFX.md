# ATM Management System - JavaFX Client

JavaFX desktop application for ATM Management System that integrates with the backend REST API.

## 🚀 Changes Made

The application has been updated to use the **backend REST API** instead of direct database connections.

### Key Changes:
1. ✅ Added **Gson** library for JSON parsing
2. ✅ Created **API Service Layer** (`com.example.atmmanagementsystem.api`)
3. ✅ Updated all controllers to use **REST API calls**
4. ✅ Implemented **JWT token-based authentication**
5. ✅ Deprecated direct database access classes

## 📋 Prerequisites

- **Java JDK 21+** 
- **JavaFX SDK 21+** (or use bundled JDK with JavaFX)
- **Maven 3.8+**
- **Backend API running** at `http://localhost:5000`

## 🛠️ Setup & Installation

### 1. Ensure Backend is Running

Before starting the JavaFX application, make sure the backend API is running:

```bash
# In the backend directory (separate project)
cd backend
npm install
npm run dev
```

The backend should be accessible at `http://localhost:5000`

### 2. Build JavaFX Application

```bash
# Clean and compile
mvn clean compile

# Or build JAR
mvn clean package
```

### 3. Run the Application

```bash
# Development mode (with JavaFX)
mvn clean javafx:run

# Or run the JAR directly
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -jar target/atm-management-system-1.0-SNAPSHOT.jar
```

## 📁 Project Structure

```
src/main/java/com/example/atmmanagementsystem/
├── api/                          # NEW: API Service Layer
│   ├── ApiConfig.java           # API configuration (base URL, timeouts)
│   ├── ApiService.java          # Main API client (HTTP requests)
│   └── dto/                     # Data Transfer Objects
│       ├── LoginRequest.java
│       ├── LoginResponse.java
│       ├── CreateAccountRequest.java
│       ├── CardlessDepositRequest.java
│       ├── TransactionData.java
│       └── ApiResponse.java
├── controllers/
│   ├── AtmController.java       # UPDATED: Uses API instead of JDBC
│   ├── BanglaBankController.java # UPDATED: Uses API for account creation
│   └── MiniStatementController.java # UPDATED: Uses API for transactions
├── model/
│   ├── Account.java             # Updated: Added no-arg constructor
│   └── Transaction.java         # Updated: Added no-arg constructor
├── service/
│   ├── AccountService.java      # UPDATED: Factory returns ApiAccountService
│   ├── ApiAccountService.java   # NEW: API-based service implementation
│   └── DatabaseAccountService.java # DEPRECATED: Old JDBC implementation
├── util/
│   ├── DatabaseConnection.java  # DEPRECATED: No longer used
│   ├── DatabaseInitializer.java # DEPRECATED: Backend handles this
│   └── SecurityUtil.java        # DEPRECATED: Backend handles PIN hashing
└── BanglaBankApplication.java   # UPDATED: Removed DB initialization
```

## 🔐 Authentication Flow

The JavaFX app now uses JWT tokens for authentication:

1. **User enters card number and PIN** → App calls `POST /api/accounts/login`
2. **Backend validates credentials** → Returns JWT token + account data
3. **Token stored in ApiService** → Used for all subsequent authenticated requests
4. **All protected endpoints** include `Authorization: Bearer <token>` header

### Authenticated Endpoints:
- Deposit: `POST /api/transactions/:cardNumber/deposit`
- Withdraw: `POST /api/transactions/:cardNumber/withdraw`
- Balance Check: `GET /api/accounts/:cardNumber/balance`
- PIN Change: `PUT /api/accounts/:cardNumber/pin`
- Transaction History: `GET /api/transactions/:cardNumber`

## 🔄 API Integration Details

### Before (Direct Database):
```java
// Old approach - Direct JDBC
Account acc = service.findByCardNumber(cardNumber);
String hashedPin = SecurityUtil.hashPin(inputPin);
if (acc.getPin().equals(hashedPin)) {
    // Login success
}
```

### After (REST API):
```java
// New approach - HTTP API
LoginResponse response = apiService.login(cardNumber, inputPin);
if (response.isSuccess() && response.getToken() != null) {
    // Token automatically stored in ApiService
    // Login success
}
```

## 📊 API Endpoints Used

| Feature | HTTP Method | Endpoint | Auth Required |
|---------|-------------|----------|---------------|
| Login | POST | `/api/accounts/login` | No |
| Create Account | POST | `/api/accounts` | No |
| Get Account | GET | `/api/accounts/:cardNumber` | No |
| Get Balance | GET | `/api/accounts/:cardNumber/balance` | Yes |
| Deposit | POST | `/api/transactions/:cardNumber/deposit` | Yes |
| Withdraw | POST | `/api/transactions/:cardNumber/withdraw` | Yes |
| Change PIN | PUT | `/api/accounts/:cardNumber/pin` | Yes |
| Reset PIN | POST | `/api/accounts/:cardNumber/pin/reset` | No |
| Block Card | POST | `/api/accounts/:cardNumber/block` | No |
| Unblock Card | POST | `/api/accounts/:cardNumber/unblock` | Admin |
| Get Transactions | GET | `/api/transactions/:cardNumber` | Yes |
| Cardless Deposit | POST | `/api/transactions/cardless-deposit` | No |

## 🧪 Testing

### 1. Start Backend
```bash
cd backend
npm run dev
```

Backend should be at: `http://localhost:5000`
Swagger UI: `http://localhost:5000/api-docs`

### 2. Seed Sample Data (Optional)
```bash
cd backend
npm run seed
```

This creates sample accounts with PIN: `1234`

### 3. Launch JavaFX App
```bash
mvn clean javafx:run
```

### 4. Test Login
- Insert Card: Enter card number from seeded data
- PIN: `1234`

## ⚠️ Important Notes

1. **Backend Must Be Running**: The JavaFX app will fail to connect if the backend is not running at `http://localhost:5000`

2. **CORS**: The backend should have CORS enabled to allow requests from the JavaFX app

3. **Network Dependency**: All operations now require network connectivity to the backend

4. **Deprecated Classes**: 
   - `DatabaseConnection` - No longer used
   - `DatabaseInitializer` - Backend handles this
   - `SecurityUtil` - Backend handles PIN hashing
   - `DatabaseAccountService` - Replaced by `ApiAccountService`

5. **Token Management**: JWT tokens are stored in memory (ApiService singleton). Tokens are lost when the app closes.

## 🐛 Troubleshooting

### "Connection refused" errors
- Ensure backend is running at `http://localhost:5000`
- Check if port 5000 is not blocked by firewall

### "Login failed" errors
- Verify backend database has the account
- Check if card is not blocked
- Ensure PIN is correct (3 failed attempts will block the card)

### "Account creation failed"
- Check if phone/email/NID are unique
- Ensure initial deposit is at least 100 TK

## 📝 Development

### Adding New API Endpoints

1. Add DTO class in `api/dto/`
2. Add method in `ApiService.java`
3. Add method in `ApiAccountService.java`
4. Update controller to call the new service method

### Changing Backend URL

Edit `api/ApiConfig.java`:
```java
public static final String BASE_URL = "http://your-server:port/api";
```

## 📄 License

ISC

## 👨‍💻 Author

ATM Management System - JavaFX Client
