package com.sehmi.engine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sehmi.engine.actions.*
import com.sehmi.engine.core.ComposeRuleScope
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Technical contract tests for the UI Automation Engine.
 * 
 * These tests use isolated Compose components to verify that the engine's 
 * core logic, multi-touch math, and failure handling work correctly without 
 * requiring a host application module.
 */
@RunWith(AndroidJUnit4::class)
class EngineContractTest : ComposeRuleScope {

    @get:Rule
    override val composeRule = createComposeRule()

    @Test
    fun testAccessibilityAuditFailsOnMissingLabels() {
        composeRule.setContent {
            Column(Modifier.testTag("root")) {
                Button(onClick = {}, modifier = Modifier.testTag("valid_button")) {
                    Text("Click Me")
                }
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { }
                        .testTag("invalid_box"),
                )
            }
        }

        assertThrows(AssertionError::class.java) {
            assertInteractiveNodesHaveLabels()
        }
    }

    @Test
    fun testRotationGestureSafety() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .testTag("rotatable_box")
            )
        }

        // Verifies math execution doesn't crash the engine
        rotate("rotatable_box", 90f)
    }

    @Test
    fun testMultiFingerSwipeSafety() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .testTag("swipe_box")
            )
        }

        multiFingerSwipe("swipe_box", fingers = 3, direction = Direction.UP)
    }

    @Test
    fun testClickAtOffsetBoundaries() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { }
                    .testTag("box")
            )
        }

        // Test corners and center
        clickAtOffset("box", 0f, 0f)
        clickAtOffset("box", 1f, 1f)
        clickAtOffset("box", 0.5f, 0.5f)
    }

    @Test
    fun testSwipeUntilVisibleFailure() {
        composeRule.setContent {
            Box(Modifier.size(100.dp).testTag("root"))
        }

        assertThrows(AssertionError::class.java) {
            swipeUntilVisible("never_appearing_tag", Direction.UP, maxSwipes = 2)
        }
    }

    @Test
    fun testDragAndDropSafety() {
        composeRule.setContent {
            Column {
                Box(Modifier.size(50.dp).testTag("source"))
                Box(Modifier.size(50.dp).testTag("target"))
            }
        }

        dragAndDrop("source", "target")
    }
}
