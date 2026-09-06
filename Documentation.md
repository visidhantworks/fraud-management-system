# Fraud Risk Management (FRM) & Payment System

A production-oriented Fraud Risk Management and Payment Processing system built with **Java, Spring Boot, Spring Security, JWT, PostgreSQL, JPA/Hibernate, HTML5, CSS3, and Vanilla JavaScript**.

The system simulates a payment platform in which authenticated users can make payments while every transaction is evaluated by a configurable fraud-risk engine. An administrator can monitor transactions and fraud decisions through a dedicated dashboard.

---

## 1. Project Overview

The application combines two major capabilities:

1. **Payment Processing**
   - Authenticated users can initiate payments.
   - Payments require a user-defined PIN.
   - PINs are stored as BCrypt hashes rather than plaintext.
   - Each payment includes transaction amount and geographic coordinates.
   - Transaction history is persisted.

2. **Fraud Risk Management**
   - Every payment is evaluated against configurable fraud rules.
   - Risk factors currently include:
     - Transaction amount
     - Transaction frequency
     - Failed attempts
     - Location
   - Individual rule results are persisted.
   - A total risk score determines whether the payment succeeds, fails, or is blocked.
   - High-risk activity can temporarily lock transactions and invalidate the active session.

The project is designed to demonstrate a realistic backend architecture rather than a simple CRUD application.

---

## 2. Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Web / REST
- Spring Security
- JWT authentication
- Spring Data JPA
- Hibernate
- PostgreSQL
- BCrypt password/PIN hashing
- Maven

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript
- Fetch API
- Browser Geolocation API

No frontend framework is required.

### Database

- PostgreSQL
- Local PostgreSQL for development
- Neon PostgreSQL for the deployed environment

### Deployment

- GitHub for source control
- Render for application deployment
- Neon for production PostgreSQL

---

## 3. High-Level Architecture

The backend follows a layered architecture:

```text
                    +----------------------+
                    |      Frontend        |
                    | HTML / CSS / JS      |
                    +----------+-----------+
                               |
                               | HTTP / JSON
                               v
                    +----------------------+
                    |    REST Controller   |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |       Service        |
                    | Business Logic       |
                    +----------+-----------+
                               |
              +----------------+----------------+
              |                                 |
              v                                 v
    +--------------------+             +--------------------+
    |     Repository     |             |   Security Layer   |
    | Spring Data JPA    |             | JWT / Spring Sec.  |
    +---------+----------+             +--------------------+
              |
              v
    +--------------------+
    |     PostgreSQL     |
    +--------------------+
```

The transaction flow adds the fraud engine inside the service layer:

```text
Payment Request
      |
      v
JWT Authentication
      |
      v
Active Session Validation
      |
      v
Transaction Lock Check
      |
      v
PIN Verification
      |
      v
Fraud Risk Evaluation
      |
      +---- Amount Risk
      |
      +---- Frequency Risk
      |
      +---- Failed Attempt Risk
      |
      +---- Location Risk
      |
      v
Total Risk Score
      |
      +---- < 75      -> SUCCESS
      |
      +---- 75 - 99   -> FAILED
      |
      +---- >= 100    -> BLOCKED
                              |
                              v
                       Session Deactivated
                       Transaction Locked
```

---

## 4. Package Structure

The backend follows a clear separation of responsibilities.

```text
com.sidhant.fraudmanagement
│
├── controller
│   ├── AuthController
│   ├── TransactionController
│   └── AdminController
│
├── service
│   ├── AuthService
│   ├── TransactionService
│   ├── FraudRiskService
│   └── Location
│
├── repository
│   ├── UserRepository
│   ├── TransactionRepository
│   ├── FraudAssessmentRepository
│   ├── FraudRuleResultRepository
│   ├── FrmRuleRepository
│   └── ActiveSessionRepository
│
├── entity
│   ├── User
│   ├── Transaction
│   ├── FraudAssessment
│   ├── FraudRuleResult
│   ├── FrmRule
│   └── ActiveSession
│
├── dto
│   ├── request
│   │   ├── LoginRequest
│   │   └── PaymentRequest
│   │
│   └── response
│       ├── LoginResponse
│       └── TransactionResponse
│
├── security
│   ├── JwtService
│   ├── JwtAuthenticationFilter
│   ├── CustomUserDetailsService
│   └── SecurityConfig
│
└── exception
    ├── GlobalExceptionHandler
    ├── InvalidPinException
    ├── TransactionFailedException
    ├── TransactionBlockedException
    ├── ActiveSessionException
    ├── AdminPaymentException
    └── UserNotFoundException
```

