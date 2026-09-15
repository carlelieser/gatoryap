# Gatoryap

A social platform. Kotlin Multiplatform clients (Android, iOS) sharing a Compose
Multiplatform UI, backed by a Ktor server and Postgres.

```
core/    models and validation, shared by server and clients
app/     composeApp (shared UI), androidApp, iosApp
server/  Ktor API, Exposed, Flyway
```

## Getting started

Needs a JDK, Docker, and [`just`](https://github.com/casey/just). iOS work also
needs Xcode and [XcodeGen](https://github.com/yonaskolb/XcodeGen).

```sh
just run          # Postgres + server on :8080
just ios-project  # generate the Xcode project (first time only)
just --list       # everything else
```

Migrations run on server boot. Postgres listens on 5433.

Licensing is per-directory: see [LICENSE](LICENSE), and
[CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.
