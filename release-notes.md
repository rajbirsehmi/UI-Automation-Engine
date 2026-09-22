# 🚀 Release Notes - v0.3.0-rc03_1

## [0.3.0-rc03_1]

### 🔄 Maintenance
- **Version Bump**: Update version number bump with no code changes.

## [0.3.0-rc03]

### 🔄 Maintenance
- **Version Bump**: Update version number bump with no code changes.

## [0.3.0-rc02] - 2026-11-23

### 🔄 Maintenance
- **Version Bump**: Update version number bump with no code changes.

# 🚀 Release Notes - v0.3.0-rc01

## [0.3.0-rc01] - 2026-11-22

### 🏗 Dedicated Compose Architecture
- **Pure Compose Focus**: Removed the legacy Espresso layer to reduce library footprint and focus strictly on high-performance Jetpack Compose automation.
- **Enhanced Robot Scope**: Expanded `ComposeRuleScope` with deep hierarchy navigation and specialized semantics matchers.

### ✨ Compose Power-ups
- **Hierarchy Finders**: Added `clickFirstChild`, `clickLastChild`, and `clickChildAtIndex` to target nodes by position.
- **Relationship Matchers**: Introduced `hasAnyDescendant`, `hasAnyAncestor`, and `hasAnySibling` for contextual element discovery.
- **State Matchers**: Added first-class support for `isDialog`, `isPopup`, `isHeading`, and `hasStateDescription`.
- **Advanced Gestures**: Implemented `mouseClick` (simulating left/right/center clicks for desktop/large-screen tests) and `rotaryScroll` (first-class support for Wear OS bezel/crown input).
- **Specialized Wait Conditions**: Added robust polling for node states: `waitUntilExists`, `waitUntilDoesNotExist`, and `waitUntilNodeCount`.

### 📱 System & Device Control
- **`onDevice { ... }` Scope**: New DSL block for hardware and OS-level interactions using UI Automator.
- **Hardware Keys**: Robust simulation for `pressHome`, `pressBack`, `pressRecentApps`, and `pressDPadCenter`.
- **Device Management**: Added `wakeUp`, `sleep`, `unlock` (keyguard dismissal), and `setOrientation` controls.
- **Shell Command Execution**: Exposed `executeShell(command)` for high-power system interactions within the robot pattern.
- **Notification Shade Control**: Added `clearNotifications` and improved shade expansion reliability.

### 🛡 Robustness & Interop
- **Auto-Interop**: Introduced `testTagsAsResourceId` configuration to automatically expose Compose test tags to UI Automator, facilitating cross-backend interactions.
- **Enhanced Lint Rules**: `DirectUiTestApiUsage` now guards against direct UI Automator usage in tests, ensuring all system interactions benefit from the engine's diagnostic pipeline.

# 🚀 Release Notes - v0.3.0-beta06

## [0.3.0-beta06] - 2026-11-20

### 🛡️ Obfuscation & Security
- **R8 Integration**: Enabled code shrinking and obfuscation for the library's release build.
- **Consumer ProGuard Rules**: Bundled `consumer-proguard-rules.pro` into the AAR. This ensures library consumers' builds automatically preserve critical entry points (Public APIs, Compose functions, Hilt components) while safely obfuscating internal engine logic.

### 📦 Enhanced Publishing Workflow
- **Dokka Integration**: Configured Dokka to generate KDoc-based documentation.
- **New Publication Artifacts**: The Maven publication now includes `-sources.jar` and `-javadoc.jar` artifacts alongside the AAR.
- **Log4j2 Resilience**: Added ProGuard rules to ensure Log4j2 reflection-based plugin discovery works correctly in obfuscated environments.

# 🚀 Release Notes - v0.3.0-beta05

## [0.3.0-beta05] - 2026-11-16

### ✨ Global Diagnostic Capture
- **Universal Failure Monitoring**: Introduced `FailureDiagnosticWatcher`, a JUnit `TestWatcher` that automatically triggers diagnostics for *any* test failure. This ensures screenshots are captured even when using standard Compose assertions or when failures occur outside the engine's robust action pipeline.
- **Redundancy Prevention**: A new thread-local tracking system prevents multiple diagnostic captures for a single failure, keeping your artifacts directory clean and focused.

### 🛡️ Robust Artifact Storage
- **Multi-Path Fallbacks**: The engine now intelligently resolves storage locations for screenshots and logs. It iterates through prioritized candidates (config, instrumentation args, external cache, internal cache) until a writable directory is found.
- **Writability Validation**: Improved error handling and logging when storage directories are restricted or full.

### 💉 Hilt Reliability
- **Improved DI Lifecycle Sync**: The `HiltAutomationComposeTestRule` now integrates directly with the global diagnostic watcher, ensuring Hilt-based tests benefit from the same high-fidelity failure reports as standard tests.

## [0.3.0-beta04] - 2026-11-15

### ✨ New Diagnostic Capabilities
- **Logcat Tail Capture**: Automation failures now automatically capture the last 100 lines of Logcat and save them to a `.log` file (configurable).
- **View Hierarchy Dumps**: Optionally capture a full XML dump of the Android View hierarchy on failure (`autoCaptureViewHierarchy`), helping debug hybrid or complex layout issues.
- **Enriched Artifact Reports**: The `AssertionError` message now explicitly lists all generated artifacts (Screenshot, Logcat, and Hierarchy) with their file paths.

### ⚙️ Enhanced Configuration
- Added `autoCaptureLogcat` (Default: `true`) to `UiTestEngine.Configuration`.
- Added `autoCaptureViewHierarchy` (Default: `false`) to `UiTestEngine.Configuration`.
- Added `logcatTailLines` (Default: `100`) to customize the depth of log captures.

## [0.3.0-beta03] - 2026-11-14

### ✨ New High-Precision Features
- **Enhanced Permission Handling**: `handlePermissionDialog` now supports 3-way permission dialogs ("While using the app", "Only this time", and "Don't Allow") using the new `PermissionAction` enum.
- **Programmatic Permission Bypass**: Added `UiTestEngine.enablePermission("PERMISSION")` to directly grant runtime permissions programmatically using `UiAutomation` without clicking through any UI dialogs.
- **Robust Regex Matching**: Improved fuzzy text matching for system dialogs to handle variations across Android OS versions and locales more reliably.

### 🛠 Internal Refinements
- **Enriched Failure Diagnostics**: Automation failures now include a `sendStatus` report to the instrumentation runner, facilitating easier artifact discovery in CI environments.
- **Soft Scrolling**: `scrollToTag` and `scrollToText` now gracefully handle nodes that are already visible or not within a scrollable container, preventing unnecessary `AssertionError` triggers.

---
