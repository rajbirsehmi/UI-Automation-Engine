# 🤖 UI Automation Engine

[![JitPack](https://jitpack.io/v/rajbirsehmi/UI-Engine.svg)](https://jitpack.io/#rajbirsehmi/UI-Engine)
[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg?style=flat-square)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg?style=flat-square)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-navy.svg?style=flat-square)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**UI Automation Engine** is an industrial-grade testing framework for Jetpack Compose. It eliminates the most common pain points of UI testing: **flakiness**, **race conditions**, and **opaque failure logs**. 

By providing a robust orchestration layer over standard Compose APIs, it ensures your tests are stable, readable, and provide immediate diagnostic artifacts on failure.

---

## 📖 Table of Contents
- [Core Philosophy](#-core-philosophy)
- [Architecture: The Robot Pattern](#-architecture-the-robot-pattern)
- [The Robustness Pipeline](#-the-robustness-pipeline)
- [Key Features](#-key-features)
- [Diagnostic Infrastructure](#-diagnostic-infrastructure)
- [Advanced Time Control](#-advanced-time-control)
- [Hilt Integration](#-hilt-integration)
- [Static Analysis (Lint)](#-static-analysis-lint)
- [Installation](#-installation)

---

## 🎯 Core Philosophy

Testing UI shouldn't be about fighting the framework. Most Compose tests fail because they check for elements before recomposition finishes or because they don't handle scroll states. 

The Engine follows three core principles:
1. **Readable DSL**: Tests should read like user stories.
2. **Deterministic Execution**: Every action is synchronized with the Compose clock and idle state.
3. **Artifact-First Failures**: A failing test should tell you *why* it failed with a screenshot and a tree dump, not just a stack trace.

---

## 🏗 Architecture: The Robot Pattern

The engine enforces the **Robot Pattern**, separating the "What" of the test from the "How" of the implementation.

### 1. The Robot (`ComposeRuleScope`)
Robots implement the `ComposeRuleScope` to gain access to the engine's robust action suite. By default, they utilize the global rule managed by `UiEngine`, but can also accept a rule via constructor for specific needs.

```kotlin
// Modern Robot (Using UiEngine)
class GestureRobot : ComposeRuleScope {
    fun tapMainBox() {
        clickOnTag("gesture_box") // Automatic idle-sync & scroll
    }

    fun verifyStatus(text: String) {
        assertTextEquals("gesture_status", text) // Automatic polling
    }
}
```

### 2. The DSL (`withRobot`)
The engine provides two ways to invoke your robots.

#### 🚀 Centralized Invocation (Recommended)
This approach eliminates the need to pass the rule to every robot instance. Use `UiEngine.createRule()` to initialize your test—it automatically manages the engine's lifecycle.

```kotlin
@get:Rule
val rule = UiEngine.createRule() // Combined rule for Compose + Engine

@Test
fun testGestureFlow() {
    UiEngine.withRobot(GestureRobot()) {
        tapMainBox()
        verifyStatus("Tapped")
    }
}
```

#### 🛠 Standard Extension
You can also call `withRobot` directly on the `ComposeTestRule`.

```kotlin
@Test
fun testGestureFlow() {
    composeTestRule.withRobot(GestureRobot()) {
        tapMainBox()
    }
}
```

---

## 🛡 The Robustness Pipeline

Every extension method in the engine (like `clickOnTag`, `enterText`, `swipe`) is wrapped in a `runRobustly` block. This pipeline performs the following steps:

1.  **Idle Synchronization**: Waits for the Compose UI to be stable.
2.  **Auto-Scrolling**: Attempts to bring the node into the viewport before interaction.
3.  **Polling & Retry**: Uses `waitUntil` to handle elements that might be appearing via animations or network delays.
4.  **Failure Capture**: If all retries fail, it automatically triggers the diagnostic suite.

---

## ✨ Key Features

| Action Category | Examples | Benefits |
| :--- | :--- | :--- |
| **Gestures** | `clickOnTag`, `doubleTap`, `longPress`, `dragAndDrop`, `pinchToZoom`, `rotate`, `multiFingerSwipe` | Handles complex multi-touch interactions including orbital rotation and multi-finger patterns. |
| **Text Input** | `enterText`, `replaceText`, `clearText`, `pressImeAction`, `requestFocus` | Ensures the keyboard is ready and verifies state after input. |
| **Scrolling** | `scrollToTag`, `scrollToIndex`, `scrollToKey`, `swipeUntilVisible` | Prevents "Node not found" errors in long LazyColumns and dynamic lists. |
| **System** | `pressBack`, `pressHome`, `handlePermissionDialog`, `openNotificationShade`, `clickNotification`, `toggleQuickSetting` | Deep integration with Android OS, including notifications and system settings via UIAutomator. |
| **Accessibility**| `navigateByAccessibility`, `assertFocusOrder`, `assertInteractiveNodesHaveLabels` | Automated focus traversal simulation and batch audits for accessibility compliance. |
| **Guardrails** | `resourcePrefix = "engine_"` | Built-in lint rules and naming conventions to prevent resource collisions in host apps. |

---

## 📸 Diagnostic Infrastructure

Debugging failed CI runs is often impossible without visual feedback. The Engine provides:

### Automatic Screenshots
On every `AssertionError`, a high-resolution screenshot is saved to the device's cache directory: `FAILURE_<timestamp>.png`.

### Semantics Tree Dump
The engine dumps the entire **unmerged semantics tree** to Logcat under the `ComposeAutomation` tag. This reveals the exact state of the UI at the microsecond of failure.

### Log4j2 Integration
The entire engine is instrumented with Log4j2. You can see every internal decision in your logs:
```text
INFO  GestureActions - Starting clickOnTag: testTag=login_button
DEBUG GestureActions - Attempting to scroll to tag: login_button
DEBUG GestureActions - Waiting for tag to be displayed and enabled
DEBUG GestureActions - Performing semantics click on tag: login_button
```

> [!TIP]
> You can disable these verbose step-by-step logs by setting `verboseLogging = false` in the `UiEngine.configure()` block.

---

## 🕒 Advanced Time Control

Testing animations? The Engine provides a safer way to manipulate the `MainTestClock`.

*   **`advanceTime(ms)`**: Advances the clock by a specific duration.
*   **`advanceTimeUntil(condition)`**: Advances the clock in frame-increments until a UI state is met (perfect for finishing animations).
*   **`withPausedClock { /* ... */ }`**: Automatically pauses the clock, runs your logic, and resumes it, ensuring no side effects on other tests.

---

## 💉 Hilt Integration

The engine is built for modern DI-heavy architectures and supports both Hilt and non-Hilt projects via **Product Flavors**.

### 1. Standard Variant
Optimized for minimal footprint. Use this if your testing environment does not require Dagger Hilt.

### 2. Hilt Variant
Includes the necessary Hilt testing dependencies (`hilt-android-testing`) and KSP processing. Use this if you need to access Hilt components within your robots.

#### 🛠 Hilt Setup Example
```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FeatureTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    // Automatically registers with UiEngine and handles Hilt lifecycle
    @get:Rule(order = 1)
    val rule = UiEngine.createHiltRule(MainActivity::class.java)

    @Test
    fun testWithInjectedRobot() {
        hiltRule.inject()
        
        UiEngine.withRobot(InjectedRobot()) {
            performBusinessLogic()
        }
    }
}

class InjectedRobot : ComposeRuleScope {
    // Access your Hilt graph directly inside the robot
    private val api: MyApiService by UiEngine.getTestEntryPoint()

    fun performBusinessLogic() {
        // ...
    }
}
```

*   **Rule Chaining**: Always use `order = 0` for `HiltAndroidRule` to ensure the Dagger graph is ready before the Activity starts.
*   **Entry Points**: Use the `by UiEngine.getTestEntryPoint()` delegate to access singletons without constructor injection.

---

## ⚙️ Global Configuration

You can customize the engine's behavior (timeouts, logging, diagnostics) globally by calling `UiEngine.configure()`. This is typically done in a custom `TestRunner` or a `@BeforeClass` method.

```kotlin
UiEngine.configure(
    UiEngine.Configuration(
        defaultTimeoutMillis = 10_000L, // 10 seconds for robust actions
        pollIntervalMillis = 100L,      // Interval between retries
        verboseLogging = false,         // Cleaner Logcat for passing tests
        autoCaptureScreenshots = true,  // Capture screenshot on failure
        autoDumpSemantics = true        // Dump semantics tree on failure
    )
)
```

---

## 🚫 Static Analysis (Lint)

The `:engine-lint` module ensures your team doesn't regress into flaky habits. 

### 1. Forbidden API Usage (`DirectUiTestApiUsage`)
Detects direct usage of standard Compose APIs like `performClick()` or `onNodeWithTag()` and flags them as errors, suggesting the robust Engine equivalent via QuickFix.

### 2. Missing Engine Setup (`MissingUiEngineSetup`)
Ensures that `UiEngine.withRobot` is never called without a properly configured rule. It flags missing `UiEngineRule` declarations or manual setup calls as errors before your tests ever run.

---

## 🛠 Installation

### 1. Add Repository
Add the JitPack repository to your root `settings.gradle.kts` (and `mavenLocal()` if you are testing the engine locally):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal() // For local development/testing
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Configure Versions
Add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
engine = "0.3.0-alpha"

[libraries]
uiengine = { group = "com.github.rajbirsehmi.UI-Engine", name = "robot-testing-engine", version.ref = "engine" }
engine-lint = { group = "com.github.rajbirsehmi.UI-Engine", name = "engine-lint", version.ref = "engine" }
```

### 3. Add Dependency & Configuration
Choose the configuration that matches your project's Dependency Injection setup.

#### For Standard Projects (Non-Hilt)
In your module-level `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        // ...
        missingDimensionStrategy("di", "standard")
    }
}

dependencies {
    androidTestImplementation(libs.uiengine) {
        artifact {
            type = "aar"
            classifier = "standardDebug"
        }
    }
}
```

#### For Hilt-based Projects
In your module-level `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        // ...
        missingDimensionStrategy("di", "hilt")
    }
}

dependencies {
    androidTestImplementation(libs.uiengine) {
        artifact {
            type = "aar"
            classifier = "hiltDebug"
        }
    }
}
```

> [!NOTE]
> The `standardDebug` and `hiltDebug` classifiers are used respectively to ensure compatibility with standard test builds, while the `missingDimensionStrategy` handles the internal flavor selection.

### 4. Optional: Static Analysis
To enforce robust testing patterns, add the lint check:

```kotlin
dependencies {
    lintChecks(libs.engine.lint)
}
```

### 5. Packaging Configuration
To avoid resource collisions from the underlying Log4j2 dependency, add this to your `android` block:

```kotlin
android {
    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
        }
    }
}
```

---

## 📄 Documentation
For the full API reference (Actions, Assertions, Gestures), see [ENGINE_REFERENCE.md](ENGINE_REFERENCE.md).

---

## 📄 License
Copyright © 2026 Sehmi. Distributed under the [MIT License](LICENSE). Affiliation and credit are appreciated but not required.
