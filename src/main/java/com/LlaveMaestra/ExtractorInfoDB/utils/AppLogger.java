package com.LlaveMaestra.ExtractorInfoDB.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppLogger {

    private static final Logger log = LoggerFactory.getLogger(AppLogger.class);

    private AppLogger() {
    }

    public static void logInfo(String message) {
        log.info(message);
    }

    public static void logWarning(String warning) {
        log.warn(warning);
    }

    public static void logError(String error) {
        log.error(error);
    }

    public static void logDebug(String debug) {
        log.debug(debug);
    }
}
