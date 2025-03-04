# Security Overview

## Authentication & Authorization

### JWT (JSON Web Token)
- **Libraries**: io.jsonwebtoken (JJWT)
- **Implementation**: Token-based authentication using JWT for stateless sessions
- **Components**:
  - `JwtTokenProvider`: Generates and validates JWT tokens
  - `JwtTokenFilter`: Intercepts requests to extract and validate JWT tokens

### Spring Security
- Stateless session management
- Role-based access control
- Endpoint security allowing 
- Auth endpoints can be used without login/JWT 


## Rate Limiting
- **Library**: Bucket4j
- **Implementation**: IP-based rate limiting to prevent abuse
- **Components**:
  - `RateLimitingFilter`: Intercepts and limits requests based on client IP
  - `RateLimitConfig`: Configures token bucket algorithm parameters
- **Features**:
  - Configurable request capacity
  - Time-based token refill
  - Per-IP rate limiting

## Data Security

### Password Handling
- **Library**: BCrypt (from Spring Security)
- **Implementation**: One-way hashing of passwords with BCrypt
- **Features**:
  - Salt-based password hashing

### Transaction Security
- Input validation for all transfer requests
- Protection against overdrafts
- Prevention of self-transfers
- TODO: consider stricter input sanitization

### Known Limitations
- Current implementation uses in-memory rate limiting
- JWT secret management needs improvement or replacement with OAuth2 for prod