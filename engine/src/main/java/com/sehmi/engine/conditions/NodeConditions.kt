package com.sehmi.engine.conditions

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import com.sehmi.engine.core.ComposeRuleScope
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("NodeConditions")

/**
 * Checks if a node with the given [testTag] exists in the semantics tree.
 *
 * Unlike assertions, this returns a [Boolean] and does not throw an exception 
 * if the node is missing.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @return True if the node exists, false otherwise.
 */
fun ComposeRuleScope.hasTag(testTag: String, useUnmergedTree: Boolean = false): Boolean {
    logger.debug("Checking if tag exists: $testTag")
    return try {
        uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree).fetchSemanticsNode()
        true
    } catch (e: AssertionError) {
        false
    }
}

/**
 * Checks if a node with the given [testTag] is currently displayed on the screen.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @return True if the node is displayed, false otherwise.
 */
fun ComposeRuleScope.isDisplayed(testTag: String, useUnmergedTree: Boolean = false): Boolean {
    logger.debug("Checking if tag is displayed: $testTag")
    return try {
        uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree).assertIsDisplayed()
        true
    } catch (e: AssertionError) {
        false
    }
}

/**
 * Checks if a node with the given [testTag] is enabled (interactive).
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @return True if the node is enabled, false otherwise.
 */
fun ComposeRuleScope.isEnabled(testTag: String, useUnmergedTree: Boolean = false): Boolean {
    logger.debug("Checking if tag is enabled: $testTag")
    return try {
        uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree).assertIsEnabled()
        true
    } catch (e: AssertionError) {
        false
    }
}

/**
 * Checks if a node with the given [testTag] is currently selected.
 *
 * Useful for tabs, radio buttons, or selection lists.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @return True if the node is selected, false otherwise.
 */
fun ComposeRuleScope.isSelected(testTag: String, useUnmergedTree: Boolean = false): Boolean {
    logger.debug("Checking if tag is selected: $testTag")
    return try {
        uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree).assertIsSelected()
        true
    } catch (e: AssertionError) {
        false
    }
}

/**
 * Checks if a toggleable node (like a Switch or Checkbox) with the given [testTag] 
 * is in the ON state.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @return True if the node is ON, false otherwise.
 */
fun ComposeRuleScope.isChecked(testTag: String, useUnmergedTree: Boolean = false): Boolean {
    logger.debug("Checking if tag is checked: $testTag")
    return try {
        val node = uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree).fetchSemanticsNode()
        val state = node.config.getOrNull(SemanticsProperties.ToggleableState)
        state == ToggleableState.On
    } catch (e: AssertionError) {
        false
    }
}

/**
 * Checks if a node with the given [testTag] contains the specified [text].
 *
 * @param testTag The unique identifier for the UI element.
 * @param text The string to search for.
 * @param substring Whether to match as a substring (true) or exact match (false).
 * @param ignoreCase Whether to ignore case during matching.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @return True if the node contains the text, false otherwise.
 */
fun ComposeRuleScope.hasText(
    testTag: String,
    text: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
    useUnmergedTree: Boolean = false
): Boolean {
    logger.debug("Checking if tag '$testTag' has text: '$text'")
    return try {
        uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree)
            .assert(hasText(text, substring = substring, ignoreCase = ignoreCase))
        true
    } catch (e: AssertionError) {
        false
    }
}
