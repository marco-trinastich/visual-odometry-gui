/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.utils;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.border.RoundedCornerBorder;
import com.mtm.vogui.utilities.ImageUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Function;

public class SwingUtils {

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

    public static @NotNull Border getRoundedTitledBorder(String title, Color borderColor) {
        return getRoundedTitledBorder(title, borderColor, 15, 0);
    }

    public static @NotNull Border getRoundedTitledBorder(String title, Color borderColor, int margin, int padding) {
        // Default rounded border insets (from RoundedCornerBorder); settings sections use their own.
        return wrapRoundedBorder(new RoundedCornerBorder(borderColor, title), margin, padding, padding);
    }

    /**
     * Content-box border for a settings section, fully tuned via the {@code SETTINGS_PANEL_BORDER_*}
     * constants (margin + rounded-border inset + padding, with split vertical/horizontal).
     */
    public static @NotNull Border getSettingsSectionBorder(Color borderColor) {
        return getSettingsSectionBorder(null, borderColor);
    }

    public static @NotNull Border getSettingsSectionBorder(String title, Color borderColor) {
        var roundedBorder = new RoundedCornerBorder(borderColor, title,
                GuiConstants.SETTINGS_PANEL_BORDER_INSET_V, GuiConstants.SETTINGS_PANEL_BORDER_INSET_H);
        return wrapRoundedBorder(roundedBorder,
                GuiConstants.SETTINGS_PANEL_BORDER_MARGIN,
                GuiConstants.SETTINGS_PANEL_BORDER_PADDING_V,
                GuiConstants.SETTINGS_PANEL_BORDER_PADDING_H);
    }

    private static @NotNull Border wrapRoundedBorder(RoundedCornerBorder roundedBorder,
                                                     int margin, int vPadding, int hPadding) {
        // Compound border: empty margin + rounded titled + empty (vertical/horizontal) padding
        var innerBorder = (vPadding > 0 || hPadding > 0) ?
                BorderFactory.createCompoundBorder(roundedBorder,
                        BorderFactory.createEmptyBorder(vPadding, hPadding, vPadding, hPadding)) :
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

    // Frame geometry

    public static @NotNull Dimension getDefaultFrameDimension() {
        //Gets current Screen Size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //Depending on current Screen Size, generates opportune Width and Height for the Frames
        int frameHeight, frameWidth;
        frameWidth = (int) screenSize.getWidth() >= 1030 ? 530 : (int) (screenSize.getWidth() / 3f);
        frameHeight = (int) (screenSize.getHeight()) >= 930 ? 930 : (int) screenSize.getHeight();

        return new Dimension(frameWidth, frameHeight);
    }

    public static void resizeAndCenter(@NotNull JFrame frame, int frameSize, boolean screenCenter) {
        frame.setPreferredSize(new Dimension(frameSize, frameSize));
        frame.setSize(new Dimension(frameSize, frameSize));

        int containerWidth;
        int containerHeight;
        if (screenCenter) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            containerWidth = (int) screenSize.getWidth();
            containerHeight = (int) screenSize.getHeight();
        } else {
            containerWidth = (int) getDefaultFrameDimension().getWidth() * 2;
            containerHeight = (int) getDefaultFrameDimension().getHeight();
        }

        if (containerWidth >= frameSize && containerHeight >= frameSize) {
            frame.setLocationRelativeTo(null);
            frame.setLocation(
                    (int) ((containerWidth / 2) - 0.5 * frameSize),
                    (int) ((containerHeight / 2) - 0.5 * frameSize)
            );
        }
    }

    /**
     * Base label font of the CURRENT LookAndFeel, read live from {@code UIManager}: a frozen
     * constant would capture the pre-LookAndFeel value at class-load time (and booting Swing
     * from a static initializer is a side effect nobody expects from a constants class).
     */
    public static Font defaultFont() {
        return UIManager.getFont(GuiConstants.LABEL_FONT_PROP);
    }

    /**
     * Bold variant of {@link #defaultFont()}.
     */
    public static Font boldFont() {
        Font baseFont = defaultFont();
        return new Font(baseFont.getFontName(), Font.BOLD, baseFont.getSize());
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
        SwingUtils.setUIProperty(property, newValue, key -> key::endsWith);
    }

    public static void setUIPropertyContains(String property, Object newValue) {
        SwingUtils.setUIProperty(property, newValue, key -> key::contains);
    }

    /**
     * Re-renders a toggle's label: bold when selected, plain otherwise
     * (the settings panels bold-highlight every active option).
     */
    public static void setBoldToggleText(@NotNull JToggleButton toggle, String label) {
        toggle.setText(String.format(GuiConstants.HTML_TAG,
                toggle.isSelected() ? String.format(GuiConstants.BOLD_TAG, label) : label));
    }

    public static void setSystemAntiAliasing() {
        // Enable system anti-aliasing
        System.setProperty(GuiConstants.AWT_AA_FONT, GuiConstants.ON);
        System.setProperty(GuiConstants.SWING_AA_TEXT, GuiConstants.TRUE);
        System.setProperty(GuiConstants.JAVA2D_X_RENDER, GuiConstants.TRUE);
    }

    // Icon/image resource loading (presentation-side: ImageUtils stays pure imaging)

    public static BufferedImage getResourceImage(String imageRes) {
        return getResourceImage(imageRes, null, null);
    }

    public static BufferedImage getResourceImage(String imageRes, Integer width, Float alpha) {
        return getResourceImage(imageRes, width, null, alpha);
    }

    @SneakyThrows
    public static BufferedImage getResourceImage(String imageRes, Integer width, Integer height, Float alpha) {
        BufferedImage image = null;
        try (InputStream imageStream = SwingUtils.class.getResourceAsStream(imageRes)) {
            if (imageStream != null) {
                // Load image
                image = ImageIO.read(imageStream);

                // Apply alpha
                if (alpha != null && alpha < 1.0) {
                    image = ImageUtils.modifyBufferedImageAlpha(image, alpha);
                }

                // Scale
                if (width != null) {
                    image = ImageUtils.resizeBufferedImage(image, width, height);
                }
            }
        }

        return image;
    }
}
