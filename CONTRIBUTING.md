# Contributing to MemeVote

Thank you for considering contributing to MemeVote! This document outlines the process for contributing to the project and provides guidelines to help you get started.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct:

- Be respectful and inclusive
- Use welcoming and inclusive language
- Be supportive of others
- Accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the issue tracker to avoid duplicates. When you create a bug report, include as many details as possible:

1. **Use a clear and descriptive title**
2. **Describe the exact steps to reproduce the problem**
3. **Provide specific examples** (e.g., screenshots, error messages)
4. **Describe the behavior you observed and what you expected to see**
5. **Include details about your configuration and environment**

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

1. **Use a clear and descriptive title**
2. **Provide a detailed description of the suggested enhancement**
3. **Explain why this enhancement would be useful**
4. **Include any relevant examples or mockups**

### Pull Requests

1. **Fork the repository**
2. **Create a new branch** for your feature or bugfix
3. **Make your changes**
4. **Write or update tests** as needed
5. **Ensure the test suite passes**
6. **Update documentation** if necessary
7. **Submit a pull request**

## Development Workflow

### Setting Up the Development Environment

Follow the instructions in the [README.md](README.md) to set up your development environment.

### Coding Standards

#### Backend (Java)

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write Javadoc comments for public methods
- Keep methods small and focused on a single responsibility
- Use appropriate design patterns

#### Frontend (Angular/TypeScript)

- Follow the [Angular Style Guide](https://angular.io/guide/styleguide)
- Use TypeScript features appropriately
- Maintain component isolation and reusability
- Use reactive programming patterns with RxJS
- Keep templates clean and readable

### Testing

- Write unit tests for all new features and bug fixes
- Ensure all tests pass before submitting a pull request
- Aim for high test coverage

#### Backend Testing

- Use JUnit 5 for unit tests
- Use MockMvc for controller tests
- Use Mockito for mocking dependencies

#### Frontend Testing

- Use Jasmine and Karma for unit tests
- Use TestBed for component testing
- Test services and components separately

### Git Workflow

1. **Create a branch** from `main` for your work
   ```
   git checkout -b feature/your-feature-name
   ```
   or
   ```
   git checkout -b fix/issue-you-are-fixing
   ```

2. **Make your changes** and commit them with clear, descriptive messages
   ```
   git commit -m "Add feature: description of the feature"
   ```

3. **Push your branch** to your fork
   ```
   git push origin feature/your-feature-name
   ```

4. **Create a pull request** against the `main` branch of the original repository

## Continuous Integration

We use GitHub Actions for continuous integration:

1. **On every push** to any branch, the CI pipeline will:
   - Build the project
   - Run tests
   - Generate test coverage reports

2. **On pull requests**, the CI pipeline will additionally:
   - Comment on the PR with test coverage information
   - Highlight any coverage changes

### Coverage Requirements

- Backend code should maintain at least 80% test coverage
- All new code should be covered by tests
- Critical components should aim for higher coverage

## Pull Request Process

1. Update the README.md or documentation with details of changes if needed
2. Update the CHANGELOG.md with details of changes if applicable
3. The PR should work in all supported environments
4. Ensure all automated CI checks pass
5. Address any coverage issues highlighted by the CI
6. Get at least one code review from a maintainer
7. Once approved, a maintainer will merge your PR

## Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)

Thank you for contributing to MemeVote!
