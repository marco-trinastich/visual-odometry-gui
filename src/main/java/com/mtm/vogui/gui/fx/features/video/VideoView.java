/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.video;

import com.mtm.vogui.gui.fx.shared.components.VideoPane;
import com.mtm.vogui.gui.fx.state.GuiState;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;

/**
 * Video feature (humble view, JavaFX twin of {@code gui.swing.features.video.VideoView}): owns the
 * input-preview and vo-output viewports and binds each to the shared {@link GuiState} frame streams.
 * Code-built (not FXML) because the panels are purely dynamic image surfaces. Holds no logic beyond
 * composition + binding; the {@code FxRenderSink} feeds the state.
 * <p>
 * The feature owns its own internal layout: {@link #content()} returns a single {@link Region} (a
 * {@code SplitPane} of the two viewports), so it honours the uniform one-{@code Region}-per-feature
 * facade contract just like every other {@code XxxView} — the shell mounts one node, not two.
 */
public class VideoView {

    private static final String INPUT_PLACEHOLDER = "No input preview";
    private static final String OUTPUT_PLACEHOLDER = "No VO output";

    private final SplitPane content;

    public VideoView(GuiState guiState) {
        VideoPane inputPane = new VideoPane(INPUT_PLACEHOLDER);
        VideoPane outputPane = new VideoPane(OUTPUT_PLACEHOLDER);
        inputPane.imageProperty().bind(guiState.inputFrameProperty());
        outputPane.imageProperty().bind(guiState.outputFrameProperty());

        this.content = new SplitPane(inputPane, outputPane);
    }

    /** The video feature's viewports (input | output) as one node, for the shell to mount. */
    public Region content() {
        return content;
    }
}
