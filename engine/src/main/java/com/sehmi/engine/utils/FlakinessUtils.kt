package com.sehmi.engine.utils

import android.util.Log
import com.sehmi.engine.UiEngine
import com.sehmi.engine.actions.takeScreenshot
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.matchers.printUnmergedTree
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("FlakinessUtils")

/**
 * A generic explicit wait utility that polls a given [action] until it succeeds or 
 * the [timeoutMillis] is reached.
 *
 * This version is aware of the [ComposeRuleScope] and ensures that the Compose 
 * virtual clock is advanced (via waitForIdle) between polling attempts. 
 * This is critical for actions that trigger timers or delays (like gestures).
 *
 * @param T The return type of the action.
 * @param timeoutMillis Total time to wait before throwing the last encountered error.
 * @param pollIntervalMillis Time to wait between consecutive polling attempts.
 * @param action The block of code to execute and poll.
 * @return The result of the [action] if it succeeds within the timeout.
 * @throws Throwable The last error encountered if the timeout is reached.
 */
internal fun <T> ComposeRuleScope.waitUntil(
    timeoutMillis: Long = UiEngine.config.defaultTimeoutMillis,
    pollIntervalMillis: Long = UiEngine.config.pollIntervalMillis,
    action: () -> T,
): T {
    logger.debugStep("Starting robust waitUntil: timeoutMillis=$timeoutMillis")
    val startTime = System.currentTimeMillis()
    var lastError: Throwable? = null
    var attempt = 0

    // Use a smaller internal poll step for better responsiveness, 
    // but cap it at the requested pollIntervalMillis.
    val internalStep = minOf(50L, pollIntervalMillis)

    while ((System.currentTimeMillis() - startTime) < timeoutMillis) {
        attempt++
        try {
            logger.debugStep("Executing waitUntil action (attempt $attempt)")
            val result = action()
            logger.debugStep("waitUntil action succeeded on attempt $attempt")
            return result
        } catch (e: Throwable) {
            lastError = e
            logger.debugStep("waitUntil attempt $attempt failed: ${e.message}. Syncing UI and retrying...")
            
            // Sync UI state
            composeRule.waitForIdle()
            
            // Responsive sleep: check the clock frequently to avoid over-sleeping
            Thread.sleep(internalStep)
        }
    }

    logger.error("waitUntil timed out after $timeoutMillis ms")
    throw lastError ?: RuntimeException("Timeout reached during waitUntil without specific error.")
}

/**
 * Top-level utility for cases where ComposeRuleScope is not available.
 * Warning: This version does NOT advance the Compose virtual clock automatically.
 */
internal fun <T> waitUntil(
    timeoutMillis: Long = UiEngine.config.defaultTimeoutMillis,
    pollIntervalMillis: Long = UiEngine.config.pollIntervalMillis,
    action: () -> T,
): T {
    val startTime = System.currentTimeMillis()
    var lastError: Throwable? = null
    while ((System.currentTimeMillis() - startTime) < timeoutMillis) {
        try {
            return action()
        } catch (e: Throwable) {
            lastError = e
            Thread.sleep(pollIntervalMillis)
        }
    }
    throw lastError ?: RuntimeException("Timeout reached during waitUntil.")
}

/**
 * Wraps a block of UI interaction code in a robust execution pipeline.
 *
 * This utility ensures high reliability in tests by:
 * 1. Waiting for the Compose UI to be idle before execution.
 * 2. Catching any failures and enriching them with diagnostic information.
 * 3. Automatically capturing a screenshot and dumping the semantics tree to Logcat 
 *    under the `ComposeAutomation` tag.
 *
 * It is the primary robustness mechanism used by almost all high-level actions 
 * and assertions in the engine.
 *
 * @param T The return type of the block.
 * @param description A human-readable description of the action being performed, 
 *                    included in failure reports.
 * @param tag Optional test tag associated with the action, used for diagnostic 
 *            tree dumps.
 * @param block The interaction or assertion block to execute robustly.
 * @return The result of the [block].
 * @throws AssertionError A wrapped error containing original failure details and 
 *                        references to diagnostic artifacts (screenshot/tree dump).
 */
internal fun <T> ComposeRuleScope.runRobustly(
    description: String,
    tag: String? = null,
    block: ComposeRuleScope.() -> T
): T {
    val isNested = UiEngine.inRobustContext
    if (!isNested) {
        logger.infoStep("Starting runRobustly: description='$description', tag=${tag ?: "N/A"}")
        UiEngine.inRobustContext = true
    }
    
    return try {
        if (!isNested) {
            logger.debugStep("Waiting for Compose UI to be idle")
            composeRule.waitForIdle()
        }
        
        val result = this.block()
        
        if (!isNested) {
            logger.debugStep("Robust action completed successfully: $description")
        }
        result
    } catch (e: Throwable) {
        // If nested, we don't capture diagnostics yet, we let the top-most block handle it
        if (isNested) throw e

        val timestamp = System.currentTimeMillis()
        val failureName = "FAILURE_$timestamp"
        
        logger.error("Robust action failed: $description. Error: ${e.message}")
        Log.e("ComposeAutomation", "Robust action failed: $description. Capturing diagnostics...")
        
        // Capture Diagnostics
        try {
            if (UiEngine.config.autoDumpSemantics) {
                logger.debugStep("Capturing diagnostics: printUnmergedTree")
                printUnmergedTree(tag)
            }
            if (UiEngine.config.autoCaptureScreenshots) {
                logger.debugStep("Capturing diagnostics: takeScreenshot({})", failureName)
                takeScreenshot(failureName)
            }
        } catch (diagError: Throwable) {
            Log.e("ComposeAutomation", "Failed to capture diagnostics: ${diagError.message}")
        }

        val enrichedMessage = """
            |Automation Failure: $description
            |Target Tag: ${tag ?: "N/A"}
            |Artifact: $failureName.png
            |Original Error: ${e.message}
        """.trimMargin()
        
        throw AssertionError(enrichedMessage, e)
    } finally {
        if (!isNested) {
            UiEngine.inRobustContext = false
            logger.debugStep("runRobustly finished for: $description")
        }
    }
}

