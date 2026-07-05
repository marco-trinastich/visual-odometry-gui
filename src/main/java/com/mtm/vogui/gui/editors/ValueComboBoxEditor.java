/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.editors;

import com.mtm.vogui.models.interfaces.WithValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Function;

public class ValueComboBoxEditor<T extends WithValue> implements ComboBoxEditor {
    private final JTextField editor;
    private final Function<String, T> generator;

    private T lastItem;

    public ValueComboBoxEditor(Function<String, T> generator) {
        this.editor = new JTextField();
        this.generator = generator;
    }

    @Override
    public Component getEditorComponent() {
        return this.editor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setItem(Object object) {
        T item = (T) object;
        this.lastItem = item;
        this.editor.setText(item != null ? item.value() : "");
    }

    @Override
    public Object getItem() {
        String text = this.editor.getText();
        if (this.lastItem != null && this.lastItem.value().equals(text)) {
            return this.lastItem;
        } else if (this.generator != null) {
            return this.generator.apply(text);
        }
        return null;
    }

    @Override
    public void selectAll() {
        this.editor.selectAll();
    }

    @Override
    public void addActionListener(ActionListener l) {
        this.editor.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        this.editor.removeActionListener(l);
    }
}
