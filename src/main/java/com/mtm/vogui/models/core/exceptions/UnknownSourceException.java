package com.mtm.vogui.models.core.exceptions;

import com.mtm.vogui.models.constants.Messages;

public class UnknownSourceException extends Exception {
    public UnknownSourceException(String message) {
        super(String.format(Messages.UNKNOWN_SOURCE_EXCEPTION, message));
    }
}
