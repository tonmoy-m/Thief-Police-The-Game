<table>
  <tr>
    <td width="120" valign="middle">
      <img src="https://github.com/user-attachments/assets/c4375d7a-1214-448c-bd83-6d0b89a88e97" width="96" height="96" alt="Thief-Police icon">
    </td>
    <td valign="middle">
      <h1>Thief‑Police — The Game</h1>
      <p>Android game built with Jetpack Compose. This repository contains the complete version of the game developed in this project: token selection, role assignment and reveal, Babu instructions, Police guessing, scoring, and round progression.</p>
    </td>
  </tr>
</table>

## Table of contents
- [Demo](#demo)
- [Features](#features)
- [Wiki](https://github.com/tonmoy-m/Thief-Police-The-Game/wiki)
- [Screenshot](#screenshot)
- [Requirements](#requirements)
- [Run (Android Studio)](#run-android-studio)
- [Build (command line)](#build-command-line)
- [Release (signed APK/AAB)](#release-publish)
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

## Wiki

Detailed play instructions, scoring logic, examples, and FAQs are available on the project wiki:  
https://github.com/tonmoy-m/Thief-Police-The-Game/wiki

Quick links:
- How to Play: https://github.com/tonmoy-m/Thief-Police-The-Game/wiki/How%20to%20Play
- Scoring: https://github.com/tonmoy-m/Thief-Police-The-Game/wiki/Scoring

## Screenshot

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/b242c7d0-87b0-459c-9ac9-07e3af9dc27c" width="240" alt="Screenshot 1" /></td>
    <td><img src="https://github.com/user-attachments/assets/a1f7a181-a139-46e4-9175-b7852db071a3" width="240" alt="Screenshot 2" /></td>
    <td><img src="https://github.com/user-attachments/assets/74d33903-0914-40a6-b4ba-10c82005c885" width="240" alt="Screenshot 3" /></td>
    <td><img src="https://github.com/user-attachments/assets/0bbd4cd0-1b4e-49a0-ac0e-ba3bd74d312c" width="240" alt="Screenshot 4" /></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/2c476fbc-9052-49d2-b351-b6be0e0b26c1" width="240" alt="Screenshot 5" /></td>
    <td><img src="https://github.com/user-attachments/assets/d8ce8c37-0a41-451d-a4ad-9b7d6d1f8d58" width="240" alt="Screenshot 6" /></td>
    <td><img src="https://github.com/user-attachments/assets/f90f7b3d-9e47-4ff8-9d04-19248889caa9" width="240" alt="Screenshot 7" /></td>
    <td></td>
  </tr>
</table>

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


## Contact
GitHub: [@tonmoy-m](https://github.com/tonmoy-m)
