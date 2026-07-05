/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.tracker;

import com.mtm.vogui.models.enums.settings.TrackerType;
import com.mtm.vogui.models.interfaces.WithDefault;
import jakarta.enterprise.context.Dependent;

import java.io.Serial;
import java.io.Serializable;

/**
 * Tracker settings
 * <p/>
 * Options related to tracker type and options
 */
@Dependent
public class TrackerSettings implements Serializable, WithDefault<TrackerSettings> {

    private TrackerType trackerType;

    //KLT Parameters
    private int kltTracker_templateRadius;
    private int kltTracker_pyramidLevels;
    private int kltTracker_maxFeatures;
    private int kltTracker_radius;
    private float kltTracker_threshold;

    //SURF Parameters
    private int surfTracker_maxFeaturesPerScale;
    private int surfTracker_extractRadius;
    private int surfTracker_initialSampleSize;

    private boolean trackerShowActiveTracks;
    private boolean trackerShowNewTracks;

    @Serial
    private static final long serialVersionUID = -4351575446151780166L;


    public TrackerSettings() {
        loadDefaults();
    }

    /**
     * Copy constructor
     */
    public TrackerSettings(TrackerSettings anotherTrackerSettings) {
        this.trackerType = anotherTrackerSettings.trackerType;
        this.kltTracker_templateRadius = anotherTrackerSettings.kltTracker_templateRadius;
        this.kltTracker_pyramidLevels = anotherTrackerSettings.kltTracker_pyramidLevels;
        this.kltTracker_maxFeatures = anotherTrackerSettings.kltTracker_maxFeatures;
        this.kltTracker_radius = anotherTrackerSettings.kltTracker_radius;
        this.kltTracker_threshold = anotherTrackerSettings.kltTracker_threshold;
        this.surfTracker_maxFeaturesPerScale = anotherTrackerSettings.surfTracker_maxFeaturesPerScale;
        this.surfTracker_extractRadius = anotherTrackerSettings.surfTracker_extractRadius;
        this.surfTracker_initialSampleSize = anotherTrackerSettings.surfTracker_initialSampleSize;
        this.trackerShowActiveTracks = anotherTrackerSettings.trackerShowActiveTracks;
        this.trackerShowNewTracks = anotherTrackerSettings.trackerShowNewTracks;
    }

    public void loadDefaults() {
        //Default values
        this.setTrackerType(TrackerType.Klt);                   //Sets default tracker type to KLT
        this.setKltTracker_templateRadius(3);                   //Sets default klt templateRadius=3
        this.setKltTracker_pyramidLevels(4);                    //Sets default klt pyramidScaling=[1,2,4,8]
        this.setKltTracker_maxFeatures(600);                    //Sets default klt maxFeatures=600 (Also 200 is a good value)
        this.setKltTracker_radius(3);                           //Sets default klt radius=3
        this.setKltTracker_threshold(1.00f);                    //Sets default klt threshold=1.00(float)
        this.setSurfTracker_maxFeaturesPerScale(200);           //Sets default surf maxFeaturesPerScale=200
        this.setSurfTracker_extractRadius(3);                   //Sets default surf extractRadius=3
        this.setSurfTracker_initialSampleSize(2);               //Sets default surf initialSampleSize=2
        this.setTrackerShowActiveTracks(true);                  //Tracker Show Active Tracks enabled by default
        this.setTrackerShowNewTracks(false);                    //Tracker Show New Tracks disabled by default
    }

    public TrackerType getTrackerType() {
        return trackerType;
    }

    public void setTrackerType(TrackerType trackerType) {
        this.trackerType = trackerType;
    }

    public int getKltTracker_templateRadius() {
        return kltTracker_templateRadius;
    }

    public void setKltTracker_templateRadius(int kltTracker_templateRadius) {
        this.kltTracker_templateRadius = kltTracker_templateRadius;
    }

    public int getKltTracker_pyramidLevels() {
        return kltTracker_pyramidLevels;
    }

    public void setKltTracker_pyramidLevels(int kltTracker_pyramidLevels) {
        this.kltTracker_pyramidLevels = kltTracker_pyramidLevels;
    }

    public int getKltTracker_maxFeatures() {
        return kltTracker_maxFeatures;
    }

    public void setKltTracker_maxFeatures(int kltTracker_maxFeatures) {
        this.kltTracker_maxFeatures = kltTracker_maxFeatures;
    }

    public int getKltTracker_radius() {
        return kltTracker_radius;
    }

    public void setKltTracker_radius(int kltTracker_radius) {
        this.kltTracker_radius = kltTracker_radius;
    }

    public float getKltTracker_threshold() {
        return kltTracker_threshold;
    }

    public void setKltTracker_threshold(float kltTracker_threshold) {
        this.kltTracker_threshold = kltTracker_threshold;
    }

    public int getSurfTracker_maxFeaturesPerScale() {
        return surfTracker_maxFeaturesPerScale;
    }

    public void setSurfTracker_maxFeaturesPerScale(int surfTracker_maxFeaturesPerScale) {
        this.surfTracker_maxFeaturesPerScale = surfTracker_maxFeaturesPerScale;
    }

    public int getSurfTracker_extractRadius() {
        return surfTracker_extractRadius;
    }

    public void setSurfTracker_extractRadius(int surfTracker_extractRadius) {
        this.surfTracker_extractRadius = surfTracker_extractRadius;
    }

    public int getSurfTracker_initialSampleSize() {
        return surfTracker_initialSampleSize;
    }

    public void setSurfTracker_initialSampleSize(int surfTracker_initialSampleSize) {
        this.surfTracker_initialSampleSize = surfTracker_initialSampleSize;
    }

    public boolean isTrackerShowActiveTracks() {
        return trackerShowActiveTracks;
    }

    public void setTrackerShowActiveTracks(boolean trackerShowActiveTracks) {
        this.trackerShowActiveTracks = trackerShowActiveTracks;
    }

    public boolean isTrackerShowNewTracks() {
        return trackerShowNewTracks;
    }

    public void setTrackerShowNewTracks(boolean trackerShowNewTracks) {
        this.trackerShowNewTracks = trackerShowNewTracks;
    }
}
