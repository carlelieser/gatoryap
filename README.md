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
just run     # Postgres + server on :8080
just --list  # everything else
```

Migrations run on server boot. Postgres listens on 5433.

## Notes

Licensing is per-directory — `server/` is AGPL-3.0, `core/` and `app/` are
Apache-2.0. See [LICENSE](LICENSE) for why, and [CONTRIBUTING.md](CONTRIBUTING.md)
before opening a pull request.

Kotlin, AGP, and Gradle versions are mutually constrained; `gradle/libs.versions.toml`
explains what to check before bumping them.

`app/iosApp/iosApp.xcodeproj` is generated — run `just ios-project` after cloning.
