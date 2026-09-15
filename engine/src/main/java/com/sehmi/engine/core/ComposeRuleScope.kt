package com.sehmi.engine.core

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.sehmi.engine.UiTestEngine

/**
 * Represents a scope that provides access to the [ComposeTestRule].
 *
 * Robots or test classes should implement this interface to gain access to the engine's 
 * robust automation extensions for actions, assertions, and gestures.
 *
 * Implementing this scope allows for a clean DSL-like syntax when performing UI interactions 
 * within the Compose testing framework.
 *
 * @property uiTestEngineRule The underlying [ComposeTestRule] used for UI interactions.
 */
interface ComposeRuleScope {
    /**
     * The [ComposeTestRule] used for UI interactions. 
     * Defaults to the rule managed by [UiTestEngine].
     */
    val uiTestEngineRule: ComposeTestRule
        get() = UiTestEngine.uiTestEngineRule
}

/**
 * DSL entry-point to execute actions and assertions on a robot within the context 
 * of a [ComposeTestRule].
 *
 * This function bridges the standard Compose test rule with the custom robot architecture, 
 * allowing developers to write tests that are more readable and maintainable.
 *
 * Example usage:
 * ```
 * composeTestRule.withRobot(LoginRobot()) {
 *     enterUsername("user")
 *     enterPassword("pass")
 *     clickLogin()
 * }
 * ```
 *
 * @param T The type of the robot, which must implement [ComposeRuleScope].
 * @param robot The robot instance to execute the block on.
 * @param block The interaction block containing actions/assertions.
 */
inline fun <T : ComposeRuleScope> ComposeTestRule.withRobot(
    robot: T,
    crossinline block: T.() -> Unit
) {
    robot.block()
}