---

# 5. Authentication and Authorization

Authentication uses **JWT tokens** combined with an application-level active-session mechanism.

## Login Flow

```text
User enters email + password
          |
          v
AuthController
          |
          v
AuthService
          |
          +--> Find user
          |
          +--> BCrypt password verification
          |
          +--> Check active session
          |
          +--> Generate JWT
          |
          +--> Create/update active_sessions row
          |
          v
Return JWT + user information
```

Passwords are never compared as plaintext. BCrypt is used to verify the stored password hash.

## JWT

The JWT contains authenticated user information and is sent by the frontend as:

```http
Authorization: Bearer <token>
```

The `JwtAuthenticationFilter`:

1. Reads the Authorization header.
2. Extracts the JWT.
3. Validates the token.
4. Extracts the user's email.
5. Loads the user.
6. Checks whether the user's application-level session is active.
7. Checks the 30-minute inactivity timeout.
8. Updates `last_activity_at`.
9. Places the authenticated user into Spring Security's `SecurityContext`.

---

# 6. Application-Level Active Sessions

Spring Security is configured as stateless:

```text
SessionCreationPolicy.STATELESS
```

This does not mean the application cannot track sessions.

The project maintains its own `active_sessions` table.

The purpose is to enforce:

- One active login per user.
- Explicit logout.
- Session invalidation after suspicious activity.
- Inactivity-based session expiration.

## Session lifecycle

```text
LOGIN
  |
  v
is_active = true
  |
  v
Requests update last_activity_at
  |
  +---- 30 minutes inactivity ----> is_active = false
  |
LOGOUT
  |
  v
is_active = false
```

When the same user logs in again after logout, the existing session row can be reused with a new session ID.

---

# 7. Payment PIN Security

Users provide a PIN when making payments.

The PIN is never stored directly in the database.

Instead:

```text
User PIN
   |
   v
BCrypt
   |
   v
pin_hash
```

During payment:

```text
Entered PIN
     |
     v
BCrypt.matches()
     |
     +---- valid ----> Continue fraud evaluation
     |
     +---- invalid --> INVALID_PIN
```

A wrong PIN does not automatically log the user out.

---

# 8. Payment Request

A payment request contains the information required for processing and fraud analysis.

Conceptually:

```json
{
  "amount": 1000,
  "pin": "1234",
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

The exact PIN value is never persisted as plaintext.

Location is provided by the client and represented internally by:

```java
public record Location(
    Double latitude,
    Double longitude
) {}
```

The current implementation works with latitude/longitude coordinates rather than city names.

---

# 9. Fraud Risk Management Engine

The fraud engine evaluates every payment using configurable database rules.

The four current risk factors are:

```text
1. Amount
2. Frequency
3. Failed Attempts
4. Location
```

Each rule has:

- Rule code
- Threshold
- Optional time window
- Risk points
- Enabled flag

This makes the fraud engine configurable without hardcoding all rule values into Java.

---

# 10. Risk Scoring

The system calculates individual rule contributions and combines them into a total risk score.

Current scoring model:

```text
Amount Risk        = 30
Frequency Risk     = 20 × frequency escalation
Failed Attempts    = 25
Location Risk      = 25

Maximum demo score is not artificially capped.
```

The decision thresholds are:

```text
Risk Score < 75
    -> SUCCESS

Risk Score 75 - 99
    -> FAILED

Risk Score >= 100
    -> BLOCKED
```

This separation between risk calculation and decision thresholds allows the fraud engine to evolve independently.

---

# 11. Configurable Fraud Rules

Current production rule configuration:

| Rule Code | Threshold | Time Window | Risk Points |
|---|---:|---:|---:|
| HIGH_FAILED_ATTEMPTS | 3 | 10 min | 25 |
| HIGH_TRANSACTION_AMOUNT | 50,000 | — | 30 |
| HIGH_TRANSACTION_FREQUENCY | 5 | 2 min | 20 |
| LOCATION_BUCKET_RADIUS | 100 km | — | 0 |
| UNUSUAL_LOCATION | 1000 km | — | 25 |

All five rules are currently enabled.

The `LOCATION_BUCKET_RADIUS` rule is an internal configuration used by the location algorithm and contributes zero direct risk points.

---

# 12. Transaction Amount Risk

The current amount rule is:

```text
Amount >= 50,000
    -> +30 risk
