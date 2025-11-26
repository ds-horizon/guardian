<div align="center">
  <h1>Guardian</h1>
  <p><strong>Enterprise-grade Authentication & Authorization for Modern Applications</strong></p>
</div>

## 🌟 Overview

Guardian is a robust, open-source authentication and authorization solution designed for modern applications. It provides a comprehensive suite of authentication methods (passwordless, username/password, social login) with full OAuth 2.0 and OpenID Connect support, while giving you complete control over your user data.

## Why Guardian?

*   🔐 **Enterprise-Grade Security**: Built with security best practices and regular security audits

*   🎯 **Flexible Integration**: Works seamlessly with your existing user service

*   🏢 **Multi-Tenant Ready**: Supports multiple tenants with logical isolation

*   🚀 **Quick Implementation**: Get up and running in minutes

*   📱 **Multi-Platform Support**: Native support for web, mobile, and API authentication

*   🔑 **OAuth 2.0 & OIDC**: Full OAuth 2.0 and OIDC protocol support, secured with PKCE and discovery endpoints.

*   ⚡ **High Performance**: Built on Vert.x for reactive, non-blocking I/O


## 📋 Table of Contents

*   [Features](#-features)
*   [Getting Started](#-getting-started)
*   [Configuration](#-configuration)
*   [API Reference](#-api-reference)
*   [Deployment](#-deployment)
*   [Contributing](#-contributing)
*   [Community](#-community)
*   [License](#-license)

## ✨ Features

### Authentication Methods

*   **📱 Passwordless Authentication**

*   **🔑 Username Password Authentication**

*   **🌐 Social Authentication**

### Session Management

*   **📊 Multi-device session tracking**

*   **🔒 Secure session management**

*   **⚡ Real-time session invalidation**

*   **🔄 Token refresh mechanisms**

*   **🚪 Universal logout**

### OAuth 2.0 & OpenID Connect

*   **🔐 Full OAuth 2.0 implementation (Authorization Code, Implicit, Client Credentials)**

*   **✅ OpenID Connect 1.0 compliant (Discovery, UserInfo, JWKS endpoints)**

*   **🛡️ PKCE support for enhanced security**

*   **📝 Consent management**

### Multi-Tenant Architecture

*   **📈 Scalable design for serving multiple tenants**

*   **⚙️ Tenant-level configuration**


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

Make sure the following ports are free and not in use by other services:

*   `3306` – MySQL database
*   `6379` – Redis cache
*   `8080` – Guardian application server
*   `6000` – Mock user/communication service (for development)

If any of these ports are in use, you'll need to stop the conflicting services or modify the port mappings in `docker-compose.yaml`.

### Quick Start

1.  **Clone the repository**:
```bash
git clone https://github.com/ds-horizon/guardian.git
cd guardian
```

2.  **Start Guardian**:
```bash
./quick-start.sh
```

This script will:
- Build the Guardian application using Maven
- Start all required services (MySQL, Redis, Guardian, and mock services) using Docker Compose
- Run database migrations and seed initial data

**Wait for services to be ready**: The first startup may take 2-3 minutes. You can verify Guardian is running by checking:
```bash
curl http://localhost:8080/healthcheck
```

3.  **Test the setup** with passwordless flow:

The seed data includes a pre-configured tenant (`tenant1`) and client (`client1`) for testing.

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

**Expected response**: You'll receive a JSON response containing a `state` field. Copy this value for the next step.

**Complete authentication** (using mock OTP for development):
```bash
curl --location 'localhost:8080/v2/passwordless/complete' \
--header 'tenant-id: tenant1' \
--header 'Content-Type: application/json' \
--data '{
    "state": "<paste-state-from-init-response-here>",
    "otp": "999999"
}'
```

## ⚙️ Configuration

Guardian supports a wide range of configuration options for both the core application and individual tenants, including database, client, google, passwordless, and admin settings.

For the full list of configuration parameters and how to configure them, see the [Configuration Guide](https://ds-horizon.github.io/guardian-website/docs/configuration/configuration/).

## 📚 API Reference

### Guardian API Specification
The complete API specification including all endpoints, request/response schemas, and examples can be found in the [Guardian OpenAPI Specification](../src/main/resources/oas/guardian.yaml).

### Integration Endpoints
For information about the endpoints that your services need to implement to integrate with Guardian, refer to the [Integration Endpoints Specification](../src/main/resources/oas/integrations.yaml).

## 🚀 Deployment

Guardian can be deployed using Docker Compose for development or containerized / virtual machine deployments for production environments.

For detailed deployment instructions, production best practices, and infrastructure setup, see the [Deployment Guide](https://ds-horizon.github.io/guardian-website/docs/deployment/deployment/).


## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](https://github.com/ds-horizon/guardian/blob/main/CONTRIBUTING.md "https://github.com/ds-horizon/guardian/blob/main/CONTRIBUTING.md") for details on:

*   Code of Conduct

*   Development Process

*   Pull Request Process

*   Coding Standards

*   Testing Guidelines


## 👥 Community

*   💬 [GitHub Discussions](https://github.com/ds-horizon/guardian/discussions "https://github.com/ds-horizon/guardian/discussions") - Ask questions and share ideas

*   🐛 [Issue Tracker](https://github.com/ds-horizon/guardian/issues "https://github.com/ds-horizon/guardian/issues") - Report bugs and request features

*   📖 [Documentation](https://ds-horizon.github.io/guardian-website/docs/ "https://ds-horizon.github.io/guardian-website/docs/") - Comprehensive guides


## 📄 License

Guardian is licensed under the [MIT License](https://github.com/ds-horizon/guardian/blob/main/LICENSE "https://github.com/ds-horizon/guardian/blob/main/LICENSE").

* * *

Built with ❤️ by the Guardian team and contributors