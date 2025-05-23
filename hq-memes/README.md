# MemeVote

MemeVote is a full-stack web application for sharing and voting on memes. Users can upload memes, categorize them, vote on their favorites, and comment on submissions.

## Features

- **User Authentication**: Secure JWT-based authentication system
- **Meme Management**: Upload, browse, and search memes
- **Voting System**: Vote on your favorite memes
- **Commenting**: Engage with other users through comments
- **Categories**: Organize memes by categories
- **Real-time Updates**: WebSocket integration for live updates
- **Responsive Design**: Mobile-friendly interface using Bootstrap
- **User Profiles**: Personalized user profiles with avatar support

## Tech Stack

### Frontend
- **Framework**: Angular 15
- **UI Library**: Bootstrap 5
- **State Management**: RxJS
- **HTTP Client**: Angular HttpClient
- **WebSockets**: SockJS & STOMP
- **Notifications**: ngx-toastr
- **Authentication**: JWT with @auth0/angular-jwt

### Backend
- **Framework**: Spring Boot 2.7
- **Database**: H2 Database (dev) / MySQL (prod)
- **Security**: Spring Security with JWT
- **WebSockets**: Spring WebSocket
- **API**: RESTful API with Spring MVC
- **Build Tool**: Maven
- **Documentation**: Swagger/OpenAPI 3.0

## Project Structure

```
/
├── backend/                # Spring Boot backend
│   ├── src/                # Source code
│   ├── pom.xml             # Maven configuration
│   └── ...
├── frontend/               # Angular frontend
│   ├── src/                # Source code
│   ├── package.json        # NPM dependencies
│   └── ...
└── ...
```

## Getting Started

### Prerequisites

- Java 17+
- Node.js 14+
- npm 6+
- Maven 3.6+

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The backend will be available at http://localhost:8080

   API documentation will be available at http://localhost:8080/swagger-ui.html

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

   The frontend will be available at http://localhost:4200

## Environment Configuration

### Backend

Create `application-dev.properties` and `application-prod.properties` in `backend/src/main/resources/` for development and production environments respectively.

### Frontend

Environment configurations are located in:
- `frontend/src/environments/environment.ts` (default)
- `frontend/src/environments/environment.dev.ts` (development)
- `frontend/src/environments/environment.prod.ts` (production)

## API Documentation

The MemeVote API is documented using Swagger/OpenAPI 3.0. When the backend is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
  - Interactive documentation that allows you to explore and test the API endpoints
  - Includes request/response schemas, authentication requirements, and example values

- **OpenAPI Specification**: http://localhost:8080/v3/api-docs
  - Raw OpenAPI specification in JSON format
  - Can be imported into API tools like Postman

The API documentation includes:
- Detailed endpoint descriptions
- Request parameters and body schemas
- Response formats and status codes
- Authentication requirements
- Example requests and responses

## Continuous Integration

This project uses GitHub Actions for continuous integration with a structured workflow approach:

### Development Workflows
- **Backend CI** (.github/workflows/backend-ci.yml):
  - Triggered when backend code changes are pushed to any branch
  - Runs Java tests and generates JaCoCo coverage reports
  - Adds coverage report comments to PRs

- **Frontend CI** (.github/workflows/frontend-ci.yml):
  - Triggered when frontend code changes are pushed to any branch
  - Builds the Angular app and runs tests with Karma
  - Adds coverage report comments to PRs

### Main Branch Workflow
- **Combined CI** (.github/workflows/ci.yml):
  - Triggered when code is pushed to the main branch
  - Runs both backend and frontend tests in a single workflow
  - Can also be manually triggered via workflow_dispatch

This structure ensures quick feedback during development while maintaining comprehensive testing on the main branch.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