```

The threshold is stored in `frm_rules`, so it can be changed through database configuration rather than modifying Java source code.

The current implementation intentionally treats this as an individual high-value transaction rule.

A future enhancement could make repeated high-value payments cumulative.

---

# 13. Transaction Frequency Risk

The frequency rule detects unusually rapid payment activity.

Current configuration:

```text
Threshold: 5
Window: 2 minutes
Risk: 20 points
```

The implementation uses cumulative escalation.

Example:

```text
1st - 5th payment  -> +0
6th payment        -> +20
7th payment        -> +40
8th payment        -> +60
9th payment        -> +80
10th payment       -> +100
11th payment       -> +120
...
```

This makes repeated rapid transactions increasingly suspicious.

For example:

```text
Frequency score = max(0, paymentCount - 5) × 20
```

subject to the exact implementation used by the service.

---

# 14. Failed Attempt Risk

Failed attempts are evaluated over a configurable time window.

Current rule:

```text
3 failed attempts
within 10 minutes
    -> +25 risk
```

This allows the system to detect behavior such as repeated unsuccessful payment attempts.

The failed-attempt calculation uses historical transactions within the configured window rather than permanently treating every historical failure as suspicious.

---

# 15. Location Risk

Location analysis is based on latitude and longitude.

The current requirement is to compare the current payment location with the locations of the user's most recent transactions.

The system considers the latest **20 previous transactions**.

It does not currently depend on city names or reverse geocoding.

---

## Location Bucketing

The system uses a configurable bucket radius:

```text
LOCATION_BUCKET_RADIUS = 100 km
```

Previous transaction coordinates are grouped into approximate geographic buckets.

Conceptually:

```text
Previous Transactions
       |
       v
Compare coordinates
       |
       +---- within 100 km ----> Existing bucket
       |
       +---- outside 100 km ---> New bucket
       |
       v
Find dominant bucket
       |
       v
Compare current location
       |
       v
Haversine distance
```

A `HashMap<Location, Integer>` is used to maintain bucket representatives and occurrence counts.

The dominant location is the bucket with the highest number of previous transactions.

---

# 16. Haversine Distance

Geographic distance is calculated using the Haversine formula.

Conceptually:

```text
latitude + longitude
        |
        v
Haversine calculation
        |
        v
Distance in kilometers
```

If the current transaction is sufficiently far from the user's dominant historical location:

```text
Distance > UNUSUAL_LOCATION threshold
    -> +25 risk
```

Current threshold:

```text
1000 km
```

This is a deliberately simple fraud heuristic suitable for the current project and demonstration.

---

# 17. Transaction Locking

A high-risk transaction can place the user under a temporary transaction restriction.

The `users` table contains:

```text
transaction_locked_until
```

When a transaction reaches the blocked risk level:

```text
Risk >= 100
     |
     v
Transaction BLOCKED
     |
     +--> Session deactivated
     |
     +--> transaction_locked_until set
     |
     v
User must wait until lock expires
```

The lock is checked before processing a new payment.

If the lock is still active:

```text
Transactions are temporarily restricted
```

and the active session is deactivated.

When the lock has expired, the lock value is cleared.

Historical transaction records and their historical risk scores are not rewritten.

This is important: the system resets the user's **current ability to transact**, not the historical fraud audit trail.

---

# 18. Database Design

The PostgreSQL database currently contains six primary tables:

```text
users
transactions
fraud_assessments
fraud_rule_results
frm_rules
active_sessions
```

The high-level relationship is:

```text
                     +----------------+
                     |     users      |
                     +-------+--------+
                             |
              +--------------+--------------+
              |                             |
              v                             v
     +----------------+            +----------------+
     |  transactions  |            | active_sessions|
     +-------+--------+            +----------------+
             |
             | 1 : 1
             v
     +--------------------+
     | fraud_assessments  |
     +---------+----------+
               |
               | 1 : N
               v
     +--------------------+
     | fraud_rule_results |
     +--------------------+

     +----------------+
     |    frm_rules   |
     +----------------+
