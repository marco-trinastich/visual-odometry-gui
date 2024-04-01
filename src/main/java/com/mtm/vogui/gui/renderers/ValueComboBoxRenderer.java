package com.mtm.vogui.gui.renderers;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ValueComboBoxRenderer<T> implements ListCellRenderer<T> {
    // Core
    private final JComboBox<T> parentComboBox;
    private final PrefixedListCellRenderer<T> listCellRenderer;

    // Forced repaint listener (needed for prefix suppression on list closed)
    @Setter
    private boolean editable;
    private AWTEventListener listener;
    private boolean listenerRegistered;

    public ValueComboBoxRenderer(@NotNull JComboBox<T> parentComboBox) {
        this.parentComboBox = parentComboBox;
        this.listCellRenderer = PrefixedListCellRenderer.from(parentComboBox, true, false);

        this.editable = false;
        this.listener = null;
        this.listenerRegistered = false;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list,
                                                  @NotNull T item,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        this.registerClickOutsideListener(list);
        return this.listCellRenderer.getListCellRendererComponent(list, item, index, isSelected, cellHasFocus);
    }

    public void setHorizontalAlignment(int alignment) {
        this.listCellRenderer.setHorizontalAlignment(alignment);
    }

    public void setPrefixEnabled(boolean prefixEnabled) {
        this.listCellRenderer.prefixEnabled(prefixEnabled);
    }

    private void registerClickOutsideListener(JList<? extends T> list) {
        if (this.assignListenerIfNeeded(list)) {
            if (this.registerListener()) {
                Toolkit.getDefaultToolkit().addAWTEventListener(this.listener, AWTEvent.MOUSE_EVENT_MASK);
                this.listenerRegistered = true;
            } else if (this.unregisterListener()) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(this.listener);
                this.listenerRegistered = false;
            }
        }
    }

    private boolean registerListener() {
        return this.listCellRenderer.prefixEnabled() && !this.editable && !this.listenerRegistered;
    }

    private boolean unregisterListener() {
        return (!this.listCellRenderer.prefixEnabled() || this.editable)
                && this.listenerRegistered;
    }

    private boolean assignListenerIfNeeded(JList<? extends T> list) {
        if (this.listener == null &&
                SwingUtilities.getWindowAncestor(this.parentComboBox) instanceof JFrame rootFrame) {
            this.listener = this.getClickOutsideListener(rootFrame, this.parentComboBox, list);
        }

        return this.listener != null;
    }

    private @NotNull AWTEventListener getClickOutsideListener(JFrame rootFrame,
                                                              JComboBox<T> parentComboBox,
                                                              JList<? extends T> listRenderer) {
        return event -> {
            MouseEvent mouseEvent = (MouseEvent) event;
            Component component = mouseEvent.getComponent();

            // Ignoring mouse events from any other frame
            if (SwingUtilities.getWindowAncestor(component) == rootFrame &&
                    event.getID() == MouseEvent.MOUSE_PRESSED) {
                if (!component.equals(parentComboBox) && !component.equals(listRenderer)) {
                    parentComboBox.repaint();
                }
                //Log.info("Mouse pressed on " + component.getClass().getCanonicalName());
            }
        };
    }
}
