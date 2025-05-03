/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.core;

import lombok.Getter;

@Getter
public enum DataSize {
    Bytes("b", 1L),
    Kilobytes("kb", 1024L),
    Megabytes("mb", 1024L * 1024L),
    Gigabytes("gb", 1024L * 1024L * 1024L),
    Terabytes("tb", 1024L * 1024L * 1024L * 1024L);

    private final String unit;
    private final long size;

    DataSize(String unit, long size) {
        this.unit = unit;
        this.size = size;
    }

    public static DataSize get(int divisions) {
        switch (divisions) {
            case 1 -> {
                return Kilobytes;
            }
            case 2 -> {
                return Megabytes;
            }
            case 3 -> {
                return Gigabytes;
            }
            case 4 -> {
                return Terabytes;
            }
            default -> {
                return Bytes;
            }
        }
    }
}
