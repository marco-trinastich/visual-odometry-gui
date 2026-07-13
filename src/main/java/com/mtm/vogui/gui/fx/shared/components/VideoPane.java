/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.components;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Reusable, app-blind video viewport: letterboxes a single {@link Image} onto a dark backdrop and
 * shows a muted placeholder while no frame is set. Knows nothing about the app — bind
 * {@link #imageProperty()} to any source (a feature wires it to the shared state), so it serves the
 * input preview and the vo output alike. A conventional dark viewport reads correctly under both the
 * light and dark themes, so the backdrop is a fixed neutral rather than a theme-swapped colour.
 */
public class VideoPane extends StackPane {

    private final ImageView imageView = new ImageView();
    private final Label placeholder;

    public VideoPane(String placeholderText) {
        getStyleClass().add("video-pane");
        setStyle("-fx-background-color: #1e1f22;");
        setMinSize(0, 0);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        // Letterbox within the pane: fit both dimensions, preserveRatio keeps the aspect.
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        placeholder = new Label(placeholderText);
        placeholder.setStyle("-fx-text-fill: #8a8d91;");
        // Show the placeholder only while there is no frame to display.
        placeholder.visibleProperty().bind(imageView.imageProperty().isNull());

        getChildren().addAll(placeholder, imageView);
    }

    /** The displayed frame; bind it to a frame source. {@code null} reveals the placeholder. */
    public ObjectProperty<Image> imageProperty() {
        return imageView.imageProperty();
    }
}
