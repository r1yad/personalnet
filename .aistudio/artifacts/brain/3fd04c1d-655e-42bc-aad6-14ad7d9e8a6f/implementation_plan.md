# Automatic Android APK Compilation via GitHub Actions

This plan details the configuration and workflow setup to enable automated native Android APK compilation using GitHub Actions on every push to the `main` branch, pull requests, and manual triggers (`workflow_dispatch`).

### User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Configuration Choices**:
> - **CI Triggers**: The GitHub Actions workflow will trigger on pushes to `main`, pull requests targeting `main`, and manual execution via `workflow_dispatch`.
> - **Build Target & Output**: The workflow will compile the project via `./gradlew assembleDebug`, upload the generated APK from `app/build/outputs/apk/debug/` as a downloadable GitHub Actions artifact, and require no external paid services or sensitive secrets.
> - **CI Environment Resilience**: The workflow will automatically provide a fallback debug keystore generation step and `.env` fallback so that any clean clone in GitHub Actions runner builds reliably out-of-the-box.

---

### 1. Overview & Core Concept

- **What It Does**: Provides a production-ready CI pipeline that checks out the codebase, sets up the required JDK, verifies the Android SDK components, builds the native Android application into an installable APK using Gradle, and publishes the debug APK as a downloadable artifact.
- **Key Value**: Enables automated, reliable build verification and instant distribution of test APKs on GitHub without requiring manual local build machines or cloud signing infrastructure.

---

### 2. User Experience & CI Workflow

#### Workflow Lifecycle (`.github/workflows/android.yml`)
1. **Trigger**: Push to `main`, Pull Request to `main`, or Manual Dispatch (`workflow_dispatch`).
2. **Environment Setup**:
   - Runner: `ubuntu-latest`.
   - Java: Setup Temurin JDK 17 with Gradle cache enabled (`actions/setup-java@v4`).
   - Android SDK: Pre-installed in `ubuntu-latest` runner with Android SDK Build-Tools and Platform 36.
3. **Pre-Build Preparation**:
   - Make `./gradlew` executable (`chmod +x gradlew`).
   - Copy `.env.example` to `.env` if missing so Secrets Gradle plugin resolves without errors.
   - Generate standard `debug.keystore` in project root if not tracked by git, ensuring `debugConfig` signing succeeds.
4. **Build & Test**:
   - Execute `./gradlew assembleDebug` (plus optional unit test verification).
5. **Artifact Publishing**:
   - Upload APK located at `app/build/outputs/apk/debug/app-debug.apk` using `actions/upload-artifact@v4` with retention period.

---

### 3. Key Technical Decisions & Trade-Offs

- **Decision 1: Full Gradle Wrapper Generation (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`)**
  - *Chosen Approach*: Generate the missing Gradle wrapper binaries and scripts matching `gradle-wrapper.properties` (Gradle 9.3.1) so that GitHub Actions runners execute `./gradlew assembleDebug` seamlessly without depending on system Gradle.
  - *Why*: Standard GitHub Actions workflows require `./gradlew`.
- **Decision 2: Debug Keystore Generation in CI**
  - *Chosen Approach*: Add a step in `android.yml` to generate `debug.keystore` using `keytool` if it is not present in root.
  - *Why*: `debug.keystore` is ignored by `.gitignore` and must never be hardcoded or checked into public repositories, yet `app/build.gradle.kts` references `storeFile = file("${rootDir}/debug.keystore")`.
- **Decision 3: Secrets Plugin & Google Services Fallback**
  - *Chosen Approach*: Ensure `missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN` remains active and copy `.env.example` to `.env` during workflow run.
  - *Why*: Guarantees that public GitHub repositories compile without failing on private Google Services or secrets files.

---

### 4. Technical Architecture & Component Flow

```
┌────────────────────────────────────────────────────────┐
│                   GitHub Repository                    │
│   Push to 'main' / Pull Request / workflow_dispatch    │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│         GitHub Actions Runner (ubuntu-latest)          │
├────────────────────────────────────────────────────────┤
│ 1. actions/checkout@v4                                 │
│ 2. actions/setup-java@v4 (JDK 17 + Gradle Cache)       │
│ 3. chmod +x ./gradlew                                  │
│ 4. Environment Fallbacks (.env & debug.keystore check) │
│ 5. ./gradlew assembleDebug                             │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    Build Outputs                       │
│ - app/build/outputs/apk/debug/app-debug.apk            │
│ - actions/upload-artifact@v4                           │
│   Artifact: "personanet-debug-apk"                     │
└────────────────────────────────────────────────────────┘
```
