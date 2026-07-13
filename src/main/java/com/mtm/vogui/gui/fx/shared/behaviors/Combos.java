/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.behaviors;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

/**
 * App-blind behaviors for JavaFX {@link ComboBox}es (see {@code gui.fx.shared} package rules — no
 * settings/context/sink here, only the widget). Twin of {@link Spinners} for the editable-combo case.
 * <p>
 * {@link #commitOnFocusLost(ComboBox)} closes the same gap an editable {@code ComboBox} has: text
 * typed into the editor is only parsed into the value on {@code Enter}, so clicking or tabbing away
 * silently discards it. Wiring this commits the edit on focus-out, so a value bound to the domain
 * applies as soon as the field is left.
 */
public final class Combos {

    private Combos() {
    }

    /** Commits the editor's typed text to the combo value whenever the combo loses focus. */
    public static void commitOnFocusLost(ComboBox<?> combo) {
        combo.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) {
                commitEditorText(combo);
            }
        });
    }

    /** Commits the editor's typed text to the combo value now — for reading the value without a
     *  focus change first (e.g. a Save reached straight from a menu, which doesn't fire focus-out). */
    public static void commit(ComboBox<?> combo) {
        commitEditorText(combo);
    }

    private static <T> void commitEditorText(ComboBox<T> combo) {
        if (!combo.isEditable()) {
            return;
        }
        StringConverter<T> converter = combo.getConverter();
        if (converter == null) {
            return;
        }
        String text = combo.getEditor().getText();
        try {
            combo.setValue(converter.fromString(text));
        } catch (RuntimeException _) {
            // Unparseable input: revert the editor to the last committed value rather than commit junk.
            combo.getEditor().setText(converter.toString(combo.getValue()));
        }
    }
}
