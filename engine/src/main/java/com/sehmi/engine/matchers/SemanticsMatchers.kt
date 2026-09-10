package com.sehmi.engine.matchers

import android.util.Log
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*

/**
 * A collection of custom [SemanticsMatcher] implementations for advanced UI matching.
 *
 * These matchers extend the standard Compose testing library to support roles 
 * and regex-based content description matching.
 */
object SemanticsMatchers {

    /**
     * Matches a semantics node with a specific accessibility [Role].
     *
     * Useful for disambiguating nodes that might have similar text but different 
     * roles (e.g., a "Submit" Button vs. a "Submit" Text).
     *
     * @param role The [Role] to match against (e.g., Role.Button, Role.Checkbox).
     * @return A [SemanticsMatcher] for the specified role.
     */
    fun hasRole(role: Role): SemanticsMatcher {
        return SemanticsMatcher.expectValue(SemanticsProperties.Role, role)
    }

    /**
     * A specialized matcher that checks if a node has the [Role.Button] role.
     */
    fun isButton(): SemanticsMatcher = hasRole(Role.Button)

    /**
     * A specialized matcher that checks if a node has the [Role.Checkbox] role.
     */
    fun isCheckbox(): SemanticsMatcher = hasRole(Role.Checkbox)

    /**
     * A specialized matcher that checks if a node has the [Role.Switch] role.
     */
    fun isSwitch(): SemanticsMatcher = hasRole(Role.Switch)

    /**
     * A specialized matcher that checks if a node has the [Role.Tab] role.
     */
    fun isTab(): SemanticsMatcher = hasRole(Role.Tab)

    /**
     * Matches a semantics node whose content description satisfies the given [regex].
     *
     * Useful for matching nodes with dynamic or partially known descriptions.
     *
     * @param regex The regular expression string to match.
     * @return A [SemanticsMatcher] for the regex match.
     */
    fun hasContentDescriptionRegex(regex: String): SemanticsMatcher {
        return SemanticsMatcher("contentDescription matches regex $regex") { node ->
            val description = node.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }
            description.any { it.contains(Regex(regex)) }
        }
    }
}

/**
 * Dumps the unmerged semantics tree to the system log (Logcat) for debugging purposes.
 *
 * This utility is automatically invoked by the robust action pipeline 
 * when an assertion or action fails. It can also be called manually to inspect 
 * the UI hierarchy at any point in a test.
 *
 * @param testTag Optional tag to focus the dump on a specific subtree. If null, 
 *                the entire root tree is dumped.
 */
internal fun ComposeRuleScope.printUnmergedTree(testTag: String? = null) {
    val tag = "ComposeAutomation"
    try {
        if (testTag != null) {
            val node = composeRule.onNodeWithTag(testTag, useUnmergedTree = true)
            node.printToLog(tag)
        } else {
            composeRule.onRoot(useUnmergedTree = true).printToLog(tag)
        }
    } catch (e: Throwable) {
        Log.e(tag, "Failed to print semantics tree: ${e.message}")
    }
}
