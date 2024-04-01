package com.mtm.vogui.models.settings.core.chart;

import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.interfaces.WithDefault;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Chart settings
 * <p/>
 * Options related to charts.
 */
@Data
@Dependent
public class ChartSettings implements Serializable, WithDefault<ChartSettings> {

    private ChartType type;
    private double scaleXZ;
    private double scaleY;

    @Inject
    public ChartSettings() {
        this.loadDefaults();
    }

    public ChartSettings(@NotNull ChartSettings chart) {
        this.type = chart.type;
        this.scaleXZ = chart.scaleXZ;
        this.scaleY = chart.scaleY;
    }

    public void loadDefaults() {
        this.type = ChartType.YFrames;
        this.scaleXZ = 1.0;
        this.scaleY = 1.0;
    }
}
