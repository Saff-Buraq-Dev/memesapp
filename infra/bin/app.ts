#!/usr/bin/env node
import 'dotenv/config';
import * as cdk from 'aws-cdk-lib';
import { MemeVoteConfig } from '../lib/memevote-stack';
import { MemeVoteStack } from '../lib/memevote-stack';

// Load environment variables or use defaults
const app = new cdk.App();

// Determine environment from context or environment variable
const environment = app.node.tryGetContext('environment') || process.env.ENVIRONMENT || 'dev';
const isProd = environment === 'prod' || environment === 'production';

// Environment-specific AWS account configuration
const getAwsEnv = (env: string) => {
  if (env === 'prod' || env === 'production') {
    return {
      account: process.env.PROD_AWS_ACCOUNT_ID || process.env.CDK_DEFAULT_ACCOUNT,
      region: process.env.PROD_AWS_REGION || process.env.CDK_DEFAULT_REGION || 'ca-central-1',
    };
  } else {
    return {
      account: process.env.DEV_AWS_ACCOUNT_ID || process.env.CDK_DEFAULT_ACCOUNT,
      region: process.env.DEV_AWS_REGION || process.env.CDK_DEFAULT_REGION || 'ca-central-1',
    };
  }
};

const env = getAwsEnv(environment);

// App configuration with defaults that can be overridden
const config: MemeVoteConfig = {
  // General
  projectName: process.env.PROJECT_NAME || 'memevote',
  environment: environment,

  // Domain configuration
  domainName: process.env.DOMAIN_NAME || 'gharbidev.com',
  frontendSubdomain: isProd
    ? (process.env.PROD_FRONTEND_SUBDOMAIN || process.env.FRONTEND_SUBDOMAIN || 'app')
    : (process.env.DEV_FRONTEND_SUBDOMAIN || process.env.FRONTEND_SUBDOMAIN || 'dev-app'),
  backendSubdomain: isProd
    ? (process.env.PROD_BACKEND_SUBDOMAIN || process.env.BACKEND_SUBDOMAIN || 'api')
    : (process.env.DEV_BACKEND_SUBDOMAIN || process.env.BACKEND_SUBDOMAIN || 'dev-api'),

  // Database configuration
  dbName: process.env.DB_NAME || 'memevote',
  dbUsername: process.env.DB_USERNAME || 'admin',

  // ECS configuration - Increased for Spring Boot requirements
  ecsTaskCpu: parseInt(isProd
    ? (process.env.PROD_ECS_TASK_CPU || '1024')
    : (process.env.DEV_ECS_TASK_CPU || '1024')),
  ecsTaskMemory: parseInt(isProd
    ? (process.env.PROD_ECS_TASK_MEMORY || '2048')
    : (process.env.DEV_ECS_TASK_MEMORY || '2048')),

  // Frontend configuration
  frontendCachePolicy: {
    defaultTtl: cdk.Duration.days(parseInt(process.env.FRONTEND_DEFAULT_TTL_DAYS || '1')),
    maxTtl: cdk.Duration.days(parseInt(process.env.FRONTEND_MAX_TTL_DAYS || '365')),
    minTtl: cdk.Duration.minutes(parseInt(process.env.FRONTEND_MIN_TTL_MINUTES || '5')),
  },
};


// Create the stack
new MemeVoteStack(app, `${config.projectName}-${config.environment}`, {
  env,
  config: config,
  description: `MemeVote application infrastructure for ${config.environment} environment`,
});
