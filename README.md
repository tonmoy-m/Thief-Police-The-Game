# Thief‑Police — The Game

A full Android game built with Jetpack Compose. This repository contains the complete version of the game developed in this project: token selection, role assignment and reveal, Babu instructions, Police guessing, scoring, and round progression.

## Table of contents
- [Demo](#demo)
- [Features](#features)
- [Screenshot](#screenshot)
- [Requirements](#requirements)
- [Run (Android Studio)](#run-android-studio)
- [Build (command line)](#build-command-line)
- [Release (signed APK/AAB)](#release-signed-apkaab)
- [Changelog](#changelog)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Demo
Walkthrough:
1. Player selects a token (seat).
2. Roles are randomly assigned to tokens.
3. Role reveal screen shows roles for tokens; Thief/Dacoit are briefly revealed then hidden.
4. Babu receives instructions (if applicable).
5. Police players get a chance to guess.
6. Scoring is calculated and rounds progress.

## Features
- Jetpack Compose UI with responsive components
- Full game flow:
  - Token selection
  - Role assignment/reveal (with hide/reveal timing for hidden roles)
  - Babu instructions, Police guess flow
  - Scoring and round progression
- Local CPU players with deterministic/randomized behavior
- Clean separation of token ↔ role mapping for testability
- Demo/test mode (quick flow) and full game mode

## Screenshot


## Requirements
- Android Studio Flamingo (or latest stable)
- JDK 11 or 17
- Android SDK (installed via Android Studio)
- Gradle wrapper (included in repo)

## Run (Android Studio)
1. Open the project in Android Studio.
2. Allow Gradle sync and dependency downloads.
3. Select a device/emulator and run the `app` module.

## Build (command line)
From the project root:

- Debug APK:
  - Windows: `./gradlew.bat assembleDebug`
  - macOS/Linux: `./gradlew assembleDebug`
  - Output: `app/build/outputs/apk/debug/app-debug.apk`

- Release AAB / APK (signed):
  1. Generate/obtain a keystore (do NOT commit it).
  2. Configure signing in `app/build.gradle.kts` and put passwords in `~/.gradle/gradle.properties` (or an ignored project `gradle.properties`).
  3. Build:
     - APK: `./gradlew assembleRelease`
     - AAB: `./gradlew bundleRelease`
  4. Outputs:
     - APK: `app/build/outputs/apk/release/app-release.apk`
     - AAB: `app/build/outputs/bundle/release/app-release.aab`

## Release (publish)
- Manual: build a signed APK/AAB locally, then use GitHub → Releases → Draft new release, attach APK/AAB, publish.
- Quick tag locally then create release:
  ```bash
  git tag -a v1.0.0 -m "Release v1.0.0"
  git push origin v1.0.0
  ```
  Then create a release on GitHub and attach the artifact.
- If you want automation later, I can add a GitHub Actions workflow that builds on tag pushes and uploads the artifact to the release (keystore and passwords as secrets).

## Changelog
- v1.0.0 — Full game: token selection, role reveal, Babu & Police flows, scoring, round progression.
- (Add future versions here)

## Contributing
- Bug reports and PRs welcome.
- Open an issue with steps to reproduce or propose changes via PR.
- Keep secrets and keystores out of the repo.

## License
This project is licensed under the MIT License — see `LICENSE`.

## Contact
GitHub: [@tonmoy-m](https://github.com/tonmoy-m)
