@file:Suppress("unused")

package com.sehmi.engine.actions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.math.cos
import kotlin.math.sin

private val logger: Logger = LogManager.getLogger("GestureActions")

/**
 * Supported directions for swipe gestures within the UI automation engine.
 */
enum class Direction {
    /** Swipe from bottom to top. */
    UP,
    /** Swipe from top to bottom. */
    DOWN,
    /** Swipe from right to left. */
    LEFT,
    /** Swipe from left to right. */
    RIGHT
}

/**
 * Performs a robust click on a node identified by its test tag.
 *
 * This action leverages the engine's robust action pipeline ([runRobustly]), which includes:
 * - Automatic scrolling to the element if it's within a scrollable container.
 * - Explicit waiting until the node is displayed and enabled.
 * - Automatic retry logic to handle potential flakiness.
 * - Diagnostic capture (screenshot and semantics tree dump) on failure.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or interaction fails after retries.
 */
fun ComposeRuleScope.clickOnTag(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickOnTag: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Click on tag: $testTag", testTag) {
        waitUntil {
            val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                interaction.assertIsDisplayed()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: {}", testTag)
                    interaction.performScrollTo()
                    interaction.assertIsDisplayed()
                } catch (_: AssertionError) {
                    throw e
                }
            }
            
            logger.debugStep("Waiting for tag $testTag to be enabled")
            interaction.assertIsEnabled()
            
            logger.debugStep("Performing semantics click on tag: $testTag")
            interaction.performClick()
        }
    }
    logger.debugStep("clickOnTag completed for tag: $testTag")
}

/**
 * Performs a robust click on a node containing the specified text.
 *
 * This action leverages the engine's robust action pipeline ([runRobustly]), which includes:
 * - Automatic scrolling to the element.
 * - Explicit waiting for visibility and enabled state.
 * - Retry logic and diagnostic capture on failure.
 *
 * @param text The text content of the node to click.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or interaction fails after retries.
 */
@Suppress("unused")
fun ComposeRuleScope.clickOnText(text: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickOnText: text=$text, useUnmergedTree=$useUnmergedTree")
    runRobustly("Click on text: $text") {
        waitUntil {
            val interaction = composeRule.onNodeWithText(text, useUnmergedTree = useUnmergedTree)
            try {
                interaction.assertIsDisplayed()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to text: {}", text)
                    interaction.performScrollTo()
                    interaction.assertIsDisplayed()
                } catch (_: AssertionError) {
                    throw e
                }
            }

            logger.debugStep("Waiting for text '$text' to be enabled")
            interaction.assertIsEnabled()

            logger.debugStep("Performing semantics click on text: $text")
            interaction.performClick()
        }
    }
    logger.debugStep("clickOnText completed for text: $text")
}

/**
 * Performs a robust long click on a node identified by its test tag.
 *
 * Leverages the robust action pipeline ([runRobustly]) to ensure the node is visible and
 * scrolled into view before performing the long press gesture.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or interaction fails after retries.
 */
fun ComposeRuleScope.longClickTag(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting longClickTag: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Long click on tag: $testTag", testTag) {
        waitUntil {
            val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                logger.debugStep("Attempting to scroll to tag: $testTag")
                interaction.performScrollTo()
            } catch (_: AssertionError) {
                // Ignore
            }
            logger.debugStep("Waiting for tag $testTag to be displayed")
            interaction.assertIsDisplayed()
            logger.debugStep("Performing long click on tag: $testTag")
            interaction.performTouchInput { longClick() }
            composeRule.waitForIdle()
        }
    }
    logger.debugStep("longClickTag completed for tag: $testTag")
}

/**
 * Performs a robust long click on a node containing the specified text.
 *
 * Leverages the robust action pipeline ([runRobustly]) to ensure the node is visible and
 * scrolled into view before performing the long press gesture.
 *
 * @param text The text content of the node to long click.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or interaction fails after retries.
 */
@Suppress("unused")
fun ComposeRuleScope.longClickText(text: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting longClickText: text=$text, useUnmergedTree=$useUnmergedTree")
    runRobustly("Long click on text: $text") {
        waitUntil {
            val interaction = composeRule.onNodeWithText(text, useUnmergedTree = useUnmergedTree)
            try {
                logger.debugStep("Attempting to scroll to text: {}", text)
                interaction.performScrollTo()
            } catch (_: AssertionError) {
                // Ignore
            }
            logger.debugStep("Waiting for text '$text' to be displayed")
            interaction.assertIsDisplayed()
            logger.debugStep("Performing long click on text: $text")
            interaction.performTouchInput { longClick() }
        }
    }
    logger.debugStep("longClickText completed for text: $text")
}

