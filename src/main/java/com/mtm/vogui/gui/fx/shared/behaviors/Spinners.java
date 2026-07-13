/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.behaviors;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

/**
 * App-blind behaviors for JavaFX {@link Spinner}s (see {@code gui.fx.shared} package rules — no
 * settings/context/sink here, only the widget).
 * <p>
 * {@link #commitOnFocusLost(Spinner)} closes the standard editable-Spinner gap: typed text is only
 * parsed into the value on {@code Enter}, so clicking or tabbing away silently discards it. Wiring
 * this makes the edit commit on focus-out — which, for a value bound to the domain, means the change
 * applies as soon as the field loses focus (no separate "Apply" button needed).
 */
public final class Spinners {

    private Spinners() {
    }

    /** Commits the editor's typed text to the spinner value whenever the spinner loses focus. */
    public static void commitOnFocusLost(Spinner<?> spinner) {
        spinner.focusedProperty().addListener((_, _, focused) -> {
            if (!focused) {
                commitEditorText(spinner);
            }
        });
    }

    private static <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) {
            return;
        }
        SpinnerValueFactory<T> factory = spinner.getValueFactory();
        if (factory == null) {
            return;
        }
        StringConverter<T> converter = factory.getConverter();
        if (converter == null) {
            return;
        }
        String text = spinner.getEditor().getText();
        try {
            factory.setValue(converter.fromString(text));
        } catch (RuntimeException _) {
            // Unparseable input: revert the editor to the last committed value rather than commit junk.
            spinner.getEditor().setText(converter.toString(factory.getValue()));
        }
    }
}
