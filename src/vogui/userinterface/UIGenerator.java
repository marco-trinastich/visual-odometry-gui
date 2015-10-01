package vogui.userinterface;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import com.thoughtworks.xstream.XStream;

import boofcv.gui.image.ImagePanel;
import vogui.core.Core;
import vogui.parameters.*;

@SuppressWarnings("rawtypes")
public class UIGenerator {

	
	private Core core; //Core process to be used (for Visual Odometry processing)
	private boolean isSystemLookAndFeelEnabled;

	
	public UIGenerator(Core core){		//Initializes with custom parameters
		this.setCore(core);
	}
	
	public void startApp(){
		this.createAllFrames(core);
	}
	
	private void createAllFrames(Core core){

		/***********************
		 * ALL-FRAMES CREATION *
		 ***********************/

		//Tries to enable System Look and Feel (Better Graphic User Interface)
		setSystemLookAndFeelEnabled();
		
		/*Creates Input Video Frame*/
		createInputVideoFrame(core);

		/*Creates Output Video Frame (for Visual Odometry Processing)*/
		createOutputVideoFrame(core);

		/*Creates Chart Viewer/Info Frame*/
		final JFrame chartFrame = createChartFrame(core);
		chartFrame.setVisible(true); //Sets the Frame visible

		/*Creates Main Frame (All Visual Odometry Settings+Buttons)*/
		JFrame mainFrame = createMainFrame(core);
		mainFrame.setVisible(true); //Sets the Frame visible and start the main app
		
	}

	
	private JFrame createInputVideoFrame(Core core){

		/*Creates Input Video Panel to display the input video*/
		final ImagePanel inputVideoPanel = new ImagePanel();

		/*Creates Input Video Frame*/
		final JFrame inputVideoFrame = new JFrame("Video Input");
		
		/*Sets Input Video Frame x location to 2 times the app frames default width, to the right of Chart Frame and Main Frame*/
		inputVideoFrame.setLocationRelativeTo(null);
		inputVideoFrame.setLocation((getFrameDefaultDimension().width*2)+65, 0);
	
		//Adds the panel to the frame
		inputVideoFrame.getContentPane().add(inputVideoPanel);
		
		//Adds panel and frame to the guiComponents
		core.getParameters().getGuiComponents().put("inputVideoPanel", inputVideoPanel);
		core.getParameters().getGuiComponents().put("inputVideoFrame", inputVideoFrame);
		
		return inputVideoFrame;
	}
	
	
	private JFrame createOutputVideoFrame(Core core){

		/*Creates Output Video Panel to display the output (processed) video*/
		final ImagePanel outputVideoPanel = new ImagePanel();

		/*Creates Output Video Frame*/
		final JFrame outputVideoFrame = new JFrame("VO Processing");
		
		/*Sets Output Video Frame x location to 2 times the app frames default width, and y location at frame default height/2*/
		outputVideoFrame.setLocationRelativeTo(null);
		outputVideoFrame.setLocation((getFrameDefaultDimension().width*2)+65, (getFrameDefaultDimension().height/2));
	
		//Adds the panel to the frame
		outputVideoFrame.getContentPane().add(outputVideoPanel);
		
		//Adds panel and frame to the guiComponents
		core.getParameters().getGuiComponents().put("outputVideoPanel", outputVideoPanel);
		core.getParameters().getGuiComponents().put("outputVideoFrame", outputVideoFrame);
				
		return outputVideoFrame;
	}

	
	private JFrame createChartFrame(Core core){
		
		
		/***************************
		 * CHART VIEWER/INFO FRAME *
		 ***************************/
		
		
		/**************************
		 * COMPONENTS CREATION *
		 *************************/

		
		/**
		  *  1. XZ CHART PANEL, Y CHART PANEL AND INFO PANEL CREATION
		  *  	
		  *  Creates the XZ Chart Panel, Y Chart Panel and Informations Panel
		  *  
		  */

		
			/*Creates XZ Chart Panel*/
			
			final ChartScrollPane chartXZPanel = new ChartScrollPane(Color.blue); //Border color set to blue
			//The panel is initialized with default constructor, so X and Y axis are centered
			
			/*Configures XZ Chart Panel*/
			chartXZPanel.setChartColor(Color.blue);		//Chart color set to blue
			chartXZPanel.setAxisColor(Color.black);		//Axis color set to black
			chartXZPanel.setAxisNames("X", "Z");		//Axis names set to "X" (x axis) and "Z" (y axis)
			chartXZPanel.setAxisNamesColor(Color.blue);	//Axis names color set to blue
			chartXZPanel.setAxisUnitsColor(Color.blue);	//Axis units markers color set to blue
			chartXZPanel.setShowLegend(true);			//Enables Legend showing
			/*Sets border to compound border: Titled+Empty*/
			chartXZPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("X/Z Chart Viewer"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			/*Sets panel size to 400x400*/
			chartXZPanel.setPreferredSize(new Dimension(400,400));
			

			/*Creates Y Chart Panel*/
			
			/*The panel is customly initialized so that center coordinates are specified (20, 85)
			 * and autocentering is disabled for X (Y axis is fixed at 20), and enabled for Y
			 * (X axis is at the mid of the panel)*/
			final ChartScrollPane chartYPanel = new ChartScrollPane(20,85,false,true,Color.blue); //Border color set to blue
			
			/*Configures Y Chart Panel*/
			chartYPanel.setChartColor(Color.blue);		//Chart color set to blue
			chartYPanel.setAxisColor(Color.black);		//Axis color set to black
			chartYPanel.setAxisNames("frame", "Y");		//Axis names set to "frames" (x axis) and "Y" (y axis)
			chartYPanel.setAxisNamesColor(Color.blue);	//Axis names color set to blue
			chartYPanel.setAxisUnitsColor(Color.blue);	//Axis units markers color set to blue
			/*Sets border to compund border: Titled+Empty*/
			chartYPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Y Chart Viewer"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			/*Sets panel size to 400x200*/
			chartYPanel.setPreferredSize(new Dimension(400,200));
			
			
			/*Creates Info Panel*/
			final InfoScrollPane chartInfoPanel = new InfoScrollPane(Color.blue); //Border color set to blue
			
			/*Configures Info Panel*/
			/*Sets border to compound border: Titled+Empty*/
			chartInfoPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Chart/Elaboration Info"),
					  BorderFactory.createEmptyBorder(5,5,5,5)));
			/*Sets panel size to 400x400*/
			chartInfoPanel.setPreferredSize(new Dimension(400,400));
			chartInfoPanel.setInfoPanelVisible(false);	//By default sets Info Panel not visible
			chartInfoPanel.setBufferInfoVisible(false);	//By default sets Buffer Info (device related) not visible
			chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Ready.</html>");	//Sets Status Label text to Ready

			/*	Adds a Listener to the Visual Odometry Tracked Points list, that
			 * on click moves the XZ Chart and Y Chart to the selected point
			 * */
			chartInfoPanel.lst_points.addListSelectionListener(new InfoPointsListListener(core));
			
			
			
			/**
			  *  2. CHART FRAME CREATION
			  * 
			  *  Creation of the Chart Frame.
			  *  Adding of Maximize On DoubleClick Listeners to the Panels.
			  * 	
			  */
				
			
			/*Creates Chart Frame*/
			final JFrame chartFrame = new JFrame("Chart/Info");
			chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Sets default close operation to app termination

			//Adds components to the Chart Frame (XZ Chart, Y Chart and Info Panels)
			chartFrame.getContentPane().add(chartXZPanel);
			chartFrame.getContentPane().add(chartYPanel);
			chartFrame.getContentPane().add(chartInfoPanel);

			//Adds Maximize On DoubleClick Listener for each panel, that
			//maximize/restore that panel into the chartFrame
			chartXZPanel.addMouseListener(new MaximizeOnDblClick(chartXZPanel, chartFrame));
			chartYPanel.addMouseListener(new MaximizeOnDblClick(chartYPanel, chartFrame));
			chartInfoPanel.addMouseListener(new MaximizeOnDblClick(chartInfoPanel, chartFrame));

			
			
			/**************************
			 * COMPONENTS DISPOSITION *
			 *************************/



			/**
			  *  3. CHART FRAME DISPOSITION
			  * 
			  *  Disposition of the components inside Chart Frame.
			  *  
			  */

			
			Container chartFrameContentPane = chartFrame.getContentPane(); //Gets the Chart Frame contentPane
			
			final SpringLayout panelLayout = new SpringLayout(); //Creates a new Spring Layout to dispose  
																 //components inside the panel

			//Creates an heightProportional Spring, that returns the 11/30(0.36) of chartFrame height
			Spring heightProportional = new HeightProportionalSpring(chartFrame, 11/30f);

			
			//On the first row XZ Chart Panel (that extends from
			//the top till 11/30 of the height of the Chart Frame)
			panelLayout.putConstraint(SpringLayout.NORTH, chartXZPanel, 5, SpringLayout.NORTH, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.WEST, chartXZPanel, 5, SpringLayout.WEST, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.EAST, chartXZPanel, -5, SpringLayout.EAST, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.SOUTH, chartXZPanel, heightProportional, SpringLayout.NORTH, chartFrameContentPane);
			
			//On the second row Y Chart Panel
			panelLayout.putConstraint(SpringLayout.NORTH, chartYPanel, 5, SpringLayout.SOUTH, chartXZPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chartYPanel, 5, SpringLayout.WEST, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.EAST, chartYPanel, -5, SpringLayout.EAST, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.SOUTH, chartFrameContentPane, heightProportional, SpringLayout.SOUTH, chartYPanel);
			
			//On the third row Info Panel (that extends from the
			//bottom till 11/30 of the height of the Chart Frame)
			panelLayout.putConstraint(SpringLayout.NORTH, chartInfoPanel, 5, SpringLayout.SOUTH, chartYPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chartInfoPanel, 5, SpringLayout.WEST, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.EAST, chartInfoPanel, -5, SpringLayout.EAST, chartFrameContentPane);
			panelLayout.putConstraint(SpringLayout.SOUTH, chartInfoPanel, -5, SpringLayout.SOUTH, chartFrameContentPane);
			
			//Apply the layout to the panel
			chartFrame.setLayout(panelLayout);
			
			//Sets Chart Frame position to 0,0 (top-left of the screen)
			chartFrame.setLocation(0, 0);
			//Sets Chart Frame dimension to Frame default dimension
			chartFrame.setPreferredSize(getFrameDefaultDimension());
			chartFrame.pack();

			
			
