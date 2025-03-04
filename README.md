# Array Banking Application

WIP banking API built with Spring Boot.

## Choices
PostgreSQL over MSSQL: I haven't used MSSQL before, so I went PostgreSQL for familiarity.
Java Spring Boot: Comfortable with the framework, good support for everything required such as
- Hibernate (ORM)
- Spring Security / JJWT for "simple" auth
- Testing via JUnit + Mockito (I ran out of time to write a test suite, but that the idea) 

## Features

- **User Management**
  - Account registration and authentication
  - Secure password storage
  - Balance tracking

- **Transaction Processing**
  - View transaction history
  - Random transaction generation for demo purposes
  - Paginated transaction results

- **Funds Transfer**
  - Transfer between users
  - Overdraft protection
  - Self-transfer prevention

- **Security**
  - JWT-based authentication
  - IP-based rate limiting
  - Password encryption
  - SQL injection protection

## Architecture

The application follows a standard layered architecture:

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

## Technologies

- **Spring Boot**: Core framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **PostgreSQL**: Database
- **JJWT**: JWT implementation
- **Bucket4j**: Rate limiting
- **Lombok**: Boilerplate code reduction

## License

This project is licensed under the MIT License.