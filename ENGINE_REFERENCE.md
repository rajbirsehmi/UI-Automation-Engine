# UI Automation Engine: Technical Reference

This document provides an exhaustive reference for the `:engine` module, detailing every API, its purpose, and usage examples.

---

## Table of Contents
1. [Global Configuration](#global-configuration)
2. [Core Architecture](#core-architecture)
3. [Gestures & Interactions](#gestures--interactions)
4. [Text & Focus Actions](#text--focus-actions)
5. [Scrolling & Navigation](#scrolling--navigation)
6. [Hierarchy & Relationship Actions](#hierarchy--relationship-actions)
7. [System Actions](#system-actions)
8. [Accessibility Actions](#accessibility-actions)
9. [Time & Clock Control](#time--clock-control)
10. [Assertions](#assertions)
11. [Advanced "Escape Hatches"](#advanced-escape-hatches)
12. [Hilt Integration](#hilt-integration)
13. [Diagnostics & Utilities](#diagnostics--utilities)
14. [Static Analysis (Lint Rules)](#static-analysis-lint-rules)

---

## Global Configuration

### `UiTestEngine.configure(Configuration)`
Allows global customization of the engine's behavior. Call this in a `@BeforeClass` or global test initializer.

#### Example Usage:
```kotlin
@BeforeClass
@JvmStatic
fun setupEngine() {
    UiTestEngine.configure(
        UiTestEngine.Configuration(
            defaultTimeoutMillis = 8000L,
            pollIntervalMillis = 150L,
            verboseLogging = false,
            autoCaptureScreenshots = true,
            autoDumpSemantics = true,
            autoCaptureLogcat = true,
            logcatTailLines = 150
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
| `autoCaptureLogcat` | `true` | Automatically captures a Logcat tail on failure. |
| `autoCaptureViewHierarchy`| `false`| Automatically dumps the Android View hierarchy on failure. |
| `logcatTailLines` | `100` | Number of Logcat lines to capture on failure. |
| `verboseLogging` | `true` | Enables high-level "Starting/Completed" logs for every step. |

### `UiTestEngine.enablePermission(permission, packageName = null)`
Programmatically grants a runtime permission directly to the app under test using `UiAutomation` without manual UI clicks.

#### Example Usage:
```kotlin
@Test
fun testNotificationFeature() {
    // Grant for target package automatically
    UiTestEngine.enablePermission("android.permission.POST_NOTIFICATIONS")
    
    // Or specify a package
    UiTestEngine.enablePermission("android.permission.CAMERA", "com.example.otherapp")
}
```

---

## Core Architecture

### `UiTestEngine` (Singleton)
The central manager for the `ComposeTestRule`. Use this to avoid passing rules to every robot constructor.
*   **`setComposeRule(rule)`**: Manually sets the rule (for custom rule setups).
*   **`clearComposeRule()`**: Clears the rule.
*   **`withRobot(robot) { /* ... */ }`**: DSL entry point using the global rule.

### `UiTestEngine.createRule()` (Factory)
The easiest way to initialize the engine for standard tests. Returns a wrapped `ComposeContentTestRule` that handles all lifecycle registration.
*   **Usage**: `@get:Rule val rule = UiTestEngine.createRule()`

### `UiTestEngineRule` (JUnit Rule)
A decorator rule for existing `ComposeTestRule` instances.
*   **Usage**: `@get:Rule val engineRule = UiTestEngineRule(composeRule)`
*   **Benefit**: Ensures that `setComposeRule` and `clearComposeRule` are called at the correct times.

### `ComposeRuleScope` (Interface)
The foundation of the engine. Robots must implement this to gain access to all robust extension methods.
*   **Property**: `uiTestEngineRule: ComposeTestRule`. Defaults to `UiTestEngine.uiTestEngineRule`.

### `withRobot` (Extension)
The DSL entry point for executing blocks of code within a robot's scope.
*   **Usage**:
    ```kotlin
    // Option A: Centralized
    UiTestEngine.withRobot(MyRobot()) { /* robot logic */ }

    // Option B: Standard
    uiTestEngineRule.withRobot(MyRobot()) { /* robot logic */ }
    ```

---

## Gestures & Interactions

All gesture actions are robust: they wait for idle, automatically scroll to the element, and perform retries.

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `clickOnTag` | `clickOnTag(testTag: String, useUnmergedTree: Boolean = false)` | Performs a robust click on a node with the specified test tag. |
| `clickOnText` | `clickOnText(text: String, useUnmergedTree: Boolean = false)` | Performs a robust click on a node containing the specified text. |
| `clickOnDescription` | `clickOnDescription(contentDescription: String, useUnmergedTree: Boolean = false)` | Performs a robust click on a node with the specified content description. |
| `click` | `click(matcher: SemanticsMatcher, useUnmergedTree: Boolean = false)` | Performs a robust click on a node matching a custom `SemanticsMatcher`. |
| `longClickTag` | `longClickTag(testTag: String, useUnmergedTree: Boolean = false)` | Performs a long-press on a node by tag. |
| `longClickText` | `longClickText(text: String, useUnmergedTree: Boolean = false)` | Performs a long-press on a node by text content. |
| `doubleClickTag` | `doubleClickTag(testTag: String, useUnmergedTree: Boolean = false)` | Performs a double-tap on a node by tag. |
| `swipe` | `swipe(testTag: String, direction: Direction, useUnmergedTree: Boolean = false)` | Swipes a specific node in a `Direction` (UP, DOWN, LEFT, RIGHT). |
| `dragAndDrop` | `dragAndDrop(sourceTag: String, targetTag: String)` | Drags from one tag and drops onto another. |
| `pinchToZoom` | `pinchToZoom(testTag: String, zoomIn: Boolean, useUnmergedTree: Boolean = false)` | Performs a two-finger pinch gesture on a node. |
| `clickAtOffset` | `clickAtOffset(testTag: String, xPercent: Float, yPercent: Float, useUnmergedTree: Boolean = false)` | Clicks at a percentage-based offset (0.0 to 1.0) within a node. |
| `rotate` | `rotate(testTag: String, degrees: Float, durationMillis: Long = 500L, useUnmergedTree: Boolean = false)` | Performs a two-finger rotation gesture around the node center. |
| `multiFingerSwipe` | `multiFingerSwipe(testTag: String, fingers: Int, direction: Direction, durationMillis: Long = 500L, useUnmergedTree: Boolean = false)` | Performs a simultaneous swipe with 2-4 fingers. |
| `mouseClick` | `mouseClick(testTag: String, button: Int = 0, useUnmergedTree: Boolean = false)` | Simulates a mouse click (left/right/center) for desktop/large screen testing. |
| `rotaryScroll` | `rotaryScroll(testTag: String, delta: Float, useUnmergedTree: Boolean = false)` | Simulates a rotary input event (e.g., watch bezel) on a node. |

---

## Text & Focus Actions

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `enterText` | `enterText(testTag: String, text: String, useUnmergedTree: Boolean = false)` | Appends text to a field. |
| `replaceText` | `replaceText(testTag: String, text: String, useUnmergedTree: Boolean = false)` | Clears existing text and replaces it with new text. |
| `clearText` | `clearText(testTag: String, useUnmergedTree: Boolean = false)` | Clears all text from a field. |
| `pressImeAction` | `pressImeAction(testTag: String, useUnmergedTree: Boolean = false)` | Triggers the Keyboard IME action (e.g., Search, Done, Go). |
| `requestFocus` | `requestFocus(testTag: String, useUnmergedTree: Boolean = false)` | Programmatically requests focus for a specific node. |
| `performKeyInput` | `performKeyInput(testTag: String, key: Key, useUnmergedTree: Boolean = false)` | Injects a physical hardware key event (e.g., `Key.Enter`). |

---

## Scrolling & Navigation

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `scrollToTag` | `scrollToTag(testTag: String, useUnmergedTree: Boolean = false)` | Scrolls the UI until the specified tag is in view. |
| `scrollToText` | `scrollToText(text: String, useUnmergedTree: Boolean = false)` | Scrolls the UI until the specified text is in view. |
| `scrollToIndex` | `scrollToIndex(containerTag: String, index: Int, useUnmergedTree: Boolean = false)` | Scrolls a container (LazyColumn/Row) to a specific index. |
| `scrollToKey` | `scrollToKey(containerTag: String, key: Any, useUnmergedTree: Boolean = false)` | Scrolls a container to an item with a stable key. |
| `swipeUntilVisible` | `swipeUntilVisible(containerTag: String, targetTag: String, direction: Direction, maxSwipes: Int = 10, useUnmergedTree: Boolean = false)`| Repeatedly swipes in a direction until a node appears. |
| `scrollAndClick` | `scrollAndClick(testTag: String, useUnmergedTree: Boolean = false)` | Convenience method to scroll to a tag and then click it. |

---

## Hierarchy & Relationship Actions

These actions allow targeting nodes based on their position in the semantics tree relative to other nodes.

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `clickFirstChild` | `clickFirstChild(parentTag: String, useUnmergedTree: Boolean = false)` | Clicks the first clickable child of the specified parent tag. |
| `clickLastChild` | `clickLastChild(parentTag: String, useUnmergedTree: Boolean = false)` | Clicks the last clickable child of the specified parent tag. |
| `clickChildAtIndex` | `clickChildAtIndex(parentTag: String, index: Int, useUnmergedTree: Boolean = false)`| Clicks the child at the specific index within the parent. |
| `clickDescendant` | `clickDescendant(parentTag: String, matcher: SemanticsMatcher, useUnmergedTree: Boolean = false)`| Clicks a descendant of the parent that matches the provided criteria. |
| `clickSibling` | `clickSibling(testTag: String, matcher: SemanticsMatcher, useUnmergedTree: Boolean = false)` | Clicks a sibling of the specified tag that matches the matcher. |

---

## System Actions

These actions use UIAutomator internally to interact with the Android OS outside the Compose bounds. They automatically handle synchronization with the Compose clock.

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `pressBack` | `pressBack()` | Safely triggers the system back button. |
| `pressHome` | `pressHome()` | Triggers the system home button. |
| `rotateScreen` | `rotateScreen(orientation: Orientation)` | Rotates to `Orientation.PORTRAIT` or `LANDSCAPE`. |
| `handlePermissionDialog` | `handlePermissionDialog(allow: Boolean)` | Automatically finds and clicks "Allow" or "Deny" on system dialogs. |
| `handlePermissionDialog` | `handlePermissionDialog(action: PermissionAction)`| Clicks a specific `PermissionAction` (WHILE_USING_THE_APP, ONLY_THIS_TIME, DONT_ALLOW). |
| `waitForSystemWindow` | `waitForSystemWindow(packageName: String, timeoutMillis: Long = 5000L)` | Waits for an external app or system window to appear. |
| `openNotificationShade` | `openNotificationShade()`| Opens the Android notification tray. |
| `clickNotification` | `clickNotification(searchText: String, timeoutMillis: Long = 5000L)` | Finds and clicks a notification by its text. |
| `toggleQuickSetting` | `toggleQuickSetting(settingName: String)`| Toggles a system quick setting tile (e.g., "Dark mode"). |

### `onDevice { ... }` Scope
Provides direct access to UI Automator for hardware and OS-level control.

#### Example Usage:
```kotlin
onDevice {
    pressRecentApps()
    clearNotifications()
    setOrientation(landscape = true)
}
```

| Method | Full Signature / Behavior | Description |
| :--- | :--- | :--- |
| `pressKey` | `pressKey(keyCode: Int)` | Presses a physical hardware key. |
| `pressHome` | `pressHome()` | Navigation home hardware key. |
| `pressBack` | `pressBack()` | Navigation back hardware key. |
| `pressRecentApps` | `pressRecentApps()` | Opens the recent apps screen. |
| `pressDPadCenter` | `pressDPadCenter()` | Presses the DPad center button. |
| `executeShell` | `executeShell(command: String): String` | Executes a shell command and returns the output. |
| `findObject` | `findObject(selector: BySelector): UiObject2?` | Finds a UI object by the given `BySelector`. |
| `click` | `click(selector: BySelector)` | Robustly clicks an object matching the selector (waits for presence). |
| `swipe` | `swipe(startX: Int, startY: Int, endX: Int, endY: Int, steps: Int = 10)` | Swipes from one screen coordinate point to another. |
| `openQuickSettings` | `openQuickSettings()` | Opens the quick settings panel shade. |
| `wakeUp` | `wakeUp()` | Wakes up the device screen. |
| `sleep` | `sleep()` | Puts the device to sleep. |
| `freezeRotation` | `freezeRotation()` | Locks the current screen orientation. |
| `unfreezeRotation` | `unfreezeRotation()` | Unlocks the screen rotation. |
| `clearNotifications` | `clearNotifications()` | Opens the notification shade and clicks "Clear all". |
| `unlock` | `unlock()` | Dismisses the keyguard (lock screen) via shell command. |
| `setOrientation` | `setOrientation(landscape: Boolean)` | Sets the device orientation to left (landscape) or natural (portrait). |

---

## Accessibility Actions

Utilities for auditing and simulating accessibility workflows.

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `navigateByAccessibility` | `navigateByAccessibility(direction: Direction)`| Simulates a screen-reader (TalkBack) swipe navigation. |
| `assertFocusOrder` | `assertFocusOrder(expectedTags: List<String>)` | Verifies that accessibility focus moves in the expected sequence. |
| `assertInteractiveNodesHaveLabels` | `assertInteractiveNodesHaveLabels()`| Audits the screen to ensure all clickable nodes have labels. |

---

## Time & Clock Control

Used to test animations or time-sensitive logic (like "hold to confirm" buttons).

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `advanceTime` | `advanceTime(durationMillis: Long)` | Manually advances the Compose clock by $X$ milliseconds. |
| `advanceTimeByFrame` | `advanceTimeByFrame()` | Advances the clock by exactly one frame (usually 16ms). |
| `advanceTimeUntil` | `advanceTimeUntil(timeoutMillis: Long, condition: () -> Boolean)` | Advances time in increments until a condition is met. |
| `setAutoAdvance` | `setAutoAdvance(enabled: Boolean)`| Toggles whether the framework should automatically advance time. |
| `withPausedClock` | `withPausedClock(block: () -> Unit)`| DSL to pause the clock, run actions, and automatically resume it. |

---

## Assertions

Assertions include built-in waiting and automatic diagnostics on failure. All assertion methods support an optional `useUnmergedTree: Boolean = false` argument.

### Visibility
*   `assertTagDisplayed(testTag: String, useUnmergedTree: Boolean = false)`
*   `assertTagDoesNotExist(testTag: String, useUnmergedTree: Boolean = false)`
*   `assertTagIsNotDisplayed(testTag: String, useUnmergedTree: Boolean = false)`

### State
*   `assertIsEnabled(testTag: String, useUnmergedTree: Boolean = false)` / `assertIsDisabled(testTag: String, useUnmergedTree: Boolean = false)`
*   `assertIsFocused(testTag: String, useUnmergedTree: Boolean = false)` / `assertIsNotFocused(testTag: String, useUnmergedTree: Boolean = false)`
*   `assertIsSelected(testTag: String, useUnmergedTree: Boolean = false)` / `assertIsNotSelected(testTag: String, useUnmergedTree: Boolean = false)`
*   `assertIsOn(testTag: String, useUnmergedTree: Boolean = false)` / `assertIsOff(testTag: String, useUnmergedTree: Boolean = false)`

### Content
*   `assertTextEquals(testTag: String, expectedText: String, useUnmergedTree: Boolean = false)`
*   `assertTextContains(testTag: String, substring: String, useUnmergedTree: Boolean = false)`
*   `assertValueEquals(testTag: String, expectedValue: String, useUnmergedTree: Boolean = false)`

### Hierarchy
*   `assertHasChild(parentTag: String, childTag: String, useUnmergedTree: Boolean = false)`
*   `assertHasParent(childTag: String, parentTag: String, useUnmergedTree: Boolean = false)`

---

## Conditions (State Queries)

These methods return boolean state indicators for conditional logic inside robots, instead of throwing immediately on failure. All conditions accept an optional `useUnmergedTree: Boolean = false` parameter.

| Method | Full Signature | Description |
| :--- | :--- | :--- |
| `hasTag` | `hasTag(tag: String, useUnmergedTree: Boolean = false): Boolean` | Returns true if a node with the specified tag exists in the tree. |
| `isDisplayed` | `isDisplayed(tag: String, useUnmergedTree: Boolean = false): Boolean` | Returns true if the node is currently displayed. |
| `isEnabled` | `isEnabled(tag: String, useUnmergedTree: Boolean = false): Boolean` | Returns true if the node is enabled. |
| `isSelected` | `isSelected(tag: String, useUnmergedTree: Boolean = false): Boolean` | Returns true if the node is selected. |
| `isChecked` | `isChecked(tag: String, useUnmergedTree: Boolean = false): Boolean` | Returns true if the node is checked / toggled on. |
| `hasText` | `hasText(tag: String, text: String, substring: Boolean = false, ignoreCase: Boolean = false, useUnmergedTree: Boolean = false): Boolean` | Returns true if the node has matching text content. |

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

### `UiTestEngine.createHiltRule`
Integrates Hilt and Compose correctly to ensure injection is ready before `setContent` and that the engine is registered.
```kotlin
@get:Rule(order = 0)
val hiltRule = HiltAndroidRule(this)

@get:Rule(order = 1)
val rule = UiTestEngine.createHiltRule(MainActivity::class.java)
```

### `UiTestEngine.getTestEntryPoint()`
Provides access to Hilt-injected singletons (like repositories or managers) inside a Robot via a property delegate.
```kotlin
class MyRobot : ComposeRuleScope {
    private val repo: MyRepository by UiTestEngine.getTestEntryPoint()
    
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
    3.  Takes a screenshot named `FAILURE_<timestamp>.png` (using robust multi-path resolution).
    4.  Captures a Logcat tail to `FAILURE_<timestamp>.log`.
    5.  (Optional) Dumps the View hierarchy to `FAILURE_<timestamp>.xml`.
    6.  Throws an `AssertionError` with all this context attached.

### `FailureDiagnosticWatcher`
A global JUnit `TestWatcher` that serves as a fallback for `runRobustly`. It ensures that *any* test failure triggers a diagnostic capture, even if the engine's robustness pipeline was not directly involved. It automatically skips capture if `runRobustly` already completed it for the current failure.

### `waitUntil`
A polling utility used internally for flakiness resilience.
```kotlin
waitUntil(timeoutMillis = 2000) { 
    // Code that might fail temporarily
}
```

### Specialized Waits
*   `waitUntilExists(matcher)`: Waits until exactly one node matching the criteria exists.
*   `waitUntilDoesNotExist(tag)`: Waits until a specific tag is removed from the tree.
*   `waitUntilNodeCount(matcher, count)`: Waits until a specific number of nodes match.

### `SemanticsMatchers`
Custom matchers for specialized roles and relationships.
*   `isButton()`, `isCheckbox()`, `isSwitch()`, `isTab()`
*   `isDialog()`, `isPopup()`, `isHeading()`
*   `hasStateDescription(text)`
*   `hasContentDescriptionRegex(regex)`
*   `hasAnyDescendant(matcher)`, `hasAnyAncestor(matcher)`, `hasAnySibling(matcher)`

---

## Static Analysis (Lint Rules)

The `:engine-lint` module enforces the correct usage of the framework and prevents flakiness.

### `DirectUiTestApiUsage`
**Severity**: Error
**Description**: Prevents direct usage of `androidx.compose.ui.test` APIs (like `performClick`, `onNodeWithTag`). 
**Rationale**: Direct APIs bypass the engine's robustness pipeline (retries, scrolling, idle-sync).

### `MissingUiTestEngineSetup`
**Severity**: Error
**Description**: Ensures that `UiTestEngine.withRobot` is only used when the `ComposeTestRule` is properly registered.
**Solution**: Use `UiTestEngine.createRule()` or `UiTestEngine.createHiltRule()` to initialize your test.
