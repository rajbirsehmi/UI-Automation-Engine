package com.sehmi.engine.actions

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.sehmi.engine.core.ComposeRuleScope
import com.sehmi.engine.utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

private val logger: Logger = LogManager.getLogger("SystemActions")

/**
 * Supported screen orientations for UI testing.
 */
enum class Orientation {
    /** The device's natural orientation (usually portrait for phones). */
    PORTRAIT,
    /** Rotated 90 degrees to the left. */
    LANDSCAPE
}

/**
 * Robustly presses the system back button.
 *
 * Unlike standard Compose actions, this interacts with the system via UI Automator.
 * It is designed to be safe and will not throw an exception if the action cannot be 
 * performed (e.g., already at the home screen).
 */
fun ComposeRuleScope.pressBack() {
    logger.infoStep("Starting pressBack")
    composeRule.waitForIdle()
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    logger.debugStep("Performing system back button press")
    device.pressBack()
    logger.debugStep("pressBack completed")
}

/**
 * Presses the system home button using UI Automator.
 *
 * Useful for testing app backgrounding and resumption scenarios.
 */
fun ComposeRuleScope.pressHome() {
    logger.infoStep("Starting pressHome")
    composeRule.waitForIdle()
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    logger.debugStep("Performing system home button press")
    device.pressHome()
    logger.debugStep("pressHome completed")
}

/**
 * Rotates the device screen to the specified [orientation] and waits for the 
 * Compose UI to settle.
 *
 * @param orientation The target [Orientation].
 */
@Suppress("unused")
fun ComposeRuleScope.rotateScreen(orientation: Orientation) {
    logger.infoStep("Starting rotateScreen: orientation=$orientation")
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    logger.debugStep("Rotating device to {}", orientation)
    when (orientation) {
        Orientation.PORTRAIT -> device.setOrientationNatural()
        Orientation.LANDSCAPE -> device.setOrientationLeft()
    }
    logger.debugStep("Waiting for Compose UI to idle after rotation")
    composeRule.waitForIdle()
    logger.debugStep("rotateScreen completed")
}

/**
 * Safely handles system permission dialogs by clicking the appropriate button.
 *
 * This utility uses fuzzy text matching to handle differences across Android OS versions 
 * and locales (e.g., matching "Allow", "Grant", or "Allow only while using the app").
 *
 * @param allow True to grant the permission, false to deny it.
 */
@Suppress("unused")
fun ComposeRuleScope.handlePermissionDialog(allow: Boolean) {
    logger.infoStep("Starting handlePermissionDialog: allow=$allow")
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val buttonText = if (allow) "Allow" else "Deny"
    logger.debugStep("Searching for permission dialog button with text matching '$buttonText'")
    // Use regex to handle different OS versions/locales
    val permissionButton = device.findObject(UiSelector().textMatches("(?i)($buttonText|Allow|Grant).*"))
    if (permissionButton.exists()) {
        logger.debugStep("Permission button found, clicking")
        permissionButton.click()
        composeRule.waitForIdle()
        logger.debugStep("Permission dialog handled and UI idle")
    } else {
        logger.debugStep("Permission button not found")
    }
}

/**
 * Robustly waits for a specific system window or application package to become active.
 *
 * Useful for cross-app testing or waiting for external components like a browser 
 * or system settings.
 *
 * @param packageName The package name of the application to wait for.
 * @param timeoutMillis Maximum time to wait in milliseconds.
 */
@Suppress("unused")
fun ComposeRuleScope.waitForSystemWindow(packageName: String, timeoutMillis: Long = 5000) {
    logger.infoStep("Starting waitForSystemWindow: packageName=$packageName, timeoutMillis=$timeoutMillis")
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    logger.debugStep("Waiting for package $packageName to become active")
    device.wait(Until.hasObject(By.pkg(packageName)), timeoutMillis)
    logger.debugStep("waitForSystemWindow completed")
}

