/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.core.CoreRendering;
import com.mtm.vogui.gui.components.control.visualodometry.MonoPlaneOverheadPanel;
import com.mtm.vogui.gui.components.control.visualodometry.VoFallbackPanel;
import com.mtm.vogui.gui.components.info.InfoScrollPane;
import com.mtm.vogui.gui.components.chart.ChartScrollPane;
import com.mtm.vogui.gui.components.control.visualodometry.MonoPlaneInfinityPanel;
import com.mtm.vogui.gui.components.common.combobox.DisplayValueComboBox;
import com.mtm.vogui.gui.components.common.combobox.DisplayValueEditableComboBox;
import com.mtm.vogui.gui.components.common.combobox.StringValueEditableComboBox;
import com.mtm.vogui.gui.components.common.textfield.DoubleTextField;
import com.mtm.vogui.gui.components.common.textfield.FloatTextField;
import com.mtm.vogui.gui.components.common.textfield.IntegerTextField;
import com.mtm.vogui.gui.components.common.border.RoundedCornerBorder;
import com.mtm.vogui.gui.components.common.button.BufferedImageButton;
import com.mtm.vogui.gui.components.common.button.ImageButton;
import com.mtm.vogui.gui.components.common.panel.ToolbarPanel;
import com.mtm.vogui.gui.listeners.common.MaximizeOnDblClickListener;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.settings.*;
import com.mtm.vogui.models.enums.settings.resolution.*;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.ChartAxis;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.models.settings.core.chart.ChartSettings;
import com.mtm.vogui.models.settings.core.image.ImageSettings;
import com.mtm.vogui.models.settings.core.input.InputSettings;
import com.mtm.vogui.models.settings.core.tracker.TrackerSettings;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;
import com.mtm.vogui.utilities.*;

import boofcv.BoofVersion;
import boofcv.gui.image.ImagePanel;

import com.thoughtworks.xstream.XStream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javatuples.Triplet;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
@ApplicationScoped
public class GuiApplication {

    private final Settings settings;
    private final Core core;
    private final GuiController controller;

    // Visual Odometry executor
    private Future<?> voTask;
    private final ExecutorService voExecutor;

    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    private boolean isSystemLookAndFeelEnabled;