/**
 * Performs a robust double click on a node identified by its test tag.
 *
 * Leverages the robust action pipeline ([runRobustly]) to ensure the node is visible and
 * scrolled into view before performing the double click gesture.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or interaction fails after retries.
 */
fun ComposeRuleScope.doubleClickTag(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting doubleClickTag: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Double click on tag: $testTag", testTag) {
        waitUntil {
            val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                logger.debugStep("Attempting to scroll to tag: $testTag")
                interaction.performScrollTo()
            } catch (_: AssertionError) {
                // Ignore
            }
            logger.debugStep("Waiting for tag $testTag to be displayed")
            interaction.assertIsDisplayed()
            logger.debugStep("Performing double click on tag: $testTag")
            interaction.performTouchInput { doubleClick() }
            composeRule.waitForIdle()
        }
    }
    logger.debugStep("doubleClickTag completed for tag: $testTag")
}

/**
 * Performs a robust swipe gesture on a node identified by its test tag.
 *
 * Leverages the robust action pipeline ([runRobustly]) to ensure the target node is ready
 * for interaction. The swipe direction is specified by the [direction] parameter.
 *
 * @param testTag The unique identifier for the node to swipe on.
 * @param direction The [Direction] of the swipe.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or swipe fails.
 */
@Suppress("unused")
fun ComposeRuleScope.swipe(testTag: String, direction: Direction, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting swipe: testTag=$testTag, direction=$direction, useUnmergedTree=$useUnmergedTree")
    runRobustly("Swipe $direction on tag: $testTag", testTag) {
        waitUntil {
            val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                logger.debugStep("Attempting to scroll to tag: {}", testTag)
                interaction.performScrollTo()
            } catch (_: AssertionError) {
                // Ignore
            }
            interaction
                .performTouchInput {
                    logger.debugStep("Performing swipe {} on tag: {}", direction, testTag)
                    when (direction) {
                        Direction.UP -> swipeUp()
                        Direction.DOWN -> swipeDown()
                        Direction.LEFT -> swipeLeft()
                        Direction.RIGHT -> swipeRight()
                    }
                }
        }
    }
    logger.debugStep("swipe completed for tag: $testTag")
}

/**
 * Performs a robust drag and drop gesture from a source node to a target node.
 *
 * This action attempts to scroll both the source and target nodes into view before
 * calculating coordinates and performing the drag-and-drop touch sequence.
 *
 * @param sourceTag The test tag of the element to be dragged.
 * @param targetTag The test tag of the destination where the element should be dropped.
 * @throws AssertionError if either node is not found or gesture fails.
 */
@Suppress("unused")
fun ComposeRuleScope.dragAndDrop(sourceTag: String, targetTag: String) {
    logger.infoStep("Starting dragAndDrop: sourceTag=$sourceTag, targetTag=$targetTag")
    runRobustly("Drag from $sourceTag to $targetTag", sourceTag) {
        try {
            logger.debugStep("Scrolling to source tag: $sourceTag")
            composeRule.onNodeWithTag(sourceTag).performScrollTo()
        } catch (e: AssertionError) {}
        try {
            logger.debugStep("Scrolling to target tag: $targetTag")
            composeRule.onNodeWithTag(targetTag).performScrollTo()
        } catch (e: AssertionError) {}
        
        logger.debugStep("Calculating centers for drag and drop")
        val sourceNode = composeRule.onNodeWithTag(sourceTag).fetchSemanticsNode()
        val targetNode = composeRule.onNodeWithTag(targetTag).fetchSemanticsNode()
        
        val sourceCenter = sourceNode.boundsInRoot.center
        val targetCenter = targetNode.boundsInRoot.center
        
        logger.debugStep("Performing drag from $sourceCenter to $targetCenter")
        composeRule.onNodeWithTag(sourceTag).performTouchInput {
            down(sourceCenter)
            moveTo(targetCenter)
            up()
        }
    }
    logger.debugStep("dragAndDrop completed from $sourceTag to $targetTag")
}