```

---

# 19. `users` Table

The `users` table stores application users and their authentication/security information.

Important fields include:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `name` | User name |
| `email` | Unique login identifier |
| `password_hash` | BCrypt password hash |
| `role` | USER / ADMIN authorization role |
| `pin_hash` | BCrypt payment PIN hash |
| `transaction_locked_until` | Temporary transaction restriction |

Constraints:

```text
PRIMARY KEY (id)
UNIQUE (email)
```

The system currently uses seeded users rather than a public registration workflow.

There are two payment users and an administrator.

---

# 20. `transactions` Table

Stores payment transactions.

Important fields include:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `transaction_id` | Unique transaction identifier |
| `user_id` | User who initiated the payment |
| `amount` | Payment amount |
| `latitude` | Transaction latitude |
| `longitude` | Transaction longitude |
| `status` | SUCCESS / FAILED / BLOCKED |
| `risk_score` | Total fraud risk score |
| `created_at` | Transaction timestamp |

Relationship:

```text
users 1 ---- N transactions
```

One user can have many transactions.

---

# 21. `fraud_assessments` Table

Stores the overall fraud assessment associated with a transaction.

Relationship:

```text
transactions 1 ---- 1 fraud_assessments
```

The assessment represents the fraud-analysis result for the transaction.

It provides an audit layer between the payment transaction and the individual rule results.

---

# 22. `fraud_rule_results` Table

Stores the result of individual fraud rules for an assessment.

Relationship:

```text
fraud_assessments 1 ---- N fraud_rule_results
```

For example, one assessment could contain:

```text
HIGH_TRANSACTION_AMOUNT       -> 30 points
HIGH_TRANSACTION_FREQUENCY    -> 40 points
HIGH_FAILED_ATTEMPTS          -> 0 points
UNUSUAL_LOCATION              -> 25 points
```

This structure is preferable to storing only the final score because it makes the decision explainable and auditable.

---

# 23. `frm_rules` Table

Stores configurable fraud-rule definitions.

Conceptual schema:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `rule_code` | Unique rule identifier |
| `threshold` | Rule threshold |
| `time_window_minutes` | Optional temporal window |
| `risk_points` | Risk contribution |
| `enabled` | Enables/disables rule |

Constraint:

```text
PRIMARY KEY (id)
UNIQUE (rule_code)
```

The current rule records are:

```text
HIGH_FAILED_ATTEMPTS
HIGH_TRANSACTION_AMOUNT
HIGH_TRANSACTION_FREQUENCY
LOCATION_BUCKET_RADIUS
UNUSUAL_LOCATION
```

---

# 24. `active_sessions` Table

Stores the application's active-session state.

Important fields:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `user_id` | Associated user |
| `session_id` | Unique application session |
| `is_active` | Current session state |
| `login_at` | Login timestamp |
| `logout_at` | Logout timestamp |
| `last_activity_at` | Last authenticated activity |

Constraints:

```text
PRIMARY KEY (id)
UNIQUE (session_id)
UNIQUE (user_id)
FOREIGN KEY (user_id) REFERENCES users(id)
```

The unique `user_id` constraint ensures that each user has at most one session row.

---

# 25. Complete Database Relationship Model

```text
USERS
 |
 | 1
 |
 +------------------------+
 |                        |
 | N                      | 1
 v                        v
TRANSACTIONS        ACTIVE_SESSIONS
 |
 | 1
 v
FRAUD_ASSESSMENTS
 |
 | N
 v
FRAUD_RULE_RESULTS


FRM_RULES
   |
   | configuration referenced by fraud evaluation
   |
   +------------------------------+
                                  |
                           Fraud Risk Engine
```

Foreign-key relationships:

```text
transactions.user_id
    -> users.id

active_sessions.user_id
    -> users.id

fraud_assessments.transaction_id
    -> transactions.id

fraud_rule_results.fraud_assessment_id
    -> fraud_assessments.id
```

---

# 26. Database Integrity

The current PostgreSQL design uses:

- Primary keys
- Foreign keys
- Unique constraints
- Not-null constraints
- Database-backed rule configuration
- Generated identity/sequence-backed IDs
- Timestamp fields for auditing

Current indexes include:

```text
active_sessions_pkey
active_sessions_session_id_key
active_sessions_user_id_key

fraud_assessments_pkey

fraud_rule_results_pkey

frm_rules_pkey
frm_rules_rule_code_key

transactions_pkey
transactions_transaction_id_key

