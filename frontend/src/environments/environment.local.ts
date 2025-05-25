import { Environment } from './environment.interface';

export const environment: Environment = {
  production: false,
  // For local development with backend running locally
  apiUrl: 'http://localhost:8080/api',
  uploadsUrl: 'http://localhost:8080/uploads'
};
