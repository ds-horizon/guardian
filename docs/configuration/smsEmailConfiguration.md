# SMS/Email Configuration

This guide explains how to configure SMS and Email service integrations with Guardian for sending OTP and verification codes.

## Overview

Guardian integrates with your SMS and Email services to send OTP (One-Time Password) codes for passwordless authentication and contact verification. Both services use the same request format, with the only difference being the channel type and recipient format.

## Required Endpoints

Your SMS and Email services must implement the following endpoints:

### POST {send_sms_path} - Send SMS

Sends an SMS message with OTP or other content to a phone number.

**Request Body:**
```json
{
  "channel": "sms",
  "to": "+1234567890",
  "templateName": "otp_template",
  "templateParams": {
    "otp": "123456",
    "variable-1": "value-1",
    "variable-2": "value-2"
  }
}
```

### POST {send_email_path} - Send Email

Sends an email message with OTP or other content to an email address.

**Request Body:**
```json
{
  "channel": "email",
  "to": "user@example.com",
  "templateName": "otp_template",
  "templateParams": {
    "otp": "123456",
    "variable-1": "value-1",
    "variable-2": "value-2"
  }
}
```

**Request Parameters:**

| Parameter      | Type   | Required | Description                                    |
|----------------|--------|----------|------------------------------------------------|
| channel        | string | Yes      | Channel type: "sms" for SMS service, "email" for Email service |
| to             | string | Yes      | Phone number in E.164 format (SMS) or email address (Email) |
| templateName   | string | Yes      | Name of the template to use                    |
| templateParams | object | Yes      | Template parameters including OTP and variables |

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "Message sent successfully"
}
```

**Response (400 Bad Request):**
```json
{
  "error": {
    "message": "Invalid recipient format"
  }
}
```

**Response (500 Internal Server Error):**
```json
{
  "error": {
    "message": "Service unavailable"
  }
}
```

**Important Notes:**
- Both endpoints must accept POST requests
- Status code 200 indicates successful message delivery
- Any non-2xx status code will be treated as a failure
- The `templateParams` object will always include an `otp` field when sending OTP codes
- For SMS: Phone numbers should be in E.164 format (e.g., +1234567890)
- For Email: Email addresses should be in standard format (e.g., user@example.com)
- The `channel` field will be "sms" for SMS service calls and "email" for Email service calls

## Database Configuration

### SMS Configuration

Configure the SMS service in the `sms_config` table:

```sql
INSERT INTO sms_config (
  tenant_id,
  is_ssl_enabled,
  host,
  port,
  send_sms_path,
  template_name,
  template_params
) VALUES (
  'tenant1',
  false,
  'localhost',
  8082,
  '/sms/send',
  'otp_template',
  '{"otp": "{{otp}}", "app_name": "MyApp"}'
);
```

### SMS Table Schema

| Field           | Type         | Description                            |
|-----------------|--------------|----------------------------------------|
| tenant_id       | CHAR(10)     | Your tenant identifier (Primary Key)   |
| is_ssl_enabled  | BOOLEAN      | Whether SSL is enabled for SMS service |
| host            | VARCHAR(256) | SMS service host address               |
| port            | INT          | SMS service port number                |
| send_sms_path   | VARCHAR(256) | API path for sending SMS               |
| template_name   | VARCHAR(256) | Name of the SMS template               |
| template_params | JSON         | Template parameters in JSON format     |

### Email Configuration

Configure the Email service in the `email_config` table:

```sql
INSERT INTO email_config (
  tenant_id,
  is_ssl_enabled,
  host,
  port,
  send_email_path,
  template_name,
  template_params
) VALUES (
  'tenant1',
  false,
  'localhost',
  8083,
  '/email/send',
  'otp_template',
  '{"otp": "{{otp}}", "app_name": "MyApp"}'
);
```

### Email Table Schema

| Field           | Type         | Description                              |
|-----------------|--------------|------------------------------------------|
| tenant_id       | CHAR(10)     | Your tenant identifier (Primary Key)     |
| is_ssl_enabled  | BOOLEAN      | Whether SSL is enabled for email service |
| host            | VARCHAR(256) | Email service host address                |
| port            | INT          | Email service port number                 |
| send_email_path | VARCHAR(256) | API path for sending emails               |
| template_name   | VARCHAR(256) | Name of the email template               |
| template_params | JSON         | Template parameters in JSON format       |

### Configuration Fields Explained

**tenant_id**: Unique identifier for your tenant in Guardian

**is_ssl_enabled**: Set to `true` if your service uses HTTPS, `false` for HTTP

**host**: The hostname or IP address of your service (e.g., `localhost`, `sms.example.com`, `email.example.com`)

**port**: The port number where your service is running (e.g., `8082`, `8083`, `443`)

**send_sms_path / send_email_path**: The API endpoint path for sending messages (e.g., `/sms/send`, `/email/send`, `/api/v1/sms`, `/api/v1/email`)

**template_name**: Default template name to use for messages. This can be overridden in the request

**template_params**: Default template parameters as a JSON object. These will be merged with request-specific parameters

## Integration with Guardian Flows

Guardian will call your SMS and Email services in the following scenarios:

1. **Passwordless Authentication**: When a user initiates passwordless login/signup via SMS or Email
2. **Contact Verification**: When verifying phone numbers or email addresses for account security
3. **OTP Resend**: When a user requests to resend an OTP code

## Template Parameters

The `templateParams` object will contain:

- **otp**: The OTP code that needs to be sent (always present for OTP messages)
- **Custom variables**: Any additional variables defined in your template configuration

Example template parameters:
```json
{
  "otp": "123456",
  "app_name": "MyApp",
  "expiry_minutes": "5"
}
```

## Differences Between SMS and Email

While both services use the same request format, there are key differences:

| Aspect | SMS Service | Email Service |
|--------|------------|---------------|
| Channel | `"sms"` | `"email"` |
| Recipient Format | E.164 phone number (e.g., `+1234567890`) | Email address (e.g., `user@example.com`) |
| Configuration Table | `sms_config` | `email_config` |
| Path Field | `send_sms_path` | `send_email_path` |
| Use Cases | Phone verification, SMS OTP | Email verification, Email OTP |

## Best Practices

1. **Recipient Format**: 
   - For SMS: Always validate and normalize phone numbers to E.164 format
   - For Email: Validate email addresses using standard email validation
2. **Error Handling**: Return appropriate HTTP status codes and error messages
3. **Security**: Use HTTPS (`is_ssl_enabled: true`) in production environments
4. **Rate Limiting**: Implement rate limiting on your services to prevent abuse
5. **Logging**: Log all message sending attempts for debugging and auditing
6. **Template Management**: Support dynamic template names and parameters
7. **Idempotency**: Consider implementing idempotency keys to prevent duplicate sends
8. **Delivery Status**: Optionally implement webhooks to track message delivery status
9. **Channel Detection**: Use the `channel` field to determine whether to process as SMS or Email

## Troubleshooting

### Messages Not Being Sent

- Verify the `host`, `port`, and path fields (`send_sms_path` or `send_email_path`) are correct
- Check that your service is accessible from Guardian
- Ensure SSL configuration matches your service setup
- Check Guardian logs for error messages
- Verify the correct configuration table is used (`sms_config` for SMS, `email_config` for Email)

### Invalid Request Format

- Ensure your endpoint accepts JSON request bodies
- Verify the request includes all required fields: `channel`, `to`, `templateName`, `templateParams`
- Check that `templateParams` includes the `otp` field
- Verify the `channel` field matches your service type ("sms" or "email")

### Authentication Failures

- If using HTTPS, verify SSL certificates are valid
- Check network connectivity between Guardian and your service
- Verify firewall rules allow communication

### Wrong Channel Type

- Ensure your SMS service only processes requests with `channel: "sms"`
- Ensure your Email service only processes requests with `channel: "email"`
- Implement validation to reject requests with incorrect channel types

