/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Small shared builders for the telemetry sections: consistent AtlantaFX-styled section headers,
 * muted captions and bold value labels, plus a two-column caption|value grid. Keeps the read-only
 * sections terse and visually uniform (a modern label/value readout, not the Swing spring layout).
 */
public final class TelemetryUi {

    public static final String EMPTY = "—";

    private TelemetryUi() {
    }

    /** A quiet, bold section title — a discreet divider, only slightly larger than the content. */
    public static Label header(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("telemetry-header");
        return label;
    }

    /** A muted caption (the left column of a readout row). */
    public static Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        return label;
    }

    /** A bold dynamic value label, starting empty. */
    public static Label value() {
        Label label = new Label(EMPTY);
        label.getStyleClass().add(Styles.TEXT_BOLD);
        return label;
    }

    /** A tight two-column (caption | value) grid. */
    public static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(4);
        return grid;
    }

    /** Adds a {@code caption | value} row to a grid. */
    public static void row(GridPane grid, int rowIndex, String caption, Label value) {
        grid.addRow(rowIndex, caption(caption), value);
    }

    /** Wraps a section body under a bold header. */
    public static VBox section(String title, Node body) {
        VBox box = new VBox(6, header(title), body);
        box.getStyleClass().add("telemetry-section");
        return box;
    }

    /** A {@code caption value} pair laid out horizontally (for compact rows like X/Y/Z). */
    public static HBox axis(String name, Label value) {
        HBox box = new HBox(5, caption(name), value);
        box.setAlignment(Pos.BASELINE_LEFT);
        return box;
    }

    /**
     * A collapsible disclosure: a clickable chevron header that toggles the body, collapsed by default.
     * For secondary/bulky readouts (e.g. the rotation matrix) that aren't glanced at continuously.
     */
    public static VBox disclosure(String title, Node body) {
        Label toggle = caption("▸ " + title);
        toggle.setCursor(Cursor.HAND);
        body.setVisible(false);
        body.setManaged(false);
        toggle.setOnMouseClicked(_ -> {
            boolean show = !body.isVisible();
            body.setVisible(show);
            body.setManaged(show);
            toggle.setText((show ? "▾ " : "▸ ") + title);
        });
        return new VBox(4, toggle, body);
    }
}
