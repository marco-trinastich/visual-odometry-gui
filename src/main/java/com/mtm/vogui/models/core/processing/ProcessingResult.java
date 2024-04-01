package com.mtm.vogui.models.core.processing;

import com.mtm.vogui.models.enums.core.ProcessingState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessingResult {
    private ProcessingState state;
    private Exception exception;
}
