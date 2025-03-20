# Banking Application

A simple banking app I built as a week long challenge to test my full-stack abilities, and learn more about docker compose and observability.

SSR frontend information found in `/banking/README.md`
SPA frontend information found in `/frontend/README.md`

## Technologies

- **Spring Boot**: Core framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **Hibernate**: Database ORM
- **PostgreSQL**: Database
- **JJWT**: JWT auth implementation
- **Bucket4j**: Rate limiting
- **Lombok**: Boilerplate code reduction
- **JUnit/Mockito**: Testing Framework
- **HTMX/Thymeleaf**: HTML template-based, server-rendered frontend

## Features

- **User Management**
  - Account registration and authentication
  - Secure password storage
  - Balance tracking

- **Transaction Processing**
  - View transaction history
  - Random transaction generation
  - Paginated transaction results

- **Funds Transfer**
  - Transfer between users
  - Overdraft protection
  - Self-transfer prevention

- **Security**
  - JWT-based authentication
  - IP-based rate limiting
  - Password encryption

## Folder Architecture
```
com.array.banking
├── config/           # Application configuration classes
├── controller/       # REST API endpoints
├── dto/              # Data transfer objects
├── model/            # Domain entities
├── repository/       # Data access layer
├── security/         # Security components
└── service/          # Business logic
```

## Database Schema

The application uses a PostgreSQL database with two main tables:
- `users` - Stores user information and account balances
- `transactions` - Records all financial transactions

Database transactions and triggers act as a second layer of protection for preventing overdrafts.

## API Endpoints

### Authentication

- `POST /banking/v1/auth/register` - Register a new user
- `POST /banking/v1/auth/login` - Authenticate user and get JWT token

### Banking Operations (secured requiring login/JWT)

- `GET /banking/v1/balance` - Get current user balance
- `GET /banking/v1/transactions` - Get paginated transaction history
- `POST /banking/v1/transfer` - Transfer funds to another user

## Getting Started

### Running
1. Start the Postgres database `docker compose up -d`
2. Start the Spring Boot app
  - `cd banking`
  - `./mvnw spring-boot:run`
3. Visit the Swagger UI
  - http://localhost:8080/swagger-ui/index.html#
4. Register some users -> login -> interact with banking-controller endpoints

### Run Tests
  - `cd banking`
  - `./mvnw test`

### Direct API Usage Example

1. Register a user:
   ```
   curl -X POST http://localhost:8080/banking/v1/auth/register \
   -H "Content-Type: application/json" \
   -d '{"username":"user1","password":"password123","email":"user1@example.com"}'
   ```

2. Login to get JWT token:
   ```
   curl -X POST http://localhost:8080/banking/v1/auth/login \
   -H "Content-Type: application/json" \
   -d '{"username":"user1","password":"password123"}'
   ```

3. Check balance (using token):
   ```
   curl -X GET http://localhost:8080/banking/v1/balance \
   -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

# How To Run
- `docker compose up`
- `cd banking`
- `./mvnw spring-boot:run`
- http://localhost:8080/ssr/login

## Technologies
- **Thymeleaf**: Java Spring server-side template engine

## SSR vs SPA frontend versions
Both versions have the same functionality, but different UIs. The SPA uses Material UI and the SSR version uses pure CSS. Given more time I'd look into including Tailwind and DaisyUI to make it more visually appealing and clean up the CSS.

If both versions are running, you can switch between the two by changing the path from `ssr` to `spa` or vice versa and you'll be forwarded to the matching port automatically. 

### SSR
- SSR urls start with http://localhost:8080/ssr.
- Started from /banking (detailed start up instructions are in `/banking/README.md`)
- SSR templates can be found in `/banking/src/main/resources/templates/ssr`

### SPA:
- SPA urls start with http://localhost:3000/spa.
- Started from /frontend (detailed start up instructions are in `/frontend/README.md`)

### Viable Paths 
SPA Only (http://localhost:3000):
- /spa or /spa/landing will lead to a landing page allowing a selection between the two versions

Shared:
- /login
- /register
- /dashboard
- /transactions
- /transfer

## Troubleshooting
#### ERR_CONNECTION_REFUSED
There is some logic hard-coded with these port numbers, so make sure the app is actually running on the expected ports. Sometimes it'll automatically choose a new port if you have something running on it already when starting the app.
