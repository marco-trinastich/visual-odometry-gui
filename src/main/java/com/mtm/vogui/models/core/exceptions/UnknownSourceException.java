/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.exceptions;

import com.mtm.vogui.models.constants.Messages;

@SuppressWarnings("serial")
public class UnknownSourceException extends Exception {
    public UnknownSourceException(String message) {
        super(String.format(Messages.UNKNOWN_SOURCE_EXCEPTION, message));
    }
}
