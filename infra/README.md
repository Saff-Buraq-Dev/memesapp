# MemeVote Infrastructure

This directory contains the AWS CDK code to deploy the MemeVote application infrastructure with support for multiple environments (development and production).

## Architecture

The infrastructure consists of:

- **Frontend**: S3 bucket + CloudFront distribution for static hosting
- **Backend**: ECS Fargate service with Application Load Balancer (scales 0-1 instances)
- **Database**: Aurora Serverless v2 MySQL cluster (auto-scaling)
- **Networking**: Default VPC (simplified setup)
- **DNS**: Route53 configuration for custom domain
- **Security**: AWS Secrets Manager for database credentials

## Multi-Environment Support

The infrastructure supports two environments:

- **Development**: Deployed on feature branches and PRs
  - Uses minimal resources for cost optimization
  - Deploys to development AWS account
  - Subdomains: `dev-app.yourdomain.com`, `dev-api.yourdomain.com`

- **Production**: Deployed on main branch
  - Uses production-grade resources
  - Deploys to production AWS account
  - Subdomains: `app.yourdomain.com`, `api.yourdomain.com`

## Cost Optimization Features

- **Scale-to-Zero**: ECS service scales from 0 to 1 instances based on traffic
- **Aurora Serverless v2**: Automatically scales database capacity (0.5-4 ACUs)
- **Default VPC**: No NAT Gateway costs (uses public subnets)
- **Fargate**: No EC2 instance management overhead
- **Request-based scaling**: Scales up quickly on traffic, scales down after 5 minutes
- **Secrets Manager**: Automatic credential rotation and management
- **CloudFront caching**: Reduces backend requests and costs
- **CloudFront price class**: Limited to North America and Europe

## Prerequisites

1. AWS CLI installed and configured
2. Node.js and npm installed
3. A registered domain name with a Route53 hosted zone in your AWS account

## Setup

1. Copy `.env.example` to `.env` and fill in your configuration:

```bash
cp .env.example .env
```

2. Install dependencies:

```bash
npm install
```

3. Build the CDK app:

```bash
npm run build
```

## Deployment

### Automated Deployment (Recommended)

The infrastructure is automatically deployed via GitHub Actions:

- **Development**: Deploys automatically on feature branch pushes and PRs
- **Production**: Deploys automatically on main branch pushes (with optional approval)

See `DEPLOYMENT_SETUP.md` in the root directory for complete setup instructions.

### Manual Deployment

1. Bootstrap your AWS environment (if not already done):

```bash
# For development
npm run cdk bootstrap -- --context environment=dev

# For production
npm run cdk bootstrap -- --context environment=prod
```

2. Deploy the stack:

```bash
# Deploy to development
npm run cdk deploy -- --context environment=dev

# Deploy to production
npm run cdk deploy -- --context environment=prod
```

## Configuration

All configuration is done through environment variables. See `.env.example` for available options.

### Required Environment Variables

None! Database credentials are automatically managed by AWS Secrets Manager.

### Optional Environment Variables

- `AWS_ACCOUNT_ID`: Your AWS account ID
- `AWS_REGION`: AWS region to deploy to (default: ca-central-1)
- `PROJECT_NAME`: Name of the project (default: memevote)
- `ENVIRONMENT`: Environment name (default: dev)
- `DOMAIN_NAME`: Your domain name
- `FRONTEND_SUBDOMAIN`: Subdomain for the frontend (default: app)
- `BACKEND_SUBDOMAIN`: Subdomain for the backend (default: api)
- `DB_NAME`: Database name (default: memevote)
- `DB_USERNAME`: Database username (default: admin)
- `ECS_TASK_CPU`: CPU units for ECS tasks (default: 256 for dev, 512 for prod)
- `ECS_TASK_MEMORY`: Memory for ECS tasks in MiB (default: 512 for dev, 1024 for prod)
- `FRONTEND_DEFAULT_TTL_DAYS`: Default TTL for CloudFront cache (default: 1)
- `FRONTEND_MAX_TTL_DAYS`: Maximum TTL for CloudFront cache (default: 365)
- `FRONTEND_MIN_TTL_MINUTES`: Minimum TTL for CloudFront cache (default: 5)

## Cleanup

To destroy the stack:

```bash
npm run destroy
```

## Notes

- The frontend deployment assumes that the Angular app has been built to `../frontend/dist/frontend`
- The backend deployment builds a Docker image from the `../backend` directory
- For production use, consider changing the removal policies for S3 and RDS to `RETAIN`
- For production use, enable Multi-AZ for the RDS instance
