/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core;

import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import com.mtm.vogui.gui.GuiApplication;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.core.processing.tracking.PointFactory;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.utilities.CommonUtils;
import georegression.struct.point.Point2D_F64;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CoreRendering {
    private final static int LONGER_RENDER_INTERVAL = 10;
    private final static int DEFAULT_GUI_VIDEO_SIZE = 400;


    // Rendering

    /**
     * ResizeAndRepositionVideoFrames
     * Adjust size, position and title of input/output video frames
     */
    public static void resizeAndRepositionVideoFrames(@NotNull Settings settings, @NotNull ProcessingParameters params) {
        ImagePanel inputVideoPanel = settings.state().guiController().inputVideoPanel();
        JFrame inputVideoFrame = settings.state().guiController().inputVideoFrame();
        ImagePanel outputVideoPanel = settings.state().guiController().outputVideoPanel();
        JFrame outputVideoFrame = settings.state().guiController().outputVideoFrame();

        // Image settings
        var isFullResolutionPreview = params.frozenSettings().core().input().fullResolutionPreview();
        var isResize = params.frozenSettings().core().image().resize();
        var resizeWidth = params.frozenSettings().core().image().resizeWidth();
        var resizeHeight = params.frozenSettings().core().image().resizeHeight();

        // Get frame size (defined upon source opening)
        Dimension frameSize = params.frameSize();

        SwingUtilities.invokeLater(() -> {
            // Hide frames if visible
            inputVideoFrame.setVisible(false);
            outputVideoFrame.setVisible(false);

            // Resize frames
            Dimension inputVideoFrameDimension = (isFullResolutionPreview ?
                    frameSize :
                    new Dimension(DEFAULT_GUI_VIDEO_SIZE, DEFAULT_GUI_VIDEO_SIZE));

            Dimension outputVideoFrameDimension = (isFullResolutionPreview ?
                    (!isResize ? frameSize : new Dimension(resizeWidth, resizeHeight)) :
                    new Dimension(DEFAULT_GUI_VIDEO_SIZE, DEFAULT_GUI_VIDEO_SIZE));
            inputVideoPanel.setPreferredSize(inputVideoFrameDimension);
            inputVideoPanel.setCentering(true);
            outputVideoPanel.setPreferredSize(outputVideoFrameDimension);
            outputVideoPanel.setCentering(true);

            // Reposition frames
            inputVideoFrame.setLocationRelativeTo(null);
            inputVideoFrame.setLocation((GuiApplication.getDefaultFrameDimension().width * 2) + 65, 0);
            outputVideoFrame.setLocationRelativeTo(null);
            outputVideoFrame.setLocation((GuiApplication.getDefaultFrameDimension().width * 2) + 65,
                    (GuiApplication.getDefaultFrameDimension().height / 2));

            // Reset frames title
            inputVideoFrame.setTitle(String.format(GuiConstants.INPUT_VIDEO_FRAME_TITLE_PATTERN,
                    frameSize.width,
                    frameSize.height,
                    inputVideoFrameDimension.width,
                    inputVideoFrameDimension.height,
                    isFullResolutionPreview ? GuiConstants.VIDEO_FULL_RESOLUTION : ""
            ));

            outputVideoFrame.setTitle(String.format(GuiConstants.OUTPUT_VIDEO_FRAME_TITLE_PATTERN,
                    !isResize ? frameSize.width : resizeWidth,
                    !isResize ? frameSize.height : resizeHeight,
                    !isResize ? "" : GuiConstants.VIDEO_RESIZED,
                    outputVideoFrameDimension.width,
                    outputVideoFrameDimension.height,
                    isFullResolutionPreview ? GuiConstants.VIDEO_FULL_RESOLUTION : ""
            ));

            // Refresh video frames
            inputVideoFrame.pack();
            outputVideoFrame.pack();
        });
    }

    public static void renderStartPoint(@NotNull Settings settings, @NotNull ProcessingParameters params) {
        var trackedPoints = settings.state().trackedPoints();
        var chartType = params.frozenSettings().core().chart().type();
        var chartYSettings = settings.state().guiController().chartYPanel().settings();

        SwingUtilities.invokeLater(() -> {
            // Add start point
            trackedPoints.addElement(params.pointFactory().newStartPoint());
            chartYSettings.axisNames(chartType);
        });
    }

    public static void renderEndPoint(@NotNull Settings settings, ProcessingParameters params) {
        var chartXZPanel = settings.state().guiController().chartXZPanel();
        var chartYPanel = settings.state().guiController().chartYPanel();
        var trackedPoints = settings.state().trackedPoints();

        SwingUtilities.invokeLater(() -> {
            if (chartXZPanel.hasPointsLastChart() && chartYPanel.hasPointsLastChart()) {
                chartXZPanel.closeChart();
                chartYPanel.closeChart();
                trackedPoints.addElement(params.pointFactory().newEndPoint());
            } else {
                PointFactory.removeLastChart(trackedPoints);
            }
        });
    }

    public static void renderClearAllPoints(@NotNull Settings settings) {
        var chartXZPanel = settings.state().guiController().chartXZPanel();
        var chartYPanel = settings.state().guiController().chartYPanel();
        var infoPanel = settings.state().guiController().infoPanel();
        var trackedPoints = settings.state().trackedPoints();

        SwingUtilities.invokeLater(() -> {
            // Clear and reset charts and info panel
            trackedPoints.clear();
            chartXZPanel.clearAllPoints();
            chartYPanel.clearAllPoints();
            chartXZPanel.resetSize();
            chartYPanel.resetSize();
            infoPanel.setInfoPanelVisible(false);
        });
    }

    public static void renderVO(Settings settings, ProcessingStatus status, ProcessingParameters params,
                                boolean voResult) {
        renderInputVideo(settings, status);
        renderTrackedFeatures(settings, status, voResult);
        renderOutputVideo(settings, status);
        renderInfoPanel(settings, status, params, voResult);
        renderCharts(settings, status, params, voResult);
    }

    public static void renderInputVideo(@NotNull Settings settings, BufferedImage image) {
        var inputVideoPanel = settings.state().guiController().inputVideoPanel();
        var inputVideoFrame = settings.state().guiController().inputVideoFrame();

        SwingUtilities.invokeLater(() -> {
            if (inputVideoFrame == null || inputVideoPanel == null)
                return;

            // Input video rendering (if needed)
            if (settings.core().input().inputPreview()) {
                if (!inputVideoFrame.isVisible())
                    inputVideoFrame.setVisible(true);
                inputVideoPanel.setImageRepaint(image);
            } else {
                if (inputVideoFrame.isVisible())
                    inputVideoFrame.setVisible(false);
            }
        });
    }

    public static void renderCurrentFps(@NotNull Settings settings, FpsStatus fpsStatus, ProcessingStatus status,
                                        @NotNull ProcessingParameters params) {
        // Running every second

        // Settings
        var sourceType = settings.core().input().source();
        var chartType = settings.core().chart().type();

        // Gui components
        var infoPanel = settings.state().guiController().infoPanel();
        var chartYPanel = settings.state().guiController().chartYPanel();
        var trackedPoints = settings.state().trackedPoints();
        var pointFactory = params.pointFactory();

        // Update input fps
        if (SourceType.Device.is(sourceType)) {
            var deviceFps = settings.state().device().getCurrentFPS();
            fpsStatus.inputCurrentFPS(deviceFps);
        }

        // Frozen processing/fps status (so that AWT will later be able to display the correct info)
        FpsStatus frozenFpsStatus = fpsStatus.deepClone();
        ProcessingStatus frozenStatus = status.deepClone();
        frozenStatus.fps(frozenFpsStatus);

        SwingUtilities.invokeLater(() -> {
            infoPanel.setCurrentFps(frozenStatus.fps());

            if (settings.state().processing().not(ProcessingState.Paused)) {
                if (ChartType.YSeconds.is(chartType)) {
                    // Add (second,Y) point to y Chart (altitude/second)
                    chartYPanel.addPoint(frozenStatus.fps().totalSeconds(), -frozenStatus.translation().getY());
                    trackedPoints.addElement(pointFactory.newPoint(frozenStatus));
                }
            }
        });
    }

    public static void renderAppStatus(@NotNull Settings settings) {
        renderAppStatus(settings, (Exception) null);
    }

    public static void renderAppStatus(@NotNull Settings settings, Exception processingEx) {
        var state = settings.state().processing().get();
        renderAppStatus(settings, AppStatus.from(state, processingEx));
    }

    public static void renderAppStatus(@NotNull Settings settings, AppStatus appStatus) {
        var infoPanel = settings.state().guiController().infoPanel();
        SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(infoPanel, () -> infoPanel.setAppStatus(appStatus)));
    }

    public static void renderBufferStatus(@NotNull Settings settings, BufferStatus bufferStatus) {
        var infoPanel = settings.state().guiController().infoPanel();

        if (bufferStatus == null) {
            if (infoPanel.isBufferInfoVisible()) {
                // Hide buffer info
                SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(infoPanel,
                        () -> infoPanel.setBufferInfoVisible(false)));
            }
        } else {
            var isInfiniteBuffer = bufferStatus.maxBufferItems() == AwaitableBuffer.INFINITE_BUFFER;

            // Render buffer info
            SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(infoPanel, () -> {
                if (!infoPanel.isBufferInfoVisible()) {
                    infoPanel.setBufferInfoVisible(true);
                }

                infoPanel.setBufferProgressBar(bufferStatus, isInfiniteBuffer);
                infoPanel.setBufferLabel(bufferStatus, isInfiniteBuffer);
            }));
        }
    }

    // Private members

    private static void renderInputVideo(@NotNull Settings settings, @NotNull ProcessingStatus status) {
        var sourceType = settings.core().input().source();
        if (SourceType.Video.is(sourceType)) {
            renderInputVideo(settings, status.frame().input().buffered());
        }
    }

    private static void renderTrackedFeatures(@NotNull Settings settings, @NotNull ProcessingStatus status,
                                              boolean voResult) {
        if (voResult) {
            // Write on vo (output) buffered image
            Graphics2D g2 = status.frame().vo().buffered().createGraphics();

            // Draw active tracks
            if (settings.core().tracker().isTrackerShowActiveTracks()) {
                for (Point2D_F64 p : status.tracking().trackInliers()) {
                    VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.blue);
                }
            }

            // Draw new tracks
            if (settings.core().tracker().isTrackerShowNewTracks()) {
                for (Point2D_F64 p : status.tracking().trackNew()) {
                    VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.green);
                }
            }

            g2.dispose();
        }
    }

    private static void renderOutputVideo(@NotNull Settings settings, ProcessingStatus status) {
        var outputVideoPanel = settings.state().guiController().outputVideoPanel();
        var outputVideoFrame = settings.state().guiController().outputVideoFrame();

        SwingUtilities.invokeLater(() -> {
            if (outputVideoFrame == null || outputVideoPanel == null)
                return;
            // Vo process video rendering (output)
            if (!outputVideoFrame.isVisible())
                outputVideoFrame.setVisible(true);
            outputVideoPanel.setImageRepaint(status.frame().vo().buffered());
        });
    }

    private static void renderInfoPanel(@NotNull Settings settings, @NotNull ProcessingStatus status,
                                        @NotNull ProcessingParameters params, boolean voResult) {
        renderMainInfo(settings, status, params, voResult);
        //renderBufferInfo(settings);
    }

    private static void renderMainInfo(@NotNull Settings settings, @NotNull ProcessingStatus status,
                                       @NotNull ProcessingParameters params, boolean voResult) {
        // Frozen info
        var sourceType = params.frozenSettings().core().input().source();
        final var deviceFps = SourceType.Device.is(sourceType) ? settings.state().device().getAverageFPS() : 0;
        var chartType = params.frozenSettings().core().chart().type();
        var trackedPoints = settings.state().trackedPoints();
        var frozenStatus = status.deepClone();

        // Longer interval render
        if (status.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
            // Set previous coords to current
            status.prevTranslation(frozenStatus.translation().copy());
        }

        var infoPanel = settings.state().guiController().infoPanel();

        SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(infoPanel, () -> {
            // Show info panel
            if (!infoPanel.isInfoPanelVisible()) {
                infoPanel.setInfoPanelVisible(true);
            }

            // Set calibration file
            infoPanel.setCalibrationFile(params.frozenSettings().core().input().calibration().path());

            // Set processed source path
            String sourcePath = SourceType.Video.is(sourceType) ?
                    params.frozenSettings().core().input().video().path() :
                    params.frozenSettings().core().input().device().path().name();
            infoPanel.setProcessedSource(sourcePath, sourceType);

            // Update processed frames
            infoPanel.setProcessedFrames(frozenStatus.fps().totalProcessed(), frozenStatus.fps().totalFrames());

            // Update elapsed time
            infoPanel.setElapsedTime(frozenStatus.fps().totalSeconds());

            // Update position (x/y/z)
            infoPanel.setPosition(frozenStatus.translation());

            // Update rotation matrix
            infoPanel.setRotation(frozenStatus.rotation());

            // Update tracking status
            infoPanel.setTrackingStatus(frozenStatus.tracking());

            // Set input/vo average fps
            if (SourceType.Device.is(sourceType)) {
                // For video device update input average fps, since it can differ from processing rate
                frozenStatus.fps().inputAverageFPS(deviceFps);
            }
            infoPanel.setAverageFps(frozenStatus.fps());

            if (voResult) {
                // Update tracked points list
                Double loggedY = ChartType.YSeconds.is(chartType) ? null : -frozenStatus.translation().getY();
                trackedPoints.addElement(params.pointFactory().newPoint(frozenStatus, loggedY));
            }

            // Longer interval render --> items below are rendered on a slower basis for them to be more meaningful
            if (frozenStatus.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
                // Update rotation/altitude panel
                infoPanel.setDirectionPanels(frozenStatus.translation(), frozenStatus.prevTranslation());

                // Update covered distance
                infoPanel.setIncrementalDistance(frozenStatus.translation(), frozenStatus.prevTranslation());
            }
        }));
    }

    private static void renderCharts(@NotNull Settings settings, @NotNull ProcessingStatus status,
                                     @NotNull ProcessingParameters params, boolean voResult) {
        var chartXZPanel = settings.state().guiController().chartXZPanel();
        var chartYPanel = settings.state().guiController().chartYPanel();
        var chartType = params.frozenSettings().core().chart().type();

        var chartXZScale = settings.core().chart().scaleXZ();
        var chartYScale = settings.core().chart().scaleY();
        var frozenStatus = status.deepClone();

        // Charts rendering

        if (voResult) {
            SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(() -> {
                // Update xz chart scale factor (real-time)
                if (chartXZPanel.settings().chartScale() != chartXZScale) {
                    chartXZPanel.settings().chartScale(chartXZScale);
                    chartXZPanel.resetSize();
                }

                // Update y chart scale factor (real-time)
                if (chartYPanel.settings().chartScale() != chartYScale) {
                    chartYPanel.settings().chartScale(chartYScale);
                    chartYPanel.resetSize();
                }

                // Add estimated xz point (2D translation)
                chartXZPanel.addPoint(frozenStatus.translation().getX(), frozenStatus.translation().getZ());

                if (ChartType.YFrames.is(chartType)) {
                    // Add estimated y point (altitude/frame)
                    chartYPanel.addPoint(frozenStatus.fps().totalProcessed(), -frozenStatus.translation().getY());
                }
            }, chartXZPanel, chartYPanel));
        }
    }
}
