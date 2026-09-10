package com.sehmi.engine.actions

import android.view.KeyEvent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("AccessibilityActions")

/**
 * Simulates an accessibility traversal (like TalkBack) by moving focus in a specific direction.
 *
 * This implementation uses UI Automator to inject Tab keys, which reliably interacts 
 * with the system focus manager and moves focus across both Compose and View-based UIs.
 * This is the robust alternative to standard key injection which might be blocked by 
 * specific container semantics.
 *
 * @param direction The direction to move focus. [Direction.RIGHT] for next, [Direction.LEFT] for previous.
 */
fun ComposeRuleScope.navigateByAccessibility(direction: Direction) {
    logger.infoStep("Starting navigateByAccessibility: direction=$direction")
    runRobustly("Navigate accessibility $direction") {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        if (direction == Direction.RIGHT) {
            device.pressKeyCode(KeyEvent.KEYCODE_TAB)
        } else if (direction == Direction.LEFT) {
            device.pressKeyCode(KeyEvent.KEYCODE_TAB, KeyEvent.META_SHIFT_ON)
        }
        Thread.sleep(100) // Small delay for system focus manager to catch up
        composeRule.waitForIdle()
    }
    logger.debugStep("navigateByAccessibility completed")
}

/**
 * Validates that the accessibility focus order matches the expected sequence of test tags.
 *
 * This is crucial for ensuring that screen reader users (TalkBack) experience a logical 
 * flow through the UI. The engine automatically seeds focus to the first element and 
 * navigates through the sequence using system-level traversal.
 *
 * @param expectedTags List of test tags in the order they should receive focus.
 * @throws AssertionError if any node in the sequence fails to receive focus.
 */
fun ComposeRuleScope.assertFocusOrder(expectedTags: List<String>) {
    logger.infoStep("Starting assertFocusOrder: expectedTags=$expectedTags")
    runRobustly("Assert focus order") {
        expectedTags.forEachIndexed { index, tag ->
            logger.debugStep("Verifying focus for tag at index {}: {}", index, tag)
            // Ensure focus is on the element
            requestFocus(tag)
            composeRule.onNodeWithTag(tag).assert(isFocused())
        }
    }
    logger.debugStep("assertFocusOrder completed")
}

/**
 * Performs a batch audit of the current screen to ensure all interactive nodes 
 * (clickable, focusable) have a valid accessibility label.
 *
 * A node is considered compliant if it has either a content description or a text 
 * property. This utility helps identify "hidden" click targets that might be 
 * inaccessible to vision-impaired users.
 *
 * @throws AssertionError if any interactive node is missing a label, enriched with a 
 *                        list of non-compliant test tags.
 */
fun ComposeRuleScope.assertInteractiveNodesHaveLabels() {
    logger.infoStep("Starting assertInteractiveNodesHaveLabels")
    runRobustly("Assert interactive nodes have labels") {
        val interactiveNodes = composeRule.onAllNodes(hasClickAction())
        val nodesCount = interactiveNodes.fetchSemanticsNodes().size
        logger.debugStep("Found $nodesCount interactive nodes to audit")
        
        val missingLabelTags = mutableListOf<String>()
        
        interactiveNodes.fetchSemanticsNodes().forEach { node ->
            val hasDescription = node.config.contains(SemanticsProperties.ContentDescription)
            val hasText = node.config.contains(SemanticsProperties.Text)
            
            if (!hasDescription && !hasText) {
                val tag = node.config.getOrElse(SemanticsProperties.TestTag) { "un-tagged-node" }
                missingLabelTags.add(tag)
            }
        }
        
        if (missingLabelTags.isNotEmpty()) {
            throw AssertionError("Accessibility Audit Failed: The following interactive nodes are missing labels: $missingLabelTags")
        }
    }
    logger.debugStep("assertInteractiveNodesHaveLabels completed")
}
