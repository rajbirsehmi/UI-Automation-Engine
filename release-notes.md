# 🚀 Release Notes - v0.3.0-beta03

## [0.3.0-beta03] - 2026-11-14

### ✨ New High-Precision Features
- **Enhanced Permission Handling**: `handlePermissionDialog` now supports 3-way permission dialogs ("While using the app", "Only this time", and "Don't Allow") using the new `PermissionAction` enum.
- **Programmatic Permission Bypass**: Added `UiTestEngine.enablePermission("PERMISSION")` to directly grant runtime permissions programmatically using `UiAutomation` without clicking through any UI dialogs.
- **Robust Regex Matching**: Improved fuzzy text matching for system dialogs to handle variations across Android OS versions and locales more reliably.

### 🛠 Internal Refinements
- **Enriched Failure Diagnostics**: Automation failures now include a `sendStatus` report to the instrumentation runner, facilitating easier artifact discovery in CI environments.
- **Soft Scrolling**: `scrollToTag` and `scrollToText` now gracefully handle nodes that are already visible or not within a scrollable container, preventing unnecessary `AssertionError` triggers.

---