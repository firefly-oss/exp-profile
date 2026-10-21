<a href="https://fireflysoftware.io/openplatform/">
<picture>
  <source media="(max-width: 640px) and (prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-narrow-dark.svg">
  <source media="(max-width: 640px)" srcset="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-narrow-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-dark.svg">
  <img src="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-light.svg" alt="Firefly OpenPlatform - Experience - Profile" width="100%">
</picture>
</a>

# Firefly OpenPlatform - Experience - Profile

Developed by the Firefly Software Foundation. [dev@fireflysoftware.io](mailto:dev@fireflysoftware.io)

[Website](https://fireflysoftware.io/) · [OpenPlatform](https://fireflysoftware.io/openplatform/) · [Module catalogue](https://fireflysoftware.io/openplatform/modules/)

> Backend-for-Frontend service for customer profile self-service management

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Functional Verticals](#functional-verticals)
- [API Endpoints](#api-endpoints)
- [Domain SDK Dependencies](#domain-sdk-dependencies)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Testing](#testing)

## Overview

`exp-profile` is the experience-layer service that provides authenticated customers with self-service access to their profile data. It exposes endpoints for reading and updating personal data, contact data, addresses, uploaded documents, consents, identity documents, and active contract summaries.

The service uses **simple composition** exclusively: every endpoint either aggregates read results from multiple domain services in parallel (using `Mono.zip()`) or forwards a single write command to the appropriate domain SDK. No workflow, Redis, or persistent journey state exists in this service.

Downstream dependencies are `domain-customer-people` (personal data, addresses, identity documents, and consents) and `core-common-contract-mgmt` (active contract summaries). The two are consumed independently — each via its own `ClientFactory` in the `-infra` module.

The party id is a path variable on `/api/v1/experience/profile/{partyId}`. IdentityPort resolves the actor, then AccessPort checks the action, before any domain call.

## Architecture

```
Frontend / Mobile App
         |
         v
exp-profile  (port 8103)
         |
         +---> domain-customer-people-sdk     (CustomersApi, AddressesApi, DocumentsApi, ConsentsApi)
         |
         +---> core-common-contract-mgmt-sdk  (ContractsApi)
```

The `getProfile` endpoint issues parallel calls to personal data, addresses, and identity documents via `Mono.zip()`, returning a single aggregated `ProfileDTO`.

## Module Structure

| Module | Purpose |
|--------|---------|
| `exp-profile-interfaces` | (Reserved for future shared contracts) |
| `exp-profile-core` | `ProfileService` interface, `ProfileServiceImpl`, command DTOs (personal data, contact data, address, document, consent, identity document), query DTOs (`ProfileDTO`, `AddressDTO`, `DocumentDTO`, `ConsentDTO`, `IdentityDocumentDTO`, `ContractSummaryDTO`) |
| `exp-profile-infra` | `CustomerPeopleClientFactory`, `ContractsClientFactory`, and their `@ConfigurationProperties` |
| `exp-profile-web` | `ProfileController`, Spring Boot application class, `application.yaml` |
| `exp-profile-sdk` | Auto-generated reactive SDK from the OpenAPI spec |

## Functional Verticals

| Vertical | Endpoints | Description |
|----------|-----------|-------------|
| Profile | 1 | Aggregated profile view (personal data + addresses + identity documents) |
| Personal & Contact Data | 2 | Partial update of name/date of birth; partial update of email/phone |
| Addresses | 4 | List, add, update, and delete addresses |
| Documents | 3 | List document metadata, upload a document, download document content |
| Consents | 2 | List consents, update consent status (ACCEPTED/REVOKED) |
| Identity Documents | 3 | List, add, and delete identity documents |
| Contracts | 1 | List active contract summaries |

## API Endpoints

### Profile

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/profile` | Retrieve the aggregated profile for the authenticated party | `200 OK` |

### Personal & Contact Data

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `PATCH` | `/api/v1/experience/profile/personal-data` | Partially update name and date of birth (only provided fields applied) | `204 No Content` |
| `PATCH` | `/api/v1/experience/profile/contact-data` | Partially update email and phone (only provided fields applied) | `204 No Content` |

### Addresses

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/profile/addresses` | List all addresses for the authenticated party | `200 OK` |
| `POST` | `/api/v1/experience/profile/addresses` | Add a new address | `201 Created` |
| `PUT` | `/api/v1/experience/profile/addresses/{id}` | Update an existing address | `200 OK` |
| `DELETE` | `/api/v1/experience/profile/addresses/{id}` | Delete an address | `204 No Content` |

### Documents

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/profile/documents` | List document metadata for the authenticated party | `200 OK` |
| `POST` | `/api/v1/experience/profile/documents` | Upload a document and return its metadata | `201 Created` |
| `GET` | `/api/v1/experience/profile/documents/{id}` | Download the raw binary content of a document | `200 OK` |

### Consents

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/profile/consents` | List all registered consents for the authenticated party | `200 OK` |
| `PUT` | `/api/v1/experience/profile/consents/{id}` | Update the status of a specific consent (ACCEPTED or REVOKED) | `200 OK` |

### Identity Documents

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/profile/identity-documents` | List all identity documents for the authenticated party | `200 OK` |
| `POST` | `/api/v1/experience/profile/identity-documents` | Register a new identity document (passport, national ID, etc.) | `201 Created` |
| `DELETE` | `/api/v1/experience/profile/identity-documents/{id}` | Remove an identity document | `204 No Content` |

### Contracts

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/profile/contracts` | List active contract summaries for the authenticated party | `200 OK` |

## Domain SDK Dependencies

| SDK | ClientFactory | APIs Used | Purpose |
|-----|--------------|-----------|---------|
| `domain-customer-people-sdk` | `CustomerPeopleClientFactory` | `CustomersApi`, `AddressesApi`, `DocumentsApi`, `ConsentsApi` | Personal data, contact data, address CRUD, document storage, consent management, identity documents |
| `core-common-contract-mgmt-sdk` | `ContractsClientFactory` | `ContractsApi` | Active contract summary retrieval |

## Configuration

```yaml
server:
  port: ${SERVER_PORT:8103}

api-configuration:
  domain-platform:
    customer-people:
      base-path: ${CUSTOMER_PEOPLE_URL:http://localhost:8081}
    common-contracts:
      base-path: ${CONTRACTS_URL:http://localhost:8090}
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8103` | HTTP server port |
| `CUSTOMER_PEOPLE_URL` | `http://localhost:8081` | Base URL for `domain-customer-people` |
| `CONTRACTS_URL` | `http://localhost:8090` | Base URL for `core-common-contract-mgmt` |

## Running Locally

```bash
# Prerequisites — ensure domain-customer-people and core-common-contract-mgmt are running
cd exp-profile
mvn spring-boot:run -pl exp-profile-web
```

Server starts on port `8103`. Swagger UI: [http://localhost:8103/swagger-ui.html](http://localhost:8103/swagger-ui.html)

## Testing

```bash
mvn clean verify
```

Tests cover `ProfileServiceImpl` (unit tests with mocked SDK clients using Mockito and `StepVerifier`) and `ProfileController` (WebTestClient-based controller tests verifying HTTP status codes and response shapes for all 17 endpoints).


## Identity, access, and decisions

Each journey command resolves the actor, then checks access, before it calls a domain port. A denial does not call the domain.

1. **IdentityPort** posts to `POST /api/v1/identity/subjects` on domain-core-security-center. The default username is `sample-journey`, which resolves to actor `11111111-1111-4111-8111-111111111111`. `denied-actor` resolves to `22222222-2222-4222-8222-222222222222`. `sample-employee` resolves to `33333333-3333-4333-8333-333333333333`. Experience code does not call Keycloak. The security center property `firefly.security-center.identity.adapter` is `sandbox` by default, or `keycloak`, `cognito`, or `internal-db`.
2. **AccessPort** posts to `POST /api/v1/access/checks` with the actor, action (`READ`, `CREATE`, `UPDATE`, `CLOSE`, or `DECIDE`), resource type, and resource id. The sample actor may READ and CREATE the sample resources. The denied actor receives `ACTOR_NOT_ENTITLED`.
3. Lending application create and onboarding initiation then call **DecisionPort** at `POST /api/v1/decisions` on domain-lending-credit-scoring. A `DECLINE` does not call the domain command.
4. The domain port runs only after access is allowed.

Set `firefly.experience.access.adapter=sandbox` only when the security-center process is not running. The default calls the security center. Set `api-configuration.domain-platform.security-center.base-path` (default `http://localhost:8085`).
