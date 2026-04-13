# ATM Management System - API Documentation

**Base URL:** `http://localhost:5000/api`  
**Version:** 1.0.0

---

## Table of Contents

- [Authentication](#authentication)
- [Common Response Format](#common-response-format)
- [Health Endpoints](#health-endpoints)
- [Account Endpoints](#account-endpoints)
  - [Create Account](#1-create-account)
  - [Login/Authenticate](#2-loginauthenticate)
  - [Get Account Details](#3-get-account-details)
  - [List All Accounts (Admin)](#4-list-all-accounts-admin)
  - [Get Balance](#5-get-balance)
  - [Block Account](#6-block-account)
  - [Unblock Account (Admin)](#7-unblock-account-admin)
  - [Update PIN](#8-update-pin)
  - [Reset PIN (Forgot PIN)](#9-reset-pin-forgot-pin)
- [Transaction Endpoints](#transaction-endpoints)
  - [Deposit Money](#1-deposit-money)
  - [Withdraw Money](#2-withdraw-money)
  - [Get Transaction History](#3-get-transaction-history)
  - [Get All Transactions (Admin)](#4-get-all-transactions-admin)
  - [Cardless Deposit](#5-cardless-deposit)

---

## Authentication

**Type:** JWT Bearer Token  
**Header:** `Authorization: Bearer <token>`

Obtain token by calling the [Login/Authenticate](#2-loginauthenticate) endpoint.

**Token Payload:**
```json
{
  "card_number": "16-digit card number",
  "account_number": "12-digit account number"
}
```

**Token Expiry:** Configurable via `JWT_EXPIRE` env var (default: 24h)

---

## Common Response Format

### Success Response
```json
{
  "success": true,
  "message": "Descriptive message",
  "data": { ... }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error message",
  "errors": [
    {
      "field": "field_name",
      "message": "Validation error message"
    }
  ]
}
```

---

## Health Endpoints

### GET `/health`

Server health check (no authentication required).

**Response (200):**
```json
{
  "success": true,
  "message": "Server is running",
  "timestamp": "2026-04-08T12:00:00.000Z",
  "uptime": 123.456
}
```

### GET `/api/health`

API health check (no authentication required).

**Response (200):**
```json
{
  "success": true,
  "message": "API is running",
  "timestamp": "2026-04-08T12:00:00.000Z",
  "uptime": 123.456
}
```

---

## Account Endpoints

### 1. Create Account

Create a new bank account with initial deposit.

**Endpoint:** `POST /api/accounts`  
**Authentication:** None (Public)

**Request Body:**
```json
{
  "name": "John Doe",
  "phone_number": "01712345678",
  "email": "johndoe@example.com",
  "gender": "Male",
  "profession": "Software Engineer",
  "nid": "1234567890123456",
  "address": "123 Main Street, Dhaka",
  "nationality": "Bangladeshi",
  "initial_deposit": 1000
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `name` | string | Yes | Minimum 2 characters, trimmed |
| `phone_number` | string | Yes | 10-15 digits, pattern: `^[0-9]{10,15}$` |
| `email` | string | No | Valid email format |
| `gender` | string | Yes | Must be: `Male`, `Female`, or `Other` |
| `profession` | string | Yes | Non-empty string |
| `nid` | string | Yes | Digits only |
| `address` | string | Yes | Non-empty string |
| `nationality` | string | No | Default: "Bangladeshi" |
| `initial_deposit` | number | Yes | Minimum 100 TK |

**Success Response (201):**
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "account": {
      "account_number": "123456789012",
      "card_number": "1234567890123456",
      "name": "John Doe",
      "phone_number": "01712345678",
      "email": "johndoe@example.com",
      "gender": "Male",
      "profession": "Software Engineer",
      "nationality": "Bangladeshi",
      "nid": "1234567890123456",
      "address": "123 Main Street, Dhaka",
      "balance": 1000,
      "created_at": "2026-04-08T12:00:00.000Z"
    },
    "pin": "1234"
  }
}
```

**Error Responses:**
- **400 Bad Request:** Validation errors
- **409 Conflict:** Duplicate phone number, email, or NID

---

### 2. Login/Authenticate

Authenticate using card number and PIN.

**Endpoint:** `POST /api/accounts/login`  
**Authentication:** None (Public)

**Request Body:**
```json
{
  "card_number": "1234567890123456",
  "pin": "1234"
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `card_number` | string | Yes | Exactly 16 digits, pattern: `^[0-9]{16}$` |
| `pin` | string | Yes | Exactly 4 digits, pattern: `^[0-9]{4}$` |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "account": {
      "account_number": "123456789012",
      "card_number": "1234567890123456",
      "name": "John Doe",
      "balance": 5000
    }
  }
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `token` | string | JWT token for authenticated requests |
| `account.account_number` | string | 12-digit account number |
| `account.card_number` | string | 16-digit card number |
| `account.name` | string | Account holder's full name |
| `account.balance` | number | Current account balance in TK

**Error Responses:**
- **400 Bad Request:** Validation errors
- **404 Not Found:** Account not found
- **401 Unauthorized:** Invalid PIN
- **403 Forbidden:** Account blocked due to multiple failed attempts (3 failed attempts)

---

### 3. Get Account Details

Retrieve account information by account number.

**Endpoint:** `GET /api/accounts/:accountNumber`  
**Authentication:** None (Public)

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `accountNumber` | string | 12-digit account number |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Account retrieved",
  "data": {
    "account_number": "123456789012",
    "card_number": "1234567890123456",
    "name": "John Doe",
    "phone_number": "01712345678",
    "email": "johndoe@example.com",
    "gender": "Male",
    "profession": "Software Engineer",
    "nationality": "Bangladeshi",
    "nid": "1234567890123456",
    "address": "123 Main Street, Dhaka",
    "balance": 5000,
    "created_at": "2026-04-08T12:00:00.000Z"
  }
}
```

**Note:** Sensitive fields (`pin`, `blocked`, `failed_pin_attempts`) are excluded from the response.

**Error Responses:**
- **404 Not Found:** Account not found

---

### 4. List All Accounts (Admin)

Retrieve all accounts with pagination (Admin only).

**Endpoint:** `GET /api/accounts`  
**Authentication:** Required (Valid JWT Token - Admin role)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `limit` | integer | 50 | Number of records to return |
| `offset` | integer | 0 | Number of records to skip |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Accounts retrieved",
  "data": [
    {
      "account_number": "123456789012",
      "card_number": "1234567890123456",
      "name": "John Doe",
      "phone_number": "01712345678",
      "email": "johndoe@example.com",
      "gender": "Male",
      "profession": "Software Engineer",
      "nationality": "Bangladeshi",
      "balance": 5000,
      "created_at": "2026-04-08T12:00:00.000Z"
    }
  ],
  "pagination": {
    "limit": 50,
    "offset": 0
  }
}
```

**Error Responses:**
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token, token verification failed, or insufficient permissions

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 5. Get Balance

Retrieve account balance.

**Endpoint:** `GET /api/accounts/:cardNumber/balance`  
**Authentication:** Required (Valid JWT Token)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Balance retrieved",
  "data": {
    "balance": 5000
  }
}
```

**Error Responses:**
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token or token verification failed
- **404 Not Found:** Account not found

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 6. Block Account

Block an account card using NID verification.

**Endpoint:** `POST /api/accounts/:cardNumber/block`  
**Authentication:** None (Public with NID verification)

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Request Body:**
```json
{
  "nid_proof": "3456"
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `nid_proof` | string | Yes | Last 4 digits of NID |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Account blocked successfully"
}
```

**Error Responses:**
- **400 Bad Request:** NID verification failed
- **404 Not Found:** Account not found

---

### 7. Unblock Account (Admin)

Unblock an account card (Admin only).

**Endpoint:** `POST /api/accounts/:cardNumber/unblock`  
**Authentication:** Required (Valid JWT Token - Admin role)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Account unblocked successfully"
}
```

**Error Responses:**
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token, token verification failed, or insufficient permissions
- **404 Not Found:** Account not found

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 8. Update PIN

Update account PIN.

**Endpoint:** `PUT /api/accounts/:cardNumber/pin`  
**Authentication:** Required (Valid JWT Token)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Request Body:**
```json
{
  "new_pin": "5678"
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `new_pin` | string | Yes | Exactly 4 digits, pattern: `^[0-9]{4}$` |

**Business Rules:**
- New PIN cannot be the same as the current PIN

**Success Response (200):**
```json
{
  "success": true,
  "message": "PIN updated successfully"
}
```

**Error Responses:**
- **400 Bad Request:** Validation errors or same PIN
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token or token verification failed
- **404 Not Found:** Account not found

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 9. Reset PIN (Forgot PIN)

Reset forgotten PIN using NID verification.

**Endpoint:** `POST /api/accounts/:cardNumber/pin/reset`  
**Authentication:** None (Public with NID verification)

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Request Body:**
```json
{
  "nid_proof": "3456"
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `nid_proof` | string | Yes | Last 4 digits of NID |

**Success Response (200):**
```json
{
  "success": true,
  "message": "PIN reset successful. Please save your new PIN.",
  "data": {
    "new_pin": "4321"
  }
}
```

**Error Responses:**
- **400 Bad Request:** NID verification failed
- **404 Not Found:** Account not found

---

## Transaction Endpoints

### 1. Deposit Money

Deposit money into an account.

**Endpoint:** `POST /api/transactions/:cardNumber/deposit`  
**Authentication:** Required (Valid JWT Token)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Request Body:**
```json
{
  "amount": 1000
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `amount` | number | Yes | 500-25,000 TK, must be multiple of 500 |

**Business Rules:**
- Amount must be between 500 and 25,000 TK
- Amount must be a multiple of 500
- Daily deposit limit: 50,000 TK or 5 transactions per day

**Success Response (200):**
```json
{
  "success": true,
  "message": "Deposit successful",
  "data": {
    "transaction": {
      "id": 1,
      "card_number": "1234567890123456",
      "amount": 1000,
      "transaction_type": "DEPOSIT",
      "timestamp": "2026-04-08T12:00:00.000Z"
    },
    "balance": 6000
  }
}
```

**Error Responses:**
- **400 Bad Request:** Validation errors or amount not multiple of 500
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token or token verification failed
- **404 Not Found:** Account not found
- **429 Too Many Requests:** Daily deposit limit exceeded

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 2. Withdraw Money

Withdraw money from an account.

**Endpoint:** `POST /api/transactions/:cardNumber/withdraw`  
**Authentication:** Required (Valid JWT Token)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Request Body:**
```json
{
  "amount": 1000
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `amount` | number | Yes | 500-25,000 TK, must be multiple of 500 |

**Business Rules:**
- Amount must be between 500 and 25,000 TK
- Amount must be a multiple of 500
- Must maintain minimum balance of 500 TK
- Daily withdrawal limit: 50,000 TK or 5 transactions per day

**Success Response (200):**
```json
{
  "success": true,
  "message": "Withdrawal successful",
  "data": {
    "transaction": {
      "id": 2,
      "card_number": "1234567890123456",
      "amount": 1000,
      "transaction_type": "WITHDRAW",
      "timestamp": "2026-04-08T12:00:00.000Z"
    },
    "balance": 5000
  }
}
```

**Error Responses:**
- **400 Bad Request:** Validation errors, insufficient funds, or amount not multiple of 500
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token or token verification failed
- **404 Not Found:** Account not found
- **429 Too Many Requests:** Daily withdrawal limit exceeded

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 3. Get Transaction History

Retrieve transaction history for a specific card.

**Endpoint:** `GET /api/transactions/:cardNumber`  
**Authentication:** Required (Valid JWT Token)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `cardNumber` | string | 16-digit card number |

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `type` | string | All | Filter by type: `DEPOSIT` or `WITHDRAW` |
| `date_from` | string | - | Start date (date format) |
| `date_to` | string | - | End date (date format) |
| `limit` | integer | 50 | Number of records to return |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Transactions retrieved",
  "data": [
    {
      "id": 1,
      "card_number": "1234567890123456",
      "amount": 1000,
      "transaction_type": "DEPOSIT",
      "timestamp": "2026-04-08T12:00:00.000Z"
    },
    {
      "id": 2,
      "card_number": "1234567890123456",
      "amount": 500,
      "transaction_type": "WITHDRAW",
      "timestamp": "2026-04-08T13:00:00.000Z"
    }
  ]
}
```

**Error Responses:**
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token or token verification failed
- **404 Not Found:** Account not found

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 4. Get All Transactions (Admin)

Retrieve all transactions with filtering and pagination (Admin only).

**Endpoint:** `GET /api/transactions`  
**Authentication:** Required (Valid JWT Token - Admin role)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Token Validation:**
- Token must be valid and not expired
- Token is verified on every request
- Invalid or expired tokens will return 401/403 errors
- Token must contain `card_number` and `account_number` claims

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `card_number` | string | All | Filter by card number |
| `type` | string | All | Filter by type: `DEPOSIT` or `WITHDRAW` |
| `date_from` | string | - | Start date (date format) |
| `date_to` | string | - | End date (date format) |
| `limit` | integer | 50 | Number of records to return |
| `offset` | integer | 0 | Number of records to skip |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Transactions retrieved",
  "data": [
    {
      "id": 1,
      "card_number": "1234567890123456",
      "amount": 1000,
      "transaction_type": "DEPOSIT",
      "timestamp": "2026-04-08T12:00:00.000Z"
    }
  ],
  "pagination": {
    "total": 100,
    "limit": 50,
    "offset": 0
  }
}
```

**Error Responses:**
- **401 Unauthorized:** Missing token, expired token, or no authorization header
- **403 Forbidden:** Invalid token, token verification failed, or insufficient permissions
- **404 Not Found:** Account not found

**Token Error Responses:**
```json
// No authorization header
{
  "success": false,
  "message": "No authorization header provided"
}

// No token provided
{
  "success": false,
  "message": "No token provided"
}

// Token expired
{
  "success": false,
  "message": "Token expired"
}

// Invalid token
{
  "success": false,
  "message": "Invalid token"
}
```

---

### 5. Cardless Deposit

Deposit money to an account without a card using NID verification.

**Endpoint:** `POST /api/transactions/cardless-deposit`  
**Authentication:** None (Public with NID verification)

**Request Body:**
```json
{
  "account_number": "123456789012",
  "nid_proof": "3456",
  "amount": 1000
}
```

**Validation Rules:**
| Field | Type | Required | Rules |
|-------|------|----------|-------|
| `account_number` | string | Yes | Exactly 12 digits, pattern: `^[0-9]{12}$` |
| `nid_proof` | string | Yes | Exactly 4 digits, pattern: `^[0-9]{4}$` |
| `amount` | number | Yes | 500-25,000 TK, must be multiple of 500 |

**Business Rules:**
- Amount must be between 500 and 25,000 TK
- Amount must be a multiple of 500
- NID must match the account holder's NID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Cardless deposit successful",
  "data": {
    "transaction": {
      "id": 3,
      "card_number": "1234567890123456",
      "amount": 1000,
      "transaction_type": "DEPOSIT",
      "timestamp": "2026-04-08T12:00:00.000Z"
    },
    "balance": 6000
  }
}
```

**Error Responses:**
- **400 Bad Request:** Validation errors or NID verification failed
- **404 Not Found:** Account not found

---

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation errors) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 409 | Conflict (duplicate data) |
| 429 | Too Many Requests (daily limits exceeded) |
| 500 | Internal Server Error |
| 503 | Service Unavailable (database connection issues) |

---

## Rate Limiting

- **Global Rate Limit:** 100 requests per 15-minute window on `/api/*` routes
- **Transaction Limits:** Daily limits of 50,000 TK or 5 transactions per day for deposits and withdrawals

---

## Security Features

- JWT authentication for protected routes
- PIN lockout after 3 failed attempts (account blocked)
- NID verification for sensitive operations
- Input validation on all endpoints
- HTTP security headers via Helmet
- CORS configuration
- Request logging via Winston

---

## Data Models

### Account Response
```typescript
interface AccountResponse {
  account_number: string;
  card_number: string;
  name: string;
  phone_number: string;
  email?: string;
  gender: string;
  profession: string;
  nationality: string;
  balance: number;
  created_at: Date;
}
```

### Transaction
```typescript
interface Transaction {
  id: number;
  card_number: string;
  amount: number;
  transaction_type: 'DEPOSIT' | 'WITHDRAW';
  timestamp: Date;
}
```

### API Response
```typescript
interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
  errors?: any[];
}
```

---

## Notes

- All monetary amounts are in Bangladeshi Taka (TK)
- Account numbers are 12 digits
- Card numbers are 16 digits
- PINs are 4 digits
- Minimum initial deposit: 100 TK
- Minimum account balance: 500 TK
- Sensitive fields (PIN, blocked status, failed attempts) are never exposed in API responses

---

**Generated:** April 8, 2026  
**API Version:** 1.0.0
