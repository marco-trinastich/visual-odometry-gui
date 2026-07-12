/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.input;

import com.mtm.vogui.core.integration.discovery.DeviceDiscovery;
import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.controlpanel.toolbar.ToolbarView;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueComboBox;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueEditableComboBox;
import com.mtm.vogui.gui.swing.shared.components.combobox.StringValueEditableComboBox;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.context.settings.input.InputSettings;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.DeviceType;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.enums.settings.resolution.DeviceResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Input settings feature (humble view): owns the Input section of the control panel
 * (calibration path, video/device source, device type/path/resolution and adjustments).
 * Widgets commit live into the settings; {@link #load()} refreshes them back after a
 * load/reset; the {@code show*} intents reflect values healed by the core at capture time.
 */
public class InputSettingsView {

    private final AppContext context;
    private final ToolbarView toolbar;

    private final StringValueEditableComboBox txtCalibration;
    private final JRadioButton optVideoSource;
    private final StringValueEditableComboBox txtVideoSource;
    private final JButton btnVideoSourceBrowsing;
    private final JRadioButton optDeviceSource;
    private final DisplayValueComboBox<DeviceType> txtDeviceType;
    private final DisplayValueEditableComboBox<DevicePath> txtDevicePath;
    private final JLabel lblDeviceResolution;
    private final DisplayValueEditableComboBox<Resolution> txtDeviceResolution;
    private final JCheckBox chkDeviceSustainFramerate;
    private final JCheckBox chkDeviceTimeoutImageIO;
    private final JCheckBox chkDeviceKeepFormat;
    private final JPanel deviceAdjustmentsPanel;
    private final JCheckBox chkFullResolutionPreview;
    private final JCheckBox chkInputPreviewEnabled;

    private final JPanel panel;

    public InputSettingsView(@NotNull AppContext context, @NotNull ToolbarView toolbar) {
        this.context = context;
        this.toolbar = toolbar;
        InputSettings inputSettings = context.settings().input();

        /* Calibration */
        final JLabel lblCalibration = new JLabel("<html><b>Calibration</b></html>");

        this.txtCalibration = new StringValueEditableComboBox(
                inputSettings.calibration().paths(),
                selected -> inputSettings.calibration().path(selected)
        );
        this.txtCalibration.setPrefixEnabled(false);
        this.txtCalibration.setSelectedItem(inputSettings.calibration().path());
        installRecentPathDeletion(this.txtCalibration, inputSettings.calibration());

        final JButton btnCalibrationBrowsing = new JButton("...");
        btnCalibrationBrowsing.addActionListener(
                new BrowseButtonListener(this.txtCalibration, inputSettings.calibration(),
                        "Open Calibration",
                        new String[]{".yaml", ".yml"},
                        new String[]{"YAML Camera Calibration (*.yaml)", "YAML Camera Calibration (*.yml)",
                                "YAML Camera Calibration (*.yaml, *.yml)"},
                        false));

        /* Source */
        boolean isVideo = inputSettings.source().is(SourceType.Video);
        boolean isDevice = inputSettings.source().is(SourceType.Device);

        final JLabel lblSource = new JLabel("<html><b>Source</b></html>");

        /* Video source */
        this.optVideoSource = new JRadioButton();
        this.optVideoSource.setSelected(isVideo);
        SwingUtils.setBoldToggleText(this.optVideoSource, "Video");
        this.optVideoSource.addActionListener(_ -> this.onSourceChanged(SourceType.Video));

        this.txtVideoSource = new StringValueEditableComboBox(
                inputSettings.video().paths(),
                selected -> inputSettings.video().path(selected)
        );
        this.txtVideoSource.setPrefixEnabled(false);
        this.txtVideoSource.setEnabled(isVideo);
        this.txtVideoSource.setSelectedItem(inputSettings.video().path());
        installRecentPathDeletion(this.txtVideoSource, inputSettings.video());

        this.btnVideoSourceBrowsing = new JButton("...");
        this.btnVideoSourceBrowsing.setEnabled(isVideo);
        this.btnVideoSourceBrowsing.addActionListener(
                new BrowseButtonListener(this.txtVideoSource, inputSettings.video(),
                        "Open Video",
                        new String[]{".avi", ".mp4", ".mjpeg"},
                        new String[]{"AVI Audio/Video Interleave (*.avi)", "MPEG-4/H.264 Video (*.mp4)", "Motion JPEG Video (*.mjpeg)", "All supported media (*.mjpeg, *.mp4, *.avi)"},
                        true));

        /* Device source */
        this.optDeviceSource = new JRadioButton();
        this.optDeviceSource.setSelected(isDevice);
        SwingUtils.setBoldToggleText(this.optDeviceSource, "Device");
        this.optDeviceSource.addActionListener(_ -> this.onSourceChanged(SourceType.Device));

        // Device path ComboBox
        healDevicePath(context);
        this.txtDevicePath = new DisplayValueEditableComboBox<>(
                devicePaths(context),
                path -> context.settings().input().device().path(path),
                DevicePath::from,
                _ -> this.refreshDeviceResolutions());
        this.txtDevicePath.setEnabled(isDevice);
        this.txtDevicePath.setSelectedItem(context.settings().input().device().path());

        // Device type ComboBox
        this.txtDeviceType = new DisplayValueComboBox<>(
                DeviceType.values(),
                value -> inputSettings.device().type(value),
                index -> DeviceType.values()[index],
                selection -> {
                    // Reload device paths if needed
                    if (!selection.previous().value().is(selection.current().value())) {
                        DeviceDiscovery.forType(inputSettings.device().type()).reload();
                        healDevicePath(context);
                        this.txtDevicePath.setModel(new DefaultComboBoxModel<>(devicePaths(context)));
                        // Selecting the item commits it to settings through the combo listener
                        this.txtDevicePath.setSelectedItem(inputSettings.device().path());
                        // The new driver may advertise a different resolution set
                        this.refreshDeviceResolutions();
                    }
                }
        );
        this.txtDeviceType.setEnabled(isDevice);
        this.txtDeviceType.setSelectedItem(inputSettings.device().type().value());

        // Device type/path container
        JPanel txtDeviceContainer = new JPanel(new GridBagLayout());
        txtDeviceContainer.setOpaque(false);
        GridBagConstraints txtDeviceConstraints = new GridBagConstraints();
        txtDeviceConstraints.weightx = 0.5;
        txtDeviceConstraints.fill = GridBagConstraints.HORIZONTAL;
        txtDeviceContainer.add(this.txtDeviceType, txtDeviceConstraints);
        txtDeviceContainer.add(this.txtDevicePath, txtDeviceConstraints);
        this.txtDeviceType.setPreferredSize(new Dimension(1, this.txtDeviceType.getPreferredSize().height));
        this.txtDevicePath.setPreferredSize(new Dimension(1, this.txtDevicePath.getPreferredSize().height));

        /* Device resolution */
        this.lblDeviceResolution = new JLabel("Resolution");
        this.lblDeviceResolution.setEnabled(isDevice);

        this.txtDeviceResolution = new DisplayValueEditableComboBox<>(
                DeviceResolution.values(),
                resolution -> context.settings().input().device().resolution(resolution),
                CustomResolution::from);
        this.txtDeviceResolution.setEnabled(isDevice);
        this.txtDeviceResolution.setHorizontalAlignment(SwingConstants.CENTER);
        this.txtDeviceResolution.setSelectedItem(inputSettings.device().resolution());

        /* Device controls */
        this.chkDeviceSustainFramerate = new JCheckBox();
        this.chkDeviceSustainFramerate.setSelected(inputSettings.device().v4l4j().sustainFramerate());
        SwingUtils.setBoldToggleText(this.chkDeviceSustainFramerate, "Sustain Framerate");
        this.chkDeviceSustainFramerate.setEnabled(isDevice);
        this.chkDeviceSustainFramerate.addActionListener(_ -> this.onSustainFramerateToggled());

        this.chkDeviceTimeoutImageIO = new JCheckBox();
        this.chkDeviceTimeoutImageIO.setSelected(inputSettings.device().v4l4j().timeoutImageIO());
        SwingUtils.setBoldToggleText(this.chkDeviceTimeoutImageIO, "Timeout Image I/O");
        this.chkDeviceTimeoutImageIO.setEnabled(isDevice);
        this.chkDeviceTimeoutImageIO.addActionListener(_ -> this.onTimeoutImageIOToggled());

        this.chkDeviceKeepFormat = new JCheckBox();
        this.chkDeviceKeepFormat.setSelected(inputSettings.device().v4l4j().keepFormat());
        SwingUtils.setBoldToggleText(this.chkDeviceKeepFormat, "Keep Format");
        this.chkDeviceKeepFormat.setEnabled(isDevice);
        this.chkDeviceKeepFormat.addActionListener(_ -> this.onKeepFormatToggled());

        /* Device adjustments panel */
        this.deviceAdjustmentsPanel = new JPanel();
        this.deviceAdjustmentsPanel.setOpaque(false);
        this.deviceAdjustmentsPanel.setBorder(
                SwingUtils.getSettingsSectionBorder("Adjustments",
                        isDevice ?
                                GuiConstants.PANEL_BORDER_ACTIVE_COLOR :
                                GuiConstants.PANEL_BORDER_INACTIVE_COLOR));
        this.deviceAdjustmentsPanel.setEnabled(isDevice);

        this.deviceAdjustmentsPanel.add(this.lblDeviceResolution);
        this.deviceAdjustmentsPanel.add(this.txtDeviceResolution);
        this.deviceAdjustmentsPanel.add(this.chkDeviceSustainFramerate);
        this.deviceAdjustmentsPanel.add(this.chkDeviceTimeoutImageIO);
        this.deviceAdjustmentsPanel.add(this.chkDeviceKeepFormat);

        SpringLayout panelLayout = new SpringLayout();

        //Device Width/Height Label and TextField, and Device Sustain Framerate CheckBox
        //are disposed on the same row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, this.lblDeviceResolution, -1, SpringLayout.NORTH, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, this.lblDeviceResolution, 8, SpringLayout.WEST, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtDeviceResolution, -3, SpringLayout.NORTH, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtDeviceResolution, 8, SpringLayout.EAST, this.lblDeviceResolution);
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkDeviceSustainFramerate, -3, SpringLayout.NORTH, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkDeviceSustainFramerate, 0, SpringLayout.EAST, this.txtDeviceResolution);

        //Device Timeout Image I/O CheckBox and Device Keep Format CheckBox are disposed on the second row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkDeviceTimeoutImageIO, 10, SpringLayout.SOUTH, this.lblDeviceResolution);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkDeviceTimeoutImageIO, 0, SpringLayout.WEST, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkDeviceKeepFormat, 10, SpringLayout.SOUTH, this.lblDeviceResolution);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkDeviceKeepFormat, 0, SpringLayout.WEST, this.chkDeviceSustainFramerate);

        //Device Adjustments Panel height is defined by constraining its bottom to the bottom of the second row
        panelLayout.putConstraint(SpringLayout.SOUTH, this.deviceAdjustmentsPanel, 0, SpringLayout.SOUTH, this.chkDeviceTimeoutImageIO);

        this.deviceAdjustmentsPanel.setLayout(panelLayout);

        /* Bottom part (full resolution preview, input preview) */
        this.chkFullResolutionPreview = new JCheckBox();
        this.chkFullResolutionPreview.setSelected(inputSettings.fullResolutionPreview());
        SwingUtils.setBoldToggleText(this.chkFullResolutionPreview, "Full-Resolution Preview");
        this.chkFullResolutionPreview.addActionListener(_ -> this.onFullResolutionPreviewToggled());

        this.chkInputPreviewEnabled = new JCheckBox();
        this.chkInputPreviewEnabled.setSelected(inputSettings.inputPreview());
        SwingUtils.setBoldToggleText(this.chkInputPreviewEnabled, "Enable Input Preview (Slower)");
        this.chkInputPreviewEnabled.addActionListener(_ -> this.onInputPreviewToggled());

        // Now that every device widget exists, swap the static model for the device-advertised sizes
        this.refreshDeviceResolutions();

        /* Input settings panel creation */
        this.panel = new JPanel();
        this.panel.setOpaque(false);

        //Sets a compound border (TitledBorder+EmptyBorder)
        this.panel.setBorder(SwingUtils
                .getSettingsSectionBorder("Input", GuiConstants.PANEL_BORDER_ACTIVE_COLOR));

        //Adds to the panel all the components (Calibration, Source (Video/Device), Bottom Part)
        this.panel.add(lblCalibration);
        this.panel.add(this.txtCalibration);
        this.panel.add(btnCalibrationBrowsing);
        this.panel.add(lblSource);
        this.panel.add(this.optVideoSource);
        this.panel.add(this.txtVideoSource);
        this.panel.add(this.btnVideoSourceBrowsing);
        this.panel.add(this.optDeviceSource);
        this.panel.add(txtDeviceContainer);
        this.panel.add(this.deviceAdjustmentsPanel);
        this.panel.add(this.chkFullResolutionPreview);
        this.panel.add(this.chkInputPreviewEnabled);

        /* Input settings panel disposition */
        panelLayout = new SpringLayout();

        //Calibration Label, Selection ComboBox and Browsing Button
        //are disposed on the same row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, lblCalibration, 0, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblCalibration, 3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtCalibration, -3, SpringLayout.NORTH, lblCalibration);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtCalibration, 10, SpringLayout.EAST, lblCalibration);
        panelLayout.putConstraint(SpringLayout.EAST, this.txtCalibration, -35, SpringLayout.EAST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, btnCalibrationBrowsing, 0, SpringLayout.NORTH, this.txtCalibration);
        panelLayout.putConstraint(SpringLayout.WEST, btnCalibrationBrowsing, -30, SpringLayout.EAST, btnCalibrationBrowsing);
        panelLayout.putConstraint(SpringLayout.EAST, btnCalibrationBrowsing, -3, SpringLayout.EAST, this.panel);
        panelLayout.putConstraint(SpringLayout.SOUTH, btnCalibrationBrowsing, 0, SpringLayout.SOUTH, this.txtCalibration);

        //Source Label is disposed in the second row, under Calibration Label
        panelLayout.putConstraint(SpringLayout.NORTH, lblSource, 10, SpringLayout.SOUTH, lblCalibration);
        panelLayout.putConstraint(SpringLayout.WEST, lblSource, 3, SpringLayout.WEST, this.panel);

        //Video Source OptionButton, Selection ComboBox and Browsing Button
        //are disposed on the third row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, this.optVideoSource, 5, SpringLayout.SOUTH, lblSource);
        panelLayout.putConstraint(SpringLayout.WEST, this.optVideoSource, -6, SpringLayout.WEST, lblSource);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtVideoSource, 1, SpringLayout.NORTH, this.optVideoSource);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtVideoSource, 4, SpringLayout.WEST, txtDeviceContainer);
        panelLayout.putConstraint(SpringLayout.EAST, this.txtVideoSource, -35, SpringLayout.EAST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.btnVideoSourceBrowsing, 0, SpringLayout.NORTH, this.txtVideoSource);
        panelLayout.putConstraint(SpringLayout.WEST, this.btnVideoSourceBrowsing, -30, SpringLayout.EAST, this.btnVideoSourceBrowsing);
        panelLayout.putConstraint(SpringLayout.EAST, this.btnVideoSourceBrowsing, -3, SpringLayout.EAST, this.panel);
        panelLayout.putConstraint(SpringLayout.SOUTH, this.btnVideoSourceBrowsing, 0, SpringLayout.SOUTH, this.txtVideoSource);

        //Device Source OptionButton and Selection ComboBox are disposed
        //on the fourth row
        panelLayout.putConstraint(SpringLayout.NORTH, this.optDeviceSource, 7, SpringLayout.SOUTH, this.optVideoSource);
        panelLayout.putConstraint(SpringLayout.WEST, this.optDeviceSource, -6, SpringLayout.WEST, lblSource);
        panelLayout.putConstraint(SpringLayout.NORTH, txtDeviceContainer, 1, SpringLayout.NORTH, this.optDeviceSource);
        panelLayout.putConstraint(SpringLayout.WEST, txtDeviceContainer, 12, SpringLayout.EAST, this.optDeviceSource);
        panelLayout.putConstraint(SpringLayout.EAST, txtDeviceContainer, 0, SpringLayout.EAST, this.panel);

        //Device Adjustments Panel is disposed on the fifth row
        panelLayout.putConstraint(SpringLayout.NORTH, this.deviceAdjustmentsPanel, 10, SpringLayout.SOUTH, this.optDeviceSource);
        panelLayout.putConstraint(SpringLayout.WEST, this.deviceAdjustmentsPanel, -4, SpringLayout.WEST, this.optDeviceSource);
        panelLayout.putConstraint(SpringLayout.EAST, this.deviceAdjustmentsPanel, 5, SpringLayout.EAST, this.panel);

        //Bottom Components are disposed on the sixth row
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkFullResolutionPreview, -3, SpringLayout.SOUTH, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkFullResolutionPreview, -6, SpringLayout.WEST, lblSource);
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkInputPreviewEnabled, -3, SpringLayout.SOUTH, this.deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkInputPreviewEnabled, 3, SpringLayout.EAST, this.chkFullResolutionPreview);

        //Input Settings Panel height is defined by constraining its bottom to the bottom of the sixth row
        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, -3, SpringLayout.SOUTH, this.chkFullResolutionPreview);

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
        var inputSettings = this.context.settings().input();

        //Calibration ComboBox
        this.txtCalibration.setModel(new DefaultComboBoxModel<>(inputSettings.calibration().paths()));
        this.txtCalibration.setSelectedItem(inputSettings.calibration().path());

        //Video Source ComboBox
        this.txtVideoSource.setModel(new DefaultComboBoxModel<>(inputSettings.video().paths()));
        this.txtVideoSource.setSelectedItem(inputSettings.video().path());

        //Source radio buttons (onSourceChanged realigns both radios and every dependent widget)
        this.onSourceChanged(inputSettings.source());

        // Device Type ComboBox
        this.txtDeviceType.setSelectedItem(inputSettings.device().type());

        //Device Path ComboBox
        healDevicePath(this.context);
        this.txtDevicePath.setModel(new DefaultComboBoxModel<>(devicePaths(this.context)));
        this.txtDevicePath.setSelectedItem(inputSettings.device().path());

        //Device Resolution combo box (repopulated from the device-advertised sizes)
        this.refreshDeviceResolutions();

        //Device controls CheckBoxes
        this.chkDeviceSustainFramerate.setSelected(inputSettings.device().v4l4j().sustainFramerate());
        this.onSustainFramerateToggled();
        this.chkDeviceTimeoutImageIO.setSelected(inputSettings.device().v4l4j().timeoutImageIO());
        this.onTimeoutImageIOToggled();
        this.chkDeviceKeepFormat.setSelected(inputSettings.device().v4l4j().keepFormat());
        this.onKeepFormatToggled();

        //Preview CheckBoxes
        this.chkFullResolutionPreview.setSelected(inputSettings.fullResolutionPreview());
        this.onFullResolutionPreviewToggled();
        this.chkInputPreviewEnabled.setSelected(inputSettings.inputPreview());
        this.onInputPreviewToggled();
    }

    // Core-healed values, reflected back into the widgets (safe from any thread)

    public void showDeviceResolution(Resolution resolution) {
        SwingUtilities.invokeLater(() -> this.txtDeviceResolution.setSelectedItem(resolution));
    }

    public void showDevicePath(DevicePath devicePath) {
        SwingUtilities.invokeLater(() -> this.txtDevicePath.setSelectedItem(devicePath));
    }

    public void showRecentPath(@NotNull RecentPathTarget target, @NotNull PathSettings pathSettings,
                               String usedPath) {
        JComboBox<String> pathComboBox = switch (target) {
            case Calibration -> this.txtCalibration;
            case VideoSource -> this.txtVideoSource;
        };
        SwingUtilities.invokeLater(() -> {
            pathComboBox.setModel(new DefaultComboBoxModel<>(pathSettings.paths()));
            pathComboBox.setSelectedItem(usedPath);
        });
    }

    // Handlers

    /**
     * An input source option has been chosen (Video or Device): commits the source and
     * realigns every source-dependent widget, including the toolbar's timed button.
     */
    private void onSourceChanged(@NotNull SourceType inputSource) {
        this.context.settings().input().source(inputSource);

        boolean isVideo = SourceType.Video.is(inputSource);
        boolean isDevice = SourceType.Device.is(inputSource);

        this.optVideoSource.setSelected(isVideo);
        SwingUtils.setBoldToggleText(this.optVideoSource, "Video");
        this.txtVideoSource.setEnabled(isVideo);
        this.btnVideoSourceBrowsing.setEnabled(isVideo);

        this.optDeviceSource.setSelected(isDevice);
        SwingUtils.setBoldToggleText(this.optDeviceSource, "Device");
        this.txtDeviceType.setEnabled(isDevice);
        this.txtDevicePath.setEnabled(isDevice);
        this.deviceAdjustmentsPanel.setBorder(SwingUtils.getSettingsSectionBorder("Adjustments",
                isDevice ?
                        GuiConstants.PANEL_BORDER_ACTIVE_COLOR :
                        GuiConstants.PANEL_BORDER_INACTIVE_COLOR));
        this.deviceAdjustmentsPanel.setEnabled(isDevice);
        for (Component comp : this.deviceAdjustmentsPanel.getComponents()) {
            comp.setEnabled(isDevice);
        }

        // Toolbar button visibility
        this.toolbar.setTimedEnabled(isDevice);
    }

    private void onSustainFramerateToggled() {
        this.context.settings().input().device().v4l4j().sustainFramerate(this.chkDeviceSustainFramerate.isSelected());
        SwingUtils.setBoldToggleText(this.chkDeviceSustainFramerate, "Sustain Framerate");
    }

    private void onTimeoutImageIOToggled() {
        this.context.settings().input().device().v4l4j().timeoutImageIO(this.chkDeviceTimeoutImageIO.isSelected());
        SwingUtils.setBoldToggleText(this.chkDeviceTimeoutImageIO, "Timeout Image I/O");
    }

    private void onKeepFormatToggled() {
        this.context.settings().input().device().v4l4j().keepFormat(this.chkDeviceKeepFormat.isSelected());
        SwingUtils.setBoldToggleText(this.chkDeviceKeepFormat, "Keep Format");
    }

    private void onFullResolutionPreviewToggled() {
        this.context.settings().input().fullResolutionPreview(this.chkFullResolutionPreview.isSelected());
        SwingUtils.setBoldToggleText(this.chkFullResolutionPreview, "Full-Resolution Preview");
    }

    private void onInputPreviewToggled() {
        this.context.settings().input().inputPreview(this.chkInputPreviewEnabled.isSelected());
        SwingUtils.setBoldToggleText(this.chkInputPreviewEnabled, "Enable Input Preview (Slower)");
    }

    // Device discovery helpers

    /**
     * Available device identifiers for the currently selected driver, as ComboBox descriptors
     */
    private static DevicePath @NotNull [] devicePaths(@NotNull AppContext context) {
        var device = context.settings().input().device();
        return CommonUtils.getDevicePathDescriptors(DeviceDiscovery.forType(device.type()).listDevices());
    }

    /**
     * Heals the persisted device path so the GUI always shows what will actually run.
     * The requested-to-actual mapping is fully delegated to
     * {@link DeviceDiscovery#resolveDevice}, the same call the cameras make at capture
     * time: startup and play can never disagree on which device a path means.
     */
    private static void healDevicePath(@NotNull AppContext context) {
        var device = context.settings().input().device();
        var discovery = DeviceDiscovery.forType(device.type());

        String saved = device.path().id().trim();
        String resolved = discovery.resolveDevice(saved);
        if (!resolved.isEmpty() && !resolved.equals(saved)) {
            device.path(CommonUtils.getDevicePathDescriptor(resolved));
        }
    }

    /**
     * Resolutions advertised by the currently selected device, decorated with the
     * standard names when they match a {@link DeviceResolution}. Empty when the
     * device cannot be queried, so callers can fall back to the static list.
     */
    private static Resolution @NotNull [] availableDeviceResolutions(@NotNull AppContext context) {
        var device = context.settings().input().device();
        List<Dimension> sizes =
                DeviceDiscovery.forType(device.type()).listViewSizes(device.path().id().trim());
        return sizes.stream()
                .distinct()
                .sorted(Comparator.comparingLong(size -> (long) size.width * size.height))
                .map(size -> {
                    Resolution standard = DeviceResolution.findByResolution(size.width, size.height);
                    return standard != null ? standard : CustomResolution.from(size.width, size.height);
                })
                .toArray(Resolution[]::new);
    }

    /**
     * Repopulates the device resolution ComboBox from the device-advertised sizes.
     * A saved resolution the device no longer advertises is replaced by the nearest
     * advertised one. When the device cannot be queried the static standard list is
     * kept and the saved value is preserved (capture-time adjustment remains the
     * safety net for values the device refuses).
     */
    private void refreshDeviceResolutions() {
        var device = this.context.settings().input().device();
        Resolution[] available = availableDeviceResolutions(this.context);
        if (available.length == 0) {
            this.txtDeviceResolution.setModel(new DefaultComboBoxModel<>(DeviceResolution.values()));
            this.txtDeviceResolution.setSelectedItem(device.resolution());
            return;
        }

        // Same nearest-match metric used by the cameras at capture time, applied to the
        // already-queried list (no second hardware query)
        Resolution saved = device.resolution();
        Resolution nearest = Arrays.stream(available)
                .min(Comparator.comparingLong(item ->
                        CommonUtils.getResolutionDistance(item.width(), item.height(), saved.width(), saved.height())))
                .orElse(available[0]);
        this.txtDeviceResolution.setModel(new DefaultComboBoxModel<>(available));
        // Selecting the item commits it to settings through the combo listener
        this.txtDeviceResolution.setSelectedItem(nearest);
        device.resolution(nearest);
    }

    /**
     * Shift+Delete on the highlighted dropdown entry removes it from the recent-paths
     * history (plain Delete is left to the editor for normal text editing; the current
     * editor text is preserved).
     */
    private static void installRecentPathDeletion(JComboBox<String> pathComboBox,
                                                  @NotNull PathSettings pathSettings) {
        pathComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() != KeyEvent.VK_DELETE || !event.isShiftDown()
                        || !pathComboBox.isPopupVisible()) {
                    return;
                }
                if (!(pathComboBox.getUI().getAccessibleChild(pathComboBox, 0) instanceof ComboPopup popup)) {
                    return;
                }
                int index = popup.getList().getSelectedIndex();
                if (index < 0) {
                    return;
                }

                String editorText = String.valueOf(pathComboBox.getEditor().getItem());
                pathSettings.removeRecentPath(pathComboBox.getItemAt(index));
                pathComboBox.setModel(new DefaultComboBoxModel<>(pathSettings.paths()));
                pathComboBox.setSelectedItem(editorText);
                pathComboBox.showPopup();
                event.consume();
            }
        });
    }

    /**
     * Opens the file browsing dialog for a recent-path ComboBox and commits the choice
     * to the recent-paths history. The dialog is anchored to the ComboBox's own window.
     */
    private static final class BrowseButtonListener implements ActionListener {

        private final JComboBox<String> pathComboBox;
        private final PathSettings pathSettings;
        private final String dialogTitle;
        private final String[] dialogFileFilterExtension;
        private final String[] dialogFileFilterDescription;
        private final boolean enableDirectorySelection;

        private BrowseButtonListener(JComboBox<String> pathComboBox,
                                     PathSettings pathSettings, String dialogTitle,
                                     String[] dialogFileFilterExtension, String[] dialogFileFilterDescription,
                                     boolean enableDirectorySelection) {
            this.pathComboBox = pathComboBox;
            this.pathSettings = pathSettings;
            this.dialogTitle = dialogTitle;
            this.dialogFileFilterExtension = dialogFileFilterExtension;
            this.dialogFileFilterDescription = dialogFileFilterDescription;
            this.enableDirectorySelection = enableDirectorySelection;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {        //When clicking the browsing button
            String dialogPath = (String) pathComboBox.getSelectedItem();//assumes the current Path TextComponent
            //content as the current searching path
            File currentFile = resolveAgainstWorkingDirectory(dialogPath);

            //Creates a new File Browsing Dialog starting from the current path
            //(or from the application working directory when the current path is empty/invalid)
            JFileChooser browse = new JFileChooser(resolveStartingDirectory(currentFile));
            if (currentFile != null && currentFile.isFile()) {
                browse.setSelectedFile(currentFile);            //Preselects the currently configured file
            }
            browse.setDialogTitle(dialogTitle);                    //Sets title to dialogTitle
            for (int i = 0; i < dialogFileFilterExtension.length; i++) {
                final String fileExt = dialogFileFilterExtension[i];
                final String fileDesc = dialogFileFilterDescription[i];
                browse.setFileFilter(new FileFilter() {    //Creates a new file filter for each passed extension

                    @Override
                    public boolean accept(File file) {    //File filter accepted extensions
                        return file.getName().endsWith(fileExt) || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {    //File filter description
                        return fileDesc;
                    }
                });
            }

            if (dialogFileFilterExtension.length > 1) {
                browse.setFileFilter(new FileFilter() {    //Creates a new file filter for all supported extension

                    @Override
                    public boolean accept(File file) {    //File filter accepted extensions
                        boolean accepted = false;
                        for (String fileExt : dialogFileFilterExtension) {
                            accepted = accepted || file.getName().endsWith(fileExt);
                        }
                        return accepted || file.isDirectory();
                    }

                    @Override
                    public String getDescription() {    //File filter description
                        if (dialogFileFilterDescription.length == dialogFileFilterExtension.length + 1)
                            return dialogFileFilterDescription[dialogFileFilterDescription.length - 1];
                        else {
                            String fileDesc = "All supported files (";
                            for (String fileExt : dialogFileFilterExtension) {
                                fileDesc += "*" + fileExt + ", ";
                            }
                            fileDesc = fileDesc.substring(0, fileDesc.length() - 2) + ")";
                            return fileDesc;
                        }
                    }
                });
            }

            browse.setFileSelectionMode(enableDirectorySelection ? JFileChooser.FILES_AND_DIRECTORIES : JFileChooser.FILES_ONLY);

            if (browse.showOpenDialog(pathComboBox) == 0) {//Opens the File Browsing Dialog and if an existing file has been selected
                String choice = browse.getSelectedFile().getAbsolutePath(); //Gets the selected file
                // Browse confirmation commits the choice to the recent-paths history
                pathSettings.pushRecentPath(choice);
                pathComboBox.setModel(new DefaultComboBoxModel<>(pathSettings.paths()));
                pathComboBox.setSelectedItem(choice);    //Sets the Path TextComponent content
                //to the file path (triggering also a change
                //to the Path parameter, thanks to
                //the TextComponent change Listener)
            }
        }

        private static File resolveAgainstWorkingDirectory(String path) {
            if (path == null || path.isBlank()) {
                return null;
            }
            File file = new File(path.trim());
            return file.isAbsolute() ? file : new File(System.getProperty("user.dir"), file.getPath());
        }

        private static @NotNull File resolveStartingDirectory(File currentFile) {
            // Nearest existing ancestor directory of the current path
            File dir = currentFile != null && currentFile.isDirectory() ? currentFile
                    : currentFile != null ? currentFile.getParentFile() : null;
            while (dir != null && !dir.isDirectory()) {
                dir = dir.getParentFile();
            }
            // Fallback: application working directory
            return dir != null ? dir : new File(System.getProperty("user.dir"));
        }
    }
}
