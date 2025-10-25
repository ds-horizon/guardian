<div align="center">
  <h1>Guardian</h1>
  <p><strong>Enterprise-grade Authentication & Authorization for Modern Applications</strong></p>
</div>

## 🌟 Overview

Guardian is a robust, open-source authentication and authorization solution designed for modern applications.
It provides a comprehensive suite of authentication methods while giving you complete control over your user data.

### Why Guardian?

- 🔐 **Enterprise-Grade Security**: Built with security best practices and regular security audits
- 🎯 **Flexible Integration**: Works seamlessly with your existing user service
- 🚀 **Quick Implementation**: Get up and running in minutes
- 📱 **Multi-Platform Support**: Native support for web, mobile, and API authentication

## 📋 Table of Contents

- [Features](#-features)
- [Getting Started](#-getting-started)
- [Contributing](#-contributing)
- [Community](#-community)
- [License](#-license)

## ✨ Features

### Authentication Methods
- 📱 **Passwordless Authentication**
  - SMS/Email OTP
- 🔑 **Traditional Authentication**
  - Username/Password
- 🌐 **Social Authentication**
  - Google
  - Facebook
  - Custom Providers

### Session Management

- 📊 Multi-device session tracking
- 🔒 Secure session management
- ⚡ Real-time session invalidation
- 🔄 Token refresh mechanisms

### Developer Experience

- 🎯 RESTful APIs
- 📚 Comprehensive SDK support

## 🚀 Getting Started

### Prerequisites

* **Docker** ([Download Docker Desktop](https://www.docker.com/products/docker-desktop/))
* **Docker Compose CLI** ([Install instructions](https://docs.docker.com/compose/install/))
* **Maven**
* **Java 17**

### Quick Start

#### Prerequisites
You can verify the installations by running the following commands in your terminal:

```bash
docker --version
mvn --version
java -version
```

Ensure that **Java 17** is the active version in use. Maven should also be configured to use Java 17 - you can verify this by checking that `mvn --version` shows Java 17 in its output.

Additionally, make sure the following ports are free and not in use by other services :

* `3306` – MySQL
* `6379` – Redis
* `8080` – Application server
* `6000` – Auxiliary services/API

These ports are required for the application to run without conflicts.

1. Clone the repository:
```bash
git clone https://github.com/ds-horizon/guardian.git
cd guardian
```

2. Start Guardian:
```bash
./quick-start.sh
```

3. Test the setup with a passwordless flow:

```bash
# Initialize passwordless authentication
curl --location 'localhost:8080/v1/passwordless/init' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
  "flow": "signinup",
  "responseType": "token",
  "contacts": [{
    "channel": "sms",
    "identifier": "9999999999"
  }],
  "metaInfo": {
    "ip": "127.0.0.1",
    "location": "localhost",
    "deviceName": "localhost",
    "source": "app"
  }
}'

# Complete authentication (using mock OTP for development)
curl --location 'localhost:8080/v1/passwordless/complete' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
  "state": "<state-from-init-response>",
  "otp": "999999"
}'
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:

- Code of Conduct
- Development Process
- Pull Request Process
- Coding Standards

## 👥 Community

- [GitHub Discussions](https://github.com/ds-horizon/guardian/discussions)

## 📄 License

Guardian is licensed under the [MIT License](LICENSE).

---

<div align="center">
  <sub>Built with ❤️ by the Guardian team and contributors</sub>
</div>
