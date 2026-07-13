/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TrackerType implements WithValue, Comparable {
    Klt("KLT (Standard)", true),
    Klt2("KLT (Modern)", true),
    Surf("Surf (Standard)", true),
    Surf2("Surf (Dda Two Pass)", false),
    Default("Default (KLT, standard parameters)", true);

    private final String value;
    private final boolean enabled;

    TrackerType(String value, boolean enabled) {
        this.value = value;
        this.enabled = enabled;
    }

    /**
     * The types actually wired to a tracker algorithm in {@code CoreSetup.setupTracker}, for the UIs
     * to offer (both the JavaFX and Swing tracker combos populate from here, so the not-implemented
     * types stay hidden in one place).
     */
    public static TrackerType[] enabledValues() {
        return Arrays.stream(values())
                .filter(TrackerType::enabled)
                .toArray(TrackerType[]::new);
    }

    public boolean isKlt() {
        return is(Klt) || is(Klt2) || is(Default);
    }

    public boolean isSurf() {
        return is(Surf) || is(Surf2);
    }
}
