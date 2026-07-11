/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.rendering;

import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.enums.settings.resolution.DeviceResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;

/**
 * Reflects runtime-negotiated values back into settings, then notifies the {@link RenderSink}
 * so the GUI selectors follow. The settings mutation stays core-side; only the widget refresh
 * crosses the sink.
 */
public class SettingsSync {

    /**
     * Backfills the persisted resolution target with the resolution the device actually
     * granted, whenever capture-time adjustment changed the requested one
     */
    public static void reflectGrantedResolution(@NotNull AppContext context, @NotNull RenderSink sink,
                                                Dimension actual) {
        if (actual == null) {
            return;
        }
        var device = context.settings().input().device();
        if (device.targetWidth() == actual.width && device.targetHeight() == actual.height) {
            return;
        }

        DeviceResolution standard = DeviceResolution.findByResolution(actual.width, actual.height);
        Resolution resolution = standard != null ? standard : CustomResolution.from(actual.width, actual.height);
        device.resolution(resolution);

        sink.deviceResolutionChanged(resolution);
    }

    /**
     * Reflects into settings/GUI the device that was actually opened, when the capture
     * fell back to a different one than requested (same contains-matching semantics as
     * discovery). No-op when the requested device was honored.
     */
    public static void reflectOpenedDevice(@NotNull AppContext context, @NotNull RenderSink sink,
                                           String actualName) {
        if (actualName == null || actualName.isBlank()) {
            return;
        }
        var device = context.settings().input().device();
        String requested = device.path().id().trim();
        if (!requested.isEmpty() && actualName.contains(requested)) {
            return;
        }

        var descriptor = CommonUtils.getDevicePathDescriptor(actualName);
        device.path(descriptor);

        sink.devicePathChanged(descriptor);
    }

    /**
     * Records a successfully used path in its most-recently-used history and refreshes
     * the corresponding selector. Typed paths enter the history only through here, so
     * only values that actually opened are ever recorded.
     */
    public static void commitRecentPath(@NotNull RenderSink sink, @NotNull RecentPathTarget target,
                                        @NotNull PathSettings pathSettings, String usedPath) {
        if (usedPath == null || usedPath.isBlank()) {
            return;
        }
        pathSettings.pushRecentPath(usedPath);

        sink.recentPathUsed(target, pathSettings, usedPath);
    }
}
