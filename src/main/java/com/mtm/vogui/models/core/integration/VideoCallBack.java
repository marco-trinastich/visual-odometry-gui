/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.integration;

import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

/**
 * Callback for video streams.
 *
 * @author Peter Abeles
 * @author Marco Trinastich
 */
public interface VideoCallBack {

    /**
     * Called when the camera has been initialized and the image properties are known.
     */
    void init(int width, int height, ImageType<? extends ImageBase<?>> imageType);

    /**
     * Passes in the next frame in the sequence. Time in this function should be minimized to avoid causing a
     * backlog in the video image buffer.
     *
     * @param frame      New image frame in BoofCV image format.
     * @param sourceData Platform specific image data.
     * @param timeStamp  Time the video frame was collected.
     */
    void nextFrame(ImageBase<?> frame, Object sourceData, long timeStamp);

    void stop();

    /**
     * Used to inform the video stream if a request has been made to stop processing the video sequence.
     * This function is checked after each call to {@link #nextFrame(ImageBase, Object, long)}.
     *
     * @return true if a request has been made to stop the steam
     */
    boolean stopRequested();

    /**
     * Called when the video stream has stopped.
     */
    void stopped();
}
