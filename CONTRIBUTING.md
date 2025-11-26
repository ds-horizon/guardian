# Contributing to Guardian

First off, thank you for considering contributing to Guardian! It's people like you that make Guardian such a great tool.

## Table of Contents

* [Code of Conduct](#code-of-conduct)

* [How Can I Contribute?](#how-can-i-contribute)

* [Development Setup](#development-setup)

* [Development Process](#development-process)

* [Coding Standards](#coding-standards)

* [Testing Guidelines](#testing-guidelines)

* [Pull Request Process](#pull-request-process)

* [Documentation](#documentation)

* [License](#license)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. We pledge to make participation in our project a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, gender identity and expression, level of experience, nationality, personal appearance, race, religion, or sexual identity and orientation.

### Our Standards

**Examples of behavior that contributes to creating a positive environment include:**

*   Using welcoming and inclusive language

*   Being respectful of differing viewpoints and experiences

*   Gracefully accepting constructive criticism

*   Focusing on what is best for the community

*   Showing empathy towards other community members


**Examples of unacceptable behavior include:**

*   The use of sexualized language or imagery and unwelcome sexual attention or advances

*   Trolling, insulting/derogatory comments, and personal or political attacks

*   Public or private harassment

*   Publishing others' private information without explicit permission

*   Other conduct which could reasonably be considered inappropriate in a professional setting


### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting the project maintainers. All complaints will be reviewed and investigated and will result in a response that is deemed necessary and appropriate to the circumstances.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the [issue list](https://github.com/ds-horizon/guardian/issues "https://github.com/ds-horizon/guardian/issues") as you might find out that you don't need to create one.

When creating a bug report, please include as many details as possible:

#### Before Submitting a Bug Report

*   **Check the documentation** - The issue might be covered in the [docs](./docs)

*   **Check existing issues** - The bug might already be reported

*   **Check recent changes** - Verify if the unexpected behavior is a result of a recent, intended feature update.


#### How to Report a Bug

1.  **Use a clear and descriptive title**

2.  **Describe the exact steps to reproduce the problem**

3.  **Provide specific examples** to demonstrate the steps

4.  **Describe the behavior you observed** after following the steps

5.  **Explain which behavior you expected** to see instead and why

6.  **Include relevant logs** and error messages

7.  **Include environment details**:

    *   Java version

    *   Maven version

    *   Docker version (if applicable)

    *   Operating system

8.  **Include screenshots** if applicable

---

### 🐞 Bug Report Template

#### Describe the bug
A clear and concise description of what the bug is.

#### To Reproduce
Steps to reproduce the behavior:

1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

#### Expected behavior
A clear and concise description of what you expected to happen.

#### Screenshots
If applicable, add screenshots to help explain your problem.

#### Environment

- **OS:** e.g. macOS 14.0
- **Java Version:** e.g. 17.0.1
- **Maven Version:** e.g. 3.9.0
- **Guardian Version:** e.g. 0.0.2-SNAPSHOT

#### Additional context
Add any other context about the problem here.

---

### Suggesting Enhancements

Enhancement suggestions are tracked as [GitHub issues](https://github.com/ds-horizon/guardian/issues). When creating an enhancement suggestion, make sure to:

1.  **Use a clear and descriptive title**

2.  **Provide a step-by-step description** of the suggested enhancement

3.  **Provide specific examples** to demonstrate the steps

4.  **Describe the current behavior** and explain which behavior you expected to see instead

5.  **Explain why this enhancement would be useful**

6.  **List any alternatives** you've considered


### 🌟 Enhancement Template

#### Is your feature request related to a problem? Please describe.
A clear and concise description of what the problem is.  
Example: *I'm frustrated when [...] because [...].*

#### Describe the solution you'd like
A clear and concise description of what you want to happen.  
Example: *It would be great if Guardian could [...].*

#### Describe alternatives you've considered
A clear and concise description of any alternative solutions or features you've considered.  
Example: *I've tried [...], but it doesn’t fully solve the issue because [...].*

#### Additional context
Add any other context or screenshots about the feature request here.  
You may include code snippets, mockups, or configuration details if relevant.

---

### Pull Requests

Pull requests are welcome! Please follow these guidelines:

1.  **Fork the repository** and create your branch from `main`

2.  **Follow coding standards** ([see below](#coding-standards))

3.  **Add tests** for new functionality

4.  **Update documentation** for API changes

5.  **Ensure the test suite passes**

6.  **Make sure your code lints**

7.  **Update CHANGELOG.md** (if applicable)

8.  **Issue that pull request!**


## Development Setup

### Prerequisites

*   **Java 17** (JDK) - [Download](https://www.oracle.com/java/technologies/downloads/#java17)

*   **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)

*   **Docker** ≥ 20.10 - [Download](https://www.docker.com/products/docker-desktop/)

*   **Docker Compose** ≥ 2.0

*   **IDE** (IntelliJ IDEA(recommended), Eclipse, or VS Code)


### Setting Up Development Environment

#### 1. Fork and Clone

 Fork the repository on GitHub, then run:

```bash
    git clone https://github.com/YOUR_USERNAME/guardian.git
    cd guardian
    git remote add upstream https://github.com/ds-horizon/guardian.git
```

#### 2.  **Build the Project**


`mvn clean install`

#### 3.  **Run Tests**


 Run all tests

``` bash 
   mvn clean verify -Dlogback.configurationFile=logback/logback-development.xml 
```

Run specific test class

```bash 
   mvn clean -Dit.test=PasswordlessInitIT verify -Dlogback.configurationFile=logback/logback-development.xml
```

4.  **Start Services**


 Start all the components of Guardian 
```bash 
  ./quick-start.sh
 ```
Stop all the components of Guardian 

```bash
  docker compose down
```

### IDE Setup

#### IntelliJ IDEA

1.  Open the project

2.  Import Maven project (auto-detected)

3.  Set JDK 17 as project SDK

4.  Configure code style: Settings → Editor → Code Style → Java → Import → Google Style


#### Eclipse

1.  Import as Maven project

2.  Set Java 17 as JRE

3.  Install Google Java Format plugin


## Development Process

### Branch Naming

*   `feature/description` - New features

*   `bugfix/description` - Bug fixes

*   `docs/description` - Documentation updates

*   `refactor/description` - Code refactoring

*   `test/description` - Test additions/updates


### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) format:

**Important**: Commits should be GPG-signed for authenticity.

```text
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:

*   `feat`: New feature

*   `fix`: Bug fix

*   `docs`: Documentation changes

*   `style`: Code style changes (formatting, etc.)

*   `refactor`: Code refactoring

*   `test`: Adding or updating tests

*   `chore`: Maintenance tasks


**Examples**:

```text 
feat(auth): add passwordless authentication flow

Add support for SMS and Email OTP-based passwordless authentication.
Includes rate limiting and retry mechanisms.

Closes #123
```
```text
fix(token): resolve token expiration issue

Fix issue where tokens were not properly validated for expiration.
Added additional validation checks.

Fixes #456
```
### Workflow

1.  **Create a branch** from `main`

    `git checkout -b feature/my-feature`

2.  **Make your changes**

    *   Write code

    *   Add tests

    *   Update documentation

3.  **Commit your changes**

    `git add . git commit -S -m "feat: add new feature"`

    **Note**: The `-S` flag signs the commit with GPG. Ensure your GPG key is configured.

4.  **Keep your branch updated**

    `git fetch upstream git rebase upstream/main`

5.  **Push to your fork**

    `git push origin feature/my-feature`

6.  **Create Pull Request**

    *   Go to GitHub

    *   Click "New Pull Request"

    *   Select your branch

    *   Fill out the PR template


## Coding Standards

### Java Style Guide

*   Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

*   Use meaningful variable and method names

*   Keep methods focused and concise (max 50 lines recommended)

*   Use JavaDoc for public APIs

*   Avoid deep nesting (max 3 levels)


### Code Formatting

We use Google Java Format. Configure your IDE:

**IntelliJ IDEA**:

1.  Install "Google Java Format" plugin

2.  Enable "Reformat code on save"


**VS Code**:

1.  Install "Google Java Format" extension

2.  Enable format on save


**Manual Formatting**:

` mvn com.spotify.fmt:fmt-maven-plugin:2.20:format`

### Code Quality

*   **No warnings**: Fix all compiler warnings

*   **No TODO comments**: Remove or implement TODOs

*   **Meaningful names**: Use descriptive variable and method names

*   **DRY principle**: Don't Repeat Yourself

*   **Single Responsibility**: Each class/method should do one thing

*   **Error handling**: Handle errors appropriately

*   **Logging**: Use appropriate log levels


### Example

```text
 /**
 * Authenticates a user using username and password.
 *
 * @param username the username
 * @param password the password
 * @param tenantId the tenant identifier
 * @return Single emitting the authenticated user
 * @throws AuthenticationException if credentials are invalid
 */
public Single<User> authenticate(String username, String password, String tenantId) {
  // Implementation
}
```

## Testing Guidelines

### Test Structure

*   **Unit Tests**: Test individual components in isolation

*   **Integration Tests**: Test component interactions

*   **Test Naming**: `MethodName_StateUnderTest_ExpectedBehavior`


### Writing Tests

```text
@Test
void authenticate_WithValidCredentials_ReturnsUser() {
  // Given
  String username = "john";
  String password = "password123";
  
  // When
  User user = authService.authenticate(username, password);
  
  // Then
  assertNotNull(user);
  assertEquals("john", user.getUsername());
}
```
### Test Coverage

*   Aim for **80%+ code coverage**

*   Test happy paths

*   Test error cases

*   Test edge cases

*   Test boundary conditions


### Running Tests

```text
# Run all tests
mvn clean verify -Dlogback.configurationFile=logback/logback-development.xml

# Run specific test class
mvn clean -Dit.test=PasswordlessInitIT verify -Dlogback.configurationFile=logback/logback-development.xml
```
## Pull Request Process

### Before Submitting

*   Code follows style guidelines

*   All tests pass

*   New tests added for new functionality

*   Documentation updated

*   CHANGELOG.md updated (if applicable)

*   No merge conflicts with main

*   Branch is up to date with main


### PR Template

```text
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests added and passing
- [ ] Dependent changes merged

## Related Issues
Closes #123
```
### Review Process

1.  **Automated Checks**: CI/CD runs tests, linting, and security scans

2.  **Code Review**: At least two maintainers review (required for merge)

3.  **Feedback**: Address review comments

4.  **Approval**: Maintainers approve PR

5.  **Merge**: Squash and merge to main (maintainers only)


**Note**: Direct commits to main are strictly prohibited. All changes must go through pull requests.

## Documentation

### Code Documentation

*   **JavaDoc**: All public APIs must have JavaDoc

*   **Inline Comments**: Explain "why", not "what"

*   **README Updates**: Update README for user-facing changes

*   **API Docs**: Update OpenAPI spec for API changes


### Documentation Updates

When adding features:

*   Update relevant documentation in `docs/`

*   Add examples if applicable

*   Update API reference if API changed

*   Update configuration guide if config changed


## Project Structure

```text
guardian/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dreamsportslabs/guardian/
│   │   │       ├── cache/          # Caching layer
│   │   │       ├── client/         # Database clients
│   │   │       ├── config/         # Configuration classes
│   │   │       ├── constant/       # Constants
│   │   │       ├── dao/            # Data access objects
│   │   │       ├── dto/            # Data transfer objects
│   │   │       ├── exception/      # Exception classes
│   │   │       ├── filter/         # Request filters
│   │   │       ├── injection/      # Dependency injection
│   │   │       ├── jwtVerifier/    # JWT verification
│   │   │       ├── registry/       # Registry pattern
│   │   │       ├── rest/           # REST endpoints
│   │   │       ├── service/        # Business logic
│   │   │       ├── utils/          # Utility classes
│   │   │       ├── validation/    # Validation logic
│   │   │       └── verticle/       # Vert.x verticles
│   │   └── resources/
│   │       ├── oas/                # OpenAPI specifications
│   │       └── migrations/         # Database migrations
│   └── test/
│       └── java/                   # Test code
├── docs/                            # Documentation
├── CONTRIBUTING.md                  # This file
├── LICENSE                          # MIT License
└── README.md                        # Project README
```
## Community

### Getting Help

*   **Documentation**: Check [docs](./docs) first

*   **Discussions**: Ask on [GitHub Discussions](https://github.com/ds-horizon/guardian/discussions)

*   **Issues**: Search [existing issues](https://github.com/ds-horizon/guardian/issues)


### Communication Channels

*   **GitHub Discussions**: General questions and discussions

*   **GitHub Issues**: Bug reports and feature requests

*   **Pull Requests**: Code contributions


### Recognition

Contributors will be:

*   Listed in CONTRIBUTORS.md (if created)

*   Mentioned in release notes

*   Credited in documentation


## Questions?

If you have questions about contributing:

1.  Check the [documentation](./docs)

2.  Search [existing issues](https://github.com/ds-horizon/guardian/issues)

3.  Ask on [GitHub Discussions](https://github.com/ds-horizon/guardian/discussions)

4.  Contact maintainers (if needed)


## License

By contributing, you agree that your contributions will be licensed under the [MIT License](./LICENSE).

* * *

Thank you for contributing to Guardian! 🎉