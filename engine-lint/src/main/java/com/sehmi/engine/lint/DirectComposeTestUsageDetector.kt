package com.sehmi.engine.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*

class DirectComposeTestUsageDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf(
        // Compose
        "onNodeWithTag", "onNodeWithText", "onNodeWithContentDescription",
        "performClick", "performTextInput", "performScrollTo",
        "assertIsDisplayed", "assertExists", "assertDoesNotExist",
        // UI Automator
        "findObject", "wait", "pressBack", "pressHome"
    )

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val file = node.getContainingUFile() ?: return
        val packageName = file.packageName
        
        // Allowed in com.sehmi.engine.* package (the engine itself)
        if (packageName.startsWith("com.sehmi.engine")) return

        // Detect forbidden usage in test sources.
        if (!context.isTestSource && !context.file.path.contains("test", ignoreCase = true)) return

        val methodName = method.name
        val evaluator = context.evaluator
        
        val isComposeTestApi = (evaluator.getPackage(method)?.qualifiedName?.startsWith("androidx.compose.ui.test") == true) ||
                               (method.containingClass?.qualifiedName?.startsWith("androidx.compose.ui.test") == true)
        
        val isAutomatorApi = (evaluator.getPackage(method)?.qualifiedName?.startsWith("androidx.test.uiautomator") == true) ||
                              (method.containingClass?.qualifiedName?.startsWith("androidx.test.uiautomator") == true)

        if (isComposeTestApi || isAutomatorApi) {
            val parent = node.uastParent
            if (parent is UQualifiedReferenceExpression && parent.receiver == node) {
                // Skip receivers, wait for selectors to handle the whole chain
                return
            }
            
            var reportNode: UElement = node
            if (parent is UQualifiedReferenceExpression && parent.selector == node) {
                reportNode = parent
            }

            val fix = createFix(reportNode, methodName)
            
            context.report(
                ISSUE,
                reportNode,
                context.getLocation(reportNode),
                "UI automation actions must use the high-level com.sehmi.engine DSL extensions instead of direct testing APIs to ensure robustness and diagnostic capture.",
                fix,
            )
        }
    }

    private fun createFix(node: UElement, methodName: String): LintFix? {
        val source = node.sourcePsi?.text ?: return null
        
        return when (methodName) {
            "performClick" -> {
                val tagMatch = Regex("onNodeWithTag\\s*\\(\\s*([^)]*)\\s*\\)").find(source)
                val tag = tagMatch?.groupValues?.get(1)
                if (tag != null) {
                    LintFix.create()
                        .name("Replace with clickOnTag(...)")
                        .replace()
                        .all()
                        .with("clickOnTag($tag)")
                        .build()
                } else null
            }
            "assertIsDisplayed" -> {
                val tagMatch = Regex("onNodeWithTag\\s*\\(\\s*([^)]*)\\s*\\)").find(source)
                val tag = tagMatch?.groupValues?.get(1)
                if (tag != null) {
                    LintFix.create()
                        .name("Replace with assertTagDisplayed(...)")
                        .replace()
                        .all()
                        .with("assertTagDisplayed($tag)")
                        .build()
                } else null
            }
            "findObject" -> {
                LintFix.create()
                    .name("Wrap with onDevice { ... }")
                    .replace()
                    .all()
                    .with("onDevice { $source }")
                    .build()
            }
            else -> null
        }
    }

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "DirectUiTestApiUsage",
            briefDescription = "Forbidden direct usage of Compose or UI Automator testing APIs",
            explanation = "UI automation actions must use the high-level com.sehmi.engine DSL extensions instead of direct testing APIs to ensure robustness and diagnostic capture.",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                DirectComposeTestUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
