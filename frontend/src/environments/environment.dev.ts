import { Environment } from './environment.interface';

export const environment: Environment = {
  production: false,
  // For AWS development environment
  apiUrl: 'https://dev-api.dev.gharbidev.com/api',
  uploadsUrl: 'https://dev-api.dev.gharbidev.com/uploads'
};
