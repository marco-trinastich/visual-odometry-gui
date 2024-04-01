package com.mtm.vogui.models.core.exceptions;

import com.mtm.vogui.models.constants.Messages;

public class BufferTimeoutException extends Exception {
    public BufferTimeoutException() {
        super(Messages.BUFFER_TIMEOUT_EXCEPTION);
    }
}
