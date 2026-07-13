/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.trackedpoints;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui.TelemetryUi;
import com.mtm.vogui.gui.fx.shared.components.SmoothList;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.utilities.CommonUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Tracked-points section (humble view): the run's point log, backed by the observable {@code GuiState}
 * list. Built on {@link SmoothList} (Flowless) rather than a {@code ListView} so the wheel scroll is
 * genuinely pixel-smooth over the thousands of rows a run produces — the native {@code VirtualFlow}
 * snaps cell-by-cell. Follows the newest entry live (yielding while the user scrolls); segment
 * start/end markers render muted. Lives in the bottom pane of the telemetry split so it stays visible
 * and resizable.
 * <p>
 * Clipboard behaviour mirrors the Swing view: selecting a row (click or keyboard) copies that row's
 * text, and a primary double-click copies the whole log. The formatting lives in {@link #rowText} so
 * the on-screen label and the clipboard string never drift apart.
 */
public class TrackedPointsView {

    private static final String NA = "n/a";
    private static final double ROW_HEIGHT = 24;

    private final ObservableList<TrackedPoint> points;
    private final SmoothList<TrackedPoint> list;
    private final Region root;

    public TrackedPointsView(ObservableList<TrackedPoint> points) {
        this.points = points;

        this.list = new SmoothList<>(points, ROW_HEIGHT, TrackedPointsView::renderRow);
        list.setContextMenu(contextMenu(list));
        list.setOnCopy(() -> copySelected(list));

        Region listNode = list.node();
        VBox box = new VBox(6, TelemetryUi.header("Tracked points"), listNode);
        box.getStyleClass().add("telemetry-tracked-points");
        VBox.setVgrow(listNode, Priority.ALWAYS);
        this.root = box;
    }

    public Region node() {
        return this.root;
    }

    /** The user-selected point (identity selection from the list), for cross-feature chart navigation. */
    public ReadOnlyObjectProperty<TrackedPoint> selectedProperty() {
        return this.list.selectedItemProperty();
    }

    /** Fills a recycled row label; must reset the marker class each call (see {@link SmoothList}). */
    private static void renderRow(Label label, TrackedPoint point) {
        label.getStyleClass().remove("tracked-point-marker");
        if (point == null) {
            label.setText(null);
            return;
        }
        label.setText(rowText(point));
        if (point.startPoint() || point.endPoint()) {
            label.getStyleClass().add("tracked-point-marker");
        }
    }

    /** Single source of truth for a row's text — shared by the label renderer and the clipboard copy. */
    private static String rowText(TrackedPoint point) {
        if (point.startPoint()) {
            return "── segment " + point.chartId() + " ──";
        }
        if (point.endPoint()) {
            return "── end ──";
        }
        return "#" + point.frame() + "  " + point.formattedTime()
                + "   x " + axis(point.x()) + "  y " + axis(point.y()) + "  z " + axis(point.z())
                + "   " + point.inliersPercent() + "%";
    }

    /** Right-click menu: copy the selected row (⌘/Ctrl+C) or the whole log; items disable when N/A. */
    private ContextMenu contextMenu(SmoothList<TrackedPoint> list) {
        MenuItem copy = new MenuItem("Copy");
        copy.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copy.setOnAction(_ -> copySelected(list));
        copy.disableProperty().bind(list.selectedItemProperty().isNull());

        MenuItem copyAll = new MenuItem("Copy all");
        copyAll.setOnAction(_ -> copyAll());
        copyAll.disableProperty().bind(Bindings.isEmpty(points));

        return new ContextMenu(copy, copyAll);
    }

    private void copySelected(SmoothList<TrackedPoint> list) {
        TrackedPoint selected = list.selectedItemProperty().get();
        if (selected != null) {
            copyToClipboard(rowText(selected));
        }
    }

    private void copyAll() {
        if (points.isEmpty()) {
            return;
        }
        StringBuilder output = new StringBuilder();
        for (TrackedPoint point : points) {
            output.append(rowText(point)).append('\n');
        }
        copyToClipboard(output.toString());
    }

    private static void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private static String axis(Double value) {
        return value == null ? NA : CommonUtils.roundBigDecimal(value, 2).toString();
    }
}
