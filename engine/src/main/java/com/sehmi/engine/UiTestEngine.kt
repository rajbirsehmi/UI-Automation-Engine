package com.sehmi.engine

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.sehmi.engine.actions.captureLogcat
import com.sehmi.engine.actions.captureViewHierarchy
import com.sehmi.engine.actions.takeScreenshot
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.matchers.printUnmergedTree
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement
import android.util.Log

/**
 * Global configuration and entry point for the UI Automation Engine.
 *
 * This object allows host applications to configure engine-wide settings such as 
 * default timeouts and logging levels, and manages the lifecycle of the [ComposeTestRule].
 */
object UiTestEngine {
    private val logger: Logger = LogManager.getLogger("UiTestEngine")
    private val rule = ThreadLocal<ComposeTestRule>()
    private val isRobustContext = ThreadLocal.withInitial { false }
    private val diagnosticsCaptured = ThreadLocal.withInitial { false }

    /**
     * Returns whether the current thread is already executing within a robust 
     * action block. Internal use only.
     */
    internal var inRobustContext: Boolean
        get() = isRobustContext.get() ?: false
        set(value) = isRobustContext.set(value)

    /**
     * Tracks whether diagnostics have already been captured for the current failure.
     * Internal use only.
     */
    internal var wasDiagnosticsCaptured: Boolean
        get() = diagnosticsCaptured.get() ?: false
        set(value) = diagnosticsCaptured.set(value)

    /**
     * Configuration settings for the engine.
     */
    data class Configuration(
        /** The default timeout for robust actions in milliseconds. */
        val defaultTimeoutMillis: Long = 5000L,
        /** The polling interval for wait operations in milliseconds. */
        val pollIntervalMillis: Long = 100L,
        /** Whether to automatically capture screenshots on failure. */
        val autoCaptureScreenshots: Boolean = true,
        /** The directory to save screenshots in. If null, uses additionalTestOutputDir or app cache. */
        val screenshotDirectory: String? = null,
        /** Whether to automatically dump the semantics tree on failure. */
        val autoDumpSemantics: Boolean = true,
        /** Whether to enable verbose logging for every automation step. */
        val verboseLogging: Boolean = true,
        /** Whether to automatically capture Logcat tail on failure. */
        val autoCaptureLogcat: Boolean = true,
        /** Whether to automatically dump the Android View hierarchy on failure. */
        val autoCaptureViewHierarchy: Boolean = false,
        /** The number of Logcat lines to capture on failure. */
        val logcatTailLines: Int = 100,
    )

    private var _config: Configuration = Configuration()

    /**
     * The current engine configuration.
     */
    val config: Configuration get() = _config

    /**
     * The current [ComposeTestRule] managed by the engine.
     *
     * @throws IllegalStateException if the rule has not been set.
     */
    val uiTestEngineRule: ComposeTestRule
        get() = rule.get() ?: throw IllegalStateException(
            "ComposeTestRule is not set in UiTestEngine. " +
            "Ensure you are using createUiAutomationRule() or have registered " +
            "UiTestEngineRule in your test class.",
        )

    /**
     * Initializes the UI Engine with custom configuration.
     *
     * While the engine is largely stateless, calling this method allows for 
     * customizing global behavior before running tests.
     *
     * @param configuration The custom [Configuration] to apply.
     */
    fun configure(configuration: Configuration) {
        if (configuration.verboseLogging) {
            logger.info("Configuring UI Engine: $configuration")
        }
        _config = configuration
    }

