package com.sehmi.engine

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sehmi.engine.assertions.assertTagDisplayed
import com.sehmi.engine.core.ComposeRuleScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.io.File
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
    private val entryPoint: MyTestEntryPoint by UiTestEngine.getTestEntryPoint()

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
    val rule = UiTestEngine.createHiltRule(ComponentActivity::class.java)

    @Test
    fun testHiltRuleAndEntryPoint() {
        hiltRule.inject()

        rule.setContent {
            Text("Hilt Test", modifier = Modifier.testTag("hilt_text"))
        }

        UiTestEngine.withRobot(HiltMockRobot()) {
            verifyDependency()
            checkUi()
        }
    }

    @Test
    fun testHiltFailureCapturesDiagnostics() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val customDir = File(context.cacheDir, "hilt_diag_test").absolutePath
        
        UiTestEngine.configure(
            UiTestEngine.Configuration(
                screenshotDirectory = customDir,
                autoCaptureScreenshots = true,
                autoDumpSemantics = false
            )
        )

        val dir = File(customDir)
        dir.deleteRecursively()
        dir.mkdirs()

        val watcher = FailureDiagnosticWatcher()
        watcher.starting(Description.createTestDescription(this.javaClass, "hiltMockTest"))
        
        watcher.failed(RuntimeException("Hilt Failure"), Description.createTestDescription(this.javaClass, "hiltMockTest"))
        
        assertTrue("Diagnostics flag should be set", UiTestEngine.wasDiagnosticsCaptured)
        val files = dir.listFiles()
        assertTrue("Screenshot should exist in hilt diag dir", 
            files?.any { it.name.contains("GLOBAL_FAILURE_") && it.name.endsWith(".png") } == true)
        
        dir.deleteRecursively()
        UiTestEngine.configure(UiTestEngine.Configuration())
    }
}
