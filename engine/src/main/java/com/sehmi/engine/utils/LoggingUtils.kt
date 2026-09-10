package com.sehmi.engine.utils

import com.sehmi.engine.UiEngine
import org.apache.logging.log4j.Logger

/**
 * Extension function to log an info message only if verbose logging is enabled in [UiEngine].
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun Logger.infoStep(message: String) {
    if (UiEngine.config.verboseLogging) {
        info(message)
    }
}

/**
 * Extension function to log a debug message only if verbose logging is enabled in [UiEngine].
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun Logger.debugStep(message: String) {
    if (UiEngine.config.verboseLogging) {
        debug(message)
    }
}

/**
 * Extension function to log a debug message with parameters only if verbose logging is enabled.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun Logger.debugStep(message: String, vararg params: Any?) {
    if (UiEngine.config.verboseLogging) {
        debug(message, *params)
    }
}
