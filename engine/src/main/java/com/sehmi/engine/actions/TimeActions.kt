package com.sehmi.engine.actions

import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("TimeActions")

/**
 * Robustly advances the main clock by the specified amount of time in milliseconds.
 *
 * Use this to manually trigger time-dependent behaviors like animations, transitions, 
 * or delayed states in tests when [setAutoAdvance] is set to false.
 * Leverages the robust action pipeline ([runRobustly]).
 *
 * @param millis The amount of time to advance the clock by.
 */
fun ComposeRuleScope.advanceTime(millis: Long) {
    logger.infoStep("Starting advanceTime: millis=$millis")
    runRobustly("Advance time by $millis ms") {
        logger.debugStep("Advancing clock by $millis ms")
        composeRule.mainClock.advanceTimeBy(millis)
    }
    logger.debugStep("advanceTime completed")
}

/**
 * Robustly advances the main clock by exactly one frame.
 *
 * Useful for inspecting UI state at precise points during an animation.
 * Leverages the robust action pipeline ([runRobustly]).
 */
fun ComposeRuleScope.advanceTimeByFrame() {
    logger.infoStep("Starting advanceTimeByFrame")
    runRobustly("Advance time by one frame") {
        logger.debugStep("Advancing clock by one frame")
        composeRule.mainClock.advanceTimeByFrame()
    }
    logger.debugStep("advanceTimeByFrame completed")
}

/**
 * Robustly advances the main clock until a specific [condition] is met.
 *
 * Note: The condition should rely on state driven by the clock (like animations).
 * Leverages the robust action pipeline ([runRobustly]).
 *
 * @param timeoutMillis Maximum time to advance the clock before failing.
 * @param condition The predicate that must be satisfied.
 * @throws AssertionError if the condition is not met within [timeoutMillis].
 */
fun ComposeRuleScope.advanceTimeUntil(timeoutMillis: Long = 1000L, condition: () -> Boolean) {
    logger.infoStep("Starting advanceTimeUntil: timeoutMillis=$timeoutMillis")
    runRobustly("Advance time until condition met") {
        logger.debugStep("Advancing clock until condition is met (timeout=$timeoutMillis)")
        composeRule.mainClock.advanceTimeUntil(timeoutMillis, condition)
    }
    logger.debugStep("advanceTimeUntil completed")
}

/**
 * Sets whether the Compose framework should automatically advance the clock to 
 * reach an idle state.
 *
 * Disabling auto-advance allows for manual control over animations and time-based 
 * logic using [advanceTime].
 *
 * @param enabled True to enable auto-advance (default), false to disable.
 */
fun ComposeRuleScope.setAutoAdvance(enabled: Boolean) {
    logger.infoStep("Starting setAutoAdvance: enabled=$enabled")
    composeRule.mainClock.autoAdvance = enabled
    logger.debugStep("setAutoAdvance completed")
}

/**
 * Executes a [block] of code with the main clock paused (autoAdvance = false).
 *
 * The previous auto-advance state is automatically restored after the block 
 * finishes, ensuring test isolation.
 *
 * @param block The block of code to execute while the clock is paused.
 */
fun ComposeRuleScope.withPausedClock(block: () -> Unit) {
    logger.infoStep("Starting withPausedClock")
    val wasAutoAdvance = composeRule.mainClock.autoAdvance
    logger.debugStep("Pausing clock (previous autoAdvance state: $wasAutoAdvance)")
    composeRule.mainClock.autoAdvance = false
    try {
        block()
    } finally {
        logger.debugStep("Restoring clock autoAdvance state to: $wasAutoAdvance")
        composeRule.mainClock.autoAdvance = wasAutoAdvance
        logger.debugStep("withPausedClock completed")
    }
}

