/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.renderers;

import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class PrefixedListCellRenderer<T> extends JLabel implements ListCellRenderer<T> {
    private final JComboBox<T> parentComboBox;
    private final ListCellRenderer<Object> defaultListCellRenderer;
    private final boolean labelRenderer;
    private boolean prefixEnabled;
    private boolean centered;

    private boolean itemHovered;
    private boolean itemSelected;

    @SuppressWarnings("unchecked")
    public PrefixedListCellRenderer(@NotNull JComboBox<T> parentComboBox,
                                    boolean prefixEnabled,
                                    boolean centered) {
        super();

        this.parentComboBox = parentComboBox;
        this.defaultListCellRenderer = (ListCellRenderer<Object>) parentComboBox.getRenderer();
        this.labelRenderer = this.defaultListCellRenderer instanceof JLabel;
        this.prefixEnabled = prefixEnabled;
        this.centered = centered;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        this.itemHovered = isSelected;
        this.itemSelected = this.parentComboBox.getSelectedIndex() == index;
        var listCell = this.defaultListCellRenderer
                .getListCellRendererComponent(list, this.getContent(value), index, isSelected, cellHasFocus);

        if (this.labelRenderer) {
            var lblListCell = (JLabel) listCell;
            this.setText(lblListCell.getText());
            this.setFont(lblListCell.getFont());
            this.setPreferredSize(lblListCell.getPreferredSize());
            this.setBackground(this.computeBackground());
            this.setForeground(this.computeForeground());

            return this;
        } else {
            return listCell;
        }
    }

    @Override
    public void setHorizontalAlignment(int alignment) {
        super.setHorizontalAlignment(alignment);
        this.centered = alignment == SwingConstants.CENTER;
    }

    @Override
    public void paint(@NotNull Graphics g) {
        // Draw list item background
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (this.prefixEnabled && this.itemSelected) {
            // Draw prefix
            final int y = getHeight() - 4;
            g.setColor(getForeground());
            g.drawString(GuiConstants.COMBO_BOX_CHECKMARK, 6, y);
        }

        super.paint(g);
    }

    private @NotNull String getContent(@NotNull T item) {
        var itemValue = this.getItemValue(item);
        return this.getSpacedValue(itemValue);
    }

    private String getItemValue(@NotNull T item) {
        return WithValue.class.isAssignableFrom(item.getClass()) ?
                ((WithValue) item).value() :
                (String) item;
    }

    private String getSpacedValue(String itemValue) {
        String spacedValue = itemValue;
        if (this.parentComboBox.isPopupVisible()) {
            spacedValue = this.prefixEnabled ?
                    this.getPrefixedSpacedValue(itemValue) :
                    this.getNotPrefixedSpacedValue(itemValue);
        }
        return spacedValue;
    }

    private String getPrefixedSpacedValue(String itemValue) {
        return this.labelRenderer ?
                this.getDefaultSpacedValue(itemValue) :
                this.getFallbackSpacedValue(itemValue);
    }

    private String getNotPrefixedSpacedValue(String itemValue) {
        return !this.centered ?
                GuiConstants.COMBO_BOX_NO_PREFIX_SPACE + itemValue :
                itemValue;
    }

    private @NotNull String getFallbackSpacedValue(String itemValue) {
        return this.itemSelected ?
                GuiConstants.COMBO_BOX_FALLBACK_PREFIX + itemValue :
                GuiConstants.COMBO_BOX_FALLBACK_SPACE + itemValue;
    }

    private @NotNull String getDefaultSpacedValue(String itemValue) {
        return this.centered ?
                GuiConstants.COMBO_BOX_CENTERED_SPACE + itemValue + GuiConstants.COMBO_BOX_CENTERED_SPACE :
                GuiConstants.COMBO_BOX_DEFAULT_SPACE + itemValue;
    }

    private Color computeBackground() {
        return this.itemHovered ?
                GuiConstants.LIST_SELECTION_BACKGROUND_COLOR :
                GuiConstants.LIST_BACKGROUND_COLOR;
    }

    private Color computeForeground() {
        return this.itemHovered ?
                GuiConstants.LIST_SELECTION_FOREGROUND_COLOR :
                GuiConstants.LIST_FOREGROUND_COLOR;
    }

    public static <T> @NotNull PrefixedListCellRenderer<T> from(JComboBox<T> parentComboBox,
                                                                boolean prefixEnabled,
                                                                boolean centered) {
        return new PrefixedListCellRenderer<>(parentComboBox, prefixEnabled, centered);
    }
}
