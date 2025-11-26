# Deployment Guide
This guide explains how to deploy and configure Guardian in a production environment, including database setup, environment configuration, and running the service.
## Table of Contents
*   [Overview](#overview)
*   [Prerequisites](#prerequisites)
*   [Local Development](#local-development)
*   [Production Deployment](#production-deployment)
*   [Environment Variables Reference](#environment-variables-reference)
*   [Troubleshooting](#troubleshooting)
## Overview
This guide covers deploying Guardian in both local development and production environments. It includes setup instructions for MySQL, Redis, configuration, and running the application.
## Prerequisites
Before deploying Guardian, ensure you have:
*   **Java 17+**: [Download](https://docs.oracle.com/en/java/javase/17/install/index.html)
*   **MySQL 8.0+**: Database for storing Guardian data : [Download](https://dev.mysql.com/downloads/mysql/)
*   **Redis 6.2+**: Cache for temporary state management :[Download](https://redis.io/downloads/)
*   **Maven 3.6+** (for local development): [Download](https://maven.apache.org/docs/3.6.3/release-notes.html)
*   **Docker Desktop** (for local development): [Download](https://docs.docker.com/desktop/setup/install/mac-install/)
## Local Development
### Prerequisites
Verify installations:
```bash
java -version # Java 17+
mvn --version # Maven 3.6+
docker --version # 20.10+
docker compose version # 2.0+
```
### Port Requirements
Ensure these ports are available:
*   `3306` - MySQL
*   `6379` - Redis
*   `8080` - Guardian application
*   `6000` - Mock services (optional)
### Quick Start
The repository includes a `docker-compose.yaml` that sets up everything:
```bash
# Clone the git repo
git clone https://github.com/ds-horizon/guardian.git

cd guardian
# Build and start all services
./quick-start.sh
```
This script:
1.  Builds the Guardian JAR using Maven
2.  Builds Docker images
3.  Starts MySQL, Redis, and Guardian containers
4.  Runs database migrations automatically
### Verify Installation
**1. Check health endpoint:**
```bash
curl http://localhost:8080/healthcheck
```
**2. Test passwordless authentication:**
```bash
curl --location 'http://localhost:8080/v2/passwordless/init' \
  --header 'Content-Type: application/json' \
  --header 'tenant-id: tenant1' \
  --data '{
    "client_id": "client1",
    "scopes": ["default"],
    "flow": "signinup",
    "response_type": "token",
    "contacts": [{
      "channel": "sms",
      "identifier": "9999999999"
    }]
  }'
```
## Production Deployment
### Infrastructure Requirements
Before deploying Guardian, ensure you have:
1.  **MySQL Database** (8.0+)
2.  **Redis Cache** (6.2+)
3.  A compute environment capable of running **Java 17+**
### Database Setup
Use any external or managed MySQL instance and create:
```sql
CREATE DATABASE guardian;
CREATE USER 'guardian'@'%' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON guardian.* TO 'guardian'@'%';
FLUSH PRIVILEGES;
```
These values will be referenced in `.env`.
### Redis Setup
Guardian requires **Redis 6.2+** (standalone or cluster mode).
> **Note**: Set `GUARDIAN_REDIS_TYPE=STANDALONE` or `GUARDIAN_REDIS_TYPE=CLUSTER` accordingly in `.env` file
### Configuration
#### Step 1: Create a Directory for Guardian
Create a directory where Guardian will run and store its configuration, logs, and application files.
```bash
mkdir -p /opt/guardian
```
#### Step 2: Download the Prebuilt Guardian JAR
Download the prebuilt Guardian JAR from [releases](https://github.com/ds-horizon/guardian/releases).
Place the following on your server:
```text
/opt/guardian/
  ├─ guardian.jar
  ├─ logback/
  └─ .env
```
#### Step 3: Create .env File
Create a `.env` file with the following configuration:
```text
# Database Configuration
GUARDIAN_MYSQL_WRITER_HOST=<your-db-host.example.com>
GUARDIAN_MYSQL_READER_HOST=<your-db-host.example.com>  # Can be same as writer if no read replica
GUARDIAN_MYSQL_DATABASE=guardian
GUARDIAN_MYSQL_USER=<db-user>
GUARDIAN_MYSQL_PASSWORD=<db-password>
GUARDIAN_MYSQL_WRITER_MAX_POOL_SIZE=10
GUARDIAN_MYSQL_READER_MAX_POOL_SIZE=40

# Redis Configuration
GUARDIAN_REDIS_HOST=<your-redis-host.example.com> # Use CLUSTER endpoint for Cluster mode
GUARDIAN_REDIS_PORT=6379
GUARDIAN_REDIS_TYPE=STANDALONE  # Use CLUSTER for Redis cluster mode

# Application Configuration
GUARDIAN_PORT=8080
LOGBACK_FILE=logback/logback.xml

# HTTP Client Configuration (optional, defaults shown)
GUARDIAN_HTTP_CONNECT_TIMEOUT=1000
GUARDIAN_HTTP_READ_TIMEOUT=1000
GUARDIAN_HTTP_WRITE_TIMEOUT=1000
GUARDIAN_TENANT_CONFIG_REFRESH_INTERVAL=10
```
> **Note**: Store sensitive values in a secrets manager or protected files with restricted permissions
```bash
chmod 600 /opt/guardian/.env
```
Ensure the machine can reach DB and Redis before running migrations.
### Database Migrations
Guardian uses Liquibase for database schema management. Migrations are located in `src/main/resources/migrations/`.
> **Note**: Run migrations manually on your database before starting Guardian. This is the recommended and safest approach for production.
#### Running through Liquibase
1.  Install Liquibase (if not installed): [Download](https://www.liquibase.org/download)
2.  Run this from any machine or any host with database access:
    ```bash
    liquibase \
      --url="jdbc:mysql://your-db-host.example.com:3306/guardian" \
      --username=<db-user> \
      --password=<db-password> \
      --changeLogFile=src/main/resources/migrations/changelog.xml \
      update
    ```
3.  Verify the schema:
    ```bash
    mysql -h <your-db-host.example.com> -u <db-user> -p guardian -e "SHOW TABLES;"
    ```
    You should see tables like `tenant`, `client`, `scope`, etc.
### Running the Application
#### Run as a Service (Systemd or Equivalent)
Create service file `/etc/systemd/system/guardian.service`:
```text
[Unit]
Description=Guardian Authentication Service
After=network.target

[Service]
Type=simple
User=guardian
Group=guardian
WorkingDirectory=/opt/guardian
EnvironmentFile=/opt/guardian/.env
ExecStart=/usr/bin/java -Dlogback.configurationFile=/opt/guardian/logback/logback.xml -jar /opt/guardian/guardian.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=guardian

[Install]
WantedBy=multi-user.target
```
Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable guardian
sudo systemctl start guardian
sudo systemctl status guardian
```
View logs:
```bash
sudo journalctl -u guardian -f
```
#### Direct Java Execution
Useful for testing or simple setups.
```bash
cd /opt/guardian
source .env
java -Dlogback.configurationFile=logback/logback.xml -jar guardian.jar
```
### Verification
**1. Health check:**
```bash
curl http://localhost:8080/healthcheck
```
**2. Test authentication flow:**
```bash
curl --location 'http://localhost:8080/v2/passwordless/init' \
  --header 'Content-Type: application/json' \
  --header 'tenant-id: tenant1' \
  --data '{
    "client_id": "client1",
    "scopes": ["default"],
    "flow": "signinup",
    "response_type": "token",
    "contacts": [{
      "channel": "sms",
      "identifier": "9999999999"
    }]
  }'
```
**3. Check application logs:**
```bash
sudo journalctl -u guardian -f
```
## Environment Variables Reference
| Variable | Description | Example | Required | Default |
| --- | --- | --- | --- | --- |
| **Database** | | | | |
| GUARDIAN_MYSQL_WRITER_HOST | MySQL primary host | db.example.com | Yes | None |
| GUARDIAN_MYSQL_READER_HOST | MySQL read replica host | db-read.example.com | Yes | None |
| GUARDIAN_MYSQL_DATABASE | Database name | guardian | Yes | None |
| GUARDIAN_MYSQL_USER | Database username | admin | Yes | None |
| GUARDIAN_MYSQL_PASSWORD | Database password | SecurePass123! | Yes | None |
| GUARDIAN_MYSQL_WRITER_MAX_POOL_SIZE | Writer connection pool | 10 | No | 10 |
| GUARDIAN_MYSQL_READER_MAX_POOL_SIZE | Reader connection pool | 40 | No | 40 |
| **Redis** | | | | |
| GUARDIAN_REDIS_HOST | Redis host | redis.example.com | Yes | None |
| GUARDIAN_REDIS_PORT | Redis port | 6379 | No | 6379 |
| GUARDIAN_REDIS_TYPE | Redis mode | STANDALONE or CLUSTER | No | STANDALONE |
| **Application** | | | | |
| GUARDIAN_PORT | Application port | 8080 | No | 8080 |
| LOGBACK_FILE | Logback config | logback/logback.xml | No | logback/logback.xml |
| **HTTP Client** | | | | |
| GUARDIAN_HTTP_CONNECT_TIMEOUT | Connect timeout (ms) | 1000 | No | 1000 |
| GUARDIAN_HTTP_READ_TIMEOUT | Read timeout (ms) | 1000 | No | 1000 |
| GUARDIAN_HTTP_WRITE_TIMEOUT | Write timeout (ms) | 1000 | No | 1000 |
| GUARDIAN_TENANT_CONFIG_REFRESH_INTERVAL | Config cache TTL (s) | 10 | No | 10 |
| GUARDIAN_HTTP_CLIENT_KEEP_ALIVE | Enable keep-alive | true | No | true |
| GUARDIAN_HTTP_CLIENT_KEEP_ALIVE_TIMEOUT | Keep-alive timeout (ms) | 8000 | No | 8000 |
| GUARDIAN_HTTP_CLIENT_IDLE_TIMEOUT | Idle timeout (ms) | 6000 | No | 6000 |
| GUARDIAN_HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE | Max pool size | 256 | No | 256 |
| GUARDIAN_APPLICATION_SHUTDOWN_GRACE_PERIOD | Shutdown grace period (s) | 30 | No | 30 |
## Troubleshooting
### Check Service Logs First
```bash
# Systemd
sudo journalctl -u guardian -n 100
```
If the issue isn't clear, continue with the checks below.
### Common Issues and Fixes
1.  **Database connection failure**
    -  Verify `GUARDIAN_MYSQL_WRITER_HOST` is correct
    -  Test connection: `mysql -h <HOST> -u <USER> -p`
    -  Check firewall/security group rules
    -  Verify credentials
2.  **Redis connection failure**
    -  Verify `GUARDIAN_REDIS_HOST` is correct
    -  Test connection: `redis-cli -h <HOST> ping`
    -  Verify `GUARDIAN_REDIS_TYPE` matches your setup
3.  **Port already in use**
    -  Check if port 8080 is available: `netstat -tuln | grep 8080` or `ss -tuln | grep 8080`
    -  Change `GUARDIAN_PORT` if needed
4.  **JAR file not found**
    -  Verify file exists: `ls -la /opt/guardian/guardian.jar`
    -  Check file permissions: `sudo chown guardian:guardian /opt/guardian/guardian.jar`