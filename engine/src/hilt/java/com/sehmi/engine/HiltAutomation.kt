package com.sehmi.engine

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.EntryPoints
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.reflect.KProperty

/**
 * A [ComposeTestRule] wrapper that integrates with Hilt and the UI Automation Engine.
 */
class HiltAutomationComposeTestRule<A : ComponentActivity>(
    private val uiTestEngineRule: AndroidComposeTestRule<*, A>,
) : ComposeContentTestRule by uiTestEngineRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                UiTestEngine.setComposeRule(uiTestEngineRule)
                try {
                    uiTestEngineRule.apply(base, description).evaluate()
                } finally {
                    UiTestEngine.clearComposeRule()
                }
            }
        }
    }
}

/**
 * Creates a [ComposeContentTestRule] that integrates Hilt injection with the UI Automation Engine.
 *
 * @param activityClass The Activity class to launch for the test.
 */
@Suppress("UNUSED_PARAMETER", "DEPRECATION")
fun <A : ComponentActivity> UiTestEngine.createHiltRule(
    activityClass: Class<A>
): ComposeContentTestRule {
    val uiTestEngineRule = createAndroidComposeRule(activityClass)
    return HiltAutomationComposeTestRule(uiTestEngineRule)
}

/**
 * A delegate that provides access to Hilt-injected singletons (entry points) within a test or robot.
 */
class HiltEntryPointDelegate<T>(private val entryPointClass: Class<T>) {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            value = EntryPoints.get(context, entryPointClass)
        }
        return value!!
    }
}

/**
 * Provides access to Hilt-injected singletons (entry points) within a test or robot.
 *
 * Example:
 * ```
 * val viewModel: MyViewModel by UiTestEngine.getTestEntryPoint()
 * ```
 */
inline fun <reified T> UiTestEngine.getTestEntryPoint(): HiltEntryPointDelegate<T> {
    return HiltEntryPointDelegate(T::class.java)
}
