---
title: Getting Started
description: Get up and running with Guardian in 5 minutes!
---


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
