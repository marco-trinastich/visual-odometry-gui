package com.mtm.vogui.models.core.processing.tracking;

import boofcv.abst.tracker.PointTracker;
import boofcv.factory.tracker.*;
import boofcv.struct.image.ImageGray;
import lombok.*;

@Data
@Builder
public class Tracker {
    private ConfigPointTracker config;
    private PointTracker<? extends ImageGray<?>> instance;

    public static Tracker from(ConfigPointTracker config, PointTracker<? extends ImageGray<?>> instance) {
        return Tracker.builder()
                .config(config)
                .instance(instance)
                .build();
    }
}
