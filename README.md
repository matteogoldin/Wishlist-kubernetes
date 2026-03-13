# Wishlists App
[![Java CI with Maven](https://github.com/matteogoldin/Wishlist/actions/workflows/maven_build_linux.yml/badge.svg)](https://github.com/matteogoldin/Wishlist/actions/workflows/maven_build_linux.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=matteogoldin_Wishlist&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=matteogoldin_Wishlist)
[![Coverage Status](https://coveralls.io/repos/github/matteogoldin/Wishlist/badge.svg?branch=main)](https://coveralls.io/github/matteogoldin/Wishlist?branch=main)
[![pitest](https://github.com/matteogoldin/Wishlist/actions/workflows/pit.yml/badge.svg)](https://github.com/matteogoldin/Wishlist/actions/workflows/pit.yml)

## Requirements
- JDK11 or JDK17
- Eclipse IDE
- Maven
- Docker

## Usage
- Import the Git repository in Eclipse IDE
- Build the project with Maven
  ```bash
  mvn clean install
  ```
- Create a Docker container with an instance of MySQL runninng on it
  ```bash
  docker run --name wishlist-app-container -d -p 3309:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=wishlists-schema -e MYSQL_USER=java-client -e MYSQL_PASSWORD=password --restart unless-stopped -v mysql:/var/lib/mysql mysql:8.0.33
  ```
- Launch the application
  ```bash
  java -jar <full-path-project-root-directory>\target\wishlists-0.0.1-jar-with-dependencies.jar
  ```
  
## Scope
**Wishlists App** is a small Java application for wishlist management, created in a learning context focused on **TDD, build automation, quality checks, and CI**. The project started as a **Swing desktop application** backed by **MySQL** and built with **Maven**, and has since been extended to include a **REST API layer built with Javalin**, enabling HTTP-based access to the same business logic. The REST entry point is designed to be **container-friendly** and deployable on **Docker and Kubernetes**.

---

## Core stack
- **Java**
- **Maven**
- **Swing** for the desktop UI
- **Hibernate / JPA** for persistence
- **MySQL** as the relational database
- **JUnit, Mockito, AssertJ, and Cucumber** for testing at different levels
- **Log4j2** for logging

---

## General structure
```text
wishlists/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── api/
│   │   │   ├── app/
│   │   │   ├── businesslogic/
│   │   │   ├── daos/
│   │   │   ├── model/
│   │   │   ├── utils/
│   │   │   └── view/
│   │   └── resources/
│   ├── test/
│   │   ├── java/
│   │   └── resources/
│   ├── it/
│   │   └── java/
│   ├── e2e/
│   │   └── java/
│   └── bdd/
│       ├── java/
│       └── resources/
```

### Didactic note
This layout follows a very useful convention:
- `main` contains **production code**
- `test` contains **unit tests**
- `it` contains **integration tests**
- `e2e` contains **end-to-end tests**
- `bdd` contains support for **BDD-style specifications**

Keeping these layers separate makes the intent of each test suite clearer and avoids mixing very different testing goals.

---

## Packages

### `api`
Contains the **REST API layer**, built on top of [Javalin](https://javalin.io/).

Sub-packages:
- `dto` — Data Transfer Objects used to serialize/deserialize HTTP request and response bodies
- `mapper` — mappers that convert between domain model objects and DTOs
- `router` — registers the HTTP routes on the Javalin instance
- `service` — service facade consumed by the router to execute business operations

---

### `app`
Contains the **application entry point**.

Responsibilities:
- application bootstrap
- initial startup logic
- first wiring of the main components

---

### `businesslogic`
Contains the core application behavior.

Responsibilities:
- coordinate wishlist operations
- mediate between UI and persistence
- enforce application rules

---

### `daos`
Contains the data access layer.

Responsibilities:
- encapsulate database access
- centralize CRUD and persistence logic
- isolate business logic from JPA/Hibernate details

---

### `model`
Contains the domain model.

Responsibilities:
- represent the domain objects
- define the data handled by the application
- serve as the base for ORM persistence

---

### `view`
Contains the Swing user interface.

Responsibilities:
- render screens and dialogs
- collect user input
- delegate actions to the business layer

---

### `utils`
Contains shared support code.

Responsibilities:
- host small utilities or support annotations
- isolate cross-cutting technical details from the core domain

---

## Resources

### `src/main/resources`
Contains runtime resources.

Present elements:
- `META-INF/`
- `log4j2.xml`

Typical role:
- JPA configuration through `persistence.xml` inside `META-INF`
- logging configuration

---

## Environment Variables

The REST entry point (`WishlistRestApp`) supports the following environment variables, which take precedence over the corresponding CLI arguments and `persistence.xml` defaults. This makes the application straightforward to configure in **Docker** and **Kubernetes** environments without rebuilding the image.

| Variable      | Description                                                                 | Default (persistence.xml)                              |
|---------------|-----------------------------------------------------------------------------|--------------------------------------------------------|
| `PORT`        | HTTP port the Javalin server listens on                                     | `8080`                                                 |
| `DB_URL`      | Full JDBC URL of the MySQL instance (e.g. `jdbc:mysql://mysql-svc:3306/wishlists`) | `jdbc:mysql://localhost:3309/wishlists-schema` |
| `DB_USER`     | Database username                                                           | `java-client`                                          |
| `DB_PASSWORD` | Database password                                                           | `password`                                             |

Example (Docker):
```bash
docker run \
  -e PORT=8080 \
  -e DB_URL=jdbc:mysql://mysql-svc:3306/wishlists \
  -e DB_USER=java-client \
  -e DB_PASSWORD=secret \
  wishlists-rest:latest
```

Example (Kubernetes `env` block in a Pod spec):
```yaml
env:
  - name: PORT
    value: "8080"
  - name: DB_URL
    value: "jdbc:mysql://mysql-svc:3306/wishlists"
  - name: DB_USER
    valueFrom:
      secretKeyRef:
        name: db-secret
        key: username
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: db-secret
        key: password
```

---

## Testing structure

### Technologies
Technologies used in this layer:
- **JUnit** for test execution and lifecycle
- **Mockito** for mocks and isolation
- **AssertJ** for more expressive assertions
- **AssertJ Swing** for UI tests

### `src/test/java` — unit tests
This area contains the fastest and most isolated tests.

### Didactic note
Unit tests verify a single class or a very small collaboration in isolation. They provide fast feedback and are the natural foundation for TDD.

---

### `src/it/java` — integration tests
This area contains tests that verify the collaboration between multiple components.

Typical focus:
- interaction between business logic and persistence
- configuration correctness
- database-related behavior

### Didactic note
Integration tests are slower than unit tests, but they validate whether individually correct parts also work together correctly.

---

### `src/e2e/java` — end-to-end tests
This area contains full-flow tests that exercise the system from a user-oriented perspective.

Typical focus:
- application startup
- GUI interaction flows
- complete business scenarios

### Didactic note
End-to-end tests provide the highest confidence on real behavior, but they are also the most expensive to maintain. In a learning project, they are useful because they show the difference between “code works in isolation” and “the system works as a whole”.

---

### `src/bdd` — behavior-driven specifications
This area supports **BDD-style** scenarios.

Technologies used:
- **Cucumber** for executable specifications
- feature files written in a natural-language style
- Java step definitions that bind the scenarios to code

### Didactic note
BDD helps describe software behavior in a form that is more readable than technical test code alone. It is especially valuable because it shows the link between requirements, acceptance criteria, and automated verification.

---

## Testing and quality tooling

### JUnit
JUnit is the baseline test framework used to run automated tests and organize test classes, methods, and lifecycle hooks.

Why it matters here:
- it provides the standard foundation for automated verification
- it integrates naturally with Maven and CI
- it supports the other testing layers built on top of it

### Mockito
Mockito is used to replace real collaborators with controlled test doubles.

Why it matters here:
- it helps isolate business logic
- it reduces the need for heavy infrastructure in unit tests
- it makes interaction-based tests easier to write

### AssertJ
AssertJ provides a fluent assertion style.

Why it matters here:
- assertions become more readable
- failure messages are often clearer
- tests become easier to understand for readers of the repository

### Swing UI testing

The Swing user interface is tested with **AssertJ Swing**, a library designed for functional testing of desktop GUIs. It allows tests to interact with windows and widgets in a way that is close to real user behavior, for example by clicking buttons, typing into text fields, and checking component state. In practice, this makes GUI tests more readable and more stable than low-level event-based checks.

### Cucumber
Cucumber supports **Behavior-Driven Development (BDD)** by turning executable scenarios into living specifications. In practice, feature files describe behavior in a human-readable way, while step definitions bind those steps to Java code. The project includes `cucumber-java` and `cucumber-junit` as test dependencies.

Why it matters here:
- it connects requirements to executable checks
- it makes acceptance behavior visible even to non-developers
- it is useful in a didactic portfolio because it shows testing beyond classic unit tests

### JaCoCo
JaCoCo is the code coverage tool used in the Maven build. In this project it is also exposed through a dedicated Maven profile, which makes it easy to generate coverage reports during CI. 

Why it matters here:
- it shows which parts of the code are exercised by tests
- it helps identify weakly tested areas
- it gives a measurable signal, even though coverage alone is never enough to prove quality

### PIT / PITEST
PIT performs **mutation testing**. Instead of only measuring whether code is executed, it changes the code in small ways and checks whether the test suite detects the changes. The project includes the `pitest-maven` plugin and a dedicated `pit` Maven profile that runs mutation coverage during the `verify` phase. 

Why it matters here:
- it is stricter than plain coverage
- it reveals weak assertions and superficial tests
- it is very useful in a TDD-oriented project because it measures the real fault-detection power of the test suite

### Coveralls
Coveralls is used to publish coverage information generated by the Maven build. The project includes the `coveralls-maven-plugin` and a CI workflow that runs the `coveralls` Maven profile.

Why it matters here:
- it makes coverage trends visible from outside the repository
- it gives quick feedback through badges and hosted reports

### SonarCloud
SonarCloud is used for code quality analysis. The Maven configuration contains Sonar properties and exclusions, and the CI workflow runs the Sonar Maven scanner during verification. 

Why it matters here:
- it complements tests with static analysis
- it helps surface maintainability and code-smell issues

---

## Build and automation
The project uses **Maven** not only to compile the code, but also to orchestrate different verification layers.

Relevant aspects:
- executable jar packaging
- separate handling of unit, integration, end-to-end, and BDD tests
- test and quality reporting
- coverage and mutation testing support
- Docker support for the database

---

## GitHub Actions
The repository includes multiple GitHub Actions workflows dedicated to build portability and code quality. The workflow directory currently contains `maven_build_linux.yml`, `maven_build_mac_windows.yml`, `coveralls_sonar.yml`, and `pit.yml`. 

### `maven_build_linux.yml`
This workflow runs on pull requests on `ubuntu-latest`, tests both Java 11 and Java 17, caches Maven dependencies, and executes `xvfb-run mvn verify` in the `wishlists` directory. It also uploads failed GUI test artifacts when a build fails. `xvfb-run` is particularly relevant because Swing tests may need a virtual display in a headless CI environment. 

### `maven_build_mac_windows.yml`
This workflow runs on pull requests across a matrix of Java 11/17 and `macos-latest` / `windows-latest`. It executes `mvn test`, again caching Maven dependencies and archiving failed GUI test artifacts on failure. Its main purpose is portability: it checks that the project behaves consistently across operating systems and JDK versions. 

### `coveralls_sonar.yml`
This workflow runs on both push and pull request events on Ubuntu with JDK 17. It caches Sonar and Maven dependencies, executes `xvfb-run mvn verify -Pcoveralls` together with the Sonar Maven scanner, then generates and archives a Surefire report site. This workflow is the main CI entry point for coverage publishing and static quality analysis. 

### `pit.yml`
This workflow runs on push and pull request events on Ubuntu with JDK 11, executes `xvfb-run mvn verify -Ppit`, and archives the PIT mutation reports as artifacts. Its purpose is to keep mutation testing part of the automated quality loop rather than a one-off local activity.