    /**
     * Programmatically grants a runtime permission to the app under test.
     *
     * This uses the system's [android.app.UiAutomation] to grant the specified runtime [permission]
     * directly without needing any UI interaction.
     *
     * Example:
     * ```
     * UiTestEngine.enablePermission("android.permission.POST_NOTIFICATIONS")
     * ```
     *
     * @param permission The fully qualified name of the permission (e.g. "android.permission.POST_NOTIFICATIONS").
     * @param packageName Optional package name of the app. Defaults to the target context's package name.
     */
    fun enablePermission(permission: String, packageName: String? = null) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetPackage = packageName ?: instrumentation.targetContext.packageName
        logger.info("Granting runtime permission: $permission to package: $targetPackage")
        instrumentation.uiAutomation.grantRuntimePermission(targetPackage, permission)
    }

    /**
     * Sets the [ComposeTestRule] for the current test thread.
     * 
     * Internal use only via JUnit Rules.
     */
    fun setComposeRule(composeTestRule: ComposeTestRule) {
        rule.set(composeTestRule)
    }

    /**
     * Clears the [ComposeTestRule] from the engine.
     *
     * Internal use only via JUnit Rules.
     */
    fun clearComposeRule() {
        rule.remove()
    }

    /**
     * DSL entry-point to execute actions and assertions on a robot.
     *
     * This version uses the rule stored in [UiTestEngine], allowing for a cleaner syntax
     * when the rule is set globally.
     *
     * @param T The type of the robot, which must implement [ComposeRuleScope].
     * @param robot The robot instance to execute the block on.
     * @param block The interaction block containing actions/assertions.
     */
    inline fun <T : ComposeRuleScope> withRobot(
        robot: T,
        crossinline block: T.() -> Unit
    ) {
        robot.block()
    }

    /**
     * Creates a [ComposeContentTestRule] that is automatically registered with the [UiTestEngine].
     *
     * This is the recommended way to initialize the UI Automation Engine in your tests,
     * as it eliminates the need for manual setup.
     *
     * Example:
     * ```
     * @get:Rule
     * val rule = UiTestEngine.createRule()
     *
     * @Test
     * fun myTest() {
     *     UiTestEngine.withRobot(MyRobot()) { /* robot logic */ }
     * }
     * ```
     */
    @Suppress("DEPRECATION")
    fun createRule(): UiTestEngineContentRule {
        return UiTestEngineContentRule(createComposeRule())
    }
}

/**
 * A JUnit Rule that automatically captures diagnostics on test failure.
 */
class FailureDiagnosticWatcher : TestWatcher() {
    public override fun starting(description: Description) {
        UiTestEngine.wasDiagnosticsCaptured = false
    }

    public override fun failed(e: Throwable, description: Description) {
        if (UiTestEngine.wasDiagnosticsCaptured) return

        val timestamp = System.currentTimeMillis()
        val failureName = "GLOBAL_FAILURE_${description.methodName}_$timestamp"
        
        Log.e("ComposeAutomation", "Test failed: ${description.displayName}. Capturing diagnostics...")
        
        try {
            if (UiTestEngine.config.autoDumpSemantics) {
                // We can't easily access the rule here without it being set, 
                // but it should be set in the current thread.
                try {
                    val scope = object : ComposeRuleScope {
                        override val uiTestEngineRule: ComposeTestRule get() = UiTestEngine.uiTestEngineRule
                    }
                    scope.printUnmergedTree()
                } catch (t: Throwable) {
                    Log.w("ComposeAutomation", "Failed to dump semantics: ${t.message}")
                }
            }
            if (UiTestEngine.config.autoCaptureScreenshots) {
                takeScreenshot(failureName)
            }
            if (UiTestEngine.config.autoCaptureLogcat) {
                captureLogcat(failureName, UiTestEngine.config.logcatTailLines)
            }
            if (UiTestEngine.config.autoCaptureViewHierarchy) {
                captureViewHierarchy(failureName)
            }
            UiTestEngine.wasDiagnosticsCaptured = true
        } catch (diagError: Throwable) {
            Log.e("ComposeAutomation", "Failed to capture global diagnostics: ${diagError.message}")
        }
    }
}

/**
 * A JUnit Rule that automatically registers the [ComposeTestRule] with [UiTestEngine].
 *
 * Internal use only.
 */
@Suppress("unused")
class UiTestEngineRule(private val composeTestRule: ComposeTestRule) : TestWatcher() {
    private val diagnosticWatcher = FailureDiagnosticWatcher()

    override fun apply(base: Statement, description: Description): Statement {
        // Wrap the base statement with both this watcher and the diagnostic watcher
        return diagnosticWatcher.apply(super.apply(base, description), description)
    }

    override fun starting(description: Description) {
        UiTestEngine.setComposeRule(composeTestRule)
    }

    override fun finished(description: Description) {
        UiTestEngine.clearComposeRule()
    }
}

/**
 * A [ComposeContentTestRule] wrapper that automatically registers itself with [UiTestEngine].
 */
class UiTestEngineContentRule(
    private val baseRule: ComposeContentTestRule
) : ComposeContentTestRule by baseRule {
    private val diagnosticWatcher = FailureDiagnosticWatcher()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                UiTestEngine.setComposeRule(baseRule)
                try {
                    diagnosticWatcher.apply(baseRule.apply(base, description), description).evaluate()
                } finally {
                    UiTestEngine.clearComposeRule()
                }
            }
        }
    }
}
