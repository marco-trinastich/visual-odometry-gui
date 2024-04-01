package com.mtm.vogui.models.settings.core;

import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.chart.ChartSettings;
import com.mtm.vogui.models.settings.core.image.ImageSettings;
import com.mtm.vogui.models.settings.core.input.InputSettings;
import com.mtm.vogui.models.settings.core.tracker.TrackerSettings;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Data
@Dependent
public class CoreSettings implements Serializable, WithDefault<CoreSettings> {
    private InputSettings input;
    private ImageSettings image;
    private TrackerSettings tracker;
    private VisualOdometrySettings visualOdometry;
    private ChartSettings chart;

    @Inject
    public CoreSettings(InputSettings input, ImageSettings image,
                        TrackerSettings tracker, VisualOdometrySettings visualOdometry,
                        ChartSettings chart) {
        this.input = input;
        this.image = image;
        this.tracker = tracker;
        this.visualOdometry = visualOdometry;
        this.chart = chart;
    }

    public CoreSettings(@NotNull CoreSettings core) {
        this.input(new InputSettings(core.input));
        this.image(new ImageSettings(core.image));
        this.tracker(new TrackerSettings(core.tracker));
        this.visualOdometry(new VisualOdometrySettings(core.visualOdometry));
        this.chart(new ChartSettings(core.chart));
    }

    public void loadDefaults(){
        this.input().loadDefaults();
        this.image().loadDefaults();
        this.tracker().loadDefaults();
        this.visualOdometry().loadDefaults();
        this.chart().loadDefaults();
    }
}