/**
 * Performs a robust pinch-to-zoom gesture on a node identified by its test tag.
 *
 * Useful for maps, images, or other components that support multi-touch scaling.
 * Leverages the robust action pipeline ([runRobustly]).
 *
 * @param testTag The unique identifier for the UI element.
 * @param zoomIn True to zoom in (pinch out), false to zoom out (pinch in).
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or gesture fails.
 */
@Suppress("unused")
@OptIn(ExperimentalTestApi::class)
fun ComposeRuleScope.pinchToZoom(testTag: String, zoomIn: Boolean = true, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting pinchToZoom: testTag=$testTag, zoomIn=$zoomIn, useUnmergedTree=$useUnmergedTree")
    runRobustly("${if (zoomIn) "Zoom In" else "Zoom Out"} on tag: $testTag", testTag) {
        waitUntil {
            val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                logger.debugStep("Attempting to scroll to tag: $testTag")
                interaction.performScrollTo()
            } catch (e: AssertionError) {}
            
            logger.debugStep("Performing pinch gesture (zoomIn=$zoomIn) on tag: $testTag")
            interaction
                .performTouchInput {
                    val start0 = center + Offset(-width / 4f, -height / 4f)
                    val end0 = center + Offset(-width / 2f, -height / 2f)
                    val start1 = center + Offset(width / 4f, height / 4f)
                    val end1 = center + Offset(width / 2f, height / 2f)
                    
                    if (zoomIn) {
                        pinch(start0, end0, start1, end1)
                    } else {
                        pinch(end0, start0, end1, start1)
                    }
                }
        }
    }
    logger.debugStep("pinchToZoom completed for tag: $testTag")
}

/**
 * Performs a robust click at a specific percentage offset within a node.
 *
 * This is useful for interacting with specific parts of a component where standard
 * [performClick] might not hit the desired area.
 *
 * @param testTag The unique identifier for the UI element.
 * @param xPercentage Horizontal offset from 0.0 (left) to 1.0 (right).
 * @param yPercentage Vertical offset from 0.0 (top) to 1.0 (bottom).
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or gesture fails.
 */
fun ComposeRuleScope.clickAtOffset(testTag: String, xPercentage: Float, yPercentage: Float, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickAtOffset: testTag=$testTag, offset=($xPercentage, $yPercentage), useUnmergedTree=$useUnmergedTree")
    runRobustly("Click at offset ($xPercentage, $yPercentage) on tag: $testTag", testTag) {
        val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
        try {
            logger.debugStep("Attempting to scroll to tag: $testTag")
            interaction.performScrollTo()
        } catch (_: AssertionError) {}
        
        logger.debugStep("Performing click at offset ($xPercentage, $yPercentage) on tag: $testTag")
        interaction.performTouchInput {
            click(Offset(width * xPercentage, height * yPercentage))
        }
    }
    logger.debugStep("clickAtOffset completed for tag: $testTag")
}

/**
 * Performs a robust two-finger rotation gesture on a node identified by its test tag.
 *
 * This interaction calculates an orbital path for two touch pointers around the center 
 * of the semantics node. It is useful for verifying behaviors in maps, image editors, 
 * or custom 3D viewers.
 *
 * @param testTag The unique identifier for the UI element to rotate.
 * @param degrees The number of degrees to rotate (positive for clockwise, negative for counter-clockwise).
 * @param durationMillis The duration of the rotation gesture in milliseconds.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws AssertionError if the node is not found or interaction fails.
 */
