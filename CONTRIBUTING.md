# Contributing to Gatoryap

## Contributor License Agreement

Every contribution requires a signed CLA before it can be merged. See [CLA.md](CLA.md).

This is not boilerplate. Gatoryap ships to the Apple App Store, whose terms
conflict with GPL-family licenses. The CLA grants the project the relicensing
rights needed to keep distributing there. Without it, a single AGPL-licensed
contribution in a client module would make App Store distribution impossible.

## Licensing, by directory

Check which module you are editing before you start:

| Directory        | License      | Notes                                        |
| ---------------- | ------------ | -------------------------------------------- |
| `server/`        | AGPL-3.0     | Never linked into client binaries             |
| `core/`          | Apache-2.0   | Shared by server and clients                  |
| `app/`           | Apache-2.0   | Linked into App Store binaries                |

Every source file starts with an SPDX header matching its directory:

```kotlin
// SPDX-License-Identifier: Apache-2.0
```

**The boundary is load-bearing.** AGPL code must never be linked into `core/` or
`app/`. Clients talk to the server over HTTP only. A pull request that makes
`core/` or `app/` depend on `server/` will be rejected.

## Getting set up

Requirements: JDK (any recent version — Gradle provisions the right one),
Docker, and [`just`](https://github.com/casey/just). For iOS work you also need
Xcode and [XcodeGen](https://github.com/yonaskolb/XcodeGen).

```sh
just db-up      # start Postgres
just run        # run the server
just build      # compile everything
just test       # run tests
```

See the [README](README.md) for the full command list.

## Dependency versions

All versions live in `gradle/libs.versions.toml`, nowhere else. Read the comment
at the top of that file before bumping Kotlin, AGP, or Gradle.

## Pull requests

- One logical change per PR.
- `just build` and `just test` must pass.
- Touching `app/` or `core/`? Confirm the Android and iOS builds still work:
  `just android` and `just ios`.
- Match the style of the code around you.
