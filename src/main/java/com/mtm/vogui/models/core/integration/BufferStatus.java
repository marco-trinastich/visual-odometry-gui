/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.integration;

import com.mtm.vogui.utilities.CommonUtils;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class BufferStatus {
    private String heapInitialSize;
    private String heapMaxSize;
    private String bufferSize;
    private String maxBufferSize;
    private String imageSize;

    private long bufferItems;
    private long maxBufferItems;

    public static @NotNull BufferStatus from(long imageSize, double heapInitialSize, long heapMaxSize, long bufferSize,
                                             double maxBufferSize, long bufferItems, long maxBufferItems) {
        return BufferStatus.builder()
                .imageSize(CommonUtils.getSizeString(imageSize))
                .heapInitialSize(CommonUtils.getSizeString(heapInitialSize))
                .heapMaxSize(CommonUtils.getSizeString(heapMaxSize))
                .bufferSize(CommonUtils.getSizeString(bufferSize))
                .maxBufferSize(CommonUtils.getSizeString((maxBufferSize)))
                .bufferItems(bufferItems)
                .maxBufferItems(maxBufferItems)
                .build();
    }
}