fun ComposeRuleScope.rotate(
    testTag: String,
    degrees: Float,
    durationMillis: Long = 500L,
    useUnmergedTree: Boolean = false,
) {
    logger.infoStep("Starting rotate: testTag=$testTag, degrees=$degrees, duration=$durationMillis")
    runRobustly("Rotate $degrees degrees on tag: $testTag", testTag) {
        val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
        try {
            interaction.performScrollTo()
        } catch (e: AssertionError) {}

        interaction.performTouchInput {
            val radius = minOf(width, height) / 4f
            val nodeCenter = center
            
            val startAngle1 = 0.0
            val startAngle2 = Math.PI
            
            val endAngle1 = startAngle1 + Math.toRadians(degrees.toDouble())
            val endAngle2 = startAngle2 + Math.toRadians(degrees.toDouble())
            
            val p1Start = nodeCenter + Offset(radius * cos(startAngle1).toFloat(), radius * sin(startAngle1).toFloat())
            val p2Start = nodeCenter + Offset(radius * cos(startAngle2).toFloat(), radius * sin(startAngle2).toFloat())
            
            down(0, p1Start)
            down(1, p2Start)
            
            val steps = (durationMillis / 16).toInt().coerceAtLeast(1)
            for (i in 1..steps) {
                val fraction = i.toFloat() / steps
                val currentAngle1 = startAngle1 + ((endAngle1 - startAngle1) * fraction)
                val currentAngle2 = startAngle2 + ((endAngle2 - startAngle2) * fraction)
                
                moveTo(0, nodeCenter + Offset(radius * cos(currentAngle1).toFloat(), radius * sin(currentAngle1).toFloat()))
                moveTo(1, nodeCenter + Offset(radius * cos(currentAngle2).toFloat(), radius * sin(currentAngle2).toFloat()))
                advanceEventTime(16)
            }
            
            up(0)
            up(1)
        }
    }
    logger.debugStep("rotate completed for tag: $testTag")
}

/**
 * Performs a robust multi-finger swipe gesture on a node identified by its test tag.
 *
 * Simulates a simultaneous swipe with multiple touch pointers (2 to 4 fingers). 
 * This is commonly used for system-level gestures (like 3-finger swipe to switch apps) 
 * or custom complex navigation patterns within a professional application.
 *
 * @param testTag The unique identifier for the UI element to swipe on.
 * @param fingers The number of fingers to use (supports 2, 3, or 4).
 * @param direction The [Direction] of the swipe.
 * @param durationMillis The duration of the swipe in milliseconds.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookups.
 * @throws IllegalArgumentException if the finger count is not between 2 and 4.
 * @throws AssertionError if the node is not found or interaction fails.
 */
fun ComposeRuleScope.multiFingerSwipe(
    testTag: String,
    fingers: Int,
    direction: Direction,
    durationMillis: Long = 300L,
    useUnmergedTree: Boolean = false,
) {
    logger.infoStep("Starting multiFingerSwipe: testTag=$testTag, fingers=$fingers, direction=$direction")
    require(fingers in (2..4)) { "multiFingerSwipe supports 2 to 4 fingers." }
    
    runRobustly("$fingers-finger swipe $direction on tag: $testTag", testTag) {
        val interaction = composeRule.onNodeWithTag(testTag, useUnmergedTree)
        try {
            interaction.performScrollTo()
        } catch (e: AssertionError) {}
        
        interaction.performTouchInput {
            val startOffsets = mutableListOf<Offset>()
            val endOffsets = mutableListOf<Offset>()
            
            val spread = 20f // pixels between fingers
            
            for (i in 0 until fingers) {
                val fingerOffset = (i - ((fingers - 1) / 2f)) * spread
                val start = when (direction) {
                    Direction.UP -> Offset(center.x + fingerOffset, bottom - 10f)
                    Direction.DOWN -> Offset(center.x + fingerOffset, top + 10f)
                    Direction.LEFT -> Offset(right - 10f, center.y + fingerOffset)
                    Direction.RIGHT -> Offset(left + 10f, center.y + fingerOffset)
                }
                val end = when (direction) {
                    Direction.UP -> Offset(center.x + fingerOffset, top + 10f)
                    Direction.DOWN -> Offset(center.x + fingerOffset, bottom - 10f)
                    Direction.LEFT -> Offset(left + 10f, center.y + fingerOffset)
                    Direction.RIGHT -> Offset(right - 10f, center.y + fingerOffset)
                }
                startOffsets.add(start)
                endOffsets.add(end)
            }
            
            // All fingers down
            startOffsets.forEachIndexed { index, offset -> down(index, offset) }
            
            // Move all fingers
            val steps = (durationMillis / 16).toInt().coerceAtLeast(1)
            for (step in 1..steps) {
                val fraction = step.toFloat() / steps
                startOffsets.forEachIndexed { index, start ->
                    val end = endOffsets[index]
                    moveTo(index, start + ((end - start) * fraction))
                }
                advanceEventTime(16)
            }
            
            // All fingers up
            for (i in 0 until fingers) {
                up(i)
            }
        }
    }
    logger.debugStep("multiFingerSwipe completed for tag: $testTag")
}