users_email_key
users_pkey
```

The local and Neon databases were aligned for:

- Columns
- Data types
- Nullability
- Defaults
- Constraints
- Foreign keys
- Unique constraints
- Indexes
- FRM rule configuration

---

# 27. Transaction Processing Sequence

A normal payment follows this sequence:

```text
1. User logs in
       |
2. JWT generated
       |
3. Active session created
       |
4. User submits payment
       |
5. JWT validated
       |
6. Active session validated
       |
7. Inactivity timeout checked
       |
8. Transaction lock checked
       |
9. PIN verified
       |
10. Transaction created
       |
11. Fraud rules evaluated
       |
12. Individual rule results stored
       |
13. Total risk score calculated
       |
14. Decision made
       |
       +---- SUCCESS
       |
       +---- FAILED
       |
       +---- BLOCKED
                 |
                 +--> session invalidated
                 +--> temporary transaction lock
```

---

# 28. Exception Handling

The backend uses centralized exception handling through `@RestControllerAdvice`.

Current application exceptions include:

- `InvalidPinException`
- `TransactionFailedException`
- `TransactionBlockedException`
- `UserNotFoundException`
- `AdminPaymentException`
- `ActiveSessionException`

Responses include machine-readable error codes.

Examples:

```json
{
  "code": "INVALID_PIN",
  "error": "..."
}
```

```json
{
  "code": "PAYMENT_FAILED",
  "error": "..."
}
```

```json
{
  "code": "PAYMENT_BLOCKED",
  "error": "..."
}
```

This allows the frontend to distinguish different failure scenarios without depending only on human-readable messages.

---

# 29. HTTP Decision Mapping

The current API behavior uses:

```text
200 OK
    -> Successful operation

400 BAD REQUEST
    -> Payment failed

401 UNAUTHORIZED
    -> Invalid authentication / invalid PIN / expired session

403 FORBIDDEN
    -> Transaction blocked / authorization restriction

404 NOT FOUND
    -> User/resource not found

409 CONFLICT
    -> Active session conflict
```

---

# 30. REST API Structure

The frontend communicates with the backend using JSON REST APIs.

Current core endpoints include:

```text
POST /api/auth/login
POST /api/auth/logout

POST /api/transactions
GET  /api/transactions/my

GET  /api/admin/transactions
```

Authentication endpoints are publicly accessible where required, while protected transaction and administrative endpoints require authentication.

The frontend is served from the same Spring Boot application:

```text
/frontend/login.html
/frontend/user-dashboard.html
/frontend/admin-dashboard.html
```

Using the same origin allows:

```javascript
const API_BASE_URL = "";
```

---

# 31. Frontend Architecture

The frontend is intentionally framework-free.

```text
HTML
 |
 +---- CSS
 |
 +---- auth.js
 |
 +---- api.js
 |
 +---- Dashboard JavaScript
 |
 +---- Fetch API
 |
 v
Spring Boot REST API
```

`api.js` provides a central API layer.

It is responsible for:

- Attaching JWT tokens.
- Sending JSON requests.
- Parsing JSON responses.
- Handling API errors.
- Clearing authentication storage when a session expires.

The frontend stores the JWT and basic user information in browser `localStorage`.

---

# 32. Authentication Storage

The frontend currently uses:

```text
frm_token
frm_user
```

The token is attached to authenticated API requests:

```http
Authorization: Bearer <JWT>
```

When the backend reports a session expiration or invalid session, the frontend clears local authentication state and redirects the user to the login page.

---

# 33. Admin vs User

The application has two major roles.

## USER

A normal user can:

- Log in.
- Make payments.
- Enter a payment PIN.
- Submit transaction location.
- View personal transaction history.
- Log out.

## ADMIN

The administrator can:

- Log in.
- Monitor transactions.
- View fraud-related transaction information.
- Access the administrative dashboard.

The admin is not a payment user and is prevented from making payments.

---

# 34. Security Model

The project applies multiple layers of security:

```text
Layer 1
JWT authentication
        |
Layer 2
Spring Security authorization
        |
Layer 3
Application-level active session
        |
Layer 4
Session inactivity timeout
        |
Layer 5
Payment PIN verification
        |
Layer 6
Fraud risk evaluation
        |
