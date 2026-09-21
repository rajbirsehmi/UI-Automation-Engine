package com.sehmi.engine.conditions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sehmi.engine.UiTestEngine
import com.sehmi.engine.core.ComposeRuleScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NodeConditionsTest {

    @get:Rule
    val engineRule = UiTestEngine.createRule()

    @Composable
    private fun TestContent() {
        Column {
            Text("Hello World", modifier = Modifier.testTag("text_tag"))
            Button(onClick = {}, modifier = Modifier.testTag("button_tag"), enabled = true) {
                Text("Enabled Button")
            }
            Button(onClick = {}, modifier = Modifier.testTag("disabled_button_tag"), enabled = false) {
                Text("Disabled Button")
            }
            Checkbox(checked = true, onCheckedChange = {}, modifier = Modifier.testTag("checked_tag"))
            Checkbox(checked = false, onCheckedChange = {}, modifier = Modifier.testTag("unchecked_tag"))
            Switch(checked = true, onCheckedChange = {}, modifier = Modifier.testTag("switch_on"))
        }
    }

    @Test
    fun hasTag_returnsCorrectBoolean() {
        engineRule.setContent { TestContent() }

        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            assertTrue(hasTag("text_tag"))
            assertTrue(hasTag("button_tag"))
            assertFalse(hasTag("non_existent"))
        }
    }

    @Test
    fun isDisplayed_returnsCorrectBoolean() {
        engineRule.setContent { TestContent() }

        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            assertTrue(isDisplayed("text_tag"))
            assertFalse(isDisplayed("non_existent"))
        }
    }

    @Test
    fun isEnabled_returnsCorrectBoolean() {
        engineRule.setContent { TestContent() }

        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            assertTrue(isEnabled("button_tag"))
            assertFalse(isEnabled("disabled_button_tag"))
            assertFalse(isEnabled("non_existent"))
        }
    }

    @Test
    fun isChecked_returnsCorrectBoolean() {
        engineRule.setContent { TestContent() }

        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            assertTrue(isChecked("checked_tag"))
            assertFalse(isChecked("unchecked_tag"))
            assertTrue(isChecked("switch_on"))
            assertFalse(isChecked("non_existent"))
        }
    }

    @Test
    fun hasText_returnsCorrectBoolean() {
        engineRule.setContent { TestContent() }

        UiTestEngine.withRobot(object : ComposeRuleScope {}) {
            assertTrue(hasText("text_tag", "Hello World"))
            assertTrue(hasText("text_tag", "Hello", substring = true))
            assertFalse(hasText("text_tag", "Goodbye"))
            assertFalse(hasText("non_existent", "Hello"))
        }
    }
}
