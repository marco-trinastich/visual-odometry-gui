/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.image;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueComboBox;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueEditableComboBox;
import com.mtm.vogui.gui.swing.shared.components.textfield.IntegerTextField;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.image.ImageSettings;
import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.enums.settings.resolution.ResizeResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Image settings feature (humble view): owns the Image section of the control panel.
 * Widgets commit live into the settings; {@link #load()} refreshes them back from the
 * settings after a load/reset.
 */
public class ImageSettingsView {

    private final AppContext context;

    private final DisplayValueComboBox<ImageTypeDescriptor> txtImageType;
    private final JCheckBox chkImageResize;
    private final DisplayValueEditableComboBox<Resolution> txtImageResize;
    private final JCheckBox chkInternalImagePreview;
    private final JCheckBox chkFrameSkipEnabled;
    private final IntegerTextField txtFrameSkipValue;

    private final JPanel panel;

    public ImageSettingsView(@NotNull AppContext context) {
        this.context = context;
        ImageSettings imageSettings = context.settings().image();
        ImageSettings defaultImageSettings = imageSettings.getDefault();

        /* Image type */
        final JLabel lblImageType = new JLabel("<html><b>Image Type:</b></html>");

        this.txtImageType = new DisplayValueComboBox<>(
                ImageTypeDescriptor.values(),
                imageSettings::descriptor,
                index -> ImageTypeDescriptor.values()[index]
        );
        this.txtImageType.setSelectedItem(imageSettings.descriptor());

        /* Image resize + internal preview */
        this.chkImageResize = new JCheckBox();
        this.chkImageResize.setSelected(imageSettings.resize());
        SwingUtils.setBoldToggleText(this.chkImageResize, "Resize");
        this.chkImageResize.addActionListener(_ -> this.onResizeToggled());

        this.txtImageResize = new DisplayValueEditableComboBox<>(
                ResizeResolution.values(),
                imageSettings::resolution,
                CustomResolution::from);
        this.txtImageResize.setEnabled(imageSettings.resize());
        this.txtImageResize.setHorizontalAlignment(SwingConstants.CENTER);
        this.txtImageResize.setPreferredSize(new Dimension(100, this.txtImageResize.getPreferredSize().height));
        this.txtImageResize.setSelectedItem(imageSettings.resolution());

        this.chkInternalImagePreview = new JCheckBox();
        this.chkInternalImagePreview.setSelected(imageSettings.internalImagePreview());
        SwingUtils.setBoldToggleText(this.chkInternalImagePreview, "Preview Internal Image (Slower)");
        this.chkInternalImagePreview.addActionListener(_ -> this.onInternalPreviewToggled());

        /* Frame skip */
        this.chkFrameSkipEnabled = new JCheckBox();
        this.chkFrameSkipEnabled.setSelected(imageSettings.frameSkipEnabled());
        SwingUtils.setBoldToggleText(this.chkFrameSkipEnabled, "Frame skip");
        this.chkFrameSkipEnabled.addActionListener(_ -> this.onFrameSkipToggled());

        this.txtFrameSkipValue = new IntegerTextField(
                NumberConstraints.StrictlyPositive,
                imageSettings::frameSkipValue,
                imageSettings::frameSkipValue,
                defaultImageSettings.frameSkipValue(),
                3,
                JTextField.CENTER
        );

        /* Panel creation */
        this.panel = new JPanel();
        this.panel.setOpaque(false);

        //Sets a compound border (TitledBorder+EmptyBorder)
        this.panel.setBorder(SwingUtils
                .getSettingsSectionBorder("Image", GuiConstants.PANEL_BORDER_ACTIVE_COLOR));

        this.panel.add(lblImageType);
        this.panel.add(this.txtImageType);
        this.panel.add(this.chkImageResize);
        this.panel.add(this.txtImageResize);
        this.panel.add(this.chkInternalImagePreview);
        this.panel.add(this.chkFrameSkipEnabled);
        this.panel.add(this.txtFrameSkipValue);

        /* Panel disposition */
        SpringLayout panelLayout = new SpringLayout();

        //On the first row Image Type components
        panelLayout.putConstraint(SpringLayout.NORTH, lblImageType, 0, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblImageType, 3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtImageType, -3, SpringLayout.NORTH, lblImageType);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtImageType, 12, SpringLayout.EAST, lblImageType);
        panelLayout.putConstraint(SpringLayout.EAST, this.txtImageType, -3, SpringLayout.EAST, this.panel);

        // Image Keep Original, Resize Width/Height
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkImageResize, 10, SpringLayout.SOUTH, lblImageType);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkImageResize, -3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtImageResize, 1, SpringLayout.NORTH, this.chkImageResize);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtImageResize, 4, SpringLayout.WEST, this.txtImageType);

        // Internal Image Preview CheckBox
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkInternalImagePreview, 0, SpringLayout.NORTH, this.chkImageResize);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkInternalImagePreview, 5, SpringLayout.EAST, this.txtImageResize);

        // Frame skip CheckBox and TextField
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkFrameSkipEnabled, 6, SpringLayout.SOUTH, this.chkInternalImagePreview);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkFrameSkipEnabled, 0, SpringLayout.WEST, this.chkImageResize);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtFrameSkipValue, 1, SpringLayout.NORTH, this.chkFrameSkipEnabled);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtFrameSkipValue, 4, SpringLayout.WEST, this.txtImageType);

        //Panel height is defined by constraining its bottom to the bottom of its last row
        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.chkFrameSkipEnabled);

        this.panel.setLayout(panelLayout);
    }

    /**
     * Section panel, consumed by the control panel composition only.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Refreshes every widget from the current settings (after a load/reset).
     */
    public void load() {
        var imageSettings = this.context.settings().image();

        this.txtImageType.setSelectedItem(imageSettings.descriptor());

        this.chkImageResize.setSelected(imageSettings.resize());
        this.onResizeToggled();
        this.txtImageResize.setSelectedItem(imageSettings.resolution());

        this.chkInternalImagePreview.setSelected(imageSettings.internalImagePreview());
        this.onInternalPreviewToggled();

        this.chkFrameSkipEnabled.setSelected(imageSettings.frameSkipEnabled());
        this.onFrameSkipToggled();
        this.txtFrameSkipValue.updateModel(imageSettings.frameSkipValue());
    }

    // Toggle handlers (commit + bold highlight + dependent enablement)

    private void onResizeToggled() {
        this.context.settings().image().resize(this.chkImageResize.isSelected());
        SwingUtils.setBoldToggleText(this.chkImageResize, "Resize");
        this.txtImageResize.setEnabled(this.chkImageResize.isSelected());
    }

    private void onInternalPreviewToggled() {
        this.context.settings().image().internalImagePreview(this.chkInternalImagePreview.isSelected());
        SwingUtils.setBoldToggleText(this.chkInternalImagePreview, "Preview Internal Image (Slower)");
    }

    private void onFrameSkipToggled() {
        this.context.settings().image().frameSkipEnabled(this.chkFrameSkipEnabled.isSelected());
        SwingUtils.setBoldToggleText(this.chkFrameSkipEnabled, "Frame skip");
    }
}
