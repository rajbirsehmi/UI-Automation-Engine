# UI Automation Engine: Technical Reference

This document provides an exhaustive reference for the `:engine` module, detailing every API, its purpose, and usage examples.

---

## Table of Contents
1. [Core Architecture](#core-architecture)
2. [Gestures & Interactions](#gestures--interactions)
3. [Text & Focus Actions](#text--focus-actions)
4. [Scrolling & Navigation](#scrolling--navigation)
5. [System Actions](#system-actions)
6. [Time & Clock Control](#time--clock-control)
7. [Assertions](#assertions)
8. [Advanced "Escape Hatches"](#advanced-escape-hatches)
9. [Hilt Integration](#hilt-integration)
10. [Diagnostics & Utilities](#diagnostics--utilities)
11. [Static Analysis (Lint Rules)](#static-analysis-lint-rules)

---

## Global Configuration

### `UiEngine.configure(Configuration)`
Allows global customization of the engine's behavior. Call this in a `@BeforeClass` or global test initializer.

#### Example Usage:
```kotlin
@BeforeClass
@JvmStatic
fun setupEngine() {
    UiEngine.configure(
        UiEngine.Configuration(
            defaultTimeoutMillis = 8000L,
            pollIntervalMillis = 150L,
            verboseLogging = false,
            autoCaptureScreenshots = true,
            autoDumpSemantics = true
        )
    )
}
```

| Property | Default | Description |
| :--- | :--- | :--- |
| `defaultTimeoutMillis` | `5000L` | Maximum time to wait for a robust action or assertion. |
| `pollIntervalMillis` | `100L` | Interval between polling attempts in `waitUntil`. |
| `autoCaptureScreenshots`| `true` | Automatically takes a screenshot on failure. |
| `autoDumpSemantics` | `true` | Automatically dumps the unmerged tree on failure. |
| `verboseLogging` | `true` | Enables high-level "Starting/Completed" logs for every step. |

---

## Core Architecture

### `UiEngine` (Singleton)
The central manager for the `ComposeTestRule`. Use this to avoid passing rules to every robot constructor.
*   **`setComposeRule(rule)`**: Manually sets the rule (for custom rule setups).
*   **`clearComposeRule()`**: Clears the rule.
*   **`withRobot(robot) { /* ... */ }`**: DSL entry point using the global rule.

### `UiEngine.createRule()` (Factory)
The easiest way to initialize the engine for standard tests. Returns a wrapped `ComposeContentTestRule` that handles all lifecycle registration.
*   **Usage**: `@get:Rule val rule = UiEngine.createRule()`

### `UiEngineRule` (JUnit Rule)
A decorator rule for existing `ComposeTestRule` instances.
*   **Usage**: `@get:Rule val engineRule = UiEngineRule(composeRule)`
*   **Benefit**: Ensures that `setComposeRule` and `clearComposeRule` are called at the correct times.

### `ComposeRuleScope` (Interface)
The foundation of the engine. Robots must implement this to gain access to all robust extension methods.
*   **Property**: `composeRule: ComposeTestRule`. Defaults to `UiEngine.composeRule`.

### `withRobot` (Extension)
The DSL entry point for executing blocks of code within a robot's scope.
*   **Usage**:
    ```kotlin
    // Option A: Centralized
    UiEngine.withRobot(MyRobot()) { /* robot logic */ }

    // Option B: Standard
    composeRule.withRobot(MyRobot()) { /* robot logic */ }
    ```

---

## Gestures & Interactions

All gesture actions are robust: they wait for idle, automatically scroll to the element, and perform retries.

| Method | Description |
| :--- | :--- |
| `clickOnTag(tag)` | Performs a robust click on a node with the specified test tag. |
| `clickOnText(text)` | Performs a robust click on a node containing the specified text. |
| `longClickTag(tag)` | Performs a long-press on a node by tag. |
| `longClickText(text)` | Performs a long-press on a node by text content. |
| `doubleClickTag(tag)` | Performs a double-tap on a node by tag. |
| `swipe(tag, direction)` | Swipes a specific node in a `Direction` (UP, DOWN, LEFT, RIGHT). |
| `dragAndDrop(src, target)` | Drags from one tag and drops onto another. |
| `pinchToZoom(tag, zoomIn)` | Performs a two-finger pinch gesture on a node. |
| `rotate(tag, degrees)` | Performs a two-finger rotation gesture around the node center. |
| `multiFingerSwipe(tag, fingers, direction)` | Performs a simultaneous swipe with 2-4 fingers. |
| `clickAtOffset(tag, x, y)` | Clicks at a percentage-based offset (0.0 to 1.0) within a node. |

---

## Text & Focus Actions

| Method | Description |
| :--- | :--- |
| `enterText(tag, text)` | Appends text to a field. |
| `replaceText(tag, text)` | Clears existing text and replaces it with new text. |
| `clearText(tag)` | Clears all text from a field. |
| `pressImeAction(tag)` | Triggers the Keyboard IME action (e.g., Search, Done, Go). |
| `requestFocus(tag)` | Programmatically requests focus for a specific node. |
| `performKeyInput(tag, key)` | Injects a physical hardware key event (e.g., `Key.Enter`). |

---

## Scrolling & Navigation

| Method | Description |
| :--- | :--- |
| `scrollToTag(tag)` | Scrolls the UI until the specified tag is in view. |
| `scrollToText(text)` | Scrolls the UI until the specified text is in view. |
| `scrollToIndex(tag, i)` | Scrolls a container (LazyColumn/Row) to a specific index. |
| `scrollToKey(tag, key)` | Scrolls a container to an item with a stable key. |
| `swipeUntilVisible(tag, dir)`| Repeatedly swipes in a direction until a node appears. |
| `scrollAndClick(tag)` | Convenience method to scroll to a tag and then click it. |

---

## System Actions

These actions use UIAutomator internally to interact with the Android OS outside the Compose bounds. They automatically handle synchronization with the Compose clock.

| Method | Description |
| :--- | :--- |
| `pressBack()` | Safely triggers the system back button. |
| `pressHome()` | Triggers the system home button. |
| `rotateScreen(orient)` | Rotates to `Orientation.PORTRAIT` or `LANDSCAPE`. |
| `handlePermissionDialog(allow)` | Automatically finds and clicks "Allow" or "Deny" on system dialogs. |
| `waitForSystemWindow(pkg)` | Waits for an external app or system window to appear. |
| `openNotificationShade()`| Opens the Android notification tray. |
| `clickNotification(text)` | Finds and clicks a notification by its text. |
| `toggleQuickSetting(name)`| Toggles a system quick setting tile (e.g., "Dark mode"). |

---

## Accessibility Actions

Utilities for auditing and simulating accessibility workflows.

| Method | Description |
| :--- | :--- |
| `navigateByAccessibility(dir)`| Simulates a screen-reader (TalkBack) swipe navigation. |
| `assertFocusOrder(tags)` | Verifies that accessibility focus moves in the expected sequence. |
| `assertInteractiveNodesHaveLabels()`| Audits the screen to ensure all clickable nodes have labels. |

---

## Time & Clock Control

Used to test animations or time-sensitive logic (like "hold to confirm" buttons).

| Method | Description |
| :--- | :--- |
| `advanceTime(ms)` | Manually advances the Compose clock by $X$ milliseconds. |
| `advanceTimeByFrame()` | Advances the clock by exactly one frame (usually 16ms). |
| `advanceTimeUntil(cond)` | Advances time in increments until a condition is met. |
| `setAutoAdvance(enabled)`| Toggles whether the framework should automatically advance time. |
| `withPausedClock { /* ... */ }`| DSL to pause the clock, run actions, and automatically resume it. |

---

## Assertions

Assertions include built-in waiting and automatic diagnostics on failure.

### Visibility
*   `assertTagDisplayed(tag)`
*   `assertTagDoesNotExist(tag)`
*   `assertTagIsNotDisplayed(tag)`

### State
*   `assertIsEnabled(tag)` / `assertIsDisabled(tag)`
*   `assertIsFocused(tag)` / `assertIsNotFocused(tag)`
*   `assertIsSelected(tag)` / `assertIsNotSelected(tag)`
*   `assertIsOn(tag)` / `assertIsOff(tag)`

### Content
*   `assertTextEquals(tag, text)`
*   `assertTextContains(tag, substring)`
*   `assertValueEquals(tag, value)`

### Hierarchy
*   `assertHasChild(parent, child)`
*   `assertHasParent(child, parent)`

---

## Advanced "Escape Hatches"

When standard actions fail, use `executeAdvancedAction` for low-level control.

```kotlin
executeAdvancedAction(testTag = "canvas") {
    gesture {
        // Multi-touch sequence
        down(Offset(10f, 10f))
        moveTo(Offset(100f, 100f))
        up()
    }
    keySequence(listOf(Key.A, Key.B, Key.Enter))
}
```

---

## Hilt Integration

### `UiEngine.createHiltRule`
Integrates Hilt and Compose correctly to ensure injection is ready before `setContent` and that the engine is registered.
```kotlin
@get:Rule(order = 0)
val hiltRule = HiltAndroidRule(this)

@get:Rule(order = 1)
val rule = UiEngine.createHiltRule(MainActivity::class.java)
```

### `UiEngine.getTestEntryPoint()`
Provides access to Hilt-injected singletons (like repositories or managers) inside a Robot via a property delegate.
```kotlin
class MyRobot : ComposeRuleScope {
    private val repo: MyRepository by UiEngine.getTestEntryPoint()
    
    fun performAction() {
        repo.doSomething()
    }
}
```

---

## Diagnostics & Utilities

### `runRobustly`
The engine's "secret sauce." Every action is wrapped in this.
*   **On Success**: Just works.
*   **On Failure**:
    1.  Logs the failure with a human-readable description.
    2.  Dumps the semantics tree to Logcat.
    3.  Takes a screenshot named `FAILURE_<timestamp>.png`.
    4.  Throws an `AssertionError` with all this context attached.

### `waitUntil`
A polling utility used internally for flakiness resilience.
```kotlin
waitUntil(timeoutMillis = 2000) { 
    // Code that might fail temporarily
}
```

### `SemanticsMatchers`
Custom matchers for specialized roles.
*   `isButton()`, `isCheckbox()`, `isSwitch()`, `isTab()`
*   `hasContentDescriptionRegex(regex)`

---

## Static Analysis (Lint Rules)

The `:engine-lint` module enforces the correct usage of the framework and prevents flakiness.

### `DirectUiTestApiUsage`
**Severity**: Error
**Description**: Prevents direct usage of `androidx.compose.ui.test` APIs (like `performClick`, `onNodeWithTag`). 
**Rationale**: Direct APIs bypass the engine's robustness pipeline (retries, scrolling, idle-sync).

### `MissingUiEngineSetup`
**Severity**: Error
**Description**: Ensures that `UiEngine.withRobot` is only used when the `ComposeTestRule` is properly registered.
**Solution**: Use `UiEngine.createRule()` or `UiEngine.createHiltRule()` to initialize your test.
