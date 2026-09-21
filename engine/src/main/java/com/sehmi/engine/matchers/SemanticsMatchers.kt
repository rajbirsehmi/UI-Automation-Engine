package com.sehmi.engine.matchers

import android.util.Log
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
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
     * Matches a semantics node that is a Dialog.
     */
    fun isDialog(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.IsDialog, Unit)

    /**
     * Matches a semantics node that is a Popup.
     */
    fun isPopup(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.IsPopup, Unit)

    /**
     * Matches a semantics node that has a "Heading" property.
     */
    fun isHeading(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit)

    /**
     * Matches a semantics node with a specific state description.
     */
    fun hasStateDescription(text: String): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, text)

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

    /**
     * Matches a node that has at least one descendant matching the given [matcher].
     */
    fun hasAnyDescendant(matcher: SemanticsMatcher): SemanticsMatcher {
        return androidx.compose.ui.test.hasAnyDescendant(matcher)
    }

    /**
     * Matches a node that has at least one ancestor matching the given [matcher].
     */
    fun hasAnyAncestor(matcher: SemanticsMatcher): SemanticsMatcher {
        return androidx.compose.ui.test.hasAnyAncestor(matcher)
    }

    /**
     * Matches a node that has a sibling matching the given [matcher].
     */
    fun hasAnySibling(matcher: SemanticsMatcher): SemanticsMatcher {
        return androidx.compose.ui.test.hasAnySibling(matcher)
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
            val node = uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree = true)
            node.printToLog(tag)
        } else {
            uiTestEngineRule.onRoot(useUnmergedTree = true).printToLog(tag)
        }
    } catch (e: Throwable) {
        Log.e(tag, "Failed to print semantics tree: ${e.message}")
    }
}

/**
 * Returns the unmerged semantics tree as a formatted string.
 *
 * @param testTag Optional tag to focus the dump on a specific subtree.
 * @return The semantics tree as a string, or an error message if capture fails.
 */
fun ComposeRuleScope.dumpSemantics(testTag: String? = null): String {
    return try {
        if (testTag != null) {
            uiTestEngineRule.onNodeWithTag(testTag, useUnmergedTree = true).printToString()
        } else {
            uiTestEngineRule.onRoot(useUnmergedTree = true).printToString()
        }
    } catch (e: Throwable) {
        "Failed to dump semantics: ${e.message}"
    }
}