Layer 7
Temporary transaction lock
```

This means authentication alone is not sufficient to complete a payment.

---

# 35. Important Security Properties

### Password security

Passwords are stored as BCrypt hashes.

### PIN security

Payment PINs are stored as BCrypt hashes.

### JWT

Authentication is token-based.

### Stateless Spring Security

Spring Security does not create server-side HTTP sessions.

### Application sessions

The application separately maintains session state in PostgreSQL.

### Session expiry

Inactive sessions expire after 30 minutes.

### Fraud lock

Blocked activity can invalidate the current session and temporarily restrict further payments.

### Role-based access

Administrative endpoints are protected separately from user payment functionality.

---

# 36. Transaction Consistency

The payment service uses Spring's transaction management:

```java
@Transactional(
    noRollbackFor = {
        TransactionBlockedException.class,
        TransactionFailedException.class,
        InvalidPinException.class
    }
)
```

This is important because fraud outcomes are business results that still need to be persisted.

For example, if a transaction is blocked:

```text
Create transaction
      |
Fraud assessment
      |
Rule results
      |
Risk score
      |
BLOCKED
      |
Exception used for HTTP response
```

The transaction and fraud-audit records should remain persisted instead of being rolled back simply because a business exception is used to communicate the blocked result.

---

# 37. Fraud Audit Trail

The project intentionally separates:

```text
Transaction
      |
      +--> Final status
      |
      +--> Total risk score

Fraud Assessment
      |
      +--> Overall assessment

Fraud Rule Results
      |
      +--> Individual rule contributions
```

This gives the administrator more information than simply seeing:

```text
Payment = BLOCKED
```

The system can instead answer:

```text
Why was it blocked?
Which rules triggered?
How many points did each rule contribute?
What was the final risk score?
```

This is an important characteristic of an FRM system.

---

# 38. Example Risk Evaluation

Suppose a user makes a transaction with:

```text
Amount = ₹100,000
Frequency contribution = 40
Failed-attempt contribution = 0
Location contribution = 0
```

Then:

```text
30 + 40 + 0 + 0 = 70
```

Decision:

```text
70 < 75
    -> SUCCESS
```

Another transaction:

```text
Amount = ₹1,000
Frequency contribution = 80
Failed-attempt contribution = 0
Location contribution = 0
```

Total:

```text
80
```

Decision:

```text
75 <= 80 < 100
    -> FAILED
```

A transaction with:

```text
Amount = ₹100,000
Frequency contribution = 100
Location contribution = 25
```

Total:

```text
155
```

Decision:

```text
155 >= 100
    -> BLOCKED
```

The exact score depends on the historical transactions and configured rules at runtime.

---

# 39. Historical Data vs Current Risk State

The system deliberately keeps historical fraud decisions immutable.

For example:

```text
Transaction #101
Risk Score = 80
Status = FAILED
```

remains:

```text
Risk Score = 80
Status = FAILED
```

even after the user's temporary lock expires.

When the lock expires, only the current transaction restriction is cleared:

```text
transaction_locked_until = NULL
```

New transactions are evaluated using their own relevant time windows and current state.

This preserves the audit trail.

---

# 40. Production Configuration

Environment-specific configuration should not be hardcoded into source control.

The production deployment uses environment configuration for values such as:

```text
DATABASE_URL / datasource configuration
DATABASE_USERNAME
DATABASE_PASSWORD
JWT secret
```

Sensitive credentials should be stored in the deployment platform's environment variables rather than committed to GitHub.

The local `application.properties` is intentionally treated separately from the committed source when it contains environment-specific configuration.

---

# 41. Deployment Architecture

The deployed architecture is:

```text
                    Internet
                       |
                       v
                  Render App
                       |
          +------------+------------+
          |                         |
          v                         v
   Spring Boot App            Static Frontend
          |
          v
      PostgreSQL
        Neon
```

GitHub acts as the source-control repository.

Render builds and runs the Spring Boot application.

Neon provides the production PostgreSQL database.

---

# 42. Local Development

Run the backend with Maven:

```bash
./mvnw spring-boot:run
```

or:

```bash
mvn spring-boot:run
```

Build the application:

```bash
mvn clean package
```

The frontend can be accessed through Spring Boot:

```text
http://localhost:8080/frontend/login.html
```

Serving the frontend through Spring Boot keeps the frontend and API on the same origin during local development.

---

# 43. Database Environment

The project uses PostgreSQL in both development and deployment.

Development:

```text
Local PostgreSQL
```

Production:

```text
Neon PostgreSQL
```

The production schema must contain all application tables before the Render application starts because Hibernate validates the expected schema.

---

# 44. Why This Architecture?

The project intentionally avoids placing everything inside a controller.

Instead:

```text
Controller
    -> HTTP responsibility

