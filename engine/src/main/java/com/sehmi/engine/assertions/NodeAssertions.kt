@file:Suppress("unused")

package com.sehmi.engine.assertions

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger("NodeAssertions")

/**
 * Robustly asserts that a node with the given test tag is displayed on the screen.
 *
 * This assertion leverages the engine's robust action pipeline ([runRobustly]), which:
 * - Automatically scrolls to the target node.
 * - Explicitly waits until the node is displayed using a polling mechanism.
 * - Captures diagnostics on failure.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is not displayed within the timeout.
 */
fun ComposeRuleScope.assertTagDisplayed(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertTagDisplayed: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag displayed: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsDisplayed()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsDisplayed()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertTagDisplayed completed for tag: $testTag")
}

/**
 * Robustly asserts that a node with the given test tag does not exist in the 
 * semantics tree.
 *
 * Leverages [runRobustly] and [waitUntil] to handle cases where an element 
 * might take time to be removed from the UI.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node still exists after the timeout.
 */
fun ComposeRuleScope.assertTagDoesNotExist(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertTagDoesNotExist: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag does not exist: $testTag", testTag) {
        this.waitUntil {
            logger.debugStep("Asserting tag $testTag does not exist")
            composeRule.onNodeWithTag(testTag, useUnmergedTree).assertDoesNotExist()
        }
    }
    logger.debugStep("assertTagDoesNotExist completed for tag: $testTag")
}

/**
 * Robustly asserts that a node with the given test tag is NOT displayed.
 *
 * Note: The node might exist in the tree but be hidden or off-screen.
 * Leverages [runRobustly] and [waitUntil].
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node remains displayed after the timeout.
 */
fun ComposeRuleScope.assertTagIsNotDisplayed(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertTagIsNotDisplayed: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is not displayed: $testTag", testTag) {
        this.waitUntil {
            logger.debugStep("Asserting tag $testTag is not displayed")
            composeRule.onNodeWithTag(testTag, useUnmergedTree).assertIsNotDisplayed()
        }
    }
    logger.debugStep("assertTagIsNotDisplayed completed for tag: $testTag")
}

/**
 * Robustly asserts that a node identified by its test tag is enabled.
 *
 * Leverages [runRobustly] to ensure the node is scrolled into view and visible 
 * before checking its enabled state.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is not enabled or not found.
 */
fun ComposeRuleScope.assertIsEnabled(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsEnabled: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is enabled: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsEnabled()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsEnabled()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsEnabled completed for tag: $testTag")
}

/**
 * Robustly asserts that a node identified by its test tag is disabled.
 *
 * Leverages [runRobustly] to ensure the node is scrolled into view and visible 
 * before checking its disabled state.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is enabled or not found.
 */
