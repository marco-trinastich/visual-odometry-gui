/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.bridge;

import au.edu.jcu.v4l4j.*;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.*;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.integration.VideoCallBack;
import com.mtm.vogui.utilities.LogUtils;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Wrapper around V4L4J for processing videos with BoofCv.
 * </p>
 */
public class V4l4jVideo extends WindowAdapter implements CaptureCallback {

    private FrameGrabber frameGrabber;
    private VideoDevice videoDevice;
    private VideoCallBack videoCallBack;
    private ImageType<? extends ImageBase<?>> imageType;
    private Exception captureException;

    // Controls
    private boolean ctrlSustainFps = false;
    private boolean ctrlTimeoutImage = false;
    private boolean ctrlKeepFormat = false;

    @Setter
    private boolean convertBufferedImage = false;

    public V4l4jVideo() {
        this.captureException = null;
    }

    public boolean start(String devicePath, int width, int height, ImageType<? extends ImageBase<?>> imageType,
                         VideoCallBack coreCallback) {
        this.captureException = null;
        this.videoCallBack = coreCallback;

        // Init video device
        try {
            this.initFrameGrabber(devicePath, width, height, imageType);
        } catch (V4L4JException ex) {
            LogUtils.errorf(ex, Messages.DEVICE_SETUP_ERROR, ex.getMessage());
            this.stopCapture();
            return false;
        }

        // Start capture
        try {
            this.frameGrabber.startCapture();
            return true;
        } catch (V4L4JException ex) {
            LogUtils.errorf(ex, Messages.DEVICE_INIT_ERROR, ex.getMessage());
            this.stopCapture();
            return false;
        }
    }

    /**
     * Initialize FrameGrabber
     *
     * @throws V4L4JException if any parameter is invalid
     */
    private void initFrameGrabber(String devicePath, int width, int height,
                                  @NotNull ImageType<? extends ImageBase<?>> imageType)
            throws V4L4JException {
        // Set video device
        this.videoDevice = new VideoDevice(devicePath);

        // Set frame grabber
        int channel = 0;
        this.frameGrabber = this.videoDevice.getJPEGFrameGrabber(width, height, channel, V4L4JConstants.STANDARD_WEBCAM, 80);
        this.frameGrabber.setCaptureCallback(this);

        // Enable device controls
        this.setControlsEnabled(true);

        // Run init callback;
        this.imageType = imageType;
        this.videoCallBack.init(frameGrabber.getWidth(), frameGrabber.getHeight(), imageType);
    }

    @Override
    public void nextFrame(VideoFrame frame) {
        // This method is called when a new frame is ready.
        ImageBase<?> leftImg = null;
        if (this.convertBufferedImage) {
            leftImg = ConvertBufferedImage.convertFrom(frame.getBufferedImage(), this.imageType.getClass(), true);
        }
        this.videoCallBack.nextFrame(leftImg, frame.getBufferedImage(), frame.getCaptureTime());

        // recycle the frame
        frame.recycle();

        if (this.videoCallBack.stopRequested()) {
            this.stopCapture();
        }
    }

    /**
     * Cleanup capture
     * <p/>
     * This method stops the capture and releases frame grabber and related resources
     */
    public void stopCapture() {
        try {
            this.frameGrabber.stopCapture();
        } catch (Exception ex) {
            // ex, not ex.getMessage(): driver exceptions often carry no message at all
            Log.errorf(Messages.DEVICE_V4L4J_CLOSE_ERROR, ex);
            // this error may be thrown if the frame grabber is already stopped, but it will be notified anyway since
            // this should not happen
            this.captureException = ex;
        }

        try {
            // disable controls
            this.setControlsEnabled(false);
        } catch (Exception ex) {
            Log.errorf(Messages.DEVICE_V4L4J_CLOSE_ERROR, ex);
            this.captureException = ex;
        }

        try {
            // release the frame grabber and video device
            this.videoDevice.releaseFrameGrabber();
            this.videoDevice.release();
        } catch (Exception ex) {
            Log.errorf(Messages.DEVICE_V4L4J_CLOSE_ERROR, ex);
            this.captureException = ex;
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running lock)
        this.videoCallBack.stopped();
    }

    /**
     * Window closing event
     * </p>
     * Close camera before exiting
     *
     * @param e window event
     */
    public void windowClosing(WindowEvent e) {
        this.stopCapture();
    }

