/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.utilities;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.github.sarxos.webcam.Webcam;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class WebcamDriverUtils {

    private WebcamDriverUtils() {
    }

    /**
     * Activates the native webcam driver (JNA + AVFoundation/MediaFoundation/V4L2), replacing the
     * default BridJ-based driver, which has no Apple Silicon natives and is broken on macOS >= Catalina.
     * <p/>
     * Must be called before any other sarxos {@code Webcam} API call.
     */
    public static void useNativeDriver() {
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
        try (var library = WebcamDriverUtils.class.getResourceAsStream("/" + arch + "/libvideocapture.dylib")) {
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
            System.err.println("Webcam natives pre-extraction failed (harmless): " + exc.getMessage());
        }
    }
}
