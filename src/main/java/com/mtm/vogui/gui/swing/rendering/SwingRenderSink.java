/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.rendering;

import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.swing.GuiApplication;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.core.processing.tracking.PointFactory;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.utilities.CommonUtils;
import georegression.struct.point.Point2D_F64;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Swing implementation of the core {@link RenderSink}: the only place where core-produced
 * data meets widgets. Every method marshals to the EDT via {@code SwingUtilities.invokeLater}
 * (dialogs excepted: they block the calling vo worker thread until the user answers).
 * Not a bean: built by {@code gui.RenderSinkProducer} only when the Swing UI is active.
 */
public class SwingRenderSink implements RenderSink {
    private final static int LONGER_RENDER_INTERVAL = 10;
    private final static int DEFAULT_GUI_VIDEO_SIZE = 400;

    private final AppContext context;

    public SwingRenderSink(AppContext context) {
        this.context = context;
    }

    // Dialogs

    @Override
    public void notifyError(String message) {
        JOptionPane.showConfirmDialog(mainFrame(), message, "Error",
                JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean confirmOrCancel(String message) {
        int choice = JOptionPane.showConfirmDialog(mainFrame(), message, "Error",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        return choice == JOptionPane.OK_OPTION;
    }

    // App status

    @Override
    public void renderAppStatus(AppStatus appStatus) {
        var infoPanel = context.state().guiController().infoPanel();
        SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(infoPanel, () -> infoPanel.setAppStatus(appStatus)));
    }

    // Processing lifecycle

    @Override
    public void renderStartPoint(@NotNull ProcessingParameters params) {
        var trackedPoints = context.state().trackedPoints();
        var chartType = params.frozenContext().settings().chart().type();
        var chartYSettings = context.state().guiController().chartYPanel().settings();

        SwingUtilities.invokeLater(() -> {
            // Add start point
            trackedPoints.addElement(params.pointFactory().newStartPoint());
            chartYSettings.axisNames(chartType);
        });
    }

    @Override
    public void renderEndPoint(ProcessingParameters params) {
        var chartXZPanel = context.state().guiController().chartXZPanel();
        var chartYPanel = context.state().guiController().chartYPanel();
        var trackedPoints = context.state().trackedPoints();

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

    @Override
    public void renderClearAllPoints() {
        var chartXZPanel = context.state().guiController().chartXZPanel();
        var chartYPanel = context.state().guiController().chartYPanel();
        var infoPanel = context.state().guiController().infoPanel();
        var trackedPoints = context.state().trackedPoints();

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

    /**
     * Adjust size, position and title of input/output video frames
     */
    @Override
    public void resizeAndRepositionVideoFrames(@NotNull ProcessingParameters params) {
        ImagePanel inputVideoPanel = context.state().guiController().inputVideoPanel();
        JFrame inputVideoFrame = context.state().guiController().inputVideoFrame();
        ImagePanel outputVideoPanel = context.state().guiController().outputVideoPanel();
        JFrame outputVideoFrame = context.state().guiController().outputVideoFrame();

        // Image settings
        var isFullResolutionPreview = params.frozenContext().settings().input().fullResolutionPreview();
        var isResize = params.frozenContext().settings().image().resize();
        var resizeWidth = params.frozenContext().settings().image().resizeWidth();
        var resizeHeight = params.frozenContext().settings().image().resizeHeight();

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

    // Per-frame rendering

    @Override
    public void renderVO(ProcessingStatus status, ProcessingParameters params, boolean voResult) {
        renderInputVideo(status);
        renderTrackedFeatures(status, voResult);
        renderOutputVideo(status);
        renderInfoPanel(status, params, voResult);
        renderCharts(status, params, voResult);
    }

    @Override
    public void renderInputVideo(BufferedImage image) {
        var inputVideoPanel = context.state().guiController().inputVideoPanel();
        var inputVideoFrame = context.state().guiController().inputVideoFrame();

        SwingUtilities.invokeLater(() -> {
            if (inputVideoFrame == null || inputVideoPanel == null)
                return;

            // Input video rendering (if needed)
            if (context.settings().input().inputPreview()) {
                if (!inputVideoFrame.isVisible())
                    inputVideoFrame.setVisible(true);
                inputVideoPanel.setImageRepaint(image);
            } else {
                if (inputVideoFrame.isVisible())
                    inputVideoFrame.setVisible(false);
            }
        });
    }

    @Override
    public void renderCurrentFps(FpsStatus fpsStatus, ProcessingStatus status,
                                 @NotNull ProcessingParameters params) {
        // Running every second

        // Settings
        var sourceType = context.settings().input().source();
        var chartType = context.settings().chart().type();

        // Gui components
        var infoPanel = context.state().guiController().infoPanel();
        var chartYPanel = context.state().guiController().chartYPanel();
        var trackedPoints = context.state().trackedPoints();
        var pointFactory = params.pointFactory();

        // Update input fps
        if (SourceType.Device.is(sourceType)) {
            var deviceFps = context.state().device().getCurrentFPS();
            fpsStatus.inputCurrentFPS(deviceFps);
        }

        // Frozen processing/fps status (so that AWT will later be able to display the correct info)
        FpsStatus frozenFpsStatus = fpsStatus.deepClone();
        ProcessingStatus frozenStatus = status.deepClone();
        frozenStatus.fps(frozenFpsStatus);

        SwingUtilities.invokeLater(() -> {
            infoPanel.setCurrentFps(frozenStatus.fps());

            if (context.state().processing().not(ProcessingState.Paused)) {
                if (ChartType.YSeconds.is(chartType)) {
                    // Add (second,Y) point to y Chart (altitude/second)
                    chartYPanel.addPoint(frozenStatus.fps().totalSeconds(), -frozenStatus.translation().getY());
                    trackedPoints.addElement(pointFactory.newPoint(frozenStatus));
                }
            }
        });
    }

    @Override
    public void renderBufferStatus(BufferStatus bufferStatus) {
        var infoPanel = context.state().guiController().infoPanel();

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

    // Settings healed by the core, reflected into the GUI

    @Override
    public void deviceResolutionChanged(Resolution resolution) {
        SwingUtilities.invokeLater(() -> {
            @SuppressWarnings("unchecked")
            var txtDeviceResolution = (JComboBox<Resolution>) context.state().guiComponents().get("txtDeviceResolution");
            if (txtDeviceResolution != null) {
                txtDeviceResolution.setSelectedItem(resolution);
            }
        });
    }

    @Override
    public void devicePathChanged(DevicePath devicePath) {
        SwingUtilities.invokeLater(() -> {
            @SuppressWarnings("unchecked")
            var txtDevicePath = (JComboBox<DevicePath>) context.state().guiComponents().get("txtDevicePath");
            if (txtDevicePath != null) {
                txtDevicePath.setSelectedItem(devicePath);
            }
        });
    }

    @Override
    public void recentPathUsed(@NotNull RecentPathTarget target, @NotNull PathSettings pathSettings,
                               String usedPath) {
        String comboKey = switch (target) {
            case Calibration -> "txtCalibration";
            case VideoSource -> "txtVideoSource";
        };

        SwingUtilities.invokeLater(() -> {
            @SuppressWarnings("unchecked")
            var pathComboBox = (JComboBox<String>) context.state().guiComponents().get(comboKey);
            if (pathComboBox != null) {
                pathComboBox.setModel(new DefaultComboBoxModel<>(pathSettings.paths()));
                pathComboBox.setSelectedItem(usedPath);
            }
        });
    }

    @Override
    public void kltPyramidLevelsChanged(int pyramidLevels) {
        SwingUtilities.invokeLater(() -> {
            var pyramidLevelsField = (JTextField) context.state().guiComponents().get("txtKltTracker_pyramidLevels");
            if (pyramidLevelsField != null) {
                pyramidLevelsField.setText(String.valueOf(pyramidLevels));
            }
        });
    }

    // Private members

    private JFrame mainFrame() {
        return (JFrame) context.state().guiComponents().get("mainFrame");
    }

    private void renderInputVideo(@NotNull ProcessingStatus status) {
        var sourceType = context.settings().input().source();
        if (SourceType.Video.is(sourceType)) {
            renderInputVideo(status.frame().input().buffered());
        }
    }

    private void renderTrackedFeatures(@NotNull ProcessingStatus status, boolean voResult) {
        if (voResult) {
            // Write on vo (output) buffered image
            Graphics2D g2 = status.frame().vo().buffered().createGraphics();

            // Draw active tracks
            if (context.settings().tracker().showActiveTracks()) {
                for (Point2D_F64 p : status.tracking().trackInliers()) {
                    VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.blue);
                }
            }

            // Draw new tracks
            if (context.settings().tracker().showNewTracks()) {
                for (Point2D_F64 p : status.tracking().trackNew()) {
                    VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.green);
                }
            }

            g2.dispose();
        }
    }

    private void renderOutputVideo(ProcessingStatus status) {
        var outputVideoPanel = context.state().guiController().outputVideoPanel();
        var outputVideoFrame = context.state().guiController().outputVideoFrame();

        SwingUtilities.invokeLater(() -> {
            if (outputVideoFrame == null || outputVideoPanel == null)
                return;
            // Vo process video rendering (output)
            if (!outputVideoFrame.isVisible())
                outputVideoFrame.setVisible(true);
            outputVideoPanel.setImageRepaint(status.frame().vo().buffered());
        });
    }

    private void renderInfoPanel(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                                 boolean voResult) {
        renderMainInfo(status, params, voResult);
    }

    private void renderMainInfo(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                                boolean voResult) {
        // Frozen info
        var sourceType = params.frozenContext().settings().input().source();
        final var deviceFps = SourceType.Device.is(sourceType) ? context.state().device().getAverageFPS() : 0;
        var chartType = params.frozenContext().settings().chart().type();
        var trackedPoints = context.state().trackedPoints();
        var frozenStatus = status.deepClone();

        // Longer interval render
        if (status.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
            // Set previous coords to current
            status.prevTranslation(frozenStatus.translation().copy());
        }

        var infoPanel = context.state().guiController().infoPanel();

        SwingUtilities.invokeLater(() -> CommonUtils.runIfNotNull(infoPanel, () -> {
            // Show info panel
            if (!infoPanel.isInfoPanelVisible()) {
                infoPanel.setInfoPanelVisible(true);
            }

            // Set calibration file
            infoPanel.setCalibrationFile(params.frozenContext().settings().input().calibration().path());

            // Set processed source path
            String sourcePath = SourceType.Video.is(sourceType) ?
                    params.frozenContext().settings().input().video().path() :
                    params.frozenContext().settings().input().device().path().name();
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

    private void renderCharts(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                              boolean voResult) {
        var chartXZPanel = context.state().guiController().chartXZPanel();
        var chartYPanel = context.state().guiController().chartYPanel();
        var chartType = params.frozenContext().settings().chart().type();

        var chartXZScale = context.settings().chart().scaleXZ();
        var chartYScale = context.settings().chart().scaleY();
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
