# MemeVote Deployment Configuration

This document outlines the actual configuration used in the deployed environments.

## Infrastructure Overview

### Architecture
- **Frontend**: S3 + CloudFront (Static hosting)
- **Backend**: ECS Fargate (Scale 0-1 instances)
- **Database**: Aurora Serverless v2 (Auto-scaling)
- **Networking**: Default VPC (Cost optimized)
- **Security**: AWS Secrets Manager for credentials

### Domain Structure
```
Production:
- Frontend: https://app.gharbidev.com
- Backend API: https://api.gharbidev.com
- Database: Aurora Serverless v2 cluster

Development:
- Frontend: https://dev-app.gharbidev.com
- Backend API: https://dev-api.gharbidev.com
- Database: Aurora Serverless v2 cluster
```

## Environment Variables

### ECS Container Environment (Set by CDK)

**Production Environment:**
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://gharbidev-prod-aurora-cluster.cluster-xyz.ca-central-1.rds.amazonaws.com:3306/memevote?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
AWS_REGION=ca-central-1
DB_SECRET_NAME=gharbidev-prod-db-credentials
PORT=8080
CORS_ALLOWED_ORIGINS=https://app.gharbidev.com
```

**Development Environment:**
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://gharbidev-dev-aurora-cluster.cluster-xyz.ca-central-1.rds.amazonaws.com:3306/memevote?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
AWS_REGION=ca-central-1
DB_SECRET_NAME=gharbidev-dev-db-credentials
PORT=8080
CORS_ALLOWED_ORIGINS=https://dev-app.gharbidev.com
```

### AWS Secrets Manager

**Secret Structure:**
```json
{
  "username": "admin",
  "password": "auto-generated-secure-password"
}
```

**Secret Names:**
- Production: `gharbidev-prod-db-credentials`
- Development: `gharbidev-dev-db-credentials`

## Application Configuration

### Backend (Spring Boot)

**application-prod.properties:**
- Uses Aurora Serverless v2 with SSL
- Credentials from AWS Secrets Manager
- Optimized connection pool for serverless
- Health checks enabled
- CORS configured for frontend domain

**application-dev.properties:**
- Local development uses H2 in-memory database
- Comments show Aurora configuration for testing
- CORS allows both localhost and deployed dev frontend

### Frontend (Angular)

**environment.prod.ts:**
- API URL points to production backend
- Uploads URL for file handling

**environment.dev.ts:**
- Default: localhost backend for local development
- Comments show deployed dev environment URLs

## Auto-Scaling Configuration

### Aurora Serverless v2
```
Development:
- Min Capacity: 0.5 ACU (1GB RAM)
- Max Capacity: 1 ACU (2GB RAM)
- Auto-pause: After 5 minutes of inactivity

Production:
- Min Capacity: 0.5 ACU (1GB RAM)
- Max Capacity: 4 ACU (8GB RAM)
- Auto-pause: After 5 minutes of inactivity
```

### ECS Fargate
```
Both Environments:
- Desired Count: 0 (Start with no instances)
- Min Capacity: 0 instances
- Max Capacity: 1 instance
- Scale-up Trigger: 10 requests per target
- Scale-up Cooldown: 30 seconds
- Scale-down Cooldown: 5 minutes
```

## Cost Optimization

### Expected Costs (per day)

**99% of time (no traffic):**
- Aurora Serverless: ~$0.30 (minimum capacity)
- ECS: $0.00 (no instances running)
- S3 + CloudFront: ~$0.10
- Route53: ~$0.02
- **Total: ~$0.42/day**

**During traffic:**
- Aurora scales automatically based on load
- ECS spins up 1 instance (~$0.50/day if running 24/7)
- Scales back down after 5 minutes of low traffic

### Annual Cost Estimate
- Low traffic (current): ~$150-200/year
- Medium traffic: ~$300-500/year
- High traffic: ~$1000-2000/year (would need to increase max capacity)

## Security Features

1. **No Hardcoded Credentials**: All database credentials in Secrets Manager
2. **IAM Roles**: ECS tasks use instance profiles, no access keys
3. **Encrypted Storage**: Secrets encrypted at rest and in transit
4. **SSL/TLS**: All communication encrypted
5. **CORS**: Restricted to specific frontend domains
6. **VPC Security**: Database in private subnets (when using custom VPC)

## Monitoring & Health Checks

### Application Health
- Spring Boot Actuator endpoints: `/actuator/health`
- Database connectivity monitoring
- ECS health checks every 30 seconds

### Infrastructure Monitoring
- CloudWatch metrics for Aurora and ECS
- ALB health checks
- CloudFront access logs

## Deployment Commands

### Infrastructure Deployment
```bash
cd infra
npm install
npm run cdk deploy -- --context environment=dev
npm run cdk deploy -- --context environment=prod
```

### Application Deployment
```bash
# Backend is automatically built and deployed via ECS
# Frontend is automatically deployed to S3 via CDK

# Manual frontend build (if needed)
cd frontend
npm run build:prod
```

## Local Development

### Backend
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend
```bash
cd frontend
npm start
# Runs on http://localhost:4200
```

### Testing with AWS
To test the backend with actual Aurora Serverless:
1. Set environment variables in your IDE/terminal
2. Ensure AWS credentials are configured
3. Run with `prod` profile but point to dev database
