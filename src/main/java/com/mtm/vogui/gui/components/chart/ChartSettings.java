package com.mtm.vogui.gui.components.chart;

import com.mtm.vogui.models.enums.gui.ChartAxis;
import com.mtm.vogui.models.enums.settings.ChartType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Data
public class ChartSettings {
    // Chart settings
    private double chartScale;
    private boolean centeredOriginX;
    private boolean centeredOriginY;
    private boolean followNewPoints;
    private boolean showLegend;
    private boolean thickPoints;

    // Axis settings
    private boolean showAxis;
    private boolean showAxisUnits;
    private boolean showPermanentAxisNames;
    private ChartAxis xAxisName;
    private ChartAxis yAxisName;

    // Colors
    private Color backgroundColor;
    private Color plotColor;
    private Color axisColor;
    private Color axisUnitsColor;
    private Color axisNamesColor;
    private boolean multipleColors;

    public ChartSettings(boolean autoCenterX, boolean autoCenterY) {
        // Chart
        this.chartScale = 1.0;
        this.centeredOriginX = autoCenterX;
        this.centeredOriginY = autoCenterY;
        this.followNewPoints = true;
        this.showLegend = false;
        this.thickPoints = false;

        // Axis
        this.showAxis = true;
        this.showAxisUnits = true;
        this.showPermanentAxisNames = true;
        this.xAxisName = ChartAxis.X;
        this.yAxisName = ChartAxis.Y;

        // Colors
        this.plotColor = Color.blue;
        this.axisColor = Color.black;
        this.axisUnitsColor = Color.blue;
        this.axisNamesColor = Color.blue;
        this.multipleColors = true;
    }

    public void axisNames(ChartAxis x, ChartAxis y) {
        this.xAxisName = x;
        this.yAxisName = y;
    }

    public void axisNames(@NotNull ChartType chartType) {
        this.xAxisName = chartType.xAxis();
        this.yAxisName = chartType.yAxis();
    }
}
