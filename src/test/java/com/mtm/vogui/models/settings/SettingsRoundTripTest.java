/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings;

import com.mtm.vogui.models.config.Config;
import com.mtm.vogui.models.settings.core.CoreSettings;
import com.mtm.vogui.models.settings.core.chart.ChartSettings;
import com.mtm.vogui.models.settings.core.image.ImageSettings;
import com.mtm.vogui.models.settings.core.input.InputSettings;
import com.mtm.vogui.models.settings.core.input.calibration.CalibrationSettings;
import com.mtm.vogui.models.settings.core.input.device.DeviceSettings;
import com.mtm.vogui.models.settings.core.input.device.boofcv.BoofCvCameraSettings;
import com.mtm.vogui.models.settings.core.input.device.v4l4j.V4l4jCameraSettings;
import com.mtm.vogui.models.settings.core.input.video.VideoSettings;
import com.mtm.vogui.models.settings.core.tracker.TrackerSettings;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;
import com.mtm.vogui.models.settings.core.visualodometry.monoplaneinfinity.MonoPlaneInfinitySettings;
import com.mtm.vogui.models.settings.core.visualodometry.monoplaneoverhead.MonoPlaneOverheadSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Save/load round-trip of {@link Settings} on disk (xml and dat formats),
 * without booting the Quarkus application.
 */
class SettingsRoundTripTest {

    private static final String TEST_FILE_NAME = "target/settings-roundtrip-test";
    private static final String[] TEST_VIDEO_PATHS = new String[]{"assets/example_a.mjpeg", "assets/example_b.mp4"};

    private final Config config = () -> new Config.Settings() {
        @Override
        public String fileName() {
            return TEST_FILE_NAME;
        }

        @Override
        public String[] allowedXmlClasses() {
            return new String[]{"com.mtm.vogui.**", "java.**", "boofcv.**", "org.apache.commons.**"};
        }
    };

    @BeforeEach
    void deleteTestFiles() throws IOException {
        Settings settings = newSettings();
        Files.deleteIfExists(settings.xmlPath());
        Files.deleteIfExists(settings.datPath());
    }

    @Test
    void xmlRoundTrip() {
        Settings saved = newSettings();
        customize(saved);
        assertTrue(saved.saveToXml());

        Settings loaded = newSettings();
        assertTrue(loaded.loadFromXml());
        assertCustomized(loaded);
    }

    @Test
    void datRoundTrip() {
        Settings saved = newSettings();
        customize(saved);
        assertTrue(saved.saveToDat());

        Settings loaded = newSettings();
        assertTrue(loaded.loadFromDat());
        assertCustomized(loaded);
    }

    private void customize(Settings settings) {
        settings.core().input().video().paths(TEST_VIDEO_PATHS);
        settings.core().input().video().path(TEST_VIDEO_PATHS[0]);
        settings.core().tracker().setKltTracker_maxFeatures(123);
        settings.core().chart().scaleXZ(42.0);
    }

    private void assertCustomized(Settings settings) {
        assertArrayEquals(TEST_VIDEO_PATHS, settings.core().input().video().paths());
        assertEquals(TEST_VIDEO_PATHS[0], settings.core().input().video().path());
        assertEquals(123, settings.core().tracker().getKltTracker_maxFeatures());
        assertEquals(42.0, settings.core().chart().scaleXZ());
    }

    private Settings newSettings() {
        return new Settings(newCore(), null, config);
    }

    private CoreSettings newCore() {
        return new CoreSettings(
                new InputSettings(new CalibrationSettings(), new VideoSettings(),
                        new DeviceSettings(new BoofCvCameraSettings(), new V4l4jCameraSettings())),
                new ImageSettings(),
                new TrackerSettings(),
                new VisualOdometrySettings(new MonoPlaneInfinitySettings(), new MonoPlaneOverheadSettings()),
                new ChartSettings());
    }
}