    @Inject
    public GuiApplication(Settings settings, Core core, GuiController controller) {
        this.settings = settings;
        this.core = core;
        this.controller = controller;
        this.voExecutor = Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_EXECUTOR_THREAD));
    }

    /**
     * Start application after creating all gui frames
     */
    public void start() {
        // Configure base UI look and feel
        setBaseUI();

        // Input video frame
        createInputVideoFrame();

        // Output video frame (-> visual odometry processing)
        createOutputVideoFrame();

        // Chart/info frame
        createChartFrame();

        // Main frame (-> settings/buttons)
        createMainFrame();
    }

    private void createInputVideoFrame() {
        // Input video panel and frame
        ImagePanel inputVideoPanel = new ImagePanel();
        JFrame inputVideoFrame = new JFrame(GuiConstants.INPUT_VIDEO_FRAME_TITLE);

        // Sets input video frame X location to twice the app frames default width,
        // to the right of chart frame and main frame
        inputVideoFrame.setLocationRelativeTo(null);
        inputVideoFrame.setLocation(
                (getDefaultFrameDimension().width * 2) + 65,
                0
        );

        // Adds the panel to the frame
        inputVideoFrame.getContentPane().add(inputVideoPanel);

        // Bind to controller
        controller.inputVideoPanel(inputVideoPanel);
        controller.inputVideoFrame(inputVideoFrame);
    }

    private void createOutputVideoFrame() {
        // Output video panel (for processed video)
        ImagePanel outputVideoPanel = new ImagePanel();
        JFrame outputVideoFrame = new JFrame(GuiConstants.OUTPUT_VIDEO_FRAME_TITLE);

        // Sets output video frame X location to twice the app frames default width, and Y location at
        // frame default height/2
        outputVideoFrame.setLocationRelativeTo(null);
        outputVideoFrame.setLocation(
                (getDefaultFrameDimension().width * 2) + 65,
                (getDefaultFrameDimension().height / 2)
        );

        //Adds the panel to the frame
        outputVideoFrame.getContentPane().add(outputVideoPanel);

        // Bind to controller
        controller.outputVideoPanel(outputVideoPanel);
        controller.outputVideoFrame(outputVideoFrame);
    }

    private void createChartFrame() {
        // Chart panels
        var chartPanels = createChartPanels();
        ChartScrollPane chartXZPanel = chartPanels.getValue0();
        ChartScrollPane chartYPanel = chartPanels.getValue1();
        InfoScrollPane chartInfoPanel = chartPanels.getValue2();

        // Chart frame
        JFrame chartFrame = createChartFrame(chartPanels);

        // Layout components
        layoutChartFrame(chartFrame, chartPanels);

        // Make chart frame visible
        chartFrame.setVisible(true);

        // Bind to controller
        controller.chartXZPanel(chartXZPanel);
        controller.chartYPanel(chartYPanel);
        controller.infoPanel(chartInfoPanel);
    }

    private Triplet<ChartScrollPane, ChartScrollPane, InfoScrollPane> createChartPanels() {
        // XZ chart panel
        // Panel initialized with default constructor -> X/Y axis centered
        ChartScrollPane chartXZPanel = new ChartScrollPane(GuiConstants.CHART_XZ_TITLE);

        // Configuration
        chartXZPanel.settings().backgroundColor(Color.white);
        chartXZPanel.settings().plotColor(Color.blue);
        chartXZPanel.settings().axisColor(Color.black);
        chartXZPanel.settings().axisNames(ChartAxis.X, ChartAxis.Z);
        chartXZPanel.settings().axisNamesColor(Color.blue);
        chartXZPanel.settings().axisUnitsColor(Color.blue);
        chartXZPanel.settings().showLegend(true);
        chartXZPanel.setPreferredSize(new Dimension(400, 400));

        // Y chart panel
        // Custom origin coordinates, auto-centering disabled for X (Y axis is fixed at 20) and enabled for Y
        // (X axis centered)
        ChartScrollPane chartYPanel =
                new ChartScrollPane(20, 85, false, true, GuiConstants.CHART_Y_TITLE);

        // Configuration
        chartYPanel.settings().backgroundColor(Color.white);
        chartYPanel.settings().plotColor(Color.blue);
        chartYPanel.settings().axisColor(Color.black);
        chartYPanel.settings().axisNames(ChartAxis.Frame, ChartAxis.Y);
        chartYPanel.settings().axisNamesColor(Color.blue);
        chartYPanel.settings().axisUnitsColor(Color.blue);
        chartYPanel.setPreferredSize(new Dimension(400, 200));

        // Info panel
        InfoScrollPane chartInfoPanel =
                new InfoScrollPane(this.settings, this.controller, GuiConstants.INFO_PANEL_TITLE);
        chartInfoPanel.setInfoPanelVisible(false);
        chartInfoPanel.setBufferInfoVisible(false);
        chartInfoPanel.setAppStatus(AppStatus.Ready);
        chartInfoPanel.setPreferredSize(new Dimension(400, 400));

        return new Triplet<>(chartXZPanel, chartYPanel, chartInfoPanel);
    }

    private JFrame createChartFrame(Triplet<ChartScrollPane, ChartScrollPane, InfoScrollPane> chartPanels) {
        ChartScrollPane chartXZPanel = chartPanels.getValue0();
        ChartScrollPane chartYPanel = chartPanels.getValue1();
        InfoScrollPane chartInfoPanel = chartPanels.getValue2();

        // Chart frame
        JFrame chartFrame = new JFrame(GuiConstants.INFO_FRAME_TITLE);
        // Sets default close operation to app termination
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Adds chart panels to chart frame
        chartFrame.getContentPane().add(chartXZPanel);
        chartFrame.getContentPane().add(chartYPanel);
        chartFrame.getContentPane().add(chartInfoPanel);

        // Adds maximize on double click listener to each panel
        chartXZPanel.addMouseListener(new MaximizeOnDblClickListener(chartXZPanel, chartFrame));
        chartYPanel.addMouseListener(new MaximizeOnDblClickListener(chartYPanel, chartFrame));
        chartInfoPanel.addMouseListener(new MaximizeOnDblClickListener(chartInfoPanel, chartFrame));

        return chartFrame;
    }

    private void layoutChartFrame(JFrame chartFrame,
                                  Triplet<ChartScrollPane, ChartScrollPane, InfoScrollPane> chartPanels) {
        ChartScrollPane chartXZPanel = chartPanels.getValue0();
        ChartScrollPane chartYPanel = chartPanels.getValue1();
        InfoScrollPane chartInfoPanel = chartPanels.getValue2();

        // Disposition of components inside chart frame
        Container chartFrameCP = chartFrame.getContentPane();

        // Prepare a spring layout to dispose components inside the frame
        SpringLayout frameLayout = new SpringLayout();
        // Creates a height proportional spring, that returns 11/30 (0.36) of chartFrame height
        Spring hpSpring = new HeightProportionalSpring(chartFrame, 11 / 30f);

        // On first row => XZ chart panel (that extends from the top till 11/30 of the height of chart frame)
        frameLayout.putConstraint(SpringLayout.NORTH, chartXZPanel, 5, SpringLayout.NORTH, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.WEST, chartXZPanel, 5, SpringLayout.WEST, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.EAST, chartXZPanel, -5, SpringLayout.EAST, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.SOUTH, chartXZPanel, hpSpring, SpringLayout.NORTH, chartFrameCP);

        // On second row => Y chart panel
        frameLayout.putConstraint(SpringLayout.NORTH, chartYPanel, 5, SpringLayout.SOUTH, chartXZPanel);
        frameLayout.putConstraint(SpringLayout.WEST, chartYPanel, 5, SpringLayout.WEST, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.EAST, chartYPanel, -5, SpringLayout.EAST, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.SOUTH, chartFrameCP, hpSpring, SpringLayout.SOUTH, chartYPanel);

        // On third row => info panel (that extends from the bottom till 11/30 of the height of chart frame)
        frameLayout.putConstraint(SpringLayout.NORTH, chartInfoPanel, 5, SpringLayout.SOUTH, chartYPanel);
        frameLayout.putConstraint(SpringLayout.WEST, chartInfoPanel, 5, SpringLayout.WEST, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.EAST, chartInfoPanel, -5, SpringLayout.EAST, chartFrameCP);
        frameLayout.putConstraint(SpringLayout.SOUTH, chartInfoPanel, -5, SpringLayout.SOUTH, chartFrameCP);

        // Apply the layout to the panel
        chartFrame.setLayout(frameLayout);
        // Sets chart frame position to 0,0 (top-left of the screen)
        chartFrame.setLocation(0, 0);
        // Sets Chart Frame dimension to Frame default dimension
        chartFrame.setPreferredSize(getDefaultFrameDimension());
        chartFrame.pack();
    }

    private void createMainFrame() {
        // Main scroll pane (containing all settings sub panels)
        JScrollPane mainScrollPane = createMainScrollPane();

        // Operations panel
        ToolbarPanel toolbarPanel = createToolbarPanel();

        // Main frame
        JFrame mainFrame = createMainFrame(mainScrollPane, toolbarPanel);
        if (OSUtils.isMac()) {
            mainFrame.getRootPane().putClientProperty(GuiConstants.MACOS_TRANSPARENT_TITLE_PROPERTY, true);
        }


        // Main frame layout
        layoutMainFrame(mainFrame, mainScrollPane, toolbarPanel);

        mainFrame.setVisible(true);

        // Bind to controller
        this.settings.state().guiComponents().put("btnStartVO", toolbarPanel.btnStartVO());
        this.settings.state().guiComponents().put("btnPauseVO", toolbarPanel.btnPauseVO());
        this.settings.state().guiComponents().put("btnResetVO", toolbarPanel.btnResetVO());
        this.settings.state().guiComponents().put("btnStopVO", toolbarPanel.btnStopVO());
        this.settings.state().guiComponents().put("btnClearVO", toolbarPanel.btnClearVO());
        this.settings.state().guiComponents().put("btnTimedProcessingVO", toolbarPanel.btnTimedProcessingVO());
        this.settings.state().guiComponents().put("mainFrame", mainFrame);
    }

    private JFrame createMainFrame(JScrollPane mainScrollPane, ToolbarPanel toolbarPanel) {
        JFrame mainFrame = new JFrame(GuiConstants.MAIN_FRAME_TITLE);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add all components
        mainFrame.getContentPane().add(mainScrollPane);
        mainFrame.getContentPane().add(toolbarPanel.btnSettings());
        mainFrame.getContentPane().add(toolbarPanel.btnStartVO());
        mainFrame.getContentPane().add(toolbarPanel.btnPauseVO());
        mainFrame.getContentPane().add(toolbarPanel.btnResetVO());
        mainFrame.getContentPane().add(toolbarPanel.btnStopVO());
        mainFrame.getContentPane().add(toolbarPanel.btnClearVO());
        mainFrame.getContentPane().add(toolbarPanel.btnTimedProcessingVO());

        return mainFrame;
    }

    private JScrollPane createMainScrollPane() {

        /*******************
         * MAIN SCROLLPANE *
         *******************/


        /***********************
         * COMPONENTS CREATION *
         ***********************/


        /**
         *  1. TITLE LABEL / SETTINGS SUBPANELS CREATION
         *
         *  Creation of all the mainPanel components and SubPanels
         *
         */

        /*Creates Input Settings Panel*/
        final JPanel inputSettingsPanel = createInputSettingsPanel();

        /*Creates Internal Image Settings Panel*/
        final JPanel internalImageSettingsPanel = createInternalImageSettingsPanel();

        /*Creates Tracker Settings Panel*/
        final JPanel trackerSettingsPanel = createTrackerSettingsPanel();

        /*Creates Visual Odometry Settings Panel*/
        final JPanel visualOdometrySettingsPanel = createVisualOdometrySettingsPanel();

        /*Creates Chart/Output Settings Panel*/
        final JPanel chartSettingsPanel = createChartSettingsPanel();


        /**
         *  2. MAIN PANEL CREATION
         *
         *  Creation of the Main Panel.
         *
         */

        final JPanel mainPanel = new JPanel();

        //Adds title label and all subpanels to mainPanel
        mainPanel.add(inputSettingsPanel);
        mainPanel.add(internalImageSettingsPanel);
        mainPanel.add(trackerSettingsPanel);
        mainPanel.add(visualOdometrySettingsPanel);
        mainPanel.add(chartSettingsPanel);


        /**************************
         * COMPONENTS DISPOSITION *
         *************************/


        /**
         *  3. MAIN PANEL DISPOSITION
         *
         *  Disposition of the components inside the Main Panel.
         *
         */


        SpringLayout panelLayout = new SpringLayout();

        // Input Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, inputSettingsPanel, 10, SpringLayout.NORTH, mainPanel);
        panelLayout.putConstraint(SpringLayout.WEST, inputSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, inputSettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Internal Image Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, internalImageSettingsPanel, 1, SpringLayout.SOUTH, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, internalImageSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, internalImageSettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Tracker Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, trackerSettingsPanel, 1, SpringLayout.SOUTH, internalImageSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, trackerSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, trackerSettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Visual Odometry Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, visualOdometrySettingsPanel, 1, SpringLayout.SOUTH, trackerSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, visualOdometrySettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, visualOdometrySettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Chart Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, chartSettingsPanel, 1, SpringLayout.SOUTH, visualOdometrySettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, chartSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, chartSettingsPanel, -5, SpringLayout.EAST, mainPanel);
        panelLayout.putConstraint(SpringLayout.SOUTH, chartSettingsPanel, 1, SpringLayout.SOUTH, mainPanel);

        //Adds layout to the panel
        mainPanel.setLayout(panelLayout);

        //Sets Main Panel preferred size depending on isSystemLookAndFeelEnabled value:
        mainPanel.setPreferredSize(
                isSystemLookAndFeelEnabled ? new Dimension(480, 990) : new Dimension(480, 910));


        /**
         *  4. MAIN SCROLLPANE CREATION
         *
         *  Creation of the Main ScrollPane.
         *
         */

        //Creates Main ScrollPane
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return mainScrollPane;
    }

    private @NotNull JPanel createInputSettingsPanel() {
        /* Parameters Managed from Input Settings Panel */
        InputSettings inputSettings = settings.core().input();
        HashMap<String, Component> guiComponents = settings.state().guiComponents();


        SpringLayout panelLayout = null;    //Layout Object needed for
        //Components Disposition in the Panel


        /***********************
         * COMPONENTS CREATION *
         ***********************/


        /**
         *  1. CALIBRATION PART
         *
         *  First and upper part of the Input Settings Panel, with the Calibration Settings Part.
         *
         */

        /*Calibration Label*/
        final JLabel lblCalibration = new JLabel("<html><b>Calibration</b></html>");

        // Calibration ComboBox*/
        var txtCalibration = new StringValueEditableComboBox(
                inputSettings.calibration().paths(),
                selected -> inputSettings.calibration().path(selected)
        );
        txtCalibration.setPrefixEnabled(false);
        txtCalibration.setSelectedItem(inputSettings.calibration().path());

        /*Calibration Browsing Button*/
        final JButton btnCalibrationBrowsing = new JButton("...");
        /*Listener (for clicks on Calibration Browsing Button)*/
        btnCalibrationBrowsing.addActionListener(
                new BrowseButtonListener(guiComponents, txtCalibration,
                        "Open Calibration",
                        new String[]{".xml"},
                        new String[]{"XML Camera Calibration File (*.xml)"},
                        false));


        /**
         *  2. SOURCE PART
         *
         *  Second part of the Input Settings Panel, with the Source Settings Part.
         *
         */

        /*Loads default/saved Input Source value (Video or Device) at Startup*/
        boolean isVideo = inputSettings.source().is(SourceType.Video);
        boolean isDevice = inputSettings.source().is(SourceType.Device);



        /*Source Label*/
        final JLabel lblSource = new JLabel("<html><b>Source</b></html>");


        /** VIDEO SOURCE **/


        /*Video Source OptionButton (to activate Video Source [File])*/
        final JRadioButton optVideoSource = new JRadioButton(isVideo ? "<html><b>Video</b></html>" : "<html>Video</html>");
        optVideoSource.setSelected(isVideo); //Startup selection based on default/loaded value
        /*Listener (for click on Video Source OptionButton)*/
        optVideoSource.addActionListener(
                new InputSourceOptionListener(SourceType.Video, this.settings));

        // Video source path ComboBox
        var txtVideoSource = new StringValueEditableComboBox(
                inputSettings.video().paths(),
                selected -> inputSettings.video().path(selected)
        );
        txtVideoSource.setPrefixEnabled(false);
        txtVideoSource.setEnabled(isVideo);
        txtVideoSource.setSelectedItem(inputSettings.video().path());

        // Video source path browse button
        var btnVideoSourceBrowsing = new JButton("...");
        btnVideoSourceBrowsing.setEnabled(isVideo);
        /*Listener (for clicks on Video Source Browsing Button)*/
        btnVideoSourceBrowsing.addActionListener(
                new BrowseButtonListener(guiComponents, txtVideoSource,
                        "Open Video",
                        new String[]{".avi", ".mp4", ".mjpeg"},
                        new String[]{"AVI Audio/Video Interleave (*.avi)", "MPEG-4/H.264 Video (*.mp4)", "Motion JPEG Video (*.mjpeg)", "All supported media (*.mjpeg, *.mp4, *.avi)"},
                        true));


        /** DEVICE SOURCE **/


        /*Device Source OptionButton (to activate Device Source [Camera])*/
        final JRadioButton optDeviceSource = new JRadioButton(isDevice ? "<html><b>Device</b></html>" : "<html>Device</html>");
        optDeviceSource.setSelected(isDevice); //Startup selection based on default/loaded value
        /*Listener (for click on Device Source OptionButton)*/
        optDeviceSource.addActionListener(new InputSourceOptionListener(SourceType.Device, this.settings));

        // Device path ComboBox
        var txtDevicePath = new DisplayValueEditableComboBox<>(
                settings.core().input().device().paths(),
                path -> settings.core().input().device().path(path),
                DevicePath::from);
        txtDevicePath.setEnabled(isDevice);
        txtDevicePath.setSelectedItem(settings.core().input().device().path());

        // Device type ComboBox
        var txtDeviceType = new DisplayValueComboBox<>(
                DeviceType.values(),
                value -> inputSettings.device().type(value),
                index -> DeviceType.values()[index],
                selection -> {
                    // Reload device paths if needed
                    if (!selection.previous().value().is(selection.current().value())) {
                        inputSettings.device().reloadPaths();
                        txtDevicePath.setModel(new DefaultComboBoxModel<>(inputSettings.device().paths()));
                    }
                }
        );
        txtDeviceType.setEnabled(isDevice);
        txtDeviceType.setSelectedItem(inputSettings.device().type().value());

        // Device type/path container
        JPanel txtDeviceContainer = new JPanel(new GridBagLayout());
        txtDeviceContainer.setOpaque(false);
        GridBagConstraints txtDeviceConstraints = new GridBagConstraints();
        txtDeviceConstraints.weightx = 0.5;
        txtDeviceConstraints.fill = GridBagConstraints.HORIZONTAL;
        txtDeviceContainer.add(txtDeviceType, txtDeviceConstraints);
        txtDeviceContainer.add(txtDevicePath, txtDeviceConstraints);
        txtDeviceType.setPreferredSize(new Dimension(1, txtDeviceType.getPreferredSize().height));
        txtDevicePath.setPreferredSize(new Dimension(1, txtDevicePath.getPreferredSize().height));

        /** DEVICE RESOLUTION **/


        // Device Resolution Label
        final JLabel lblDeviceResolution = new JLabel("Resolution");
        lblDeviceResolution.setEnabled(isDevice);

        // Device Resolution combo box
        var txtDeviceResolution = new DisplayValueEditableComboBox<>(
                DeviceResolution.values(),
                resolution -> settings.core().input().device().resolution(resolution),
                CustomResolution::from);
        txtDeviceResolution.setEnabled(isDevice);
        txtDeviceResolution.setHorizontalAlignment(SwingConstants.CENTER);
        txtDeviceResolution.setSelectedItem(inputSettings.device().resolution());

        /** DEVICE CONTROLS **/


        /*Loads default/saved Device Control CheckBox values at Startup*/
        boolean isDeviceSustainFramerateEnabled = inputSettings.device().v4l4j().sustainFramerate();
        boolean isDeviceTimeoutImageIOEnabled = inputSettings.device().v4l4j().timeoutImageIO();
        boolean isDeviceKeepFormatEnabled = inputSettings.device().v4l4j().keepFormat();


        /*Device Sustain Framerate*/
        final JCheckBox chkDeviceSustainFramerate = new JCheckBox(isDeviceSustainFramerateEnabled ? "<html><b>Sustain Framerate</b></html>" : "<html>Sustain Framerate</html>");
        chkDeviceSustainFramerate.setSelected(isDeviceSustainFramerateEnabled); //Sets default/saved Device Sustain Framerate CheckBox value on Startup
        chkDeviceSustainFramerate.setEnabled(isDevice);    //If default/saved Input Source is DEVICE_INPUT("device") is enabled
        /*Listener (for Device Sustain Framerate CheckBox clicks)*/
        chkDeviceSustainFramerate.addActionListener(
                new ParameterCheckBoxListener("deviceSustainFramerate", chkDeviceSustainFramerate, this.settings));

        /*Device Timeout Image I/O*/
        final JCheckBox chkDeviceTimeoutImageIO = new JCheckBox(isDeviceTimeoutImageIOEnabled ? "<html><b>Timeout Image I/O</b></html>" : "<html>Timeout Image I/O</html>");
        chkDeviceTimeoutImageIO.setSelected(isDeviceTimeoutImageIOEnabled); //Sets default/saved Device Timeout Image I/O CheckBox value on Startup
        chkDeviceTimeoutImageIO.setEnabled(isDevice); //If default/saved Input Source is DEVICE_INPUT("device") is enabled
        /*Listener (for Device Timeout Image I/O CheckBox clicks)*/
        chkDeviceTimeoutImageIO.addActionListener(
                new ParameterCheckBoxListener("deviceTimeoutImageIO", chkDeviceTimeoutImageIO, this.settings));

        /*Device Keep Format*/
        final JCheckBox chkDeviceKeepFormat = new JCheckBox(isDeviceKeepFormatEnabled ? "<html><b>Keep Format</b></html>" : "<html>Keep Format</html>");
        chkDeviceKeepFormat.setSelected(isDeviceKeepFormatEnabled);    //Sets default/saved Device Keep Format CheckBox value on Startup
        chkDeviceKeepFormat.setEnabled(isDevice); //If default/saved Input Source is DEVICE_INPUT("device") is enabled
        /*Listener (for Device Keep Format CheckBox clicks)*/
        chkDeviceKeepFormat.addActionListener(
                new ParameterCheckBoxListener("deviceKeepFormat", chkDeviceKeepFormat, this.settings));


        /** DEVICE ADJUSTMENTS PANEL - CREATION **/

        JPanel deviceAdjustmentsPanel = new JPanel(); //Creates the panel that will contain all Device Adjustments Components
        deviceAdjustmentsPanel.setOpaque(false);
        deviceAdjustmentsPanel.setBorder(
                GuiUtils.getRoundedTitledBorder("Adjustments",
                        isDevice ?
                                GuiConstants.PANEL_BORDER_ACTIVE_COLOR :
                                GuiConstants.PANEL_BORDER_INACTIVE_COLOR,
                        10,
                        10));
        deviceAdjustmentsPanel.setEnabled(isDevice); //If default/saved Input Source is DEVICE_INPUT("device") is enabled


        //Adds to the panel all the Device Adjustments Components: Device Width/Height Labels/TextField
        //and Device Controls CheckBox
        deviceAdjustmentsPanel.add(lblDeviceResolution);
        deviceAdjustmentsPanel.add(txtDeviceResolution);
        deviceAdjustmentsPanel.add(chkDeviceSustainFramerate);
        deviceAdjustmentsPanel.add(chkDeviceTimeoutImageIO);
        deviceAdjustmentsPanel.add(chkDeviceKeepFormat);


        /** DEVICE ADJUSTMENTS PANEL - DISPOSITION **/


        panelLayout = new SpringLayout(); //Initialize a new SpringLayout

        //SpringLayout Configuration:

        //Device Width/Height Label and TextField, and Device Sustain Framerate CheckBox
        //are disposed on the same row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, lblDeviceResolution, -1, SpringLayout.NORTH, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblDeviceResolution, -6, SpringLayout.WEST, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtDeviceResolution, -3, SpringLayout.NORTH, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, txtDeviceResolution, 8, SpringLayout.EAST, lblDeviceResolution);
        panelLayout.putConstraint(SpringLayout.NORTH, chkDeviceSustainFramerate, -3, SpringLayout.NORTH, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, chkDeviceSustainFramerate, 0, SpringLayout.EAST, txtDeviceResolution);

        //Device Timeout Image I/O CheckBox and Device Keep Format CheckBox are disposed on the second row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, chkDeviceTimeoutImageIO, 10, SpringLayout.SOUTH, lblDeviceResolution);
        panelLayout.putConstraint(SpringLayout.WEST, chkDeviceTimeoutImageIO, -14, SpringLayout.WEST, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, chkDeviceKeepFormat, 10, SpringLayout.SOUTH, lblDeviceResolution);
        panelLayout.putConstraint(SpringLayout.WEST, chkDeviceKeepFormat, 0, SpringLayout.WEST, chkDeviceSustainFramerate);

        //Device Adjustments Panel height is defined by constraining its bottom to the bottom of the second row
        panelLayout.putConstraint(SpringLayout.SOUTH, deviceAdjustmentsPanel, 0, SpringLayout.SOUTH, chkDeviceTimeoutImageIO);


        //Applying the Layout to the Device Adjustments Panel
        deviceAdjustmentsPanel.setLayout(panelLayout);


        /**
         *  3. BOTTOM PART (Full Resolution Preview, Input Preview)
         *
         *  Bottom part of the Input Settings Panel, with the last components.
         *
         */


        /**Some Input/Output Settings CheckBox**/


        // Full-Resolution preview CheckBox (applies to input/output preview resolution)
        JCheckBox chkFullResolutionPreview = new JCheckBox(inputSettings.fullResolutionPreview() ? "<html><b>Full-Resolution Preview</b></html>" : "<html>Full-Resolution Preview</html>");
        chkFullResolutionPreview.setSelected(inputSettings.fullResolutionPreview()); //Sets the CheckBox to the default/saved value on Startup
        /*Listener (for clicks on Full-Resolution Preview CheckBox)*/
        chkFullResolutionPreview.addActionListener(
                new ParameterCheckBoxListener("fullResolutionPreview", chkFullResolutionPreview, this.settings));

        /*Input Preview Enabled CheckBox [Enable Input (Video file/Camera) Preview]*/
        final JCheckBox chkInputPreviewEnabled = new JCheckBox(inputSettings.inputPreview() ? "<html><b>Enable Input Preview (Slower)</b></html>" : "<html>Enable Input Preview (Slower)</html>");
        chkInputPreviewEnabled.setSelected(inputSettings.inputPreview()); //Sets the CheckBox to default/saved value on Startup
        /*Listener (for clicks on Input Preview Enabled CheckBox)*/
        chkInputPreviewEnabled.addActionListener(
                new ParameterCheckBoxListener("inputPreviewEnabled", chkInputPreviewEnabled, this.settings));


        /**
         *  4. POPULATE GUICOMPONENTS
         *
         *  Adds all the most important (and reused) components to the guiComponents HashMap
         *
         */

        guiComponents.put("txtCalibration", txtCalibration);
        guiComponents.put("optVideoSource", optVideoSource);
        guiComponents.put("txtVideoSource", txtVideoSource);
        guiComponents.put("btnVideoSourceBrowsing", btnVideoSourceBrowsing);
        guiComponents.put("optDeviceSource", optDeviceSource);
        guiComponents.put("txtDeviceType", txtDeviceType);
        guiComponents.put("txtDevicePath", txtDevicePath);
        guiComponents.put("lblDeviceResolution", lblDeviceResolution);
        guiComponents.put("txtDeviceResolution", txtDeviceResolution);
        guiComponents.put("chkDeviceSustainFramerate", chkDeviceSustainFramerate);
        guiComponents.put("chkDeviceTimeoutImageIO", chkDeviceTimeoutImageIO);
        guiComponents.put("chkDeviceKeepFormat", chkDeviceKeepFormat);
        guiComponents.put("deviceAdjustmentsPanel", deviceAdjustmentsPanel);
        guiComponents.put("chkFullResolutionPreview", chkFullResolutionPreview);
        guiComponents.put("chkInputPreviewEnabled", chkInputPreviewEnabled);


        /**
         *  5. INPUT SETTINGS PANEL CREATION
         *
         *  Creation of the Input Settings Panel.
         *
         */

        final JPanel inputSettingsPanel = new JPanel(); //Creates the Input Settings Panel, that will contain all the previous components
        inputSettingsPanel.setOpaque(false);

        //Sets a compound border (TitledBorder+EmptyBorder)
        inputSettingsPanel.setBorder(GuiUtils
                .getRoundedTitledBorder("Input", GuiConstants.PANEL_BORDER_ACTIVE_COLOR, 10, 10));

        //Adds to the panel all the components (Calibration, Source (Video/Device), Bottom Part)
        inputSettingsPanel.add(lblCalibration);
        inputSettingsPanel.add(txtCalibration);
        inputSettingsPanel.add(btnCalibrationBrowsing);
        inputSettingsPanel.add(lblSource);
        inputSettingsPanel.add(optVideoSource);
        inputSettingsPanel.add(txtVideoSource);
        inputSettingsPanel.add(btnVideoSourceBrowsing);
        inputSettingsPanel.add(optDeviceSource);
        inputSettingsPanel.add(txtDeviceContainer);
        inputSettingsPanel.add(deviceAdjustmentsPanel);
        inputSettingsPanel.add(chkFullResolutionPreview);
        inputSettingsPanel.add(chkInputPreviewEnabled);


        /**************************
         * COMPONENTS DISPOSITION *
         *************************/


        /**
         *  6. INPUT SETTINGS PANEL DISPOSITION
         *
         *  Disposition of the components inside Input Settings Panel.
         *
         */

        panelLayout = new SpringLayout(); //Initialize a new SpringLayout

        //SpringLayout Configuration:

        //Calibration Label, Selection ComboBox and Browsing Button
        //are disposed on the same row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, lblCalibration, 0, SpringLayout.NORTH, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblCalibration, 3, SpringLayout.WEST, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtCalibration, -3, SpringLayout.NORTH, lblCalibration);
        panelLayout.putConstraint(SpringLayout.WEST, txtCalibration, 10, SpringLayout.EAST, lblCalibration);
        panelLayout.putConstraint(SpringLayout.EAST, txtCalibration, -35, SpringLayout.EAST, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, btnCalibrationBrowsing, 0, SpringLayout.NORTH, txtCalibration);
        panelLayout.putConstraint(SpringLayout.WEST, btnCalibrationBrowsing, -30, SpringLayout.EAST, btnCalibrationBrowsing);
        panelLayout.putConstraint(SpringLayout.EAST, btnCalibrationBrowsing, -3, SpringLayout.EAST, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.SOUTH, btnCalibrationBrowsing, 0, SpringLayout.SOUTH, txtCalibration);

        //Source Label is disposed in the second row, under Calibration Label
        panelLayout.putConstraint(SpringLayout.NORTH, lblSource, 10, SpringLayout.SOUTH, lblCalibration);
        panelLayout.putConstraint(SpringLayout.WEST, lblSource, 3, SpringLayout.WEST, inputSettingsPanel);

        //Video Source OptionButton, Selection ComboBox and Browsing Button
        //are disposed on the third row, close together
        panelLayout.putConstraint(SpringLayout.NORTH, optVideoSource, 5, SpringLayout.SOUTH, lblSource);
        panelLayout.putConstraint(SpringLayout.WEST, optVideoSource, -6, SpringLayout.WEST, lblSource);
        panelLayout.putConstraint(SpringLayout.NORTH, txtVideoSource, 1, SpringLayout.NORTH, optVideoSource);
        panelLayout.putConstraint(SpringLayout.WEST, txtVideoSource, 4, SpringLayout.WEST, txtDeviceContainer);    // lato sx di txt_video associato al sx di txt_device perchè lbl_video è più corta
        panelLayout.putConstraint(SpringLayout.EAST, txtVideoSource, -35, SpringLayout.EAST, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, btnVideoSourceBrowsing, 0, SpringLayout.NORTH, txtVideoSource);
        panelLayout.putConstraint(SpringLayout.WEST, btnVideoSourceBrowsing, -30, SpringLayout.EAST, btnVideoSourceBrowsing);
        panelLayout.putConstraint(SpringLayout.EAST, btnVideoSourceBrowsing, -3, SpringLayout.EAST, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.SOUTH, btnVideoSourceBrowsing, 0, SpringLayout.SOUTH, txtVideoSource);

        //Device Source OptionButton and Selection ComboBox are disposed
        //on the fourth row
        panelLayout.putConstraint(SpringLayout.NORTH, optDeviceSource, 7, SpringLayout.SOUTH, optVideoSource);
        panelLayout.putConstraint(SpringLayout.WEST, optDeviceSource, -6, SpringLayout.WEST, lblSource);
        panelLayout.putConstraint(SpringLayout.NORTH, txtDeviceContainer, 1, SpringLayout.NORTH, optDeviceSource);
        panelLayout.putConstraint(SpringLayout.WEST, txtDeviceContainer, 12, SpringLayout.EAST, optDeviceSource);
        panelLayout.putConstraint(SpringLayout.EAST, txtDeviceContainer, 0, SpringLayout.EAST, inputSettingsPanel);

        //Device Adjustments Panel is disposed on the fifth row
        panelLayout.putConstraint(SpringLayout.NORTH, deviceAdjustmentsPanel, 10, SpringLayout.SOUTH, optDeviceSource);
        panelLayout.putConstraint(SpringLayout.WEST, deviceAdjustmentsPanel, -4, SpringLayout.WEST, optDeviceSource);
        panelLayout.putConstraint(SpringLayout.EAST, deviceAdjustmentsPanel, 5, SpringLayout.EAST, inputSettingsPanel);

        //Bottom Components are disposed on the sixth row
        panelLayout.putConstraint(SpringLayout.NORTH, chkFullResolutionPreview, -3, SpringLayout.SOUTH, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, chkFullResolutionPreview, -6, SpringLayout.WEST, lblSource);
        panelLayout.putConstraint(SpringLayout.NORTH, chkInputPreviewEnabled, -3, SpringLayout.SOUTH, deviceAdjustmentsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, chkInputPreviewEnabled, 3, SpringLayout.EAST, chkFullResolutionPreview);

        //Input Settings Panel height is defined by constraining its bottom to the bottom of the sixth row
        panelLayout.putConstraint(SpringLayout.SOUTH, inputSettingsPanel, -3, SpringLayout.SOUTH, chkFullResolutionPreview);


        //Applying the Layout to the Input Settings Panel
        inputSettingsPanel.setLayout(panelLayout);


        return inputSettingsPanel;
    }

    private @NotNull JPanel createInternalImageSettingsPanel() {
        /* Parameters Managed from Internal Image Settings Panel */
        ImageSettings imageSettings = this.settings.core().image();
        ImageSettings defaultImageSettings = imageSettings.getDefault();
        HashMap<String, Component> guiComponents = this.settings.state().guiComponents();

        SpringLayout panelLayout = null; //Layout Object needed for components disposition


        /***********************
         * COMPONENTS CREATION *
         ***********************/


        /**
         *  1. IMAGE TYPE PART
         *
         *  First and upper part of the Internal Image Settings Panel.
         *
         */


        /*Image Type Label*/
        final JLabel lblImageType = new JLabel("<html><b>Image Type:</b></html>");

        // Image type ComboBox
        var txtImageType = new DisplayValueComboBox<>(
                ImageTypeDescriptor.values(),
                imageSettings::descriptor,
                index -> ImageTypeDescriptor.values()[index]
        );
        txtImageType.setSelectedItem(imageSettings.descriptor());

        /**
         *  2. IMAGE RESIZE PART
         *
         *  Second and central part of the Internal Image Settings Panel. Contains Image Resize
         *  components and Internal Image Preview CheckBox.
         */

        // Image resize CheckBox
        JCheckBox chkImageResize = new JCheckBox(
                imageSettings.resize() ? "<html><b>Resize</b></html>" : "<html>Resize</html>");
        chkImageResize.setSelected(imageSettings.resize());
        chkImageResize.addActionListener(
                new ParameterCheckBoxListener("imageResize", chkImageResize, this.settings));

        // Image Resize ComboBox
        var txtImageResize = new DisplayValueEditableComboBox<>(
                ResizeResolution.values(),
                imageSettings::resolution,
                CustomResolution::from);
        txtImageResize.setEnabled(imageSettings.resize());
        txtImageResize.setHorizontalAlignment(SwingConstants.CENTER);
        txtImageResize.setPreferredSize(new Dimension(100, txtImageResize.getPreferredSize().height));
        txtImageResize.setSelectedItem(imageSettings.resolution());

        // Internal Image Preview CheckBox
        JCheckBox chkInternalImagePreview = new JCheckBox(
                imageSettings.internalImagePreview() ? "<html><b>Preview Internal Image (Slower)</b></html>" : "<html>Preview Internal Image (Slower)</html>");
        chkInternalImagePreview.setSelected(imageSettings.internalImagePreview());
        chkInternalImagePreview.addActionListener(
                new ParameterCheckBoxListener("internalImagePreview", chkInternalImagePreview, this.settings));

        /**
         *  3. BOTTOM PART (Frame skip)
         *
         *  Last part of the Internal Image Settings Panel: contains the Frame skip component.
         */

        // Frame skip enabled CheckBox
        final JCheckBox chkFrameSkipEnabled = new JCheckBox(
                imageSettings.frameSkipEnabled() ? "<html><b>Frame skip</b></html>" : "<html>Frame skip</html>");
        chkFrameSkipEnabled.setSelected(imageSettings.frameSkipEnabled());
        /*Listener*/
        chkFrameSkipEnabled.addActionListener(
                new ParameterCheckBoxListener("frameSkipEnabled", chkFrameSkipEnabled, this.settings));

        // Frame skip value TextField
        var txtFrameSkipValue = new IntegerTextField(
                NumberConstraints.StrictlyPositive,
                imageSettings::frameSkipValue,
                imageSettings::frameSkipValue,
                defaultImageSettings.frameSkipValue(),
                3,
                JTextField.CENTER
        );

        /**
         *  4. POPULATE GUICOMPONENTS
         *
         *  Adds all the most important (and reused) components to the guiComponents HashMap
         *
         */


        guiComponents.put("txtImageType", txtImageType);
        guiComponents.put("chkImageKeepOriginal", chkImageResize);
        guiComponents.put("chkInternalImagePreview", chkInternalImagePreview);
        guiComponents.put("txtImageResize", txtImageResize);
        guiComponents.put("chkFrameSkipEnabled", chkFrameSkipEnabled);
        guiComponents.put("txtFrameSkipValue", txtFrameSkipValue);


        /**
         *  5. INTERNAL IMAGE SETTINGS PANEL CREATION
         *
         *  Creation of the Internal Image Settings Panel.
         *
         */


        final JPanel internalImageSettingsPanel = new JPanel();
        internalImageSettingsPanel.setOpaque(false);

        //Sets a compound border (TitledBorder+EmptyBorder)
        internalImageSettingsPanel.setBorder(GuiUtils
                .getRoundedTitledBorder("Image", GuiConstants.PANEL_BORDER_ACTIVE_COLOR, 10, 10));

        //Adds to the panel all the components (Image Type, Image Resize,
        //Image Keep Original, Internal Image Preview, and Bottom Part [Frame Decimate, Image Buffer Size])
        internalImageSettingsPanel.add(lblImageType);
        internalImageSettingsPanel.add(txtImageType);
        internalImageSettingsPanel.add(chkImageResize);
        internalImageSettingsPanel.add(txtImageResize);
        internalImageSettingsPanel.add(chkInternalImagePreview);
        internalImageSettingsPanel.add(chkFrameSkipEnabled);
        internalImageSettingsPanel.add(txtFrameSkipValue);


        /**************************
         * COMPONENTS DISPOSITION *
         *************************/


        /**
         *  6. INTERNAL IMAGE SETTINGS PANEL DISPOSITION
         *
         *  Disposition of the components inside Internal Image Settings Panel.
         *
         */


        panelLayout = new SpringLayout(); //Initialize a new SpringLayout

        //On the first row Image Type components
        panelLayout.putConstraint(SpringLayout.NORTH, lblImageType, 0, SpringLayout.NORTH, internalImageSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblImageType, 3, SpringLayout.WEST, internalImageSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtImageType, -3, SpringLayout.NORTH, lblImageType);
        panelLayout.putConstraint(SpringLayout.WEST, txtImageType, 12, SpringLayout.EAST, lblImageType);
        panelLayout.putConstraint(SpringLayout.EAST, txtImageType, -3, SpringLayout.EAST, internalImageSettingsPanel);

        // Image Keep Original, Resize Width/Height
        panelLayout.putConstraint(SpringLayout.NORTH, chkImageResize, 10, SpringLayout.SOUTH, lblImageType);
        panelLayout.putConstraint(SpringLayout.WEST, chkImageResize, -3, SpringLayout.WEST, internalImageSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtImageResize, 1, SpringLayout.NORTH, chkImageResize);
        panelLayout.putConstraint(SpringLayout.WEST, txtImageResize, 4, SpringLayout.WEST, txtImageType);

        // Internal Image Preview CheckBox
        panelLayout.putConstraint(SpringLayout.NORTH, chkInternalImagePreview, 0, SpringLayout.NORTH, chkImageResize);
        panelLayout.putConstraint(SpringLayout.WEST, chkInternalImagePreview, 5, SpringLayout.EAST, txtImageResize);

        // Frame skip CheckBox and TextField
        panelLayout.putConstraint(SpringLayout.NORTH, chkFrameSkipEnabled, 6, SpringLayout.SOUTH, chkInternalImagePreview);
        panelLayout.putConstraint(SpringLayout.WEST, chkFrameSkipEnabled, 0, SpringLayout.WEST, chkImageResize);
        panelLayout.putConstraint(SpringLayout.NORTH, txtFrameSkipValue, 1, SpringLayout.NORTH, chkFrameSkipEnabled);
        panelLayout.putConstraint(SpringLayout.WEST, txtFrameSkipValue, 4, SpringLayout.WEST, txtImageType);

        //Internal Image Settings Panel height is defined by constraining its bottom to the bottom of its last row
        panelLayout.putConstraint(SpringLayout.SOUTH, internalImageSettingsPanel, 0, SpringLayout.SOUTH, chkFrameSkipEnabled);

        //Applying the Layout to the Panel
        internalImageSettingsPanel.setLayout(panelLayout);

        return internalImageSettingsPanel;
    }

    private @NotNull JPanel createTrackerSettingsPanel() {
        TrackerSettings trackerSettings = this.settings.core().tracker();
        TrackerSettings defaultTrackerSettings = trackerSettings.getDefault();

        HashMap<String, Component> guiComponents = this.settings.state().guiComponents();

        SpringLayout panelLayout;

        // Tracker type
        var lblTrackerType = new JLabel("<html><b>Tracker Type:</b></html>");
        var txtTrackerType = new JComboBox<String>(
                trackerSettings.getTrackerTypeNames().values().toArray(new String[]{}));
        txtTrackerType.setSelectedItem(trackerSettings.getTrackerTypeNames().get(trackerSettings.getTrackerType()));
        /*Listener*/
        txtTrackerType.addActionListener(
                new TrackerTypeChangeListener(txtTrackerType, this.settings));

        // KLT tracker components

        // templateRadius
        var lblKltTrackerTemplateRadius = new JLabel("<html>Template Radius:</html>");
        var txtKltTrackerTemplateRadius = new IntegerTextField(
                trackerSettings::setKltTracker_templateRadius,
                trackerSettings::getKltTracker_templateRadius,
                defaultTrackerSettings.getKltTracker_templateRadius(),
                5,
                JTextField.CENTER
        );

        // pyramidScaling
        var lblKltTrackerPyramidLevels = new JLabel("<html>Pyramid Levels:</html>");
        var txtKltTrackerPyramidLevels = new IntegerTextField(
                trackerSettings::setKltTracker_pyramidLevels,
                trackerSettings::getKltTracker_pyramidLevels,
                defaultTrackerSettings.getKltTracker_pyramidLevels(),
                5,
                JTextField.CENTER
        );

        // maxFeatures
        var lblKltTrackerMaxFeatures = new JLabel("<html>Max Features:</html>");
        var txtKltTracker_maxFeatures = new IntegerTextField(
                trackerSettings::setKltTracker_maxFeatures,
                trackerSettings::getKltTracker_maxFeatures,
                defaultTrackerSettings.getKltTracker_maxFeatures(),
                5,
                JTextField.CENTER
        );

        // radius
        var lblKltTrackerRadius = new JLabel("<html>Radius:</html>");
        var txtKltTrackerRadius = new IntegerTextField(
                trackerSettings::setKltTracker_radius,
                trackerSettings::getKltTracker_radius,
                defaultTrackerSettings.getKltTracker_radius(),
                5,
                JTextField.CENTER
        );

        // threshold
        var lblKltTrackerThreshold = new JLabel("<html>Threshold:</html>");
        var txtKltTrackerThreshold = new FloatTextField(
                trackerSettings::setKltTracker_threshold,
                trackerSettings::getKltTracker_threshold,
                defaultTrackerSettings.getKltTracker_threshold(),
                5,
                JTextField.CENTER
        );

        // panel
        var kltTrackerPanel = new JPanel();
        kltTrackerPanel.add(lblKltTrackerTemplateRadius);
        kltTrackerPanel.add(txtKltTrackerTemplateRadius);
        kltTrackerPanel.add(lblKltTrackerPyramidLevels);
        kltTrackerPanel.add(txtKltTrackerPyramidLevels);
        kltTrackerPanel.add(lblKltTrackerMaxFeatures);
        kltTrackerPanel.add(txtKltTracker_maxFeatures);
        kltTrackerPanel.add(lblKltTrackerRadius);
        kltTrackerPanel.add(txtKltTrackerRadius);
        kltTrackerPanel.add(lblKltTrackerThreshold);
        kltTrackerPanel.add(txtKltTrackerThreshold);

        kltTrackerPanel.setOpaque(false);
        kltTrackerPanel.setVisible(trackerSettings.getTrackerType().equals(TrackerSettings.DEFAULT_TRACKER) ||
                trackerSettings.getTrackerType().equals(TrackerSettings.KLT) ||
                trackerSettings.getTrackerType().equals(TrackerSettings.KLT2));

        kltTrackerPanel.setEnabled(!trackerSettings.getTrackerType().equals(TrackerSettings.DEFAULT_TRACKER));
        for (Component comp : kltTrackerPanel.getComponents()) {
            comp.setEnabled(!trackerSettings.getTrackerType().equals(TrackerSettings.DEFAULT_TRACKER));
        }

        // layout
        panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblKltTrackerTemplateRadius, 5, SpringLayout.NORTH, kltTrackerPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblKltTrackerTemplateRadius, 0, SpringLayout.WEST, kltTrackerPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtKltTrackerTemplateRadius, -1, SpringLayout.NORTH, lblKltTrackerTemplateRadius);
        panelLayout.putConstraint(SpringLayout.WEST, txtKltTrackerTemplateRadius, 3, SpringLayout.EAST, lblKltTrackerTemplateRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, lblKltTrackerPyramidLevels, 0, SpringLayout.NORTH, lblKltTrackerTemplateRadius);
        panelLayout.putConstraint(SpringLayout.WEST, lblKltTrackerPyramidLevels, 3, SpringLayout.EAST, txtKltTrackerTemplateRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, txtKltTrackerPyramidLevels, -1, SpringLayout.NORTH, lblKltTrackerPyramidLevels);
        panelLayout.putConstraint(SpringLayout.WEST, txtKltTrackerPyramidLevels, 3, SpringLayout.EAST, lblKltTrackerPyramidLevels);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblKltTrackerMaxFeatures, 10, SpringLayout.SOUTH, lblKltTrackerTemplateRadius);
        panelLayout.putConstraint(SpringLayout.WEST, lblKltTrackerMaxFeatures, 0, SpringLayout.WEST, kltTrackerPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtKltTracker_maxFeatures, -1, SpringLayout.NORTH, lblKltTrackerMaxFeatures);
        panelLayout.putConstraint(SpringLayout.WEST, txtKltTracker_maxFeatures, 0, SpringLayout.WEST, txtKltTrackerTemplateRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, lblKltTrackerRadius, 0, SpringLayout.NORTH, lblKltTrackerMaxFeatures);
        panelLayout.putConstraint(SpringLayout.WEST, lblKltTrackerRadius, 3, SpringLayout.EAST, txtKltTracker_maxFeatures);
        panelLayout.putConstraint(SpringLayout.NORTH, txtKltTrackerRadius, -1, SpringLayout.NORTH, lblKltTrackerRadius);
        panelLayout.putConstraint(SpringLayout.WEST, txtKltTrackerRadius, 3, SpringLayout.EAST, lblKltTrackerRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, lblKltTrackerThreshold, 0, SpringLayout.NORTH, lblKltTrackerRadius);
        panelLayout.putConstraint(SpringLayout.WEST, lblKltTrackerThreshold, 3, SpringLayout.EAST, txtKltTrackerRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, txtKltTrackerThreshold, -1, SpringLayout.NORTH, lblKltTrackerThreshold);
        panelLayout.putConstraint(SpringLayout.WEST, txtKltTrackerThreshold, 3, SpringLayout.EAST, lblKltTrackerThreshold);

        panelLayout.putConstraint(SpringLayout.SOUTH, kltTrackerPanel, 0, SpringLayout.SOUTH, txtKltTracker_maxFeatures);

        kltTrackerPanel.setLayout(panelLayout);

        // SURF tracker components

        // maxFeaturesPerScale
        var lblSurfTrackerMaxFeaturesPerScale = new JLabel("<html>Max Features Per Scale:</html>");
        var txtSurfTrackerMaxFeaturesPerScale = new IntegerTextField(
                trackerSettings::setSurfTracker_maxFeaturesPerScale,
                trackerSettings::getSurfTracker_maxFeaturesPerScale,
                defaultTrackerSettings.getSurfTracker_maxFeaturesPerScale(),
                5,
                JTextField.CENTER
        );

        // extractRadius
        var lblSurfTrackerExtractRadius = new JLabel("<html>Extract Radius:</html>");
        var txtSurfTrackerExtractRadius = new IntegerTextField(
                trackerSettings::setSurfTracker_extractRadius,
                trackerSettings::getSurfTracker_extractRadius,
                defaultTrackerSettings.getSurfTracker_extractRadius(),
                5,
                JTextField.CENTER
        );

        // initialSampleSize
        var lblSurfTrackerInitialSampleSize = new JLabel("<html>Initial Sample Size:</html>");
        var txtSurfTrackerInitialSampleSize = new IntegerTextField(
                trackerSettings::setSurfTracker_initialSampleSize,
                trackerSettings::getSurfTracker_initialSampleSize,
                defaultTrackerSettings.getSurfTracker_initialSampleSize(),
                5,
                JTextField.CENTER
        );

        // panel
        var surfTrackerPanel = new JPanel();
        surfTrackerPanel.add(lblSurfTrackerMaxFeaturesPerScale);
        surfTrackerPanel.add(txtSurfTrackerMaxFeaturesPerScale);
        surfTrackerPanel.add(lblSurfTrackerExtractRadius);
        surfTrackerPanel.add(txtSurfTrackerExtractRadius);
        surfTrackerPanel.add(lblSurfTrackerInitialSampleSize);
        surfTrackerPanel.add(txtSurfTrackerInitialSampleSize);

        surfTrackerPanel.setOpaque(false);
        surfTrackerPanel.setVisible(trackerSettings.getTrackerType().equals(TrackerSettings.SURF) ||
                trackerSettings.getTrackerType().equals(TrackerSettings.SURF2));

        // layout
        panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblSurfTrackerMaxFeaturesPerScale, 5, SpringLayout.NORTH, surfTrackerPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblSurfTrackerMaxFeaturesPerScale, 0, SpringLayout.WEST, surfTrackerPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtSurfTrackerMaxFeaturesPerScale, -1, SpringLayout.NORTH, lblSurfTrackerMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.WEST, txtSurfTrackerMaxFeaturesPerScale, 3, SpringLayout.EAST, lblSurfTrackerMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.NORTH, lblSurfTrackerExtractRadius, 0, SpringLayout.NORTH, lblSurfTrackerMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblSurfTrackerExtractRadius, 3, SpringLayout.EAST, txtSurfTrackerMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.NORTH, txtSurfTrackerExtractRadius, -1, SpringLayout.NORTH, lblSurfTrackerExtractRadius);
        panelLayout.putConstraint(SpringLayout.WEST, txtSurfTrackerExtractRadius, 3, SpringLayout.EAST, lblSurfTrackerExtractRadius);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblSurfTrackerInitialSampleSize, 10, SpringLayout.SOUTH, lblSurfTrackerMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblSurfTrackerInitialSampleSize, 0, SpringLayout.WEST, surfTrackerPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtSurfTrackerInitialSampleSize, -1, SpringLayout.NORTH, lblSurfTrackerInitialSampleSize);
        panelLayout.putConstraint(SpringLayout.WEST, txtSurfTrackerInitialSampleSize, 3, SpringLayout.EAST, lblSurfTrackerInitialSampleSize);

        panelLayout.putConstraint(SpringLayout.SOUTH, surfTrackerPanel, 0, SpringLayout.SOUTH, txtSurfTrackerInitialSampleSize);

        surfTrackerPanel.setLayout(panelLayout);

        // Tracker options

        // Show active Tracks
        var chkTrackerShowActiveTracks =
                new JCheckBox(trackerSettings.isTrackerShowActiveTracks() ? "<html><b>Show Active Tracks</b></html>" : "<html>Show Active Tracks</html>");
        chkTrackerShowActiveTracks.setSelected(trackerSettings.isTrackerShowActiveTracks());
        /*Listener*/
        chkTrackerShowActiveTracks.addActionListener(
                new ParameterCheckBoxListener("trackerShowActiveTracks", chkTrackerShowActiveTracks, this.settings));

        // Show new tracks
        var chkTrackerShowNewTracks =
                new JCheckBox(trackerSettings.isTrackerShowNewTracks() ? "<html><b>Show New Tracks</b></html>" : "<html>Show New Tracks</html>");
        chkTrackerShowNewTracks.setSelected(trackerSettings.isTrackerShowNewTracks());
        /*Listener*/
        chkTrackerShowNewTracks.addActionListener(
                new ParameterCheckBoxListener("trackerShowNewTracks", chkTrackerShowNewTracks, this.settings));

        // Fill guiComponents

        guiComponents.put("txtTrackerType", txtTrackerType);
        guiComponents.put("txtKltTracker_templateRadius", txtKltTrackerTemplateRadius);
        guiComponents.put("txtKltTracker_pyramidLevels", txtKltTrackerPyramidLevels);
        guiComponents.put("txtKltTracker_maxFeatures", txtKltTracker_maxFeatures);
        guiComponents.put("txtKltTracker_radius", txtKltTrackerRadius);
        guiComponents.put("txtKltTracker_threshold", txtKltTrackerThreshold);
        guiComponents.put("kltTrackerPanel", kltTrackerPanel);
        guiComponents.put("txtSurfTracker_maxFeaturesPerScale", txtSurfTrackerMaxFeaturesPerScale);
        guiComponents.put("txtSurfTracker_extractRadius", txtSurfTrackerExtractRadius);
        guiComponents.put("txtSurfTracker_initialSampleSize", txtSurfTrackerInitialSampleSize);
        guiComponents.put("surfTrackerPanel", surfTrackerPanel);
        guiComponents.put("chkTrackerShowActiveTracks", chkTrackerShowActiveTracks);
        guiComponents.put("chkTrackerShowNewTracks", chkTrackerShowNewTracks);

        // Tracker settings panel

        var trackerSettingsPanel = new JPanel();
        trackerSettingsPanel.add(lblTrackerType);
        trackerSettingsPanel.add(txtTrackerType);
        trackerSettingsPanel.add(kltTrackerPanel);
        trackerSettingsPanel.add(surfTrackerPanel);
        trackerSettingsPanel.add(chkTrackerShowActiveTracks);
        trackerSettingsPanel.add(chkTrackerShowNewTracks);

        trackerSettingsPanel.setOpaque(false);
        trackerSettingsPanel.setBorder(GuiUtils.getRoundedTitledBorder("Tracker",
                GuiConstants.PANEL_BORDER_ACTIVE_COLOR, 10, 10));

        panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblTrackerType, 0, SpringLayout.NORTH, trackerSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblTrackerType, 3, SpringLayout.WEST, trackerSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtTrackerType, -3, SpringLayout.NORTH, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.WEST, txtTrackerType, 3, SpringLayout.EAST, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.EAST, txtTrackerType, -3, SpringLayout.EAST, trackerSettingsPanel);

        // second row (klt tracker panel)
        panelLayout.putConstraint(SpringLayout.NORTH, kltTrackerPanel, 5, SpringLayout.SOUTH, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.WEST, kltTrackerPanel, 3, SpringLayout.WEST, trackerSettingsPanel);
        panelLayout.putConstraint(SpringLayout.EAST, kltTrackerPanel, 0, SpringLayout.EAST, trackerSettingsPanel);

        // second row (surf tracker panel)
        panelLayout.putConstraint(SpringLayout.NORTH, surfTrackerPanel, 5, SpringLayout.SOUTH, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.WEST, surfTrackerPanel, 3, SpringLayout.WEST, trackerSettingsPanel);
        panelLayout.putConstraint(SpringLayout.EAST, surfTrackerPanel, 0, SpringLayout.EAST, trackerSettingsPanel);

        // third row
        panelLayout.putConstraint(SpringLayout.NORTH, chkTrackerShowActiveTracks, 2, SpringLayout.SOUTH, kltTrackerPanel);
        panelLayout.putConstraint(SpringLayout.WEST, chkTrackerShowActiveTracks, 0, SpringLayout.WEST, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.NORTH, chkTrackerShowNewTracks, 0, SpringLayout.NORTH, chkTrackerShowActiveTracks);
        panelLayout.putConstraint(SpringLayout.WEST, chkTrackerShowNewTracks, 3, SpringLayout.EAST, chkTrackerShowActiveTracks);

        panelLayout.putConstraint(SpringLayout.SOUTH, trackerSettingsPanel, 0, SpringLayout.SOUTH, chkTrackerShowActiveTracks);

        trackerSettingsPanel.setLayout(panelLayout);

        return trackerSettingsPanel;
    }

    private @NotNull JPanel createVisualOdometrySettingsPanel() {
        VisualOdometrySettings voSettings = this.settings.core().visualOdometry();
        HashMap<String, Component> guiComponents = this.settings.state().guiComponents();

        // Visual odometry components
        var monoPlaneInfinityPanel = new MonoPlaneInfinityPanel(voSettings);
        var monoPlaneOverheadPanel = new MonoPlaneOverheadPanel(voSettings);
        var fallbackPanel = new VoFallbackPanel();

        // Visual odometry scroll pane
        var voScrollPane = new JScrollPane();
        voScrollPane.setOpaque(false);
        voScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        voScrollPane.setBorder(GuiUtils
                .getRoundedBorder(GuiConstants.PANEL_BORDER_ACTIVE_COLOR, 10, 10));

        // Visual odometry type
        var lblVisualOdometryType = new JLabel("<html><b>VO Type:</b></html>");
        var txtVisualOdometryType = new DisplayValueComboBox<>(
                VisualOdometryType.values(),
                voSettings::type,
                index -> VisualOdometryType.values()[index],
                selection -> this.switchVoPanel(selection.current().value(), voScrollPane, monoPlaneInfinityPanel,
                        monoPlaneOverheadPanel, fallbackPanel)
        );
        txtVisualOdometryType.setSelectedItem(voSettings.type());

        // Fill guiComponents
        guiComponents.put("txtVisualOdometryType", txtVisualOdometryType);
        guiComponents.put("txtMonoPlaneInfinity_thresholdAdd", monoPlaneInfinityPanel.txtThresholdAdd());
        guiComponents.put("txtMonoPlaneInfinity_thresholdRetire", monoPlaneInfinityPanel.txtThresholdRetire());
        guiComponents.put("txtMonoPlaneInfinity_inlierPixelTol", monoPlaneInfinityPanel.txtInlierPixelTol());
        guiComponents.put("txtMonoPlaneInfinity_ransacIterations", monoPlaneInfinityPanel.txtRansacIterations());
        guiComponents.put("monoPlaneInfinityPanel", monoPlaneInfinityPanel);
        guiComponents.put("txtMonoPlaneOverhead_cellSize", monoPlaneOverheadPanel.txtCellSize());
        guiComponents.put("txtMonoPlaneOverhead_maxCellsPerPixel", monoPlaneOverheadPanel.txtMaxCellsPerPixel());
        guiComponents.put("txtMonoPlaneOverhead_mapHeightFraction", monoPlaneOverheadPanel.txtMapHeightFraction());
        guiComponents.put("txtMonoPlaneOverhead_inlierGroundTol", monoPlaneOverheadPanel.txtInlierGroundTol());
        guiComponents.put("txtMonoPlaneOverhead_ransacIteration", monoPlaneOverheadPanel.txtRansacIterations());
        guiComponents.put("txtMonoPlaneOverhead_thresholdRetire", monoPlaneOverheadPanel.txtThresholdRetire());
        guiComponents.put("txtMonoPlaneOverhead_absoluteMinimumTracks", monoPlaneOverheadPanel.txtAbsoluteMinimumTracks());
        guiComponents.put("txtMonoPlaneOverhead_respawnTrackFraction", monoPlaneOverheadPanel.txtRespawnTrackFraction());
        guiComponents.put("txtMonoPlaneOverhead_respawnCoverageFraction", monoPlaneOverheadPanel.txtRespawnCoverageFraction());
        guiComponents.put("monoPlaneOverheadPanel", monoPlaneOverheadPanel);
        guiComponents.put("monoPlaneScrollPane", voScrollPane);

        // Visual odometry settings panel
        var voSettingsPanel = new JPanel();
        voSettingsPanel.add(lblVisualOdometryType);
        voSettingsPanel.add(txtVisualOdometryType);
        voSettingsPanel.add(voScrollPane);

        voSettingsPanel.setOpaque(false);
        voSettingsPanel.setBorder(GuiUtils.getRoundedTitledBorder("Visual Odometry",
                GuiConstants.PANEL_BORDER_ACTIVE_COLOR, 10, 10));

        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblVisualOdometryType, 0, SpringLayout.NORTH, voSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblVisualOdometryType, 3, SpringLayout.WEST, voSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtVisualOdometryType, -3, SpringLayout.NORTH, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.WEST, txtVisualOdometryType, 3, SpringLayout.EAST, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.EAST, txtVisualOdometryType, -3, SpringLayout.EAST, voSettingsPanel);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, voScrollPane, 10, SpringLayout.SOUTH, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.WEST, voScrollPane, 0, SpringLayout.WEST, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.EAST, voScrollPane, 0, SpringLayout.EAST, voSettingsPanel);

        // panel height
        panelLayout.putConstraint(SpringLayout.SOUTH, voSettingsPanel, 0, SpringLayout.SOUTH, voScrollPane);

        voSettingsPanel.setLayout(panelLayout);

        // Shows current active vo panel
        this.switchVoPanel(voSettings.type(), voScrollPane, monoPlaneInfinityPanel, monoPlaneOverheadPanel,
                fallbackPanel);

        return voSettingsPanel;
    }

    private void switchVoPanel(@NotNull VisualOdometryType selectedVoType,
                               JScrollPane voScrollPane,
                               MonoPlaneInfinityPanel monoPlaneInfinityPanel,
                               MonoPlaneOverheadPanel monoPlaneOverheadPanel,
                               VoFallbackPanel fallbackPanel) {
        switch (selectedVoType) {
            case VisualOdometryType.Default, VisualOdometryType.MonoPlaneInfinity -> {
                voScrollPane.setViewportView(monoPlaneInfinityPanel);
                monoPlaneInfinityPanel.setEnabled(!VisualOdometryType.Default.is(selectedVoType));
                for (Component component : monoPlaneInfinityPanel.getComponents()) {
                    component.setEnabled(!VisualOdometryType.Default.is(selectedVoType));
                }
            }
            case VisualOdometryType.MonoPlaneOverhead -> voScrollPane.setViewportView(monoPlaneOverheadPanel);
            default -> voScrollPane.setViewportView(fallbackPanel);
        }
    }

    private @NotNull JPanel createChartSettingsPanel() {
        /* Parameters Managed from Chart Settings Panel */
        ChartSettings chartSettings = this.settings.core().chart();
        ChartSettings defaultChartSettings = chartSettings.getDefault();
        HashMap<String, Component> guiComponents = this.settings.state().guiComponents();

        SpringLayout panelLayout = null; //Layout Object needed for components disposition


        /***********************
         * COMPONENTS CREATION *
         ***********************/


        /**
         *  1. CHART TYPE PART
         *
         *  First and upper part of the Chart Settings Panel.
         *
         */


        // Chart type label
        JLabel lblChartType = new JLabel("<html><b>Chart Type:</b></html>");

        // Chart type ComboBox
        var txtChartType = new DisplayValueComboBox<>(
                ChartType.values(),
                chartSettings::type,
                index -> ChartType.values()[index]
        );
        txtChartType.setSelectedItem(chartSettings.type());

        /**
         *  2. CHART X/Z AND CHART Y PART
         *
         *  Second part of the Chart Settings Panel.
         *
         */


        // Chart X/Z
        var lblChartXZ = new JLabel("<html><b>Chart X/Z</b></html>");

        // Chart X/Z scale
        var lblChartXZScale = new JLabel("<html>Scale: </html>");
        var txtChartXZScale = new DoubleTextField(
                NumberConstraints.NotZero,
                chartSettings::scaleXZ,
                chartSettings::scaleXZ,
                defaultChartSettings.scaleXZ(),
                5,
                JTextField.CENTER
        );

        // Applying loaded Chart X/Z Scale
        ChartScrollPane chartXZPanel = this.settings.state().guiController().chartXZPanel();
        if (chartXZPanel != null) {
            chartXZPanel.settings().chartScale(chartSettings.scaleXZ());
            chartXZPanel.resetSize();
        }

        // Chart XZ apply scale button
        var btnChartXZApplyScale = new JButton("Apply");
        btnChartXZApplyScale.addActionListener(new ChartButtonListener("chartXZApplyScale", this.settings));

        // Chart XZ move to origin button
        var btnChartXZMoveToOrigin = new JButton("Origin");
        btnChartXZMoveToOrigin.addActionListener(new ChartButtonListener("chartXZMoveToOrigin", this.settings));

        // Chart XZ move to last point button
        var btnChartXZMoveToLastPoint = new JButton("Last");
        btnChartXZMoveToLastPoint.addActionListener(new ChartButtonListener("chartXZMoveToLastPoint", this.settings));

        // Chart XZ 3D points CheckBox
        var chkChartXZ3DPoints = new JCheckBox("<html>3D points</html>");
        chkChartXZ3DPoints.addActionListener(new ChartButtonListener("chartXZ3DPoints", this.settings));

        // Chart Y
        var lblChartY = new JLabel("<html><b>Chart Y</b></html>");

        // Chart Y scale
        var lblChartYScale = new JLabel("<html>Scale: </html>");
        var txtChartYScale = new DoubleTextField(
                NumberConstraints.NotZero,
                chartSettings::scaleY,
                chartSettings::scaleY,
                defaultChartSettings.scaleY(),
                5,
                JTextField.CENTER
        );

        // Applying loaded Chart Y Scale
        ChartScrollPane chartYPanel = this.settings.state().guiController().chartYPanel();
        if (chartYPanel != null) {
            chartYPanel.settings().chartScale(chartSettings.scaleY());
            chartYPanel.resetSize();
        }

        // Chart Y apply scale button
        var btnChartYApplyScale = new JButton("Apply");
        btnChartYApplyScale.addActionListener(new ChartButtonListener("chartYApplyScale", this.settings));

        // Chart Y move to origin button
        var btnChartYMoveToOrigin = new JButton("Origin");
        btnChartYMoveToOrigin.addActionListener(new ChartButtonListener("chartYMoveToOrigin", this.settings));

        // Chart Y move to last point button
        var btnChartYMoveToLastPoint = new JButton("Last");
        btnChartYMoveToLastPoint.addActionListener(new ChartButtonListener("chartYMoveToLastPoint", this.settings));


        /**
         *  3. POPULATE GUICOMPONENTS
         *
         *  Adds all the most important (and reused) components to the guiComponents HashMap
         *
         */


        guiComponents.put("txtChartType", txtChartType);
        guiComponents.put("txtChartXZScale", txtChartXZScale);
        guiComponents.put("chkChartXZ3DPoints", chkChartXZ3DPoints);
        guiComponents.put("txtChartYScale", txtChartYScale);


        /**
         *  4. CHART SETTINGS PANEL CREATION
         *
         *  Creation of the Chart Settings Panel.
         *
         */


        //Create compound border: Titled+Empty
        final JPanel chartSettingsPanel = new JPanel();
        chartSettingsPanel.setOpaque(false);
        chartSettingsPanel.setBorder(GuiUtils.getRoundedTitledBorder("Chart",
                GuiConstants.PANEL_BORDER_ACTIVE_COLOR, 10, 10));

        //Adds components to the panel
        chartSettingsPanel.add(lblChartType);
        chartSettingsPanel.add(txtChartType);
        chartSettingsPanel.add(lblChartXZ);
        chartSettingsPanel.add(lblChartXZScale);
        chartSettingsPanel.add(txtChartXZScale);
        chartSettingsPanel.add(btnChartXZApplyScale);
        chartSettingsPanel.add(btnChartXZMoveToOrigin);
        chartSettingsPanel.add(btnChartXZMoveToLastPoint);
        chartSettingsPanel.add(chkChartXZ3DPoints);
        chartSettingsPanel.add(lblChartY);
        chartSettingsPanel.add(lblChartYScale);
        chartSettingsPanel.add(txtChartYScale);
        chartSettingsPanel.add(btnChartYApplyScale);
        chartSettingsPanel.add(btnChartYMoveToOrigin);
        chartSettingsPanel.add(btnChartYMoveToLastPoint);


        /**************************
         * COMPONENTS DISPOSITION *
         *************************/


        /**
         *  5. CHART SETTINGS PANEL DISPOSITION
         *
         *  Disposition of the components inside Chart Settings Panel.
         *
         */


        panelLayout = new SpringLayout();

        //On the first row Chart Type Label/TextField
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartType, 0, SpringLayout.NORTH, chartSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartType, 3, SpringLayout.WEST, chartSettingsPanel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtChartType, -3, SpringLayout.NORTH, lblChartType);
        panelLayout.putConstraint(SpringLayout.WEST, txtChartType, 3, SpringLayout.EAST, lblChartType);
        panelLayout.putConstraint(SpringLayout.EAST, txtChartType, -3, SpringLayout.EAST, chartSettingsPanel);

        //On the second row Chart XZ Label
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartXZ, 8, SpringLayout.SOUTH, lblChartType);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartXZ, 0, SpringLayout.WEST, lblChartType);

        //On the third row Chart XZ Scale Label/TextField, Apply Scale Button, Move to Origin Button,
        //Move to Last Point Button, and 3D Points CheckBox
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartXZScale, 7, SpringLayout.SOUTH, lblChartXZ);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartXZScale, 5, SpringLayout.WEST, lblChartXZ);
        panelLayout.putConstraint(SpringLayout.NORTH, txtChartXZScale, -3, SpringLayout.NORTH, lblChartXZScale);
        panelLayout.putConstraint(SpringLayout.WEST, txtChartXZScale, 3, SpringLayout.EAST, lblChartXZScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZApplyScale, -1, SpringLayout.NORTH, txtChartXZScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartXZApplyScale, 3, SpringLayout.EAST, txtChartXZScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZMoveToOrigin, 0, SpringLayout.NORTH, btnChartXZApplyScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartXZMoveToOrigin, 3, SpringLayout.EAST, btnChartXZApplyScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZMoveToLastPoint, 0, SpringLayout.NORTH, btnChartXZMoveToOrigin);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartXZMoveToLastPoint, 3, SpringLayout.EAST, btnChartXZMoveToOrigin);
        panelLayout.putConstraint(SpringLayout.NORTH, chkChartXZ3DPoints, 0, SpringLayout.NORTH, btnChartXZMoveToLastPoint);
        panelLayout.putConstraint(SpringLayout.WEST, chkChartXZ3DPoints, 3, SpringLayout.EAST, btnChartXZMoveToLastPoint);

        //On the fourth row Chart Y Label
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartY, 8, SpringLayout.SOUTH, lblChartXZScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartY, 0, SpringLayout.WEST, lblChartType);

        //On the fifth row Chart Y Scale Label/TextField, Apply Scale Button, Move to Origin Button
        //and Move to Last Point Button
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartYScale, 7, SpringLayout.SOUTH, lblChartY);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartYScale, 5, SpringLayout.WEST, lblChartY);
        panelLayout.putConstraint(SpringLayout.NORTH, txtChartYScale, -3, SpringLayout.NORTH, lblChartYScale);
        panelLayout.putConstraint(SpringLayout.WEST, txtChartYScale, 3, SpringLayout.EAST, lblChartYScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartYApplyScale, -1, SpringLayout.NORTH, txtChartYScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartYApplyScale, 3, SpringLayout.EAST, txtChartYScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartYMoveToOrigin, 0, SpringLayout.NORTH, btnChartYApplyScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartYMoveToOrigin, 3, SpringLayout.EAST, btnChartYApplyScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartYMoveToLastPoint, 0, SpringLayout.NORTH, btnChartYMoveToOrigin);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartYMoveToLastPoint, 3, SpringLayout.EAST, btnChartYMoveToOrigin);

        //The height of the panel is constrained to the fifth row bottom
        panelLayout.putConstraint(SpringLayout.SOUTH, chartSettingsPanel, 0, SpringLayout.SOUTH, lblChartYScale);

        //Adds the layout to the panel
        chartSettingsPanel.setLayout(panelLayout);

        return chartSettingsPanel;
    }

    private ToolbarPanel createToolbarPanel() {
        // Settings button
        ImageButton btnSettings = new BufferedImageButton(GuiConstants.BTN_SETTINGS);

        // Settings popup menu
        JMenuItem mnuLoadSettings = new JMenuItem(GuiConstants.MNU_LOAD_SETTINGS_TEXT);
        mnuLoadSettings.addActionListener(new MainButtonListener("loadSettings", this.settings, this.core));
        JMenuItem mnuSaveSettings = new JMenuItem(GuiConstants.MNU_SAVE_SETTINGS_TEXT);
        mnuSaveSettings.addActionListener(new MainButtonListener("saveSettings", this.settings, this.core));
        JMenuItem mnuResetSettings = new JMenuItem(GuiConstants.MNU_RESET_SETTINGS_TEXT);
        mnuResetSettings.addActionListener(new MainButtonListener("resetSettings", this.settings, this.core));
        JMenuItem mnuSwitchSettings = new JMenuItem(GuiConstants.MNU_SWITCH_SETTINGS_TEXT);
        mnuSwitchSettings.addActionListener(new MainButtonListener("switchSettings", this.settings, this.core));

        JPopupMenu popupSettings = new JPopupMenu();
        popupSettings.add(mnuLoadSettings);
        popupSettings.add(mnuSaveSettings);
        popupSettings.add(mnuResetSettings);
        popupSettings.add(mnuSwitchSettings);

        // Force menu size calculation
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        popupSettings.show(null, (int) screenSize.getWidth(), (int) screenSize.getHeight());
        popupSettings.setVisible(false);
        popupSettings.setPreferredSize(new Dimension(popupSettings.getWidth(), popupSettings.getHeight()));

        // Popup display listener
        btnSettings.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getComponent().contains(e.getPoint())) {
                    popupSettings.show(
                            e.getComponent(),
                            e.getComponent().getWidth() - (int) popupSettings.getPreferredSize().getWidth(),
                            -((int) popupSettings.getPreferredSize().getHeight() + 2)
                    );
                }
            }
        });

        // Start visual odometry button
        ImageButton btnStartVO =
                new BufferedImageButton(GuiConstants.BTN_START, GuiConstants.BTN_START_DISABLED);
        btnStartVO.setToolTipText(GuiConstants.BTN_START_TOOLTIP);
        btnStartVO.addActionListener(new MainButtonListener("startVisualOdometry", this.settings, this.core));

        // Pause visual odometry button (enabled on process start)
        ImageButton btnPauseVO =
                new BufferedImageButton(GuiConstants.BTN_PAUSE, GuiConstants.BTN_PAUSE_DISABLED,
                        GuiConstants.BTN_PAUSE_DISABLED);
        btnPauseVO.setToolTipText(GuiConstants.BTN_PAUSE_TOOLTIP);
        btnPauseVO.setEnabled(false);
        btnPauseVO.addActionListener(new MainButtonListener("pauseVisualOdometry", this.settings, this.core));

        // Stop visual odometry button (enabled on process start)
        ImageButton btnStopVO =
                new BufferedImageButton(GuiConstants.BTN_STOP, GuiConstants.BTN_STOP_DISABLED);
        btnStopVO.setToolTipText(GuiConstants.BTN_STOP_TOOLTIP);
        btnStopVO.setEnabled(false);
        btnStopVO.addActionListener(new MainButtonListener("stopVisualOdometry", this.settings, this.core));

        // Reset visual odometry button (enabled on process start)
        ImageButton btnResetVO = new BufferedImageButton(GuiConstants.BTN_RESET);
        btnResetVO.setToolTipText(GuiConstants.BTN_RESET_TOOLTIP);
        btnResetVO.setEnabled(false);
        btnResetVO.addActionListener(new MainButtonListener("resetVisualOdometry", this.settings, this.core));

        // Clear visual odometry button (enabled on process start)
        ImageButton btnClearVO = new BufferedImageButton(GuiConstants.BTN_CLEAR);
        btnClearVO.setToolTipText(GuiConstants.BTN_CLEAR_TOOLTIP);
        btnClearVO.setEnabled(false);
        btnClearVO.addActionListener(new MainButtonListener("clearVisualOdometry", this.settings, this.core));

        // Timed processing button (device only)
        ImageButton btnTimedProcessingVO = new BufferedImageButton(
                GuiConstants.BTN_TIMED_PROCESSING_VO, GuiConstants.BTN_TIMED_PROCESSING_VO,
                GuiConstants.BTN_EMPTY
        );
        btnTimedProcessingVO.setAlternativeOpacity(1.0f);
        btnTimedProcessingVO.setToolTipText(GuiConstants.BTN_TIMED_PROCESSING_VO_TOOLTIP);
        btnTimedProcessingVO.setEnabled(false);
        btnTimedProcessingVO.addActionListener(
                new MainButtonListener("timedStopVisualOdometry", this.settings, this.core));

        return ToolbarPanel.builder()
                .btnSettings(btnSettings)
                .btnStartVO(btnStartVO)
                .btnPauseVO(btnPauseVO)
                .btnResetVO(btnResetVO)
                .btnStopVO(btnStopVO)
                .btnClearVO(btnClearVO)
                .btnTimedProcessingVO(btnTimedProcessingVO)
                .build();
    }

    private void layoutMainFrame(@NotNull JFrame mainFrame, JScrollPane mainScrollPane, @NotNull ToolbarPanel toolbar) {
        SpringLayout panelLayout = new SpringLayout();
        Container contentPane = mainFrame.getContentPane();

        // Main scroll pane
        panelLayout.putConstraint(SpringLayout.NORTH, mainScrollPane, 0, SpringLayout.NORTH, contentPane);
        panelLayout.putConstraint(SpringLayout.WEST, mainScrollPane, 0, SpringLayout.WEST, contentPane);
        panelLayout.putConstraint(SpringLayout.EAST, mainScrollPane, 0, SpringLayout.EAST, contentPane);
        panelLayout.putConstraint(
                SpringLayout.SOUTH,
                mainScrollPane,
                -5,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );

        // Toolbar buttons
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnStartVO(),
                -40,
                SpringLayout.SOUTH,
                contentPane
        );
        panelLayout.putConstraint(
                SpringLayout.WEST,
                toolbar.btnStartVO(),
                10,
                SpringLayout.WEST,
                contentPane
        );
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnPauseVO(),
                0,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.WEST,
                toolbar.btnPauseVO(),
                5,
                SpringLayout.EAST,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnStopVO(),
                0,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.WEST,
                toolbar.btnStopVO(),
                5,
                SpringLayout.EAST,
                toolbar.btnPauseVO()
        );
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnResetVO(),
                0,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.WEST,
                toolbar.btnResetVO(),
                5,
                SpringLayout.EAST,
                toolbar.btnStopVO()
        );
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnClearVO(),
                0,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.WEST,
                toolbar.btnClearVO(),
                5,
                SpringLayout.EAST,
                toolbar.btnResetVO()
        );
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnTimedProcessingVO(),
                0,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.WEST,
                toolbar.btnTimedProcessingVO(),
                5,
                SpringLayout.EAST,
                toolbar.btnClearVO()
        );

        // Settings button
        panelLayout.putConstraint(
                SpringLayout.NORTH,
                toolbar.btnSettings(),
                0,
                SpringLayout.NORTH,
                toolbar.btnStartVO()
        );
        panelLayout.putConstraint(
                SpringLayout.EAST,
                toolbar.btnSettings(),
                -10,
                SpringLayout.EAST,
                contentPane
        );

        // Add layout to main frame
        mainFrame.setLayout(panelLayout);

        // Move main frame to the right of chart frame
        // (X location set to frame default width + 55 and Y location to the screen top)
        mainFrame.setLocation(getDefaultFrameDimension().width + 55, 0);
        // Resize frame to frame default width/height
        mainFrame.setPreferredSize(getDefaultFrameDimension());
        mainFrame.pack();
    }

    private void setBaseUI() {
        // Configure app UI (title, icon, about)
        setAppBaseUI();

        // Configure UI theme
        setThemeUI();
    }


    private void setAppBaseUI() {
        // Set app title
        if (OSUtils.isMac()) {
            System.setProperty(GuiConstants.MACOS_APP_TITLE_PROPERTY, GuiConstants.APP_TITLE);
        }

        // Set app icon
        Image appIcon = ImageUtils.getResourceImage(GuiConstants.APP_ICON);
        if (appIcon != null) {
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(appIcon);
        }

        // Set app about window
        if (Desktop.isDesktopSupported()) {
            JFrame aboutFrame = this.createAboutFrame();
            Desktop.getDesktop().setAboutHandler(e -> {
                resetAboutFrame(aboutFrame, true);
            });
        }
    }

    private @NotNull JFrame createAboutFrame() {
        JLabel appImage = new JLabel();
        Image appIcon = ImageUtils.getResourceImage(GuiConstants.APP_ICON, 150, 1f);
        if (appIcon != null) {
            appImage.setIcon(new ImageIcon(appIcon));
        }

        JLabel appInfo = new JLabel(String.format(GuiConstants.APP_TITLE_PATTERN, this.appVersion));

        JLabel appDescription = new JLabel(GuiConstants.APP_DESCRIPTION);
        GuiUtils.setFont(appDescription, 10);

        JLabel javaInfo = new JLabel(String.format(GuiConstants.JAVA_INFO,
                System.getProperty(GuiConstants.JAVA_VERSION)));
        javaInfo.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        GuiUtils.setFont(javaInfo, 10, Font.ITALIC);

        JLabel boofCvInfo = new JLabel(String.format(GuiConstants.BOOFCV_INFO,
                BoofVersion.VERSION,
                BoofVersion.BUILD_DATE
        ));
        GuiUtils.setFont(boofCvInfo, 10, Font.ITALIC);

        JLabel licenseInfo = new JLabel(GuiConstants.LICENSE_INFO);
        licenseInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        GuiUtils.setFont(licenseInfo, 10, Font.ITALIC);

        JLabel authorInfo = new JLabel(String.format(GuiConstants.AUTHOR_INFO, LocalDateTime.now().getYear()));

        JPanel aboutPanel = new JPanel(new GridBagLayout());
        aboutPanel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = c.gridx = 0;
        aboutPanel.add(appImage, c);
        c.gridy = 1;
        aboutPanel.add(appInfo, c);
        c.gridy = 2;
        aboutPanel.add(appDescription, c);
        c.gridy = 3;
        aboutPanel.add(javaInfo, c);
        c.gridy = 4;
        aboutPanel.add(boofCvInfo, c);
        c.gridy = 5;
        aboutPanel.add(licenseInfo, c);
        c.gridy = 6;
        aboutPanel.add(authorInfo, c);

        JFrame aboutFrame = new JFrame(GuiConstants.ABOUT_TITLE);
        aboutFrame.getContentPane().add(aboutPanel);
        resetAboutFrame(aboutFrame, false);
        aboutFrame.pack();

        return aboutFrame;
    }

    private void resetAboutFrame(JFrame aboutFrame, boolean show) {
        resizeAndCenter(aboutFrame, 350, false);
        if (show) {
            aboutFrame.setVisible(true);
        }
    }

    private void setThemeUI() {
        // Enable native lookAndFeel
        try {
            isSystemLookAndFeelEnabled = true;
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ex) {
            isSystemLookAndFeelEnabled = false;
        }

        // Set title bar auto light/dark
        if (OSUtils.isMac()) {
            System.setProperty(GuiConstants.MACOS_APP_APPEARANCE_PROPERTY, GuiConstants.MACOS_APP_APPEARANCE_VALUE);
        }

        // Enable system anti-aliasing
        GuiUtils.setSystemAntiAliasing();

        // Global theming
        GuiUtils.setUIPropertyEndsWith(GuiConstants.ALL_BACKGROUNDS_PROP, GuiConstants.APP_BACKGROUND_COLOR);
        GuiUtils.setUIProperty(GuiConstants.COMBO_BOX_BACKGROUND_PROP, GuiConstants.COMBO_BOX_BACKGROUND_COLOR);
        GuiUtils.setUIProperty(GuiConstants.LIST_BACKGROUND_PROP, GuiConstants.LIST_BACKGROUND_COLOR);
        GuiUtils.setUIProperty(GuiConstants.LIST_SELECTION_BACKGROUND_PROP,
                GuiConstants.LIST_SELECTION_BACKGROUND_COLOR);
        GuiUtils.setUIProperty(GuiConstants.TEXT_FIELD_BACKGROUND_PROP, GuiConstants.TEXT_FIELD_BACKGROUND_COLOR);
        GuiUtils.setUIProperty(GuiConstants.TEXT_FIELD_BORDER_PROP,
                new RoundedCornerBorder(
                        GuiConstants.TEXT_FIELD_BORDER_BASE_COLOR,
                        GuiConstants.TEXT_FIELD_BORDER_HIGHLIGHT_COLOR
                ));
    }

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

    @SuppressWarnings("unchecked")
    public static Boolean refreshGuiFromParameters(Settings settings) {

        try {
            EventListener listener;//Used for some components Listeners, to save, disable and re-enable
            //them (to prevent side-effects when loading)


            /**Extracts all components from guiComponents and Resets them to new Parameters value*/


            /**Input Settings Panel Reloading**/

            //Calibration ComboBox
            JComboBox txtCalibration = (JComboBox) settings.state().guiComponents().get("txtCalibration");
            txtCalibration.setModel(new DefaultComboBoxModel<>(settings.core().input().calibration().paths()));
            txtCalibration.setSelectedItem(settings.core().input().calibration().path());

            //Video Source RadioButton
            JRadioButton optVideoSource = (JRadioButton) settings.state().guiComponents().get("optVideoSource");
            optVideoSource.setSelected(SourceType.Video.is(settings.core().input().source()));
            if (optVideoSource.isSelected()) { //Trigger Listener
                for (ActionListener actionListener : optVideoSource.getActionListeners()) {
                    actionListener.actionPerformed(new ActionEvent(optVideoSource, ActionEvent.ACTION_PERFORMED, null));
                }
            }

            //Video Source ComboBox
            JComboBox txtVideoSource = (JComboBox) settings.state().guiComponents().get("txtVideoSource");
            txtVideoSource.setModel(new DefaultComboBoxModel<>(settings.core().input().video().paths()));
            txtVideoSource.setSelectedItem(settings.core().input().video().path());

            //Device Source RadioButton
            JRadioButton optDeviceSource = (JRadioButton) settings.state().guiComponents().get("optDeviceSource");
            optDeviceSource.setSelected(SourceType.Device.is(settings.core().input().source()));
            if (optDeviceSource.isSelected()) { //Trigger Listener
                for (ActionListener actionListener : optDeviceSource.getActionListeners()) {
                    actionListener.actionPerformed(new ActionEvent(optDeviceSource, ActionEvent.ACTION_PERFORMED, null));
                }
            }

            // Device Type ComboBox
            var txtDeviceType = (DisplayValueComboBox<DeviceType>)
                    settings.state().guiComponents().get("txtDeviceType");
            txtDeviceType.setSelectedItem(settings.core().input().device().type());

            //Device Path ComboBox
            JComboBox<DevicePath> txtDevicePath = (JComboBox) settings.state().guiComponents().get("txtDevicePath");
            txtDevicePath.setModel(new DefaultComboBoxModel<>(settings.core().input().device().paths()));
            txtDevicePath.setSelectedItem(settings.core().input().device().path());

            //Device Resolution combo box
            JComboBox<Resolution> txtDeviceResolution = (JComboBox) settings.state().guiComponents().get("txtDeviceResolution");
            txtDeviceResolution.setSelectedItem(settings.core().input().device().resolution());


            //Device Sustain Framerate CheckBox
            JCheckBox chkDeviceSustainFramerate = (JCheckBox) settings.state().guiComponents().get("chkDeviceSustainFramerate");
            chkDeviceSustainFramerate.setSelected(settings.core().input().device().v4l4j().sustainFramerate());
            for (ActionListener actionListener : chkDeviceSustainFramerate.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkDeviceSustainFramerate, ActionEvent.ACTION_PERFORMED, null));
            }

            //Device Timeout Image IO CheckBox
            JCheckBox chkDeviceTimeoutImageIO = (JCheckBox) settings.state().guiComponents().get("chkDeviceTimeoutImageIO");
            chkDeviceTimeoutImageIO.setSelected(settings.core().input().device().v4l4j().timeoutImageIO());
            for (ActionListener actionListener : chkDeviceTimeoutImageIO.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkDeviceTimeoutImageIO, ActionEvent.ACTION_PERFORMED, null));
            }

            //Device Keep Format CheckBox
            JCheckBox chkDeviceKeepFormat = (JCheckBox) settings.state().guiComponents().get("chkDeviceKeepFormat");
            chkDeviceKeepFormat.setSelected(settings.core().input().device().v4l4j().keepFormat());
            for (ActionListener actionListener : chkDeviceKeepFormat.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkDeviceKeepFormat, ActionEvent.ACTION_PERFORMED, null));
            }

            //Device Full Resolution Preview CheckBox
            JCheckBox chkFullResolutionPreview = (JCheckBox) settings.state().guiComponents().get("chkFullResolutionPreview");
            chkFullResolutionPreview.setSelected(settings.core().input().fullResolutionPreview());
            for (ActionListener actionListener : chkFullResolutionPreview.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkFullResolutionPreview, ActionEvent.ACTION_PERFORMED, null));
            }

            //Device Input Preview Enabled CheckBox
            JCheckBox chkInputPreviewEnabled = (JCheckBox) settings.state().guiComponents().get("chkInputPreviewEnabled");
            chkInputPreviewEnabled.setSelected(settings.core().input().inputPreview());
            for (ActionListener actionListener : chkInputPreviewEnabled.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkInputPreviewEnabled, ActionEvent.ACTION_PERFORMED, null));
            }


            /**Internal Image Settings Panel Reloading**/


            //Image Type ComboBox
            var txtImageType = (DisplayValueComboBox<ImageTypeDescriptor>) settings.state().guiComponents().get("txtImageType");
            txtImageType.setSelectedItem(settings.core().image().descriptor());

            // Image resize CheckBox
            JCheckBox chkImageResize = (JCheckBox) settings.state().guiComponents().get("chkImageResize");
            chkImageResize.setSelected(settings.core().image().resize());
            for (ActionListener actionListener : chkImageResize.getActionListeners()) {
                actionListener.actionPerformed(new ActionEvent(chkImageResize, ActionEvent.ACTION_PERFORMED, null));
            }

            //Image Resize Width TextField
            JComboBox<Resolution> txtImageResize = (JComboBox) settings.state().guiComponents().get("txtImageResize");
            txtImageResize.setSelectedItem(settings.core().image().resolution());

            // Internal image preview CheckBox
            JCheckBox chkInternalImagePreview = (JCheckBox) settings.state().guiComponents().get("chkInternalImagePreview");
            chkInternalImagePreview.setSelected(settings.core().image().internalImagePreview());
            for (ActionListener actionListener : chkInternalImagePreview.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkInternalImagePreview, ActionEvent.ACTION_PERFORMED, null));
            }

            // Frame skip enabled CheckBox
            JCheckBox chkFrameSkipEnabled = (JCheckBox) settings.state().guiComponents().get("chkFrameSkipEnabled");
            chkFrameSkipEnabled.setSelected(settings.core().image().frameSkipEnabled());
            for (ActionListener actionListener : chkFrameSkipEnabled.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkFrameSkipEnabled, ActionEvent.ACTION_PERFORMED, null));
            }

            // Frame skip value
            var txtFrameSkipValue = (IntegerTextField) settings.state().guiComponents().get("txtFrameSkipValue");
            txtFrameSkipValue.updateModel(settings.core().image().frameSkipValue());

            // Tracker settings

            // Tracker type ComboBox
            JComboBox txtTrackerType = (JComboBox) settings.state().guiComponents().get("txtTrackerType");
            listener = null;
            for (ActionListener actionListener : txtTrackerType.getActionListeners()) {
                if (actionListener instanceof TrackerTypeChangeListener) listener = actionListener;
            }
            txtTrackerType.removeActionListener((ActionListener) listener); //Disable Listener (not needed when only changing model, but safer)
            //Reloading Contents
            txtTrackerType.setModel(new DefaultComboBoxModel<>(
                    settings.core().tracker().getTrackerTypeNames().values().toArray(new String[]{})));
            txtTrackerType.addActionListener((ActionListener) listener); //Enable Listener
            txtTrackerType.setSelectedItem( //Select item triggering Listener (for GUI Changes)
                    settings.core().tracker().getTrackerTypeNames().get(
                            settings.core().tracker().getTrackerType()));

            //KLT Tracker

            // templateRadius
            var txtKltTrackerTemplateRadius = (IntegerTextField) settings.state().guiComponents()
                    .get("txtKltTracker_templateRadius");
            txtKltTrackerTemplateRadius
                    .updateModel(settings.core().tracker().getKltTracker_templateRadius());

            // pyramidLevels
            var txtKltTrackerPyramidLevels = (IntegerTextField) settings.state().guiComponents()
                    .get("txtKltTracker_pyramidLevels");
            txtKltTrackerPyramidLevels.updateModel(settings.core().tracker().getKltTracker_pyramidLevels());

            // maxFeatures
            var txtKltTrackerMaxFeatures = (IntegerTextField) settings.state().guiComponents()
                    .get("txtKltTracker_maxFeatures");
            txtKltTrackerMaxFeatures.updateModel(settings.core().tracker().getKltTracker_maxFeatures());

            // radius
            var txtKltTrackerRadius = (IntegerTextField) settings.state().guiComponents()
                    .get("txtKltTracker_radius");
            txtKltTrackerRadius.updateModel(settings.core().tracker().getKltTracker_radius());

            // threshold
            var txtKltTrackerThreshold = (FloatTextField) settings.state().guiComponents()
                    .get("txtKltTracker_threshold");
            txtKltTrackerThreshold.updateModel(settings.core().tracker().getKltTracker_threshold());

            // SURF Tracker

            // maxFeaturesPerScale
            var txtSurfTrackerMaxFeaturesPerScale = (IntegerTextField) settings.state().guiComponents()
                    .get("txtSurfTracker_maxFeaturesPerScale");
            txtSurfTrackerMaxFeaturesPerScale
                    .updateModel(settings.core().tracker().getSurfTracker_maxFeaturesPerScale());

            // extractRadius
            var txtSurfTrackerExtractRadius = (IntegerTextField) settings.state().guiComponents()
                    .get("txtSurfTracker_extractRadius");
            txtSurfTrackerExtractRadius.updateModel(settings.core().tracker().getSurfTracker_extractRadius());

            // initialSampleSize
            var txtSurfTrackerInitialSampleSize = (IntegerTextField) settings.state().guiComponents()
                    .get("txtSurfTracker_initialSampleSize");
            txtSurfTrackerInitialSampleSize.updateModel(settings.core().tracker().getSurfTracker_initialSampleSize());

            // Tracker options

            // Show active tracks CheckBox
            var chkTrackerShowActiveTracks = (JCheckBox) settings.state().guiComponents().get("chkTrackerShowActiveTracks");
            chkTrackerShowActiveTracks.setSelected(settings.core().tracker().isTrackerShowActiveTracks());
            for (ActionListener actionListener : chkTrackerShowActiveTracks.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkTrackerShowActiveTracks, ActionEvent.ACTION_PERFORMED, null));
            }

            // Show new tracks CheckBox
            var chkTrackerShowNewTracks = (JCheckBox) settings.state().guiComponents().get("chkTrackerShowNewTracks");
            chkTrackerShowNewTracks.setSelected(settings.core().tracker().isTrackerShowNewTracks());
            for (ActionListener actionListener : chkTrackerShowNewTracks.getActionListeners()) { //Trigger Listener
                actionListener.actionPerformed(new ActionEvent(chkTrackerShowNewTracks, ActionEvent.ACTION_PERFORMED, null));
            }


            // Visual odometry settings

            // Visual odometry type ComboBox
            var txtVisualOdometryType = (DisplayValueComboBox<VisualOdometryType>) settings.state().guiComponents()
                    .get("txtVisualOdometryType");
            txtVisualOdometryType.setSelectedItem(settings.core().visualOdometry().type());

            // MonoPlaneInfinity

            // thresholdAdd
            var txtMonoPlaneInfinityThresholdAdd = (IntegerTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneInfinity_thresholdAdd");
            txtMonoPlaneInfinityThresholdAdd
                    .updateModel(settings.core().visualOdometry().monoPlaneInfinity().thresholdAdd());

            // thresholdRetire
            var txtMonoPlaneInfinityThresholdRetire = (IntegerTextField) settings.state()
                    .guiComponents().get("txtMonoPlaneInfinity_thresholdRetire");
            txtMonoPlaneInfinityThresholdRetire
                    .updateModel(settings.core().visualOdometry().monoPlaneInfinity().thresholdRetire());

            // inlierPixelTol
            var txtMonoPlaneInfinityInlierPixelTol = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneInfinity_inlierPixelTol");
            txtMonoPlaneInfinityInlierPixelTol
                    .updateModel(settings.core().visualOdometry().monoPlaneInfinity().inlierPixelTol());

            // ransacIterations
            var txtMonoPlaneInfinityRansacIterations = (IntegerTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneInfinity_ransacIterations");
            txtMonoPlaneInfinityRansacIterations
                    .updateModel(settings.core().visualOdometry().monoPlaneInfinity().ransacIterations());


            // MonoPlaneOverhead

            // cellSize
            var txtMonoPlaneOverheadCellSize = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_cellSize");
            txtMonoPlaneOverheadCellSize
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().cellSize());

            // maxCellsPerPixel
            var txtMonoPlaneOverheadMaxCellsPerPixel = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_maxCellsPerPixel");
            txtMonoPlaneOverheadMaxCellsPerPixel
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().maxCellsPerPixel());

            // mapHeightFraction
            var txtMonoPlaneOverheadMapHeightFraction = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_mapHeightFraction");
            txtMonoPlaneOverheadMapHeightFraction
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().mapHeightFraction());

            // inlierGroundTol
            var txtMonoPlaneOverheadInlierGroundTol = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_inlierGroundTol");
            txtMonoPlaneOverheadInlierGroundTol
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().inlierGroundTol());

            // ransacIteration
            var txtMonoPlaneOverheadRansacIteration = (IntegerTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_ransacIteration");
            txtMonoPlaneOverheadRansacIteration
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().ransacIterations());

            // thresholdRetire
            var txtMonoPlaneOverheadThresholdRetire = (IntegerTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_thresholdRetire");
            txtMonoPlaneOverheadThresholdRetire
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().thresholdRetire());

            // absoluteMinimumTracks
            var txtMonoPlaneOverheadAbsoluteMinimumTracks = (IntegerTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_absoluteMinimumTracks");
            txtMonoPlaneOverheadAbsoluteMinimumTracks
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().absoluteMinimumTracks());

            // respawnTrackFraction
            var txtMonoPlaneOverheadRespawnTrackFraction = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_respawnTrackFraction");
            txtMonoPlaneOverheadRespawnTrackFraction
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().respawnTrackFraction());

            // respawnCoverageFraction
            var txtMonoPlaneOverheadRespawnCoverageFraction = (DoubleTextField) settings.state().guiComponents()
                    .get("txtMonoPlaneOverhead_respawnCoverageFraction");
            txtMonoPlaneOverheadRespawnCoverageFraction
                    .updateModel(settings.core().visualOdometry().monoPlaneOverhead().respawnCoverageFraction());


            /**Chart Settings Panel Reloading**/


            // Chart type ComboBox
            var txtChartType = (DisplayValueComboBox<ChartType>) settings.state().guiComponents().get("txtChartType");
            txtChartType.setSelectedItem(settings.core().chart().type());

            // Chart XZ scale TextField
            var txtChartXZScale = (DoubleTextField) settings.state().guiComponents().get("txtChartXZScale");
            txtChartXZScale.updateModel(settings.core().chart().scaleXZ());
            // Applying loaded Chart XZ Scale
            ChartScrollPane chartXZPanel = settings.state().guiController().chartXZPanel();
            chartXZPanel.settings().chartScale(settings.core().chart().scaleXZ());
            chartXZPanel.resetSize();

            // Chart Y Scale TextField
            var txtChartYScale = (DoubleTextField) settings.state().guiComponents().get("txtChartYScale");
            txtChartYScale.updateModel(settings.core().chart().scaleY());
            // Applying loaded Chart Y Scale
            ChartScrollPane chartYPanel = settings.state().guiController().chartYPanel();
            chartYPanel.settings().chartScale(settings.core().chart().scaleY());
            chartYPanel.resetSize();

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private final class MainButtonListener extends MouseAdapter implements ActionListener {
        //Basic Parameters
        private final String function;
        private final Settings settings;
        private final Core core;

        //Parameters for MouseListener mode only (single/double click management)
        private MouseEvent mouseEvent;
        private Timer timer;
        private int clickInterval;

        //Parameters for Load/Save Buttons only
        private static String saveFormat = "XML";//Can be XML or Serialized (Object Output)

        //Declaration of components needed by the single-click functions
        private ImageButton btnStartVisualOdometry;
        private ImageButton btnPauseVisualOdometry;
        private ImageButton btnResetVisualOdometry;
        private ImageButton btnStopVisualOdometry;
        private ImageButton btnClearVisualOdometry;
        private ImageButton btnTimedVO;
        private JTextField txtTimedStopVisualOdometry;
        private ChartScrollPane chartXZPanel;
        private ChartScrollPane chartYPanel;
        private InfoScrollPane chartInfoPanel;
        private JFrame mainFrame;

        public MainButtonListener(String function, Settings settings, Core core) {
            this.function = function;
            this.settings = settings;
            this.core = core;
        }


        public void mouseClicked(MouseEvent evt) { //Triggers when this class is used as MouseListener
            //(Manages Button single-click/double-click)

            //If the class is used as MouseListener and clickInterval or Timer aren't initialized
            if (timer == null || clickInterval == 0) { //Initilizes them to distinguish between click and doubleclick
                clickInterval = 200; //(Integer)Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
                timer = new Timer(clickInterval, this);
            }

            if (evt.getClickCount() > 2) return; //For more than 2 clicks exits

            mouseEvent = evt;
            if (!timer.isRunning()) { //If timer isn't started starts it
                //and wait 200 ms for another click,
                //if there isn't a second click the timer
                //triggers the actionListener for single click management
                timer.restart();
            } else {                    //If timer is still running we have catched a second click,
                timer.stop();        //so the timer is stopped and doubleClick is managed
                doubleClick(mouseEvent);
            }
        }


        @Override
        public void actionPerformed(ActionEvent evt) { //Triggered from this class (if is used as ActionListener)
            //or from the timer (if is used as MouseListener)
            //(Manages Main Frame buttons single clicks)

            if (timer != null)
                timer.stop(); //If the class is used as MouseListener stops the timer that was waiting for dblClick
            singleClick(mouseEvent == null ? evt : mouseEvent); //Launches singleClick routine with the correct event (mouse or action)
            //depending on how this class is used (MouseListener or ActionListener)
        }

        @SuppressWarnings("unused")
        public void singleClick(AWTEvent evt) {

            //Determines the correct Event Type (Depending if this class is used as Action or Mouse Listener)
            ActionEvent aevt = (evt instanceof ActionEvent) ? (ActionEvent) evt : null;
            MouseEvent mevt = (evt instanceof MouseEvent) ? (MouseEvent) evt : null;

            String function = aevt != null && !aevt.getActionCommand().isEmpty() ? aevt.getActionCommand() : this.function;

            //Extracts components needed by the single-click functions only if they haven't been already extracted
            //(does this at Runtime, here in the singleClick trigger, because all the guiComponents are surely loaded)
            if (btnStartVisualOdometry == null || btnPauseVisualOdometry == null || btnResetVisualOdometry == null
                    || btnStopVisualOdometry == null || btnClearVisualOdometry == null || btnTimedVO == null
                    || txtTimedStopVisualOdometry == null || chartXZPanel == null || chartYPanel == null || chartInfoPanel == null) {

                btnStartVisualOdometry = (ImageButton) this.settings.state().guiComponents().get("btnStartVO");
                btnPauseVisualOdometry = (ImageButton) this.settings.state().guiComponents().get("btnPauseVO");
                btnResetVisualOdometry = (ImageButton) this.settings.state().guiComponents().get("btnResetVO");
                btnStopVisualOdometry = (ImageButton) this.settings.state().guiComponents().get("btnStopVO");
                btnClearVisualOdometry = (ImageButton) this.settings.state().guiComponents().get("btnClearVO");
                btnTimedVO = (ImageButton) this.settings.state().guiComponents().get("btnTimedProcessingVO");
                txtTimedStopVisualOdometry = (JTextField) this.settings.state().guiComponents().get("txtTimedStopVisualOdometry");
                chartXZPanel = this.settings.state().guiController().chartXZPanel();
                chartYPanel = this.settings.state().guiController().chartYPanel();
                chartInfoPanel = this.settings.state().guiController().infoPanel();
                mainFrame = (JFrame) this.settings.state().guiComponents().get("mainFrame");
            }

            switch (function) {    //Depending on function value associated to the button acts differently:
                case "loadSettings": //On click on Load Settings
                    switch (saveFormat) {
                        case "XML":
                            try {
                                //Update Parameters reference into the passed Core to the new Parameters
                                this.settings.loadFromXml();
                                boolean loadSuccess = refreshGuiFromParameters(this.settings);
                                //Updates Status Label content
                                if (loadSuccess)
                                    chartInfoPanel.setAppStatus(AppStatus.XMLSettingsLoaded);
                                else throw new Exception();

                            } catch (Exception exc) {
                                if (!new File("settings.xml").exists()) {
                                    chartInfoPanel.setAppStatus(AppStatus.XMLSettingsNotFound);
                                } else {
                                    chartInfoPanel.setAppStatus(AppStatus.XMLSettingsLoadError);
                                }
                            }
                            break;
                        case "Serialized":
                            try {
                                //Update Parameters reference into the passed Core to the new Parameters
                                this.settings.loadFromDat();
                                boolean loadSuccess = refreshGuiFromParameters(this.settings);
                                //Updates Status Label content
                                if (loadSuccess)
                                    chartInfoPanel.setAppStatus(AppStatus.DATSettingsLoaded);
                                else throw new Exception();
                            } catch (Exception exc) {
                                if (!new File("settings.dat").exists()) {
                                    chartInfoPanel.setAppStatus(AppStatus.DATSettingsNotFound);
                                } else {
                                    chartInfoPanel.setAppStatus(AppStatus.DATSettingsLoadError);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "saveSettings": //On single-click on Save Settings
                    switch (saveFormat) {
                        case "XML":
                            try {
                                ArrayList<Object> parametersToWrite = new ArrayList<>();
                                parametersToWrite.add(this.settings.core().input());
                                parametersToWrite.add(this.settings.core().image());
                                parametersToWrite.add(this.settings.core().tracker());
                                parametersToWrite.add(this.settings.core().visualOdometry());
                                parametersToWrite.add(this.settings.core().chart());

                                XStream xstream = new XStream();
                                String xmlOutput = xstream.toXML(parametersToWrite);
                                byte[] contentInBytes = xmlOutput.getBytes();

                                FileOutputStream fileOutputStream = new FileOutputStream("settings.xml");
                                fileOutputStream.write(contentInBytes);
                                fileOutputStream.flush();
                                fileOutputStream.close();

                                //Updates Status Label content
                                chartInfoPanel.setAppStatus(AppStatus.XMLSettingsSaved);
                            } catch (IOException exc) {
                                chartInfoPanel.setAppStatus(AppStatus.XMLSettingsSaveError);
                                exc.printStackTrace();
                            }
                            break;
                        case "Serialized":
                            try {
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                                        new FileOutputStream("settings.dat"));
                                objectOutputStream.writeObject(this.settings.core().input());
                                objectOutputStream.writeObject(this.settings.core().image());
                                objectOutputStream.writeObject(this.settings.core().tracker());
                                objectOutputStream.writeObject(this.settings.core().visualOdometry());
                                objectOutputStream.writeObject(this.settings.core().chart());
                                objectOutputStream.flush();
                                objectOutputStream.close();

                                //Updates Status Label content
                                chartInfoPanel.setAppStatus(AppStatus.DATSettingsSaved);
                            } catch (Exception exc) {
                                chartInfoPanel.setAppStatus(AppStatus.DATSettingsSaveError);
                                exc.printStackTrace();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "resetSettings": //On click on Reset Settings
                    //Resets parameters to Default
                    this.settings.loadDefaults();
                    boolean resetSuccess = refreshGuiFromParameters(settings);
                    //Updates Status Label content
                    if (resetSuccess) {
                        chartInfoPanel.setAppStatus(AppStatus.SettingsReset);
                    } else {
                        chartInfoPanel.setAppStatus(AppStatus.SettingsResetError);
                    }
                    break;
                case "switchSettings":
                    int choice = JOptionPane.showOptionDialog(this.settings.state().guiComponents().get("mainFrame"),
                            "Do you want to change Save Format? (Actual save format: " + saveFormat + ")",
                            "Change Save Format",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Serialized", "XML"},
                            "XML");
                    switch (choice) {
                        case 1: //XML
                            saveFormat = "XML";
                            break;
                        case 0: //Serialized
                            saveFormat = "Serialized";
                            break;
                        default: //If canceled leaves current saveFormat
                            break;
                    }
                    break;
                case "startVisualOdometry":
                    // Start button clicked
                    if (voTask == null || voTask.isDone()) {
                        voTask = this.startVisualOdometry();
                    }
                    break;
                case "pauseVisualOdometry":
                    // Pause/Resume button clicked
                    this.pauseVisualOdometry();
                    break;
                case "resetVisualOdometry":
                    // Reset vo button clicked
                    this.resetVisualOdometry();
                    break;
                case "stopVisualOdometry":
                    // Stop vo button clicked
                    this.stopVisualOdometry();
                    break;
                case "clearVisualOdometry":
                    // Clear vo button clicked
                    this.clearVisualOdometry();
                    break;
                case "timedStopVisualOdometry":
                    // Timed processing button clicked
                    this.timedStopVisualOdometry();
                    break;
            }
        }

        public void doubleClick(MouseEvent evt) {
        }

        private @NotNull Future<?> startVisualOdometry() {
            return this.startVisualOdometry(false);
        }

        private @NotNull Future<?> startVisualOdometry(boolean isTimed) {
            // Run vo processing in dedicated thread
            this.setRunningToolbar(isTimed);
            return voExecutor.submit(() -> {
                this.core.start();
                this.setReadyToolbar(null);
            });
        }

        private void pauseVisualOdometry() {
            if (this.settings.state().processing().not(ProcessingState.Paused)) {
                // Notify pause to vo thread
                CoreUtils.setProcessingStateSafe(this.settings, ProcessingState.Paused);
            } else {
                // Notify resume to vo thread
                CoreUtils.setProcessingStateSafe(this.settings, ProcessingState.Running);
            }
            // Switch pause/resume icon
            this.btnPauseVisualOdometry.switchIconSet();
        }

        private void resetVisualOdometry() {
            // Notify reset to vo thread
            CoreUtils.setResetRequested(this.settings, true);
        }

        private void stopVisualOdometry() {
            // Notify stop to vo thread
            CoreUtils.setProcessingStateSafe(this.settings, ProcessingState.Stopped);

            Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_STOP_THREAD)).submit(() -> {
                // Wait for task full stop (or error) and reset toolbar
                this.settings.state().processing().waitUntil(
                        ProcessingState.StandBy,
                        ProcessingState.Error
                );
                this.setReadyToolbar(null);
            });
        }

        private void clearVisualOdometry() {
            if (this.settings.state().processing().is(ProcessingState.Running) ||
                    this.settings.state().processing().is(ProcessingState.Paused)) {
                // Notify clear to vo thread
                CoreUtils.setProcessingStateSafe(this.settings, ProcessingState.Cleared);

                Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_CLEAR_THREAD)).submit(() -> {
                    // Wait for task full stop (or error) and reset toolbar
                    this.settings.state().processing().waitUntil(
                            ProcessingState.StandBy,
                            ProcessingState.Error
                    );
                    this.setReadyToolbar(false);
                });
            } else {
                CoreRendering.renderClearAllPoints(this.settings);
                CoreRendering.renderAppStatus(this.settings, AppStatus.Cleared);
                this.setReadyToolbar(false);
            }
        }

        private void timedStopVisualOdometry() {
            final int totalSeconds;
            String choice = (String) JOptionPane.showInputDialog(
                    this.mainFrame,
                    GuiConstants.DLG_TIMED_PROCESSING_MESSAGE,
                    GuiConstants.DLG_TIMED_PROCESSING_TITLE,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    GuiConstants.DLG_TIMED_PROCESSING_DEFAULT_VALUE
            );
            if (choice == null) {
                return;
            } else {
                totalSeconds = Integer.parseInt(choice);
            }

            // Start timed vo process
            this.startVisualOdometry(true);

            Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_THREAD)).submit(() -> {
                // Suspend thread until vo process is running (or error)
                this.btnDisableAndRepaint(this.btnTimedVO);
                this.settings.state().processing().waitUntil(
                        ProcessingState.Running,
                        ProcessingState.Error
                );
                if (this.settings.state().processing().is(ProcessingState.Error))
                    return;
                this.btnSwitchAndSetText(this.btnTimedVO, totalSeconds);

                // Start countdown
                AtomicInteger seconds = new AtomicInteger(0);
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
                        NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_COUNTDOWN_THREAD));
                service.scheduleAtFixedRate(() -> timedStop(settings, service, seconds, totalSeconds),
                        1, 1, TimeUnit.SECONDS);
            });
        }

        private void timedStop(@NotNull Settings settings, ScheduledExecutorService service, AtomicInteger seconds,
                               int totalSeconds) {
            {
                if (settings.state().processing().is(ProcessingState.Paused)) {
                    // Wait until resume
                    service.shutdown();
                    settings.state().processing().waitUntilNot(ProcessingState.Paused);
                    service.scheduleAtFixedRate(() -> timedStop(settings, service, seconds, totalSeconds),
                            0, 1, TimeUnit.SECONDS);
                } else if (settings.state().processing().not(ProcessingState.Running)) {
                    // Stop if vo thread isn't running
                    service.shutdown();
                    return;
                }

                // Update counter
                int currSeconds = seconds.incrementAndGet();
                this.btnTimedVO.setForegroundText(String.valueOf(totalSeconds - currSeconds));

                // If countdown ended
                if (currSeconds == totalSeconds) {
                    // Stop capture and processing
                    if (settings.state().processing().is(ProcessingState.Running) ||
                            settings.state().device().isRunning()) {
                        try {
                            settings.state().device().stop();
                        } catch (CameraException ignored) {
                            // any camera exception will be managed by vo thread itself on cleanup phase
                        }
                    }
                    service.shutdown();
                }
            }
        }

        private void setRunningToolbar(boolean isTimed) {
            // Disable start button / enable others
            this.setToolbarStatus(false, !isTimed, true, true,
                    true, false);
        }

        private void setReadyToolbar(Boolean clearEnabled) {
            clearEnabled = clearEnabled != null ? clearEnabled : this.chartXZPanel.hasPoints();
            boolean timedEnabled = SourceType.Device.is(settings.core().input().source());

            this.setToolbarStatus(true, false, false, false,
                    clearEnabled, timedEnabled);
            this.restoreButtons();
        }

        private void setToolbarStatus(boolean startEnabled, boolean pauseEnabled, boolean stopEnabled,
                                      boolean resetEnabled, boolean clearEnabled, boolean timedEnabled) {
            this.btnStartVisualOdometry.setEnabled(startEnabled);
            this.btnPauseVisualOdometry.setEnabled(pauseEnabled);
            this.btnStopVisualOdometry.setEnabled(stopEnabled);
            this.btnResetVisualOdometry.setEnabled(resetEnabled);
            this.btnClearVisualOdometry.setEnabled(clearEnabled);
            this.btnTimedVO.setEnabled(timedEnabled);
        }

        private void btnDisableAndRepaint(@NotNull JButton button) {
            button.setEnabled(false);
            button.repaint();
        }

        private void btnSwitchAndSetText(@NotNull ImageButton button, int totalSeconds) {
            button.switchIconSet();
            button.setForegroundText(String.valueOf(totalSeconds));
        }

        private void restoreButtons() {
            this.btnTimedVO.removeForegroundText();
            this.btnTimedVO.defaultIconSet();
            this.btnPauseVisualOdometry.defaultIconSet();
        }
    }


    private final class BrowseButtonListener implements ActionListener {

        private final HashMap<String, Component> mainFrameContainer;
        private final JComboBox<String> pathComboBox;
        private final String dialogTitle;
        private final String[] dialogFileFilterExtension;
        private final String[] dialogFileFilterDescription;
        private final boolean enableDirectorySelection;

        private BrowseButtonListener(HashMap<String, Component> mainFrameContainer, JComboBox<String> pathComboBox, String dialogTitle,
                                     String[] dialogFileFilterExtension, String[] dialogFileFilterDescription, boolean enableDirectorySelection) {

            this.mainFrameContainer = mainFrameContainer;
            this.pathComboBox = pathComboBox;
            this.dialogTitle = dialogTitle;
            this.dialogFileFilterExtension = dialogFileFilterExtension;
            this.dialogFileFilterDescription = dialogFileFilterDescription;
            this.enableDirectorySelection = enableDirectorySelection;

        }

        @Override
        public void actionPerformed(ActionEvent evt) {        //When clicking the browsing button
            String dialogPath = (String) pathComboBox.getSelectedItem();//assumes the current Path TextComponent
            //content as the current searching path

            JFileChooser browse = new JFileChooser(dialogPath);    //Creates a new File Browsing Dialog at dialogPath path
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

            if (browse.showOpenDialog(mainFrameContainer.get("mainFrame")) == 0) {//Opens the File Browsing Dialog and if an existing file has been selected
                File choice = browse.getSelectedFile(); //Gets the selected file
                pathComboBox.setSelectedItem(choice.getAbsolutePath());    //Sets the Path TextComponent content
                //to the file path (triggering also a change
                //to the Path parameter, thanks to
                //the TextComponent change Listener)
            }
        }
    }


    private final class InputSourceOptionListener implements ActionListener {
        private final SourceType inputSource;
        private final Settings settings;

        //Extracts from guiComponents all the components needed (Video/Device components)
        private JRadioButton optVideoSource;
        private JComboBox<String> txtVideoSource;
        private JButton btnVideoSourceBrowsing;

        private JRadioButton optDeviceSource;
        private JComboBox<String> txtDeviceType;
        private JComboBox<String> txtDevicePath;
        private JPanel deviceAdjustmentsPanel;

        private ImageButton btnTimedProcessingVO;

        private InputSourceOptionListener(SourceType inputSource, Settings settings) {
            this.inputSource = inputSource;
            this.settings = settings;
        }

        @SuppressWarnings("unchecked") // component-registry lookups: JComboBox<String> casts are safe by construction
        private void getComponents() {
            this.optVideoSource = (JRadioButton) this.settings.state().guiComponents().get("optVideoSource");
            this.txtVideoSource = (JComboBox<String>) this.settings.state().guiComponents().get("txtVideoSource");
            this.btnVideoSourceBrowsing = (JButton) this.settings.state().guiComponents().get("btnVideoSourceBrowsing");

            this.optDeviceSource = (JRadioButton) this.settings.state().guiComponents().get("optDeviceSource");
            this.txtDeviceType = (JComboBox<String>) this.settings.state().guiComponents().get("txtDeviceType");
            this.txtDevicePath = (JComboBox<String>) this.settings.state().guiComponents().get("txtDevicePath");
            this.deviceAdjustmentsPanel = (JPanel) this.settings.state().guiComponents().get("deviceAdjustmentsPanel");

            this.btnTimedProcessingVO = (ImageButton) this.settings.state().guiComponents().get("btnTimedProcessingVO");
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            this.getComponents();

            //An Input Source option button has been clicked
            //(could be Video or Device)

            //Input Source parameter is set to inputSource (VIDEO_INPUT("video") or DEVICE_INPUT("device"))
            this.settings.core().input().source(inputSource);

            boolean isVideo = SourceType.Video.is(inputSource);
            boolean isDevice = SourceType.Device.is(inputSource);

            //Modify visibility of components from the Input Settings Panel
            optVideoSource.setSelected(isVideo);                    //Select/unselect Video Source OptionButton
            optVideoSource.setText(                                    //Sets Video Source OptionButton Text
                    isVideo ? "<html><b>Video</b></html>" : "<html>Video</html>");
            txtVideoSource.setEnabled(isVideo);                        //Enable/disable Video Source ComboBox
            btnVideoSourceBrowsing.setEnabled(isVideo);                //Enable/disable Video Source Browsing Button

            optDeviceSource.setSelected(isDevice);                    //Select/unselect Device Source OptionButton
            optDeviceSource.setText(                                //Sets Device Source OptionButton Text
                    isDevice ? "<html><b>Device</b></html>" : "<html>Device</html>");
            txtDeviceType.setEnabled(isDevice);                    //Enable/disable Device Source ComboBox
            txtDevicePath.setEnabled(isDevice);                    //Enable/disable Device Source ComboBox
            deviceAdjustmentsPanel.setBorder(GuiUtils.getRoundedTitledBorder("Adjustments",
                    isDevice ?
                            GuiConstants.PANEL_BORDER_ACTIVE_COLOR :
                            GuiConstants.PANEL_BORDER_INACTIVE_COLOR,
                    10,
                    10));        //Sets Device Adjustments Panel title and border
            deviceAdjustmentsPanel.setEnabled(isDevice);            //Enable/disable Device Adjustments Panel
            for (Component comp : deviceAdjustmentsPanel.getComponents()) {
                comp.setEnabled(isDevice);                             //Each component in Device Adjustments Panel is enabled/disabled
            }

            // Toolbar button visibility
            btnTimedProcessingVO.setEnabled(isDevice);
        }
    }

    private final class ParameterCheckBoxListener implements ActionListener {

        private final String controlledParameter;
        private final JCheckBox controllerCheckBox;
        private final Settings settings;

        private ParameterCheckBoxListener(String controlledParameter, JCheckBox controllerCheckBox, Settings settings) {

            this.controlledParameter = controlledParameter;
            this.controllerCheckBox = controllerCheckBox;
            this.settings = settings;

        }

        @Override
        public void actionPerformed(ActionEvent evt) {	/*On click updates the controlled Parameter to
														  the current controller CheckBox status. If the
														  checkbox has been selected then the text becomes bold,
														  else it restores to normal*/
            switch (controlledParameter) {
                case "deviceSustainFramerate":
                    this.settings.core().input().device().v4l4j().sustainFramerate(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Sustain Framerate</b></html>" : "<html>Sustain Framerate</html>");
                    break;
                case "deviceTimeoutImageIO":
                    this.settings.core().input().device().v4l4j().timeoutImageIO(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Timeout Image I/O</b></html>" : "<html>Timeout Image I/O</html>");
                    break;
                case "deviceKeepFormat":
                    this.settings.core().input().device().v4l4j().keepFormat(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Keep Format</b></html>" : "<html>Keep Format</html>");
                    break;
                case "fullResolutionPreview":
                    this.settings.core().input().fullResolutionPreview(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Full-Resolution Preview</b></html>" : "<html>Full-Resolution Preview</html>");
                    break;
                case "inputPreviewEnabled":
                    this.settings.core().input().inputPreview(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Enable Input Preview (Slower)</b></html>" : "<html>Enable Input Preview (Slower)</html>");
                    break;
                case "imageResize":
                    this.settings.core().image().resize(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(controllerCheckBox.isSelected() ? "<html><b>Resize</b></html>" : "<html>Resize</html>");
                    this.settings.state().guiComponents().get("txtImageResize").setEnabled(controllerCheckBox.isSelected());
                    break;
                case "internalImagePreview":
                    this.settings.core().image().internalImagePreview(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Preview Internal Image (Slower)</b></html>" : "<html>Preview Internal Image (Slower)</html>");
                    break;
                case "frameSkipEnabled":
                    this.settings.core().image().frameSkipEnabled(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Frame skip</b></html>" : "<html>Frame skip</html>");
                    break;
                case "trackerShowActiveTracks":
                    this.settings.core().tracker().setTrackerShowActiveTracks(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Show Active Tracks</b></html>" : "<html>Show Active Tracks</html>");
                    break;
                case "trackerShowNewTracks":
                    this.settings.core().tracker().setTrackerShowNewTracks(controllerCheckBox.isSelected());
                    controllerCheckBox.setText(
                            controllerCheckBox.isSelected() ? "<html><b>Show New Tracks</b></html>" : "<html>Show New Tracks</html>");
                    break;
            }
        }
    }

    private final class TrackerTypeChangeListener implements ActionListener {

        private final JComboBox<String> txtTrackerType;
        private final Settings settings;

        private TrackerTypeChangeListener(JComboBox<String> txtTrackerType, Settings settings) {

            this.txtTrackerType = txtTrackerType;
            this.settings = settings;

        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            final JPanel kltTrackerPanel = (JPanel) this.settings.state().guiComponents().get("kltTrackerPanel");
            final JPanel surfTrackerPanel = (JPanel) this.settings.state().guiComponents().get("surfTrackerPanel");

            //Obtains selectedTrackerType using selected Index in Tracker Type (Names) ComboBox
            String selectedTrackerType = "";
            Iterator<String> trackerTypes = this.settings.core().tracker().getTrackerTypeNames().keySet().iterator();
            for (int i = 0; i < txtTrackerType.getSelectedIndex(); i++) {
                trackerTypes.next();
            }
            selectedTrackerType = trackerTypes.next();

            //Updates trackerType parameter to the new selectedTrackerType value
            this.settings.core().tracker().setTrackerType(selectedTrackerType);

            //Depending on which Tracker Type has been selected does some other actions:

            //For default (that is KLT Two-Pass with default parameters), KLT and KLT Two-Pass tracker: Shows KLT Tracker Panel (else hides it)
            kltTrackerPanel.setVisible(selectedTrackerType.equals(TrackerSettings.DEFAULT_TRACKER) ||
                    selectedTrackerType.equals(TrackerSettings.KLT) ||
                    selectedTrackerType.equals(TrackerSettings.KLT2));
            kltTrackerPanel.setEnabled(!selectedTrackerType.equals(TrackerSettings.DEFAULT_TRACKER));//If default disables klt settings panel
            for (Component comp : kltTrackerPanel.getComponents()) {
                comp.setEnabled(!selectedTrackerType.equals(TrackerSettings.DEFAULT_TRACKER));//And each component in klt settings panel is enabled/disabled
            }

            //For SURF or SURF Two-Pass tracker: Shows SURF Tracker Panel (else hides it)
            surfTrackerPanel.setVisible(selectedTrackerType.equals(TrackerSettings.SURF) ||
                    selectedTrackerType.equals(TrackerSettings.SURF2));
        }
    }

    private final class ChartButtonListener implements ActionListener {

        private final String function;
        private final Settings settings;

        public ChartButtonListener(String function, Settings settings) {

            this.function = function;
            this.settings = settings;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            ChartScrollPane chartXZPanel = this.settings.state().guiController().chartXZPanel();
            ChartScrollPane chartYPanel = this.settings.state().guiController().chartYPanel();

            switch (function) {
                case "chartXZApplyScale": //Change XZ Chart scale
                    chartXZPanel.settings().chartScale(this.settings.core().chart().scaleXZ());
                    chartXZPanel.resetSize();
                    break;
                case "chartXZMoveToOrigin"://Chart X/Z Move to Origin
                    chartXZPanel.moveToOrigin();
                    break;
                case "chartXZMoveToLastPoint"://Chart X/Z Move To Last Point
                    chartXZPanel.moveToLast();
                    break;
                case "chartXZ3DPoints": //Chart X/Z 3D Points

                    JCheckBox chkChartXZ_3DPoints = (JCheckBox) this.settings.state().guiComponents().get("chkChartXZ3DPoints");

                    chkChartXZ_3DPoints.setText(
                            chkChartXZ_3DPoints.isSelected() ? "<html><b>3D Points</b></html>" : "<html>3D Points</html>");
                    chartXZPanel.settings().thickPoints(chkChartXZ_3DPoints.isSelected());
                    chartXZPanel.repaint();
                    break;
                case "chartYApplyScale": //Change Y Chart Scale
                    chartYPanel.settings().chartScale(this.settings.core().chart().scaleY());
                    chartYPanel.resetSize();
                    break;
                case "chartYMoveToOrigin"://Chart Y Move to Origin
                    chartYPanel.moveToOrigin();
                    break;
                case "chartYMoveToLastPoint"://Chart Y Move to Last Point
                    chartYPanel.moveToLast();
                    break;
            }
        }
    }


    private final class HeightProportionalSpring extends Spring {

        private final Component component;
        private final float proportion;

        private HeightProportionalSpring(Component component, float proportion) {

            this.component = component;
            this.proportion = proportion;

        }

        @Override
        public void setValue(int direction) {
        }

        @Override
        public int getValue() {
            return (int) Math.round(component.getHeight() * proportion);
        }

        @Override
        public int getPreferredValue() {
            return (int) Math.round(component.getHeight() * proportion);
        }

        @Override
        public int getMinimumValue() {
            return (int) Math.round(component.getHeight() * proportion);
        }

        @Override
        public int getMaximumValue() {
            return (int) Math.round(component.getHeight() * proportion);
        }
    }

}