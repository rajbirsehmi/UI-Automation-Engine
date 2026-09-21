package com.sehmi.engine

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sehmi.engine.actions.*
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.core.onDevice
import com.sehmi.engine.matchers.SemanticsMatchers.hasAnyDescendant
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag

@RunWith(AndroidJUnit4::class)
class OverhaulTest {

    @get:Rule
    val engineRule = UiTestEngine.createRule()

    @Composable
    private fun TestContent() {
        Column(modifier = Modifier.testTag("parent")) {
            Text("Child 1", modifier = Modifier.testTag("child1"))
            Button(onClick = {}, modifier = Modifier.testTag("child2")) {
                Text("Button Content")
            }
            Text("Child 3", modifier = Modifier.testTag("child3"))
        }
    }

    @Test
    fun testHierarchyFinders() {
        engineRule.setContent { TestContent() }

        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            // Test clickFirstChild
            clickFirstChild("parent") // Clicks child1 (Text) - no-op click but verifies lookup
            
            // Test clickLastChild
            clickLastChild("parent") // Clicks child3
            
            // Test clickChildAtIndex
            clickChildAtIndex("parent", 1) // Clicks button
            
            // Test relationship matcher
            assertTrue(hasAnyDescendant(hasText("Button Content")).matches(engineRule.onNodeWithTag("parent").fetchSemanticsNode()))
        }
    }

    @Test
    fun testOnDeviceScope() {
        // This test interacts with the device, so we just verify it doesn't crash 
        // and can access basic properties.
        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            onDevice {
                device.waitForIdle()
                assertTrue(timeoutMillis > 0)
                // We won't actually press hardware keys to avoid disrupting the test environment 
                // but we verify the scope is functional.
                executeShell("echo success")
            }
        }
    }
}
