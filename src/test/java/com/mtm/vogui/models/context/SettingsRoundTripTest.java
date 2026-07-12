/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context;

import com.mtm.vogui.models.context.config.Config;
import com.mtm.vogui.models.context.settings.Settings;
import com.mtm.vogui.models.enums.gui.UiToolkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Save/load round-trip of {@link AppContext} settings on disk (json and yaml formats),
 * without booting the Quarkus application.
 */
class SettingsRoundTripTest {

    private static final String TEST_FILE_NAME = "target/settings-roundtrip-test";
    private static final String[] TEST_VIDEO_PATHS = new String[]{"assets/example_a.mjpeg", "assets/example_b.mp4"};

    private final Config config = new Config() {
        @Override
        public UiToolkit ui() {
            return UiToolkit.JavaFx;
        }

        @Override
        public Config.Settings settings() {
            return () -> TEST_FILE_NAME;
        }
    };

    @BeforeEach
    void deleteTestFiles() throws IOException {
        AppContext context = newContext();
        Files.deleteIfExists(context.jsonPath());
        Files.deleteIfExists(context.yamlPath());
    }

    @Test
    void jsonRoundTrip() {
        AppContext saved = newContext();
        customize(saved);
        assertTrue(saved.saveToJson());

        AppContext loaded = newContext();
        assertTrue(loaded.loadFromJson());
        assertCustomized(loaded);
    }

    @Test
    void yamlRoundTrip() {
        AppContext saved = newContext();
        customize(saved);
        assertTrue(saved.saveToYaml());

        AppContext loaded = newContext();
        assertTrue(loaded.loadFromYaml());
        assertCustomized(loaded);
    }

    private void customize(AppContext context) {
        context.settings().input().video().paths(TEST_VIDEO_PATHS);
        context.settings().input().video().path(TEST_VIDEO_PATHS[0]);
        context.settings().tracker().klt().maxFeatures(123);
        context.settings().chart().scaleXZ(42.0);
    }

    private void assertCustomized(AppContext context) {
        assertArrayEquals(TEST_VIDEO_PATHS, context.settings().input().video().paths());
        assertEquals(TEST_VIDEO_PATHS[0], context.settings().input().video().path());
        assertEquals(123, context.settings().tracker().klt().maxFeatures());
        assertEquals(42.0, context.settings().chart().scaleXZ());
    }

    private AppContext newContext() {
        return new AppContext(new Settings(), null, config);
    }
}