			/**
			  *  4. POPULATE GUICOMPONENTS
			  * 
			  *  Adds all the most important (and reused) components to the guiComponents HashMap
			  *  
			  */

			
			core.getParameters().getGuiComponents().put("chartXZPanel", chartXZPanel);
			core.getParameters().getGuiComponents().put("chartYPanel", chartYPanel);
			core.getParameters().getGuiComponents().put("chartInfoPanel", chartInfoPanel);
			core.getParameters().getGuiComponents().put("chartFrame", chartFrame);
			
			
			return chartFrame;
	}
	
	
	private JFrame createMainFrame(Core core){
		
		
		SpringLayout panelLayout = null; //SpringLayout needed for components disposition

		
		/**************
		 * MAIN FRAME *
		 **************/


			/***********************
			 * COMPONENTS CREATION *
			 ***********************/

		
			/**
			 *  1. MAIN SCROLL PANE CREATION
			 * 
			 * 	Here creates mainScrollPane that contains all the SETTINGS SUBPANELS:
			 * 	Input Settings, Internal Image Settings, Tracker Settings, 
			 * 	Visual Odometry Settings and Chart Settings
			 * 
			 */
			
			
			/*Creates mainScrollPane*/
			JScrollPane mainScrollPane = createMainScrollPane(core);

			
			/**
			 *  2. OPERATION BUTTONS CREATION
			 * 
			 * 	Creates the operations buttons (Start, Pause, Stop, ..)
			 * 	
			 */
			
			
			/*Operations Buttons*/
			
			/*Load Settings Button*/
			final JButton btnLoadSettings = new JButton("Load Settings");
			btnLoadSettings.setToolTipText("Loads saved parameters");
			/*Listener*/
			btnLoadSettings.addActionListener(new MainButtonListener("loadSettings", core));
					
			/*Save Settings Button*/
			final JButton btnSaveSettings = new JButton("Save Settings");
			btnSaveSettings.setToolTipText("Double Click to select Save Format");
			/*Listener*/
			btnSaveSettings.addMouseListener(new MainButtonListener("saveSettings", core));

			/*Reset Settings Button*/
			final JButton btnResetSettings = new JButton("Reset Settings");
			btnResetSettings.setToolTipText("Resets default parameters");
			/*Listener*/
			btnResetSettings.addActionListener(new MainButtonListener("resetSettings", core));
			
			/*Start Visual Odometry Button*/
			final JButton btnStartVisualOdometry = new JButton("Start");
			/*Listener*/
			btnStartVisualOdometry.addActionListener(new MainButtonListener("startVisualOdometry", core));
				
			/*Pause Visual Odometry Button*/
			final JButton btnPauseVisualOdometry = new JButton("Pause");
			btnPauseVisualOdometry.setEnabled(false); //Disabled on Startup (will be enabled on process start)
			/*Listener*/
			btnPauseVisualOdometry.addActionListener(new MainButtonListener("pauseVisualOdometry", core));

			/*Reset Visual Odometry Button*/
			final JButton btnResetVisualOdometry = new JButton("Reset VO");
			btnResetVisualOdometry.setEnabled(false); //Disabled on Startup
			/*Listener*/
			btnResetVisualOdometry.addActionListener(new MainButtonListener("resetVisualOdometry", core));

			/*Stop Visual Odometry Button*/
			final JButton btnStopVisualOdometry = new JButton("Stop");
			btnStopVisualOdometry.setEnabled(false); //Disabled on Startup
			/*Listener*/
			btnStopVisualOdometry.addActionListener(new MainButtonListener("stopVisualOdometry", core));
					
			/*Clear Visual Odometry Button*/
			final JButton btnClearVisualOdometry = new JButton("Clear");
			btnClearVisualOdometry.setEnabled(false); //Disabled on Startup
			/*Listener*/
			btnClearVisualOdometry.addActionListener(new MainButtonListener("clearVisualOdometry", core));
			
			/*Timed Stop Visual Odometry Button (Stop capture after N seconds, Device only)*/
			final JButton btnTimedStopVisualOdometry = new JButton("Stop capture after:");
			btnTimedStopVisualOdometry.setEnabled(false); //Disabled on Startup
			/*Listener*/
			btnTimedStopVisualOdometry.addActionListener(new MainButtonListener("timedStopVisualOdometry", core));
			
			/*Timed Stop Visual Odometry TextField*/
			final JTextField txtTimedStopVisualOdometry = new JTextField("10",3); //Initialized at 10 (seconds), 3 columns
			txtTimedStopVisualOdometry.setHorizontalAlignment(JTextField.CENTER); //Centered alignment
			txtTimedStopVisualOdometry.setEnabled(false); //Disabled on Startup
			/*Listener*/
			txtTimedStopVisualOdometry.addFocusListener(
					new IntegerParameterTextFieldListener("timedStopVisualOdometry", txtTimedStopVisualOdometry, core));

			
			
			/**
			  *  3. MAIN FRAME CREATION
			  * 
			  *  Creation of the Main Frame.
			  * 	
			  */
				
			
			//Creates mainFrame and sets Title
			final JFrame  mainFrame = new JFrame("Tracking and Mapping System based on Visual Odometry");
			//Sets mainFrame default closing operation to Exit on Close
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
			//Adds all the components to the Frame: mainScrollPane, and all the buttons (load,save,start,pause,...)
			mainFrame.add(mainScrollPane);
			mainFrame.add(btnLoadSettings);
			mainFrame.add(btnSaveSettings);
			mainFrame.add(btnResetSettings);
			mainFrame.add(btnStartVisualOdometry);
			mainFrame.add(btnPauseVisualOdometry);
			mainFrame.add(btnResetVisualOdometry);
			mainFrame.add(btnStopVisualOdometry);
			mainFrame.add(btnClearVisualOdometry);
			mainFrame.add(btnTimedStopVisualOdometry);
			mainFrame.add(txtTimedStopVisualOdometry);



			/**************************
			 * COMPONENTS DISPOSITION *
			 *************************/



			/**
			  *  4. MAIN FRAME DISPOSITION
			  * 
			  *  Disposition of the components inside Main Frame.
			  *  
			  */

			
			panelLayout = new SpringLayout();

			Container contentPane = mainFrame.getContentPane();
			
			
			//On the first row mainScrollPane
			panelLayout.putConstraint(SpringLayout.NORTH, mainScrollPane, 0, SpringLayout.NORTH, contentPane);
			panelLayout.putConstraint(SpringLayout.WEST, mainScrollPane, 0, SpringLayout.WEST, contentPane);
			panelLayout.putConstraint(SpringLayout.EAST, mainScrollPane, 0, SpringLayout.EAST, contentPane);
			panelLayout.putConstraint(SpringLayout.SOUTH, mainScrollPane, -5, SpringLayout.NORTH, btnLoadSettings);

			//On the second row Load Settings Button, Save Settings Button and Reset Settings Button
			panelLayout.putConstraint(SpringLayout.NORTH, btnLoadSettings, -65, SpringLayout.SOUTH, contentPane);
			panelLayout.putConstraint(SpringLayout.WEST, btnLoadSettings, 5, SpringLayout.WEST, contentPane);
			panelLayout.putConstraint(SpringLayout.NORTH, btnSaveSettings, 0, SpringLayout.NORTH, btnLoadSettings);
			panelLayout.putConstraint(SpringLayout.WEST, btnSaveSettings, 5, SpringLayout.EAST, btnLoadSettings);
			panelLayout.putConstraint(SpringLayout.NORTH, btnResetSettings, 0, SpringLayout.NORTH, btnSaveSettings);
			panelLayout.putConstraint(SpringLayout.WEST, btnResetSettings, 5, SpringLayout.EAST, btnSaveSettings);
			
			//On the third row VO Start, Pause, Reset, Stop, Clear, Timed Stop Buttons and Timed Stop Text Field
			panelLayout.putConstraint(SpringLayout.NORTH, btnStartVisualOdometry, 3, SpringLayout.SOUTH, btnLoadSettings);
			panelLayout.putConstraint(SpringLayout.WEST, btnStartVisualOdometry, 5, SpringLayout.WEST, contentPane);
			panelLayout.putConstraint(SpringLayout.NORTH, btnPauseVisualOdometry, 0, SpringLayout.NORTH, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.WEST, btnPauseVisualOdometry, 5, SpringLayout.EAST, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.NORTH, btnResetVisualOdometry, 0, SpringLayout.NORTH, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.WEST, btnResetVisualOdometry, 5, SpringLayout.EAST, btnPauseVisualOdometry);
			panelLayout.putConstraint(SpringLayout.NORTH, btnStopVisualOdometry, 0, SpringLayout.NORTH, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.WEST, btnStopVisualOdometry, 5, SpringLayout.EAST, btnResetVisualOdometry);
			panelLayout.putConstraint(SpringLayout.NORTH, btnClearVisualOdometry, 0, SpringLayout.NORTH, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.WEST, btnClearVisualOdometry, 5, SpringLayout.EAST, btnStopVisualOdometry);
			panelLayout.putConstraint(SpringLayout.NORTH, btnTimedStopVisualOdometry, 0, SpringLayout.NORTH, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.WEST, btnTimedStopVisualOdometry, 5, SpringLayout.EAST, btnClearVisualOdometry);
			panelLayout.putConstraint(SpringLayout.NORTH, txtTimedStopVisualOdometry, 1, SpringLayout.NORTH, btnStartVisualOdometry);
			panelLayout.putConstraint(SpringLayout.WEST, txtTimedStopVisualOdometry, 5, SpringLayout.EAST, btnTimedStopVisualOdometry);
			
			//Adds the layout to the Frame
			mainFrame.setLayout(panelLayout);
			
			//Moves the Frame: sets x location to FrameDefaultWidth+55 and y location to the top of the Screen
			//(Right side of chartFrame)
			mainFrame.setLocation(getFrameDefaultDimension().width+55, 0);
			//Resizes the Frame: to Frames Default Width/Height
			mainFrame.setPreferredSize(getFrameDefaultDimension());
			mainFrame.pack();

			
			
			/**
			  *  5. POPULATE GUICOMPONENTS
			  * 
			  *  Adds all the most important (and reused) components to the guiComponents HashMap
			  *  
			  */

			
			core.getParameters().getGuiComponents().put("btnLoadSettings", btnLoadSettings);
			core.getParameters().getGuiComponents().put("btnSaveSettings", btnSaveSettings);
			core.getParameters().getGuiComponents().put("btnStartVisualOdometry", btnStartVisualOdometry);
			core.getParameters().getGuiComponents().put("btnPauseVisualOdometry", btnPauseVisualOdometry);
			core.getParameters().getGuiComponents().put("btnResetVisualOdometry", btnResetVisualOdometry);
			core.getParameters().getGuiComponents().put("btnStopVisualOdometry", btnStopVisualOdometry);
			core.getParameters().getGuiComponents().put("btnClearVisualOdometry", btnClearVisualOdometry);
			core.getParameters().getGuiComponents().put("btnTimedStopVisualOdometry", btnTimedStopVisualOdometry);
			core.getParameters().getGuiComponents().put("txtTimedStopVisualOdometry", txtTimedStopVisualOdometry);
			core.getParameters().getGuiComponents().put("mainFrame", mainFrame);

			
			return mainFrame;
	}
	
	
	private JScrollPane createMainScrollPane(Core core) {

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
			
			
			/*Title Label*/			
			final JLabel lblTitle = new JLabel("<html><b>Visual Odometry GUI (based on BoofCV)</b></html>");
				
			/*Creates Input Settings Panel*/			
			final JPanel inputSettingsPanel = createInputSettingsPanel(core);
			
			/*Creates Internal Image Settings Panel*/
			final JPanel internalImageSettingsPanel = createInternalImageSettingsPanel(core);
					
			/*Creates Tracker Settings Panel*/
			final JPanel trackerSettingsPanel = createTrackerSettingsPanel(core);
		
			/*Creates Visual Odometry Settings Panel*/
			final JPanel visualOdometrySettingsPanel = createVisualOdometrySettingsPanel(core);
					
			/*Creates Chart/Output Settings Panel*/
			final JPanel chartSettingsPanel = createChartSettingsPanel(core);
		
			
			
			/**
			 *  2. MAIN PANEL CREATION
			 * 
			 *  Creation of the Main Panel.
			 *  
			 */

			final JPanel mainPanel = new JPanel();
			
			//Adds title label and all subpanels to mainPanel
			mainPanel.add(lblTitle);
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

			//On the first row Title Label
			panelLayout.putConstraint(SpringLayout.NORTH, lblTitle, 5, SpringLayout.NORTH, mainPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblTitle, 5, SpringLayout.WEST, mainPanel);
			panelLayout.putConstraint(SpringLayout.EAST, lblTitle, -5, SpringLayout.EAST, mainPanel);

			//On the second row Input Settings Panel
			panelLayout.putConstraint(SpringLayout.NORTH, inputSettingsPanel, 2, SpringLayout.SOUTH, lblTitle);
			panelLayout.putConstraint(SpringLayout.WEST, inputSettingsPanel, 5, SpringLayout.WEST, mainPanel);
			panelLayout.putConstraint(SpringLayout.EAST, inputSettingsPanel, -5, SpringLayout.EAST, mainPanel);

			//On the third row Internal Image Settings Panel
			panelLayout.putConstraint(SpringLayout.NORTH, internalImageSettingsPanel, 1, SpringLayout.SOUTH, inputSettingsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, internalImageSettingsPanel, 5, SpringLayout.WEST, mainPanel);
			panelLayout.putConstraint(SpringLayout.EAST, internalImageSettingsPanel, -5, SpringLayout.EAST, mainPanel);

			//On the fourth row Tracker Settings Panel
			panelLayout.putConstraint(SpringLayout.NORTH, trackerSettingsPanel, 1, SpringLayout.SOUTH, internalImageSettingsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, trackerSettingsPanel, 5, SpringLayout.WEST, mainPanel);
			panelLayout.putConstraint(SpringLayout.EAST, trackerSettingsPanel, -5, SpringLayout.EAST, mainPanel);
			
			//On the fifth row Visual Odometry Settings Panel
			panelLayout.putConstraint(SpringLayout.NORTH, visualOdometrySettingsPanel, 1, SpringLayout.SOUTH, trackerSettingsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, visualOdometrySettingsPanel, 5, SpringLayout.WEST, mainPanel);
			panelLayout.putConstraint(SpringLayout.EAST, visualOdometrySettingsPanel, -5, SpringLayout.EAST, mainPanel);
			
			//On the sixth row Chart Settings Panel
			panelLayout.putConstraint(SpringLayout.NORTH, chartSettingsPanel, 1, SpringLayout.SOUTH, visualOdometrySettingsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chartSettingsPanel, 5, SpringLayout.WEST, mainPanel);
			panelLayout.putConstraint(SpringLayout.EAST, chartSettingsPanel, -5, SpringLayout.EAST, mainPanel);
			panelLayout.putConstraint(SpringLayout.SOUTH, chartSettingsPanel, 1, SpringLayout.SOUTH, mainPanel);

			//Adds layout to the panel
			mainPanel.setLayout(panelLayout);

			//Sets Main Panel preferred size depending on isSystemLookAndFeelEnabled value:
			mainPanel.setPreferredSize(
					isSystemLookAndFeelEnabled?new Dimension(480,935):new Dimension(480,855));

			
			
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


	private JPanel createInputSettingsPanel(Core core){		
		
		//Loads current Parameters
		Parameters parameters = core.getParameters();
		
		/* Parameters Managed from Input Settings Panel */
		InputParameters inputParameters = parameters.getInputParameters();
		ChartOutputParameters chartOutputParameters = parameters.getChartOutputParameters();
		HashMap<String, Component> guiComponents = parameters.getGuiComponents();
		
		
		SpringLayout panelLayout = null; 	//Layout Object needed for
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
			final JLabel lblCalibration = new JLabel("<html><b>Calibration:</b></html>");
			
			/*Calibration ComboBox*/
			final JComboBox<String>	txtCalibration = new JComboBox<String>(inputParameters.getCalibrationsList()); //Initialized with default/saved Calibrations List
			txtCalibration.setEditable(true);
			
			/*Calibration ComboBox TextComponent*/
			final JTextComponent txtCalibrationTextComponent = (JTextComponent)txtCalibration.getEditor().getEditorComponent();
			txtCalibrationTextComponent.setDocument(new PathDocument()); //Sets a custom Document that supports PathChangeListener getting back method
			txtCalibrationTextComponent.setText(inputParameters.getCalibrationPath()); //Initialized with default/saved Calibration Path
			/*Listener (for changes in Calibration ComboBox TextComponent)*/
			txtCalibrationTextComponent.getDocument().addDocumentListener(
					new PathChangeListener("calibrationPath", txtCalibrationTextComponent, core));
			
			/*Calibration Browsing Button*/
			final JButton btnCalibrationBrowsing = new JButton("...");
			/*Listener (for clicks on Calibration Browsing Button)*/
			btnCalibrationBrowsing.addActionListener(
					new BrowseButtonListener(guiComponents, txtCalibrationTextComponent, 
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
			boolean isVideo = inputParameters.getInputSource().equals(InputParameters.VIDEO_INPUT);
			boolean isDevice = inputParameters.getInputSource().equals(InputParameters.DEVICE_INPUT);
				
			
			
			/*Source Label*/
			final JLabel lblSource = new JLabel("<html><b>Source</b></html>");
			
			
			
			/** VIDEO SOURCE **/
			
			
			/*Video Source OptionButton (to activate Video Source [File])*/
			final JRadioButton optVideoSource = new JRadioButton(isVideo?"<html><b>Video:</b></html>":"<html>Video:</html>");
			optVideoSource.setSelected(isVideo); //Startup selection based on default/loaded value
			/*Listener (for click on Video Source OptionButton)*/
			optVideoSource.addActionListener(
					new InputSourceOptionListener(InputParameters.VIDEO_INPUT, core));

			/*Video Source ComboBox (to select Video Source Path)*/
			final JComboBox<String>	txtVideoSource = new JComboBox<String>(inputParameters.getVideoPathsList()); //Initialized with default/saved Video Paths List
			txtVideoSource.setEditable(true);
			txtVideoSource.setEnabled(isVideo);
			
			/*Video Source ComboBox TextComponent*/
			final JTextComponent txtVideoSourceTextComponent = (JTextComponent) txtVideoSource.getEditor().getEditorComponent();
			txtVideoSourceTextComponent.setDocument(new PathDocument()); //Sets a custom Document that supports PathChangeListener getting back method
			txtVideoSourceTextComponent.setText(inputParameters.getVideoPath()); //Sets default/saved Video Source Path on Startup
			/*Listener (for changes in Video Source ComboBox TextComponent)*/
			txtVideoSourceTextComponent.getDocument().addDocumentListener(
					new PathChangeListener("videoPath", txtVideoSourceTextComponent, core));
			
			/*Video Source Browsing Button*/
			final JButton btnVideoSourceBrowsing = new JButton("...");
			btnVideoSourceBrowsing.setEnabled(isVideo);
			/*Listener (for clicks on Video Source Browsing Button)*/
			btnVideoSourceBrowsing.addActionListener(
					new BrowseButtonListener(guiComponents, txtVideoSourceTextComponent, 
										 "Open Video", 
										 new String[]{".avi",".mp4",".mjpeg"}, 
										 new String[]{"AVI Audio/Video Interleave (*.avi)","MPEG-4/H.264 Video (*.mp4)","Motion JPEG Video (*.mjpeg)","All supported media (*.mjpeg, *.mp4, *.avi)"},
										 true));


			
			/** DEVICE SOURCE **/
			
			
			/*Device Source OptionButton (to activate Device Source [Camera])*/
			final JRadioButton optDeviceSource = new JRadioButton(isDevice?"<html><b>Device:</b></html>":"<html>Device:</html>");
			optDeviceSource.setSelected(isDevice); //Startup selection based on default/loaded value	
			/*Listener (for click on Device Source OptionButton)*/
			optDeviceSource.addActionListener(
					new InputSourceOptionListener(InputParameters.DEVICE_INPUT, core));
			
			/*Device Source ComboBox (to select Device Source Path)*/
			final JComboBox<String>	txtDeviceSource = new JComboBox<String>(inputParameters.getDevicePathsList()); //Initialized with default/saved Device Paths List
			txtDeviceSource.setEditable(true);
			txtDeviceSource.setEnabled(isDevice); //Based on default/loaded value
			
			/*Device Source ComboBox TextComponent*/
			final JTextComponent txtDeviceSourceTextComponent = (JTextComponent) txtDeviceSource.getEditor().getEditorComponent();
			txtDeviceSourceTextComponent.setDocument(new PathDocument());  //Sets a custom Document that supports PathChangeListener getting back method
			txtDeviceSourceTextComponent.setText(inputParameters.getDevicePath()); //Sets default/saved Device Source Path on Startup
			/*Listener (for changes in Device Source ComboBox TextComponent)*/
			txtDeviceSourceTextComponent.getDocument().addDocumentListener(
					new PathChangeListener("devicePath", txtDeviceSourceTextComponent, core));
			
			
			
			/** DEVICE RESOLUTION **/
			
			
			/*Device Width Label*/
			final JLabel lblDeviceWidth = new JLabel(isDevice?"<html><b>Width:</b></html>":"<html>Width:</html>");
			lblDeviceWidth.setEnabled(isDevice);
			
			/*Device Width TextField*/
			final JTextField txtDeviceWidth = new JTextField(String.valueOf(inputParameters.getDeviceWidth()),4); //TextField initialized with default/saved Device Width as content and 4 columns
			txtDeviceWidth.setHorizontalAlignment(JTextField.CENTER);
			txtDeviceWidth.setEnabled(isDevice);
			/*Listener (for changes detection (through focus) on Device Width TextField)*/
			txtDeviceWidth.addFocusListener(
					new IntegerParameterTextFieldListener("deviceWidth", txtDeviceWidth, core));
			
			/*Device Height Label*/
			final JLabel lblDeviceHeight = new JLabel(isDevice?"<html><b>Height:</b></html>":"<html>Height:</html>");
			lblDeviceHeight.setEnabled(isDevice);
			
			/*Device Height TextField*/
			final JTextField txtDeviceHeight = new JTextField(String.valueOf(inputParameters.getDeviceHeight()),4); //TextField initialized with default/saved Device Height as content and 4 columns
			txtDeviceHeight.setHorizontalAlignment(JTextField.CENTER);
			txtDeviceHeight.setEnabled(isDevice);
			/*Listener (for changes detection (through focus) on Device Height TextField)*/
			txtDeviceHeight.addFocusListener(
					new IntegerParameterTextFieldListener("deviceHeight", txtDeviceHeight, core));
			
			
			
			/** DEVICE CONTROLS **/
			
			
			/*Loads default/saved Device Control CheckBox values at Startup*/
			boolean isDeviceSustainFramerateEnabled = inputParameters.isDevice_Control_SustainFramerate_Enabled();
			boolean isDeviceTimeoutImageIOEnabled = inputParameters.isDevice_Control_TimeoutImageIO_Enabled();
			boolean isDeviceKeepFormatEnabled = inputParameters.isDevice_Control_KeepFormat_Enabled();
			
			
			/*Device Sustain Framerate*/
			final JCheckBox chkDeviceSustainFramerate = new JCheckBox(isDeviceSustainFramerateEnabled?"<html><b>Sustain Framerate</b></html>":"<html>Sustain Framerate</html>");
			chkDeviceSustainFramerate.setSelected(isDeviceSustainFramerateEnabled); //Sets default/saved Device Sustain Framerate CheckBox value on Startup
			chkDeviceSustainFramerate.setEnabled(isDevice);	//If default/saved Input Source is DEVICE_INPUT("device") is enabled
			/*Listener (for Device Sustain Framerate CheckBox clicks)*/
			chkDeviceSustainFramerate.addActionListener(
					new ParameterCheckBoxListener("deviceSustainFramerate", chkDeviceSustainFramerate, core));
			
			/*Device Timeout Image I/O*/
			final JCheckBox chkDeviceTimeoutImageIO = new JCheckBox(isDeviceTimeoutImageIOEnabled?"<html><b>Timeout Image I/O</b></html>":"<html>Timeout Image I/O</html>");
			chkDeviceTimeoutImageIO.setSelected(isDeviceTimeoutImageIOEnabled); //Sets default/saved Device Timeout Image I/O CheckBox value on Startup
			chkDeviceTimeoutImageIO.setEnabled(isDevice); //If default/saved Input Source is DEVICE_INPUT("device") is enabled
			/*Listener (for Device Timeout Image I/O CheckBox clicks)*/
			chkDeviceTimeoutImageIO.addActionListener(
					new ParameterCheckBoxListener("deviceTimeoutImageIO", chkDeviceTimeoutImageIO, core));
			
			/*Device Keep Format*/
			final JCheckBox chkDeviceKeepFormat = new JCheckBox(isDeviceKeepFormatEnabled?"<html><b>Keep Format</b></html>":"<html>Keep Format</html>");
			chkDeviceKeepFormat.setSelected(isDeviceKeepFormatEnabled);	//Sets default/saved Device Keep Format CheckBox value on Startup
			chkDeviceKeepFormat.setEnabled(isDevice); //If default/saved Input Source is DEVICE_INPUT("device") is enabled
			/*Listener (for Device Keep Format CheckBox clicks)*/
			chkDeviceKeepFormat.addActionListener(
					new ParameterCheckBoxListener("deviceKeepFormat", chkDeviceKeepFormat, core));
			
			
			
			/** DEVICE ADJUSTMENTS PANEL - CREATION **/
			
			
			final JPanel deviceAdjustmentsPanel = new JPanel(); //Creates the panel that will contain all Device Adjustments Components
			deviceAdjustmentsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(isDevice?"<html><b>Device Adjustments</b></html>":"<html>Device Adjustments</html>"),
					  BorderFactory.createEmptyBorder(5,5,5,5))); //Sets a compound border (TitledBorder+EmptyBorder)
			
			deviceAdjustmentsPanel.setEnabled(isDevice); //If default/saved Input Source is DEVICE_INPUT("device") is enabled
			
			
			
			//Adds to the panel all the Device Adjustments Components: Device Width/Height Labels/TextField 
			//and Device Controls CheckBox 
			deviceAdjustmentsPanel.add(lblDeviceWidth);
			deviceAdjustmentsPanel.add(txtDeviceWidth);
			deviceAdjustmentsPanel.add(lblDeviceHeight);
			deviceAdjustmentsPanel.add(txtDeviceHeight);
			deviceAdjustmentsPanel.add(chkDeviceSustainFramerate);
			deviceAdjustmentsPanel.add(chkDeviceTimeoutImageIO);
			deviceAdjustmentsPanel.add(chkDeviceKeepFormat);
			
			
			
			/** DEVICE ADJUSTMENTS PANEL - DISPOSITION **/

			
			panelLayout = new SpringLayout(); //Initialize a new SpringLayout
			
			//SpringLayout Configuration:
			
			//Device Width/Height Label and TextField, and Device Sustain Framerate CheckBox
			//are disposed on the same row, close together
			panelLayout.putConstraint(SpringLayout.NORTH, lblDeviceWidth, 3,SpringLayout.NORTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblDeviceWidth, 3, SpringLayout.WEST, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtDeviceWidth, 0,SpringLayout.NORTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, txtDeviceWidth, 3, SpringLayout.EAST, lblDeviceWidth);
			panelLayout.putConstraint(SpringLayout.NORTH, lblDeviceHeight, 3,SpringLayout.NORTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblDeviceHeight, 3, SpringLayout.EAST, txtDeviceWidth);
			panelLayout.putConstraint(SpringLayout.NORTH, txtDeviceHeight, 0,SpringLayout.NORTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, txtDeviceHeight, 3, SpringLayout.EAST, lblDeviceHeight);
			panelLayout.putConstraint(SpringLayout.NORTH, chkDeviceSustainFramerate, 3, SpringLayout.NORTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chkDeviceSustainFramerate, 3, SpringLayout.EAST, txtDeviceHeight);
			
			//Device Timeout Image I/O CheckBox and Device Keep Format CheckBox are disposed on the second row, close together
			panelLayout.putConstraint(SpringLayout.NORTH, chkDeviceTimeoutImageIO, 8,SpringLayout.SOUTH, lblDeviceWidth);
			panelLayout.putConstraint(SpringLayout.WEST, chkDeviceTimeoutImageIO, 3, SpringLayout.WEST, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, chkDeviceKeepFormat, 8,SpringLayout.SOUTH, lblDeviceWidth);
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
				
				
			/*Full-Resolution Preview CheckBox (applies to input/output preview resolution)*/
			final JCheckBox chkFullResolutionPreview = new JCheckBox(chartOutputParameters.isFullResolutionPreview()?"<html><b>Full-Resolution Preview</b></html>":"<html>Full-Resolution Preview</html>");
			chkFullResolutionPreview.setSelected(chartOutputParameters.isFullResolutionPreview()); //Sets the CheckBox to the default/saved value on Startup
			/*Listener (for clicks on Full-Resolution Preview CheckBox)*/
			chkFullResolutionPreview.addActionListener(
					new ParameterCheckBoxListener("fullResolutionPreview", chkFullResolutionPreview, core));
				
			/*Input Preview Enabled CheckBox [Enable Input (Video file/Camera) Preview]*/
			final JCheckBox chkInputPreviewEnabled = new JCheckBox(inputParameters.isInputPreviewEnabled()?"<html><b>Enable Input Preview (Slower)</b></html>":"<html>Enable Input Preview (Slower)</html>");
			chkInputPreviewEnabled.setSelected(inputParameters.isInputPreviewEnabled()); //Sets the CheckBox to default/saved value on Startup
			/*Listener (for clicks on Input Preview Enabled CheckBox)*/
			chkInputPreviewEnabled.addActionListener(
					new ParameterCheckBoxListener("inputPreviewEnabled", chkInputPreviewEnabled, core));

			
			
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
			guiComponents.put("txtDeviceSource", txtDeviceSource); 
			guiComponents.put("lblDeviceWidth", lblDeviceWidth); 
			guiComponents.put("txtDeviceWidth", txtDeviceWidth); 
			guiComponents.put("lblDeviceHeight", lblDeviceHeight); 
			guiComponents.put("txtDeviceHeight", txtDeviceHeight); 
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

			//Sets a compound border (TitledBorder+EmptyBorder)
			inputSettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Input Settings</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,5)));

			//Adds to the panel all the components (Calibration, Source (Video/Device), Bottom Part)
			inputSettingsPanel.add(lblCalibration);
			inputSettingsPanel.add(txtCalibration);
			inputSettingsPanel.add(btnCalibrationBrowsing);
			inputSettingsPanel.add(lblSource);
			inputSettingsPanel.add(optVideoSource);
			inputSettingsPanel.add(txtVideoSource);
			inputSettingsPanel.add(btnVideoSourceBrowsing);
			inputSettingsPanel.add(optDeviceSource);
			inputSettingsPanel.add(txtDeviceSource);
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
			panelLayout.putConstraint(SpringLayout.NORTH, txtCalibration, -3,SpringLayout.NORTH, lblCalibration);
			panelLayout.putConstraint(SpringLayout.WEST, txtCalibration, 3, SpringLayout.EAST, lblCalibration);
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
			panelLayout.putConstraint(SpringLayout.WEST, optVideoSource, 10, SpringLayout.WEST, lblSource);
			panelLayout.putConstraint(SpringLayout.NORTH, txtVideoSource, 1,SpringLayout.NORTH, optVideoSource);
			panelLayout.putConstraint(SpringLayout.WEST, txtVideoSource, 0, SpringLayout.WEST, txtDeviceSource);	// lato sx di txt_video associato al sx di txt_device perchè lbl_video è più corta
			panelLayout.putConstraint(SpringLayout.EAST, txtVideoSource, -35, SpringLayout.EAST, inputSettingsPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, btnVideoSourceBrowsing, 0, SpringLayout.NORTH, txtVideoSource);
			panelLayout.putConstraint(SpringLayout.WEST, btnVideoSourceBrowsing, -30, SpringLayout.EAST, btnVideoSourceBrowsing);
			panelLayout.putConstraint(SpringLayout.EAST, btnVideoSourceBrowsing, -3, SpringLayout.EAST, inputSettingsPanel);
			panelLayout.putConstraint(SpringLayout.SOUTH, btnVideoSourceBrowsing, 0, SpringLayout.SOUTH, txtVideoSource);
			
			//Device Source OptionButton and Selection ComboBox are disposed
			//on the fourth row
			panelLayout.putConstraint(SpringLayout.NORTH, optDeviceSource, 7, SpringLayout.SOUTH, optVideoSource);
			panelLayout.putConstraint(SpringLayout.WEST, optDeviceSource, 10, SpringLayout.WEST, lblSource);
			panelLayout.putConstraint(SpringLayout.NORTH, txtDeviceSource, 1,SpringLayout.NORTH, optDeviceSource);
			panelLayout.putConstraint(SpringLayout.WEST, txtDeviceSource, 3, SpringLayout.EAST, optDeviceSource);
			panelLayout.putConstraint(SpringLayout.EAST, txtDeviceSource, -3, SpringLayout.EAST, inputSettingsPanel);
	
			//Device Adjustments Panel is disposed on the fifth row 
			panelLayout.putConstraint(SpringLayout.NORTH, deviceAdjustmentsPanel, 7, SpringLayout.SOUTH, optDeviceSource);
			panelLayout.putConstraint(SpringLayout.WEST, deviceAdjustmentsPanel, 5, SpringLayout.WEST, optDeviceSource);
			panelLayout.putConstraint(SpringLayout.EAST, deviceAdjustmentsPanel, -5, SpringLayout.EAST, inputSettingsPanel);
			
			//Bottom Components are disposed on the sixth row
			panelLayout.putConstraint(SpringLayout.NORTH, chkFullResolutionPreview, 5,SpringLayout.SOUTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chkFullResolutionPreview, 0, SpringLayout.WEST, lblSource);
			panelLayout.putConstraint(SpringLayout.NORTH, chkInputPreviewEnabled, 5,SpringLayout.SOUTH, deviceAdjustmentsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chkInputPreviewEnabled, 3, SpringLayout.EAST, chkFullResolutionPreview);

			//Input Settings Panel height is defined by constraining its bottom to the bottom of the sixth row
			panelLayout.putConstraint(SpringLayout.SOUTH, inputSettingsPanel, 0, SpringLayout.SOUTH, chkFullResolutionPreview);


			//Applying the Layout to the Input Settings Panel
			inputSettingsPanel.setLayout(panelLayout);

			
			return inputSettingsPanel;
	}

	

	private JPanel createInternalImageSettingsPanel(Core core){
		
		//Loads current Parameters
		Parameters parameters = core.getParameters();
		
		/* Parameters Managed from Internal Image Settings Panel */
		InternalImageParameters internalImageParameters = parameters.getInternalImageParameters();
		ChartOutputParameters chartOutputParameters = parameters.getChartOutputParameters();
		HashMap<String, Component> guiComponents = parameters.getGuiComponents();
		
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
			
			/*Image Type ComboBox*/
			final JComboBox<String>	txtImageType = new JComboBox<String>();		
			//Loads default/saved Image Types List to populate the ComboBox 
			//and default/saved selected Image Type
			for(int i=0; i<internalImageParameters.getImageTypesList().length; i++){
				//Adds current Image Type to the list
				txtImageType.addItem(internalImageParameters.getImageTypesList()[i].getImageTypeName());
			}
			//Selects the default/saved Image Type
			txtImageType.setSelectedItem(internalImageParameters.getImageType().getImageTypeName());
			/*Listener*/
			txtImageType.addActionListener(new ImageTypeChangeListener(txtImageType, core));
			
			
			
		/**
		  *  2. IMAGE RESIZE PART
		  *  
		  *  Second and central part of the Internal Image Settings Panel. Contains Image Resize
		  *  components and Internal Image Preview CheckBox.
		  */
			
			
			/*Image Resize Label*/
			final JLabel lblImageResize = new JLabel("<html><b>Resize:</b></html>");
		
			/*Image Keep Original CheckBox*/
			final JCheckBox chkImageKeepOriginal = new JCheckBox(
					internalImageParameters.isImageKeepOriginal()?"<html><b>Keep original aspect</b></html>":"<html>Keep original aspect</html>");
			chkImageKeepOriginal.setSelected(internalImageParameters.isImageKeepOriginal());		
			/*Listener*/
			chkImageKeepOriginal.addActionListener(
					new ParameterCheckBoxListener("imageKeepOriginal", chkImageKeepOriginal, core));

			/*Internal Image Preview CheckBox*/
			final JCheckBox chkInternalImagePreview = new JCheckBox(
					chartOutputParameters.isInternalImagePreview()?"<html><b>Preview Internal Image (Slower)</b></html>":"<html>Preview Internal Image (Slower)</html>");
			chkInternalImagePreview.setSelected(chartOutputParameters.isInternalImagePreview());
			/*Listener*/
			chkInternalImagePreview.addActionListener(
					new ParameterCheckBoxListener("internalImagePreview", chkInternalImagePreview, core));

			/*Image Resize Width Label*/
			final JLabel lblImageResizeWidth = new JLabel(
					internalImageParameters.isImageKeepOriginal()?"<html>Width:</html>":"<html><b>Width:</b></html>");
			
			/*Image Resize Width TextField*/
			final JTextField txtImageResizeWidth = new JTextField(
					Integer.toString(internalImageParameters.getImageResizeWidth()),4);
			txtImageResizeWidth.setHorizontalAlignment(JTextField.CENTER);
			txtImageResizeWidth.setEnabled(!internalImageParameters.isImageKeepOriginal());
			/*Listener*/
			txtImageResizeWidth.addFocusListener(
					new IntegerParameterTextFieldListener("imageResizeWidth", txtImageResizeWidth, core));
			
			/*Image Resize Height Label*/
			final JLabel lblImageResizeHeight = new JLabel(
					internalImageParameters.isImageKeepOriginal()?"<html>Height:</html>":"<html><b>Height:</b></html>");
			
			/*Image Resize Height TextField*/
			final JTextField txtImageResizeHeight = new JTextField(
					Integer.toString(internalImageParameters.getImageResizeHeight()),4);
			txtImageResizeHeight.setHorizontalAlignment(JTextField.CENTER);
			txtImageResizeHeight.setEnabled(!internalImageParameters.isImageKeepOriginal());
			/*Listener*/
			txtImageResizeHeight.addFocusListener(
					new IntegerParameterTextFieldListener("imageResizeHeight", txtImageResizeHeight, core));
			
			
			
		/**
		  *  3. BOTTOM PART (Frame Decimate, Image Buffer Size)
		  *  
		  *  Last part of the Internal Image Settings Panel: contains the Frame Decimate component
		  *  and the Image Buffer Size component.
		  */
			
			
			/*Frame Decimate Enabled CheckBox*/
			final JCheckBox chkFrameDecimateEnabled = new JCheckBox(
					internalImageParameters.isFrameDecimateEnabled()?"<html><b>Decimate frames by</b></html>":"<html>Decimate frames by</html>");
			chkFrameDecimateEnabled.setSelected(internalImageParameters.isFrameDecimateEnabled());
			/*Listener*/
			chkFrameDecimateEnabled.addActionListener(
					new ParameterCheckBoxListener("frameDecimateEnabled", chkFrameDecimateEnabled, core));
			
			/*Frame Decimate Value TextField*/
			final JTextField txtFrameDecimateValue = new JTextField(
					String.valueOf(internalImageParameters.getFrameDecimateValue()),3);
			txtFrameDecimateValue.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtFrameDecimateValue.addFocusListener(
					new IntegerParameterTextFieldListener("frameDecimateValue", txtFrameDecimateValue, core));

			
			//Loads default/saved inputSource value
			boolean isDevice = core.getParameters().getInputParameters().getInputSource().equals(InputParameters.DEVICE_INPUT);
			
			
			/*Image Buffer Size Label*/
			final JLabel lblImageBufferSize = new JLabel("<html>Image Buffer Size (Device only):</html>");
			lblImageBufferSize.setEnabled(isDevice);
			
			/*Image Buffer Size TextField*/
			final JTextField txtImageBufferSize = new JTextField(
					internalImageParameters.getImageBufferSize()==InternalImageParameters.INFINITEBUFFER?
					"Infinity":String.valueOf(internalImageParameters.getImageBufferSize()),4);
			txtImageBufferSize.setHorizontalAlignment(JTextField.CENTER);
			txtImageBufferSize.setEditable(true);
			txtImageBufferSize.setEnabled(isDevice);
			/*Listener*/
			txtImageBufferSize.addFocusListener(
					new IntegerParameterTextFieldListener("imageBufferSize", txtImageBufferSize, core));

			
			
		/**
		  *  4. POPULATE GUICOMPONENTS
		  * 
		  *  Adds all the most important (and reused) components to the guiComponents HashMap
		  *  
		  */

			
			guiComponents.put("txtImageType", txtImageType);
			guiComponents.put("chkImageKeepOriginal", chkImageKeepOriginal);
			guiComponents.put("chkInternalImagePreview", chkInternalImagePreview);
			guiComponents.put("lblImageResizeWidth", lblImageResizeWidth);
			guiComponents.put("txtImageResizeWidth", txtImageResizeWidth);
			guiComponents.put("lblImageResizeHeight", lblImageResizeHeight);
			guiComponents.put("txtImageResizeHeight", txtImageResizeHeight);
			guiComponents.put("lblImageBufferSize", lblImageBufferSize);
			guiComponents.put("txtImageBufferSize", txtImageBufferSize);
			guiComponents.put("chkFrameDecimateEnabled", chkFrameDecimateEnabled);
			guiComponents.put("txtFrameDecimateValue", txtFrameDecimateValue);

			
			
		/**
		  *  5. INTERNAL IMAGE SETTINGS PANEL CREATION
		  * 
		  *  Creation of the Internal Image Settings Panel.
		  *  
		  */

			
			final JPanel internalImageSettingsPanel = new JPanel();

			//Sets a compound border (TitledBorder+EmptyBorder)
			internalImageSettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Internal Images Settings:</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,5)));
			
			//Adds to the panel all the components (Image Type, Image Resize,
			//Image Keep Original, Internal Image Preview, and Bottom Part [Frame Decimate, Image Buffer Size])
			internalImageSettingsPanel.add(lblImageType);
			internalImageSettingsPanel.add(txtImageType);
			internalImageSettingsPanel.add(lblImageResize);
			internalImageSettingsPanel.add(chkImageKeepOriginal);
			internalImageSettingsPanel.add(chkInternalImagePreview);
			internalImageSettingsPanel.add(lblImageResizeWidth);
			internalImageSettingsPanel.add(txtImageResizeWidth);
			internalImageSettingsPanel.add(lblImageResizeHeight);
			internalImageSettingsPanel.add(txtImageResizeHeight);
			internalImageSettingsPanel.add(chkFrameDecimateEnabled);
			internalImageSettingsPanel.add(txtFrameDecimateValue);
			internalImageSettingsPanel.add(lblImageBufferSize);
			internalImageSettingsPanel.add(txtImageBufferSize);
			


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
			panelLayout.putConstraint(SpringLayout.NORTH, txtImageType, -3,SpringLayout.NORTH, lblImageType);
			panelLayout.putConstraint(SpringLayout.WEST, txtImageType, 3, SpringLayout.EAST, lblImageType);
			panelLayout.putConstraint(SpringLayout.EAST, txtImageType, -3, SpringLayout.EAST, internalImageSettingsPanel);
			
			//On the second row Image Resize Label
			panelLayout.putConstraint(SpringLayout.NORTH, lblImageResize, 10, SpringLayout.SOUTH, lblImageType);
			panelLayout.putConstraint(SpringLayout.WEST, lblImageResize, 3, SpringLayout.WEST, internalImageSettingsPanel);
			
			//On the third row Image Keep Original and Internal Image Preview CheckBox
			panelLayout.putConstraint(SpringLayout.NORTH, chkImageKeepOriginal, 5, SpringLayout.SOUTH, lblImageResize);
			panelLayout.putConstraint(SpringLayout.WEST, chkImageKeepOriginal, 10, SpringLayout.WEST, lblImageResize);
			panelLayout.putConstraint(SpringLayout.NORTH, chkInternalImagePreview, 5, SpringLayout.SOUTH, lblImageResize);
			panelLayout.putConstraint(SpringLayout.WEST, chkInternalImagePreview, 3, SpringLayout.EAST, chkImageKeepOriginal);
			
			//On the fourth row Image Resize Width and Height components
			panelLayout.putConstraint(SpringLayout.NORTH, lblImageResizeWidth, 5,SpringLayout.SOUTH, chkImageKeepOriginal);
			panelLayout.putConstraint(SpringLayout.WEST, lblImageResizeWidth, 10, SpringLayout.WEST, lblImageResize);	
			panelLayout.putConstraint(SpringLayout.NORTH, txtImageResizeWidth, -1,SpringLayout.NORTH, lblImageResizeWidth);
			panelLayout.putConstraint(SpringLayout.WEST, txtImageResizeWidth, 3, SpringLayout.EAST, lblImageResizeWidth);	
			panelLayout.putConstraint(SpringLayout.NORTH, lblImageResizeHeight, 0,SpringLayout.NORTH, lblImageResizeWidth);
			panelLayout.putConstraint(SpringLayout.WEST, lblImageResizeHeight, 3, SpringLayout.EAST, txtImageResizeWidth);	
			panelLayout.putConstraint(SpringLayout.NORTH, txtImageResizeHeight, -1,SpringLayout.NORTH, lblImageResizeHeight);
			panelLayout.putConstraint(SpringLayout.WEST, txtImageResizeHeight, 3, SpringLayout.EAST, lblImageResizeHeight);	
	
			//On the fifth row Frame Decimate CheckBox and TextField
			panelLayout.putConstraint(SpringLayout.NORTH, chkFrameDecimateEnabled, 15, SpringLayout.SOUTH, lblImageResizeWidth);
			panelLayout.putConstraint(SpringLayout.WEST, chkFrameDecimateEnabled, 3, SpringLayout.WEST, internalImageSettingsPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtFrameDecimateValue, 0, SpringLayout.NORTH, chkFrameDecimateEnabled);
			panelLayout.putConstraint(SpringLayout.WEST, txtFrameDecimateValue, 5, SpringLayout.EAST, chkFrameDecimateEnabled);
	
			//On the sixth row Image Buffer Size Label and TextField
			panelLayout.putConstraint(SpringLayout.NORTH, lblImageBufferSize, 10, SpringLayout.SOUTH, chkFrameDecimateEnabled);
			panelLayout.putConstraint(SpringLayout.WEST, lblImageBufferSize, 3, SpringLayout.WEST, internalImageSettingsPanel);	
			panelLayout.putConstraint(SpringLayout.NORTH, txtImageBufferSize, -1,SpringLayout.NORTH, lblImageBufferSize);
			panelLayout.putConstraint(SpringLayout.WEST, txtImageBufferSize, 3, SpringLayout.EAST, lblImageBufferSize);	
			
			//Internal Image Settings Panel height is defined by constraining its bottom to the bottom of the sixth row
			panelLayout.putConstraint(SpringLayout.SOUTH, internalImageSettingsPanel, 0, SpringLayout.SOUTH, lblImageBufferSize);
	
			//Applying the Layout to the Panel
			internalImageSettingsPanel.setLayout(panelLayout);

			return internalImageSettingsPanel;
	}
	
	
	
	private JPanel createTrackerSettingsPanel(Core core){
	
		//Loads current Parameters
		Parameters parameters = core.getParameters();

		/* Parameters Managed from Tracker Settings Panel */
		TrackerParameters trackerParameters = parameters.getTrackerParameters();
		HashMap<String, Component> guiComponents = parameters.getGuiComponents();

		SpringLayout panelLayout = null; //Layout Object needed for components disposition

			

		/***********************
		 * COMPONENTS CREATION *
		 ***********************/
			
		
		
		/**
		  *  1. TRACKER TYPE PART
		  *  
		  *  First and upper part of the Tracker Settings Panel.
		  *  
		  */

		
			/*Tracker Type Label*/
			final JLabel lblTrackerType = new JLabel("<html><b>Tracker Type:</b></html>");
			
			/*Tracker Type ComboBox*/
			final JComboBox<String>	txtTrackerType = new JComboBox<String>(
					trackerParameters.getTrackerTypeNames().values().toArray(new String[]{}));
			txtTrackerType.setSelectedItem(trackerParameters.getTrackerTypeNames().get(trackerParameters.getTrackerType()));
			/*Listener*/
			txtTrackerType.addActionListener(
					new TrackerTypeChangeListener(txtTrackerType, core));
			

			
		/**
		  *  2. KLT/SURF TRACKER COMPONENTS AND PANELS
		  *  
		  *  Second part of the Tracker Settings Panel.
		  *  
		  */

			
			/*KLT Tracker Components*/
			
			/*KLT Tracker templateRadius Label*/
			final JLabel lblKltTracker_templateRadius = new JLabel("<html>Template Radius:</html>");
			
			/*KLT Tracker templateRadius TextField*/
			final JTextField txtKltTracker_templateRadius = new JTextField(
					String.valueOf(trackerParameters.getKltTracker_templateRadius()),5);
			txtKltTracker_templateRadius.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtKltTracker_templateRadius.addFocusListener(
					new IntegerParameterTextFieldListener("kltTracker_templateRadius", txtKltTracker_templateRadius, core));

			/*KLT Tracker pyramidScaling Label*/
			final JLabel lblKltTracker_pyramidScaling = new JLabel("<html>Pyramid Scaling:</html>");
			
			/*KLT Tracker pyramidScaling TextField*/
			final JTextField txtKltTracker_pyramidScaling = new JTextField(
					trackerParameters.getKltTracker_pyramidScaling(),5);		
			txtKltTracker_pyramidScaling.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtKltTracker_pyramidScaling.addFocusListener(
					new StringParameterTextFieldListener("kltTracker_pyramidScaling", txtKltTracker_pyramidScaling, core));
			
			/*KLT Tracker maxFeatures Label*/			
			final JLabel lblKltTracker_maxFeatures = new JLabel("<html>Max Features:</html>");
			
			/*KLT Tracker maxFeatures TextField*/
			final JTextField txtKltTracker_maxFeatures = new JTextField(
					String.valueOf(trackerParameters.getKltTracker_maxFeatures()),5);		
			txtKltTracker_maxFeatures.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtKltTracker_maxFeatures.addFocusListener(
					new IntegerParameterTextFieldListener("kltTracker_maxFeatures", txtKltTracker_maxFeatures, core));
			
			/*KLT Tracker radius Label*/
			final JLabel lblKltTracker_radius = new JLabel("<html>Radius:</html>");
			
			/*KLT Tracker radius TextField*/
			final JTextField txtKltTracker_radius = new JTextField(
					String.valueOf(trackerParameters.getKltTracker_radius()),5);		
			txtKltTracker_radius.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtKltTracker_radius.addFocusListener(
					new IntegerParameterTextFieldListener("kltTracker_radius", txtKltTracker_radius, core));
			
			/*KLT Tracker threshold Label*/
			final JLabel lblKltTracker_threshold = new JLabel("<html>Threshold:</html>");
			
			/*KLT Tracker threshold TextField*/
			final JTextField txtKltTracker_threshold = new JTextField(
					String.valueOf(trackerParameters.getKltTracker_threshold()),5);		
			txtKltTracker_threshold.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtKltTracker_threshold.addFocusListener(
					new FloatParameterTextFieldListener("kltTracker_threshold", txtKltTracker_threshold, core));
						
			
			
			/** KLT TRACKER PANEL - CREATION **/
			
			
			final JPanel kltTrackerPanel = new JPanel();
			kltTrackerPanel.setVisible(trackerParameters.getTrackerType().equals(TrackerParameters.DEFAULT_TRACKER)||
					 				   trackerParameters.getTrackerType().equals(TrackerParameters.KLT)||
					 				   trackerParameters.getTrackerType().equals(TrackerParameters.KLT2));
			kltTrackerPanel.setEnabled(!trackerParameters.getTrackerType().equals(TrackerParameters.DEFAULT_TRACKER));//If default tracker(klt2 with default parameters) is selected, disables klt settings panel
			
			
			//Adds all components to the panel (KLT templateRadius, pyramidScaling,
			//maxFeatures, radius and threshold)
			kltTrackerPanel.add(lblKltTracker_templateRadius);
			kltTrackerPanel.add(txtKltTracker_templateRadius);
			kltTrackerPanel.add(lblKltTracker_pyramidScaling);
			kltTrackerPanel.add(txtKltTracker_pyramidScaling);
			kltTrackerPanel.add(lblKltTracker_maxFeatures);
			kltTrackerPanel.add(txtKltTracker_maxFeatures);
			kltTrackerPanel.add(lblKltTracker_radius);
			kltTrackerPanel.add(txtKltTracker_radius);
			kltTrackerPanel.add(lblKltTracker_threshold);
			kltTrackerPanel.add(txtKltTracker_threshold);
			
			
			//Each component in klt settings panel is enabled/disabled depending on default/saved parameter
			for(Component comp: kltTrackerPanel.getComponents()){	
				comp.setEnabled(!trackerParameters.getTrackerType().equals(TrackerParameters.DEFAULT_TRACKER));
			}
			
			
			/** KLT TRACKER PANEL - DISPOSITION **/
			
			
			panelLayout = new SpringLayout();
			
			//On the first row KLT Tracker templateRadius and pyramidScaling Label and TextField 
			panelLayout.putConstraint(SpringLayout.NORTH, lblKltTracker_templateRadius, 5, SpringLayout.NORTH, kltTrackerPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblKltTracker_templateRadius, 0, SpringLayout.WEST, kltTrackerPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtKltTracker_templateRadius, -1,SpringLayout.NORTH, lblKltTracker_templateRadius);
			panelLayout.putConstraint(SpringLayout.WEST, txtKltTracker_templateRadius, 3, SpringLayout.EAST, lblKltTracker_templateRadius);
			panelLayout.putConstraint(SpringLayout.NORTH, lblKltTracker_pyramidScaling, 0, SpringLayout.NORTH, lblKltTracker_templateRadius);
			panelLayout.putConstraint(SpringLayout.WEST, lblKltTracker_pyramidScaling, 3, SpringLayout.EAST, txtKltTracker_templateRadius);
			panelLayout.putConstraint(SpringLayout.NORTH, txtKltTracker_pyramidScaling, -1,SpringLayout.NORTH, lblKltTracker_pyramidScaling);
			panelLayout.putConstraint(SpringLayout.WEST, txtKltTracker_pyramidScaling, 3, SpringLayout.EAST, lblKltTracker_pyramidScaling);
			
			//On the second row KLT Tracker maxFeatures, radius and threshold
			panelLayout.putConstraint(SpringLayout.NORTH, lblKltTracker_maxFeatures, 10, SpringLayout.SOUTH, lblKltTracker_templateRadius);
			panelLayout.putConstraint(SpringLayout.WEST, lblKltTracker_maxFeatures, 0, SpringLayout.WEST, kltTrackerPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtKltTracker_maxFeatures, -1,SpringLayout.NORTH, lblKltTracker_maxFeatures);
			panelLayout.putConstraint(SpringLayout.WEST, txtKltTracker_maxFeatures, 0, SpringLayout.WEST, txtKltTracker_templateRadius);
			panelLayout.putConstraint(SpringLayout.NORTH, lblKltTracker_radius, 0, SpringLayout.NORTH, lblKltTracker_maxFeatures);
			panelLayout.putConstraint(SpringLayout.WEST, lblKltTracker_radius, 3, SpringLayout.EAST, txtKltTracker_maxFeatures);
			panelLayout.putConstraint(SpringLayout.NORTH, txtKltTracker_radius, -1,SpringLayout.NORTH, lblKltTracker_radius);
			panelLayout.putConstraint(SpringLayout.WEST, txtKltTracker_radius, 3, SpringLayout.EAST, lblKltTracker_radius);
			panelLayout.putConstraint(SpringLayout.NORTH, lblKltTracker_threshold, 0, SpringLayout.NORTH, lblKltTracker_radius);
			panelLayout.putConstraint(SpringLayout.WEST, lblKltTracker_threshold, 3, SpringLayout.EAST, txtKltTracker_radius);
			panelLayout.putConstraint(SpringLayout.NORTH, txtKltTracker_threshold, -1,SpringLayout.NORTH, lblKltTracker_threshold);
			panelLayout.putConstraint(SpringLayout.WEST, txtKltTracker_threshold, 3, SpringLayout.EAST, lblKltTracker_threshold);
	
			//The panel height is constrained to the second row bottom
			panelLayout.putConstraint(SpringLayout.SOUTH, kltTrackerPanel, 0, SpringLayout.SOUTH, txtKltTracker_maxFeatures);
			
			//Applies the layout to the panel
			kltTrackerPanel.setLayout(panelLayout);
			
			
			
			/*SURF Tracker Components*/

			/*SURF Tracker maxFeaturesPerScale Label*/
			final JLabel lblSurfTracker_maxFeaturesPerScale = new JLabel("<html>Max Features Per Scale:</html>");
			
			/*SURF Tracker maxFeaturesPerScale TextField*/
			final JTextField txtSurfTracker_maxFeaturesPerScale = new JTextField(
					String.valueOf(trackerParameters.getSurfTracker_maxFeaturesPerScale()),5);		
			txtSurfTracker_maxFeaturesPerScale.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtSurfTracker_maxFeaturesPerScale.addFocusListener(
					new IntegerParameterTextFieldListener("surfTracker_maxFeaturesPerScale", txtSurfTracker_maxFeaturesPerScale, core));
						
			/*SURF Tracker extractRadius Label*/
			final JLabel lblSurfTracker_extractRadius = new JLabel("<html>Extract Radius:</html>");
			
			/*SURF Tracker extractRadius TextField*/
			final JTextField txtSurfTracker_extractRadius = new JTextField(
					String.valueOf(trackerParameters.getSurfTracker_extractRadius()),5);		
			txtSurfTracker_extractRadius.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtSurfTracker_extractRadius.addFocusListener(
					new IntegerParameterTextFieldListener("surfTracker_extractRadius", txtSurfTracker_extractRadius, core));
			
			/*SURF Tracker initialSampleSize Label*/
			final JLabel lblSurfTracker_initialSampleSize = new JLabel("<html>Initial Sample Size:</html>");
			
			/*SURF Tracker initialSampleSize TextField*/
			final JTextField txtSurfTracker_initialSampleSize = new JTextField(
					String.valueOf(trackerParameters.getSurfTracker_initialSampleSize()),5);		
			txtSurfTracker_initialSampleSize.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtSurfTracker_initialSampleSize.addFocusListener(
					new IntegerParameterTextFieldListener("surfTracker_initialSampleSize", txtSurfTracker_initialSampleSize, core));
						
			
			
			/** SURF TRACKER PANEL - CREATION **/
			
			
			final JPanel surfTrackerPanel = new JPanel();
			surfTrackerPanel.setVisible(trackerParameters.getTrackerType().equals(TrackerParameters.SURF)||
										trackerParameters.getTrackerType().equals(TrackerParameters.SURF2));
						
			
			//Adds all components to the panel (SURF maxFeaturesPerScale,
			//extractRadius and initialSampleSize)
			surfTrackerPanel.add(lblSurfTracker_maxFeaturesPerScale);
			surfTrackerPanel.add(txtSurfTracker_maxFeaturesPerScale);
			surfTrackerPanel.add(lblSurfTracker_extractRadius);
			surfTrackerPanel.add(txtSurfTracker_extractRadius);
			surfTrackerPanel.add(lblSurfTracker_initialSampleSize);
			surfTrackerPanel.add(txtSurfTracker_initialSampleSize);
			
	
			
			/** SURF TRACKER PANEL - DISPOSITION **/
			
			
			panelLayout = new SpringLayout();
			
			//On the first row SURF Tracker maxFeaturesPerScale and extractRadius
			panelLayout.putConstraint(SpringLayout.NORTH, lblSurfTracker_maxFeaturesPerScale, 5, SpringLayout.NORTH, surfTrackerPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblSurfTracker_maxFeaturesPerScale, 0, SpringLayout.WEST, surfTrackerPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtSurfTracker_maxFeaturesPerScale, -1,SpringLayout.NORTH, lblSurfTracker_maxFeaturesPerScale);
			panelLayout.putConstraint(SpringLayout.WEST, txtSurfTracker_maxFeaturesPerScale, 3, SpringLayout.EAST, lblSurfTracker_maxFeaturesPerScale);
			panelLayout.putConstraint(SpringLayout.NORTH, lblSurfTracker_extractRadius, 0, SpringLayout.NORTH, lblSurfTracker_maxFeaturesPerScale);
			panelLayout.putConstraint(SpringLayout.WEST, lblSurfTracker_extractRadius, 3, SpringLayout.EAST, txtSurfTracker_maxFeaturesPerScale);
			panelLayout.putConstraint(SpringLayout.NORTH, txtSurfTracker_extractRadius, -1,SpringLayout.NORTH, lblSurfTracker_extractRadius);
			panelLayout.putConstraint(SpringLayout.WEST, txtSurfTracker_extractRadius, 3, SpringLayout.EAST, lblSurfTracker_extractRadius);

			//On the second row SURF Tracker initialSampleSize
			panelLayout.putConstraint(SpringLayout.NORTH, lblSurfTracker_initialSampleSize, 10, SpringLayout.SOUTH, lblSurfTracker_maxFeaturesPerScale);
			panelLayout.putConstraint(SpringLayout.WEST, lblSurfTracker_initialSampleSize, 0, SpringLayout.WEST, surfTrackerPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtSurfTracker_initialSampleSize, -1,SpringLayout.NORTH, lblSurfTracker_initialSampleSize);
			panelLayout.putConstraint(SpringLayout.WEST, txtSurfTracker_initialSampleSize, 3, SpringLayout.EAST, lblSurfTracker_initialSampleSize);
	
			//The height of the panel is constrained to the bottom of the second row
			panelLayout.putConstraint(SpringLayout.SOUTH, surfTrackerPanel, 0, SpringLayout.SOUTH, txtSurfTracker_initialSampleSize);
			
			//Adds the layout to the panel
			surfTrackerPanel.setLayout(panelLayout);
			
			
			
		/**
		  *  3. BOTTOM PART (Tracker Show Active Tracks+Tracker Show New Tracks)
		  *  
		  *  Last part of the Tracker Settings Panel.
		  *  
		  */


			/*Tracker Show Active Tracks CheckBox*/
			final JCheckBox chkTrackerShowActiveTracks = 
					new JCheckBox(trackerParameters.isTrackerShowActiveTracks()?"<html><b>Show Active Tracks</b></html>":"<html>Show Active Tracks</html>");
			chkTrackerShowActiveTracks.setSelected(trackerParameters.isTrackerShowActiveTracks());		
			/*Listener*/
			chkTrackerShowActiveTracks.addActionListener(
					new ParameterCheckBoxListener("trackerShowActiveTracks", chkTrackerShowActiveTracks, core));
	
			/*Tracker Show New Tracks CheckBox*/
			final JCheckBox chkTrackerShowNewTracks = 
					new JCheckBox(trackerParameters.isTrackerShowNewTracks()?"<html><b>Show New Tracks</b></html>":"<html>Show New Tracks</html>");
			chkTrackerShowNewTracks.setSelected(trackerParameters.isTrackerShowNewTracks());		
			/*Listener*/
			chkTrackerShowNewTracks.addActionListener(
					new ParameterCheckBoxListener("trackerShowNewTracks", chkTrackerShowNewTracks, core));
	

			
		/**
		  *  4. POPULATE GUICOMPONENTS
		  * 
		  *  Adds all the most important (and reused) components to the guiComponents HashMap
		  *  
		  */

			
			guiComponents.put("txtTrackerType", txtTrackerType);
			guiComponents.put("txtKltTracker_templateRadius", txtKltTracker_templateRadius);
			guiComponents.put("txtKltTracker_pyramidScaling", txtKltTracker_pyramidScaling);
			guiComponents.put("txtKltTracker_maxFeatures", txtKltTracker_maxFeatures);
			guiComponents.put("txtKltTracker_radius", txtKltTracker_radius);
			guiComponents.put("txtKltTracker_threshold", txtKltTracker_threshold);
			guiComponents.put("kltTrackerPanel", kltTrackerPanel);
			guiComponents.put("txtSurfTracker_maxFeaturesPerScale", txtSurfTracker_maxFeaturesPerScale);
			guiComponents.put("txtSurfTracker_extractRadius", txtSurfTracker_extractRadius);
			guiComponents.put("txtSurfTracker_initialSampleSize", txtSurfTracker_initialSampleSize);
			guiComponents.put("surfTrackerPanel",surfTrackerPanel);
			guiComponents.put("chkTrackerShowActiveTracks", chkTrackerShowActiveTracks);
			guiComponents.put("chkTrackerShowNewTracks", chkTrackerShowNewTracks);

			
			
		/**
		  *  5. TRACKER SETTINGS PANEL CREATION
		  * 
		  *  Creation of the Tracker Settings Panel.
		  *  
		  */

			
			//Sets a compound border (TitledBorder+EmptyBorder)
			final JPanel trackerSettingsPanel = new JPanel();
			trackerSettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Tracker Settings:</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,0)));
			
			//Adds to the panel all the components (Tracker Type, KLT and SURF Trackers Panels,
			//Tracker Show Active Tracks and Tracker Show New Tracks)
			trackerSettingsPanel.add(lblTrackerType);
			trackerSettingsPanel.add(txtTrackerType);
			trackerSettingsPanel.add(kltTrackerPanel);
			trackerSettingsPanel.add(surfTrackerPanel);
			trackerSettingsPanel.add(chkTrackerShowActiveTracks);
			trackerSettingsPanel.add(chkTrackerShowNewTracks);
			


		/**************************
		 * COMPONENTS DISPOSITION *
		 *************************/



		/**
		  *  6. TRACKER SETTINGS PANEL DISPOSITION
		  * 
		  *  Disposition of the components inside Tracker Settings Panel.
		  *  
		  */
				
			
			panelLayout = new SpringLayout();
			
			//On the first row Tracker Type Label and ComboBox
			panelLayout.putConstraint(SpringLayout.NORTH, lblTrackerType, 0, SpringLayout.NORTH, trackerSettingsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblTrackerType, 3, SpringLayout.WEST, trackerSettingsPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtTrackerType, -3,SpringLayout.NORTH, lblTrackerType);
			panelLayout.putConstraint(SpringLayout.WEST, txtTrackerType, 3, SpringLayout.EAST, lblTrackerType);
			panelLayout.putConstraint(SpringLayout.EAST, txtTrackerType, -3, SpringLayout.EAST, trackerSettingsPanel);
	
			//On the second row KLT Tracker Panel (overlapped with SURF Tracker Panel, one at a time is visible)
			panelLayout.putConstraint(SpringLayout.NORTH, kltTrackerPanel, 5, SpringLayout.SOUTH, lblTrackerType);
			panelLayout.putConstraint(SpringLayout.WEST, kltTrackerPanel, 3, SpringLayout.WEST, trackerSettingsPanel);
			panelLayout.putConstraint(SpringLayout.EAST, kltTrackerPanel, 0, SpringLayout.EAST, trackerSettingsPanel);
			
			//On the second row SURF Tracker Panel (in the same position of KLT Tracker Panel, one at a time is visible)
			panelLayout.putConstraint(SpringLayout.NORTH, surfTrackerPanel, 5, SpringLayout.SOUTH, lblTrackerType);
			panelLayout.putConstraint(SpringLayout.WEST, surfTrackerPanel, 3, SpringLayout.WEST, trackerSettingsPanel);
			panelLayout.putConstraint(SpringLayout.EAST, surfTrackerPanel, 0, SpringLayout.EAST, trackerSettingsPanel);
		
			//On the third row Tracker Show Active Tracks and Tracker Show New Tracks CheckBox
			panelLayout.putConstraint(SpringLayout.NORTH, chkTrackerShowActiveTracks, 2, SpringLayout.SOUTH, kltTrackerPanel);
			panelLayout.putConstraint(SpringLayout.WEST, chkTrackerShowActiveTracks, 0, SpringLayout.WEST, lblTrackerType);
			panelLayout.putConstraint(SpringLayout.NORTH, chkTrackerShowNewTracks, 0, SpringLayout.NORTH, chkTrackerShowActiveTracks);
			panelLayout.putConstraint(SpringLayout.WEST, chkTrackerShowNewTracks, 3, SpringLayout.EAST, chkTrackerShowActiveTracks);
			
			//The height of the panel is constrained to the bottom of the third row
			panelLayout.putConstraint(SpringLayout.SOUTH, trackerSettingsPanel, 0, SpringLayout.SOUTH, chkTrackerShowActiveTracks);
			
			//Adds the layout to the panel
			trackerSettingsPanel.setLayout(panelLayout);
			
			return trackerSettingsPanel;
	}
	
	
	
	private JPanel createVisualOdometrySettingsPanel(Core core){
	
		//Loads current Parameters
		Parameters parameters = core.getParameters();
		
		/* Parameters Managed from Visual Odometry Settings Panel */
		VisualOdometryParameters visualOdometryParameters = parameters.getVisualOdometryParameters();
		HashMap<String, Component> guiComponents = parameters.getGuiComponents();

		SpringLayout panelLayout = null; //Layout Object needed for components disposition

			

		/***********************
		 * COMPONENTS CREATION *
		 ***********************/
			
		
		
		/**
		  *  1. VISUAL ODOMETRY TYPE PART
		  *  
		  *  First and upper part of the Visual Odometry Settings Panel.
		  *  
		  */


			/**
			 * BOOFCV VISUAL ODOMETRY ALGORITHMS: 
			 * 
			 * The following are all the Visual Odometry Algorithms offered by BoofCv Factories	
			 * 
			 * MONO:
			 * FactoryVisualOdometry.monoPlaneInfinity(thresholdAdd, thresholdRetire, inlierPixelTol, ransacIterations, tracker, imageType)
			 * FactoryVisualOdometry.monoPlaneOverhead(cellSize, maxCellsPerPixel, mapHeightFraction, inlierGroundTol, ransacIterations, thresholdRetire, absoluteMinimumTracks, respawnTrackFraction, respawnCoverageFraction, tracker, imageType)
			 * 
			 * STEREO:
			 * FactoryVisualOdometry.stereoDepth(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations, refineIterations, doublePass, sparseDisparity, tracker, imageType)
			 * FactoryVisualOdometry.stereoDualTrackerPnP(thresholdAdd, thresholdRetire, inlierPixelTol, epipolarPixelTol, ransacIterations, refineIterations, trackerLeft, trackerRight, descriptor, imageType)
			 * FactoryVisualOdometry.stereoQuadPnP(inlierPixelTol, epipolarPixelTol, maxDistanceF2F, maxAssociationError, ransacIterations, refineIterations, detector, imageType)
			 * 
			 * DEPTH:
			 * FactoryVisualOdometry.depthDepthPnP(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations, refineIterations, doublePass, sparseDepth, tracker, visualType, depthType)
			 *
			 * Only MONO algorithms have been implemented for now.
			 * 
			 */

			
			/*Visual Odometry Type Label*/
			final JLabel lblVisualOdometryType = new JLabel("<html><b>VO Type:</b></html>");
			
			/*Visual Odometry Type ComboBox*/
			final JComboBox<String>	txtVisualOdometryType = new JComboBox<String>(
					visualOdometryParameters.getVisualOdometryTypeNames().values().toArray(new String[]{}));		
			txtVisualOdometryType.setSelectedItem(
					visualOdometryParameters.getVisualOdometryTypeNames().get(visualOdometryParameters.getVisualOdometryType()));
			/*Listener*/
			txtVisualOdometryType.addActionListener(new VisualOdometryTypeChangeListener(txtVisualOdometryType, core));			
			
			
			
		/**
		  *  2. MONOPLANE INFINITY/MONOPLANE OVERHEAD VISUAL ODOMETRY COMPONENTS AND PANELS
		  *  
		  *  Second part of the Visual Odometry Settings Panel.
		  *  
		  */

				
			/*monoPlaneInfinity Components*/
			
			
			/*monoPlaneInfinity thresholdAdd Label*/
			final JLabel lblMonoPlaneInfinity_thresholdAdd = new JLabel("<html>thresholdAdd:</html>");
			
			/*monoPlaneInfinity thresholdAdd TextField*/
			final JTextField txtMonoPlaneInfinity_thresholdAdd = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneInfinity_thresholdAdd()),5);
			txtMonoPlaneInfinity_thresholdAdd.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneInfinity_thresholdAdd.addFocusListener(
					new IntegerParameterTextFieldListener("monoPlaneInfinity_thresholdAdd", txtMonoPlaneInfinity_thresholdAdd, core));
			
			/*monoPlaneInfinity thresholdRetire Label*/
			final JLabel lblMonoPlaneInfinity_thresholdRetire = new JLabel("<html>thresholdRetire:</html>");
			
			/*monoPlaneInfinity thresholdRetire TextField*/
			final JTextField txtMonoPlaneInfinity_thresholdRetire = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneInfinity_thresholdRetire()),5);		
			txtMonoPlaneInfinity_thresholdRetire.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneInfinity_thresholdRetire.addFocusListener(
					new IntegerParameterTextFieldListener("monoPlaneInfinity_thresholdRetire", txtMonoPlaneInfinity_thresholdRetire, core));
						
			/*monoPlaneInfinity inlierPixelTol Label*/
			final JLabel lblMonoPlaneInfinity_inlierPixelTol = new JLabel("<html>inlierPixelTol:</html>");
			
			/*monoPlaneInfinity inlierPixelTol TextField*/
			final JTextField txtMonoPlaneInfinity_inlierPixelTol = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneInfinity_inlierPixelTol()),5);		
			txtMonoPlaneInfinity_inlierPixelTol.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneInfinity_inlierPixelTol.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneInfinity_inlierPixelTol", txtMonoPlaneInfinity_inlierPixelTol, core));
						
			/*monoPlaneInfinity ransacIterations Label*/
			final JLabel lblMonoPlaneInfinity_ransacIterations = new JLabel("<html>ransacIterations:</html>");
			
			/*monoPlaneInfinity ransacIterations TextField*/
			final JTextField txtMonoPlaneInfinity_ransacIterations = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneInfinity_ransacIterations()),5);		
			txtMonoPlaneInfinity_ransacIterations.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneInfinity_ransacIterations.addFocusListener(
					new IntegerParameterTextFieldListener("monoPlaneInfinity_ransacIterations", txtMonoPlaneInfinity_ransacIterations, core));
									
		
			
			/** MONOPLANE INFINITY PANEL - CREATION **/
			

			final JPanel monoPlaneInfinityPanel = new JPanel();
			//If the default/saved VisualOdometry type is default(monoPlaneInfinity with default parameters), disables the panel
			monoPlaneInfinityPanel.setEnabled(!visualOdometryParameters.getVisualOdometryType().equals(VisualOdometryParameters.DEFAULT_VISUALODOMETRY));
			
			//Adds all components to the panel
			monoPlaneInfinityPanel.add(lblMonoPlaneInfinity_thresholdAdd);
			monoPlaneInfinityPanel.add(txtMonoPlaneInfinity_thresholdAdd);
			monoPlaneInfinityPanel.add(lblMonoPlaneInfinity_thresholdRetire);
			monoPlaneInfinityPanel.add(txtMonoPlaneInfinity_thresholdRetire);
			monoPlaneInfinityPanel.add(lblMonoPlaneInfinity_inlierPixelTol);
			monoPlaneInfinityPanel.add(txtMonoPlaneInfinity_inlierPixelTol);
			monoPlaneInfinityPanel.add(lblMonoPlaneInfinity_ransacIterations);
			monoPlaneInfinityPanel.add(txtMonoPlaneInfinity_ransacIterations);
			
			
			//Each component in monoPlaneInfinity settings panel is enabled/disabled depending on default/saved parameter
			for(Component comp: monoPlaneInfinityPanel.getComponents()){	
				comp.setEnabled(!visualOdometryParameters.getVisualOdometryType().equals(VisualOdometryParameters.DEFAULT_VISUALODOMETRY));
			}
			
			
			/** MONOPLANE INFINITY PANEL - DISPOSITION **/
			
			
			panelLayout = new SpringLayout();

			//On the first row thresholdAdd and thresholdRetire
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneInfinity_thresholdAdd, 8, SpringLayout.NORTH, monoPlaneInfinityPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneInfinity_thresholdAdd, 5, SpringLayout.WEST, monoPlaneInfinityPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneInfinity_thresholdAdd, -1,SpringLayout.NORTH, lblMonoPlaneInfinity_thresholdAdd);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneInfinity_thresholdAdd, 3, SpringLayout.EAST, lblMonoPlaneInfinity_thresholdAdd);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneInfinity_thresholdRetire, 0, SpringLayout.NORTH, lblMonoPlaneInfinity_thresholdAdd);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneInfinity_thresholdRetire, 3, SpringLayout.EAST, txtMonoPlaneInfinity_thresholdAdd);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneInfinity_thresholdRetire, -1,SpringLayout.NORTH, lblMonoPlaneInfinity_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneInfinity_thresholdRetire, 0, SpringLayout.WEST, txtMonoPlaneInfinity_ransacIterations);
			
			//On the second row inlierPixelTol and ransacIterations
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneInfinity_inlierPixelTol, 10, SpringLayout.SOUTH, lblMonoPlaneInfinity_thresholdAdd);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneInfinity_inlierPixelTol, 5, SpringLayout.WEST, monoPlaneInfinityPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneInfinity_inlierPixelTol, -1,SpringLayout.NORTH, lblMonoPlaneInfinity_inlierPixelTol);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneInfinity_inlierPixelTol, 0, SpringLayout.WEST, txtMonoPlaneInfinity_thresholdAdd);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneInfinity_ransacIterations, 0, SpringLayout.NORTH, lblMonoPlaneInfinity_inlierPixelTol);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneInfinity_ransacIterations, 3, SpringLayout.EAST, txtMonoPlaneInfinity_inlierPixelTol);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneInfinity_ransacIterations, -1, SpringLayout.NORTH, lblMonoPlaneInfinity_ransacIterations);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneInfinity_ransacIterations, 3, SpringLayout.EAST, lblMonoPlaneInfinity_ransacIterations);
			
			//Adds the layout to the panel
			monoPlaneInfinityPanel.setLayout(panelLayout);
			
			//Sets panel dimensions (same height as monoPlaneOverhead panel (to match into the same Scroll Pane))
			monoPlaneInfinityPanel.setPreferredSize(new Dimension(monoPlaneInfinityPanel.getPreferredSize().width,70));

			
			
			/*monoPlaneOverhead Components*/
			
			
			/*monoPlaneOverhead cellSize Label*/
			final JLabel lblMonoPlaneOverhead_cellSize = new JLabel("<html>cellSize:</html>");
			
			/*monoPlaneOverhead cellSize TextField*/
			final JTextField txtMonoPlaneOverhead_cellSize = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_cellSize()),5);
			txtMonoPlaneOverhead_cellSize.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_cellSize.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneOverhead_cellSize", txtMonoPlaneOverhead_cellSize, core));
			
			/*monoPlaneOverhead maxCellsPerPixel Label*/
			final JLabel lblMonoPlaneOverhead_maxCellsPerPixel = new JLabel("<html>maxCellsPerPixel:</html>");
			
			/*monoPlaneOverhead maxCellsPerPixel TextField*/
			final JTextField txtMonoPlaneOverhead_maxCellsPerPixel = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_maxCellsPerPixel()),5);		
			txtMonoPlaneOverhead_maxCellsPerPixel.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_maxCellsPerPixel.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneOverhead_maxCellsPerPixel", txtMonoPlaneOverhead_maxCellsPerPixel, core));
			
			/*monoPlaneOverhead mapHeightFraction Label*/
			final JLabel lblMonoPlaneOverhead_mapHeightFraction = new JLabel("<html>mapHeightFraction:</html>");
			
			/*monoPlaneOverhead mapHeightFraction TextField*/
			final JTextField txtMonoPlaneOverhead_mapHeightFraction = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_mapHeightFraction()),5);		
			txtMonoPlaneOverhead_mapHeightFraction.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_mapHeightFraction.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneOverhead_mapHeightFraction", txtMonoPlaneOverhead_mapHeightFraction, core));
						
			/*monoPlaneOverhead inlierGroundTol Label*/
			final JLabel lblMonoPlaneOverhead_inlierGroundTol = new JLabel("<html>inlierGroundTol:</html>");
			
			/*monoPlaneOverhead inlierGroundTol TextField*/
			final JTextField txtMonoPlaneOverhead_inlierGroundTol = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_inlierGroundTol()),5);		
			txtMonoPlaneOverhead_inlierGroundTol.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_inlierGroundTol.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneOverhead_inlierGroundTol", txtMonoPlaneOverhead_inlierGroundTol, core));
						
			/*monoPlaneOverhead ransacIterations Label*/
			final JLabel lblMonoPlaneOverhead_ransacIterations = new JLabel("<html>ransacIterations:</html>");
			
			/*monoPlaneOverhead ransacIterations TextField*/
			final JTextField txtMonoPlaneOverhead_ransacIterations = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_ransacIterations()),5);		
			txtMonoPlaneOverhead_ransacIterations.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_ransacIterations.addFocusListener(
					new IntegerParameterTextFieldListener("monoPlaneOverhead_ransacIterations", txtMonoPlaneOverhead_ransacIterations, core));
						
			/*monoPlaneOverhead thresholdRetire Label*/
			final JLabel lblMonoPlaneOverhead_thresholdRetire = new JLabel("<html>thresholdRetire:</html>");
			
			/*monoPlaneOverhead thresholdRetire TextField*/
			final JTextField txtMonoPlaneOverhead_thresholdRetire = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_thresholdRetire()),5);		
			txtMonoPlaneOverhead_thresholdRetire.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_thresholdRetire.addFocusListener(
					new IntegerParameterTextFieldListener("monoPlaneOverhead_thresholdRetire", txtMonoPlaneOverhead_thresholdRetire, core));
						
			/*monoPlaneOverhead absoluteMinimumTracks Label*/
			final JLabel lblMonoPlaneOverhead_absoluteMinimumTracks = new JLabel("<html>absoluteMinimumTracks:</html>");
			
			/*monoPlaneOverhead absoluteMinimumTracks TextField*/
			final JTextField txtMonoPlaneOverhead_absoluteMinimumTracks = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_absoluteMinimumTracks()),5);		
			txtMonoPlaneOverhead_absoluteMinimumTracks.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_absoluteMinimumTracks.addFocusListener(
					new IntegerParameterTextFieldListener("monoPlaneOverhead_absoluteMinimumTracks", txtMonoPlaneOverhead_absoluteMinimumTracks, core));
			
			/*monoPlaneOverhead respawnTrackFraction Label*/
			final JLabel lblMonoPlaneOverhead_respawnTrackFraction = new JLabel("<html>respawnTrackFraction:</html>");
			
			/*monoPlaneOverhead respawnTrackFraction TextField*/
			final JTextField txtMonoPlaneOverhead_respawnTrackFraction = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_respawnTrackFraction()),5);		
			txtMonoPlaneOverhead_respawnTrackFraction.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_respawnTrackFraction.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneOverhead_respawnTrackFraction", txtMonoPlaneOverhead_respawnTrackFraction, core));
						
			/*monoPlaneOverhead respawnCoverageFraction Label*/
			final JLabel lblMonoPlaneOverhead_respawnCoverageFraction = new JLabel("<html>respawnCoverageFraction:</html>");
			
			/*monoPlaneOverhead respawnCoverageFraction TextField*/
			final JTextField txtMonoPlaneOverhead_respawnCoverageFraction = new JTextField(
					String.valueOf(visualOdometryParameters.getMonoPlaneOverhead_respawnCoverageFraction()),5);		
			txtMonoPlaneOverhead_respawnCoverageFraction.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtMonoPlaneOverhead_respawnCoverageFraction.addFocusListener(
					new DoubleParameterTextFieldListener("monoPlaneOverhead_respawnCoverageFraction", txtMonoPlaneOverhead_respawnCoverageFraction, core));
									

			
			/** MONOPLANE OVERHEAD PANEL - CREATION **/
			

			final JPanel monoPlaneOverheadPanel = new JPanel();

			//Adds all components to the panel
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_cellSize);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_cellSize);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_maxCellsPerPixel);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_maxCellsPerPixel);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_mapHeightFraction);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_mapHeightFraction);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_inlierGroundTol);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_inlierGroundTol);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_ransacIterations);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_ransacIterations);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_thresholdRetire);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_thresholdRetire);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_absoluteMinimumTracks);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_absoluteMinimumTracks);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_respawnTrackFraction);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_respawnTrackFraction);
			monoPlaneOverheadPanel.add(lblMonoPlaneOverhead_respawnCoverageFraction);
			monoPlaneOverheadPanel.add(txtMonoPlaneOverhead_respawnCoverageFraction);
			
	
			
			/** MONOPLANE OVERHEAD PANEL - DISPOSITION **/
			
			
			panelLayout = new SpringLayout();

			//On the first row cellSize, maxCellsPerPixel, mapHeightFraction, inlierGroundTol and ransacIterations
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_cellSize, 0, SpringLayout.NORTH, monoPlaneOverheadPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_cellSize, 5, SpringLayout.WEST, monoPlaneOverheadPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_cellSize, -1,SpringLayout.NORTH, lblMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_cellSize, 0, SpringLayout.WEST, txtMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_maxCellsPerPixel, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_maxCellsPerPixel, 3, SpringLayout.EAST, txtMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_maxCellsPerPixel, -1,SpringLayout.NORTH, lblMonoPlaneOverhead_maxCellsPerPixel);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_maxCellsPerPixel, 0, SpringLayout.WEST, txtMonoPlaneOverhead_absoluteMinimumTracks);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_mapHeightFraction, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_mapHeightFraction, 3, SpringLayout.EAST, txtMonoPlaneOverhead_maxCellsPerPixel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_mapHeightFraction, -1,SpringLayout.NORTH, lblMonoPlaneOverhead_mapHeightFraction);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_mapHeightFraction, 0, SpringLayout.WEST, txtMonoPlaneOverhead_respawnTrackFraction);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_inlierGroundTol, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_inlierGroundTol, 3, SpringLayout.EAST, txtMonoPlaneOverhead_mapHeightFraction);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_inlierGroundTol, -1,SpringLayout.NORTH, lblMonoPlaneOverhead_inlierGroundTol);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_inlierGroundTol, 0, SpringLayout.WEST, txtMonoPlaneOverhead_respawnCoverageFraction);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_ransacIterations, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_ransacIterations, 3, SpringLayout.EAST, txtMonoPlaneOverhead_inlierGroundTol);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_ransacIterations, -1,SpringLayout.NORTH, lblMonoPlaneOverhead_ransacIterations);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_ransacIterations, 3, SpringLayout.EAST, lblMonoPlaneOverhead_ransacIterations);
			
			//On the second row thresholdRetire, absoluteMinimumTracks, respawnTrackFraction and respawnCoverageFraction
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_thresholdRetire, 10, SpringLayout.SOUTH, lblMonoPlaneOverhead_cellSize);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_thresholdRetire, 5, SpringLayout.WEST, monoPlaneOverheadPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_thresholdRetire, -1,SpringLayout.NORTH, lblMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_thresholdRetire, 3, SpringLayout.EAST, lblMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_absoluteMinimumTracks, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_absoluteMinimumTracks, 3, SpringLayout.EAST, txtMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_absoluteMinimumTracks, -1, SpringLayout.NORTH, lblMonoPlaneOverhead_absoluteMinimumTracks);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_absoluteMinimumTracks, 3, SpringLayout.EAST, lblMonoPlaneOverhead_absoluteMinimumTracks);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_respawnTrackFraction, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_respawnTrackFraction, 3, SpringLayout.EAST, txtMonoPlaneOverhead_absoluteMinimumTracks);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_respawnTrackFraction, -1, SpringLayout.NORTH, lblMonoPlaneOverhead_respawnTrackFraction);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_respawnTrackFraction, 3, SpringLayout.EAST, lblMonoPlaneOverhead_respawnTrackFraction);
			panelLayout.putConstraint(SpringLayout.NORTH, lblMonoPlaneOverhead_respawnCoverageFraction, 0, SpringLayout.NORTH, lblMonoPlaneOverhead_thresholdRetire);
			panelLayout.putConstraint(SpringLayout.WEST, lblMonoPlaneOverhead_respawnCoverageFraction, 3, SpringLayout.EAST, txtMonoPlaneOverhead_respawnTrackFraction);
			panelLayout.putConstraint(SpringLayout.NORTH, txtMonoPlaneOverhead_respawnCoverageFraction, -1, SpringLayout.NORTH, lblMonoPlaneOverhead_respawnCoverageFraction);
			panelLayout.putConstraint(SpringLayout.WEST, txtMonoPlaneOverhead_respawnCoverageFraction, 3, SpringLayout.EAST, lblMonoPlaneOverhead_respawnCoverageFraction);

			
			//Adds the layout to the panel
			monoPlaneOverheadPanel.setLayout(panelLayout);
			
			//Sets panel dimensions (same height as monoPlaneInfinity panel, and a big width to contain 
			//all the components horizontally (using the ScrollPane))
			monoPlaneOverheadPanel.setPreferredSize(new Dimension(1200,70));

			
			
			/** MONOPLANE (VISUAL ODOMETRY) SCROLL-PANEL
			  *
			  * This scrollpane will contain all Mono Visual Odometry Settings depending 
			  * on the selected mono Visual Odometry Type (Infinity or Overhead)
			  * 
			  **/
			
			
			final JScrollPane monoPlaneScrollPane = new JScrollPane();
			monoPlaneScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			
			//Depending on the selected Visual Odometry Type sets the monoPlaneScrollPane viewPortView (content)
			//(The viewPortViews needs to be all the same height for correct displaying (in this case we have
			//set the height of monoPlaneInfinity and monoPlaneOverhead to 70, the maximum of the two heights)
			switch(visualOdometryParameters.getVisualOdometryType()){
				case VisualOdometryParameters.DEFAULT_VISUALODOMETRY:
				case VisualOdometryParameters.MONOPLANEINFINITY:
					monoPlaneScrollPane.setViewportView(monoPlaneInfinityPanel);
					break;
				case VisualOdometryParameters.MONOPLANEOVERHEAD:
					monoPlaneScrollPane.setViewportView(monoPlaneOverheadPanel);
					break;
				default:
					JPanel newJPanel = new JPanel();
					newJPanel.setPreferredSize(new Dimension(newJPanel.getPreferredSize().width,70));
					monoPlaneScrollPane.setViewportView(newJPanel);
					break;
			}
			
			
			
		/**
		  *  3. POPULATE GUICOMPONENTS
		  * 
		  *  Adds all the most important (and reused) components to the guiComponents HashMap
		  *  
		  */

				
			guiComponents.put("txtVisualOdometryType", txtVisualOdometryType);
			guiComponents.put("txtMonoPlaneInfinity_thresholdAdd", txtMonoPlaneInfinity_thresholdAdd);
			guiComponents.put("txtMonoPlaneInfinity_thresholdRetire", txtMonoPlaneInfinity_thresholdRetire);
			guiComponents.put("txtMonoPlaneInfinity_inlierPixelTol", txtMonoPlaneInfinity_inlierPixelTol);
			guiComponents.put("txtMonoPlaneInfinity_ransacIterations", txtMonoPlaneInfinity_ransacIterations);
			guiComponents.put("monoPlaneInfinityPanel", monoPlaneInfinityPanel);
			guiComponents.put("txtMonoPlaneOverhead_cellSize", txtMonoPlaneOverhead_cellSize);
			guiComponents.put("txtMonoPlaneOverhead_maxCellsPerPixel", txtMonoPlaneOverhead_maxCellsPerPixel);
			guiComponents.put("txtMonoPlaneOverhead_mapHeightFraction", txtMonoPlaneOverhead_mapHeightFraction);
			guiComponents.put("txtMonoPlaneOverhead_inlierGroundTol", txtMonoPlaneOverhead_inlierGroundTol);
			guiComponents.put("txtMonoPlaneOverhead_ransacIteration", txtMonoPlaneOverhead_ransacIterations);
			guiComponents.put("txtMonoPlaneOverhead_thresholdRetire", txtMonoPlaneOverhead_thresholdRetire);
			guiComponents.put("txtMonoPlaneOverhead_absoluteMinimumTracks", txtMonoPlaneOverhead_absoluteMinimumTracks);
			guiComponents.put("txtMonoPlaneOverhead_respawnTrackFraction", txtMonoPlaneOverhead_respawnTrackFraction);
			guiComponents.put("txtMonoPlaneOverhead_respawnCoverageFraction", txtMonoPlaneOverhead_respawnCoverageFraction);
			guiComponents.put("monoPlaneOverheadPanel", monoPlaneOverheadPanel);
			guiComponents.put("monoPlaneScrollPane", monoPlaneScrollPane);

			
			
		/**
		  *  4. VISUAL ODOMETRY SETTINGS PANEL CREATION
		  * 
		  *  Creation of the Visual Odometry Settings Panel.
		  *  
		  */

				
			//Create compound border: Titled+Empty
			final JPanel visualOdometrySettingsPanel = new JPanel();
			visualOdometrySettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Visual Odometry Settings:<b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,5)));

			
			//Adds components to the panel
			visualOdometrySettingsPanel.add(lblVisualOdometryType);
			visualOdometrySettingsPanel.add(txtVisualOdometryType);
			visualOdometrySettingsPanel.add(monoPlaneScrollPane);
			


		/**************************
		 * COMPONENTS DISPOSITION *
		 *************************/



		/**
		  *  5. VISUAL ODOMETRY SETTINGS PANEL DISPOSITION
		  * 
		  *  Disposition of the components inside Visual Odometry Settings Panel.
		  *  
		  */
				
			
			panelLayout = new SpringLayout();
			
			//On the first row Visual Odometry Type label and combobox
			panelLayout.putConstraint(SpringLayout.NORTH, lblVisualOdometryType, 0, SpringLayout.NORTH, visualOdometrySettingsPanel);
			panelLayout.putConstraint(SpringLayout.WEST, lblVisualOdometryType, 3, SpringLayout.WEST, visualOdometrySettingsPanel);
			panelLayout.putConstraint(SpringLayout.NORTH, txtVisualOdometryType, -3,SpringLayout.NORTH, lblVisualOdometryType);
			panelLayout.putConstraint(SpringLayout.WEST, txtVisualOdometryType, 3, SpringLayout.EAST, lblVisualOdometryType);
			panelLayout.putConstraint(SpringLayout.EAST, txtVisualOdometryType, -3, SpringLayout.EAST, visualOdometrySettingsPanel);
			
			//On the second row monoPlane ScrollPane (can contain monoPlaneInfinity or monoPlaneOverhead)
			panelLayout.putConstraint(SpringLayout.NORTH, monoPlaneScrollPane, 10, SpringLayout.SOUTH, lblVisualOdometryType);
			panelLayout.putConstraint(SpringLayout.WEST, monoPlaneScrollPane, 0, SpringLayout.WEST, lblVisualOdometryType);
			panelLayout.putConstraint(SpringLayout.EAST, monoPlaneScrollPane, 0, SpringLayout.EAST, visualOdometrySettingsPanel);
		
			//The height of the panel is constrained to the bottom of the second row
			panelLayout.putConstraint(SpringLayout.SOUTH, visualOdometrySettingsPanel, 0, SpringLayout.SOUTH, monoPlaneScrollPane);
			
			//Adds layout to panel
			visualOdometrySettingsPanel.setLayout(panelLayout);
			
			return visualOdometrySettingsPanel;
	}
	
	
	
	private JPanel createChartSettingsPanel(Core core){

		//Loads current Parameters
		Parameters parameters = core.getParameters();
		
		/* Parameters Managed from Chart Settings Panel */
		ChartOutputParameters chartOutputParameters = parameters.getChartOutputParameters();
		HashMap<String, Component> guiComponents = parameters.getGuiComponents();

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

			
			/*Chart Type Label*/
			final JLabel lblChartType = new JLabel("<html><b>Chart Type:</b></html>");
			
			/*Chart Type ComboBox*/
			final JComboBox<String>	txtChartType = new JComboBox<String>(
					chartOutputParameters.getChartTypeNames().values().toArray(new String[]{}));		
			txtChartType.setSelectedItem(
					chartOutputParameters.getChartTypeNames().get(chartOutputParameters.getChartType()));			
			/*Listener*/
			txtChartType.addActionListener(new ChartTypeChangeListener(txtChartType, core));
			
			
			
		/**
		  *  2. CHART X/Z AND CHART Y PART
		  *  
		  *  Second part of the Chart Settings Panel.
		  *  
		  */

			
			/*Chart X/Z Label*/
			final JLabel lblChartXZ = new JLabel("<html><b>Chart X/Z</b></html>");
			
			/*Chart X/Z Scale Label*/
			final JLabel lblChartXZ_Scale = new JLabel("<html>Scale: </html>");
			
			/*Chart X/Z Scale TextField*/
			final JTextField txtChartXZ_Scale = new JTextField(
					String.valueOf(chartOutputParameters.getChartXZ_Scale()),5);
			txtChartXZ_Scale.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtChartXZ_Scale.addFocusListener(
					new DoubleParameterTextFieldListener("chartXZ_Scale", txtChartXZ_Scale, core));
			
			//Applying loaded Chart X/Z Scale
			ChartScrollPane chartXZPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartXZPanel");
			if(chartXZPanel != null){
				chartXZPanel.setChartScalingFactor(chartOutputParameters.getChartXZ_Scale());
				chartXZPanel.resetSize();
			}
			
			/*Chart XZ Apply Scale Button*/
			final JButton btnChartXZ_applyScale = new JButton("Apply");
			/*Listener*/
			btnChartXZ_applyScale.addActionListener(new ChartButtonListener("chartXZ_applyScale", core));
			
			/*Chart XZ Move to Origin Button*/
			final JButton btnChartXZ_moveToOrigin = new JButton("Origin");
			/*Listener*/
			btnChartXZ_moveToOrigin.addActionListener(new ChartButtonListener("chartXZ_moveToOrigin", core));

			/*Chart XZ Move to Last Point Button*/
			final JButton btnChartXZ_moveToLastPoint = new JButton("Last");
			/*Listener*/
			btnChartXZ_moveToLastPoint.addActionListener(new ChartButtonListener("chartXZ_moveToLastPoint", core));
			
			/*Chart XZ 3D Points CheckBox*/
			final JCheckBox chkChartXZ_3DPoints = new JCheckBox("<html>3D Chart Points</html>");
			/*Listener*/
			chkChartXZ_3DPoints.addActionListener(new ChartButtonListener("chartXZ_3DPoints", core));

			
			/*Chart Y Label*/
			final JLabel lblChartY = new JLabel("<html><b>Chart Y</b></html>");
			
			/*Chart Y Scale Label*/
			final JLabel lblChartY_Scale = new JLabel("<html>Scale: </html>");
			
			/*Chart Y Scale TextField*/
			final JTextField txtChartY_Scale = new JTextField(
					String.valueOf(chartOutputParameters.getChartY_Scale()),5);
			txtChartY_Scale.setHorizontalAlignment(JTextField.CENTER);
			/*Listener*/
			txtChartY_Scale.addFocusListener(new DoubleParameterTextFieldListener("chartY_Scale", txtChartY_Scale, core));
			
			//Applying loaded Chart Y Scale
			ChartScrollPane chartYPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartYPanel");
			if(chartYPanel != null){
				chartYPanel.setChartScalingFactor(chartOutputParameters.getChartY_Scale());
				chartYPanel.resetSize();
			}
			
			/*Chart Y Apply Scale Button*/
			final JButton btnChartY_applyScale = new JButton("Apply");
			/*Listener*/
			btnChartY_applyScale.addActionListener(new ChartButtonListener("chartY_applyScale", core));

			/*Chart Y Move to Origin Button*/
			final JButton btnChartY_moveToOrigin = new JButton("Origin");
			/*Listener*/
			btnChartY_moveToOrigin.addActionListener(new ChartButtonListener("chartY_moveToOrigin", core));

			/*Chart Y Move to Last Point Button*/
			final JButton btnChartY_moveToLastPoint = new JButton("Last");
			/*Listener*/
			btnChartY_moveToLastPoint.addActionListener(new ChartButtonListener("chartY_moveToLastPoint", core));
				
				
				
		/**
		  *  3. POPULATE GUICOMPONENTS
		  * 
		  *  Adds all the most important (and reused) components to the guiComponents HashMap
		  *  
		  */

						
			guiComponents.put("txtChartType", txtChartType);
			guiComponents.put("txtChartXZ_Scale", txtChartXZ_Scale);			
			guiComponents.put("chkChartXZ_3DPoints", chkChartXZ_3DPoints);
			guiComponents.put("txtChartY_Scale", txtChartY_Scale);

			
			
		/**
		  *  4. CHART SETTINGS PANEL CREATION
		  * 
		  *  Creation of the Chart Settings Panel.
		  *  
		  */

					
			//Create compound border: Titled+Empty
			final JPanel chartSettingsPanel = new JPanel();
			chartSettingsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("<html><b>Chart Settings:</b></html>"),
																		  BorderFactory.createEmptyBorder(5,5,5,5)));
			
			//Adds components to the panel
			chartSettingsPanel.add(lblChartType);
			chartSettingsPanel.add(txtChartType);
			chartSettingsPanel.add(lblChartXZ);
			chartSettingsPanel.add(lblChartXZ_Scale);
			chartSettingsPanel.add(txtChartXZ_Scale);
			chartSettingsPanel.add(btnChartXZ_applyScale);
			chartSettingsPanel.add(btnChartXZ_moveToOrigin);
			chartSettingsPanel.add(btnChartXZ_moveToLastPoint);
			chartSettingsPanel.add(chkChartXZ_3DPoints);
			chartSettingsPanel.add(lblChartY);
			chartSettingsPanel.add(lblChartY_Scale);
			chartSettingsPanel.add(txtChartY_Scale);
			chartSettingsPanel.add(btnChartY_applyScale);
			chartSettingsPanel.add(btnChartY_moveToOrigin);
			chartSettingsPanel.add(btnChartY_moveToLastPoint);
			


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
			panelLayout.putConstraint(SpringLayout.NORTH, txtChartType, -3,SpringLayout.NORTH, lblChartType);
			panelLayout.putConstraint(SpringLayout.WEST, txtChartType, 3, SpringLayout.EAST, lblChartType);
			panelLayout.putConstraint(SpringLayout.EAST, txtChartType, -3, SpringLayout.EAST, chartSettingsPanel);
	
			//On the second row Chart XZ Label
			panelLayout.putConstraint(SpringLayout.NORTH, lblChartXZ, 8, SpringLayout.SOUTH, lblChartType);
			panelLayout.putConstraint(SpringLayout.WEST, lblChartXZ, 0, SpringLayout.WEST, lblChartType);
			
			//On the third row Chart XZ Scale Label/TextField, Apply Scale Button, Move to Origin Button, 
			//Move to Last Point Button, and 3D Points CheckBox 
			panelLayout.putConstraint(SpringLayout.NORTH, lblChartXZ_Scale, 7,SpringLayout.SOUTH, lblChartXZ);
			panelLayout.putConstraint(SpringLayout.WEST, lblChartXZ_Scale, 5, SpringLayout.WEST, lblChartXZ);
			panelLayout.putConstraint(SpringLayout.NORTH, txtChartXZ_Scale, -3,SpringLayout.NORTH, lblChartXZ_Scale);
			panelLayout.putConstraint(SpringLayout.WEST, txtChartXZ_Scale, 3, SpringLayout.EAST, lblChartXZ_Scale);
			panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZ_applyScale, -1,SpringLayout.NORTH, txtChartXZ_Scale);
			panelLayout.putConstraint(SpringLayout.WEST, btnChartXZ_applyScale, 3, SpringLayout.EAST, txtChartXZ_Scale);
			panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZ_moveToOrigin, 0,SpringLayout.NORTH, btnChartXZ_applyScale);
			panelLayout.putConstraint(SpringLayout.WEST, btnChartXZ_moveToOrigin, 3, SpringLayout.EAST, btnChartXZ_applyScale);
			panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZ_moveToLastPoint, 0,SpringLayout.NORTH, btnChartXZ_moveToOrigin);
			panelLayout.putConstraint(SpringLayout.WEST, btnChartXZ_moveToLastPoint, 3, SpringLayout.EAST, btnChartXZ_moveToOrigin);
			panelLayout.putConstraint(SpringLayout.NORTH, chkChartXZ_3DPoints, 0,SpringLayout.NORTH, btnChartXZ_moveToLastPoint);
			panelLayout.putConstraint(SpringLayout.WEST, chkChartXZ_3DPoints, 3, SpringLayout.EAST, btnChartXZ_moveToLastPoint);
			
			//On the fourth row Chart Y Label
			panelLayout.putConstraint(SpringLayout.NORTH, lblChartY, 8, SpringLayout.SOUTH, lblChartXZ_Scale);
			panelLayout.putConstraint(SpringLayout.WEST, lblChartY, 0, SpringLayout.WEST, lblChartType);

			//On the fifth row Chart Y Scale Label/TextField, Apply Scale Button, Move to Origin Button
			//and Move to Last Point Button
			panelLayout.putConstraint(SpringLayout.NORTH, lblChartY_Scale, 7,SpringLayout.SOUTH, lblChartY);
			panelLayout.putConstraint(SpringLayout.WEST, lblChartY_Scale, 5, SpringLayout.WEST, lblChartY);
			panelLayout.putConstraint(SpringLayout.NORTH, txtChartY_Scale, -3,SpringLayout.NORTH, lblChartY_Scale);
			panelLayout.putConstraint(SpringLayout.WEST, txtChartY_Scale, 3, SpringLayout.EAST, lblChartY_Scale);
			panelLayout.putConstraint(SpringLayout.NORTH, btnChartY_applyScale, -1,SpringLayout.NORTH, txtChartY_Scale);
			panelLayout.putConstraint(SpringLayout.WEST, btnChartY_applyScale, 3, SpringLayout.EAST, txtChartY_Scale);
			panelLayout.putConstraint(SpringLayout.NORTH, btnChartY_moveToOrigin, 0,SpringLayout.NORTH, btnChartY_applyScale);
			panelLayout.putConstraint(SpringLayout.WEST, btnChartY_moveToOrigin, 3, SpringLayout.EAST, btnChartY_applyScale);
			panelLayout.putConstraint(SpringLayout.NORTH, btnChartY_moveToLastPoint, 0,SpringLayout.NORTH, btnChartY_moveToOrigin);
			panelLayout.putConstraint(SpringLayout.WEST, btnChartY_moveToLastPoint, 3, SpringLayout.EAST, btnChartY_moveToOrigin);
			
			//The height of the panel is constrained to the fifth row bottom
			panelLayout.putConstraint(SpringLayout.SOUTH, chartSettingsPanel, 0, SpringLayout.SOUTH, lblChartY_Scale);
			
			//Adds the layout to the panel
			chartSettingsPanel.setLayout(panelLayout);

			return chartSettingsPanel;
	}

	
	private void setSystemLookAndFeelEnabled(){

		//Tries to enable SystemLookAndFeel and set if it's enabled
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			isSystemLookAndFeelEnabled = true;
		}catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex){
			isSystemLookAndFeelEnabled = false;
		}

	}

	
	public static Dimension getFrameDefaultDimension(){

		//Gets current Screen Size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		//Depending on current Screen Size, generates opportune Width and Height for the Frames
		int frameHeight, frameWidth;
		frameWidth = (int)screenSize.getWidth()>=1030 ? 530:(int)(screenSize.getWidth()/3f);
		frameHeight = (int)(screenSize.getHeight())>=930 ? 930:(int)screenSize.getHeight();
		
		return new Dimension(frameWidth, frameHeight);
		
	}
	
	
	@SuppressWarnings("unchecked")
	public static Boolean reloadParameters(Parameters parameters, Core core){

		try{
			
			//Update Parameters reference into the passed Core to the new Parameters
			core.setParameters(parameters);

	
			EventListener listener;//Used for some components Listeners, to save, disable and re-enable
								   //them (to prevent side-effects when loading)
			
			
			/**Extracts all components from guiComponents and Resets them to new Parameters value*/ 
			
			
			/**Input Settings Panel Reloading**/

			//Calibration ComboBox
			JComboBox txtCalibration = (JComboBox)parameters.getGuiComponents().get("txtCalibration");
			PathDocument txtCalibrationDocument = (PathDocument) ((JTextComponent)txtCalibration.getEditor().getEditorComponent()).getDocument();
			listener = txtCalibrationDocument.getPathChangeListener();
			txtCalibrationDocument.removeDocumentListener((DocumentListener)listener); //Disable Listener
				//Reloading Contents
				txtCalibration.setModel(new DefaultComboBoxModel<>(parameters.getInputParameters().getCalibrationsList()));
				txtCalibration.setSelectedItem(parameters.getInputParameters().getCalibrationPath());
			txtCalibrationDocument.addDocumentListener((DocumentListener)listener); //Enable Listener

			//Video Source RadioButton
			JRadioButton optVideoSource = (JRadioButton)parameters.getGuiComponents().get("optVideoSource");
			optVideoSource.setSelected(parameters.getInputParameters().getInputSource().equals(InputParameters.VIDEO_INPUT)); 
			if(optVideoSource.isSelected()) { //Trigger Listener
				for(ActionListener actionListener: optVideoSource.getActionListeners()){
					actionListener.actionPerformed(new ActionEvent(optVideoSource, ActionEvent.ACTION_PERFORMED, null));
				}
			}
			
			//Video Source ComboBox
			JComboBox txtVideoSource = (JComboBox)parameters.getGuiComponents().get("txtVideoSource");
			PathDocument txtVideoSourceDocument = (PathDocument) ((JTextComponent)txtVideoSource.getEditor().getEditorComponent()).getDocument();
			listener = txtVideoSourceDocument.getPathChangeListener();
			txtVideoSourceDocument.removeDocumentListener((DocumentListener)listener); //Disable Listener
				//Reloading Contents
				txtVideoSource.setModel(new DefaultComboBoxModel<>(parameters.getInputParameters().getVideoPathsList()));
				txtVideoSource.setSelectedItem(parameters.getInputParameters().getVideoPath());
			txtVideoSourceDocument.addDocumentListener((DocumentListener)listener); //Enable Listener

			//Device Source RadioButton
			JRadioButton optDeviceSource = (JRadioButton)parameters.getGuiComponents().get("optDeviceSource");
			optDeviceSource.setSelected(parameters.getInputParameters().getInputSource().equals(InputParameters.DEVICE_INPUT));
			if(optDeviceSource.isSelected()) { //Trigger Listener
				for(ActionListener actionListener: optDeviceSource.getActionListeners()){
					actionListener.actionPerformed(new ActionEvent(optDeviceSource, ActionEvent.ACTION_PERFORMED, null));
				}
			}
			
			//Device Source ComboBox
			JComboBox txtDeviceSource = (JComboBox)parameters.getGuiComponents().get("txtDeviceSource");
			PathDocument txtDeviceSourceDocument = (PathDocument) ((JTextComponent)txtDeviceSource.getEditor().getEditorComponent()).getDocument();
			listener = txtDeviceSourceDocument.getPathChangeListener();
			txtDeviceSourceDocument.removeDocumentListener((DocumentListener)listener); //Disable Listener
				//Reloading Contents
				txtDeviceSource.setModel(new DefaultComboBoxModel<>(parameters.getInputParameters().getDevicePathsList()));
				txtDeviceSource.setSelectedItem(parameters.getInputParameters().getDevicePath());
			txtDeviceSourceDocument.addDocumentListener((DocumentListener)listener); //Enable Listener

			//Device Width TextField
			JTextField txtDeviceWidth = (JTextField)parameters.getGuiComponents().get("txtDeviceWidth");
			txtDeviceWidth.setText(String.valueOf(parameters.getInputParameters().getDeviceWidth()));

			//Device Height TextField
			JTextField txtDeviceHeight = (JTextField)parameters.getGuiComponents().get("txtDeviceHeight");
			txtDeviceHeight.setText(String.valueOf(parameters.getInputParameters().getDeviceHeight()));

			//Device Sustain Framerate CheckBox
			JCheckBox chkDeviceSustainFramerate = (JCheckBox)parameters.getGuiComponents().get("chkDeviceSustainFramerate");
			chkDeviceSustainFramerate.setSelected(parameters.getInputParameters().isDevice_Control_SustainFramerate_Enabled());
			for(ActionListener actionListener: chkDeviceSustainFramerate.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkDeviceSustainFramerate, ActionEvent.ACTION_PERFORMED, null));
			}
		
			//Device Timeout Image IO CheckBox
			JCheckBox chkDeviceTimeoutImageIO = (JCheckBox)parameters.getGuiComponents().get("chkDeviceTimeoutImageIO");
			chkDeviceTimeoutImageIO.setSelected(parameters.getInputParameters().isDevice_Control_TimeoutImageIO_Enabled());
			for(ActionListener actionListener: chkDeviceTimeoutImageIO.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkDeviceTimeoutImageIO, ActionEvent.ACTION_PERFORMED, null));
			}

			//Device Keep Format CheckBox
			JCheckBox chkDeviceKeepFormat = (JCheckBox)parameters.getGuiComponents().get("chkDeviceKeepFormat");
			chkDeviceKeepFormat.setSelected(parameters.getInputParameters().isDevice_Control_KeepFormat_Enabled());
			for(ActionListener actionListener: chkDeviceKeepFormat.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkDeviceKeepFormat, ActionEvent.ACTION_PERFORMED, null));
			}

			//Device Full Resolution Preview CheckBox
			JCheckBox chkFullResolutionPreview = (JCheckBox)parameters.getGuiComponents().get("chkFullResolutionPreview");
			chkFullResolutionPreview.setSelected(parameters.getChartOutputParameters().isFullResolutionPreview());
			for(ActionListener actionListener: chkFullResolutionPreview.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkFullResolutionPreview, ActionEvent.ACTION_PERFORMED, null));
			}

			//Device Input Preview Enabled CheckBox
			JCheckBox chkInputPreviewEnabled = (JCheckBox)parameters.getGuiComponents().get("chkInputPreviewEnabled");
			chkInputPreviewEnabled.setSelected(parameters.getInputParameters().isInputPreviewEnabled());
			for(ActionListener actionListener: chkInputPreviewEnabled.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkInputPreviewEnabled, ActionEvent.ACTION_PERFORMED, null));
			}
	
			
		
			/**Internal Image Settings Panel Reloading**/
			
			
			//Image Type ComboBox
			JComboBox txtImageType = (JComboBox)parameters.getGuiComponents().get("txtImageType");
			listener = null;
			for(ActionListener actionListener: txtImageType.getActionListeners()){
				if(actionListener instanceof ImageTypeChangeListener) listener = actionListener;
			}
			txtImageType.removeActionListener((ActionListener)listener); //Disable Listener
				//Reloading Contents
				txtImageType.setModel(new DefaultComboBoxModel<>());
				for(int i=0; i<parameters.getInternalImageParameters().getImageTypesList().length; i++){
					txtImageType.addItem(parameters.getInternalImageParameters().getImageTypesList()[i].getImageTypeName());
				}
				txtImageType.setSelectedItem(parameters.getInternalImageParameters().getImageType().getImageTypeName());
			txtImageType.addActionListener((ActionListener)listener); //Enable Listener
			
			//Image Keep Original CheckBox
			JCheckBox chkImageKeepOriginal = (JCheckBox)parameters.getGuiComponents().get("chkImageKeepOriginal");
			chkImageKeepOriginal.setSelected(parameters.getInternalImageParameters().isImageKeepOriginal());
			for(ActionListener actionListener: chkImageKeepOriginal.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkImageKeepOriginal, ActionEvent.ACTION_PERFORMED, null));
			}

			//Internal Image Preview CheckBox
			JCheckBox chkInternalImagePreview = (JCheckBox)parameters.getGuiComponents().get("chkInternalImagePreview");
			chkInternalImagePreview.setSelected(parameters.getChartOutputParameters().isInternalImagePreview());
			for(ActionListener actionListener: chkInternalImagePreview.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkInternalImagePreview, ActionEvent.ACTION_PERFORMED, null));
			}
			
			//Image Resize Width TextField
			JTextField txtImageResizeWidth = (JTextField)parameters.getGuiComponents().get("txtImageResizeWidth");
			txtImageResizeWidth.setText(String.valueOf(parameters.getInternalImageParameters().getImageResizeWidth()));
			
			//Image Resize Height TextField
			JTextField txtImageResizeHeight = (JTextField)parameters.getGuiComponents().get("txtImageResizeHeight");
			txtImageResizeHeight.setText(String.valueOf(parameters.getInternalImageParameters().getImageResizeHeight()));

			//Image Buffer Size TextField
			JTextField txtImageBufferSize = (JTextField)parameters.getGuiComponents().get("txtImageBufferSize");
			int bufferSize = parameters.getInternalImageParameters().getImageBufferSize();
			txtImageBufferSize.setText(bufferSize == InternalImageParameters.INFINITEBUFFER?"Infinity":String.valueOf(bufferSize));
			
			//Frame Decimate Enabled CheckBox
			JCheckBox chkFrameDecimateEnabled = (JCheckBox)parameters.getGuiComponents().get("chkFrameDecimateEnabled");
			chkFrameDecimateEnabled.setSelected(parameters.getInternalImageParameters().isFrameDecimateEnabled());
			for(ActionListener actionListener: chkFrameDecimateEnabled.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkFrameDecimateEnabled, ActionEvent.ACTION_PERFORMED, null));
			}
			
			//Frame Decimate Value TextField
			JTextField txtFrameDecimateValue = (JTextField)parameters.getGuiComponents().get("txtFrameDecimateValue");
			txtFrameDecimateValue.setText(String.valueOf(parameters.getInternalImageParameters().getFrameDecimateValue()));

			
			
			/**Tracker Settings Panel Reloading**/
			
			
			//Tracker Type ComboBox
			JComboBox txtTrackerType = (JComboBox)parameters.getGuiComponents().get("txtTrackerType");
			listener = null;
			for(ActionListener actionListener: txtTrackerType.getActionListeners()){
				if(actionListener instanceof TrackerTypeChangeListener) listener = actionListener;
			}
			txtTrackerType.removeActionListener((ActionListener)listener); //Disable Listener (not needed when only changing model, but safer)
				//Reloading Contents
				txtTrackerType.setModel(new DefaultComboBoxModel<>(
						parameters.getTrackerParameters().getTrackerTypeNames().values().toArray(new String[]{})));
			txtTrackerType.addActionListener((ActionListener)listener); //Enable Listener
			txtTrackerType.setSelectedItem( //Select item triggering Listener (for GUI Changes)
					parameters.getTrackerParameters().getTrackerTypeNames().get(
							parameters.getTrackerParameters().getTrackerType()));

			//KLT Tracker templateRadius TextField
			JTextField txtKltTracker_templateRadius = (JTextField)parameters.getGuiComponents().get("txtKltTracker_templateRadius");
			txtKltTracker_templateRadius.setText(String.valueOf(parameters.getTrackerParameters().getKltTracker_templateRadius()));

			//KLT Tracker pyramidScaling TextField
			JTextField txtKltTracker_pyramidScaling = (JTextField)parameters.getGuiComponents().get("txtKltTracker_pyramidScaling");
			txtKltTracker_pyramidScaling.setText(parameters.getTrackerParameters().getKltTracker_pyramidScaling());

			//KLT Tracker maxFeatures TextField
			JTextField txtKltTracker_maxFeatures = (JTextField)parameters.getGuiComponents().get("txtKltTracker_maxFeatures");
			txtKltTracker_maxFeatures.setText(String.valueOf(parameters.getTrackerParameters().getKltTracker_maxFeatures()));

			//KLT Tracker radius TextField
			JTextField txtKltTracker_radius = (JTextField)parameters.getGuiComponents().get("txtKltTracker_radius");
			txtKltTracker_radius.setText(String.valueOf(parameters.getTrackerParameters().getKltTracker_radius()));

			//KLT Tracker threshold TextField
			JTextField txtKltTracker_threshold = (JTextField)parameters.getGuiComponents().get("txtKltTracker_threshold");
			txtKltTracker_threshold.setText(String.valueOf(parameters.getTrackerParameters().getKltTracker_threshold()));
			
			//SURF Tracker maxFeaturesPerScale TextField
			JTextField txtSurfTracker_maxFeaturesPerScale = (JTextField)parameters.getGuiComponents().get("txtSurfTracker_maxFeaturesPerScale");
			txtSurfTracker_maxFeaturesPerScale.setText(String.valueOf(parameters.getTrackerParameters().getSurfTracker_maxFeaturesPerScale()));

			//SURF Tracker extractRadius TextField
			JTextField txtSurfTracker_extractRadius = (JTextField)parameters.getGuiComponents().get("txtSurfTracker_extractRadius");
			txtSurfTracker_extractRadius.setText(String.valueOf(parameters.getTrackerParameters().getSurfTracker_extractRadius()));

			//SURF Tracker initialSampleSize
			JTextField txtSurfTracker_initialSampleSize = (JTextField)parameters.getGuiComponents().get("txtSurfTracker_initialSampleSize");
			txtSurfTracker_initialSampleSize.setText(String.valueOf(parameters.getTrackerParameters().getSurfTracker_initialSampleSize()));

			//Tracker Show Active Tracks CheckBox
			JCheckBox chkTrackerShowActiveTracks = (JCheckBox)parameters.getGuiComponents().get("chkTrackerShowActiveTracks");
			chkTrackerShowActiveTracks.setSelected(parameters.getTrackerParameters().isTrackerShowActiveTracks());
			for(ActionListener actionListener: chkTrackerShowActiveTracks.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkTrackerShowActiveTracks, ActionEvent.ACTION_PERFORMED, null));
			}

			//Tracker Show New Tracks CheckBox
			JCheckBox chkTrackerShowNewTracks = (JCheckBox)parameters.getGuiComponents().get("chkTrackerShowNewTracks");
			chkTrackerShowNewTracks.setSelected(parameters.getTrackerParameters().isTrackerShowNewTracks());
			for(ActionListener actionListener: chkTrackerShowNewTracks.getActionListeners()){ //Trigger Listener
				actionListener.actionPerformed(new ActionEvent(chkTrackerShowNewTracks, ActionEvent.ACTION_PERFORMED, null));
			}

			
			
			/**Visual Odometry Settings Panel Reloading**/
			
			
			//Visual Odometry Type ComboBox
			JComboBox txtVisualOdometryType = (JComboBox)parameters.getGuiComponents().get("txtVisualOdometryType");
			listener = null;
			for(ActionListener actionListener: txtVisualOdometryType.getActionListeners()){
				if(actionListener instanceof VisualOdometryTypeChangeListener) listener = actionListener;
			}
			txtVisualOdometryType.removeActionListener((ActionListener)listener); //Disable Listener (not needed when only changing model, but safer)
				//Reloading Contents
				txtVisualOdometryType.setModel(new DefaultComboBoxModel<>(
						parameters.getVisualOdometryParameters().getVisualOdometryTypeNames().values().toArray(new String[]{})));
			txtVisualOdometryType.addActionListener((ActionListener)listener); //Enable Listener
			txtVisualOdometryType.setSelectedItem( //Select item triggering Listener (for GUI Changes)
					parameters.getVisualOdometryParameters().getVisualOdometryTypeNames().get(
							parameters.getVisualOdometryParameters().getVisualOdometryType()));
			
			//monoPlaneInfinity thresholdAdd TextField
			JTextField txtMonoPlaneInfinity_thresholdAdd = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneInfinity_thresholdAdd");
			txtMonoPlaneInfinity_thresholdAdd.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_thresholdAdd()));

			//monoPlaneInfinity thresholdRetire TextField
			JTextField txtMonoPlaneInfinity_thresholdRetire = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneInfinity_thresholdRetire");
			txtMonoPlaneInfinity_thresholdRetire.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_thresholdRetire()));

			//monoPlaneInfinity inlierPixelTol TextField
			JTextField txtMonoPlaneInfinity_inlierPixelTol = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneInfinity_inlierPixelTol");
			txtMonoPlaneInfinity_inlierPixelTol.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_inlierPixelTol()));

			//monoPlaneInfinity ransacIterations TextField
			JTextField txtMonoPlaneInfinity_ransacIterations = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneInfinity_ransacIterations");
			txtMonoPlaneInfinity_ransacIterations.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_ransacIterations()));

			//monoPlaneOverhead cellSize TextField
			JTextField txtMonoPlaneOverhead_cellSize = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_cellSize");
			txtMonoPlaneOverhead_cellSize.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_cellSize()));

			//monoPlaneOverhead maxCellsPerPixel TextField
			JTextField txtMonoPlaneOverhead_maxCellsPerPixel = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_maxCellsPerPixel");
			txtMonoPlaneOverhead_maxCellsPerPixel.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_maxCellsPerPixel()));

			//monoPlaneOverhead mapHeightFraction TextField
			JTextField txtMonoPlaneOverhead_mapHeightFraction = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_mapHeightFraction");
			txtMonoPlaneOverhead_mapHeightFraction.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_mapHeightFraction()));
			
			//monoPlaneOverhead inlierGroundTol TextField
			JTextField txtMonoPlaneOverhead_inlierGroundTol = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_inlierGroundTol");
			txtMonoPlaneOverhead_inlierGroundTol.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_inlierGroundTol()));
			
			//monoPlaneOverhead ransacIteration TextField
			JTextField txtMonoPlaneOverhead_ransacIteration = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_ransacIteration");
			txtMonoPlaneOverhead_ransacIteration.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_ransacIterations()));
			
			//monoPlaneOverhead thresholdRetire TextField
			JTextField txtMonoPlaneOverhead_thresholdRetire = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_thresholdRetire");
			txtMonoPlaneOverhead_thresholdRetire.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_thresholdRetire()));
			
			//monoPlaneOverhead absoluteMinimumTracks TextField
			JTextField txtMonoPlaneOverhead_absoluteMinimumTracks = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_absoluteMinimumTracks");
			txtMonoPlaneOverhead_absoluteMinimumTracks.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_absoluteMinimumTracks()));

			//monoPlaneOverhead respawnTrackFraction TextField
			JTextField txtMonoPlaneOverhead_respawnTrackFraction = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_respawnTrackFraction");
			txtMonoPlaneOverhead_respawnTrackFraction.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_respawnTrackFraction()));
			
			//monoPlaneOverhead respawnCoverageFraction TextField
			JTextField txtMonoPlaneOverhead_respawnCoverageFraction = (JTextField)parameters.getGuiComponents().get("txtMonoPlaneOverhead_respawnCoverageFraction");
			txtMonoPlaneOverhead_respawnCoverageFraction.setText(String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_respawnCoverageFraction()));
	

			
			/**Chart Settings Panel Reloading**/
			
			
			//Chart Type ComboBox
			JComboBox txtChartType = (JComboBox)parameters.getGuiComponents().get("txtChartType");
			listener = null;
			for(ActionListener actionListener: txtChartType.getActionListeners()){
				if(actionListener instanceof ChartTypeChangeListener) listener = actionListener;
			}
			txtChartType.removeActionListener((ActionListener)listener); //Disable Listener (not needed when only changing model, but safer)
				//Reloading Contents
				txtChartType.setModel(new DefaultComboBoxModel<>(
						parameters.getChartOutputParameters().getChartTypeNames().values().toArray(new String[]{})));
			txtChartType.addActionListener((ActionListener)listener); //Enable Listener				
			txtChartType.setSelectedItem( //Select item triggering Listener (for GUI Changes)
					parameters.getChartOutputParameters().getChartTypeNames().get(
							parameters.getChartOutputParameters().getChartType()));

			//Chart XZ Scale TextField
			JTextField txtChartXZ_Scale = (JTextField)parameters.getGuiComponents().get("txtChartXZ_Scale");			
			txtChartXZ_Scale.setText(String.valueOf(parameters.getChartOutputParameters().getChartXZ_Scale()));
			//Applying loaded Chart XZ Scale
			ChartScrollPane chartXZPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartXZPanel");
			chartXZPanel.setChartScalingFactor(parameters.getChartOutputParameters().getChartXZ_Scale());
			chartXZPanel.resetSize();
			
			//Chart Y Scale TextField
			JTextField txtChartY_Scale = (JTextField)parameters.getGuiComponents().get("txtChartY_Scale");
			txtChartY_Scale.setText(String.valueOf(parameters.getChartOutputParameters().getChartY_Scale()));
			//Applying loaded Chart Y Scale
			ChartScrollPane chartYPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartYPanel");
			chartYPanel.setChartScalingFactor(parameters.getChartOutputParameters().getChartY_Scale());
			chartYPanel.resetSize();
			
			return true;
		}catch(Exception exc){
			return false;
		}	
	}
	
	
	public Core getCore(){
		return this.core;
	}
	
	public void setCore(Core core){
		this.core = core;
	}


	private static final class MainButtonListener extends MouseAdapter implements ActionListener {

		//Basic Parameters
		private String function;
		private Core core;

		//Parameters for MouseListener mode only (single/double click management)
        private MouseEvent mouseEvent;
        private Timer timer;
	    private int clickInterval;

		//Parameters for Load/Save Buttons only
		private static String saveFormat="XML";//Can be XML or Serialized (Object Output)

		//Declaration of components needed by the single-click functions
		private JButton btnStartVisualOdometry;
		private JButton btnPauseVisualOdometry;
		private JButton btnResetVisualOdometry;
		private JButton btnStopVisualOdometry;
		private JButton btnClearVisualOdometry;
		private JButton btnTimedStopVisualOdometry;
		private JTextField txtTimedStopVisualOdometry;
		private ChartScrollPane chartXZPanel;
		private ChartScrollPane chartYPanel;
		private InfoScrollPane chartInfoPanel;
		
		public MainButtonListener(String function, Core core) {
		
			this.function = function;
			this.core = core;
			
		}

		
		public void mouseClicked (MouseEvent evt) { //Triggers when this class is used as MouseListener
												  	//(Manages Button single-click/double-click)

			//If the class is used as MouseListener and clickInterval or Timer aren't initialized
			if(timer==null||clickInterval==0){ //Initilizes them to distinguish between click and doubleclick
				clickInterval = 200; //(Integer)Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
				timer = new Timer(clickInterval, this);
			}
			
			if (evt.getClickCount() > 2) return; //For more than 2 clicks exits
			
			mouseEvent = evt;
			if (!timer.isRunning()){ //If timer isn't started starts it
									 //and wait 200 ms for another click,
									 //if there isn't a second click the timer 
									 //triggers the actionListener for single click management
				timer.restart();
			}else{					//If timer is still running we have catched a second click,
				timer.stop();		//so the timer is stopped and doubleClick is managed 
				doubleClick(mouseEvent);
			}		
		}
	    
		
		@Override
		public void actionPerformed(ActionEvent evt) { //Triggered from this class (if is used as ActionListener) 
													   //or from the timer (if is used as MouseListener)
													   //(Manages Main Frame buttons single clicks)

            if(timer!=null)timer.stop(); //If the class is used as MouseListener stops the timer that was waiting for dblClick
            singleClick(mouseEvent==null?evt:mouseEvent); //Launches singleClick routine with the correct event (mouse or action) 
            											  //depending on how this class is used (MouseListener or ActionListener)
		}

		@SuppressWarnings("unused")
		public void singleClick(AWTEvent evt) {
			
			//Determines the correct Event Type (Depending if this class is used as Action or Mouse Listener)
			ActionEvent aevt = (evt instanceof ActionEvent)?(ActionEvent) evt:null; 
			MouseEvent mevt = (evt instanceof MouseEvent)?(MouseEvent) evt:null;
			
			//Loads current instance of Parameters
			Parameters parameters = core.getParameters();
			
			//Extracts components needed by the single-click functions only if they haven't been already extracted
			//(does this at Runtime, here in the singleClick trigger, because all the guiComponents are surely loaded)
			if(btnStartVisualOdometry==null||btnPauseVisualOdometry==null||btnResetVisualOdometry==null
			 ||btnStopVisualOdometry==null||btnClearVisualOdometry==null||btnTimedStopVisualOdometry==null
			 ||txtTimedStopVisualOdometry==null||chartXZPanel==null||chartYPanel==null||chartInfoPanel==null){
				
				btnStartVisualOdometry = (JButton)parameters.getGuiComponents().get("btnStartVisualOdometry");
				btnPauseVisualOdometry = (JButton)parameters.getGuiComponents().get("btnPauseVisualOdometry");
				btnResetVisualOdometry = (JButton)parameters.getGuiComponents().get("btnResetVisualOdometry");
				btnStopVisualOdometry = (JButton)parameters.getGuiComponents().get("btnStopVisualOdometry");
				btnClearVisualOdometry = (JButton)parameters.getGuiComponents().get("btnClearVisualOdometry");
				btnTimedStopVisualOdometry = (JButton)parameters.getGuiComponents().get("btnTimedStopVisualOdometry");
				txtTimedStopVisualOdometry = (JTextField)parameters.getGuiComponents().get("txtTimedStopVisualOdometry");
				chartXZPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartXZPanel");
				chartYPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartYPanel");
				chartInfoPanel = (InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel");
			}
			
			switch(function){	//Depending on function value associated to the button acts differently:
				case "loadSettings": //On click on Load Settings
					switch(saveFormat){
						case "XML":
							try{
								XStream xstream = new XStream();
								ArrayList loadedArray = (ArrayList)xstream.fromXML(new File("Parameters.xml"));
								Parameters loadedParameters = 
										new Parameters((InputParameters)loadedArray.get(0), 
													   (InternalImageParameters)loadedArray.get(1), 
													   (TrackerParameters)loadedArray.get(2), 
													   (VisualOdometryParameters)loadedArray.get(3), 
													   (ChartOutputParameters)loadedArray.get(4), 
													   parameters.getProcessingParameters(), 
													   parameters.getProcessingFlags(), 
													   parameters.getDeviceParameters(),
													   parameters.getGuiComponents());
								
								boolean loadSuccess = reloadParameters(loadedParameters, core);
								//Updates Status Label content
								if(loadSuccess)
									chartInfoPanel.lbl_status.setText("<html><b>Status:</b> XML Settings successfully loaded (from: "+System.getProperty("user.dir")+"/Parameters.xml)</html>");
								else throw new Exception();

							}catch (Exception exc){
								if(!new File("Parameters.xml").exists()){
									chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error occurred loading settings (XML Format): Save file not found ("+System.getProperty("user.dir")+"/Parameters.xml)</html>");
								}else{
									chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error occurred loading settings (XML Format)</html>");
								}
							}
							break;
						case "Serialized":
							try{
								ObjectInputStream objectInputStream = new ObjectInputStream(
										new FileInputStream("Parameters.dat"));
								
								Parameters loadedParameters = 
										new Parameters((InputParameters)objectInputStream.readObject(), 
													   (InternalImageParameters)objectInputStream.readObject(), 
													   (TrackerParameters)objectInputStream.readObject(),
													   (VisualOdometryParameters)objectInputStream.readObject(), 
													   (ChartOutputParameters)objectInputStream.readObject(), 
													   parameters.getProcessingParameters(), 
													   parameters.getProcessingFlags(), 
													   parameters.getDeviceParameters(), 
													   parameters.getGuiComponents());
								objectInputStream.close();
								
								boolean loadSuccess = reloadParameters(loadedParameters, core);
								//Updates Status Label content
								if(loadSuccess)
									chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Serialized Settings successfully loaded (from: "+System.getProperty("user.dir")+"/Parameters.dat)</html>");
								else throw new Exception();
							}catch (Exception exc){
								if(!new File("Parameters.dat").exists()){
									chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error occurred loading settings (Serialized Format): Save file not found ("+System.getProperty("user.dir")+"/Parameters.dat)</html>");
								}else{
									chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error occurred loading settings (Serialized Format)</html>");
								}
							}
							break;
						default:
							break;
					}
					break;
				case "saveSettings": //On single-click on Save Settings
					switch(saveFormat){
						case "XML":
							try {
								ArrayList<Object> parametersToWrite = new ArrayList<Object>();
								parametersToWrite.add(parameters.getInputParameters());
								parametersToWrite.add(parameters.getInternalImageParameters());
								parametersToWrite.add(parameters.getTrackerParameters());
								parametersToWrite.add(parameters.getVisualOdometryParameters());
								parametersToWrite.add(parameters.getChartOutputParameters());
								
								XStream xstream = new XStream();
						        String xmlOutput = xstream.toXML(parametersToWrite);
								byte[] contentInBytes = xmlOutput.getBytes();

								FileOutputStream fileOutputStream = new FileOutputStream("Parameters.xml");
								fileOutputStream.write(contentInBytes);
								fileOutputStream.flush();
								fileOutputStream.close();
								
								//Updates Status Label content
								chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Settings successfully saved in XML format (to: "+System.getProperty("user.dir")+"/Parameters.xml)</html>");
							}catch (IOException exc){
								chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error saving settings in XML Format</html>");
								exc.printStackTrace();
							}
							break;
						case "Serialized":
							try{
								ObjectOutputStream objectOutputStream = new ObjectOutputStream(
										new FileOutputStream("Parameters.dat"));
								objectOutputStream.writeObject(parameters.getInputParameters());
								objectOutputStream.writeObject(parameters.getInternalImageParameters());
								objectOutputStream.writeObject(parameters.getTrackerParameters());
								objectOutputStream.writeObject(parameters.getVisualOdometryParameters());
								objectOutputStream.writeObject(parameters.getChartOutputParameters());
								objectOutputStream.flush();
								objectOutputStream.close();

								//Updates Status Label content
								chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Settings successfully saved in Serialized format (to: "+System.getProperty("user.dir")+"/Parameters.dat)</html>");
							}catch (Exception exc){
								chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error saving settings in Serialized Format</html>");
								exc.printStackTrace();
							}
							break;
						default:
							break;
					}
					break;
				case "resetSettings": //On click on Reset Settings
					
					Parameters resetParameters = new Parameters(); //Creates default parameters
					resetParameters.setProcessingParameters(core.getParameters().getProcessingParameters()); //Preserve processing/device/gui reserved parameters
					resetParameters.setProcessingFlags(core.getParameters().getProcessingFlags());
					resetParameters.setDeviceParameters(core.getParameters().getDeviceParameters());
					resetParameters.setGuiComponents(core.getParameters().getGuiComponents());
	
					boolean resetSuccess = reloadParameters(resetParameters, core); //Resets parameters to Default
					//Updates Status Label content
					if(resetSuccess){
						chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Settings successfully reset to default</html>");
					}else{
						chartInfoPanel.lbl_status.setText("<html><b>Status:</b> Error occurred resetting settings to default</html>");
					}
					break;
				case "startVisualOdometry": //On click on Start Visual Odometry

					//Disable Start Button and enable all other functions (pause,reset,stop,...)
					btnStartVisualOdometry.setEnabled(false);
					btnPauseVisualOdometry.setEnabled(true);
					btnResetVisualOdometry.setEnabled(true);
					btnStopVisualOdometry.setEnabled(true);
					btnClearVisualOdometry.setEnabled(true);
					//If we have Device input source:
					if(parameters.getInputParameters().getInputSource().equals(InputParameters.DEVICE_INPUT)){
						btnTimedStopVisualOdometry.setEnabled(true); //Enables Time Stop button and textfield
						txtTimedStopVisualOdometry.setEnabled(true);
					}
					
					//Creates a separated Thread to run Visual Odometry (core) process
					Thread thread = new Thread(new Runnable(){
						@Override
						public void run() {//While in the Thread:
							core.start(); //Starts Visual Odometry (core) Process here (based on Parameters object)
							
							//At the end of the Visual Odometry Process enable Start Button and disable all other functions
							//except for the Clear Button
							btnStartVisualOdometry.setEnabled(true);
							btnPauseVisualOdometry.setEnabled(false);
							btnResetVisualOdometry.setEnabled(false);
							btnStopVisualOdometry.setEnabled(false);
							btnTimedStopVisualOdometry.setEnabled(false); //Disables Time Stop button and textfield
							txtTimedStopVisualOdometry.setEnabled(false);
							//If the XZ Chart contains 0 points (the Visual Odometry Process hasn't generated anything)
							//or the Clear Flag has been set and the VO cleared and stopped, disable Clear Button also
							if(chartXZPanel!=null && chartXZPanel.getAllPoints().size()<=0) btnClearVisualOdometry.setEnabled(false);
						}
					});
					thread.start(); //Run Visual Odometry Thread
					break;
				case "pauseVisualOdometry": //On click on Pause/Resume Visual Odometry Button
					
					//Extracts Pause Flag from Processing Flags
					boolean pauseFlag = parameters.getProcessingFlags().isPauseVisualOdometry();
					
					//Sets/unsets Pause Processing Flag
					parameters.getProcessingFlags().setPauseVisualOdometry(!pauseFlag);
					
					//Changes Pause Button name
					btnPauseVisualOdometry.setText(!pauseFlag ? "Resume" : "Pause"); 
					
					break;
				case "resetVisualOdometry": //On click on Reset Visual Odometry Button
					
					//Sets the Reset Processing Flag (trigger Reset VO Context)
					parameters.getProcessingFlags().setResetVisualOdometry(true);
					
					//Updates Status Label content
					chartInfoPanel.lbl_status.setText("<html><b>Status:</b> VO Context Reset requested.</html>");
					
					break;
				case "stopVisualOdometry": //On click on Stop Visual Odometry Button
						
					//Unsets Pause Processing Flag (if it was set to true)
					parameters.getProcessingFlags().setPauseVisualOdometry(false); 
					//Changes Pause Button name
					btnPauseVisualOdometry.setText("Pause");
					
					//Sets Stop Processing Flag
					parameters.getProcessingFlags().setStopVisualOdometry(true); 
						
					//Enables start button and disables all other buttons (except for clear button)
					btnStartVisualOdometry.setEnabled(true);
					btnPauseVisualOdometry.setEnabled(false);
					btnResetVisualOdometry.setEnabled(false);
					btnStopVisualOdometry.setEnabled(false);
					btnTimedStopVisualOdometry.setEnabled(false);
					txtTimedStopVisualOdometry.setEnabled(false);
					break;
				case "clearVisualOdometry": //On click on Clear Visual Odometry Button
					//STOP PROCESSING AND CLEAR ALL
					
					//If a Visual Odometry is running (Processing Flag is true):
					if(parameters.getProcessingFlags().isProcessingVisualOdometry()){	
						
						//Removes the Pause Processing Flag (if it was set to true)
						parameters.getProcessingFlags().setPauseVisualOdometry(false);	
						//Changes Pause Button name
						btnPauseVisualOdometry.setText("Pause"); 
						
						//Removes the Stop Processing Flag (to be sure)
						parameters.getProcessingFlags().setStopVisualOdometry(false);
						
						//Enables the Clear Processing Flag (so that the core will stop
						//the processing and clear all the Visual Odometry)
						parameters.getProcessingFlags().setClearVisualOdometry(true);	
											
					}else{//If no Visual Odometry is running (Processing Flag is false)
						
						//Performs the Visual Odometry clearing here
						chartXZPanel.clearAllPoints(); //Clear all the XZ Chart data
						chartXZPanel.resetSize();	   //Resets the XZ Chart
						chartYPanel.clearAllPoints();  //Clear all the Y Chart data
						chartYPanel.resetSize();	   //Resets the Y Chart
						chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Cleared.</html>");
						chartInfoPanel.setInfoPanelVisible(false);	//Makes invisible the chartInfoPanel
						chartInfoPanel.clearListData();				//Clear all the Points List Data in the chartInfoPanel
					}
					
					//In the end enable start button and disable all the other buttons and the Timed Stop TextField
					btnStartVisualOdometry.setEnabled(true);
					btnPauseVisualOdometry.setEnabled(false);
					btnResetVisualOdometry.setEnabled(false);
					btnStopVisualOdometry.setEnabled(false);
					btnClearVisualOdometry.setEnabled(false);
					btnTimedStopVisualOdometry.setEnabled(false);
					txtTimedStopVisualOdometry.setEnabled(false);
					break;
				case "timedStopVisualOdometry": //On click on Timed Stop Button
					//Stops device capture after the time set into Timed Stop textfield:
					
					btnTimedStopVisualOdometry.setEnabled(false); //Disable the button
					txtTimedStopVisualOdometry.setEnabled(false); //and the TextField
					
					//Parse total seconds from the textfield content
					final int totalSeconds = Integer.parseInt(txtTimedStopVisualOdometry.getText());
					
					//Creates a separated Thread that will stop the capture after specified seconds
					Thread timedStopThread = new Thread(new Runnable(){
						@Override
						public void run() {//When the thread runs:
			
							final long startTime = System.currentTimeMillis(); //Saves the current time into start variable
							int countedSeconds=0;
								
							//Repeats this cycle until totalSeconds are past
							while(System.currentTimeMillis()<startTime+(totalSeconds*1000)){ 
																								
								try {
									Thread.sleep(1000);	//Sleeps for 1 second (1000 msec)
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
									
								//If no Visual Odometry is running (the Process has terminated before the countdown has)
								//resets the Timed Stop textfield and exit the Thread
								if(!parameters.getProcessingFlags().isProcessingVisualOdometry()){
									txtTimedStopVisualOdometry.setText(String.valueOf(totalSeconds));return;}
									
								//Increases counted seconds by 1
								countedSeconds++;
								//Decreases Timed Stop textfield content by 1
								txtTimedStopVisualOdometry.setText(String.valueOf(totalSeconds-countedSeconds));
							
							}
								
							//After the cycle, if the Visual Odometry is still running:
							if(parameters.getProcessingFlags().isProcessingVisualOdometry())
								parameters.getDeviceParameters().setStopCaptureFlag(true); 	//Sets the Stop Device Capture Flag to true, 
																//so that the Device will stop providing input (but the Visual Odometry 
																//process, will (in case) continue to process remaining buffered frames)

							//The textfield returns to the totalSeconds value
							txtTimedStopVisualOdometry.setText(String.valueOf(totalSeconds)); 
						}	
					});
					//Starts the Timed Stop Thread 
					timedStopThread.start();
					break;
			}
			
		}

		
		public void doubleClick(MouseEvent evt) {
			switch(function){
				case "saveSettings":
					int choice = JOptionPane.showOptionDialog(core.getParameters().getGuiComponents().get("mainFrame"),
															  "Do you want to change Save Format? (Actual save format: "+saveFormat+")",
															  "Change Save Format",
															  JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE, 
															  null, 
															  new String[]{"Serialized", "XML"},
								   						  	  "XML");
					switch(choice){
						case 1: //XML
							saveFormat = "XML";
							break;
						case 0: //Serialized
							saveFormat = "Serialized";
							break;
						default: //If canceled leaves current saveFormat
							break;
					}
			}
		}
	}

	
	private final class PathDocument extends PlainDocument {
		
		private static final long serialVersionUID = 1L;
		private PathChangeListener pathChangeListener;

		private PathDocument() {
			this.pathChangeListener = null;
		}

		@Override
		public void removeDocumentListener(DocumentListener listener) {
			super.removeDocumentListener(listener);
			if(listener instanceof PathChangeListener && listener.equals(pathChangeListener))
				{this.pathChangeListener = null;}
		}

		@Override
		public void addDocumentListener(DocumentListener listener) {
			super.addDocumentListener(listener);
			if(listener instanceof PathChangeListener){this.pathChangeListener = (PathChangeListener)listener;}
		}
		
		public PathChangeListener getPathChangeListener(){
			return this.pathChangeListener;
		}
	}

	
	private final class PathChangeListener implements DocumentListener {
		
		private final String pathType;
		private final JTextComponent pathTextComponent;
		private final Core core;
		
		private PathChangeListener(String pathType, JTextComponent pathTextComponent, Core core) {
			
			this.pathType = pathType;
			this.pathTextComponent = pathTextComponent;
			this.core = core;
			
		}

		@Override
		public void removeUpdate(DocumentEvent evt) { //When removing characters
			this.pathUpdate(); //Updates the Path
		}

		@Override
		public void insertUpdate(DocumentEvent evt) { //When inserting characters
			this.pathUpdate(); //Updates the Path
		}

		@Override
		public void changedUpdate(DocumentEvent evt) {//When changing the selection
			this.pathUpdate(); //Updates the Path
		}
		
		public void pathUpdate(){
			
			//Loads current inputParameters instance
			InputParameters inputParameters = core.getParameters().getInputParameters();
			
			switch(pathType){	//Depending on pathType value
				case "calibrationPath":
					inputParameters.setCalibrationPath(pathTextComponent.getText());//updates Calibration Path parameter
					break;
				case "videoPath":
					inputParameters.setVideoPath(pathTextComponent.getText()); 		//updates Video Path parameter
					break;
				case "devicePath":
					inputParameters.setDevicePath(pathTextComponent.getText()); 	//updates Device Path parameter
					break;
			}
		}
	}

	
	private final class BrowseButtonListener implements ActionListener {
		
		private final HashMap<String, Component> mainFrameContainer;
		private final JTextComponent pathTextComponent;
		private final String dialogTitle;
		private final String[] dialogFileFilterExtension;
		private final String[] dialogFileFilterDescription;
		private final boolean enableDirectorySelection;

		private BrowseButtonListener(HashMap<String, Component> mainFrameContainer, JTextComponent pathTextComponent, String dialogTitle, 
				String[] dialogFileFilterExtension, String[] dialogFileFilterDescription, boolean enableDirectorySelection) {
			
			this.mainFrameContainer = mainFrameContainer;
			this.pathTextComponent = pathTextComponent;
			this.dialogTitle = dialogTitle;
			this.dialogFileFilterExtension = dialogFileFilterExtension;
			this.dialogFileFilterDescription = dialogFileFilterDescription;
			this.enableDirectorySelection = enableDirectorySelection;
			
		}

		@Override
		public void actionPerformed(ActionEvent evt) {		//When clicking the browsing button
			String dialogPath = pathTextComponent.getText();//assumes the current Path TextComponent			
															//content as the current searching path
			
			JFileChooser browse = new JFileChooser(dialogPath);	//Creates a new File Browsing Dialog at dialogPath path
			browse.setDialogTitle(dialogTitle);					//Sets title to dialogTitle
			for(int i=0;i<dialogFileFilterExtension.length;i++){
				final String fileExt = dialogFileFilterExtension[i];
				final String fileDesc = dialogFileFilterDescription[i];
				browse.setFileFilter(new FileFilter() {	//Creates a new file filter for each passed extension

					@Override
					public boolean accept(File file) { 	//File filter accepted extensions
						return file.getName().endsWith(fileExt)||file.isDirectory();  
						}
					
					@Override
					public String getDescription() {	//File filter description
						return fileDesc; 	
						}
				});
			}

			if(dialogFileFilterExtension.length>1){					
				browse.setFileFilter(new FileFilter() {	//Creates a new file filter for all supported extension

					@Override
					public boolean accept(File file) { 	//File filter accepted extensions
						boolean accepted = false;
						for(String fileExt: dialogFileFilterExtension){
							accepted = accepted || file.getName().endsWith(fileExt);
						}
						return accepted||file.isDirectory();  
						}
					
					@Override
					public String getDescription() {	//File filter description
						if(dialogFileFilterDescription.length==dialogFileFilterExtension.length+1)
							return dialogFileFilterDescription[dialogFileFilterDescription.length-1];
						else{
							String fileDesc = "All supported files (";
							for(String fileExt: dialogFileFilterExtension){
								fileDesc+="*"+fileExt+", ";
							}
							fileDesc = fileDesc.substring(0, fileDesc.length()-2)+")";
							return fileDesc;
						}
						}
				});
			}
			
			browse.setFileSelectionMode(enableDirectorySelection?JFileChooser.FILES_AND_DIRECTORIES:JFileChooser.FILES_ONLY);
			
			if(browse.showOpenDialog(mainFrameContainer.get("mainFrame"))==0){//Opens the File Browsing Dialog and if an existing file has been selected
				File choice = browse.getSelectedFile(); //Gets the selected file
				pathTextComponent.setText(choice.getAbsolutePath()); 	//Sets the Path TextComponent content
																		//to the file path (triggering also a change
																		//to the Path parameter, thanks to
																		//the TextComponent change Listener)
			}
		}
	}

		
	private final class InputSourceOptionListener implements ActionListener {
		
		private final String inputSource;
		private final Core core;
		
		private InputSourceOptionListener(String inputSource, Core core) {
			
			this.inputSource = inputSource;
			this.core = core;
		
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent evt) {//An Input Source option button has been clicked
													  //(could be Video or Device)

			//Loads current Parameters
			Parameters parameters = core.getParameters();

			//Extracts from guiComponents all the components needed (Video/Device components) 
			final JRadioButton optVideoSource = (JRadioButton) parameters.getGuiComponents().get("optVideoSource");
			final JComboBox<String> txtVideoSource = (JComboBox<String>) parameters.getGuiComponents().get("txtVideoSource");
			final JButton btnVideoSourceBrowsing = (JButton) parameters.getGuiComponents().get("btnVideoSourceBrowsing");
			
			final JRadioButton optDeviceSource = (JRadioButton) parameters.getGuiComponents().get("optDeviceSource");
			final JComboBox<String> txtDeviceSource = (JComboBox<String>) parameters.getGuiComponents().get("txtDeviceSource");
			final JPanel deviceAdjustmentsPanel = (JPanel) parameters.getGuiComponents().get("deviceAdjustmentsPanel");
			final JLabel lblDeviceWidth = (JLabel) parameters.getGuiComponents().get("lblDeviceWidth");
			final JLabel lblDeviceHeight = (JLabel) parameters.getGuiComponents().get("lblDeviceHeight");
			
			final JLabel lblImageBufferSize = (JLabel) parameters.getGuiComponents().get("lblImageBufferSize");
			final JTextField txtImageBufferSize = (JTextField) parameters.getGuiComponents().get("txtImageBufferSize");
			
			
			//Input Source parameter is set to inputSource (VIDEO_INPUT("video") or DEVICE_INPUT("device"))
			parameters.getInputParameters().setInputSource(inputSource);
			
			boolean isVideo = inputSource.equals(InputParameters.VIDEO_INPUT);
			boolean isDevice = inputSource.equals(InputParameters.DEVICE_INPUT);
			
					//Modify visibility of components from the Input Settings Panel
					optVideoSource.setSelected(isVideo);					//Select/unselect Video Source OptionButton
					optVideoSource.setText(									//Sets Video Source OptionButton Text
							isVideo?"<html><b>Video:</b></html>":"<html>Video:</html>"); 	
					txtVideoSource.setEnabled(isVideo);						//Enable/disable Video Source ComboBox
					btnVideoSourceBrowsing.setEnabled(isVideo);				//Enable/disable Video Source Browsing Button
				
					optDeviceSource.setSelected(isDevice);					//Select/unselect Device Source OptionButton
					optDeviceSource.setText(								//Sets Device Source OptionButton Text
							isDevice?"<html><b>Device:</b></html>":"<html>Device:</html>");		
					txtDeviceSource.setEnabled(isDevice);					//Enable/disable Device Source ComboBox
					deviceAdjustmentsPanel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createTitledBorder(isDevice?"<html><b>Device Adjustments</b></html>":"<html>Device Adjustments</html>"),
							BorderFactory.createEmptyBorder(5,5,5,5)));		//Sets Device Adjustments Panel title and border
					deviceAdjustmentsPanel.setEnabled(isDevice);			//Enable/disable Device Adjustments Panel
					for(Component comp: deviceAdjustmentsPanel.getComponents()){	
						comp.setEnabled(isDevice);							 //Each component in Device Adjustments Panel is enabled/disabled
					}
					lblDeviceWidth.setText(									//Sets Device Width Label Text
							isDevice?"<html><b>Width:</b></html>":"<html>Width:</html>");			
					lblDeviceHeight.setText(								//Sets Device Height Label Text
							isDevice?"<html><b>Height:</b></html>":"<html>Height:</html>");		
				
					//Modify visibility of components from the Input Settings Panel (Device-related)
					lblImageBufferSize.setEnabled(isDevice);	//Image Buffer Size Label is disabled
					txtImageBufferSize.setEnabled(isDevice);	//Image Buffer Size TextField is disabled
		}
	}
	

	private final class IntegerParameterTextFieldListener implements FocusListener {
		
		private final String controlledParameter;
		private final JTextField controllerTextField;
		private final Core core;
		String	lastValue;

		private IntegerParameterTextFieldListener(String controlledParameter, JTextField controllerTextField, Core core) {
		
			this.controlledParameter = controlledParameter;
			this.controllerTextField = controllerTextField;
			this.core = core;
			
		}

			
		@Override
		public void focusGained(FocusEvent evt) { //When the TextField gains Focus
			
			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			//Do this for some parameters only:
			switch(controlledParameter){
				case "imageBufferSize":
					((JLabel)parameters.getGuiComponents().get("lblImageBufferSize")).setText(
							"<html><b>Image Buffer Size</b> (Device only):</html>");
					break;
			}
			try{//Then (always) try this:
				Integer.parseInt(controllerTextField.getText());//Try to read TextField value as an Integer before modifications
				lastValue = controllerTextField.getText();		//If it's a correct integer value saves it in lastValue
			}catch(Exception e){
				//Else resets lastValue to the default/saved integer parameter
				switch(controlledParameter){
					case "deviceWidth":
						lastValue = String.valueOf(parameters.getInputParameters().getDeviceWidth());
						break;
					case "deviceHeight":
						lastValue = String.valueOf(parameters.getInputParameters().getDeviceHeight());
						break;
					case "imageResizeWidth":
						lastValue = String.valueOf(parameters.getInternalImageParameters().getImageResizeWidth());
						break;
					case "imageResizeHeight":
						lastValue = String.valueOf(parameters.getInternalImageParameters().getImageResizeHeight());
						break;
					case "imageBufferSize":
						lastValue = String.valueOf(parameters.getInternalImageParameters().getImageBufferSize());
						break;
					case "frameDecimateValue":
						lastValue = String.valueOf(parameters.getInternalImageParameters().getFrameDecimateValue());
						break;
					case "kltTracker_templateRadius":
						lastValue = String.valueOf(parameters.getTrackerParameters().getKltTracker_templateRadius());
						break;
					case "kltTracker_maxFeatures":
						lastValue = String.valueOf(parameters.getTrackerParameters().getKltTracker_maxFeatures());
						break;
					case "kltTracker_radius":
						lastValue = String.valueOf(parameters.getTrackerParameters().getKltTracker_radius());
						break;
					case "surfTracker_maxFeaturesPerScale":
						lastValue = String.valueOf(parameters.getTrackerParameters().getSurfTracker_maxFeaturesPerScale());
						break;
					case "surfTracker_extractRadius":
						lastValue = String.valueOf(parameters.getTrackerParameters().getSurfTracker_extractRadius());
						break;
					case "monoPlaneInfinity_thresholdAdd":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_thresholdAdd());
						break;
					case "monoPlaneInfinity_thresholdRetire":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_thresholdRetire());
						break;
					case "monoPlaneInfinity_ransacIterations":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_ransacIterations());
						break;
					case "monoPlaneOverhead_ransacIterations":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_ransacIterations());
						break;
					case "monoPlaneOverhead_thresholdRetire":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_thresholdRetire());
						break;
					case "monoPlaneOverhead_absoluteMinimumTracks":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_absoluteMinimumTracks());
						break;
					case "timedStopVisualOdometry":
						lastValue = String.valueOf("10"); //Pick 10 seconds if no integer was inserted
						break;
				}
			}
		}

		@Override
		public void focusLost(FocusEvent evt) {	//When the TextField loses Focus
			
			//Loads Parameters
			Parameters parameters = core.getParameters();
			
			//Do this for some parameters only:
			switch(controlledParameter){
				case "imageBufferSize":
					((JLabel)parameters.getGuiComponents().get("lblImageBufferSize")).setText(
							"<html>Image Buffer Size (Device only):</html>");
					break;
			}
			try{//Then (always) try this:
				int currentValue = Integer.parseInt(controllerTextField.getText());//Try to read TextField value as an Integer after modifications/loss of focus

				//If it's a correct integer value:
				switch(controlledParameter){//Depending on controlled integer parameter acts differently:
						
					//For Strictly-Positive Integer Parameters (only >0):
					//If inserted value is >0 then updates the integer parameter
					//If inserted value is =0 restores lastValue as TextField value, and doesn't update integer parameter
					//If inserted value is <0 then sets TextField value to the opposite value 
					//and updates also the integer parameter to the opposite value
					case "deviceWidth":
						if(currentValue>0){parameters.getInputParameters().setDeviceWidth(currentValue);}
						else if(currentValue==0){controllerTextField.setText(lastValue);}
						else if(currentValue<0){ 
							controllerTextField.setText(String.valueOf(-currentValue));
							parameters.getInputParameters().setDeviceWidth(-currentValue);
						}
						break;
					case "deviceHeight":
						if(currentValue>0){parameters.getInputParameters().setDeviceHeight(currentValue);}
						else if(currentValue==0){controllerTextField.setText(lastValue);}
						else if(currentValue<0){ 
							controllerTextField.setText(String.valueOf(-currentValue));
							parameters.getInputParameters().setDeviceHeight(-currentValue);
						}
						break;
					case "imageResizeWidth":
						if(currentValue>0){parameters.getInternalImageParameters().setImageResizeWidth(currentValue);}
						else if(currentValue==0){controllerTextField.setText(lastValue);}
						else if(currentValue<0){ 
							controllerTextField.setText(String.valueOf(-currentValue));
							parameters.getInternalImageParameters().setImageResizeWidth(-currentValue);
						}
						break;
					case "imageResizeHeight":
						if(currentValue>0){parameters.getInternalImageParameters().setImageResizeHeight(currentValue);}
						else if(currentValue==0){controllerTextField.setText(lastValue);}
						else if(currentValue<0){ 
							controllerTextField.setText(String.valueOf(-currentValue));
							parameters.getInternalImageParameters().setImageResizeHeight(-currentValue);
						}
						break;
					case "frameDecimateValue":
						if(currentValue>0){parameters.getInternalImageParameters().setFrameDecimateValue(currentValue);}
						else if(currentValue==0){controllerTextField.setText(lastValue);}
						else if(currentValue<0){ 
							controllerTextField.setText(String.valueOf(-currentValue));
							parameters.getInternalImageParameters().setFrameDecimateValue(-currentValue);
						}
						break;
					
					//For Positive Integer Parameters (>=0):
					//If inserted value is >0 then updates the integer parameter
					//If inserted value is =0 then updates integer parameter
					//If inserted value is <0 then sets TextField value to the opposite value 
					//and updates also the integer parameter to the opposite value
					case "imageBufferSize":
						if(currentValue>=0){
							parameters.getInternalImageParameters().setImageBufferSize(currentValue);
							//If the value is INFINITEBUFFER(=0) the text is set to infinity
							if(currentValue==InternalImageParameters.INFINITEBUFFER)controllerTextField.setText("Infinity"); 
						}															   										
						else if(currentValue<0){
							controllerTextField.setText(String.valueOf(-currentValue));
							parameters.getInternalImageParameters().setImageBufferSize(-currentValue);
						}
						break;
					case "timedStopVisualOdometry":
						if(currentValue<0){controllerTextField.setText(String.valueOf(-currentValue));}
					break;
	
					//For Integer Parameters (<0 and >=0, all values):
					//For all values updates the integer parameter
					case "kltTracker_templateRadius":
						parameters.getTrackerParameters().setKltTracker_templateRadius(currentValue);
						break;
					case "kltTracker_maxFeatures":
						parameters.getTrackerParameters().setKltTracker_maxFeatures(currentValue);
						break;
					case "kltTracker_radius":
						parameters.getTrackerParameters().setKltTracker_radius(currentValue);
						break;
					case "surfTracker_maxFeaturesPerScale":
						parameters.getTrackerParameters().setSurfTracker_maxFeaturesPerScale(currentValue);
						break;
					case "surfTracker_extractRadius":
						parameters.getTrackerParameters().setSurfTracker_extractRadius(currentValue);
						break;
					case "monoPlaneInfinity_thresholdAdd":
						parameters.getVisualOdometryParameters().setMonoPlaneInfinity_thresholdAdd(currentValue);
						break;
					case "monoPlaneInfinity_thresholdRetire":
						parameters.getVisualOdometryParameters().setMonoPlaneInfinity_thresholdRetire(currentValue);
						break;
					case "monoPlaneInfinity_ransacIterations":
						parameters.getVisualOdometryParameters().setMonoPlaneInfinity_ransacIterations(currentValue);
						break;
					case "monoPlaneOverhead_ransacIterations":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_ransacIterations(currentValue);
						break;
					case "monoPlaneOverhead_thresholdRetire":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_thresholdRetire(currentValue);
						break;
					case "monoPlaneOverhead_absoluteMinimumTracks":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_absoluteMinimumTracks(currentValue);
						break;
				}
		
			}catch(Exception e){//If it's not an integer value
				switch(controlledParameter){//Depending on controlled integer parameter acts differently
				
					//For Integer Parameters that accept also some non integer values:
					case "imageBufferSize":
						//If the controlled parameter is imageBufferSize:
						//if the inserted value it's not an integer but is
						//a "" empty value or "Infinity" string, it's still
						//assumed as a correct value (INFINITEBUFFER Constant, = 0)
						if(controllerTextField.getText().isEmpty() || 
						   controllerTextField.getText().equalsIgnoreCase("Infinity")){
							controllerTextField.setText("Infinity"); //Sets the text to "Infinity" if is isEmpty() or has a different case
							parameters.getInternalImageParameters().setImageBufferSize(InternalImageParameters.INFINITEBUFFER);
						}else{ //else restores lastValue (that if is =INFINITEBUFFER=0, makes the text set to Infinity)
							controllerTextField.setText(
								lastValue.equals(String.valueOf(InternalImageParameters.INFINITEBUFFER))?"Infinity":lastValue);
						}
						break;
						
					//For Integer Parameters that accept only integer values:
					//Restores lastValue as TextField value, and doesn't update the integer parameter
					default: 
						controllerTextField.setText(lastValue); 
						break;
				}
			}
		}	
	}

	
	private final class FloatParameterTextFieldListener implements FocusListener {
		
		private String controlledParameter;
		private final JTextField controllerTextField;
		private final Core core;
		String	lastValue;

		private FloatParameterTextFieldListener(String controlledParameter, JTextField controllerTextField, Core core) {
			
			this.controlledParameter = controlledParameter;
			this.controllerTextField = controllerTextField;
			this.core = core;
			
		}

		@Override
		public void focusGained(FocusEvent evt) { //When the TextField gains focus, if there is a correct float value saves it to lastValue, else retrieve it from the parameter
			
			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			try{
				Float.parseFloat(controllerTextField.getText());
				lastValue = controllerTextField.getText();
			}catch(Exception e){
				switch(controlledParameter){
					case "kltTracker_threshold":
						lastValue = String.valueOf(parameters.getTrackerParameters().getKltTracker_threshold());
						break;
				}
			}
		}

		@Override
		public void focusLost(FocusEvent evt) { //When the TextField loses focus, if there is a correct float value saves it to the float parameter, else restores lastValue
			
			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			try{
				float currentValue = Float.parseFloat(controllerTextField.getText());						
				
				switch(controlledParameter){
					case "kltTracker_threshold":
						parameters.getTrackerParameters().setKltTracker_threshold(currentValue);
						break;
				}						
			}catch(Exception e){
				controllerTextField.setText(lastValue);
			}
		}
	}


	private final class DoubleParameterTextFieldListener implements FocusListener {
		
		private final String controlledParameter;
		private final JTextField controllerTextField;
		private final Core core;
		String	lastValue;

		private DoubleParameterTextFieldListener(String controlledParameter, JTextField controllerTextField, Core core) {
			
			this.controlledParameter = controlledParameter;
			this.controllerTextField = controllerTextField;
			this.core = core;
			
		}

		@Override
		public void focusGained(FocusEvent evt) { //When the TextField gains focus, if there is a correct double value saves it to lastValue, else retrieve it from the parameter
			
			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			try{
				Double.parseDouble(controllerTextField.getText());
				lastValue = controllerTextField.getText();
			}catch(Exception e){
				switch(controlledParameter){
					case "monoPlaneInfinity_inlierPixelTol":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneInfinity_inlierPixelTol());
						break;
					case "monoPlaneOverhead_cellSize":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_cellSize());
						break;
					case "monoPlaneOverhead_maxCellsPerPixel":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_maxCellsPerPixel());
						break;
					case "monoPlaneOverhead_mapHeightFraction":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_mapHeightFraction());
						break;
					case "monoPlaneOverhead_inlierGroundTol":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_inlierGroundTol());
						break;
					case "monoPlaneOverhead_respawnTrackFraction":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_respawnTrackFraction());
						break;
					case "monoPlaneOverhead_respawnCoverageFraction":
						lastValue = String.valueOf(parameters.getVisualOdometryParameters().getMonoPlaneOverhead_respawnCoverageFraction());
						break;
					case "chartXZ_Scale":
						lastValue = String.valueOf(parameters.getChartOutputParameters().getChartXZ_Scale());
						break;
					case "chartY_Scale":
						lastValue = String.valueOf(parameters.getChartOutputParameters().getChartY_Scale());
						break;
				}
			}
		}

		@Override
		public void focusLost(FocusEvent evt) { //When the TextField loses focus, if there is a correct double value saves it to the double parameter, else restores lastValue
			
			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			try{
				double currentValue = Double.parseDouble(controllerTextField.getText());
				
				switch(controlledParameter){
					case "monoPlaneInfinity_inlierPixelTol":
						parameters.getVisualOdometryParameters().setMonoPlaneInfinity_inlierPixelTol(currentValue);
						break;
					case "monoPlaneOverhead_cellSize":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_cellSize(currentValue);
						break;
					case "monoPlaneOverhead_maxCellsPerPixel":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_maxCellsPerPixel(currentValue);
						break;
					case "monoPlaneOverhead_mapHeightFraction":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_mapHeightFraction(currentValue);
						break;
					case "monoPlaneOverhead_inlierGroundTol":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_inlierGroundTol(currentValue);
						break;
					case "monoPlaneOverhead_respawnTrackFraction":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_respawnTrackFraction(currentValue);
						break;
					case "monoPlaneOverhead_respawnCoverageFraction":
						parameters.getVisualOdometryParameters().setMonoPlaneOverhead_respawnCoverageFraction(currentValue);
						break;
					case "chartXZ_Scale":
						if(currentValue!=0) parameters.getChartOutputParameters().setChartXZ_Scale(currentValue);
						else controllerTextField.setText(lastValue);
						break;
					case "chartY_Scale":
						if(currentValue!=0) parameters.getChartOutputParameters().setChartY_Scale(currentValue);
						else controllerTextField.setText(lastValue);
						break;
				}						
			}catch(Exception e){
				controllerTextField.setText(lastValue);
			}
		}
	}

	
	private final class StringParameterTextFieldListener implements FocusListener {
		
		private final String controlledParameter;
		private final JTextField controllerTextField;
		private final Core core;

		private StringParameterTextFieldListener(String controlledParameter, JTextField controllerTextField, Core core) {
			
			this.controlledParameter = controlledParameter;
			this.controllerTextField = controllerTextField;
			this.core = core;
			
		}

		@Override
		public void focusLost(FocusEvent evt) { //When the TextField loses focus updates the String Parameter
			
			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			switch(controlledParameter){
				case "kltTracker_pyramidScaling":
					parameters.getTrackerParameters().setKltTracker_pyramidScaling(controllerTextField.getText());
					break;
			}
						
		}

		@Override
		public void focusGained(FocusEvent evt) {
			
		}
	}

	
	private final class ParameterCheckBoxListener implements ActionListener {
		
		private final String controlledParameter;
		private final JCheckBox controllerCheckBox;
		private final Core core;

		private ParameterCheckBoxListener(String controlledParameter, JCheckBox controllerCheckBox, Core core) {
			
			this.controlledParameter = controlledParameter;
			this.controllerCheckBox = controllerCheckBox;
			this.core = core;
		
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {	/*On click updates the controlled Parameter to 
														  the current controller CheckBox status. If the  
														  checkbox has been selected then the text becomes bold, 
														  else it restores to normal*/

			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			switch(controlledParameter){
				case "deviceSustainFramerate":
					parameters.getInputParameters().setDevice_Control_SustainFramerate_Enabled(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Sustain Framerate</b></html>":"<html>Sustain Framerate</html>");
					break;
				case "deviceTimeoutImageIO":
					parameters.getInputParameters().setDevice_Control_TimeoutImageIO_Enabled(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Timeout Image I/O</b></html>":"<html>Timeout Image I/O</html>");
					break;
				case "deviceKeepFormat":
					parameters.getInputParameters().setDevice_Control_KeepFormat_Enabled(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Keep Format</b></html>":"<html>Keep Format</html>");
					break;
				case "fullResolutionPreview":
					parameters.getChartOutputParameters().setFullResolutionPreview(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Full-Resolution Preview</b></html>":"<html>Full-Resolution Preview</html>");
					break;
				case "inputPreviewEnabled":
					parameters.getInputParameters().setInputPreviewEnabled(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Enable Input Preview (Slower)</b></html>" : "<html>Enable Input Preview (Slower)</html>");
					break;
				case "imageKeepOriginal":
					parameters.getInternalImageParameters().setImageKeepOriginal(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Keep original aspect</b></html>":"<html>Keep original aspect</html>");
					
					//If the checkbox is selected disables the following components, else enables them
					((JLabel)parameters.getGuiComponents().get("lblImageResizeWidth")).setText(
							controllerCheckBox.isSelected()?"<html>Width:</html>":"<html><b>Width:</b></html>");
					((JLabel)parameters.getGuiComponents().get("lblImageResizeHeight")).setText(
							controllerCheckBox.isSelected()?"<html>Height:</html>":"<html><b>Height:</b></html>");
					parameters.getGuiComponents().get("txtImageResizeWidth").setEnabled(!controllerCheckBox.isSelected());
					parameters.getGuiComponents().get("txtImageResizeHeight").setEnabled(!controllerCheckBox.isSelected());
					break;
				case "internalImagePreview":
					parameters.getChartOutputParameters().setInternalImagePreview(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Preview Internal Image (Slower)</b></html>":"<html>Preview Internal Image (Slower)</html>");
					break;
				case "frameDecimateEnabled":
					parameters.getInternalImageParameters().setFrameDecimateEnabled(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Decimate frames by</b></html>":"<html>Decimate frames by</html>");
					break;
				case "trackerShowActiveTracks":
					parameters.getTrackerParameters().setTrackerShowActiveTracks(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Show Active Tracks</b></html>":"<html>Show Active Tracks</html>");
					break;
				case "trackerShowNewTracks":
					parameters.getTrackerParameters().setTrackerShowNewTracks(controllerCheckBox.isSelected());
					controllerCheckBox.setText(
							controllerCheckBox.isSelected()?"<html><b>Show New Tracks</b></html>":"<html>Show New Tracks</html>");
					break;
			}
		}
	}
	

	
	private final class ImageTypeChangeListener implements ActionListener {
		
		private final JComboBox<String> txtImageType;
		private final Core core;

		private ImageTypeChangeListener(JComboBox<String> txtImageType, Core core) {
			
			this.txtImageType = txtImageType;
			this.core = core;
			
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			//Sets the ImageType parameter to the element of the ImageTypesList array
			//corresponding to the currently selected Image Type ComboBox Index
			//(the ComboBox is populated in the same order as ImageTypesList array)
			if(txtImageType.getSelectedIndex()<0) return;
			parameters.getInternalImageParameters().setImageType(
					parameters.getInternalImageParameters().getImageTypesList()[txtImageType.getSelectedIndex()]);

		}
	}

	
	private final class TrackerTypeChangeListener implements ActionListener {
		
		private final JComboBox<String> txtTrackerType;
		private final Core core;

		private TrackerTypeChangeListener(JComboBox<String> txtTrackerType, Core core) {

			this.txtTrackerType = txtTrackerType;
			this.core = core;
			
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			final JPanel kltTrackerPanel = (JPanel)parameters.getGuiComponents().get("kltTrackerPanel");
			final JPanel surfTrackerPanel = (JPanel)parameters.getGuiComponents().get("surfTrackerPanel");
			
			//Obtains selectedTrackerType using selected Index in Tracker Type (Names) ComboBox
			String selectedTrackerType = "";
			Iterator<String> trackerTypes = parameters.getTrackerParameters().getTrackerTypeNames().keySet().iterator();
			for(int i=0; i<txtTrackerType.getSelectedIndex();i++){trackerTypes.next();}
			selectedTrackerType = trackerTypes.next();
			
			//Updates trackerType parameter to the new selectedTrackerType value
			parameters.getTrackerParameters().setTrackerType(selectedTrackerType);

			//Depending on which Tracker Type has been selected does some other actions:
			
			//For default (that is KLT Two-Pass with default parameters), KLT and KLT Two-Pass tracker: Shows KLT Tracker Panel (else hides it)
			kltTrackerPanel.setVisible(selectedTrackerType.equals(TrackerParameters.DEFAULT_TRACKER)||
					 				   selectedTrackerType.equals(TrackerParameters.KLT)||
					 				   selectedTrackerType.equals(TrackerParameters.KLT2));			
			kltTrackerPanel.setEnabled(!selectedTrackerType.equals(TrackerParameters.DEFAULT_TRACKER));//If default disables klt settings panel
			for(Component comp: kltTrackerPanel.getComponents()){	
				comp.setEnabled(!selectedTrackerType.equals(TrackerParameters.DEFAULT_TRACKER));//And each component in klt settings panel is enabled/disabled
			}
			
			//For SURF or SURF Two-Pass tracker: Shows SURF Tracker Panel (else hides it)
			surfTrackerPanel.setVisible(selectedTrackerType.equals(TrackerParameters.SURF)||
										selectedTrackerType.equals(TrackerParameters.SURF2));
		}
	}
	

	private final class VisualOdometryTypeChangeListener implements ActionListener {
		
		private final JComboBox<String> txtVisualOdometryType;
		private final Core core;

		private VisualOdometryTypeChangeListener(JComboBox<String> txtVisualOdometryType, Core core) {
			
			this.txtVisualOdometryType = txtVisualOdometryType;
			this.core = core;
			
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			JPanel monoPlaneInfinityPanel = (JPanel)parameters.getGuiComponents().get("monoPlaneInfinityPanel");
			JPanel monoPlaneOverheadPanel = (JPanel)parameters.getGuiComponents().get("monoPlaneOverheadPanel");
			JScrollPane monoPlaneScrollPane = (JScrollPane)parameters.getGuiComponents().get("monoPlaneScrollPane");
			
			//Obtains selectedVisualOdometryType using selected Index in Visual Odometry Type (Names) ComboBox
			String selectedVisualOdometryType = "";
			Iterator<String> visualOdometryTypes = parameters.getVisualOdometryParameters().getVisualOdometryTypeNames().keySet().iterator();
			for(int i=0; i<txtVisualOdometryType.getSelectedIndex();i++){visualOdometryTypes.next();}
			selectedVisualOdometryType = visualOdometryTypes.next();
			
			//Updates visualOdometryType parameter to the new selectedVisualOdometryType value
			parameters.getVisualOdometryParameters().setVisualOdometryType(selectedVisualOdometryType);

			//Depending on which Visual Odometry Type has been selected does some other actions:
			switch(selectedVisualOdometryType){
				case VisualOdometryParameters.DEFAULT_VISUALODOMETRY:
				case VisualOdometryParameters.MONOPLANEINFINITY:
					monoPlaneScrollPane.setViewportView(monoPlaneInfinityPanel);
					monoPlaneInfinityPanel.setEnabled(!selectedVisualOdometryType.equals(VisualOdometryParameters.DEFAULT_VISUALODOMETRY)); //If default(monoPlaneInfinity with default parameters) disables monoPlaneInfinity settings panel
					for(Component comp: monoPlaneInfinityPanel.getComponents()){	
						comp.setEnabled(!selectedVisualOdometryType.equals(VisualOdometryParameters.DEFAULT_VISUALODOMETRY));//And each component in monoPlaneInfinity settings panel is enabled/disabled
					}
					break;
				case VisualOdometryParameters.MONOPLANEOVERHEAD:
					monoPlaneScrollPane.setViewportView(monoPlaneOverheadPanel);
					break;
				default:
					JPanel newJPanel = new JPanel();
					newJPanel.setPreferredSize(new Dimension(newJPanel.getPreferredSize().width,70));
					monoPlaneScrollPane.setViewportView(newJPanel);
					break;
			}
		}
	}
	

	private final class ChartTypeChangeListener implements ActionListener {
		
		private final JComboBox<String> txtChartType;
		private final Core core;

		private ChartTypeChangeListener(JComboBox<String> txtChartType, Core core) {
			
			this.txtChartType = txtChartType;
			this.core = core;
			
		}

		@Override
		public void actionPerformed(ActionEvent evt) {

			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			//Obtains selectedChartType using selected Index in Chart Type (Names) ComboBox
			String selectedChartType = "";
			Iterator<String> chartTypes = parameters.getChartOutputParameters().getChartTypeNames().keySet().iterator();
			for(int i=0; i<txtChartType.getSelectedIndex();i++){chartTypes.next();}
			selectedChartType = chartTypes.next();
			
			//Updates chartType parameter to the new selectedChartType value
			parameters.getChartOutputParameters().setChartType(selectedChartType);
			
		}
	}


	private final class ChartButtonListener implements ActionListener {
		
		private final String function;
		private final Core core;
		
		public ChartButtonListener(String function, Core core){
			
			this.function = function;
			this.core = core;
		}
		
		@Override
		public void actionPerformed(ActionEvent evt) {

			//Loads current Parameters
			Parameters parameters = core.getParameters();
			
			ChartScrollPane chartXZPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartXZPanel");
			ChartScrollPane chartYPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartYPanel");

			switch(function){
				case "chartXZ_applyScale": //Change XZ Chart scale
					chartXZPanel.setChartScalingFactor(parameters.getChartOutputParameters().getChartXZ_Scale());
					chartXZPanel.resetSize();
					break;
				case "chartXZ_moveToOrigin"://Chart X/Z Move to Origin
					chartXZPanel.moveToOrigin();
					break;
				case "chartXZ_moveToLastPoint"://Chart X/Z Move To Last Point
					chartXZPanel.moveToLast();
					break;
				case "chartXZ_3DPoints": //Chart X/Z 3D Points
					
					JCheckBox chkChartXZ_3DPoints = (JCheckBox)parameters.getGuiComponents().get("chkChartXZ_3DPoints");
					
					chkChartXZ_3DPoints.setText(
							chkChartXZ_3DPoints.isSelected()?"<html><b>3D Chart Points</b></html>":"<html>3D Chart Points</html>");
					chartXZPanel.setThickPoints(chkChartXZ_3DPoints.isSelected());
					chartXZPanel.repaint();
					break;
				case "chartY_applyScale": //Change Y Chart Scale
					chartYPanel.setChartScalingFactor(parameters.getChartOutputParameters().getChartY_Scale());
					chartYPanel.resetSize();
					break;
				case "chartY_moveToOrigin"://Chart Y Move to Origin
					chartYPanel.moveToOrigin();
					break;
				case "chartY_moveToLastPoint"://Chart Y Move to Last Point
					chartYPanel.moveToLast();
					break;
			}
		}
	}

	
	private final class InfoPointsListListener implements ListSelectionListener {
		
		private final Core core;
		
		private InfoPointsListListener(Core core) {
		
			this.core = core;
		
		}

		@Override
		public void valueChanged(ListSelectionEvent evt) {
		
			//Loads Parameters
			Parameters parameters = core.getParameters();
			
			//Loads InfoPanel XZ Chart and Y Chart from guiComponents HashMap
			InfoScrollPane chartInfoPanel = (InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel");
			ChartScrollPane chartXZPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartXZPanel");
			ChartScrollPane chartYPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartYPanel");
			
			//If the selection event is generated from the InfoPanel List and
			//no changes are currently made (no adds or modification to the list,
			//that means any visual odometry process is terminated):
			if(evt.getSource().equals(chartInfoPanel.lst_points) 
					&& evt.getValueIsAdjusting()==false 
					&& !parameters.getProcessingFlags().isProcessingVisualOdometry()){
				
				//Gets selected element from Points list
				String selected = (String)chartInfoPanel.lst_points.getSelectedValue(); 
				if(selected==null) return; //If it's a null element exit from the Listener
				
				
				//Try to move XZ Chart to selected XZ Point:
				if(selected.indexOf("X:")==-1 || selected.indexOf("Z:")==-1) return; //If it's a not null element
												  	   //but doesn't contain "X:" or "Z:", exit from the listener 
												       //(eg. Chart 1, chart number element of the list)
				
				//Else if it's a point coordinate element (contains "X:", "Z:")
				//Extracts X coordinate
				String XtoEnd = selected.substring(selected.indexOf("X:")+2).trim();
				String X = XtoEnd.substring(0,XtoEnd.indexOf(","));
				//Extracts Z coordinate
				String ZtoEnd = selected.substring(selected.indexOf("Z:")+2).trim();
				String Z = ZtoEnd.substring(0,ZtoEnd.indexOf(","));
				
				//Move the XZ Chart to the (X, Z) point position
				chartXZPanel.moveToPoint(Double.parseDouble(X), Double.parseDouble(Z));
				
				
				//Try to move Y Chart to Y/f or Y/s Point:
				//Extract Y Chart Type from the selected list element
				String chartType = selected.substring(selected.lastIndexOf(",")+1).trim();
				
				//If Y Chart Type is Y/frames and selected element contains "Y:" (y coordinate):
				if(chartType.equalsIgnoreCase("(Chart Type Y/f)")
						&& selected.indexOf("Y:")!=-1){
					
					//Extract Y coordinate
					String YtoEnd = selected.substring(selected.indexOf("Y:")+2).trim();
					String Y = YtoEnd.substring(0,YtoEnd.indexOf(","));
					//Extract Frame (X) coordinate
					String FrameToEnd = selected.substring(selected.indexOf("Frame:")+6).trim();
					String Frame = FrameToEnd.substring(0,FrameToEnd.indexOf(","));
					
					//Move the Y Chart to the (Frame, Y) point position
					chartYPanel.moveToPoint(Double.parseDouble(Frame), Double.parseDouble(Y));
				
				}else if(chartType.equalsIgnoreCase("(Chart Type Y/s)") //If Y Chart Type is Y/seconds and selected 
						&& selected.indexOf("Y:")!=-1){					//element contains "Y:" (y coordinate)
					
					//Extract Y coordinate
					String YtoEnd = selected.substring(selected.indexOf("Y:")+2).trim();
					String Y = YtoEnd.substring(0,YtoEnd.indexOf(","));
					//Extract Second (X) coordinate
					String SecondToEnd = selected.substring(selected.indexOf("El. Time:")+9).trim();
					String Second = SecondToEnd.substring(0,SecondToEnd.indexOf("s"));
					
					//Move the Y Chart to the (Second, Y) point position
					chartYPanel.moveToPoint(Double.parseDouble(Second), Double.parseDouble(Y));
				}
			}				
		}
	}
	
	
	
	private final class MaximizeOnDblClick implements MouseListener{

		
		private final JComponent componentToMaximize;
		private final JFrame container;
		
		private boolean isMaximized;
		private final SpringLayout maximizeComponentLayout;
		private LayoutManager oldLayout;

		public MaximizeOnDblClick(JComponent componentToMaximize, JFrame container){
			
			this.componentToMaximize = componentToMaximize; //Component to Maximize
			this.container = container; //Container into which maximize Component
			
			this.isMaximized  = false; //Maximized status initialized to false
			
			this.maximizeComponentLayout = new SpringLayout(); //Creates a layout to maximize component into container
			this.maximizeComponentLayout.putConstraint(SpringLayout.NORTH, componentToMaximize, 5, SpringLayout.NORTH, container.getContentPane());
			this.maximizeComponentLayout.putConstraint(SpringLayout.WEST, componentToMaximize, 5, SpringLayout.WEST, container.getContentPane());
			this.maximizeComponentLayout.putConstraint(SpringLayout.EAST, componentToMaximize, -5, SpringLayout.EAST, container.getContentPane());
			this.maximizeComponentLayout.putConstraint(SpringLayout.SOUTH, componentToMaximize, -5, SpringLayout.SOUTH, container.getContentPane());
			
			this.oldLayout = null; //Old Layout is initialized to null
			
		}
		
		
		@Override
		public void mouseClicked(MouseEvent evt) {
			
			
			if(evt.getClickCount()==2){ //On double click
			
				if(!isMaximized){ //If Component is not maximized
					
					isMaximized=true; //Sets isMaximized to true
					
					oldLayout = container.getContentPane().getLayout(); //Saves old container layout
					
					//For all the components in the container:
					for(int i=0;i<container.getContentPane().getComponentCount();i++){
						
						//Extract a containerComponent
						JComponent containerComponent = (JComponent) container.getContentPane().getComponent(i);
						
						//If current containerComponent is not the componentToMaximize:
						if(!containerComponent.equals(componentToMaximize)){ 
							containerComponent.setVisible(false); //Sets this component not visible
						}
					}
					
					//Apply maximizeComponentLayout to the Container
					container.getContentPane().setLayout(maximizeComponentLayout);		
					
				}else{ //If Component is already maximized
					
					isMaximized=false; //Sets isMaximized to false
					
					//For all the components in the container:
					for(int i=0;i<container.getContentPane().getComponentCount();i++){
						
						//Extract a containerComponent
						JComponent containerComponent = (JComponent)container.getContentPane().getComponent(i);
						
						//If current containerComponent is not the componentToMaximize:
						if(!containerComponent.equals(componentToMaximize)){
							containerComponent.setVisible(true); //Sets this component visible
						}
					}
					
					//Apply oldLayout to the Container
					container.getContentPane().setLayout(oldLayout);
					
					oldLayout = null;
				}
				
				//Revalidate (and repaint) Container
				container.revalidate();
				
				//Repaint componentToMaximize
				componentToMaximize.repaint();
				
				try{	//If we are maximizing a ChartScrollPane this provides ViewPort repainting (Axis names (eg.X,Y) repaint)
				ChartScrollPane chartPanel = (ChartScrollPane) componentToMaximize;
				chartPanel.getViewport().paintComponents(chartPanel.getViewport().getGraphics());
				}catch(Exception e){}
			}
		}
		@Override
		public void mouseEntered(MouseEvent evt) {		
		}
		@Override
		public void mouseExited(MouseEvent evt) {
		}
		@Override
		public void mousePressed(MouseEvent evt) {
		}
		@Override
		public void mouseReleased(MouseEvent evt) {
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
			return (int)Math.round(component.getHeight()*proportion);
		}

		@Override
		public int getPreferredValue() {
			return (int)Math.round(component.getHeight()*proportion);
		}

		@Override
		public int getMinimumValue() {
			return (int)Math.round(component.getHeight()*proportion);
		}

		@Override
		public int getMaximumValue() {
			return (int)Math.round(component.getHeight()*proportion);
		}
	}
	
}