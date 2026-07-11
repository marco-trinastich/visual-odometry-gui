/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings;

import com.mtm.vogui.models.config.Config;
import com.mtm.vogui.models.settings.core.CoreSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Save/load round-trip of {@link Settings} on disk (json and yaml formats),
 * without booting the Quarkus application.
 */
class SettingsRoundTripTest {

    private static final String TEST_FILE_NAME = "target/settings-roundtrip-test";
    private static final String[] TEST_VIDEO_PATHS = new String[]{"assets/example_a.mjpeg", "assets/example_b.mp4"};

    private final Config config = () -> () -> TEST_FILE_NAME;

    @BeforeEach
    void deleteTestFiles() throws IOException {
        Settings settings = newSettings();
        Files.deleteIfExists(settings.jsonPath());
        Files.deleteIfExists(settings.yamlPath());
    }

    @Test
    void jsonRoundTrip() {
        Settings saved = newSettings();
        customize(saved);
        assertTrue(saved.saveToJson());

        Settings loaded = newSettings();
        assertTrue(loaded.loadFromJson());
        assertCustomized(loaded);
    }

    @Test
    void yamlRoundTrip() {
        Settings saved = newSettings();
        customize(saved);
        assertTrue(saved.saveToYaml());

        Settings loaded = newSettings();
        assertTrue(loaded.loadFromYaml());
        assertCustomized(loaded);
    }

    private void customize(Settings settings) {
        settings.core().input().video().paths(TEST_VIDEO_PATHS);
        settings.core().input().video().path(TEST_VIDEO_PATHS[0]);
        settings.core().tracker().klt().maxFeatures(123);
        settings.core().chart().scaleXZ(42.0);
    }

    private void assertCustomized(Settings settings) {
        assertArrayEquals(TEST_VIDEO_PATHS, settings.core().input().video().paths());
        assertEquals(TEST_VIDEO_PATHS[0], settings.core().input().video().path());
        assertEquals(123, settings.core().tracker().klt().maxFeatures());
        assertEquals(42.0, settings.core().chart().scaleXZ());
    }

    private Settings newSettings() {
        return new Settings(new CoreSettings(), null, config);
    }
}
