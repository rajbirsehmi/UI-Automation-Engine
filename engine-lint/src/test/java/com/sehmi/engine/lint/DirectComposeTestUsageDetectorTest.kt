package com.sehmi.engine.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

class DirectComposeTestUsageDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = DirectComposeTestUsageDetector()

    override fun getIssues(): List<Issue> = listOf(DirectComposeTestUsageDetector.ISSUE)

    override fun lint(): TestLintTask {
        return super.lint()
            .allowMissingSdk(true)
            .allowCompilationErrors()
            .testModes(TestMode.DEFAULT)
    }

    private val COMPOSE_TEST_STUBS: TestFile = kotlin("""
        package androidx.compose.ui.test
        fun onNodeWithTag(tag: String): SemanticsNodeInteraction = TODO()
        fun SemanticsNodeInteraction.performClick(): SemanticsNodeInteraction = TODO()
        fun SemanticsNodeInteraction.assertIsDisplayed(): SemanticsNodeInteraction = TODO()
        class SemanticsNodeInteraction
    """).indented()

    private val AUTOMATOR_TEST_STUBS: TestFile = kotlin("""
        package androidx.test.uiautomator
        class UiDevice {
            fun findObject(selector: Any): Any = TODO()
            fun pressBack(): Boolean = TODO()
        }
    """).indented()

    private val EXPECTED_MSG = "UI automation actions must use the high-level com.sehmi.engine DSL extensions instead of direct testing APIs to ensure robustness and diagnostic capture."

    fun testDirectPerformClick() {
        lint().files(
            COMPOSE_TEST_STUBS,
            kotlin("""
                package com.example.test
                import androidx.compose.ui.test.onNodeWithTag
                import androidx.compose.ui.test.performClick

                class MyTest {
                    fun test() {
                        onNodeWithTag("tag").performClick()
                    }
                }
            """).indented()
        )
        .run()
        .expect("""
            src/com/example/test/MyTest.kt:7: Error: $EXPECTED_MSG [DirectUiTestApiUsage]
                    onNodeWithTag("tag").performClick()
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 errors, 0 warnings
        """)
        .expectFixDiffs("""
            Fix for src/com/example/test/MyTest.kt line 7: Replace with clickOnTag(...):
            @@ -7 +7 @@
            -        onNodeWithTag("tag").performClick()
            +        clickOnTag("tag")
        """.trimIndent())
    }

    fun testAllowedInEngine() {
        lint().files(
            COMPOSE_TEST_STUBS,
            kotlin("""
                package com.sehmi.engine.advanced
                import androidx.compose.ui.test.onNodeWithTag
                import androidx.compose.ui.test.performClick

                class InternalEngineCode {
                    fun execute() {
                        onNodeWithTag("tag").performClick()
                    }
                }
            """).indented()
        )
        .run()
        .expectClean()
    }

    fun testDirectAutomator() {
        lint().files(
            AUTOMATOR_TEST_STUBS,
            kotlin("""
                package com.example.test
                import androidx.test.uiautomator.UiDevice

                class MyTest {
                    fun test(device: UiDevice) {
                        device.findObject(null)
                    }
                }
            """).indented()
        )
        .run()
        .expect("""
            src/com/example/test/MyTest.kt:6: Error: $EXPECTED_MSG [DirectUiTestApiUsage]
                    device.findObject(null)
                    ~~~~~~~~~~~~~~~~~~~~~~~
            1 errors, 0 warnings
        """)
    }
}
