/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.bridge;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.integration.VideoCallBack;
import com.mtm.vogui.utilities.LogUtils;
import io.quarkus.logging.Log;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;

/**
 * Wrapper around the JavaCV OpenCV frame grabber (OpenCv capture path).
 * <p/>
 * Plays the role the native capture engine plays for V4L4J: owns the grabber,
 * pumps frames to the {@link VideoCallBack} from a dedicated thread, and stops
 * the capture when the callback requests it.
 */
public class OpenCvVideo {

    private OpenCVFrameGrabber grabber;
    private final Java2DFrameConverter converter;
    private VideoCallBack videoCallBack;
    private Exception captureException;

    public OpenCvVideo() {
        this.converter = new Java2DFrameConverter();
        this.captureException = null;
    }

    public boolean start(int deviceIndex, int width, int height, VideoCallBack coreCallback) {
        this.captureException = null;
        this.videoCallBack = coreCallback;

        // Init and start the grabber (synchronously waits for the first real frame)
        BufferedImage firstImage;
        try {
            this.grabber = new OpenCVFrameGrabber(deviceIndex);
            // Requested size: the driver adjusts it, the granted one is read back from the frames
            this.grabber.setImageWidth(width);
            this.grabber.setImageHeight(height);
            this.grabber.start();
            firstImage = this.nextImage();
            if (firstImage == null) {
                throw new IllegalStateException(Messages.DEVICE_OPENCV_NO_FRAME);
            }
        } catch (Throwable exc) {
            // Throwable: missing OpenCV natives surface as Errors, not Exceptions
            LogUtils.errorf(exc, Messages.DEVICE_SETUP_ERROR, exc.getMessage());
            this.stopCapture();
            return false;
        }

        // Run init callback with the size actually granted by the driver
        this.videoCallBack.init(firstImage.getWidth(), firstImage.getHeight(), null);

        // Start capture thread
        Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.OPENCV_CAMERA_THREAD))
                .submit(() -> this.captureCycle(firstImage));
        return true;
    }

    private void captureCycle(BufferedImage firstImage) {
        try {
            BufferedImage capturedImage = firstImage;
            while (!this.videoCallBack.stopRequested()) {
                if (capturedImage != null) {
                    // A frame with no image (grab() itself throws on failure) is just skipped
                    this.videoCallBack.nextFrame(null, capturedImage, System.currentTimeMillis());
                }
                capturedImage = this.nextImage();
            }
        } catch (Exception ex) {
            // ex, not ex.getMessage(): driver exceptions often carry no message at all
            Log.errorf(Messages.DEVICE_OPENCV_CAPTURE_ERROR, ex);
            this.captureException = ex;
        }

        this.stopCapture();
    }

    /**
     * Grabs the next frame and converts it to a standalone image: the converter
     * reuses its backing image across frames, so each one is deep-copied before
     * entering the buffer.
     */
    private BufferedImage nextImage() throws Exception {
        Frame frame = this.grabber.grab();
        if (frame == null || frame.image == null) {
            return null;
        }
        return Java2DFrameConverter.cloneBufferedImage(this.converter.convert(frame));
    }

    /**
     * Cleanup capture
     * <p/>
     * This method stops the capture and releases the grabber
     */
    public void stopCapture() {
        try {
            if (this.grabber != null) {
                this.grabber.close();
                this.grabber = null;
            }
        } catch (Exception ex) {
            Log.errorf(Messages.DEVICE_OPENCV_CLOSE_ERROR, ex);
            this.captureException = ex;
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running lock)
        this.videoCallBack.stopped();
    }

    public boolean hasException() {
        return this.captureException != null;
    }

    public Exception getException() {
        return this.captureException;
    }
}
