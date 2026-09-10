# Release Notes - 0.3.0-alpha

## [0.3.0-alpha] - 2026-11-06

This release introduces significant performance optimizations, a refined logging architecture, and critical fixes for multi-module publication. The engine is now faster, quieter, and more robust.

### 🚀 Performance Optimizations
- **Nested Robustness Detection**: Implemented a context-aware mechanism in `runRobustly`. The engine now detects when it is already running within a robust block. 
    - **Impact**: Composite actions like `scrollAndClick` or `replaceText` now skip redundant "wait for idle" cycles. Instead of waiting multiple times, the engine synchronizes once at the entry point, significantly reducing execution time for complex steps.
- **Visibility-Aware Scrolling**: Updated all core assertions and actions (including `clickOnTag`, `enterText`, and `assertTagDisplayed`) to perform a visibility check *before* attempting to scroll.
    - **Impact**: If a target element is already in the viewport, the engine skips the expensive `performScrollTo()` operation entirely, making transitions across already-visible UI elements near-instant.
- **Granular Wait Polling**: Refactored `waitUntil` to use a more responsive internal polling frequency (50ms) while still respecting user-defined `pollIntervalMillis`.
    - **Impact**: Tests proceed immediately once a condition is met, eliminating the "over-sleep" overhead present in previous versions.

### 📊 Logging & Diagnostics
- **Configurable Verbosity**: Introduced `verboseLogging` in `UiEngine.Configuration`.
    - **Impact**: Users can now silence the "Starting/Completed" step logs for a cleaner Logcat during successful test runs. Diagnostic captures (screenshots/tree dumps) remain fully operational for failures.
- **Zero-Overhead Logging**: All internal logging utilities are now `inline` functions. 
    - **Impact**: When logging is disabled, the code is effectively removed at compile-time, ensuring there is zero runtime performance penalty for log-level checks.
- **Chunked Semantics Tree Dumps**: Improved `printUnmergedTree` to handle extremely large UI hierarchies without hitting Logcat's 4KB per-line truncation limit.

### 🛠 Build & Publication
- **JitPack Multi-module Fix**: Added a dedicated `jitpack.yml` configuration.
    - **Impact**: Correctly resolves and exposes all flavored artifacts (`standard`, `hilt`) in multi-module Android projects, ensuring `robot-testing-engine` is always downloadable.
- **Unified Publication Coordinates**: Standardized all modules under the `com.github.rajbirsehmi.UI-Engine` group ID to match JitPack's resolution requirements.
- **Dokka Integration**: Fully integrated Dokka to generate HTML and Javadoc JARs for all project variants.
    - **Impact**: Consumers now receive rich Kotlin-aware documentation and source navigation directly within Android Studio.
- **Dependency Scope Refinement**: Promoted core test APIs to `api` while isolating internal implementations like `log4j-core` to `implementation`.
    - **Impact**: Cleaner classpath for host applications and reduced risk of version conflicts with logging libraries.

---

## [0.2.5-alpha] - 2026-11-06

### Fixed
- **Multi-module JitPack Publication**: Resolved critical issue where the main `robot-testing-engine` artifact was not downloadable from JitPack.
- **Unified Publication Scope**: Standardized `groupId` to `com.sehmi.engine` across all modules.

### Changed
- **Version Bump**: Updated all components to `0.2.5-alpha`.

## [0.2.4-alpha] - 2026-11-06

### Fixed
- **Hilt Dependency Scoping**: Fixed Hilt dependency scoping in the engine AAR by promoting Hilt libraries to `api`.
- **Artifact Resolution**: Resolved issues where `0.2.3-alpha` was not found.

### Changed
- **Publication Refactoring**: Refined publication details for better clarity.
- **Version Bump**: Updated all components to `0.2.4-alpha`.
