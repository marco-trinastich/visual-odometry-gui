package com.mtm.vogui.gui.listeners.common;

import com.mtm.vogui.gui.components.common.combobox.ComboBoxItem;
import com.mtm.vogui.gui.components.common.combobox.ComboBoxSelection;
import com.mtm.vogui.utilities.GuiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

public final class ValueComboBoxListener<T> implements ItemListener {

    private final Consumer<ComboBoxItem<T>> setter;
    private final Consumer<ComboBoxSelection<T>> postSelectionAction;
    private ComboBoxItem<T> prevItem;

    public ValueComboBoxListener(Consumer<ComboBoxItem<T>> setter,
                                 Consumer<ComboBoxSelection<T>> postSelectionAction) {
        this.setter = setter;
        this.postSelectionAction = postSelectionAction;
    }

    @Override
    public void itemStateChanged(@NotNull ItemEvent e) {
        // ComboBox selection event
        var currentItem = this.getCurrentItem(e);
        if (currentItem == null) {
            return;
        }

        switch (e.getStateChange()) {
            case ItemEvent.DESELECTED -> this.prevItem = currentItem;
            case ItemEvent.SELECTED -> {
                var selection = ComboBoxSelection.<T>builder()
                        .current(currentItem)
                        .previous(this.prevItem)
                        .build();

                // Emit selected value
                this.setter.accept(currentItem);
                if (this.postSelectionAction != null) {
                    // Call post selection action
                    this.postSelectionAction.accept(selection);
                }
                this.prevItem = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private @Nullable ComboBoxItem<T> getCurrentItem(@NotNull ItemEvent e) {
        JComboBox<T> comboBox = (JComboBox<T>) e.getSource();
        T item = (T) e.getItem();

        if (comboBox == null || item == null) {
            return null;
        }

        int itemIndex = e.getStateChange() == ItemEvent.SELECTED ?
                comboBox.getSelectedIndex() :
                GuiUtils.getComboBoxItemIndex(comboBox.getModel(), item);

        return ComboBoxItem.<T>builder()
                .value(item)
                .index(itemIndex)
                .build();
    }
}