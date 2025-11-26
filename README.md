# Guardian

**Enterprise-grade Authentication & Authorization for Modern Applications**

## 🌟 Overview

Guardian is a robust, open-source authentication and authorization solution designed for modern applications. It provides a comprehensive suite of authentication methods while giving you complete control over your user data.

## Why Guardian?

*   🔐 **Enterprise-Grade Security**: Built with security best practices and regular security audits

*   🎯 **Flexible Integration**: Works seamlessly with your existing user service

*   🏢 **Multi-Tenant Ready**: Complete tenant isolation, perfect for SaaS applications

*   🚀 **Quick Implementation**: Get up and running in minutes

*   📱 **Multi-Platform Support**: Native support for web, mobile, and API authentication

*   🔑 **OAuth 2.0 & OIDC**: Full OAuth 2.0 and OIDC protocol support, secured with PKCE and discovery endpoints.

*   ⚡ **High Performance**: Built on Vert.x for reactive, non-blocking I/O


## 📋 Table of Contents

*   [Features](#-features)
*   [Getting Started](#-getting-started)
*   [Documentation](#-documentation)
*   [Contributing](#-contributing)
*   [Community](#-community)
*   [License](#-license)

## ✨ Features

### Authentication Methods

*   **📱 [Passwordless Authentication](./docs/features/passwordless-authentication.md)**

    *   SMS/Email OTP

    *   Configurable OTP length and validity

    *   Rate limiting and retry mechanisms

*   **🔑 [Username/Password Authentication](./docs/features/username-password-authentication.md)**

    *   Username/Password

    *   Email/Phone as identifier

    *   Password policy support via user service

*   **🌐 [Social Authentication](./docs/features/social-authentication.md)**

    *   [Google](./docs/features/google-authentication.md)

    *   [Facebook](./docs/features/facebook-authentication.md) 

    *   Custom OIDC providers


### [Session Management](./docs/features/post-authentication.md)

*   📊 Multi-device session tracking

*   🔒 Secure session management

*   ⚡ Real-time session invalidation

*   🔄 Token refresh mechanisms

*   🚪 Universal logout


### OAuth 2.0 & OpenID Connect

*   Full OAuth 2.0 implementation (Authorization Code, Implicit, Client Credentials)

*   OpenID Connect 1.0 compliant (Discovery, UserInfo, JWKS endpoints)

*   PKCE support for enhanced security

*   Consent management


### Multi-Tenant Architecture

*   Scalable design for serving multiple tenants
*   Per-tenant configuration


## 🚀 Getting Started

### Prerequisites

*   **Docker** ≥ 20.10 ([Download Docker Desktop](https://www.docker.com/products/docker-desktop/ "https://www.docker.com/products/docker-desktop/"))

*   **Docker Compose** ≥ 2.0 (Usually included with Docker Desktop)([Install instructions](https://docs.docker.com/compose/install/))

*   **Maven** ≥ 3.6 ([Download Maven](https://maven.apache.org/download.cgi "https://maven.apache.org/download.cgi"))

*   **Java 17** (JDK) ([Download Java 17](https://www.oracle.com/java/technologies/downloads/#java17 "https://www.oracle.com/java/technologies/downloads/#java17"))


### Verify Installations

You can verify the installations by running the following commands in your terminal:

```bash
 docker --version
mvn --version
java -version
```


**Important**: Ensure that Java 17 is the active version in use. Maven should also be configured to use Java 17 - you can verify this by checking that `mvn --version` shows Java 17 in its output.

### Port Requirements

Additionally, make sure the following ports are free and not in use by other services:

*   `3306` – MySQL

*   `6379` – Redis

*   `8080` – Application server

*   `6000` – Auxiliary services/API


These ports are required for the application to run without conflicts.

### Quick Start

1.  **Clone the repository**:
```bash
  git clone https://github.com/ds-horizon/guardian.git
```
2.  **Start Guardian**:

    **cd guardian**
```bash
  ./quick-start.sh
```
3.  **Test the setup** with passwordless flow:

    **Initialize passwordless authentication**:
```bash
  curl --location 'localhost:8080/v2/passwordless/init' \
--header 'tenant-id: tenant1' \
--header 'Content-Type: application/json' \
--data '{
    "contacts": [
        {
            "channel": "SMS",
            "identifier": "7878787878"
        }
    ],
    "flow": "SIGNINUP",
    "response_type": "token",
    "client_id": "client1"
}'
```

  **Complete authentication** (using mock OTP for development):
```bash
  curl --location 'localhost:8080/v2/passwordless/complete' \
--header 'tenant-id: tenant1' \
--header 'Content-Type: application/json' \
--data '{
    "state": "<state-from-init-response>",
    "otp": "999999"
}'
```

## 📚 Documentation

Comprehensive documentation is available in the `docs/` directory:

| Document                                                  | Description |
|-----------------------------------------------------------| --- |
| 📚 [API Reference](./docs/ApiReference.md)                | Complete REST API documentation |
| ⚙️ [Configuration](./docs/configuration/Configuration.md) | Configuration options |
| 🚀 [Deployment](./docs/deployment.md)                     | Production deployment guide |

### API Specifications

*   [Guardian OpenAPI Specification](./src/main/resources/oas/guardian.yaml) - Guardian Complete API spec

*   [Integration API Spec](./src/main/resources/oas/integrations.yaml) - All other services integrations (User service, Communication service)


## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](./CONTRIBUTING.md) for details on:

*   Code of Conduct

*   Development Process

*   Pull Request Process

*   Coding Standards

*   Testing Guidelines


## 👥 Community

*   💬 [GitHub Discussions](https://github.com/ds-horizon/guardian/discussions) - Ask questions and share ideas

*   🐛 [Issue Tracker](https://github.com/ds-horizon/guardian/issues "https://github.com/ds-horizon/guardian/issues") - Report bugs and request features

*   📖 [Documentation](./docs) - Comprehensive guides


## 📄 License

Guardian is licensed under the [MIT License](./LICENSE).

* * *

Built with ❤️ by the Guardian team and contributors