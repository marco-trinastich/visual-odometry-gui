/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class DevicePath implements WithValue {
    private String name;
    private String id;

    @Override
    public String value() {
        return this.name;
    }

    public static @NotNull DevicePath from(String id) {
        return DevicePath.from(id, id);
    }

    public static @NotNull DevicePath from(String name, String id) {
        return DevicePath.builder()
                .name(name)
                .id(id)
                .build();
    }
}
