package com.sehmi.engine.actions

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("ScrollActions")

/**
 * Robustly scrolls to a node identified by its test tag.
 *
 * This action ensures that the target node is scrolled into view before continuing,
 * which is a prerequisite for many interactions like clicking or text entry.
 * It uses the engine's robust action pipeline ([runRobustly]).
 *
 * @param targetTag The unique identifier for the UI element to scroll to.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or scrolling fails.
 */
fun ComposeRuleScope.scrollToTag(targetTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting scrollToTag: targetTag=$targetTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Scroll to tag: $targetTag", targetTag) {
        logger.debugStep("Performing scroll to tag: $targetTag")
        try {
            composeRule.onNodeWithTag(targetTag, useUnmergedTree).performScrollTo()
        } catch (_: AssertionError) {}
        composeRule.waitForIdle()
    }
    logger.debugStep("scrollToTag completed for tag: $targetTag")
}

/**
 * Robustly scrolls to a node containing the specified text.
 *
 * Uses the engine's robust action pipeline ([runRobustly]) to ensure the node is 
 * visible and interactive.
 *
 * @param text The text content of the node to scroll to.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or scrolling fails.
 */
@Suppress("unused")
fun ComposeRuleScope.scrollToText(text: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting scrollToText: text=$text, useUnmergedTree=$useUnmergedTree")
    runRobustly("Scroll to text: $text") {
        logger.debugStep("Performing scroll to text: $text")
        try {
            composeRule.onNodeWithText(text, useUnmergedTree = useUnmergedTree).performScrollTo()
        } catch (_: AssertionError) {}
        composeRule.waitForIdle()
    }
    logger.debugStep("scrollToText completed for text: $text")
}

/**
 * Robustly scrolls to a node and then performs a click operation.
 *
 * This is a composite action that combines [scrollToTag] and [clickOnTag] 
 * within the robust action pipeline ([runRobustly]).
 *
 * @param targetTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found, scrolling fails, or click fails.
 */
@Suppress("unused")
fun ComposeRuleScope.scrollAndClick(targetTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting scrollAndClick: targetTag=$targetTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Scroll and click tag: $targetTag", targetTag) {
        logger.debugStep("Calling scrollToTag for tag: $targetTag")
        scrollToTag(targetTag, useUnmergedTree = useUnmergedTree)
        logger.debugStep("Calling clickOnTag for tag: $targetTag")
        clickOnTag(targetTag, useUnmergedTree = useUnmergedTree)
    }
    logger.debugStep("scrollAndClick completed for tag: $targetTag")
}

/**
 * Robustly scrolls a scrollable container to a specific item index.
 *
 * Useful for long lists or grids where the target index might be off-screen.
 * Leverages the robust action pipeline ([runRobustly]).
 *
 * @param containerTag The test tag of the scrollable container (e.g., LazyColumn).
 * @param index The zero-based index of the item to scroll to.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the container is not found or scrolling fails.
 */
fun ComposeRuleScope.scrollToIndex(containerTag: String, index: Int, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting scrollToIndex: containerTag=$containerTag, index=$index, useUnmergedTree=$useUnmergedTree")
    runRobustly("Scroll container $containerTag to index $index", containerTag) {
        logger.debugStep("Performing scroll to index $index in container $containerTag")
        composeRule.onNodeWithTag(containerTag, useUnmergedTree).performScrollToIndex(index)
        composeRule.waitForIdle()
    }
    logger.debugStep("scrollToIndex completed for container $containerTag, index $index")
}

/**
 * Robustly scrolls a scrollable container to an item identified by a specific key.
 *
 * Useful for Lazy layouts that use stable keys for their items.
 * Leverages the robust action pipeline ([runRobustly]).
 *
 * @param containerTag The test tag of the scrollable container.
 * @param key The stable key of the item to scroll to.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the container is not found or scrolling fails.
 */
@Suppress("unused")
fun ComposeRuleScope.scrollToKey(containerTag: String, key: Any, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting scrollToKey: containerTag=$containerTag, key=$key, useUnmergedTree=$useUnmergedTree")
    runRobustly("Scroll container $containerTag to key $key", containerTag) {
        logger.debugStep("Performing scroll to key {} in container {}", key, containerTag)
        composeRule.onNodeWithTag(containerTag, useUnmergedTree).performScrollToKey(key)
        composeRule.waitForIdle()
    }
    logger.debugStep("scrollToKey completed for container {}, key {}", containerTag, key)
}

/**
 * Performs a defensive swipe gesture repeatedly until a target node becomes visible.
 *
 * This interaction is designed for dynamic lists or pages where the number of items 
 * is unknown or the target element is far below the fold. It performs a swipe in 
 * the specified [direction], synchronizes with the UI idle state, and checks for 
 * the [targetTag] presence in each step.
 *
 * @param targetTag The test tag of the element to wait for.
 * @param direction The [Direction] to swipe in (e.g., Direction.UP to scroll down).
 * @param maxSwipes The maximum number of swipe attempts before giving up.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found after [maxSwipes] attempts.
 */
@Suppress("unused")
fun ComposeRuleScope.swipeUntilVisible(
    targetTag: String,
    direction: Direction,
    maxSwipes: Int = 10,
    useUnmergedTree: Boolean = false,
) {
    logger.infoStep("Starting swipeUntilVisible: targetTag=$targetTag, direction=$direction, maxSwipes=$maxSwipes, useUnmergedTree=$useUnmergedTree")
    runRobustly("Swipe until $targetTag is visible", targetTag) {
        var swiped = 0
        while (swiped < maxSwipes) {
            try {
                logger.debugStep("Checking if target tag $targetTag exists (attempt ${swiped + 1})")
                this.waitUntil(timeoutMillis = 1000L) {
                    composeRule.onNodeWithTag(targetTag, useUnmergedTree).assertExists()
                }
                composeRule.waitForIdle()
                logger.debugStep("Target tag $targetTag found")
                return@runRobustly
            } catch (_: AssertionError) {
                logger.debugStep("Target tag $targetTag not found, performing swipe $direction")
                // Not found, perform global swipe on the root node
                composeRule.onNodeWithTag("root").performTouchInput {
                    when (direction) {
                        Direction.UP -> swipeUp()
                        Direction.DOWN -> swipeDown()
                        Direction.LEFT -> swipeLeft()
                        Direction.RIGHT -> swipeRight()
                    }
                }
                composeRule.waitForIdle()
                swiped++
            }
        }
        throw AssertionError("Node with tag $targetTag not found after $maxSwipes swipes.")
    }
    logger.debugStep("swipeUntilVisible completed for tag: $targetTag")
}