    @Override
    public void exceptionReceived(@NotNull V4L4JException ex) {
        // This method is called by v4l4j if an exception occurs while waiting for a new frame to be ready.
        // The exception is available through e.getCause()
        LogUtils.errorf(ex, Messages.DEVICE_V4L4J_ERROR, ex.getMessage(), ex.getCause());
        this.captureException = ex;
        this.stopCapture();
    }

    public boolean hasException() {
        return this.captureException != null;
    }

    public Exception getException() {
        return this.captureException;
    }

    public void setControlsActive(boolean sustainFps, boolean timeoutImage, boolean keepFormat) {
        this.ctrlSustainFps = sustainFps;
        this.ctrlTimeoutImage = timeoutImage;
        this.ctrlKeepFormat = keepFormat;
    }

    public void setAllControlsActive() {
        this.ctrlSustainFps = this.ctrlTimeoutImage = this.ctrlKeepFormat = true;
    }

    private void setControlsEnabled(boolean enabled) throws V4L4JException {
        if (this.ctrlSustainFps || this.ctrlTimeoutImage || this.ctrlKeepFormat) {
            boolean controlFound = false;
            List<Control> controls = this.videoDevice.getControlList().getList();
            for (Control control : controls) {
                if (control.getName().equalsIgnoreCase("sustain_framerate") && this.ctrlSustainFps) {
                    control.setValue(enabled ? 1 : 0);
                    controlFound = true;
                } else if (control.getName().equalsIgnoreCase("timeout_image_io") &&
                        this.ctrlTimeoutImage) {
                    control.setValue(enabled ? 1 : 0);
                    controlFound = true;
                } else if (control.getName().equalsIgnoreCase("keep_format") && this.ctrlKeepFormat) {
                    control.setValue(enabled ? 1 : 0);
                    controlFound = true;
                }
            }

            if (!controlFound) {
                throw new V4L4JException(Messages.DEVICE_V4L4J_MISSING_CONTROLS);
            }

            if (!enabled) {
                this.videoDevice.releaseControlList();
                this.ctrlSustainFps = this.ctrlTimeoutImage = this.ctrlKeepFormat = false;
            }
        }
    }

    /**
     * Queries the discrete JPEG-encodable resolutions advertised by a V4L device
     * without starting a capture. Returns an empty list when the device (or the
     * native library) is unavailable, so callers can fall back to a static list.
     */
    public static List<Dimension> listViewSizes(String devicePath) {
        if (!OSUtils.isUnix()) {
            // V4L4J is Linux-only: expected on other platforms, callers fall back quietly
            Log.debugf("V4L4J view sizes discovery skipped for %s: not a Linux platform", devicePath);
            return List.of();
        }

        VideoDevice device = null;
        try {
            device = new VideoDevice(devicePath);
            return device.getDeviceInfo().getFormatList().getJPEGEncodableFormats().stream()
                    .map(ImageFormat::getResolutionInfo)
                    .filter(info -> info.getType() == ResolutionInfo.Type.DISCRETE)
                    .flatMap(info -> info.getDiscreteResolutions().stream())
                    .map(resolution -> new Dimension(resolution.getWidth(), resolution.getHeight()))
                    .distinct()
                    .toList();
        } catch (Throwable exc) {
            Log.warnf("V4L4J view sizes discovery unavailable for %s: %s", devicePath, exc.getMessage());
            return List.of();
        } finally {
            if (device != null) {
                try {
                    device.release();
                } catch (Throwable _) {
                    // the device may already be in use by an active capture
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            V4l4jVideo camera = new V4l4jVideo();
            camera.start("/dev/video0", 320, 240,
                    new ImageType<>(ImageType.Family.GRAY, ImageDataType.U8, 1),
                    new VideoCallBack() {
                        @Override
                        public void init(int width, int height, ImageType<? extends ImageBase<?>> imageType) {
                        }

                        @Override
                        public void nextFrame(ImageBase<?> frame, Object sourceData, long timeStamp) {
                        }

                        @Override
                        public void stop() {
                        }

                        @Override
                        public boolean stopRequested() {
                            return false;
                        }

                        @Override
                        public void stopped() {
                        }
                    });
        });
    }
}
