# AWS Secrets Manager Integration

This document explains how the MemeVote backend integrates with AWS Secrets Manager for secure database credential management.

## Overview

The application now uses AWS Secrets Manager to automatically retrieve database credentials instead of hardcoded passwords. This provides:

- **Security**: Credentials are encrypted and stored securely
- **Rotation**: Automatic credential rotation capabilities
- **Audit**: Full audit trail of credential access
- **Simplicity**: No manual credential management

## How It Works

### 1. Infrastructure Setup (CDK)

The CDK stack automatically:
- Creates an Aurora Serverless v2 cluster
- Generates database credentials in Secrets Manager
- Grants ECS task permissions to read the secret
- Passes the secret name as an environment variable

### 2. Application Configuration

The Spring Boot application uses AWS SDK v2 to:
- Retrieve credentials from Secrets Manager using the `DB_SECRET_NAME` environment variable
- Parse the JSON secret containing username and password
- Create a custom DataSource with the retrieved credentials

### 3. Configuration Files

**application-prod.properties:**
```properties
# Database Configuration - MySQL with AWS Secrets Manager
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# AWS Configuration
aws.region=${AWS_REGION:ca-central-1}

# AWS Secrets Manager Configuration
db.secret.name=${DB_SECRET_NAME:memevote-dev-db-credentials}
```

**Key Components:**
- `SecretsManagerService`: Retrieves and parses credentials from AWS Secrets Manager
- `DatabaseConfig`: Creates a custom DataSource using the retrieved credentials
- `AwsConfig`: Configures the AWS Secrets Manager client

## Environment Variables

The following environment variables are set by the CDK stack:

- `SPRING_DATASOURCE_URL`: Database connection URL
- `AWS_REGION`: AWS region for Secrets Manager
- `DB_SECRET_NAME`: Name of the secret containing database credentials
- `SPRING_PROFILES_ACTIVE`: Application profile (prod)

## Local Development

For local development, the application still uses H2 database with the `dev` profile. No AWS credentials are needed.

To test with AWS locally:
1. Configure AWS credentials (`aws configure` or environment variables)
2. Set environment variables:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export AWS_REGION=ca-central-1
   export DB_SECRET_NAME=memevote-dev-db-credentials
   export SPRING_DATASOURCE_URL=jdbc:mysql://your-aurora-endpoint:3306/memevote
   ```

## Dependencies Added

```xml
<!-- AWS SDK v2 for Secrets Manager -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>secretsmanager</artifactId>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sts</artifactId>
</dependency>

<!-- AWS SDK BOM for version management -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>bom</artifactId>
            <version>2.20.56</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Security Considerations

- ECS tasks use IAM roles for authentication (no hardcoded credentials)
- Secrets are encrypted at rest and in transit
- Access is logged in CloudTrail
- Principle of least privilege: tasks can only read their specific secret

## Troubleshooting

### Common Issues

1. **Secret not found**: Check that `DB_SECRET_NAME` environment variable is correct
2. **Access denied**: Verify ECS task role has `secretsmanager:GetSecretValue` permission
3. **Wrong region**: Ensure `AWS_REGION` matches where the secret is stored

### Logs

Check application logs for AWS-related errors:
```bash
# In ECS logs
grep -i "secret\|aws" /var/log/application.log
```

### Manual Secret Retrieval (for debugging)

```bash
aws secretsmanager get-secret-value \
  --secret-id memevote-dev-db-credentials \
  --region ca-central-1
```

## Migration Notes

### Before (Manual Credentials)
```properties
spring.datasource.username=${MYSQL_USERNAME:root}
spring.datasource.password=${MYSQL_PASSWORD:}
```

### After (Secrets Manager)
```properties
spring.config.import=optional:aws-secretsmanager:${DB_SECRET_NAME}
spring.datasource.username=${username}
spring.datasource.password=${password}
```

The secret JSON structure:
```json
{
  "username": "admin",
  "password": "generated-secure-password"
}
```
