package com.mtm.vogui.gui.editors;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CenteredComboBoxEditor implements ComboBoxEditor {
    private final JTextField editor;

    public CenteredComboBoxEditor() {
        this.editor = new JTextField();
        this.editor.setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getEditorComponent() {
        return this.editor;
    }

    @Override
    public void setItem(@NotNull Object anObject) {
        this.editor.setText(anObject.toString());
    }

    @Override
    public Object getItem() {
        return this.editor.getText();
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
