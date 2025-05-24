# MemeVote Configuration Guide

This document explains the three-tier configuration structure for the MemeVote application.

## Configuration Structure

### 1. Local Development (`application-dev.properties`)
**Profile**: `dev`  
**Database**: H2 In-Memory  
**Purpose**: Local development on your machine

```properties
# Database: H2 in-memory
spring.datasource.url=jdbc:h2:mem:memevotedb;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=

# Features enabled:
- H2 Console: http://localhost:8080/h2-console
- Swagger UI: http://localhost:8080/swagger-ui.html
- Full logging and debugging
- CORS allows localhost and deployed dev frontend
```

### 2. Staging/Development Deployment (`application-staging.properties`)
**Profile**: `staging`  
**Database**: Aurora Serverless v2 (Development)  
**Domain**: `dev.gharbidev.com`  
**Purpose**: Deployed development environment for testing

```properties
# Database: Aurora Serverless v2 (dev environment)
spring.datasource.url=jdbc:mysql://gharbidev-dev-aurora-cluster...
aws.region=ca-central-1
db.secret.name=gharbidev-dev-db-credentials

# Features:
- Swagger UI enabled for API testing
- Moderate logging for debugging
- CORS allows dev frontend domain
- Connection pool optimized for serverless
```

### 3. Production (`application-prod.properties`)
**Profile**: `prod`  
**Database**: Aurora Serverless v2 (Production)  
**Domain**: `gharbidev.com`  
**Purpose**: Live production environment

```properties
# Database: Aurora Serverless v2 (prod environment)
spring.datasource.url=jdbc:mysql://gharbidev-prod-aurora-cluster...
aws.region=ca-central-1
db.secret.name=gharbidev-prod-db-credentials

# Features:
- Swagger UI disabled for security
- Minimal logging (INFO/WARN/ERROR only)
- CORS restricted to production frontend
- DDL validation only (no schema updates)
- Larger connection pool for higher traffic
```

## Domain Structure

### Development Environment
```
Frontend: https://dev-app.dev.gharbidev.com
Backend:  https://dev-api.dev.gharbidev.com
```

### Production Environment
```
Frontend: https://app.gharbidev.com
Backend:  https://api.gharbidev.com
```

## How Profiles Are Selected

### Local Development
```bash
# Run with dev profile (default)
./mvnw spring-boot:run

# Or explicitly
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### CDK Deployment
The CDK automatically selects the correct profile:
- `dev` environment → `staging` profile
- `prod` environment → `prod` profile

```typescript
SPRING_PROFILES_ACTIVE: config.environment === 'dev' ? 'staging' : 'prod'
```

## Environment Variables Set by CDK

### Development Deployment (staging profile)
```bash
SPRING_PROFILES_ACTIVE=staging
SPRING_DATASOURCE_URL=jdbc:mysql://gharbidev-dev-aurora-cluster.cluster-xyz.ca-central-1.rds.amazonaws.com:3306/memevote?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
AWS_REGION=ca-central-1
DB_SECRET_NAME=gharbidev-dev-db-credentials
CORS_ALLOWED_ORIGINS=https://dev-app.dev.gharbidev.com
```

### Production Deployment (prod profile)
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://gharbidev-prod-aurora-cluster.cluster-xyz.ca-central-1.rds.amazonaws.com:3306/memevote?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
AWS_REGION=ca-central-1
DB_SECRET_NAME=gharbidev-prod-db-credentials
CORS_ALLOWED_ORIGINS=https://app.gharbidev.com
```

## Key Differences Between Environments

| Feature | Dev (Local) | Staging (Deployed Dev) | Production |
|---------|-------------|------------------------|------------|
| Database | H2 In-Memory | Aurora Serverless v2 | Aurora Serverless v2 |
| Domain | localhost:8080 | dev.gharbidev.com | gharbidev.com |
| Swagger UI | ✅ Enabled | ✅ Enabled | ❌ Disabled |
| H2 Console | ✅ Enabled | ❌ Disabled | ❌ Disabled |
| Logging Level | DEBUG/INFO | INFO | WARN/ERROR |
| DDL Mode | update | update | validate |
| Health Details | always | when-authorized | never |
| Connection Pool | Small | Medium (5 max) | Large (10 max) |

## Deployment Commands

### Deploy Development Environment
```bash
cd infra
npx cdk deploy --context environment=dev
# Uses staging profile with dev.gharbidev.com domain
```

### Deploy Production Environment
```bash
cd infra
npx cdk deploy --context environment=prod
# Uses prod profile with gharbidev.com domain
```

## Testing Different Configurations

### Test Staging Profile Locally
To test the staging configuration locally (useful for debugging Aurora connection):

1. Set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=staging
export SPRING_DATASOURCE_URL="jdbc:mysql://gharbidev-dev-aurora-cluster.cluster-xyz.ca-central-1.rds.amazonaws.com:3306/memevote?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export AWS_REGION=ca-central-1
export DB_SECRET_NAME=gharbidev-dev-db-credentials
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

### Frontend Environment Mapping
The frontend also has corresponding environment files:

- **Local Development**: `environment.dev.ts` → `http://localhost:8080`
- **Deployed Development**: `environment.prod.ts` → `https://dev-api.dev.gharbidev.com`
- **Production**: `environment.prod.ts` → `https://api.gharbidev.com`

## Security Considerations

1. **Secrets Management**: All database credentials are stored in AWS Secrets Manager
2. **CORS**: Each environment has restricted CORS origins
3. **API Documentation**: Swagger is disabled in production
4. **Health Endpoints**: Limited exposure in production
5. **Logging**: Sensitive information logging disabled in production

This structure provides clear separation between environments while maintaining consistency and security best practices.
