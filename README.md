# MagicTheGatheringCommerce

Reactive Spring Boot commerce service scaffold for the TradingCardSite workspace.

## Included

- Java 21
- Maven wrapper
- Spring WebFlux and WebClient
- Spring Data R2DBC
- PostgreSQL JDBC and R2DBC drivers
- Flyway for PostgreSQL migrations
- Stripe Java SDK
- OAuth2 resource server dependencies
- Actuator health/info endpoints
- Dockerfile and Docker ignore file

## Local build

```powershell
.\mvnw.cmd clean package
```

## Docker Compose

The infrastructure project already contains a `magicthegatheringcommerce` service and commerce database settings. Build/run it from:

```powershell
cd ..\MagicTheGatheringInfrastructure
docker compose up --build magicthegatheringcommerce
```

Copy `.env.example` to `.env` when running this service directly outside the shared infrastructure compose setup.
