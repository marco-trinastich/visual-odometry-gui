/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.combobox;

import lombok.Builder;
import lombok.Data;

import java.util.function.Function;

@Data
@Builder
public class ComboBoxSelection<T> {
    private ComboBoxItem<T> current;
    private ComboBoxItem<T> previous;

    /**
     * Convert ComboBox selection from source type T item indexes to target type K via generator
     *
     * @param generator function able to generate a K item from T item index
     * @param <K>       target type to convert into
     * @return ComboBoxSelection<K>
     */
    public <K> ComboBoxSelection<K> convertSelectionFromIndex(Function<Integer, K> generator) {
        ComboBoxItem<K> current = null;
        if (this.current != null && this.current.index() != null) {
            current = ComboBoxItem.<K>builder()
                    .value(generator.apply(this.current.index()))
                    .index(this.current.index())
                    .build();
        }

        ComboBoxItem<K> previous = null;
        if (this.previous != null && this.previous.index() != null) {
            previous = ComboBoxItem.<K>builder()
                    .value(generator.apply(this.previous.index()))
                    .index(this.previous.index())
                    .build();
        }

        return ComboBoxSelection.<K>builder()
                .current(current)
                .previous(previous)
                .build();
    }

    /**
     * Convert ComboBox selection from source type T to target type K via generator
     *
     * @param generator function able to generate a K item from a T item
     * @param <K>       target type to convert into
     * @return ComboBoxSelection<K>
     */
    public <K> ComboBoxSelection<K> convertSelection(Function<T, K> generator) {
        ComboBoxItem<K> current = null;
        if (this.current != null && this.current.index() != null) {
            current = ComboBoxItem.<K>builder()
                    .value(generator.apply(this.current.value()))
                    .index(this.current.index())
                    .build();
        }

        ComboBoxItem<K> previous = null;
        if (this.previous != null && this.previous.index() != null) {
            previous = ComboBoxItem.<K>builder()
                    .value(generator.apply(this.previous.value()))
                    .index(this.previous.index())
                    .build();
        }

        return ComboBoxSelection.<K>builder()
                .current(current)
                .previous(previous)
                .build();
    }
}
