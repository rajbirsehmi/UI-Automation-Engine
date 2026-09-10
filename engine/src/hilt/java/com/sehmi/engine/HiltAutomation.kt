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
    private val composeRule: AndroidComposeTestRule<*, A>,
) : ComposeContentTestRule by composeRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                UiEngine.setComposeRule(composeRule)
                try {
                    composeRule.apply(base, description).evaluate()
                } finally {
                    UiEngine.clearComposeRule()
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
fun <A : ComponentActivity> UiEngine.createHiltRule(
    activityClass: Class<A>
): ComposeContentTestRule {
    val composeRule = createAndroidComposeRule(activityClass)
    return HiltAutomationComposeTestRule(composeRule)
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
 * val viewModel: MyViewModel by UiEngine.getTestEntryPoint()
 * ```
 */
inline fun <reified T> UiEngine.getTestEntryPoint(): HiltEntryPointDelegate<T> {
    return HiltEntryPointDelegate(T::class.java)
}
