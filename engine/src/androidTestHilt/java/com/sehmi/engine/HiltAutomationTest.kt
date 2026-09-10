package com.sehmi.engine

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sehmi.engine.assertions.assertTagDisplayed
import com.sehmi.engine.core.ComposeRuleScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyTestDependency @Inject constructor() {
    val message = "Hilt Dependency Workings"
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MyTestEntryPoint {
    fun getDependency(): MyTestDependency
}

class HiltMockRobot : ComposeRuleScope {
    private val entryPoint: MyTestEntryPoint by UiEngine.getTestEntryPoint()

    fun verifyDependency() {
        assertEquals("Hilt Dependency Workings", entryPoint.getDependency().message)
    }

    fun checkUi() {
        assertTagDisplayed("hilt_text")
    }
}

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltAutomationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val rule = UiEngine.createHiltRule(ComponentActivity::class.java)

    @Test
    fun testHiltRuleAndEntryPoint() {
        hiltRule.inject()

        rule.setContent {
            Text("Hilt Test", modifier = Modifier.testTag("hilt_text"))
        }

        UiEngine.withRobot(HiltMockRobot()) {
            verifyDependency()
            checkUi()
        }
    }
}
