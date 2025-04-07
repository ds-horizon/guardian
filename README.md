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

- Docker
- Maven

### Quick Start

1. Clone the repository:
```bash
git clone https://github.com/dream-sports-labs/guardian.git
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

- [GitHub Discussions](https://github.com/dream-sports-labs/guardian/discussions)

## 📄 License

Guardian is licensed under the [MIT License](LICENSE).

---

<div align="center">
  <sub>Built with ❤️ by the Guardian team and contributors</sub>
</div>
