/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.video;

import boofcv.gui.image.ImagePanel;
import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Video feature (humble view): owns the input preview and vo output frames/panels.
 * Consumers ({@code SwingRenderSink}) express intent through methods - widgets never
 * leave this class. Every mutator marshals to the EDT.
 */
public class VideoView {
    private final static int DEFAULT_GUI_VIDEO_SIZE = 400;

    private final ImagePanel inputVideoPanel = new ImagePanel();
    private final JFrame inputVideoFrame = new JFrame(GuiConstants.INPUT_VIDEO_FRAME_TITLE);
    private final ImagePanel outputVideoPanel = new ImagePanel();
    private final JFrame outputVideoFrame = new JFrame(GuiConstants.OUTPUT_VIDEO_FRAME_TITLE);

    public VideoView() {
        // Both frames sit to the right of chart frame and main frame (X = twice the app
        // frames default width); the output frame starts at half the default height
        this.inputVideoFrame.setLocationRelativeTo(null);
        this.inputVideoFrame.setLocation((SwingUtils.getDefaultFrameDimension().width * 2) + 65, 0);
        this.inputVideoFrame.getContentPane().add(this.inputVideoPanel);

        this.outputVideoFrame.setLocationRelativeTo(null);
        this.outputVideoFrame.setLocation((SwingUtils.getDefaultFrameDimension().width * 2) + 65,
                SwingUtils.getDefaultFrameDimension().height / 2);
        this.outputVideoFrame.getContentPane().add(this.outputVideoPanel);
    }

    /**
     * Shows the input preview frame (if hidden) and paints the given frame on it.
     */
    public void showInput(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            if (!this.inputVideoFrame.isVisible()) {
                this.inputVideoFrame.setVisible(true);
            }
            this.inputVideoPanel.setImageRepaint(image);
        });
    }

    /**
     * Hides the input preview frame if visible.
     */
    public void hideInput() {
        SwingUtilities.invokeLater(() -> {
            if (this.inputVideoFrame.isVisible()) {
                this.inputVideoFrame.setVisible(false);
            }
        });
    }

    /**
     * Shows the vo output frame (if hidden) and paints the given processed frame on it.
     */
    public void showOutput(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            if (!this.outputVideoFrame.isVisible()) {
                this.outputVideoFrame.setVisible(true);
            }
            this.outputVideoPanel.setImageRepaint(image);
        });
    }

    /**
     * Adjust size, position and title of input/output video frames
     */
    public void resizeAndReposition(@NotNull ProcessingParameters params) {
        // Image settings
        var isFullResolutionPreview = params.frozenContext().settings().input().fullResolutionPreview();
        var isResize = params.frozenContext().settings().image().resize();
        var resizeWidth = params.frozenContext().settings().image().resizeWidth();
        var resizeHeight = params.frozenContext().settings().image().resizeHeight();

        // Get frame size (defined upon source opening)
        Dimension frameSize = params.frameSize();

        SwingUtilities.invokeLater(() -> {
            // Hide frames if visible
            this.inputVideoFrame.setVisible(false);
            this.outputVideoFrame.setVisible(false);

            // Resize frames
            Dimension inputVideoFrameDimension = (isFullResolutionPreview ?
                    frameSize :
                    new Dimension(DEFAULT_GUI_VIDEO_SIZE, DEFAULT_GUI_VIDEO_SIZE));

            Dimension outputVideoFrameDimension = (isFullResolutionPreview ?
                    (!isResize ? frameSize : new Dimension(resizeWidth, resizeHeight)) :
                    new Dimension(DEFAULT_GUI_VIDEO_SIZE, DEFAULT_GUI_VIDEO_SIZE));
            this.inputVideoPanel.setPreferredSize(inputVideoFrameDimension);
            this.inputVideoPanel.setCentering(true);
            this.outputVideoPanel.setPreferredSize(outputVideoFrameDimension);
            this.outputVideoPanel.setCentering(true);

            // Reposition frames
            this.inputVideoFrame.setLocationRelativeTo(null);
            this.inputVideoFrame.setLocation((SwingUtils.getDefaultFrameDimension().width * 2) + 65, 0);
            this.outputVideoFrame.setLocationRelativeTo(null);
            this.outputVideoFrame.setLocation((SwingUtils.getDefaultFrameDimension().width * 2) + 65,
                    (SwingUtils.getDefaultFrameDimension().height / 2));

            // Reset frames title
            this.inputVideoFrame.setTitle(String.format(GuiConstants.INPUT_VIDEO_FRAME_TITLE_PATTERN,
                    frameSize.width,
                    frameSize.height,
                    inputVideoFrameDimension.width,
                    inputVideoFrameDimension.height,
                    isFullResolutionPreview ? GuiConstants.VIDEO_FULL_RESOLUTION : ""
            ));

            this.outputVideoFrame.setTitle(String.format(GuiConstants.OUTPUT_VIDEO_FRAME_TITLE_PATTERN,
                    !isResize ? frameSize.width : resizeWidth,
                    !isResize ? frameSize.height : resizeHeight,
                    !isResize ? "" : GuiConstants.VIDEO_RESIZED,
                    outputVideoFrameDimension.width,
                    outputVideoFrameDimension.height,
                    isFullResolutionPreview ? GuiConstants.VIDEO_FULL_RESOLUTION : ""
            ));

            // Refresh video frames
            this.inputVideoFrame.pack();
            this.outputVideoFrame.pack();
        });
    }
}
