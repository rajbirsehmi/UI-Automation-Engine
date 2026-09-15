package com.sehmi.engine.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.visitor.AbstractUastVisitor

/**
 * Detects missing setup for [UiTestEngine] when [UiTestEngine.withRobot] is used.
 */
class UiTestEngineSetupDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("withRobot")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        val containingClass = method.containingClass?.qualifiedName ?: ""
        if (containingClass != "com.sehmi.engine.UiTestEngine") return

        val uClass = node.getParentOfType<UClass>() ?: return
        
        var hasSetComposeRule = false
        var hasUiTestEngineRule = false

        uClass.accept(object : AbstractUastVisitor() {
            override fun visitCallExpression(node: UCallExpression): Boolean {
                val resolvedMethod = node.resolve()
                if (resolvedMethod?.name == "setComposeRule" && 
                    resolvedMethod.containingClass?.qualifiedName == "com.sehmi.engine.UiTestEngine") {
                    hasSetComposeRule = true
                }
                return super.visitCallExpression(node)
            }

            override fun visitVariable(node: UVariable): Boolean {
                val type = node.type.canonicalText
                if (type == "com.sehmi.engine.junit.UiTestEngineRule") {
                    hasUiTestEngineRule = true
                }
                return super.visitVariable(node)
            }
        })

        if (!hasSetComposeRule && !hasUiTestEngineRule) {
            context.report(
                ISSUE,
                node,
                context.getLocation(node),
                "Using `UiTestEngine.withRobot` requires setting the ComposeRule. " +
                "Add `@get:Rule val engineRule = UiTestEngineRule(composeRule)` to your test class."
            )
        }
    }

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "MissingUiTestEngineSetup",
            briefDescription = "UiTestEngine.withRobot used without setup",
            explanation = """
                When using `UiTestEngine.withRobot`, you must ensure that the `ComposeTestRule` 
                is registered with the engine. 
                
                You can do this by adding a `UiTestEngineRule` to your test class:
                `@get:Rule val engineRule = UiTestEngineRule(composeRule)`
                
                Or by manually calling `UiTestEngine.setComposeRule(composeRule)` in your `@Before` method.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 9,
            severity = Severity.ERROR,
            implementation = Implementation(
                UiTestEngineSetupDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
