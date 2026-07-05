/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.device.v4l4j;

import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.utilities.CommonUtils;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * V4l4j camera settings
 * <p/>
 * Options related to V4l4j device.
 * Only the selected device path is persisted: the list of the available
 * device nodes is discovered by scanning {@code /dev/video*}, never loaded from disk.
 */
@Data
@Dependent
public class V4l4jCameraSettings implements Serializable, WithDefault<V4l4jCameraSettings> {

    private static final Path DEVICES_DIRECTORY = Path.of("/dev");
    private static final Pattern DEVICE_NODE_PATTERN = Pattern.compile("/dev/video(\\d+)");

    // Common V4L device nodes, used as fallback when discovery finds none
    private static final String[] DEFAULT_DEVICE_NODES = {
            "/dev/video0",
            "/dev/video1"
    };

    private String path;
    private boolean sustainFramerate;
    private boolean timeoutImageIO;
    private boolean keepFormat;

    @Inject
    public V4l4jCameraSettings() {
        this.loadDefaults();
    }

    public V4l4jCameraSettings(@NotNull V4l4jCameraSettings v4l4j) {
        this.path = v4l4j.path != null ? v4l4j.path : CommonUtils.getStringArrayFirst(v4l4j.paths());
        this.sustainFramerate = v4l4j.sustainFramerate;
        this.timeoutImageIO = v4l4j.timeoutImageIO;
        this.keepFormat = v4l4j.keepFormat;
    }

    public String[] paths() {
        // Discovers available V4L device nodes, falling back to the common ones when none are found
        try (Stream<Path> deviceNodes = Files.list(DEVICES_DIRECTORY)) {
            String[] discovered = deviceNodes
                    .map(Path::toString)
                    .filter(node -> DEVICE_NODE_PATTERN.matcher(node).matches())
                    .sorted(Comparator.comparingInt(V4l4jCameraSettings::deviceNodeNumber))
                    .toArray(String[]::new);
            return discovered.length > 0 ? discovered : DEFAULT_DEVICE_NODES.clone();
        } catch (IOException exc) {
            return DEFAULT_DEVICE_NODES.clone();
        }
    }

    public void loadDefaults() {
        this.path = CommonUtils.getStringArrayFirst(this.paths());
        this.sustainFramerate(false);
        this.timeoutImageIO(false);
        this.keepFormat(false);
    }

    private static int deviceNodeNumber(String deviceNode) {
        var matcher = DEVICE_NODE_PATTERN.matcher(deviceNode);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
    }
}
