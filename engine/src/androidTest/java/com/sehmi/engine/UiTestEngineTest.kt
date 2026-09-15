package com.sehmi.engine

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sehmi.engine.assertions.assertTagDisplayed
import com.sehmi.engine.core.ComposeRuleScope
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class MockRobot : ComposeRuleScope {
    fun checkText() {
        assertTagDisplayed("test_tag")
    }
}

@RunWith(AndroidJUnit4::class)
class UiTestEngineTest {

    @get:Rule
    val rule = UiTestEngine.createRule()

    @Test
    fun testUiTestEngineWithRobot() {
        rule.setContent {
            Text("Hello", modifier = Modifier.testTag("test_tag"))
        }

        UiTestEngine.withRobot(MockRobot()) {
            checkText()
        }
    }
}
