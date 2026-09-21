package com.sehmi.engine.core

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.sehmi.engine.UiTestEngine

/**
 * Represents a scope for UI Automator device-level interactions.
 */
interface AutomatorScope {
    /**
     * The [UiDevice] instance for the current instrumentation.
     */
    val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /**
     * The default timeout for Automator operations.
     */
    val timeoutMillis: Long
        get() = UiTestEngine.config.automatorTimeoutMillis

    /**
     * Presses a physical hardware key on the device.
     */
    fun pressKey(keyCode: Int) {
        device.pressKeyCode(keyCode)
    }

    /**
     * Presses the home button.
     */
    fun pressHome() {
        device.pressHome()
    }

    /**
     * Presses the back button.
     */
    fun pressBack() {
        device.pressBack()
    }

    /**
     * Presses the recent apps button.
     */
    fun pressRecentApps() {
        device.pressRecentApps()
    }

    /**
     * Presses the DPad center button.
     */
    fun pressDPadCenter() {
        device.pressDPadCenter()
    }

    /**
     * Robustly executes a shell command on the device.
     */
    fun executeShell(command: String): String {
        return device.executeShellCommand(command)
    }

    /**
     * Finds a UI object by the given [selector].
     */
    fun findObject(selector: BySelector): UiObject2? {
        return device.findObject(selector)
    }

    /**
     * Robustly clicks an object matching the [selector].
     */
    fun click(selector: BySelector) {
        val obj = device.wait(Until.findObject(selector), timeoutMillis)
        obj?.click() ?: throw AssertionError("Object matching $selector not found after $timeoutMillis ms")
    }

    /**
     * Swipes from one point to another.
     */
    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, steps: Int = 10) {
        device.swipe(startX, startY, endX, endY, steps)
    }

    /**
     * Opens the quick settings panel.
     */
    fun openQuickSettings() {
        device.openQuickSettings()
    }

    /**
     * Wakes up the device screen.
     */
    fun wakeUp() {
        device.wakeUp()
    }

    /**
     * Puts the device to sleep.
     */
    fun sleep() {
        device.sleep()
    }

    /**
     * Locks the screen rotation.
     */
    fun freezeRotation() {
        device.freezeRotation()
    }

    /**
     * Unlocks the screen rotation.
     */
    fun unfreezeRotation() {
        device.unfreezeRotation()
    }

    /**
     * Clears all notifications from the shade.
     */
    fun clearNotifications() {
        device.openNotification()
        val clearAll = device.wait(Until.findObject(By.text("Clear all")), 2000L)
        clearAll?.click() ?: device.pressBack()
    }

    /**
     * Unlocks the device screen.
     */
    fun unlock() {
        wakeUp()
        executeShell("wm dismiss-keyguard")
    }

    /**
     * Sets the device orientation.
     */
    fun setOrientation(landscape: Boolean) {
        if (landscape) {
            device.setOrientationLeft()
        } else {
            device.setOrientationNatural()
        }
    }
}

/**
 * Switches the current robot context to UI Automator for a block of actions.
 */
inline fun ComposeRuleScope.onDevice(block: AutomatorScope.() -> Unit) {
    val scope = object : AutomatorScope {}
    scope.block()
}
