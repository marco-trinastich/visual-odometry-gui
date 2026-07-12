/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.combobox;

import com.mtm.vogui.gui.swing.shared.listeners.ValueComboBoxListener;
import com.mtm.vogui.gui.swing.shared.renderers.ValueComboBoxRenderer;
import com.mtm.vogui.utilities.CommonUtils;

import java.util.function.Consumer;

@SuppressWarnings("serial")
public class StringValueEditableComboBox extends AdjustableComboBox<String> {

    /**
     * ComboBox implementation based on {@code String} arrays
     *
     * @param model  string values
     * @param setter a function able to set selected string into settings
     */
    public StringValueEditableComboBox(String[] model, Consumer<String> setter) {
        this(model, setter, null);
    }

    /**
     * ComboBox implementation based on {@code String} arrays
     *
     * @param model               string values
     * @param setter              a function able to set selected string into settings
     * @param postSelectionAction an optional function to run after item selection
     */
    public StringValueEditableComboBox(String[] model,
                                       Consumer<String> setter,
                                       Consumer<ComboBoxSelection<String>> postSelectionAction) {
        super(model);

        // Safe-boxed functions
        var safeSetter = CommonUtils.getSafeConsumer(setter);
        var safePostSelectionAction = CommonUtils.getSafeConsumer(postSelectionAction);

        this.setRenderer(new ValueComboBoxRenderer<>(this));
        this.setListener(safeSetter, safePostSelectionAction);
        this.setEditable(true);
    }

    private void setListener(Consumer<String> setter, Consumer<ComboBoxSelection<String>> postSelectionAction) {
        ValueComboBoxListener<String> listener = new ValueComboBoxListener<>(
                item -> setter.accept(item.value()),
                postSelectionAction
        );
        this.addItemListener(listener);
    }
}
