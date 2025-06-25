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

Additionally, make sure the following ports are free and not in use by other services:

* `3306` – MySQL
* `6379` – Redis
* `8080` – Application server
* `6000` – Auxiliary services/API

These ports are required for the application to run without conflicts.

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

## 🛠️ Running Locally Without Docker

If you prefer to run Guardian without Docker, follow these steps:

### 1. Install Prerequisites
- **Java 17** ([Download](https://adoptium.net/))
- **Maven** ([Download](https://maven.apache.org/download.cgi))
- **MySQL** ([Download](https://dev.mysql.com/downloads/mysql/))
- **Redis** ([Download](https://redis.io/download/))
- **Liquibase** ([Download](https://www.liquibase.com/download-oss))
- **MySQL Connector/J** ([Download](https://dev.mysql.com/downloads/connector/j/))

### 2. Start MySQL and Redis
- Start a local MySQL server (default port: `3306`).
- Start a local Redis server (default port: `6379`).

### 2.1 Create the Guardian Database
Before running migrations, create the database if it does not exist:
```bash
mysql -h 127.0.0.1 -u root -p -e "CREATE DATABASE IF NOT EXISTS guardian;" 
```

### 3. Run Database Migrations with Liquibase
Guardian uses [Liquibase](https://www.liquibase.org/) to manage database schema and seed data. After starting MySQL, run the following command to apply all migrations and seed data:

```bash
liquibase --classpath=<path_to_mysql_jar> --url=jdbc:mysql://127.0.0.1:3306/guardian --username=root --password=password --changeLogFile=./resources/changelog.xml update
```

### 3.1 (Optional) Add seed data to be able to follow the Quick Start
```bash
liquibase --classpath=<path_to_mysql_jar> --url=jdbc:mysql:///127.0.0.1:3306/guardian --username=root --password=password --changeLogFile=none execute-sql --sql-file=./resources/seed.sql
```

### 4. Configure Guardian

You can modify the database connection details (host, port, database, username, password) by changing the values in the command above, or by setting the following environment variables before running Guardian:

- `GUARDIAN_MYSQL_WRITER_HOST` (default: `localhost`)
- `GUARDIAN_MYSQL_READER_HOST` (default: `localhost`)
- `GUARDIAN_MYSQL_DATABASE` (default: `guardian`)
- `GUARDIAN_MYSQL_USER` (default: `root`)
- `GUARDIAN_MYSQL_PASSWORD` (default: `password`)
- `GUARDIAN_MYSQL_READER_PORT` (default: `3306`)
- `GUARDIAN_MYSQL_WRITER_PORT` (default: `3306`)
- `GUARDIAN_REDIS_HOST` (default: `localhost`)
- `GUARDIAN_REDIS_PORT` (default: `6379`)

For example, to use a different database name and password:
```bash
export GUARDIAN_MYSQL_DATABASE=guardian
export GUARDIAN_MYSQL_PASSWORD=password
```

Guardian uses `src/main/resources/guardian-default.conf` for default config. You can override values using environment variables or by editing `guardian.conf`.

Default MySQL/Redis config:
- MySQL user: `root`, password: `password`, database: `guardian`, host: `localhost`
- Redis host: `localhost`, port: `6379`

### 5. Build the Application
```bash
mvn clean package
```

### 6. Run Guardian
```bash
java -cp target/guardian/guardian.jar com.dreamsportslabs.guardian.Main
```

Guardian will start on port `8080` by default. You can test the API as shown in the Quick Start section.

To change the port, set the `GUARDIAN_PORT` environment variable before running the application:
```bash
GUARDIAN_PORT=8080 java -cp target/guardian/guardian.jar com.dreamsportslabs.guardian.Main
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