Service
    -> Business responsibility

Repository
    -> Database responsibility

Entity
    -> Persistence model

DTO
    -> API contract

Security
    -> Authentication/authorization

Exception
    -> Error handling
```

This makes the code easier to:

- Test
- Maintain
- Extend
- Debug
- Explain in technical interviews

---

# 45. Design Decisions

### Database-driven fraud rules

Risk thresholds are stored in `frm_rules` rather than hardcoded.

### Layered architecture

Business logic is separated from HTTP and persistence.

### JWT + application session

JWT provides authentication while the database session provides active-session control and server-side revocation.

### BCrypt

Passwords and PINs are stored as hashes.

### Separate fraud tables

Transaction, assessment, and rule results are separated to preserve auditability.

### Location coordinates

Coordinates allow geographic analysis without requiring city-name data.

### Latest 20 transactions

Location analysis focuses on recent user behavior rather than the entire historical dataset.

### Temporary lock

High-risk activity causes a temporary restriction rather than permanently disabling the account.

---

# 46. Current Limitations / Future Improvements

The current implementation is intentionally a practical FRM demonstration rather than a full banking-grade fraud platform.

Potential future improvements include:

- Redis-based distributed session/state management.
- Refresh-token rotation.
- Device fingerprinting.
- IP reputation analysis.
- Merchant/category risk.
- Velocity rules across multiple dimensions.
- Cumulative high-value transaction detection.
- More sophisticated geographic clustering.
- Reverse geocoding for human-readable locations.
- Machine-learning-based fraud scoring.
- Rule management UI for administrators.
- Database migration tooling such as Flyway or Liquibase.
- Comprehensive automated integration tests.
- Rate limiting.
- Audit logging for security events.
- MFA/2FA.
- Notification/alert infrastructure.
- Idempotency keys for payment requests.
- Distributed locking for concurrent payment requests.
- Production-grade observability and metrics.

---

# 47. Interview-Level Explanation

A concise explanation of the project:

> "I built a Fraud Risk Management and Payment System using Spring Boot and PostgreSQL. Users authenticate using JWT and have a BCrypt-protected payment PIN. Every payment is evaluated by a configurable fraud engine that considers transaction amount, transaction frequency, failed attempts, and geographic location. Each rule contributes to a risk score, and configurable thresholds determine whether a transaction succeeds, fails, or is blocked. Fraud assessments and individual rule results are persisted separately to maintain an audit trail. I also implemented application-level active-session management, a 30-minute inactivity timeout, and temporary transaction locking after high-risk activity. The frontend is built using HTML, CSS, and Vanilla JavaScript and communicates with the Spring Boot REST API using Fetch."

---

# 48. Core Engineering Concepts Demonstrated

This project demonstrates practical experience with:

```text
Java 21
Spring Boot
Spring MVC
REST APIs
Spring Security
JWT
BCrypt
Role-Based Authorization
JPA
Hibernate
Spring Data JPA
PostgreSQL
Database Relationships
Foreign Keys
Unique Constraints
Indexes
Transactions
Exception Handling
DTOs
Layered Architecture
Fraud Risk Scoring
Rule-Based Decision Engines
Geospatial Distance
Haversine Formula
Session Management
Authentication
Authorization
Frontend API Integration
Fetch API
Browser Geolocation
Git
GitHub
Render
Neon PostgreSQL
```

---

# 49. Project Philosophy

The main goal of the system is not simply to process payments.

It is to demonstrate how a payment can be evaluated as a **risk-aware business transaction**.

Instead of:

```text
Request -> Database -> Success
```

the system implements:

```text
Request
   |
Authentication
   |
Session Validation
   |
PIN Verification
   |
Risk Evaluation
   |
Risk Scoring
   |
Fraud Audit
   |
Decision
   |
Transaction Outcome
```

This architecture provides a foundation for extending the application toward a more sophisticated fraud-management platform.

---

## License

Add the project's chosen license here before publishing the repository publicly.

---

## Author

**Sidhant Mahajan**

Fraud Risk Management & Payment System  
Java • Spring Boot • PostgreSQL • Spring Security • JWT
