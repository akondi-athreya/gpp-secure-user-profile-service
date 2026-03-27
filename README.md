# User Profile Service with HashiCorp Vault

Production-style Spring Boot microservice that externalizes sensitive configuration in HashiCorp Vault.

## What This Project Demonstrates

- Secure secret management using Vault instead of hardcoded credentials.
- Spring Cloud Vault bootstrap configuration (`spring-cloud-starter-vault-config`).
- Secret-driven database and JWT configuration:
  - `db.username`
  - `db.password`
  - `api.signing-key`
- Containerized startup orchestration where app startup depends on Vault health and secret initialization.
- Fail-fast startup behavior when Vault is unavailable.

## Architecture

The system runs with three containers:

- `vault`: Vault dev server (port `8200`) with static dev root token.
- `vault-init`: one-time initializer that writes required secrets to `secret/user-profile-service`.
- `app`: Spring Boot API (port `8080`) that reads secrets from Vault at startup.

Startup order is controlled with `depends_on` and health checks:

1. `vault` becomes healthy.
2. `vault-init` runs `scripts/setup-vault.sh` and is healthy only after secret path can be read.
3. `app` starts after `vault-init` is healthy.

## Required Files Included

- `docker-compose.yml`
- `Dockerfile`
- `scripts/setup-vault.sh`
- `.env.example`
- `src/` Java source code
- `pom.xml`

## Secret Path and Keys

Vault path:

- `secret/user-profile-service`

Required keys:

- `db.username`
- `db.password`
- `api.signing-key`

## API Endpoints

### 1) Create Profile

- Method: `POST`
- Path: `/api/profile`
- Request JSON:

```json
{
  "userId": "u-001",
  "username": "alice",
  "email": "alice@test.com"
}
```

- Success: `201 Created`
- Response header: `Authorization: Bearer <jwt-token>`
- Response body: created profile JSON

### 2) Get Profile by ID

- Method: `GET`
- Path: `/api/profile/{userId}`
- Success: `200 OK`
- Not found: `404 Not Found`

### 3) Vault Status

- Method: `GET`
- Path: `/api/admin/vault-status`
- Success response:

```json
{
  "vaultConnected": true,
  "secretPath": "secret/user-profile-service",
  "resolvedDbUsername": "sa"
}
```

## Local Run

### 1) (Optional) Set environment file

```bash
cp .env.example .env
```

### 2) Build and start containers

```bash
docker compose up --build
```

### 3) Stop containers

```bash
docker compose down
```

## Manual Verification Snippets

Create profile:

```bash
curl -i -X POST http://localhost:8080/api/profile \
  -H "Content-Type: application/json" \
  -d '{"userId":"u-001","username":"alice","email":"alice@test.com"}'
```

Get existing profile:

```bash
curl -i http://localhost:8080/api/profile/u-001
```

Get missing profile:

```bash
curl -i http://localhost:8080/api/profile/u-999
```

Check vault status endpoint:

```bash
curl -i http://localhost:8080/api/admin/vault-status
```

Check secrets in Vault:

```bash
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN=my-root-token vault \
  vault kv get -format=json secret/user-profile-service
```

## Fail-Fast Behavior Verification

This app is intentionally designed to fail startup when Vault is unavailable.

Example verification:

```bash
docker stop vault
docker restart app
sleep 8
docker inspect app --format '{{.State.Status}} {{.State.ExitCode}}'
```

Expected: app exits with non-zero code (typically `exited 1`).

## Security Notes

This repository uses Vault dev mode and static root token for learning only.
For production, use:

- non-root auth methods (AppRole, Kubernetes Auth, IAM auth)
- least-privilege Vault policies
- dynamic DB credentials where possible
- TLS and secret rotation