fun ComposeRuleScope.assertIsDisabled(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsDisabled: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is disabled: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            val matcher = isNotEnabled()
            try {
                node.assert(matcher)
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assert(matcher)
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsDisabled completed for tag: $testTag")
}

/**
 * Robustly asserts that a node identified by its test tag has focus.
 *
 * Leverages [runRobustly] to ensure the node is interactive before checking focus.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node does not have focus or is not found.
 */
fun ComposeRuleScope.assertIsFocused(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsFocused: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is focused: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsFocused()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsFocused()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsFocused completed for tag: $testTag")
}

/**
 * Robustly asserts that a node identified by its test tag does not have focus.
 *
 * Leverages [runRobustly] and ensures visibility before check.
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node has focus or is not found.
 */
fun ComposeRuleScope.assertIsNotFocused(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsNotFocused: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is not focused: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsNotFocused()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsNotFocused()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsNotFocused completed for tag: $testTag")
}

/**
 * Robustly asserts that a node identified by its test tag is currently selected.
 *
 * Useful for tabs, radio buttons, or selection lists. Leverages [runRobustly].
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is not selected or not found.
 */
fun ComposeRuleScope.assertIsSelected(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsSelected: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is selected: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsSelected()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsSelected()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsSelected completed for tag: $testTag")
}

/**
 * Robustly asserts that a node identified by its test tag is NOT selected.
 *
 * Leverages [runRobustly].
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is selected or not found.
 */
fun ComposeRuleScope.assertIsNotSelected(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsNotSelected: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is not selected: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsNotSelected()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsNotSelected()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsNotSelected completed for tag: $testTag")
}

/**
 * Robustly asserts that a toggleable node (like a Switch) is in the ON state.
 *
 * Leverages [runRobustly].
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is OFF or not found.
 */
fun ComposeRuleScope.assertIsOn(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsOn: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is ON: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsOn()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsOn()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsOn completed for tag: $testTag")
}

/**
 * Robustly asserts that a toggleable node (like a Switch) is in the OFF state.
 *
 * Leverages [runRobustly].
 *
 * @param testTag The unique identifier for the UI element.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the node is ON or not found.
 */
fun ComposeRuleScope.assertIsOff(testTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertIsOff: testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert tag is OFF: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertIsOff()
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertIsOff()
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertIsOff completed for tag: $testTag")
}

/**
 * Robustly asserts that a node's text matches exactly the [expectedText].
 *
 * Leverages [runRobustly] and performs automatic scrolling to the node.
 *
 * @param testTag The unique identifier for the UI element.
 * @param expectedText The exact string to match against.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the text does not match or node is not found.
 */
fun ComposeRuleScope.assertTextEquals(testTag: String, expectedText: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertTextEquals: expectedText='$expectedText', testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert text equals '$expectedText' in tag: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertTextEquals(expectedText)
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertTextEquals(expectedText)
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertTextEquals completed for tag: $testTag")
}

/**
 * Robustly asserts that a node's text contains the specified [substring].
 *
 * Leverages [runRobustly] and performs automatic scrolling to the node.
 *
 * @param testTag The unique identifier for the UI element.
 * @param substring The partial string expected to be present.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the substring is not found or node is not found.
 */
fun ComposeRuleScope.assertTextContains(testTag: String, substring: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertTextContains: substring='$substring', testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert text contains '$substring' in tag: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertTextContains(substring)
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertTextContains(substring)
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertTextContains completed for tag: $testTag")
}

/**
 * Robustly asserts that a node's state value (e.g., ProgressBar value) equals 
 * [expectedValue].
 *
 * Leverages [runRobustly].
 *
 * @param testTag The unique identifier for the UI element.
 * @param expectedValue The expected state value as a string.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the values do not match or node is not found.
 */
fun ComposeRuleScope.assertValueEquals(testTag: String, expectedValue: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertValueEquals: expectedValue='$expectedValue', testTag=$testTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert value equals '$expectedValue' in tag: $testTag", testTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree)
            try {
                node.assertValueEquals(expectedValue)
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $testTag")
                    node.performScrollTo()
                    node.assertValueEquals(expectedValue)
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertValueEquals completed for tag: $testTag")
}

/**
 * Robustly asserts that a parent node contains at least one child node identified 
 * by [childTag].
 *
 * Useful for verifying list content or composite components. Leverages [runRobustly].
 *
 * @param parentTag The test tag of the parent container.
 * @param childTag The test tag of the expected child.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the child is not found under the parent.
 */
fun ComposeRuleScope.assertHasChild(parentTag: String, childTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertHasChild: parentTag=$parentTag, childTag=$childTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert parent $parentTag has child $childTag", parentTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(parentTag, useUnmergedTree)
            val matcher = hasAnyChild(hasTestTag(childTag))
            try {
                node.assert(matcher)
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $parentTag")
                    node.performScrollTo()
                    node.assert(matcher)
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertHasChild completed for parentTag: $parentTag")
}

/**
 * Robustly asserts that a node identified by [childTag] has a parent node 
 * identified by [parentTag].
 *
 * Leverages [runRobustly].
 *
 * @param childTag The test tag of the child node.
 * @param parentTag The test tag of the expected parent.
 * @param useUnmergedTree Whether to use the unmerged semantics tree for lookup.
 * @throws AssertionError if the parent relationship is not found.
 */
fun ComposeRuleScope.assertHasParent(childTag: String, parentTag: String, useUnmergedTree: Boolean = false) {
    logger.infoStep("Starting assertHasParent: childTag=$childTag, parentTag=$parentTag, useUnmergedTree=$useUnmergedTree")
    runRobustly("Assert child $childTag has parent $parentTag", childTag) {
        this.waitUntil {
            val node = composeRule.onNodeWithTag(childTag, useUnmergedTree)
            val matcher = hasParent(hasTestTag(parentTag))
            try {
                node.assert(matcher)
            } catch (e: AssertionError) {
                try {
                    logger.debugStep("Attempting to scroll to tag: $childTag")
                    node.performScrollTo()
                    node.assert(matcher)
                } catch (_: AssertionError) {
                    throw e
                }
            }
        }
    }
    logger.debugStep("assertHasParent completed for childTag: $childTag")
}

