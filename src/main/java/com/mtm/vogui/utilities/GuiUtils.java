/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.utilities;

import com.mtm.vogui.gui.swing.components.chart.ChartSettings;
import com.mtm.vogui.gui.swing.components.common.border.RoundedCornerBorder;
import com.mtm.vogui.models.constants.GuiConstants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Enumeration;
import java.util.function.Function;

public class GuiUtils {

    // Gui utils

    public static @NotNull Spring getHalfWidthSpring(JComponent component) {
        // Spring fixed to half width of component
        return new Spring() {
            @Override
            public int getMaximumValue() {
                return Math.round(component.getWidth() / 2f);
            }

            @Override
            public int getMinimumValue() {
                return Math.round(component.getWidth() / 2f);
            }

            @Override
            public int getPreferredValue() {
                return Math.round(component.getWidth() / 2f);
            }

            @Override
            public int getValue() {
                return Math.round(component.getWidth() / 2f);
            }

            @Override
            public void setValue(int value) {
            }
        };
    }

    public static @NotNull Border getRoundedBorder(Color borderColor, int margin, int padding) {
        return getRoundedTitledBorder(null, borderColor, margin, padding);
    }

    public static @NotNull Border getRoundedTitledBorder(String title, Color borderColor) {
        return getRoundedTitledBorder(title, borderColor, 15, 0);
    }

    public static @NotNull Border getRoundedTitledBorder(String title, Color borderColor, int margin, int padding) {
        // Compound border: empty + rounded titled
        var roundedBorder = new RoundedCornerBorder(borderColor, title);
        var innerBorder = padding > 0 ?
                BorderFactory.createCompoundBorder(roundedBorder,
                        BorderFactory.createEmptyBorder(padding, padding, padding, padding)) :
                roundedBorder;

        return margin > 0 ?
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(margin, margin, margin, margin), innerBorder) :
                innerBorder;
    }

    public static <T> int getComboBoxItemIndex(@NotNull ComboBoxModel<T> model, T item) {
        for (int i = 0; i < model.getSize(); i++) {
            if (item.equals(model.getElementAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static @NotNull Shape getRingShape(double centerX, double centerY, double outerRadius, double thickness) {
        Ellipse2D outer = new Ellipse2D.Double(
                centerX - outerRadius,
                centerY - outerRadius,
                outerRadius + outerRadius,
                outerRadius + outerRadius);
        Ellipse2D inner = new Ellipse2D.Double(
                centerX - outerRadius + thickness,
                centerY - outerRadius + thickness,
                outerRadius + outerRadius - thickness - thickness,
                outerRadius + outerRadius - thickness - thickness);
        Area area = new Area(outer);
        area.subtract(new Area(inner));
        return area;
    }

    public static int getStringDisplaySize(@NotNull String string) {
        // Get string display size
        return (string.length() - 1) * 8;
    }

    public static <T> void setTextIfNeeded(@NotNull JTextField textField, T value) {
        var valueStr = String.valueOf(value);
        if (!textField.getText().equals(valueStr)) {
            textField.setText(valueStr);
        }
    }

    public static void setFont(JComponent component, int size) {
        setFont(component, size, null);
    }

    public static void setFont(@NotNull JComponent component, Integer size, Integer style) {
        Font prevFont = component.getFont();
        component.setFont(new Font(
                prevFont.getFontName(),
                style != null ? style : prevFont.getStyle(),
                size != null ? size : prevFont.getSize())
        );
    }


    // Colors

    /**
     * 10-colors wheel (starting from base color)
     * </p>
     * Note: this generator doesn't work using black/white as base color, because saturation/brightness
     * aren't adjusted
     *
     * @param baseColor base color
     * @param steps     color wheel steps
     * @return color picked from 10-colors wheel
     */
    public static @NotNull Color generateColor(@NotNull Color baseColor, int steps) {
        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        float newHue = ((100 * hsb[0] + 10 * steps) % 100) / 100;
        return Color.getHSBColor(newHue, hsb[1], hsb[2]);
    }

    public static Color getAxisNamesColor(@NotNull ChartSettings settings) {
        return settings.axisNamesColor() != null ?
                settings.axisNamesColor() :
                (settings.axisColor() != null ?
                        settings.axisColor() :
                        Color.black
                );
    }

    public static Color getPlotColor(@NotNull ChartSettings settings) {
        return settings.plotColor() != null ? settings.plotColor() : Color.blue;
    }


    // UI properties / System settings

    public static <T> void setUIProperty(T property,
                                         Object newValue,
                                         Function<String, Function<String, Boolean>> checkKeyIsProperty) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object keyObj = keys.nextElement();

            boolean isTarget = false;
            if (property instanceof String propName) {
                // Matched by property name
                if (keyObj instanceof String key &&
                        checkKeyIsProperty != null &&
                        checkKeyIsProperty.apply(key).apply(propName)) {
                    isTarget = true;
                }
            } else {
                // Matched by property type
                Object value = UIManager.get(keyObj);
                if (property.getClass().getSuperclass().isAssignableFrom(value.getClass())) {
                    isTarget = true;
                    newValue = property;
                }
            }

            if (isTarget) {
                UIManager.put(keyObj, newValue);
            }
        }
    }

    public static void setUIProperty(String property, Object value) {
        UIManager.put(property, value);
    }

    public static void setUIPropertyEndsWith(String property, Object newValue) {
        GuiUtils.setUIProperty(property, newValue, key -> key::endsWith);
    }

    public static void setUIPropertyContains(String property, Object newValue) {
        GuiUtils.setUIProperty(property, newValue, key -> key::contains);
    }

    public static void setSystemAntiAliasing() {
        // Enable system anti-aliasing
        System.setProperty(GuiConstants.AWT_AA_FONT, GuiConstants.ON);
        System.setProperty(GuiConstants.SWING_AA_TEXT, GuiConstants.TRUE);
        System.setProperty(GuiConstants.JAVA2D_X_RENDER, GuiConstants.TRUE);
    }
}
