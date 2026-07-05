/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum TrackerType implements WithValue, Comparable {
    Klt("KLT (Standard)"),
    Klt2("KLT (Modern)"),
    Surf("Surf (Standard)"),
    Surf2("Surf (Dda Two Pass)"),
    Default("Default (KLT, standard parameters)");

    private final String value;

    TrackerType(String value) {
        this.value = value;
    }

    public boolean isKlt() {
        return is(Klt) || is(Klt2) || is(Default);
    }

    public boolean isSurf() {
        return is(Surf) || is(Surf2);
    }
}
