/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.exceptions;

import com.mtm.vogui.models.constants.Messages;

@SuppressWarnings("serial")
public class BufferTimeoutException extends Exception {
    public BufferTimeoutException() {
        super(Messages.BUFFER_TIMEOUT_EXCEPTION);
    }
}
