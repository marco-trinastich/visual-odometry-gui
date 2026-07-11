/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing;

import com.mtm.vogui.models.enums.core.ProcessingState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessingResult {
    private ProcessingState state;
    private Throwable exception;
}
