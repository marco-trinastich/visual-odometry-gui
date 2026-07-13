/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.behaviors;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;

/**
 * App-blind behavior coordinating the in-flight-edit commit across the editable input widgets
 * ({@link Spinners}/{@link Combos}) from a single entry point (see {@code gui.fx.shared} package
 * rules — no settings/context/sink here, only widgets).
 * <p>
 * {@link #commitFocused(Scene)} closes the gap left by {@code commitOnFocusLost}: opening a menu or
 * closing the window does NOT fire focus-lost on the currently focused editor, so text typed but not
 * confirmed with {@code Enter} never reaches the domain — a Save/Exit driven straight from the menu
 * (or the window's close button) would then serialize the stale value. Call this on the FX thread,
 * while the scene is still alive, right before persisting.
 */
public final class Editors {

    private Editors() {
    }

    /** Commits any pending edit in the scene's currently focused Spinner/editable ComboBox editor. */
    public static void commitFocused(Scene scene) {
        if (scene == null) {
            return;
        }
        // The focus owner of a typed edit is the widget's internal editor (a TextField), a descendant
        // of the Spinner/ComboBox — walk up to the owning control and commit it.
        for (Node node = scene.getFocusOwner(); node != null; node = node.getParent()) {
            if (node instanceof Spinner<?> spinner) {
                Spinners.commit(spinner);
                return;
            }
            if (node instanceof ComboBox<?> combo) {
                Combos.commit(combo);
                return;
            }
        }
    }
}
