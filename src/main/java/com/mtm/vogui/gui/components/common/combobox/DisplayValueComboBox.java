/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.components.common.combobox;

import com.mtm.vogui.gui.listeners.common.ValueComboBoxListener;
import com.mtm.vogui.gui.renderers.ValueComboBoxRenderer;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.interfaces.AdjustableComboBox;
import com.mtm.vogui.models.interfaces.WithValue;
import com.mtm.vogui.utilities.CommonUtils;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class DisplayValueComboBox<T extends Enum<T> & WithValue> extends AdjustableComboBox<String> {
    private final Function<Integer, T> generator;

    /**
     * ComboBox implementation based on a displayable {@code WithValue} enum type
     *
     * @param model     enum values
     * @param setter    a function able to set selected item into settings
     * @param generator a function able to generate a T item from selected index
     */
    public DisplayValueComboBox(T[] model,
                                Consumer<T> setter,
                                Function<Integer, T> generator) {
        this(model, setter, generator, null);
    }

    /**
     * ComboBox implementation based on a displayable {@code WithValue} enum type
     *
     * @param model               enum values
     * @param setter              a function able to set selected item into settings
     * @param generator           a function able to generate a T item from selected index
     * @param postSelectionAction an optional function to run after item selection
     */
    public DisplayValueComboBox(T[] model,
                                Consumer<T> setter,
                                Function<Integer, T> generator,
                                Consumer<ComboBoxSelection<T>> postSelectionAction) {
        super(CommonUtils.getEnumValues(model));

        // Safe-boxed functions
        var safeGenerator = CommonUtils.getSafeGenerator(generator);
        var safeSetter = CommonUtils.getSafeConsumer(setter);
        var safePostSelectionAction = CommonUtils.getSafeConsumer(postSelectionAction);

        this.generator = safeGenerator;
        this.setRenderer(new ValueComboBoxRenderer<>(this));
        this.setListener(safeSetter, safePostSelectionAction);
    }

    public void setSelectedItem(@NotNull T item) {
        super.setSelectedItem(item.value());
    }

    @SuppressWarnings("unused")
    public T getSelectedItemT() {
        return this.generator.apply(this.getSelectedIndex());
    }

    @Override
    public void setEditable(boolean editable) {
        Log.error(Messages.DISPLAY_VALUE_NOT_EDITABLE_EXCEPTION);
    }

    private void setListener(Consumer<T> setter, Consumer<ComboBoxSelection<T>> postSelectionAction) {
        ValueComboBoxListener<String> listener = new ValueComboBoxListener<>(
                item -> setter.accept(this.generator.apply(item.index())),
                this.getStringAction(postSelectionAction, this.generator)
        );
        this.addItemListener(listener);
    }

    /**
     * Get a String ComboBox selection action
     *
     * @param typedAction an action to be performed on a T ComboBox selection
     * @param generator   a function able to generate a T item from an index
     * @return an action to be performed on a String ComboBox selection
     */
    private Consumer<ComboBoxSelection<String>> getStringAction(Consumer<ComboBoxSelection<T>> typedAction,
                                                                Function<Integer, T> generator) {
        Consumer<ComboBoxSelection<String>> action = null;
        if (typedAction != null) {
            action = selection -> typedAction.accept(selection.convertSelectionFromIndex(generator));
        }
        return action;
    }
}
