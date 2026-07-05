/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.tracker;

import com.mtm.vogui.models.interfaces.WithDefault;
import jakarta.enterprise.context.Dependent;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Tracker settings
 * <p/>
 * Options related to tracker type and options
 */
@Dependent
public class TrackerSettings implements Serializable, WithDefault<TrackerSettings> {

    private String trackerType;
    //trackerType Constants
    public static final String KLT = "klt";
    public static final String KLT2 = "klt2";
    public static final String SURF = "surf";
    public static final String SURF2 = "surf2";
    public static final String DEFAULT_TRACKER = "default"; //KLT Tracker

    private LinkedHashMap<String, String> trackerTypeNames; //Key is trackerType, Value is displayed trackerName

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

    public TrackerSettings(String trackerType, LinkedHashMap<String, String> trackerTypeNames,
                           int kltTracker_templateRadius, int kltTracker_pyramidLevels, int kltTracker_maxFeatures,
                           int kltTracker_radius, float kltTracker_threshold, int surfTracker_maxFeaturesPerScale,
                           int surfTracker_extractRadius, int surfTracker_initialSampleSize, boolean trackerShowActiveTracks,
                           boolean trackerShowNewTracks) {
        //Custom initialization
        this.setTrackerType(trackerType);
        this.setTrackerTypeNames(trackerTypeNames);
        this.setKltTracker_templateRadius(kltTracker_templateRadius);
        this.setKltTracker_pyramidLevels(kltTracker_pyramidLevels);
        this.setKltTracker_maxFeatures(kltTracker_maxFeatures);
        this.setKltTracker_radius(kltTracker_radius);
        this.setKltTracker_threshold(kltTracker_threshold);
        this.setSurfTracker_maxFeaturesPerScale(surfTracker_maxFeaturesPerScale);
        this.setSurfTracker_extractRadius(surfTracker_extractRadius);
        this.setSurfTracker_initialSampleSize(surfTracker_initialSampleSize);
        this.setTrackerShowActiveTracks(trackerShowActiveTracks);
        this.setTrackerShowNewTracks(trackerShowNewTracks);
    }

    /**
     * Copy constructor
     */
    public TrackerSettings(TrackerSettings anotherTrackerSettings) {
        this(anotherTrackerSettings.getTrackerType(), anotherTrackerSettings.getTrackerTypeNames(),
                anotherTrackerSettings.getKltTracker_templateRadius(), anotherTrackerSettings.getKltTracker_pyramidLevels(),
                anotherTrackerSettings.getKltTracker_maxFeatures(), anotherTrackerSettings.getKltTracker_radius(),
                anotherTrackerSettings.getKltTracker_threshold(), anotherTrackerSettings.getSurfTracker_maxFeaturesPerScale(),
                anotherTrackerSettings.getSurfTracker_extractRadius(), anotherTrackerSettings.getSurfTracker_initialSampleSize(),
                anotherTrackerSettings.isTrackerShowActiveTracks(), anotherTrackerSettings.isTrackerShowNewTracks());
    }

    public void loadDefaults() {
        //Default values
        this.setTrackerType(KLT);                               //Sets default tracker type to KLT
        this.setTrackerTypeNames(defaultTrackerTypeNames());    //Sets default tracker types names (KLT,KLT2,SURF,SURF2,Default(KLT))
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

    public LinkedHashMap<String, String> defaultTrackerTypeNames() {
        LinkedHashMap<String, String> defTrackerTypeNames = new LinkedHashMap<>();

        defTrackerTypeNames.put(KLT, "KLT (Standard)");
        defTrackerTypeNames.put(KLT2, "KLT (Modern)");
        defTrackerTypeNames.put(SURF, "Surf (Standard)");
        defTrackerTypeNames.put(SURF2, "Surf (Dda Two Pass)");
        defTrackerTypeNames.put(DEFAULT_TRACKER, "<html><b>Default Tracker (KLT, standard param.)</b></html>");

        return defTrackerTypeNames;
    }

    public String getTrackerType() {
        return trackerType;
    }

    public void setTrackerType(String trackerType) {
        this.trackerType = trackerType;
    }

    public LinkedHashMap<String, String> getTrackerTypeNames() {
        return trackerTypeNames;
    }


    public void setTrackerTypeNames(LinkedHashMap<String, String> trackerTypeNames) {
        this.trackerTypeNames = trackerTypeNames;
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
