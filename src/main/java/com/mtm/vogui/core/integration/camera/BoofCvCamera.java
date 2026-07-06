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
import com.mtm.vogui.models.core.exceptions.CameraStartException;
import com.mtm.vogui.models.settings.Settings;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BoofCvCamera extends BufferedCamera {

    private Webcam webcam;
    private Exception captureException;

    private BoofCvCamera(Settings settings, Consumer<BufferedImage> guiRenderer,
                         Consumer<BufferStatus> bufferRenderer) {
        super(settings, guiRenderer, bufferRenderer);
        this.captureException = null;
    }

    @Override
    public BufferedCamera start() throws CameraStartException {
        if (!this.running.get()) {
            this.captureException = null;

            // Clear buffer
            this.clearBuffer();

            // Start device
            var input = this.settings.core().input();
            this.webcam = input.device().boofCv().webcam();
            if (this.webcam == null) {
                throw new CameraStartException();
            }
            this.adjustResolution(input.device().targetWidth(), input.device().targetHeight());
            this.webcam.open();
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

    private void adjustResolution(int targetWidth, int targetHeight) {
        // The AVFoundation-based native driver only starts with resolutions the device advertises,
        // so pick the closest supported one instead of forcing a custom size like UtilWebcamCapture
        var sizes = this.webcam.getViewSizes();
        if (sizes == null || sizes.length == 0) {
            UtilWebcamCapture.adjustResolution(this.webcam, targetWidth, targetHeight);
            return;
        }
        var best = Arrays.stream(sizes)
                .min(Comparator.comparingLong(size ->
                        (long) (size.width - targetWidth) * (size.width - targetWidth)
                                + (long) (size.height - targetHeight) * (size.height - targetHeight)))
                .orElse(sizes[0]);
        this.webcam.setViewSize(best);
    }

    private void captureCycle() {
        try {
            while (this.webcam.isOpen() && !this.stopRequested.get()) {
                var image = this.webcam.getImage();
                if (image == null) {
                    // The device may not have produced a frame yet
                    Thread.sleep(5);
                    continue;
                }
                this.fillBufferAndRender(image);
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

    public static @NotNull BoofCvCamera from(Settings settings, Consumer<BufferedImage> guiRenderer,
                                             Consumer<BufferStatus> bufferRenderer) {
        return new BoofCvCamera(settings, guiRenderer, bufferRenderer);
    }
}
