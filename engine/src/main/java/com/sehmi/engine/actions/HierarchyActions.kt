package com.sehmi.engine.actions

import androidx.compose.ui.test.*
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("HierarchyActions")

/**
 * Performs a robust click on the first child of the node identified by [parentTag].
 *
 * @param parentTag The test tag of the parent container.
 * @param useUnmergedTree Whether to use the unmerged semantics tree.
 */
fun ComposeRuleScope.clickFirstChild(parentTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickFirstChild: parentTag=$parentTag")
    runRobustly("Click first child of: $parentTag", parentTag) {
        uiTestEngineRule.onNodeWithTag(parentTag, useUnmergedTree)
            .onChildren()
            .filter(hasClickAction())
            .onFirst()
            .performClick()
    }
}

/**
 * Performs a robust click on the child at the specified [index] of the node 
 * identified by [parentTag].
 *
 * @param parentTag The test tag of the parent container.
 * @param index The zero-based index of the child to click.
 * @param useUnmergedTree Whether to use the unmerged semantics tree.
 */
fun ComposeRuleScope.clickChildAtIndex(parentTag: String, index: Int, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickChildAtIndex: parentTag=$parentTag, index=$index")
    runRobustly("Click child at index $index of: $parentTag", parentTag) {
        uiTestEngineRule.onNodeWithTag(parentTag, useUnmergedTree)
            .onChildAt(index)
            .performClick()
    }
}

/**
 * Performs a robust click on a descendant of [parentTag] that matches [matcher].
 *
 * @param parentTag The test tag of the ancestor container.
 * @param matcher The matcher to identify the descendant.
 */
fun ComposeRuleScope.clickDescendant(parentTag: String, matcher: SemanticsMatcher, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickDescendant: parentTag=$parentTag")
    runRobustly("Click descendant of $parentTag matching $matcher", parentTag) {
        uiTestEngineRule.onNode(hasParent(hasTestTag(parentTag)) and matcher, useUnmergedTree = useUnmergedTree)
            .performClick()
    }
}

/**
 * Performs a robust click on the last child of the node identified by [parentTag].
 *
 * @param parentTag The test tag of the parent container.
 * @param useUnmergedTree Whether to use the unmerged semantics tree.
 */
fun ComposeRuleScope.clickLastChild(parentTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickLastChild: parentTag=$parentTag")
    runRobustly("Click last child of: $parentTag", parentTag) {
        uiTestEngineRule.onNodeWithTag(parentTag, useUnmergedTree)
            .onChildren()
            .filter(hasClickAction())
            .onLast()
            .performClick()
    }
}

/**
 * Performs a robust click on a sibling of the node identified by [testTag] 
 * that matches [matcher].
 *
 * @param testTag The test tag of the reference node.
 * @param matcher The matcher to identify the sibling.
 */
fun ComposeRuleScope.clickSibling(testTag: String, matcher: SemanticsMatcher, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting clickSibling: referenceTag=$testTag")
    runRobustly("Click sibling of $testTag matching $matcher", testTag) {
        uiTestEngineRule.onNode(hasAnySibling(hasTestTag(testTag)) and matcher, useUnmergedTree = useUnmergedTree)
            .performClick()
    }
}
