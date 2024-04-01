package com.mtm.vogui.models.core.processing;

import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.core.processing.frames.ProcessedFrame;
import com.mtm.vogui.models.core.processing.tracking.TrackingStatus;
import georegression.struct.point.Vector3D_F64;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.ejml.data.DMatrixRMaj;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@AllArgsConstructor
public class ProcessingStatus {
    private FpsStatus fps;
    private TrackingStatus tracking;
    private Vector3D_F64 translation;
    private Vector3D_F64 prevTranslation;
    private DMatrixRMaj rotation;
    private ProcessedFrame frame;

    public ProcessingStatus() {
        this.fps = FpsStatus.builder().build();
        this.tracking = TrackingStatus.builder().build();
        this.translation = new Vector3D_F64();
        this.prevTranslation = null;
        this.rotation = new DMatrixRMaj();
        this.frame = null;
    }

    public ProcessingStatus deepClone() {
        return ProcessingStatus.builder()
                .fps(fps != null ? fps.deepClone() : null)
                .tracking(tracking != null ? tracking.deepClone() : null)
                .translation(translation != null ? translation.copy() : null)
                .prevTranslation(prevTranslation != null ? prevTranslation.copy() : null)
                .rotation(rotation != null ? rotation.copy() : null)
                .build();
    }

    public static @NotNull ProcessingStatus build() {
        return new ProcessingStatus();
    }
}
