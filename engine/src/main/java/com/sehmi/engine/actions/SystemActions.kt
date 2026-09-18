package com.sehmi.engine.actions

import android.os.Bundle
import com.sehmi.engine.UiTestEngine
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
    uiTestEngineRule.waitForIdle()
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
    uiTestEngineRule.waitForIdle()
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
    uiTestEngineRule.waitForIdle()
    logger.debugStep("rotateScreen completed")
}

/**
 * Options for handling system permission dialogs.
 */
enum class PermissionAction {
    /** Grant the permission "While using the app". Also matches "Allow" or "Grant". */
    WHILE_USING_THE_APP,
    /** Grant the permission "Only this time". */
    ONLY_THIS_TIME,
    /** Deny the permission. Matches "Don't allow" or "Deny". */
    DONT_ALLOW
}

/**
 * Safely handles system permission dialogs by clicking the specified [action].
 *
 * This utility uses fuzzy text matching to handle differences across Android OS versions
 * and locales (e.g., matching "Allow", "Grant", or "While using the app").
 *
 * @param action The [PermissionAction] to perform.
 */
fun ComposeRuleScope.handlePermissionDialog(action: PermissionAction) {
    logger.infoStep("Starting handlePermissionDialog: action=$action")
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    val regex = when (action) {
        PermissionAction.WHILE_USING_THE_APP -> "(?i)(While using the app|Allow|Grant).*"
        PermissionAction.ONLY_THIS_TIME -> "(?i)(Only this time).*"
        PermissionAction.DONT_ALLOW -> "(?i)(Don't allow|Deny).*"
    }

    logger.debugStep("Searching for permission dialog button with regex matching '$regex'")
    val permissionButton = device.findObject(UiSelector().textMatches(regex))

    if (permissionButton.exists()) {
        logger.debugStep("Permission button found, clicking")
        permissionButton.click()
        uiTestEngineRule.waitForIdle()
        logger.debugStep("Permission dialog handled and UI idle")
    } else {
        logger.debugStep("Permission button not found for action $action")
    }
}

/**
 * Safely handles system permission dialogs by clicking the appropriate button.
 *
 * This is a compatibility overload. For more control (e.g., "Only this time"),
 * use [handlePermissionDialog(PermissionAction)].
 *
 * @param allow True to grant the permission (maps to [PermissionAction.WHILE_USING_THE_APP]),
 *              false to deny it (maps to [PermissionAction.DONT_ALLOW]).
 */
@Suppress("unused")
fun ComposeRuleScope.handlePermissionDialog(allow: Boolean) {
    val action = if (allow) PermissionAction.WHILE_USING_THE_APP else PermissionAction.DONT_ALLOW
    handlePermissionDialog(action)
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
 * Captures a screenshot of the current device screen and saves it as a PNG file.
 *
 * The screenshot is saved to one of the following locations (in order of priority):
 * 1. [UiTestEngine.config.screenshotDirectory] if set.
 * 2. The `additionalTestOutputDir` instrumentation argument if provided (standard for Gradle).
 * 3. The app's external cache directory.
 * 4. The app's internal cache directory.
 *
 * @param name The base name for the screenshot file (excluding extension).
 * @return The absolute path to the captured screenshot, or null if capture failed.
 */
internal fun takeScreenshot(name: String): String? {
    logger.infoStep("Starting takeScreenshot: name=$name")
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val device = UiDevice.getInstance(instrumentation)
    val context = instrumentation.targetContext

    // Resolve storage directory
    val configDir: String? = UiTestEngine.config.screenshotDirectory
    val additionalOutputDir: String? = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")

    val targetDir: File? = when {
        configDir != null -> File(configDir)
        additionalOutputDir != null -> File(additionalOutputDir)
        else -> context.externalCacheDir ?: context.cacheDir
    }

    if (targetDir != null && !targetDir.exists()) {
        targetDir.mkdirs()
    }

    val file = File(targetDir, "$name.png")
    val absolutePath = file.absolutePath
    logger.debugStep("Saving screenshot to: $absolutePath")

    val success = device.takeScreenshot(file)
    return if (success) {
        logger.debugStep("takeScreenshot completed")
        // Report to instrumentation metadata
        val status = Bundle().apply {
            putString("screenshot_path", absolutePath)
        }
        instrumentation.sendStatus(0, status)
        absolutePath
    } else {
        logger.error("takeScreenshot failed")
        null
    }
}

/**
 * Captures a tail of the Logcat output and saves it to a file.
 *
 * @param name The base name for the log file.
 * @param tailLines The number of lines to capture.
 * @return The absolute path to the log file, or null if failed.
 */
internal fun captureLogcat(name: String, tailLines: Int): String? {
    logger.infoStep("Starting captureLogcat: name=$name, tailLines=$tailLines")
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val device = UiDevice.getInstance(instrumentation)
    val context = instrumentation.targetContext

    val targetDir: File? = when {
        UiTestEngine.config.screenshotDirectory != null -> File(UiTestEngine.config.screenshotDirectory!!)
        InstrumentationRegistry.getArguments().getString("additionalTestOutputDir") != null -> 
            File(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir"))
        else -> context.externalCacheDir ?: context.cacheDir
    }

    if (targetDir != null && !targetDir.exists()) targetDir.mkdirs()

    val file = File(targetDir, "$name.log")
    return try {
        val logs = device.executeShellCommand("logcat -t $tailLines")
        file.writeText(logs)
        logger.debugStep("Logcat captured to: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        logger.error("Failed to capture logcat", e)
        null
    }
}

/**
 * Dumps the current Android View hierarchy to an XML file.
 *
 * @param name The base name for the XML file.
 * @return The absolute path to the XML file, or null if failed.
 */
internal fun captureViewHierarchy(name: String): String? {
    logger.infoStep("Starting captureViewHierarchy: name=$name")
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val device = UiDevice.getInstance(instrumentation)
    val context = instrumentation.targetContext

    val targetDir: File? = when {
        UiTestEngine.config.screenshotDirectory != null -> File(UiTestEngine.config.screenshotDirectory!!)
        InstrumentationRegistry.getArguments().getString("additionalTestOutputDir") != null -> 
            File(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir"))
        else -> context.externalCacheDir ?: context.cacheDir
    }

    if (targetDir != null && !targetDir.exists()) targetDir.mkdirs()

    val file = File(targetDir, "$name.xml")
    return try {
        device.dumpWindowHierarchy(file)
        logger.debugStep("View hierarchy dumped to: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        logger.error("Failed to dump window hierarchy", e)
        null
    }
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
        uiTestEngineRule.waitForIdle()
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
            uiTestEngineRule.waitForIdle()
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
            uiTestEngineRule.waitForIdle()
            // Close quick settings
            device.pressBack()
        } else {
            throw AssertionError("Quick setting tile '$settingName' not found.")
        }
    }
    logger.debugStep("toggleQuickSetting completed for setting: $settingName")
}
