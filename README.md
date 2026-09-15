# Gatoryap

A social platform. Kotlin Multiplatform clients (Android, iOS) sharing a Compose
Multiplatform UI, backed by a Ktor server and Postgres.

## Layout

```
core/              models, validation — shared by server and clients
app/
  composeApp/      shared Compose Multiplatform UI and client logic
  androidApp/      Android entry point
  iosApp/          iOS entry point (Xcode project)
server/            Ktor API, Exposed, Flyway migrations
```

This follows the
[recommended KMP project structure](https://kotlinlang.org/docs/multiplatform/multiplatform-project-recommended-structure.html)
for projects that share code between a server and its clients. `androidApp` is a
separate module from `composeApp` because AGP 9 requires Android app entry points
to be separated from multiplatform code.

## Licensing

Per-directory, not per-repository:

| Directory | License    |
| --------- | ---------- |
| `server/` | AGPL-3.0   |
| `core/`   | Apache-2.0 |
| `app/`    | Apache-2.0 |

The server is copyleft so a competitor cannot run a modified closed fork as a
service. The client modules are permissive because they are linked into App Store
binaries, whose terms conflict with GPL-family licenses. See [LICENSE](LICENSE)
for the full rationale, and [CONTRIBUTING.md](CONTRIBUTING.md) before submitting
changes.

"Gatoryap" and its branding are trademarks and are not covered by either license.

## Requirements

- A JDK — any recent version; Gradle downloads the pinned JDK 21 toolchain itself
- Docker
- [`just`](https://github.com/casey/just) — `brew install just`
- For iOS: Xcode, and [XcodeGen](https://github.com/yonaskolb/XcodeGen) — `brew install xcodegen`

## Running

```sh
just run          # Postgres + server on http://localhost:8080
just health       # check /healthz and /readyz
```

Migrations run automatically on server boot. Postgres listens on **5433** to
avoid colliding with other local instances.

```sh
just build        # compile everything
just test         # run tests
just android      # build the Android APK
just ios          # build the iOS app
just ios-open     # open the iOS project in Xcode
just db-shell     # psql shell
just db-reset     # wipe local database
```

`just --list` shows everything.

## Configuration

The server reads its configuration from the environment and runs with working
defaults if none is set. See [.env.example](.env.example).

`DATABASE_URL` is a plain JDBC URL — the server is agnostic about the Postgres
provider. Local development uses the Docker Compose instance; production points
at any managed Postgres.

## Dependency versions

Everything is pinned in `gradle/libs.versions.toml`.

Kotlin, AGP, and Gradle are **mutually constrained**. Check the
[KMP compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
before bumping any of them — the newest AGP release is usually ahead of what the
current Kotlin supports, and mismatches fail in ways that look unrelated.

Current set: Kotlin 2.4.20, AGP 9.3.1, Gradle 9.7.0, Compose Multiplatform 1.9.3.

## iOS project

`app/iosApp/iosApp.xcodeproj` is generated from `project.yml` by XcodeGen and is
not committed. Run `just ios-project` after cloning or after changing the spec.

The Xcode build invokes `:app:composeApp:embedAndSignAppleFrameworkForXcode` via
a pre-build script, using
[direct integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html),
so the Kotlin framework is rebuilt and signed on every Xcode build.
