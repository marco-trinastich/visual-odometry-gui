/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.components;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * A compact, app-blind direction indicator: a circular dial with a needle pointing along a
 * unit-ish {@code (x, y)} vector (math convention, {@code y} up). The modern replacement for the
 * Swing {@code DirectionPanel} (green line + black cross-hair); here it's a clean ring + accent
 * needle + centre hub, styled entirely via CSS looked-up colours (theme-aware, no hard-coded
 * colours) — see {@code .direction-dial-*} in {@code app.css}.
 * <p>
 * Reusable widget (no app/settings/sink knowledge): callers push a vector via {@link #setDirection}
 * and install their own {@code Tooltip} for the numeric detail.
 */
public class DirectionDial extends Pane {

    private final double center;
    private final double reach;
    private final Line needle;

    public DirectionDial(double diameter) {
        double ringStroke = 1.5;
        this.center = diameter / 2d;
        // Keep the needle tip a touch inside the ring so a full-magnitude vector never overshoots it.
        this.reach = this.center - ringStroke - 3;

        setPrefSize(diameter, diameter);
        setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        Circle ring = new Circle(center, center, center - ringStroke);
        ring.getStyleClass().add("direction-dial-ring");

        this.needle = new Line(center, center, center, center);
        this.needle.setStrokeLineCap(StrokeLineCap.ROUND);
        this.needle.getStyleClass().add("direction-dial-needle");

        Circle hub = new Circle(center, center, 2.5);
        hub.getStyleClass().add("direction-dial-hub");

        getStyleClass().add("direction-dial");
        getChildren().addAll(ring, needle, hub);
    }

    /**
     * Points the needle along {@code (x, y)} (math convention: {@code +y} is up on screen). A zero
     * vector collapses the needle onto the hub (a flat/no-direction reading).
     */
    public void setDirection(double x, double y) {
        needle.setEndX(center + reach * x);
        needle.setEndY(center - reach * y);
    }
}
