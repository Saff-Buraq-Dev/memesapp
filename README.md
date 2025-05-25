# MemeVote 🎭

MemeVote is a modern, full-stack web application for sharing, discovering, and voting on memes. Built with Angular and Spring Boot, it provides a complete social platform for meme enthusiasts with real-time interactions and comprehensive meme management features.

## 📋 Table of Contents

- [✨ Features](#-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [📁 Project Structure](#-project-structure)
- [🚀 Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Quick Start (Recommended)](#quick-start-recommended)
  - [Detailed Setup Instructions](#detailed-setup-instructions)
- [📚 API Documentation](#-api-documentation)
- [🔧 Troubleshooting](#-troubleshooting)
- [🚀 Deployment](#-deployment)
- [🧪 Testing](#-testing)
- [🔄 Continuous Integration](#-continuous-integration)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [🙏 Acknowledgments](#-acknowledgments)

## ✨ Features

### 🔐 **Authentication & User Management**
- **Secure JWT Authentication**: Login/register with email and password
- **User Profiles**: Customizable profiles with avatar upload
- **Profile Picture Management**: Upload and update profile pictures with automatic navbar updates

### 🎨 **Meme Management**
- **Single & Batch Upload**: Upload individual memes or multiple files at once
- **Full CRUD Operations**: Create, read, update, and delete your memes
- **Title & Category Editing**: Modify meme titles and categories after upload
- **Category Management**: Create new categories on-the-fly during upload or editing
- **File Type Support**: JPEG, PNG, GIF, and WebP formats
- **Secure File Storage**: Images stored securely with proper authorization

### 🗳️ **Interactive Features**
- **Voting System**: Like your favorite memes with real-time vote counts
- **Real-time Updates**: WebSocket-powered live updates for votes and new memes
- **Browse & Discovery**: Paginated meme browsing with category filtering
- **Search Functionality**: Find memes by title or filter by categories
- **User Collections**: "My Memes" page to manage your uploads

### 🎯 **User Experience**
- **Responsive Design**: Mobile-first design using Bootstrap 5
- **Real-time Notifications**: Toast notifications for all user actions
- **Professional UI**: Clean, modern interface with intuitive navigation
- **Loading States**: Proper loading indicators and error handling

## 🛠️ Tech Stack

### **Frontend**
- **Framework**: Angular 15 with TypeScript
- **UI Framework**: Bootstrap 5 with responsive design
- **State Management**: RxJS for reactive programming
- **HTTP Client**: Angular HttpClient with interceptors
- **Real-time Communication**: SockJS & STOMP for WebSocket connections
- **Notifications**: ngx-toastr for user feedback
- **Authentication**: JWT handling with @auth0/angular-jwt
- **Forms**: Reactive Forms with validation
- **Routing**: Angular Router with guards
- **Build Tool**: Angular CLI with Webpack

### **Backend**
- **Framework**: Spring Boot 2.7 with Java 11
- **Database**: Aurora Serverless (AWS) / H2 (local development)
- **Security**: Spring Security with JWT authentication
- **Real-time Communication**: Spring WebSocket with STOMP
- **API**: RESTful API with Spring MVC
- **File Storage**: Local file system with secure access
- **Validation**: Bean Validation (JSR-303)
- **Build Tool**: Maven 3.8+
- **Documentation**: Swagger/OpenAPI 3.0

### **Infrastructure & Deployment**
- **Cloud Provider**: AWS (ECS, Aurora, S3, CloudFront, ALB)
- **Infrastructure as Code**: AWS CDK with TypeScript
- **Containerization**: Docker for backend deployment
- **CI/CD**: GitHub Actions for automated testing and deployment
- **Monitoring**: AWS CloudWatch for logging and metrics

## 📁 Project Structure

```
memesapp/
├── 📁 backend/                     # Spring Boot backend application
│   ├── 📁 src/main/java/           # Java source code
│   │   └── 📁 com/memevote/backend/
│   │       ├── 📁 controller/      # REST API controllers
│   │       ├── 📁 service/         # Business logic services
│   │       ├── 📁 repository/      # Data access layer
│   │       ├── 📁 model/           # JPA entities
│   │       ├── 📁 dto/             # Data transfer objects
│   │       ├── 📁 security/        # Security configuration
│   │       └── 📁 config/          # Application configuration
│   ├── 📁 src/main/resources/      # Configuration files
│   │   ├── application.properties  # Main configuration
│   │   ├── application-local.properties   # Local development
│   │   ├── application-dev.properties     # Development environment
│   │   └── application-prod.properties    # Production environment
│   ├── 📄 pom.xml                  # Maven dependencies
│   └── 📄 Dockerfile               # Docker configuration
├── 📁 frontend/                    # Angular frontend application
│   ├── 📁 src/app/                 # Angular source code
│   │   ├── 📁 core/                # Core services and guards
│   │   ├── 📁 shared/              # Shared components and modules
│   │   ├── 📁 features/            # Feature modules
│   │   │   ├── 📁 auth/            # Authentication module
│   │   │   ├── 📁 memes/           # Meme browsing and management
│   │   │   └── 📁 profile/         # User profile management
│   │   └── 📁 layouts/             # Layout components
│   ├── 📁 src/environments/        # Environment configurations
│   ├── 📄 package.json             # NPM dependencies
│   └── 📄 angular.json             # Angular CLI configuration
├── 📁 infra/                       # AWS CDK infrastructure code
│   ├── 📁 lib/                     # CDK stack definitions
│   ├── 📄 cdk.json                 # CDK configuration
│   ├── 📄 .env.example             # Environment variables template
│   └── 📄 package.json             # CDK dependencies
└── 📄 README.md                    # This file
```

## 🚀 Getting Started

### **Prerequisites**

Before running the application locally, ensure you have the following installed:

- **Java 11 or higher** - [Download here](https://adoptium.net/)
- **Node.js 16 or higher** - [Download here](https://nodejs.org/)
- **npm 8 or higher** (comes with Node.js)
- **Maven 3.8 or higher** - [Download here](https://maven.apache.org/download.cgi)
- **Git** - [Download here](https://git-scm.com/)

### **Quick Start (Recommended)**

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/memesapp.git
   cd memesapp
   ```

2. **Start the backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

   ✅ Backend will be available at: http://localhost:8080

   📚 API Documentation: http://localhost:8080/swagger-ui.html

3. **Start the frontend (in a new terminal):**
   ```bash
   cd frontend
   npm install
   npm start
   ```

   ✅ Frontend will be available at: http://localhost:4200

4. **Access the application:**
   - Open your browser and navigate to http://localhost:4200
   - Create a new account or use the demo features
   - Start uploading and voting on memes!

### **Detailed Setup Instructions**

#### **Backend Setup (Spring Boot)**

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Configure application properties (optional for local development):**

   The application comes with sensible defaults for local development using H2 database. For custom configuration, create:

   ```bash
   # Create local configuration file
   touch src/main/resources/application-local.properties
   ```

   Example `application-local.properties`:
   ```properties
   # Database Configuration (H2 for local development)
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.username=sa
   spring.datasource.password=
   spring.h2.console.enabled=true

   # JWT Configuration
   jwt.secret=your-local-jwt-secret-key
   jwt.expiration=86400000

   # File Upload Configuration
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB

   # CORS Configuration
   cors.allowed-origins=http://localhost:4200
   ```

3. **Build and run the application:**
   ```bash
   # Clean build
   ./mvnw clean compile

   # Run with local profile
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

   # Alternative: Run with Maven wrapper
   ./mvnw spring-boot:run
   ```

4. **Verify backend is running:**
   - API Base URL: http://localhost:8080
   - Health Check: http://localhost:8080/actuator/health
   - API Documentation: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console

#### **Frontend Setup (Angular)**

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   # Install all npm dependencies
   npm install

   # Alternative: Use npm ci for faster, reliable builds
   npm ci
   ```

3. **Configure environment (optional):**

   The application comes with default configurations. For custom settings, modify:

   ```typescript
   // frontend/src/environments/environment.ts
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:8080/api',
     uploadsUrl: 'http://localhost:8080/uploads',
     wsUrl: 'http://localhost:8080/ws'
   };
   ```

4. **Start the development server:**
   ```bash
   # Start with live reload
   npm start

   # Alternative: Start with specific configuration
   npm run start:local

   # Build for development
   npm run build:dev
   ```

5. **Verify frontend is running:**
   - Application URL: http://localhost:4200
   - Auto-reload enabled for development
   - Browser will automatically open

## 📚 API Documentation

The MemeVote API is fully documented using **Swagger/OpenAPI 3.0**. When the backend is running locally, you can access:

### **Interactive Documentation**
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html#/
  - 🔍 Explore all API endpoints interactively
  - 🧪 Test endpoints directly from the browser
  - 📋 View request/response schemas and examples
  - 🔐 Authenticate and test protected endpoints

### **API Specification**
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
  - Raw OpenAPI 3.0 specification
  - Import into Postman, Insomnia, or other API tools
  - Generate client SDKs for different languages


## 🔧 Troubleshooting

### **Common Issues**

#### **Backend Issues**

**Port 8080 already in use:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process (replace PID with actual process ID)
kill -9 <PID>

# Or run on different port
./mvnw spring-boot:run -Dserver.port=8081
```

**Database connection issues:**
```bash
# Check H2 console at http://localhost:8080/h2-console
# Default settings:
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (leave empty)
```

**Maven build failures:**
```bash
# Clean and rebuild
./mvnw clean install

# Skip tests if needed
./mvnw clean install -DskipTests

# Update dependencies
./mvnw dependency:resolve
```

#### **Frontend Issues**

**Node modules issues:**
```bash
# Clear npm cache and reinstall
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

**Port 4200 already in use:**
```bash
# Run on different port
ng serve --port 4201

# Or kill existing process
lsof -i :4200
kill -9 <PID>
```

**CORS errors:**
- Ensure backend is running on http://localhost:8080
- Check that CORS is properly configured in backend
- Verify API URLs in environment files

### **Development Tips**

- **Hot Reload**: Both frontend and backend support hot reload during development
- **Database Reset**: Restart backend to reset H2 in-memory database
- **API Testing**: Use Swagger UI for testing API endpoints
- **Debugging**: Enable debug logging in application properties
- **File Uploads**: Check file size limits (default 10MB)

## 🚀 Deployment

### **AWS Deployment (Production)**

The application is designed to be deployed on AWS using Infrastructure as Code (CDK).

#### **Prerequisites for Deployment**
- AWS CLI configured with appropriate credentials
- AWS CDK installed: `npm install -g aws-cdk`
- Docker installed for backend containerization

#### **Infrastructure Setup**
```bash
# Navigate to infrastructure directory
cd infra

# Copy environment template
cp .env.example .env

# Edit .env with your AWS account details and domain names
# See infra/.env.example for all required variables

# Install CDK dependencies
npm install

# Bootstrap CDK (first time only)
npx cdk bootstrap

# Deploy to development environment
npx cdk deploy --context environment=dev

# Deploy to production environment
npx cdk deploy --context environment=prod
```

#### **Infrastructure Components**
- **Frontend**: Deployed to S3 with CloudFront CDN
- **Backend**: Containerized and deployed to ECS Fargate
- **Database**: Aurora Serverless for scalable, cost-effective database
- **Load Balancer**: Application Load Balancer with SSL termination
- **Domain**: Custom domain with SSL certificates
- **Monitoring**: CloudWatch for logs and metrics

### **Local Development Deployment**

For testing the full deployment pipeline locally:

```bash
# Build both applications
cd backend && ./mvnw clean package -DskipTests
cd ../frontend && npm run build:dev

# The CDK deployment will automatically pick up these builds
cd ../infra && npx cdk deploy --context environment=dev
```

## 🧪 Testing

### **Backend Testing**
```bash
cd backend

# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=MemeServiceTest

# Run integration tests
./mvnw test -Dtest=*IntegrationTest
```

### **Frontend Testing**
```bash
cd frontend

# Run unit tests
npm test

# Run tests with coverage
npm run test:coverage

# Run e2e tests
npm run e2e

# Run tests in watch mode
npm run test:watch
```

## 🔄 Continuous Integration

This project uses **GitHub Actions** for automated testing and deployment:

### **Automated Workflows**
- **Backend CI**: Runs Java tests and generates coverage reports
- **Frontend CI**: Runs Angular tests and builds the application
- **Combined CI**: Full integration testing on main branch
- **Deployment**: Automated deployment to AWS on successful builds

### **Quality Assurance**
- **Code Coverage**: JaCoCo for backend, Istanbul for frontend
- **Code Quality**: ESLint, Prettier for frontend; Checkstyle for backend
- **Security**: Dependency vulnerability scanning
- **Performance**: Build time optimization and bundle analysis

## 🤝 Contributing

We welcome contributions to MemeVote! [SeeHere's how you can help](./CONTRIBUTING.md).



## 🙏 Acknowledgments

- **Angular Team** for the amazing frontend framework
- **Spring Team** for the robust backend framework
- **AWS** for reliable cloud infrastructure
- **Bootstrap** for responsive UI components
- **All Contributors** who have helped improve this project

---

**Made with ❤️ by the MemeVote Team**

For questions, issues, or feature requests, please [open an issue](https://github.com/Saff-Buraq-Dev/memesapp/issues) on GitHub.
