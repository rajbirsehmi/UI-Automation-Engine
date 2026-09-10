package com.sehmi.engine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sehmi.engine.assertions.*
import com.sehmi.engine.actions.*
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.runRobustly
import com.sehmi.engine.utils.waitUntil
import kotlinx.coroutines.delay
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RobustnessContractTest : ComposeRuleScope {

    @get:Rule
    val rule = UiEngine.createRule()

    override val composeRule get() = rule

    @Test
    fun testWaitUntilSucceedsAfterRetry() {
        var counter = 0
        rule.setContent {
            Box(Modifier.size(10.dp).testTag("box"))
        }

        val result = waitUntil(timeoutMillis = 2000L) {
            counter++
            if (counter < 3) throw IllegalStateException("Not ready")
            "Success"
        }

        assertEquals("Success", result)
        assertEquals(3, counter)
    }

    @Test
    fun testWaitUntilTimeoutsCorrectly() {
        assertThrows(IllegalStateException::class.java) {
            waitUntil(timeoutMillis = 500L) {
                throw IllegalStateException("Always failing")
            }
        }
    }

    @Test
    fun testRunRobustlyEnrichesErrorMessage() {
        rule.setContent {
            Box(Modifier.size(10.dp).testTag("target"))
        }

        val error = assertThrows(AssertionError::class.java) {
            runRobustly("Failing Action", tag = "target") {
                throw RuntimeException("Core Error")
            }
        }

        assertTrue(error.message!!.contains("Automation Failure: Failing Action"))
        assertTrue(error.message!!.contains("Target Tag: target"))
        assertTrue(error.message!!.contains("Artifact: FAILURE_"))
        assertTrue(error.message!!.contains("Original Error: Core Error"))
    }

    @Test
    fun testVisibilityAwareScrollingSkipsScrollIfVisible() {
        // This is a behavioral test to ensure no crash occurs when 
        // the node is already visible and we call a robust assertion.
        rule.setContent {
            Text("Visible Text", modifier = Modifier.testTag("visible_tag"))
        }

        // Should not throw even if there's no scroll parent
        assertTagDisplayed("visible_tag")
        assertTextEquals("visible_tag", "Visible Text")
    }

    @Test
    fun testConfigurationTimeoutIsRespected() {
        val customTimeout = 1000L
        UiEngine.configure(
            UiEngine.Configuration(
                defaultTimeoutMillis = customTimeout,
                autoCaptureScreenshots = false,
                autoDumpSemantics = false,
            )
        )

        assertEquals(customTimeout, UiEngine.config.defaultTimeoutMillis)

        val startTime = System.currentTimeMillis()
        assertThrows(IllegalStateException::class.java) {
            // Use a low-level waitUntil to avoid the overhead of runRobustly and waitForIdle
            waitUntil(timeoutMillis = customTimeout) {
                throw IllegalStateException("Failing fast")
            }
        }
        val duration = System.currentTimeMillis() - startTime

        // Should be at least the timeout, but not significantly more
        assertTrue("Duration was $duration", duration >= customTimeout)
        assertTrue("Duration was $duration", duration < (customTimeout + 2000L))
        
        // Reset configuration to default
        UiEngine.configure(UiEngine.Configuration())
    }

    @Test
    fun testNestedRobustnessDoesNotCrash() {
        rule.setContent {
            Box(Modifier.size(100.dp).testTag("parent")) {
                Box(Modifier.size(10.dp).testTag("child"))
            }
        }

        // This triggers a nested runRobustly call internally
        runRobustly("Outer block") {
            assertTagDisplayed("parent")
            assertHasChild("parent", "child")
        }
    }

    @Test
    fun testWithPausedClockIsolation() {
        assertTrue(rule.mainClock.autoAdvance)
        
        withPausedClock {
            assertFalse(rule.mainClock.autoAdvance)
        }
        
        assertTrue(rule.mainClock.autoAdvance)
    }
}
