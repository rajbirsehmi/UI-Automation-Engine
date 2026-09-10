package com.sehmi.engine

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.sehmi.engine.core.ComposeRuleScope
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Global configuration and entry point for the UI Automation Engine.
 *
 * This object allows host applications to configure engine-wide settings such as 
 * default timeouts and logging levels, and manages the lifecycle of the [ComposeTestRule].
 */
object UiEngine {
    private val logger: Logger = LogManager.getLogger("UiEngine")
    private val rule = ThreadLocal<ComposeTestRule>()
    private val isRobustContext = ThreadLocal.withInitial { false }

    /**
     * Returns whether the current thread is already executing within a robust 
     * action block. Internal use only.
     */
    internal var inRobustContext: Boolean
        get() = isRobustContext.get() ?: false
        set(value) = isRobustContext.set(value)

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
        /** Whether to automatically dump the semantics tree on failure. */
        val autoDumpSemantics: Boolean = true,
        /** Whether to enable verbose logging for every automation step. */
        val verboseLogging: Boolean = true,
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
    val composeRule: ComposeTestRule
        get() = rule.get() ?: throw IllegalStateException(
            "ComposeTestRule is not set in UiEngine. " +
            "Ensure you are using createUiAutomationRule() or have registered " +
            "UiEngineRule in your test class.",
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
     * This version uses the rule stored in [UiEngine], allowing for a cleaner syntax
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
     * Creates a [ComposeContentTestRule] that is automatically registered with the [UiEngine].
     *
     * This is the recommended way to initialize the UI Automation Engine in your tests,
     * as it eliminates the need for manual setup.
     *
     * Example:
     * ```
     * @get:Rule
     * val rule = UiEngine.createRule()
     *
     * @Test
     * fun myTest() {
     *     UiEngine.withRobot(MyRobot()) { /* robot logic */ }
     * }
     * ```
     */
    @Suppress("DEPRECATION")
    fun createRule(): ComposeContentTestRule {
        return AutomationComposeContentTestRule(createComposeRule())
    }
}

/**
 * A JUnit Rule that automatically registers the [ComposeTestRule] with [UiEngine].
 *
 * Internal use only.
 */
@Suppress("unused")
class UiEngineRule(private val composeTestRule: ComposeTestRule) : TestWatcher() {
    override fun starting(description: Description) {
        UiEngine.setComposeRule(composeTestRule)
    }

    override fun finished(description: Description) {
        UiEngine.clearComposeRule()
    }
}

/**
 * A [ComposeContentTestRule] wrapper that automatically registers itself with [UiEngine].
 */
class AutomationComposeContentTestRule(
    private val baseRule: ComposeContentTestRule
) : ComposeContentTestRule by baseRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                UiEngine.setComposeRule(baseRule)
                try {
                    baseRule.apply(base, description).evaluate()
                } finally {
                    UiEngine.clearComposeRule()
                }
            }
        }
    }
}
