/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.interfaces;

import com.mtm.vogui.gui.renderers.ValueComboBoxRenderer;

import javax.swing.*;

public abstract class AdjustableComboBox<T> extends JComboBox<T> {
    public AdjustableComboBox(T[] model) {
        super(model);
    }

    public void setHorizontalAlignment(int alignment) {
        this.setRendererHorizontalAlignment(alignment);
        this.setEditorHorizontalAlignment(alignment);
    }

    public void setRendererHorizontalAlignment(int alignment) {
        if (this.renderer instanceof ValueComboBoxRenderer<?> displayValueRenderer) {
            displayValueRenderer.setHorizontalAlignment(alignment);
        } else {
            ((JLabel) this.renderer).setHorizontalAlignment(alignment);
        }
    }

    public void setEditorHorizontalAlignment(int alignment) {
        ((JTextField) this.getEditor().getEditorComponent()).setHorizontalAlignment(alignment);
    }

    @Override
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        if (this.getRenderer() instanceof ValueComboBoxRenderer<? super T> valueComboBoxRenderer) {
            valueComboBoxRenderer.editable(editable);
        }
    }

    public void setPrefixEnabled(boolean prefixEnabled) {
        if (this.getRenderer() instanceof ValueComboBoxRenderer<? super T> valueComboBoxRenderer) {
            valueComboBoxRenderer.setPrefixEnabled(prefixEnabled);
        }
    }
}
