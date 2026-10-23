# Code Coverage: UI Automation Engine

This project uses JaCoCo to measure and improve test coverage.

## 📊 Current Status
- **`com.sehmi.engine.conditions`**: ~86% coverage.
- **`com.sehmi.engine.actions`**: ~29% coverage.
- **Lint Rules**: Coverage enabled in `:engine-lint`.

## 🚀 How to Run

### 1. Engine Coverage (Unit + Instrumented)
Requires an emulator or device connected.

```bash
./gradlew :engine:jacocoTestReport
```
Report generated at: `engine/build/reports/jacoco/jacocoTestReport/html/index.html`

### 2. Lint Coverage (JVM only)
```bash
./gradlew :engine-lint:jacocoTestReport
```
Report generated at: `engine-lint/build/reports/jacoco/test/html/index.html`

---

## 🛠 Configuration Details

### Exclusions
The following are excluded from coverage reports to avoid noise:
- Android generated classes (`R`, `BuildConfig`, `Manifest`)
- Hilt generated classes
- Classes containing `Test` in their name

### Technical Strategy
- **Merged Reports**: The `:engine` report merges coverage from standard unit tests (JVM) and connected instrumented tests (Device).
- **Dedicated Task**: `jacocoTestReport` is configured to find class files in AGP intermediates and coverage data in `outputs/`.
