package com.mtm.vogui.models.core.processing.fps;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class FpsStatus {
    private int totalFrames;
    private int totalProcessed;
    private double totalSeconds;
    private double averageFPS;
    private double currentFPS;
    private double inputAverageFPS;
    private double inputCurrentFPS;

    public FpsStatus deepClone() {
        return FpsStatus.builder()
                .totalFrames(totalFrames)
                .totalProcessed(totalProcessed)
                .totalSeconds(totalSeconds)
                .averageFPS(averageFPS)
                .currentFPS(currentFPS)
                .inputAverageFPS(inputAverageFPS)
                .inputCurrentFPS(inputCurrentFPS)
                .build();
    }

    public void setAverage(@NotNull FpsStatus fpsStatus) {
        this.totalFrames = fpsStatus.totalFrames;
        this.totalProcessed = fpsStatus.totalProcessed;
        this.totalSeconds = fpsStatus.totalSeconds;
        this.averageFPS = fpsStatus.averageFPS;
        this.inputAverageFPS = fpsStatus.inputAverageFPS;
    }
}
