# Spring Data Remote

A Spring Data extension that enables repository interfaces to fetch entities via remote API calls (REST, gRPC, etc.) instead of database queries.

## Features

- **Pluggable Transport Layer**: Start with REST, switch to gRPC or other protocols without changing your repository code
- **Spring Data Integration**: Works seamlessly with Spring Data's repository abstraction
- **Zero Boilerplate**: Just define an interface and let the framework handle the rest
- **Spring Boot Auto-configuration**: Automatic setup when using Spring Boot

## Quick Start

### 1. Add Dependencies

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-remote-rest</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Configure Base URL

```yaml
spring:
  data:
    remote:
      rest:
        base-url: https://api.example.com
```

### 3. Define Your Entity

```java
@RemoteResource(path = "/users")
public class User {
    @Id
    private Long id;
    private String name;
    private String email;

    // getters and setters
}
```

### 4. Create a Repository

```java
public interface UserRepository extends RemoteRepository<User, Long> {
    // Standard CRUD methods are inherited:
    // - save(entity)
    // - findById(id)
    // - findAll()
    // - deleteById(id)
    // - count()
    // - existsById(id)
}
```

### 5. Enable Remote Repositories

```java
@Configuration
@EnableRemoteRepositories(basePackages = "com.example.repository")
public class AppConfig {
}
```

### 6. Use It

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

## Modules

| Module | Description |
|--------|-------------|
| `spring-data-remote-transport-api` | Transport layer SPI (interfaces) |
| `spring-data-remote-core` | Core Spring Data abstractions |
| `spring-data-remote-rest` | REST transport implementation |
| `spring-data-remote-grpc` | gRPC transport implementation |
| `spring-data-remote-vertx` | Vert.x event bus transport implementation |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Your Application                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │     UserRepository extends RemoteRepository          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                Spring Data Remote Core                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  RemoteRepositoryFactory → SimpleRemoteRepository    │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Transport SPI                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  TransportClient ← TransportRequest/TransportResponse │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌──────────┐   ┌──────────┐   ┌──────────┐
        │   REST   │   │   gRPC   │   │  Vert.x  │
        │(included)│   │(included)│   │(included)│
        └──────────┘   └──────────┘   └──────────┘
```

## Building

```bash
mvn clean install
```

## License

Apache License 2.0