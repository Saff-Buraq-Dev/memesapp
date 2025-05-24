import { Environment } from './environment.interface';

export const environment: Environment = {
  production: false,
  // For local development with backend running locally
  apiUrl: 'http://localhost:8080/api',
  uploadsUrl: 'http://localhost:8080/uploads'

  // For testing with deployed dev environment, use:
  // apiUrl: 'https://dev-api.dev.gharbidev.com/api',
  // uploadsUrl: 'https://dev-api.dev.gharbidev.com/uploads'
};