/**
 * Captures a screenshot of the current device screen and saves it as a PNG file 
 * in the external cache directory.
 *
 * This is primarily used by the robust action pipeline to collect diagnostic artifacts 
 * on failure.
 *
 * @param name The base name for the screenshot file (excluding extension).
 */
internal fun takeScreenshot(name: String) {
    logger.infoStep("Starting takeScreenshot: name=$name")
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val path = context.externalCacheDir ?: context.cacheDir
    val file = File(path, "$name.png")
    logger.debugStep("Saving screenshot to: ${file.absolutePath}")
    device.takeScreenshot(file)
    logger.debugStep("takeScreenshot completed")
}

/**
 * Robustly opens the system notification shade using UI Automator.
 *
 * This action performs a system-wide swipe from the top of the screen to expand 
 * the notification tray. It is useful for testing app behavior in response to 
 * system notifications or external interrupts.
 */
@Suppress("unused")
fun ComposeRuleScope.openNotificationShade() {
    logger.infoStep("Starting openNotificationShade")
    runRobustly("Open notification shade") {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        logger.debugStep("Performing system notification shade expansion")
        device.openNotification()
        composeRule.waitForIdle()
    }
    logger.debugStep("openNotificationShade completed")
}

/**
 * Robustly clicks a notification containing the specified [text] in the notification shade.
 *
 * This interaction leverages UI Automator to find and click a notification by its 
 * title or content text. If the notification shade is not already visible, the 
 * engine will automatically attempt to open it first.
 *
 * @param text The title or content text of the notification to click.
 * @param timeoutMillis Maximum time to wait for the notification to appear in the shade.
 * @throws AssertionError if the notification is not found within the timeout.
 */
@Suppress("unused")
fun ComposeRuleScope.clickNotification(text: String, timeoutMillis: Long = 5000L) {
    logger.infoStep("Starting clickNotification: text='$text'")
    runRobustly("Click notification with text: $text") {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Ensure shade is open
        if (!device.hasObject(By.textContains(text))) {
            logger.debugStep("Notification not visible, opening shade")
            device.openNotification()
        }
        
        logger.debugStep("Waiting for notification with text '$text'")
        val notification = device.wait(Until.findObject(By.textContains(text)), timeoutMillis)
        
        if (notification != null) {
            logger.debugStep("Notification found, clicking")
            notification.click()
            composeRule.waitForIdle()
        } else {
            throw AssertionError("Notification with text '$text' not found after ${timeoutMillis}ms")
        }
    }
    logger.debugStep("clickNotification completed for text: $text")
}

/**
 * Robustly toggles a system quick setting tile (e.g., "Dark mode", "Airplane mode").
 *
 * This interaction opens the quick settings panel (double swipe down) and searches 
 * for a tile matching the provided [settingName] (either via content description or text). 
 * Once found, it performs a click and closes the panel.
 *
 * @param settingName The displayed name or description of the quick setting tile.
 * @throws AssertionError if the quick setting tile is not found.
 */
@Suppress("unused")
fun ComposeRuleScope.toggleQuickSetting(settingName: String) {
    logger.infoStep("Starting toggleQuickSetting: settingName=$settingName")
    runRobustly("Toggle quick setting: $settingName") {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        logger.debugStep("Opening quick settings (double swipe down)")
        device.openQuickSettings()
        
        logger.debugStep("Searching for quick setting tile: $settingName")
        var tile = device.wait(Until.findObject(By.descContains(settingName)), 2000L)
        if (tile == null) {
            tile = device.wait(Until.findObject(By.textContains(settingName)), 2000L)
        }
        
        if (tile != null) {
            logger.debugStep("Quick setting tile found, clicking")
            tile.click()
            composeRule.waitForIdle()
            // Close quick settings
            device.pressBack()
        } else {
            throw AssertionError("Quick setting tile '$settingName' not found.")
        }
    }
    logger.debugStep("toggleQuickSetting completed for setting: $settingName")
}

