package com.sehmi.engine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.runRobustly
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DiagnosticsTest : ComposeRuleScope {

    @get:Rule
    val rule = UiTestEngine.createRule()

    override val uiTestEngineRule get() = rule

    @Test
    fun testDiagnosticsAreCapturedOnFailure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val customDir = File(context.cacheDir, "diag_test").absolutePath
        
        UiTestEngine.configure(
            UiTestEngine.Configuration(
                screenshotDirectory = customDir,
                autoCaptureScreenshots = true,
                autoCaptureLogcat = true,
                autoCaptureViewHierarchy = true,
                autoGenerateHtmlReport = true,
                autoDumpSemantics = false
            )
        )

        rule.setContent {
            Box(Modifier.size(10.dp).testTag("target"))
        }

        val error = assertThrows(AssertionError::class.java) {
            runRobustly("Failing Action") {
                throw RuntimeException("Trigger Diagnostics")
            }
        }

        // Verify error message formatting
        assertTrue("Error message should contain Artifacts. Message: ${error.message}", 
            error.message!!.contains("Artifacts:"))
        assertTrue(error.message!!.contains("Screenshot:"))
        assertTrue(error.message!!.contains("Logcat:"))
        assertTrue(error.message!!.contains("Hierarchy:"))
        assertTrue(error.message!!.contains("HTML Report:"))

        val dir = File(customDir)
        assertTrue("Directory should be created", dir.exists())
        val files = dir.listFiles()
        assertNotNull("Files list should not be null", files)
        
        assertTrue("Screenshot should exist", files!!.any { it.extension == "png" })
        assertTrue("Logcat should exist", files.any { it.extension == "log" })
        assertTrue("Hierarchy XML should exist", files.any { it.extension == "xml" })
        assertTrue("HTML report should exist", files.any { it.extension == "html" })
        
        // Check logcat content (at least non-empty)
        val logFile = files.find { it.extension == "log" }!!
        assertTrue("Logcat file should not be empty", logFile.length() > 0)

        // Check HTML report content
        val htmlFile = files.find { it.extension == "html" }!!
        val htmlContent = htmlFile.readText()
        assertTrue("HTML report should contain title", htmlContent.contains("Test Failure Report"))
        assertTrue("HTML report should contain failure screenshot section", htmlContent.contains("Failure Screenshot"))
        assertTrue("HTML report should contain error message", htmlContent.contains("Trigger Diagnostics"))

        // Cleanup
        dir.deleteRecursively()
        UiTestEngine.configure(UiTestEngine.Configuration())
    }
}
