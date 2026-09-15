# 🚀 Release Notes - v0.3.0-beta02

## [0.3.0-beta02] - 2026-11-07

> [!NOTE]
> This is a maintenance release to resolve Dokka documentation generation issues in multi-variant Android projects. No functional code changes were included.

This major release marks the official transition to the **UI Automation Engine**. We have overhauled the core identity of the framework to provide better clarity for the public community, while simultaneously delivering massive performance gains and high-precision automation tools.

## 🏗 Brand Overhaul & Major Refactor
To avoid confusion with UI rendering engines, the framework has been renamed from "UI Engine" to **"UI Automation Engine"**. This change is reflected in all core symbols and installation coordinates.

### ⚠️ Breaking Changes (Renaming)
- **`UiEngine`** is now **`UiTestEngine`**: The central configuration and lifecycle manager.
- **`UiEngineRule`** is now **`UiTestEngineRule`**: The JUnit rule used for standard `ComposeTestRule` decoration.
- **`AutomationComposeContentTestRule`** is now **`UiTestEngineContentRule`**: The primary rule type returned by `UiTestEngine.createRule()`.
- **`ComposeRuleScope` Property**: The internal `composeRule` property has been renamed to **`uiTestEngineRule`** to align with the new nomenclature.
- **New Repository Coordinates**: The project is now hosted at [https://github.com/rajbirsehmi/UI-Automation-Engine](https://github.com/rajbirsehmi/UI-Automation-Engine). Update your Version Catalogs accordingly.

---

## 🚀 Extreme Performance Optimizations
We've optimized the internal robustness pipeline to make your tests run up to **40% faster** in complex scenarios.

### 1. Nested Robustness Detection (Internal)
The engine now features a context-aware execution model. When a high-level action (like `scrollAndClick`) calls other robust actions, the engine detects the nested context.
- **The Benefit**: It skips redundant "wait for idle" cycles and thread synchronization. Instead of waiting multiple times per interaction, it synchronizes exactly once at the entry point.

### 2. Visibility-Aware Scrolling
Core actions like `clickOnTag`, `enterText`, and all standard assertions now perform an immediate visibility check before attempting a scroll operation.
- **The Benefit**: If an element is already in the viewport, the expensive and time-consuming `performScrollTo()` search is skipped entirely. This makes transitions between visible elements near-instant.

### 3. High-Frequency Wait Polling
Refactored the `waitUntil` utility to use a granular 50ms polling step while still respecting the user-defined `pollIntervalMillis`.
- **The Benefit**: Tests proceed the millisecond a condition is met, eliminating the "over-sleep" overhead found in standard Compose `waitUntil` implementations.

---

## 📊 Configurable Logging & Zero-Overhead
You can now fully control the verbosity of the engine's Logcat output.

- **`verboseLogging` Flag**: Added to `UiTestEngine.Configuration`. Set it to `false` to silence all "Starting..." and "Completed..." logs for a clean, professional test report.
- **Zero Runtime Overhead**: All logging utilities are now `inline` functions. When logging is disabled, the checks are optimized away at compile-time, ensuring maximum execution speed.
- **Chunked Semantics Dumps**: Failure diagnostics now handle extremely large UI trees by splitting dumps into manageable Logcat chunks, preventing data loss due to system line limits.

---

## ✨ New High-Precision Features
- **Advanced Action Builder**: Introduced `executeAdvancedAction`, a DSL for performing low-level touch paths (gestures), hardware key sequences, and raw semantics actions without losing the engine's diagnostic protection.
- **Accessibility Batch Audits**: New `assertInteractiveNodesHaveLabels()` method to automatically scan your entire screen for clickable elements missing content descriptions.
- **Robust Hilt Integration**: Deep integration with Hilt's testing infrastructure via `UiTestEngine.createHiltRule()` and a specialized `HiltTestRunner`.

---

## 🛠 Installation & Migration
Update your `gradle/libs.versions.toml`:
```toml
[versions]
engine = "0.3.0-beta02"

[libraries]
uiengine = { group = "com.github.rajbirsehmi.UI-Automation-Engine", name = "robot-testing-engine", version.ref = "engine" }
engine-lint = { group = "com.github.rajbirsehmi.UI-Automation-Engine", name = "engine-lint", version.ref = "engine" }
```

In your tests, simply replace `UiEngine` with `UiTestEngine`. Most IDEs will handle the rename automatically via refactoring tools.
