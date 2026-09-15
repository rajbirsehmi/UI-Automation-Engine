package com.sehmi.engine.utils

import com.sehmi.engine.UiTestEngine
import org.apache.logging.log4j.Logger

/**
 * Extension function to log an info message only if verbose logging is enabled in [UiTestEngine].
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun Logger.infoStep(message: String) {
    if (UiTestEngine.config.verboseLogging) {
        info(message)
    }
}

/**
 * Extension function to log a debug message only if verbose logging is enabled in [UiTestEngine].
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun Logger.debugStep(message: String) {
    if (UiTestEngine.config.verboseLogging) {
        debug(message)
    }
}

/**
 * Extension function to log a debug message with parameters only if verbose logging is enabled.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun Logger.debugStep(message: String, vararg params: Any?) {
    if (UiTestEngine.config.verboseLogging) {
        debug(message, *params)
    }
}
