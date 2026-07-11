/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.exceptions;

import com.mtm.vogui.models.constants.Messages;

@SuppressWarnings("serial")
public class NotImplementedException extends Exception {
    public NotImplementedException(String message) {
        super(String.format(Messages.NOT_IMPLEMENTED_EXCEPTION, message));
    }
}
