# AGENTS.md

Guidelines for AI agents working on this codebase.

## Project Overview

**Spring Data Remote** is a Spring Data extension that enables repository interfaces to fetch entities via remote API calls (REST, gRPC, etc.) instead of database queries.

## Module Structure

```
spring-data-remote/
├── spring-data-remote-transport-api/  # Transport layer SPI (pure interfaces)
├── spring-data-remote-core/           # Spring Data integration
└── spring-data-remote-rest/           # REST transport implementation
```

## Key Design Decisions

### Pluggable Transport Layer
- All protocol-specific code goes through `TransportClient` interface
- New protocols (e.g., gRPC) implement `TransportClient` in separate modules
- Core module never depends on protocol-specific code

### Naming Conventions
- Interfaces: No prefix/suffix (e.g., `TransportClient`)
- Implementations: Prefix with protocol (e.g., `RestTransportClient`)
- Spring Data integration classes: Prefix with `Remote` (e.g., `RemoteRepository`)

## Build & Test

```bash
# Build all modules
mvn clean compile

# Run all tests
mvn test

# Build specific module
mvn compile -pl spring-data-remote-rest
```

## Architecture Layers

```
User Repository (extends RemoteRepository)
         ↓
SimpleRemoteRepository (CRUD implementation)
         ↓
TransportClient (SPI interface)
         ↓
RestTransportClient / GrpcTransportClient (protocol impl)
```

## Adding New Transport (e.g., gRPC)

1. Create new module `spring-data-remote-grpc`
2. Implement `TransportClient<GrpcTransportConfig>`
3. Map `TransportOperation` to gRPC calls
4. Add Spring Boot auto-configuration

## Code Conventions

- Java 17+ features allowed
- No Lombok (JDK 24 compatibility issues)
- Use `@NonNull` annotations from `org.springframework.lang`
- Builder pattern for immutable DTOs
- Tests use JUnit 5 + Mockito

## Important Files

| File | Purpose |
|------|---------|
| `TransportClient.java` | Core SPI - all transports implement this |
| `RemoteRepository.java` | Main user-facing interface |
| `SimpleRemoteRepository.java` | CRUD operations implementation |
| `@RemoteResource` | Entity annotation for path configuration |
