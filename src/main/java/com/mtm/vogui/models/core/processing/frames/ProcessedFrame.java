package com.mtm.vogui.models.core.processing.frames;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessedFrame {
    private InnerFrame input;
    private InnerFrame vo;
}
