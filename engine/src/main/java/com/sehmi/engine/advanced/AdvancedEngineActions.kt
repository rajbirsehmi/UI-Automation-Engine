package com.sehmi.engine.advanced

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.sehmi.engine.actions.takeScreenshot
import com.sehmi.engine.assertions.assertTagDisplayed
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.matchers.printUnmergedTree
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("AdvancedEngineActions")

/**
 * DSL for building advanced UI actions that bypass high-level engine abstractions.
 *
 * This builder provides granular control over touch gestures, hardware key sequences, 
 * and raw Compose semantics actions. It is intended for complex scenarios that 
 * cannot be fulfilled by the standard robust action pipeline.
 *
 * @property composeRule The [ComposeTestRule] used for interactions.
 * @property targetTag The default test tag to operate on, if provided.
 */
class AdvancedActionBuilder(
    private val composeRule: ComposeTestRule,
    private val targetTag: String?,
) {

    /**
     * Executes a complex gesture using the [TouchInjectionScope].
     *
     * Provides a low-level API for multi-touch, pinch-to-zoom, custom drag paths, 
     * and precise pointer manipulation. Interactions within this block are 
     * performed directly on the semantics node.
     *
     * @param tag Optional override for the target test tag. If null, uses the tag 
     *            provided to [executeAdvancedAction].
     * @param block The touch injection sequence to execute.
     * @throws IllegalArgumentException if no tag is provided in the builder or the parent call.
     */
    fun gesture(tag: String? = targetTag, block: TouchInjectionScope.() -> Unit) {
        val tagToUse = tag ?: throw IllegalArgumentException("testTag must be provided either in executeAdvancedAction or explicitly in gesture call.")
        logger.debugStep("Executing advanced gesture on tag: $tagToUse")
        composeRule.onNodeWithTag(tagToUse).performTouchInput(block)
    }

    /**
     * Injects a sequence of hardware physical keys into the target node.
     *
     * Each key in the sequence is pressed (down) and released (up) in the exact 
     * order provided. This is useful for testing keyboard shortcuts or 
     * non-standard character input.
     *
     * @param keys The sequence of [Key] objects to press.
     * @param tag Optional override for the target test tag.
     * @throws IllegalArgumentException if no tag is provided.
     */
    @Suppress("unused")
    @OptIn(ExperimentalTestApi::class)
    fun keySequence(keys: List<Key>, tag: String? = targetTag) {
        val tagToUse = tag ?: throw IllegalArgumentException("testTag must be provided either in executeAdvancedAction or explicitly in keySequence call.")
        logger.debugStep("Executing advanced key sequence {} on tag: {}", keys, tagToUse)
        composeRule.onNodeWithTag(tagToUse).performKeyInput {
            keys.forEach { 
                keyDown(it)
                keyUp(it)
            }
        }
    }

    /**
     * Executes a raw Compose semantics action directly.
     *
     * This is an "escape hatch" for firing semantics actions that are not wrapped 
     * by standard Compose testing utilities.
     *
     * @param key The [SemanticsPropertyKey] representing the accessibility action to fire.
     * @param tag Optional override for the target test tag.
     * @throws IllegalArgumentException if no tag is provided or the key is not an AccessibilityAction.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> semantics(key: SemanticsPropertyKey<T>, tag: String? = targetTag) {
        val tagToUse = tag ?: throw IllegalArgumentException("testTag must be provided either in executeAdvancedAction or explicitly in semantics call.")
        logger.debugStep("Executing advanced semantics action {} on tag: {}", key, tagToUse)
        val interaction = composeRule.onNodeWithTag(tagToUse)
        try {
            // Raw execution of the semantics action.
            interaction.performSemanticsAction(key as SemanticsPropertyKey<androidx.compose.ui.semantics.AccessibilityAction<Function<Boolean>>>) { }
        } catch (e: Exception) {
            throw IllegalArgumentException("The provided SemanticsPropertyKey must be an AccessibilityAction to be executed.", e)
        }
    }

    /**
     * Provides direct access to the [SemanticsNodeInteraction] for a node.
     *
     * Useful for performing arbitrary operations, custom assertions, or fetching 
     * the underlying semantics node for manual inspection.
     *
     * @param tag Optional override for the target test tag.
     * @param block The raw interaction block to execute.
     * @throws IllegalArgumentException if no tag is provided.
     */
    @Suppress("unused")
    fun rawNodeInteraction(tag: String? = targetTag, block: SemanticsNodeInteraction.() -> Unit) {
        val tagToUse = tag ?: throw IllegalArgumentException("testTag must be provided either in executeAdvancedAction or explicitly in rawNodeInteraction call.")
        logger.debugStep("Executing raw node interaction on tag: $tagToUse")
        composeRule.onNodeWithTag(tagToUse).block()
    }
}

/**
 * Executes a complex, non-standard sequence of actions using the [AdvancedActionBuilder] DSL.
 *
 * This method acts as a reliable "escape hatch" for low-level interactions while 
 * maintaining the engine's diagnostic capabilities. It performs:
 * 1. An initial idle wait.
 * 2. Optional visibility verification for a provided [testTag].
 * 3. Execution of the [block] within the robust [waitUntil] loop.
 * 4. Automatic diagnostic capture (semantics tree and screenshot) on failure.
 *
 * @param testTag Optional tag to verify visibility before starting the action block.
 * @param timeoutMillis Total timeout for the action execution, including retries.
 * @param block Lambda providing access to [AdvancedActionBuilder] methods.
 * @throws AssertionError if the actions fail after the timeout, enriched with diagnostic data.
 */
fun ComposeRuleScope.executeAdvancedAction(
    testTag: String? = null,
    timeoutMillis: Long = 5000L,
    block: AdvancedActionBuilder.() -> Unit,
) {
    logger.infoStep("Starting executeAdvancedAction: testTag=${testTag ?: "N/A"}, timeoutMillis=$timeoutMillis")
    try {
        logger.debugStep("Waiting for Compose UI to idle")
        composeRule.waitForIdle()

        // Verify visibility if tag is provided
        if (testTag != null) {
            logger.debugStep("Verifying visibility for tag: $testTag")
            assertTagDisplayed(testTag)
        }

        // Wrap execution in flakiness retry logic
        logger.debugStep("Executing advanced action block within waitUntil loop")
        waitUntil(timeoutMillis = timeoutMillis) {
            val builder = AdvancedActionBuilder(composeRule, testTag)
            builder.block()
        }

        logger.debugStep("Action block finished, waiting for UI to idle")
        composeRule.waitForIdle()
    } catch (e: Throwable) {
        // Resilience: Capture diagnostics before rethrowing as AssertionError
        logger.debugStep("Advanced action failed. Capturing diagnostics...")
        printUnmergedTree(testTag)
        takeScreenshot("ADVANCED_ACTION_FAILURE_${System.currentTimeMillis()}")
        
        throw AssertionError(
            "Advanced action pipeline failed for tag: ${testTag ?: "N/A"}. " +
            "Semantics tree and failure screenshot captured for diagnostics.",
            e
        )
    }
    logger.debugStep("executeAdvancedAction completed")
}

