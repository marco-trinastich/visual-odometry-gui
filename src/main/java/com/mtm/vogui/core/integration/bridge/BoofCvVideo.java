/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.bridge;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.github.sarxos.webcam.Webcam;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Wrapper around the sarxos webcam API (BoofCv capture path).
 * <p/>
 * Loading this class activates the native webcam driver, so every raw sarxos
 * access must go through here.
 */
public final class BoofCvVideo {

    static {
        loadNativeDriver();
    }

    private BoofCvVideo() {
    }

    /**
     * The webcams currently visible to the driver
     */
    public static List<Webcam> webcams() {
        return Webcam.getWebcams();
    }

    /**
     * Activates the native webcam driver (JNA + AVFoundation/MediaFoundation/V4L2), replacing the
     * default BridJ-based driver, which has no Apple Silicon natives and is broken on macOS >= Catalina.
     * <p/>
     * Runs at class load, before any other sarxos {@code Webcam} API call.
     */
    private static void loadNativeDriver() {
        preExtractMacNatives();
        Webcam.setDriver(new NativeDriver());
    }

    /**
     * The driver static-init loads its dylib twice with different JNA options, so JNA extracts two
     * temp copies and dlopens both, making the ObjC runtime warn about duplicate classes. Extracting
     * the dylib once to a stable path lets dlopen dedupe the two loads. Best effort and cosmetic:
     * on any failure the driver still works through JNA classpath extraction, warning included.
     */
    private static void preExtractMacNatives() {
        if (!OSUtils.isMac()) {
            return;
        }

        var arch = System.getProperty("os.arch", "").contains("aarch64") ? "darwin-aarch64" : "darwin-x86-64";
        try (var library = BoofCvVideo.class.getResourceAsStream("/" + arch + "/libvideocapture.dylib")) {
            if (library == null) {
                return;
            }

            Path nativesDir = Path.of(System.getProperty("java.io.tmpdir"), "vogui-natives-" + arch);
            Files.createDirectories(nativesDir);
            Files.copy(library, nativesDir.resolve("libvideocapture.dylib"), StandardCopyOption.REPLACE_EXISTING);

            var libraryPath = System.getProperty("jna.library.path", "");
            System.setProperty("jna.library.path",
                    libraryPath.isEmpty() ? nativesDir.toString() : libraryPath + File.pathSeparator + nativesDir);
        } catch (Exception exc) {
            Log.warnf("Webcam natives pre-extraction failed (harmless): %s", exc.getMessage());
        }
    }
}
