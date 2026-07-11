/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.utilities;

import org.jboss.logging.Logger;

/**
 * Logging helpers keeping error messages short by default: the stack trace is
 * attached (at the same level) only when debug logging is enabled.
 */
public final class LogUtils {

    // Resolves the calling class so log categories stay correct despite the indirection
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private LogUtils() {
    }

    public static void errorf(Throwable exc, String format, Object... params) {
        Logger logger = Logger.getLogger(WALKER.getCallerClass());
        if (logger.isDebugEnabled()) {
            logger.errorf(exc, format, params);
        } else {
            logger.errorf(format, params);
        }
    }
}
