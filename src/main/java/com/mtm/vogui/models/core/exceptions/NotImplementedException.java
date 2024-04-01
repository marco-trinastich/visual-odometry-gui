package com.mtm.vogui.models.core.exceptions;

import com.mtm.vogui.models.constants.Messages;

public class NotImplementedException extends Exception {
    public NotImplementedException(String message) {
        super(String.format(Messages.NOT_IMPLEMENTED_EXCEPTION, message));
    }
}
