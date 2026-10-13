# snippet-engine

The execution worker of the [Snippet Searcher platform](https://github.com/JVM-austral). It parses, runs, tests, lints
and formats **PrintScript** snippets, using the team's own
[interpreter](https://github.com/JVM-austral/tp-ingsis-interpreter) as a library (published to GitHub Packages).

## Two ways in

**Synchronous (HTTP)**: the [snippet-manager](https://github.com/JVM-austral/snippet-manager) calls it when a user
is waiting for the answer:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/engine/parse` | Validate syntax and report errors |
| `POST` | `/engine/execute` | Run a snippet with the given inputs and return its output |
| `POST` | `/engine/test` | Run a snippet's test cases (inputs → expected outputs) |
| `POST` | `/engine/analyze` | Lint with the user's rules |
| `POST` | `/engine/format` | Format with the user's rules |

**Asynchronous (Redis Streams)**: when a user changes their lint or format rules, the manager publishes one event
per snippet. The engine consumes them through a consumer group, fetches each snippet's source from blob storage,
applies the new rules and reports the result back (`COMPLIANT` / `NON_COMPLIANT`). That keeps a bulk re-check of
every snippet a user owns out of their request.

Lint and format rules are configurable per user and per PrintScript version.

## Stack

Kotlin · Spring Boot 3 · Spring Data Redis (reactive) · PostgreSQL · Auth0 (JWT validation) · Docker

## Quality

- Unit and integration tests. The build fails below **80 % coverage** (JaCoCo) or on any **ktlint** violation.
- CI/CD on GitHub Actions: build and test, push the image to GHCR, deploy to the dev or prod VM over SSH.

## Run it locally

Requires read access to GitHub Packages, to download the interpreter.

```bash
cp .env-example .env
docker compose up --build
```

To run the whole platform, use the `docker-compose.yml` in
[`snippet-manager`](https://github.com/JVM-austral/snippet-manager).

Tests:

```bash
./gradlew check
```
