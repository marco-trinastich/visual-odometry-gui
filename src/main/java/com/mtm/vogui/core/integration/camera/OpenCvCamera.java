/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.camera;

import boofcv.io.webcamcapture.UtilWebcamCapture;
import com.github.sarxos.webcam.Webcam;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.settings.Settings;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
//                FrameGrabber grabber = new OpenCVFrameGrabber(0);
//                grabber.start();
//                Frame frame = grabber.grab();

public class OpenCvCamera extends BufferedCamera {

    private Webcam webcam;
    private Exception captureException;

    private OpenCvCamera(Settings settings, Consumer<BufferedImage> guiRenderer,
                         Consumer<BufferStatus> bufferRenderer) {
        super(settings, guiRenderer, bufferRenderer);
        this.captureException = null;
    }

    @Override
    public BufferedCamera start() {
        if (!this.running.get()) {
            this.captureException = null;

            // Clear buffer
            this.clearBuffer();

            // Start device
            var input = this.settings.core().input();
            this.webcam = UtilWebcamCapture.openDefault(input.device().targetWidth(), input.device().targetHeight());
            Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.BOOFCV_CAMERA_THREAD))
                    .submit(this::captureCycle);

            // Notify running state
            this.running.set(true);
        }
        return this;
    }

    @Override
    public void stop() throws CameraException {
        if (this.running.get()) {
            // Stop device and wait until stopped (this is guaranteed to happen)
            this.stopRequested.set(true);
            this.running.waitUntil(false);
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running and buffer lock)
        this.captureStopped();

        if (this.captureException != null) {
            throw new CameraException();
        }
    }

    @Override
    public double getCurrentFPS() {
        if (this.webcam != null) {
            return this.webcam.getFPS();
        }
        return 0;
    }

    @Override
    public double getAverageFPS() {
        if (this.webcam != null) {
            return this.webcam.getFPS();
        }
        return 0;
    }

    @Override
    public Dimension getFrameSize() {
        if (this.webcam != null) {
            return webcam.getViewSize();
        }
        return null;
    }

    private void captureCycle() {
        try {
            while (this.webcam.isOpen() && !this.stopRequested.get()) {
                this.fillBufferAndRender(this.webcam.getImage());
            }
        } catch (Exception ex) {
            Log.errorf(Messages.DEVICE_BOOFCV_CAPTURE_ERROR, ex.getMessage());
            this.captureException = ex;
        }

        try {
            // Close device
            this.webcam.close();
        } catch (Exception ex) {
            Log.errorf(Messages.DEVICE_BOOFCV_CLOSE_ERROR, ex.getMessage());
            this.captureException = ex;
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running and buffer lock)
        this.captureStopped();
    }

    public static @NotNull OpenCvCamera from(Settings settings, Consumer<BufferedImage> guiRenderer,
                                             Consumer<BufferStatus> bufferRenderer) {
        return new OpenCvCamera(settings, guiRenderer, bufferRenderer);
    }
}
