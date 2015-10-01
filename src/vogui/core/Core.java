package vogui.core;

import georegression.struct.point.Vector3D_F64;
import georegression.struct.se.Se3_F64;
import vogui.algorithm_generators.TrackerGenerator;
import vogui.algorithm_generators.VisualOdometryGenerator;
import vogui.integration.V4l4jVideo;
import vogui.parameters.ChartOutputParameters;
import vogui.parameters.InputParameters;
import vogui.parameters.InternalImageParameters;
import vogui.parameters.InternalImageParameters.ImageTypeDescriptor;
import vogui.parameters.Parameters;
import vogui.parameters.TrackerParameters;
import vogui.parameters.VisualOdometryParameters;
import vogui.userinterface.ChartScrollPane;
import vogui.userinterface.InfoScrollPane;
import vogui.userinterface.UIGenerator;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.swing.*;

import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.AccessPointTracks3D;
import boofcv.abst.sfm.d3.DepthVisualOdometry;
import boofcv.abst.sfm.d3.MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.StereoVisualOdometry;
import boofcv.abst.sfm.d3.VisualOdometry;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import boofcv.io.MediaManager;
import boofcv.io.UtilIO;
import boofcv.io.VideoCallBack;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.io.wrapper.images.LoadFileImageSequence;
import boofcv.struct.calib.MonoPlaneParameters;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.calib.VisualDepthParameters;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageType;


@SuppressWarnings("rawtypes")
public class Core <I extends ImageSingleBand, Depth extends ImageSingleBand> {

	
	//Parameter class on which this class depends (Contains a model of all parameters) 
	private Parameters parameters;

	public Core(Parameters parameters){
		this.setParameters(parameters);
	}

	
	/**
	 * Start
	 * - Parameters check
	 * - Visual Odometry Process setup and start
	 */
	public void start(){

		Parameters storedParameters = new Parameters(parameters); //Makes a Static-copy of Parameters
																  //before Checking and Elaboration

		/** NOTE: Parameters Explanation
		 * - Stored Parameters are used for reading definitive values (not subject to GUI modification during processing)
		 *   for elaboration and for checking (Calibration Path, Video Path, Tracker Type, Visual Odometry Type etc.).

		 * - (Original) global Parameters are used for reading real-time modifiable values, writing values and 
		 *   exchange messages between threads during processing. They are also used for reading guiComponents,
		 *   the HashMap containing all the main GUI components. 
		 *   (Input Preview Enabled, Preview Internal Image, Decimate Frames, Image Buffer max Size, 
		 *   Show Tracker Active and New Tracks, Processing Parameters (processed image), Processing Flags 
		 *   (Processing,Pause,Reset,Stop,Clear), Device Parameters (Image Buffer,Stop Capture Flag,
		 *   isStopped Capture Flag)) 
		 */
		
		//Extracts Status Label of chartInfoPanel from guiComponents
		JLabel lblStatus = ((InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel")).lbl_status;

		//Extracts mainFrame from GuiComponents (needed as JOptionPane Parent window):
		JFrame mainFrame = (JFrame)parameters.getGuiComponents().get("mainFrame");


		lblStatus.setText("<html><b>Status: </b>Initializing Elaboration...</html>");
		
		//Checks the (stored) Parameters
		boolean isCheckPassed = checkParameters(storedParameters);
		if(!isCheckPassed){
			lblStatus.setText("<html><b>Status: </b>Settings Error. Could not start Elaboration.</html>");
			return;
		}
		lblStatus.setText("<html><b>Status: </b>Settings Check passed.</html>");
		
		//Try to open the Calibration file (pointed in stored Parameters):
		if(!openCalib(storedParameters)){
			JOptionPane.showConfirmDialog(mainFrame, "Calibration file isn't valid or the specified file doesn't exist!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			lblStatus.setText("<html><b>Status: </b>Error opening Calibration file. Could not start Elaboration.</html>");
			return;
		}
		lblStatus.setText("<html><b>Status: </b>Calibration opened succesfully.</html>");
		
		//Depending on the selected Input Source:
		switch(storedParameters.getInputParameters().getInputSource()){
			case InputParameters.VIDEO_INPUT:
				if(!openVideo(storedParameters)){	//Try to open the Video file (pointed in stored Parameters):
					JOptionPane.showConfirmDialog(mainFrame, "Video file isn't valid or the specified file doesn't exist!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					lblStatus.setText("<html><b>Status: </b>Error opening Video file. Could not start Elaboration.</html>");
					return;
				}
				lblStatus.setText("<html><b>Status: </b>Video opened succesfully.</html>");
				break;
			case InputParameters.DEVICE_INPUT:
				if(!openDevice(storedParameters)){	//Try to open the Device (pointed in stored Parameters):
					if(!OSValidator.isUnix()&&storedParameters.getInputParameters().getDevicePath().indexOf("V4L4J")>=0){
						JOptionPane.showConfirmDialog(
								mainFrame, 
								"V4L4J Device Driver runs only under Linux!\nYour current os is: "
								+(OSValidator.isWindows()?
												"Windows"
												:(OSValidator.isMac()?
														"Mac"
														:"Unknown"))
								,"Error"
								,JOptionPane.PLAIN_MESSAGE
								,JOptionPane.ERROR_MESSAGE);
					}else{
						JOptionPane.showConfirmDialog(mainFrame, "Device path/type isn't valid, or doesn't exist\nor doesn't support selected adjustments!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					}
					lblStatus.setText("<html><b>Status: </b>Error opening Device. Could not start Elaboration.</html>");
					return;
				}
				lblStatus.setText("<html><b>Status: </b>Device opened succesfully.</html>");
				break;
			default:
				return;
		}

		//Try to setup a new Tracker:
		if(!setupTracker(storedParameters)){
			JOptionPane.showConfirmDialog(mainFrame, "Error setting up the Tracker!\nCheck out Tracker settings","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			lblStatus.setText("<html><b>Status: </b>Error setting up the Tracker. Could not start Elaboration.</html>");
			return;
		}
		lblStatus.setText("<html><b>Status: </b>Tracker Setup passed.</html>");
		
		//Try to setup a new Visual Odometry:
		if(!setupVisualOdometry(storedParameters)){
			JOptionPane.showConfirmDialog(mainFrame, "Error setting up the Visual Odometry!\nCheck out Visual Odometry settings, or if the selected\n Visual Odometry type is not implemented.","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			lblStatus.setText("<html><b>Status: </b>Error setting up the Visual Odometry. Could not start Elaboration.</html>");
			return;
		}
		lblStatus.setText("<html><b>Status: </b>Visual Odometry Setup passed.</html>");
		
		//Try to start the Visual Odometry Process
		if(!process(storedParameters)){
			JOptionPane.showConfirmDialog(mainFrame, "An error has occurred during the Visual Odometry elaboration!\nCheck out your Visual Odometry/Tracker Settings.\nOtherwise your input video may be invalid or not estimable, or has an inadequate calibration.", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return;
		}	
		
		//If the Visual Odometry Process has completed successfully
		//Creates a wait Thread to refresh Points List
		Thread refreshPointsList = new Thread(new Runnable(){

			@Override
			public void run() {
				InfoScrollPane chartInfoPanel = (InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel");
				try {Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
				chartInfoPanel.refreshList();
				chartInfoPanel.scrollToEndList();
			}
			
		});
		refreshPointsList.start();
	
	}
	
	
	private boolean checkParameters(Parameters storedParameters){
		

		//Extracts all the Parameters to be checked:
		String calibrationPath = storedParameters.getInputParameters().getCalibrationPath(); 
		String inputSource = storedParameters.getInputParameters().getInputSource();
		String videoPath = storedParameters.getInputParameters().getVideoPath();
		String devicePath = storedParameters.getInputParameters().getDevicePath();
		int deviceWidth = storedParameters.getInputParameters().getDeviceWidth();
		int deviceHeight = storedParameters.getInputParameters().getDeviceHeight();
		ImageTypeDescriptor imageType = storedParameters.getInternalImageParameters().getImageType();
		int imageResizeWidth = storedParameters.getInternalImageParameters().getImageResizeWidth();
		int imageResizeHeight = storedParameters.getInternalImageParameters().getImageResizeHeight();
		int imageBufferSize = storedParameters.getInternalImageParameters().getImageBufferSize();
		String trackerType = storedParameters.getTrackerParameters().getTrackerType();
		String kltTracker_pyramidScaling = storedParameters.getTrackerParameters().getKltTracker_pyramidScaling();
		String visualOdometryType = storedParameters.getVisualOdometryParameters().getVisualOdometryType();
		String chartType = storedParameters.getChartOutputParameters().getChartType();
		double chartXZ_Scale = storedParameters.getChartOutputParameters().getChartXZ_Scale();
		double chartY_Scale = storedParameters.getChartOutputParameters().getChartY_Scale();
		
		
		//Extracts mainFrame from GuiComponents (needed as JOptionPane Parent window):
		JFrame mainFrame = (JFrame)parameters.getGuiComponents().get("mainFrame");
	
		
		//Calibration Path Check
		if(calibrationPath == null || calibrationPath.isEmpty()){
			JOptionPane.showConfirmDialog(mainFrame, "Calibration path is empty!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//Input Source Check
		if(inputSource == null || inputSource.isEmpty()){
			JOptionPane.showConfirmDialog(mainFrame, "Select an Input Source!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		switch(inputSource){
			case InputParameters.VIDEO_INPUT:	//If Input Source is Video Input:

				//Video Path Check
				if (videoPath==null || videoPath.isEmpty()){
					JOptionPane.showConfirmDialog(mainFrame, "Video path is empty!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return false;}
				break;
			case InputParameters.DEVICE_INPUT:	//If Input Source is Device Input:

				//Device Path Check
				if(devicePath==null || devicePath.isEmpty()){
					JOptionPane.showConfirmDialog(mainFrame, "Device path is empty!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return false;}

				//Device Width Check
				if(deviceWidth<=0){
					JOptionPane.showConfirmDialog(mainFrame, "Device acquisition width is less than or equal to zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				//Device Height Check
				if(deviceHeight<=0){
					JOptionPane.showConfirmDialog(mainFrame, "Device acquisition height is less than or equal to zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
					return false;
				}
				break;
			default:	//If Input Source is unknown:
				JOptionPane.showConfirmDialog(mainFrame, "Wrong Input Source selected!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
				return false;
		}
		
		//Image Type Check
		if(imageType == null || imageType.getImageTypeClass() == null){
			JOptionPane.showConfirmDialog(mainFrame, "Select a correct Image Type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		//Processed Image Resize Width Check
		if(imageResizeWidth<=0){
			JOptionPane.showConfirmDialog(mainFrame, "Processed image resize width is less than or equal to zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		//Processed Image Resize Height Check
		if(imageResizeHeight<=0){
			JOptionPane.showConfirmDialog(mainFrame, "Processed image resize height is less than or equal to zero!\nUse only positive values","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//Acquired Images Buffer Size Check (Device Input Source only)
		if(inputSource.equals(InputParameters.DEVICE_INPUT) && imageBufferSize<0){
			JOptionPane.showConfirmDialog(mainFrame, "Image Buffer Size is less than zero!\nUse only values >= 0 ("+InternalImageParameters.INFINITEBUFFER+" stands for infinite)","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}		
		
		//Tracker Type Check
		if(trackerType == null || trackerType.isEmpty()){
			JOptionPane.showConfirmDialog(mainFrame, "Select a correct Tracker Type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}		

		//KLT Tracker pyramidScaling Check (KLT Trackers only)
		if((trackerType.equals(TrackerParameters.KLT)||trackerType.equals(TrackerParameters.KLT2)||trackerType.equals(TrackerParameters.DEFAULT_TRACKER))
				&& (kltTracker_pyramidScaling == null || kltTracker_pyramidScaling.isEmpty())){
			
			int choice = JOptionPane.showConfirmDialog(mainFrame, "KLT Tracker Pyramid Scaling is empty!\nUse default value (1,2,4,8)?","Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			switch(choice){
				case JOptionPane.OK_OPTION:
					((JTextField) parameters.getGuiComponents().get("txtKltTracker_pyramidScaling")).setText("1,2,4,8");
					parameters.getTrackerParameters().setKltTracker_pyramidScaling("1,2,4,8"); //Changes original Parameters (to make permanent the modify)
					storedParameters.getTrackerParameters().setKltTracker_pyramidScaling("1,2,4,8"); //Changes stored Parameter (to continue current elaboration)
					break;
				case JOptionPane.CANCEL_OPTION:
					return false;
			}
		}
		
		//Visual Odometry Check
		if(visualOdometryType == null || visualOdometryType.isEmpty()){
			JOptionPane.showConfirmDialog(mainFrame, "Select a correct Visual Odometry type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		//Chart Type Check
		if(chartType == null || chartType.isEmpty() || 
				(!chartType.equals(ChartOutputParameters.YFRAMES_CHART) && 
				 !chartType.equals(ChartOutputParameters.YSECONDS_CHART))){
			JOptionPane.showConfirmDialog(mainFrame, "Select a correct Chart Type!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}

		//XZ Chart Scale Check
		if(chartXZ_Scale == 0){
			JOptionPane.showConfirmDialog(mainFrame, "Insert an XZ Chart scaling factor different from zero!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//Y Chart Scale Check
		if(chartY_Scale == 0){
			JOptionPane.showConfirmDialog(mainFrame, "Insert an Y Chart scaling factor different from zero!","Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//If all checks passed, returns true
		return true;
	}

	
	/**
	 * openCalib
	 * - Tries to open a mono, stereo or depth calibration file depending
	 * 	 on the Visual Odometry that has been selected 
	 */
	private boolean openCalib(Parameters storedParameters){
		
		//Extracts parameters to read (from stored Parameters)
		String calibrationPath = storedParameters.getInputParameters().getCalibrationPath();
		String visualOdometryType = storedParameters.getVisualOdometryParameters().getVisualOdometryType();
		
		//Extracts MediaManager from (original) Processing Parameters
		MediaManager mediaManager = parameters.getProcessingParameters().getMediaManager();
		
		//Tries to load an XML Camera Description file (Calibration file)
		try{
	
			switch(visualOdometryType){
				case VisualOdometryParameters.DEFAULT_VISUALODOMETRY:
				case VisualOdometryParameters.MONOPLANEINFINITY:
				case VisualOdometryParameters.MONOPLANEOVERHEAD:
					//Sets the monoCalibration, and deletes the others
					parameters.getProcessingParameters().setMonoCalibration(
							UtilIO.loadXML(mediaManager.openFile(calibrationPath)));
					parameters.getProcessingParameters().setStereoCalibration(null);
					parameters.getProcessingParameters().setDepthCalibration(null);
					break;
				case VisualOdometryParameters.STEREODEPTH:
				case VisualOdometryParameters.STEREODUALTRACKERPNP:
				case VisualOdometryParameters.STEREOQUADPNP:
					//Sets the stereoCalibration, and deletes the other
					parameters.getProcessingParameters().setMonoCalibration(null);
					parameters.getProcessingParameters().setStereoCalibration(
							UtilIO.loadXML(mediaManager.openFile(calibrationPath)));
					parameters.getProcessingParameters().setDepthCalibration(null);
					break;
				case VisualOdometryParameters.DEPTHDEPTHPNP:
					//Sets the depthCalibration, and deletes the other 
					parameters.getProcessingParameters().setMonoCalibration(null);
					parameters.getProcessingParameters().setStereoCalibration(null);
					parameters.getProcessingParameters().setDepthCalibration(
							UtilIO.loadXML(mediaManager.openFile(calibrationPath)));
					break;
				default:
					//On unknown visualOdometryType, deletes all the calibrations 
					parameters.getProcessingParameters().setMonoCalibration(null);
					parameters.getProcessingParameters().setStereoCalibration(null);
					parameters.getProcessingParameters().setDepthCalibration(null);
					break;
			}
			
			//If Calibration opens successfully, returns true
			return true;
			
		}catch(Exception e){

			//On error returns false
			return false;
		}

	}

	
	/**
	 * openVideo (Used only for Video Input)
	 * - Tries to open a video file
	 */
	@SuppressWarnings("unchecked")
	private boolean openVideo(Parameters storedParameters){
		
		//Extracts parameters to read (from storedParameters)
		String videoPath = storedParameters.getInputParameters().getVideoPath();
		Class<I> imageType = storedParameters.getInternalImageParameters().getImageType().getImageTypeClass();

		//Extracts MediaManager from (original) Processing Parameters
		MediaManager mediaManager = parameters.getProcessingParameters().getMediaManager();
		
		//Tries to load the video input sequence
		try{
			File videoFile = new File(videoPath);
			
			//Loads the input video sequence
			if(videoFile.exists() && videoFile.isFile()){
				parameters.getProcessingParameters().setVideo(
						mediaManager.openVideo(videoPath, ImageType.single(imageType)));
			}else if(videoFile.exists() && videoFile.isDirectory()){
				LoadFileImageSequence<I> imagesDir = new LoadFileImageSequence<I>(ImageType.single(imageType), videoPath, "");
				parameters.getProcessingParameters().setVideo((SimpleImageSequence<I>)imagesDir);
			}else{
				return false;
			}
			
			//Acquires and sets the first input video image (as first image to process)
			I leftImg = (I)parameters.getProcessingParameters().getVideo().next(); 
			parameters.getProcessingParameters().setLeftImg(leftImg);
			
			//If video opening is successfully returns true
			return true;
			
		}catch(Exception e){
			e.printStackTrace();
			//Else returns false
			return false;
		}
	}

	
	/**
	 * openDevice (Used only for Device Input)
	 * - Tries to setup and open a Device input
	 * - Create and starts the Device callback, that will fill the buffer with acquired images,
	 * 	 will show the captured images in Input Video Frame, and will update the Device 
	 * 	 FPS Info
	 * - Creates and starts a BufferMonitor Thread, that monitors and print buffer informations 
	 *   on Info Panel
	 */
	@SuppressWarnings("unchecked")
	private boolean openDevice(Parameters storedParameters){
		
		//Extracts parameters to read (from storedParameters):
		boolean isDevice_Control_SustainFramerate_Enabled = storedParameters.getInputParameters().isDevice_Control_SustainFramerate_Enabled();
		boolean isDevice_Control_TimeoutImageIO_Enabled = storedParameters.getInputParameters().isDevice_Control_TimeoutImageIO_Enabled();
		boolean isDevice_Control_KeepFormat_Enabled = storedParameters.getInputParameters().isDevice_Control_KeepFormat_Enabled();
		String devicePath = storedParameters.getInputParameters().getDevicePath();
		int deviceWidth = storedParameters.getInputParameters().getDeviceWidth();
		int deviceHeight = storedParameters.getInputParameters().getDeviceHeight();
		Class<I> imageType = storedParameters.getInternalImageParameters().getImageType().getImageTypeClass();
		
		if(!devicePath.equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
			
			if(!OSValidator.isUnix()){return false;}
			
			if(devicePath.endsWith("(V4L4J)")){
				devicePath = devicePath.substring(0, devicePath.indexOf("(V4L4J)")).trim();
			}
			
			//Creates a new Video4Linux4Java Device object (opens a Camera)
			V4l4jVideo<I> device = new V4l4jVideo<I>();
		
			//Tries to setup and start Device Input
			try{
			
				//Setups the Device Input
				device.setConvertBufferedImage(false);	//Doesn't convert BufferedImages to BoofCV imgType internally
				device.activateControls(isDevice_Control_SustainFramerate_Enabled, //Activates selected controls 
										isDevice_Control_TimeoutImageIO_Enabled, 
										isDevice_Control_KeepFormat_Enabled);
			
				//Starts the Device Input (Camera)
				device.start(devicePath.trim(), deviceWidth, deviceHeight, ImageType.single(imageType), new CustomDeviceCallback());
		
		
				//Creates and starts a BufferMonitor Thread to monitor the Device Buffer
				//and print informations
				Thread bufferMonitor = new Thread(new BufferMonitorRunnable());		
				bufferMonitor.start();
			
				//If Device opening is successfully, returns true
				return true;		
			}catch(Exception e){ //If Device opening fails
		
				//Sets isStoppedCapture Flag to true
				parameters.getDeviceParameters().setStoppedCaptureFlag(true);
		
				//Returns false
				return false;
			}
		}else{
			try{
				//Sets WebcamCapture object into (original) Device Parameters
				parameters.getDeviceParameters().setWebcam(
						UtilWebcamCapture.openDefault(deviceWidth,deviceHeight));

				//Detect the Webcam size
				Dimension actualSize = parameters.getDeviceParameters().getWebcam().getViewSize();

				//Creates the first input device image (as first image to process)
				parameters.getProcessingParameters().setLeftImg(ImageType.single(imageType).createImage(
																			actualSize.width, actualSize.height)); 

				//Sets Stop Capture flag to false
				parameters.getDeviceParameters().setStopCaptureFlag(false);
				
				//Sets isStopped Capture flag to false (Capture started)
				parameters.getDeviceParameters().setStoppedCaptureFlag(false);

				//If the WebcamCapture object has successfully started returns true
				return true;
			}catch(Exception e){
				e.printStackTrace();
				//Close the Webcam
				if(parameters.getDeviceParameters().getWebcam()!=null){
					parameters.getDeviceParameters().getWebcam().close();
				}

				//Sets isStopped Capture flag to true
				parameters.getDeviceParameters().setStoppedCaptureFlag(true);
				
				//Returns false
				return false;
			}
		}
	}
	
	
	/**
	 * setupTracker
	 * - Tries to create and setup a Tracker
	 */
	@SuppressWarnings("unchecked")
	private boolean setupTracker(Parameters storedParameters){
		
		//Extracts parameters to read (from storedParameters):
		Class<I> imageType = storedParameters.getInternalImageParameters().getImageType().getImageTypeClass();
		String trackerType = storedParameters.getTrackerParameters().getTrackerType();
		int kltTracker_templateRadius = storedParameters.getTrackerParameters().getKltTracker_templateRadius();
		String kltTracker_pyramidScaling = storedParameters.getTrackerParameters().getKltTracker_pyramidScaling();
		int kltTracker_maxFeatures = storedParameters.getTrackerParameters().getKltTracker_maxFeatures();
		int kltTracker_radius = storedParameters.getTrackerParameters().getKltTracker_radius();
		float kltTracker_threshold = storedParameters.getTrackerParameters().getKltTracker_threshold();
		int surfTracker_maxFeaturesPerScale = storedParameters.getTrackerParameters().getSurfTracker_maxFeaturesPerScale();
		int surfTracker_extractRadius = storedParameters.getTrackerParameters().getSurfTracker_extractRadius();
		int surfTracker_initialSampleSize = storedParameters.getTrackerParameters().getSurfTracker_initialSampleSize();
		
		//Extracts mainFrame from GuiComponents (needed as JOptionPane Parent window):
		JFrame mainFrame = (JFrame)parameters.getGuiComponents().get("mainFrame");
		
		//Creates a new TrackerGenerator, usable to generate a custom Tracker based on the selected parameters
		TrackerGenerator trackerGenerator = new TrackerGenerator(imageType);
		
		switch(trackerType){
			case TrackerParameters.KLT:	//If the KLT or KLT-2Pass Tracker has been selected:
			case TrackerParameters.KLT2:
				
				//Extracts an Integer Array from the KLT Tracker pyramidScaling Parameter(String)
				int[] extracted_pyramidScaling = extract_IntArray(kltTracker_pyramidScaling);
				
				//If the extracted array is null or is 0
				if(extracted_pyramidScaling==null||extracted_pyramidScaling[0]==0){
					
					//Shows a message to tell that pyramidScaling is not valid, and asks
					//to change for default value:
					int choice = JOptionPane.showConfirmDialog(mainFrame, "KLT Tracker Pyramid Scaling is not valid!\nUse default value (1,2,4,8)?","Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
					switch(choice){
						case JOptionPane.OK_OPTION://If accepted
							//Reset KLT Tracker pyramidScaling TextField content to default value
							((JTextField) storedParameters.getGuiComponents().get("txtKltTracker_pyramidScaling")).setText("1,2,4,8");
							//Changes original Parameters (to make permanent the modify)
							parameters.getTrackerParameters().setKltTracker_pyramidScaling("1,2,4,8"); 
							//Changes stored Parameter (to continue current elaboration)
							storedParameters.getTrackerParameters().setKltTracker_pyramidScaling("1,2,4,8");
							
							//Sets the local pyramidScaling value to default value 
							kltTracker_pyramidScaling = storedParameters.getTrackerParameters().getKltTracker_pyramidScaling();
							//Tries again to extract an Integer Array from the KLT Tracker default pyramidScaling (Surely valid)
							extracted_pyramidScaling = extract_IntArray(kltTracker_pyramidScaling);
							break;
						case JOptionPane.CANCEL_OPTION://If canceled
							return false; //Exits and returns false (error in Tracker settings)
					}
				}
				
				//If the extracted array is valid
				try{ //Depending if KLT or KLT2 generates the correct Tracker:
					if(trackerType.equals(TrackerParameters.KLT)){
						parameters.getProcessingParameters().setTracker(
								trackerGenerator.createKLT(kltTracker_templateRadius, extracted_pyramidScaling, 
														   kltTracker_maxFeatures, kltTracker_radius, 
														   kltTracker_threshold));
					}else if(trackerType.equals(TrackerParameters.KLT2)){
						parameters.getProcessingParameters().setTracker(
								trackerGenerator.createKLT_TwoPass(kltTracker_templateRadius, extracted_pyramidScaling, 
																   kltTracker_maxFeatures, kltTracker_radius, 
																   kltTracker_threshold));
					}
				}catch(Exception e){//If the Tracker generation fails
					//Returns false
					return false;
				}
				break;				
			case TrackerParameters.SURF: //If the SURF Tracker has been selected:
				try{//Tries to generate the Tracker
					parameters.getProcessingParameters().setTracker(
							trackerGenerator.createSURF(surfTracker_maxFeaturesPerScale, surfTracker_extractRadius, 
														surfTracker_initialSampleSize));
				}catch(Exception e){//If the Tracker generation fails
					//Returns false
					return false;
				}
				break;
			case TrackerParameters.SURF2: //If the SURF-2Pass Tracker has been selected:
				try{//Tries to generate the Tracker
					parameters.getProcessingParameters().setTracker(
							trackerGenerator.createSURF_TwoPass(surfTracker_maxFeaturesPerScale, surfTracker_extractRadius, 
																surfTracker_initialSampleSize));
				}catch(Exception e){//If the Tracker generation fails
					//Returns false
					return false;
				}
				break;
			case TrackerParameters.DEFAULT_TRACKER: //If the Default Tracker (KLT-2Pass with default parameters) has been selected:
				try{//Tries to generate the Tracker
					parameters.getProcessingParameters().setTracker(trackerGenerator.create_default());
				}catch(Exception e){//If the Tracker generation fails
					//Returns false
					return false;
				}
				break;
			default: //If an invalid Tracker has been selected:
				//Returns false
				return false;	
		}
		
		//If the Tracker has been successfully created returns true 
		return true;
	}
	
	
	/**
	 * setupVisualOdometry
	 * - Tries to create and setup a Visual Odometry (object)
	 */
	@SuppressWarnings("unchecked")
	private boolean setupVisualOdometry(Parameters storedParameters){

		//Extracts parameters to read (from storedParameters):
		Class<I> imageType = storedParameters.getInternalImageParameters().getImageType().getImageTypeClass();
		String visualOdometryType = storedParameters.getVisualOdometryParameters().getVisualOdometryType();
		
		int monoPlaneInfinity_thresholdAdd = storedParameters.getVisualOdometryParameters().getMonoPlaneInfinity_thresholdAdd();
		int monoPlaneInfinity_thresholdRetire = storedParameters.getVisualOdometryParameters().getMonoPlaneInfinity_thresholdRetire();
		double monoPlaneInfinity_inlierPixelTol = storedParameters.getVisualOdometryParameters().getMonoPlaneInfinity_inlierPixelTol();
		int monoPlaneInfinity_ransacIterations = storedParameters.getVisualOdometryParameters().getMonoPlaneInfinity_ransacIterations();
		
		double monoPlaneOverhead_cellSize = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_cellSize();
		double monoPlaneOverhead_maxCellsPerPixel = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_maxCellsPerPixel();
		double monoPlaneOverhead_mapHeightFraction = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_mapHeightFraction();
		double monoPlaneOverhead_inlierGroundTol = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_inlierGroundTol();
		int monoPlaneOverhead_ransacIterations = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_ransacIterations();
		int monoPlaneOverhead_thresholdRetire = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_thresholdRetire();
		int monoPlaneOverhead_absoluteMinimumTracks = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_absoluteMinimumTracks();
		double monoPlaneOverhead_respawnTrackFraction = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_respawnTrackFraction();
		double monoPlaneOverhead_respawnCoverageFraction = storedParameters.getVisualOdometryParameters().getMonoPlaneOverhead_respawnCoverageFraction();
		
		
		//Extracts instantiated Tracker and Calibrations (From original Parameters)
		PointTracker<I> tracker = parameters.getProcessingParameters().getTracker();
		MonoPlaneParameters monoCalibration = parameters.getProcessingParameters().getMonoCalibration();
		StereoParameters stereoCalibration = parameters.getProcessingParameters().getStereoCalibration();
		VisualDepthParameters depthCalibration = parameters.getProcessingParameters().getDepthCalibration();
		
		//Creates a new VisualOdometryGenerator, usable to generate a custom Visual Odometry based 
		//on the selected parameters
		VisualOdometryGenerator visualOdometryGenerator = new VisualOdometryGenerator(tracker, imageType, null);
		visualOdometryGenerator.setMonoCalibration(monoCalibration);
		visualOdometryGenerator.setStereoCalibration(stereoCalibration);
		visualOdometryGenerator.setDepthCalibration(depthCalibration);
		
		switch(visualOdometryType){
			case VisualOdometryParameters.MONOPLANEINFINITY: //If the monoPlaneInfinity has been selected:
				try{ //Tries to generate the Visual Odometry
					parameters.getProcessingParameters().setVisualOdometry(
							visualOdometryGenerator.create_monoPlaneInfinity(monoPlaneInfinity_thresholdAdd, 
									monoPlaneInfinity_thresholdRetire, monoPlaneInfinity_inlierPixelTol, 
									monoPlaneInfinity_ransacIterations));
					//If the VisualOdometryGenerator returns null Visual Odometry, returns false (fail)
					if(parameters.getProcessingParameters().getVisualOdometry() == null) return false;
				}catch(Exception e){//If the Visual Odometry generation fails
					//Returns false
					return false;
				}
				break;
			case VisualOdometryParameters.MONOPLANEOVERHEAD: //If the monoPlaneOverhead has been selected:
				try{ //Tries to generate the Visual Odometry
					parameters.getProcessingParameters().setVisualOdometry(
							visualOdometryGenerator.create_monoPlaneOverhead(monoPlaneOverhead_cellSize, 
									monoPlaneOverhead_maxCellsPerPixel, monoPlaneOverhead_mapHeightFraction, 
									monoPlaneOverhead_inlierGroundTol, monoPlaneOverhead_ransacIterations, 
									monoPlaneOverhead_thresholdRetire, monoPlaneOverhead_absoluteMinimumTracks, 
									monoPlaneOverhead_respawnTrackFraction, monoPlaneOverhead_respawnCoverageFraction));
					//If the VisualOdometryGenerator returns null Visual Odometry, returns false (fail)
					if(parameters.getProcessingParameters().getVisualOdometry() == null) return false;
				}catch(Exception e){//If the Visual Odometry generation fails
					//Returns false
					return false;
				}
				break;
			case VisualOdometryParameters.STEREODEPTH:			//If stereoDepth, stereoDualTrackerPnp, stereoQuadPnp or
			case VisualOdometryParameters.STEREODUALTRACKERPNP:	//depthDepthPnp have been selected:
			case VisualOdometryParameters.STEREOQUADPNP:
			case VisualOdometryParameters.DEPTHDEPTHPNP:
				//Returns false (not implemented)
				return false;
			case VisualOdometryParameters.DEFAULT_VISUALODOMETRY: //If Default Visual Odometry (monoPlaneInfinity with default parameters) has been selected:
				try{ //Tries to generate the Visual Odometry
					parameters.getProcessingParameters().setVisualOdometry(
							visualOdometryGenerator.create_monoPlaneInfinity(75, 2, 1.5, 200));
					//If the VisualOdometryGenerator returns null Visual Odometry, returns false(fail)
					if(parameters.getProcessingParameters().getVisualOdometry() == null) return false;
				}catch(Exception e){//If the Visual Odometry generation fails
					//Returns false
					return false;
				}
				break;
			default: //If unknow Visual Odometry has been selected:
				//Returns false
				return false;
		}
		
		//If the Visual Odometry has successfully created returns true
		return true;
	}
	
	
	/**
	 * process
	 * - Tries to start the correct Visual Odometry process
	 *   depending on the selected Visual Odometry Type
	 */
	@SuppressWarnings("unchecked")
	private boolean process(Parameters storedParameters){

		//Extracts parameters to read (from storedParameters):
		String visualOdometryType = storedParameters.getVisualOdometryParameters().getVisualOdometryType();
		
		//Extracts instantiated Visual Odometry (From original Parameters)
		VisualOdometry<I> visualOdometry = parameters.getProcessingParameters().getVisualOdometry();
		
		switch(visualOdometryType){
			case VisualOdometryParameters.DEFAULT_VISUALODOMETRY: //If Default/monoPlaneInfinity/monoPlaneOverhead has been selected:
			case VisualOdometryParameters.MONOPLANEINFINITY:
			case VisualOdometryParameters.MONOPLANEOVERHEAD:
				//Specifies the visualOdometry object to Mono and 
				//tries to start a Monocular Visual Odometry Process
				MonocularPlaneVisualOdometry monoVisualOdometry = (MonocularPlaneVisualOdometry) visualOdometry;
				return processMonoVisualOdometry(monoVisualOdometry, storedParameters); //Executes the process and returns the exit status
			case VisualOdometryParameters.STEREODEPTH:
			case VisualOdometryParameters.STEREODUALTRACKERPNP:
			case VisualOdometryParameters.STEREOQUADPNP: //If stereoDepth/stereoDualTrackerPnp/stereoQuadPnp has been selected:
				//Specifies the visualOdometry object to Stereo and 
				//tries to start a Stereo Visual Odometry Process
				StereoVisualOdometry stereoVisualOdometry = (StereoVisualOdometry) visualOdometry;
				return processStereoVisualOdometry(stereoVisualOdometry, storedParameters); //Executes the process and returns the exit status
			case VisualOdometryParameters.DEPTHDEPTHPNP: //If depthDepthPnp has been selected:
				//Specifies the visualOdometry object to Depth and 
				//tries to start a Depth Visual Odometry Process
				DepthVisualOdometry depthVisualOdometry = (DepthVisualOdometry) visualOdometry;
				return processDepthVisualOdometry(depthVisualOdometry, storedParameters); //Executes the process and returns the exit status
			default: //If an unknown Visual Odometry has been selected:
				//Returns false
				return false;
		}
	}
	
	
	/**
	 * processMonoVisualOdometry
	 * - Process the Monocular Visual Odometry with all the specified Parameters,   
	 *   shows Output (processed) Video, shows Tracks, estimates the Visual Odometry, 
	 *   updates Charts, coordinates, Output (processing) FPS Info.
	 */
	@SuppressWarnings("unchecked")
	private boolean processMonoVisualOdometry(MonocularPlaneVisualOdometry<I> monoVisualOdometry, Parameters storedParameters){

		/*Sets Processing Flag to true (processing is started)*/
		parameters.getProcessingFlags().setProcessingVisualOdometry(true);
		
		/**Initialization**/
		
		/*Loads Parameters/Components needed*/
		
		//Extracts parameters that have to remain static during processing (from storedParameters):
		final String inputSource = storedParameters.getInputParameters().getInputSource();
		final Class<I> imageType = storedParameters.getInternalImageParameters().getImageType().getImageTypeClass();
		final boolean imageKeepOriginal = storedParameters.getInternalImageParameters().isImageKeepOriginal();
		final int imageResizeWidth = storedParameters.getInternalImageParameters().getImageResizeWidth();
		final int imageResizeHeight = storedParameters.getInternalImageParameters().getImageResizeHeight();
		final String chartType = storedParameters.getChartOutputParameters().getChartType();
		
		//Extracts Images Buffer from original (dynamic) Parameters
		final ArrayList<BufferedImage> buffer = parameters.getDeviceParameters().getBuffer();
		
		//Extracts Input/Output Video Frames and Panels, Info Panel, XZ Chart Panel  and Y Chart Panel 
		//from guiComponent
		ImagePanel inputVideoPanel = (ImagePanel)parameters.getGuiComponents().get("inputVideoPanel");
		JFrame inputVideoFrame = (JFrame)parameters.getGuiComponents().get("inputVideoFrame");
		ImagePanel outputVideoPanel = (ImagePanel)parameters.getGuiComponents().get("outputVideoPanel");
		JFrame outputVideoFrame = (JFrame)parameters.getGuiComponents().get("outputVideoFrame");
		ChartScrollPane chartXZPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartXZPanel");
		ChartScrollPane chartYPanel = (ChartScrollPane)parameters.getGuiComponents().get("chartYPanel");
		InfoScrollPane chartInfoPanel = (InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel");

		
		/*Resizes, reposition and set correct title to Input Video Frame and Output(Processed) Video Frame
		  (Depending on the input and output parameters)*/
		resizeAndRepositionVideoFrames(storedParameters);
		
		
		/*Sets Y Chart Panel Axis names (depending on selected Chart Type)*/
		switch(chartType){
			case ChartOutputParameters.YFRAMES_CHART:
				chartYPanel.setAxisNames("frame", "Y");
				break;
			case ChartOutputParameters.YSECONDS_CHART:
				chartYPanel.setAxisNames("seconds", "Y");
				break;
		}
		
		
    	/*Adds new Chart begin entry, to Info Panel Points List*/
    	String listData = "Chart "+(chartXZPanel.getChartsCount()+1);
    	chartInfoPanel.addListData(listData);

    	/*Creates and resets Distance covered Calculation Parameters*/
    	double distanceXZ = 0;
    	double previousX = 0; //X Sampled each frame
    	double previousZ = 0; //Z Sampled each frame
    	double distanceY = 0;
    	double previousY = 0; //Y Sampled each frame
    	
    	/*Creates and resets Rotation/Altitude Panels variables*/
    	double previousPointAngle = 0;
    	double prevX = 0; //X Sampled each 10 frames
    	double prevZ = 0; //Z Sampled each 10 frames
    	double prevY = 0; //Y Sampled each 10 frames
    	
		/*Creates and resets FPS Calculation Parameters*/
    	int totalFrames=0; //Counts also skipped frames (used for Frame decimation)
    	int totalProcessedFrames=0;	//Counts only processed frames (used for processing info and FPS calculation)
		long startTime = System.currentTimeMillis(); //Processing start time
		long currentTime = 0; //Processing current time
		BigDecimal totalSeconds = new BigDecimal(0); //Total processing elapsed time
		float averageFps = 0; //Average FPS counter (totalProcessedFrames/totalSeconds)
    	long partialTime = System.currentTimeMillis(); //Partial (1 second counter) start time
    	int currentFps=0; //Instant FPS counter (currentFps content in 1 second)
    	
    	
    	/**Processing Cycle Begin**/
    	while(parameters.getProcessingFlags().isProcessingVisualOdometry()){ //While Processing Flag is true
    		
    		/** PROCESSING FLAGS CHECK*/
    		
    		/*Verify if the Processing has to be terminated*/
    		switch(inputSource){ 
    			case InputParameters.VIDEO_INPUT: //Case Video Input
        			if(!parameters.getProcessingParameters().getVideo().hasNext()){ //If we haven't more video frames, tells to terminate
        				parameters.getProcessingFlags().setProcessingVisualOdometry(false); 
        				continue;
        			}
    				break;
    			case InputParameters.DEVICE_INPUT: //Case Device Input
    				if(!storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)") 
    						&& parameters.getDeviceParameters().isStoppedCaptureFlag() //If the capture isStopped and the buffer is empty, tells to terminate 
    						&& buffer.size()<=0){
    					parameters.getProcessingFlags().setProcessingVisualOdometry(false);
    					continue;
    				}else if(storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)") 
    						&& parameters.getDeviceParameters().isStoppedCaptureFlag()){ //If the capture isStopped, tells to terminate
    					parameters.getProcessingFlags().setProcessingVisualOdometry(false);
    					continue;
    				}
    				break;
    			default: //Case Unknown
    				parameters.getProcessingFlags().setProcessingVisualOdometry(false); //Tells to terminate
    				return false;	
    		}
    		
    		
    		/*Checks Pause Flag, and in case is true, loops the Process here (Paused status)*/
			while(parameters.getProcessingFlags().isPauseVisualOdometry()){
				try {
					//Updates Info Status Label (to Status: Processing paused.)
		    		if(!chartInfoPanel.lbl_status.getText().equalsIgnoreCase("<html><b>Status: </b>Processing paused.</html>")){
		    			chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Processing paused.</html>");
		    		}
					Thread.sleep(100); //Sleeps for 100 milliseconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
			/*Checks Reset (Visual Odometry) Flag; in case is true Resets VO Context*/
			if(parameters.getProcessingFlags().isResetVisualOdometry()){
				parameters.getProcessingFlags().setResetVisualOdometry(false);
				monoVisualOdometry.reset();
			}
			
			
			/*Checks Stop Flag, and in case is true stops processing*/
			if(parameters.getProcessingFlags().isStopVisualOdometry()) {
				
				//Adds an end point to the XZ and Y Chart (that indicates current Chart end)
				chartXZPanel.addEndPoint();
				chartYPanel.addEndPoint();

				//Adds an end chart entry to the Info Panel Points List 
				chartInfoPanel.addListData("End Chart "+chartXZPanel.getChartsCount());

				//Updates Info Status Label (to Status: Processing stopped.)
				chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Processing stopped.</html>");
				
				//Sets to false Stop Flag and Processing Flag
				parameters.getProcessingFlags().setStopVisualOdometry(false); 
				parameters.getProcessingFlags().setProcessingVisualOdometry(false);				
				
				switch(inputSource){
					case InputParameters.VIDEO_INPUT://If Video input is selected:
						parameters.getProcessingParameters().getVideo().close(); //Close Video file
						break;
					case InputParameters.DEVICE_INPUT://If Device input is selected:
						if(!parameters.getDeviceParameters().isStoppedCaptureFlag())//If Capture isn't stopped
								parameters.getDeviceParameters().setStopCaptureFlag(true);//Stops capture (Sets StopCapture Flag to true)				
						if(buffer.size()>0) //If Buffer isn't empty
								buffer.clear(); //Clears Buffer
						if(storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
							parameters.getDeviceParameters().getWebcam().close();
						}
						break;
				}
				
				//Returns true (exit Processing successfully)
				return true;				
			}
			
			
			/*Checks Clear Flag, and in case is true stops processing and clears all Charts/History*/
    		if(parameters.getProcessingFlags().isClearVisualOdometry()) {
    			
    			//Clears all Charts (saved Points) and resets Charts Size
    			chartXZPanel.clearAllPoints();
				chartXZPanel.resetSize();
				chartYPanel.clearAllPoints();
				chartYPanel.resetSize();
				
				//Clears all Info Panel Points List data
				chartInfoPanel.clearListData();
				
				//Updates Info Status Label (to Status: Cleared.)
				chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Cleared.</html>");
				//Sets the Info Panel not visible
				chartInfoPanel.setInfoPanelVisible(false);
    			
				//Sets to false Clear Flag and Processing Flag
    			parameters.getProcessingFlags().setClearVisualOdometry(false);
    			parameters.getProcessingFlags().setProcessingVisualOdometry(false);
    			
				switch(inputSource){
					case InputParameters.VIDEO_INPUT://If Video input is selected:
						parameters.getProcessingParameters().getVideo().close(); //Close Video file
						break;
					case InputParameters.DEVICE_INPUT://If Device input is selected:
						if(!parameters.getDeviceParameters().isStoppedCaptureFlag())//If Capture isn't stopped
								parameters.getDeviceParameters().setStopCaptureFlag(true);//Stops capture (Sets StopCapture Flag to true)				
						if(buffer.size()>0) //If Buffer isn't empty
								buffer.clear(); //Clears Buffer
						if(storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
							parameters.getDeviceParameters().getWebcam().close();
						}
						break;
				}
    			
				//Returns true (exit Processing successfully)
    			return true;
    		}
    
    		
    		if(inputSource.equals(InputParameters.DEVICE_INPUT) //If we are using WebcamCapture (BoofCv integrated) Device  
    															//and we have got stopRequest (from Stop after button)
    			&& storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")
    			&& parameters.getDeviceParameters().isStopCaptureFlag()){
    			
    			parameters.getDeviceParameters().getWebcam().close(); //Stops capture
				parameters.getDeviceParameters().setStopCaptureFlag(false);//Sets StopCapture Flag to false
				parameters.getDeviceParameters().setStoppedCaptureFlag(true);//Sets isStoppedCaptureFlag to true
    			
				continue;
    		}
    			
    		
    		
    		//If the Processing keeps going on, updates Info Status Label (to Status: Processing...)
    		if(!chartInfoPanel.lbl_status.getText().equalsIgnoreCase("<html><b>Status: </b>Processing...</html>")){
    			chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Processing...</html>");
    		}

    		
    		
    		/** Images Acquisition and Transformation before Processing */
    		
    		
    		/*ACQUIRE INPUT DATA (from input video)*/
    		
    		BufferedImage inputImage; //Creates inputImage (BufferedImage)

    		boolean frameToDecimate; //Variable to read if current frame has to be decimated
    		
    		switch(inputSource){
    			case InputParameters.VIDEO_INPUT: //If Video input

    				/* If frameToDecimate and isFrameDecimateEnabled are true, skips the Frame,
    	    		 * else reads the inputImage(BufferedImage for GUI) and the inputLeftImg(BoofCv format for Processing) 
    	    		 * (from Video file)*/
    	    		
    	    		/*Increases totalFrames Counter, and checks if the frame is to decimate*/
    	    		totalFrames++;
    	    		frameToDecimate = 
    	    				(totalFrames%parameters.getInternalImageParameters().getFrameDecimateValue()!=0);

    				if(parameters.getInternalImageParameters().isFrameDecimateEnabled() //If the frame has to be decimated 
    						&& frameToDecimate){
    					parameters.getProcessingParameters().getVideo().next(); //Skip frame
    					continue;
    				}
    				//Sets inputLeftImg (to the next image given from the video sequence object)
    				parameters.getProcessingParameters().setLeftImg(
    						(I)parameters.getProcessingParameters().getVideo().next());
    				//Sets inputImage (to the current gui image given from the video sequence object)
    				inputImage = (BufferedImage) parameters.getProcessingParameters().getVideo().getGuiImage();
    				break;
    			case InputParameters.DEVICE_INPUT: //If Device input
    				//If the Buffer is still empty (only for V4L4J implementation)
    				if(!storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)") &&
    				  (parameters.getProcessingParameters().getLeftImg() == null || buffer == null || buffer.size()<=0)){
    					try {
    						Thread.sleep(1); //Wait for Buffer replenishing (Sleeps for 1 millisecond)
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
    					continue;
    				}
    				
    	    		/* If frameToDecimate and isFrameDecimateEnabled are true, skips the Frame,
    	    		 * else reads the inputImage(BufferedImage for GUI) and the inputLeftImg(BoofCv format for Processing) 
    	    		 * (from Device Buffer)*/
    	    		
    	    		/*Increases totalFrames Counter, and checks if the frame is to decimate*/
    	    		totalFrames++;
    	    		frameToDecimate = 
    	    				(totalFrames%parameters.getInternalImageParameters().getFrameDecimateValue()!=0);
    			
    				//If the frame has to be decimated
    				if(parameters.getInternalImageParameters().isFrameDecimateEnabled() 
    						&& frameToDecimate){
    					if(!storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
        					buffer.remove(0); //Remove from Buffer and skip this Frame    						
    					}else if(storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
    						parameters.getDeviceParameters().getWebcam().getImage();
    					}
    					continue;
    				}
    			
    				if(!storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
	    				//Sets inputImage (to the first Buffer element and remove it from Buffer, the Buffer contains GUI images)
	    				inputImage = buffer.get(0);
	    				buffer.remove(0);
	    			
	    				//Sets inputLeftImg
	    				//(leftImg has been already initialized in Device init as an empty image with the correct format,
	    				//so here we convert inputImage (GUI BufferedImage) into inputLeftImg with that format (Boofcv format))
	    				I inputLeftImg = (I)parameters.getProcessingParameters().getLeftImg();
	    				ConvertBufferedImage.convertFrom(inputImage,inputLeftImg,true);
	    				parameters.getProcessingParameters().setLeftImg(inputLeftImg);
    				}else{
	    				//Sets inputImage
	    				inputImage = parameters.getDeviceParameters().getWebcam().getImage();
	    			
	    				//Sets inputLeftImg
	    				//(leftImg has been already initialized in openDevice as an empty image with the correct format,
	    				//so here we convert inputImage (GUI BufferedImage) into inputLeftImg with that format (Boofcv format))
	    				I inputLeftImg = (I)parameters.getProcessingParameters().getLeftImg();
	    				ConvertBufferedImage.convertFrom(inputImage,inputLeftImg,true);
	    				parameters.getProcessingParameters().setLeftImg(inputLeftImg);
    				}
    				break;
    			default: //If unknown inputSource (theoretically never come here)
    				inputImage = new BufferedImage(0, 0, 0); //Sets empty inputImage (could give errors)
    				break;
    		}

    		
    		
    		/*CREATES OUTPUT DATA (output video to show and process)*/
    		
    		/* Depending on Internal Image Parameters transforms the input images into 
    		 * the images to process (Output Images) */

    		I outputLeftImg; //Creates outputLeftImg (Output Video Image to Process)
    		BufferedImage outputImage; //Creates outputImage (Correspondent GUI Image to Show)
			
    		//Extracts needed parameters from Parameters (real time modifiable)
    		boolean isInternalImagePreview = parameters.getChartOutputParameters().isInternalImagePreview();
    		boolean isInputPreviewEnabled = parameters.getInputParameters().isInputPreviewEnabled();
    		
    		
    		if(imageKeepOriginal){ //If image Keep Original is selected (no resize requested):
    			
    			//Sets outputLeftImg = inputLeftImg (The input is processed as is)
    			outputLeftImg = (I)parameters.getProcessingParameters().getLeftImg();

    			/*Sets outputImage (output image to show)
    			 *- To an internalCopy of outputLeftImg(a conversion of the boofcv image to process
    			 *  into a GUI BufferedImage) if Internal Image Preview is selected;
    			 
    			 *- Else to a deepCopy of inputImage (with different reference) if inputSource 
    			 *  is Video and InputPreview is enabled, because the Output Image to Show
    			 *  has to be different from the Input Image, that will be showed too, 
    			 *  otherwise the output resize and Tracks will be drawn also on input;
    			 
    			 *- To inputImage (same reference) if inputSource is Device, because device callback 
    			 *  shows itself original inputImage and so we can modify it here and use it 
    			 *  as outputImage, or if InputPreview is disabled, because also if we have 
    			 *  Video inputSource, we haven't to show inputImage, so we can modify it.
    			 *  (This saves resources because there is no need to make a deepCopy in this case)
    			 */
    			outputImage = (isInternalImagePreview? 
    									internalCopy(outputLeftImg)
    									:((inputSource.equals(InputParameters.VIDEO_INPUT)||(inputSource.equals(InputParameters.DEVICE_INPUT) && storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")))
    											&& isInputPreviewEnabled)?
    											deepCopy(inputImage):
    											inputImage);
    			
    		}else{ //If image Keep Original is not selected (resize requested)
    			
    			//Sets outputImage to resized copy(with different reference) of inputImage
    			outputImage = resizeBufferedImage(inputImage, imageResizeWidth, imageResizeHeight);
    			
    			//Sets outputLeftImg to a conversion of outputImage into BoofCv format(with different reference)
   				outputLeftImg = ImageType.single(imageType).createImage(imageResizeWidth, imageResizeHeight);
    			ConvertBufferedImage.convertFrom(outputImage,outputLeftImg,true);

    			//If Internal Image Preview is selected, sets outputImage to an internalCopy of outputLeftImg
    			//(a conversion of the image to process into a BufferedImage)
    			if(isInternalImagePreview)outputImage = internalCopy(outputLeftImg);
			}
    		
    		
    		
    		/** MONOCULAR VISUAL ODOMETRY PROCESSING **/
    		
    		try{ //Tries to process Visual Odometry on outputLeftImg (image to process):
    			if( !monoVisualOdometry.process(outputLeftImg) ) { //If the algorithm fails ego-motion estimation for current image 
    				
    				//Updates Info Status Label (to Status: VO Failed.)
    				chartInfoPanel.lbl_status.setText("<html><b>Status: </b>VO Failed.</html>");
    				
    				//throw new RuntimeException("VO Failed!");
    			}
    		}catch(Exception e){ //If there is an error/exception in processing current image
    			
    			//Updates Info Status Label (to Status: Visual Odometry processing error/exception. Check parameters.)
    			chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Visual Odometry processing error/exception. Check parameters.</html>");
    			e.printStackTrace();

				//Adds an end point to the XZ and Y Chart (that indicates current Chart end)
				chartXZPanel.addEndPoint();
				chartYPanel.addEndPoint();

				//Adds an end chart entry to the Info Panel Points List 
				chartInfoPanel.addListData("End Chart "+chartXZPanel.getChartsCount());
				
				//Sets to false Processing Flag
				parameters.getProcessingFlags().setProcessingVisualOdometry(false);				

				switch(inputSource){
				case InputParameters.VIDEO_INPUT://If Video input is selected:
					parameters.getProcessingParameters().getVideo().close(); //Close Video file
					break;
				case InputParameters.DEVICE_INPUT://If Device input is selected:
					if(!parameters.getDeviceParameters().isStoppedCaptureFlag())//If Capture isn't stopped
							parameters.getDeviceParameters().setStopCaptureFlag(true);//Stops capture (Sets StopCapture Flag to true)				
					if(buffer.size()>0) //If Buffer isn't empty
							buffer.clear(); //Clears Buffer
					if(storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
						parameters.getDeviceParameters().getWebcam().close();
					}
					break;
				}

    			//Returns false (exit Processing with a failure)
    			return false;
    		}

    		
    		/*If the algorithm has estimated successfully the ego-motion
    		 *
    		 * Extracts Real World Informations for current frame: 
    		 *
    		 **/
    		
			Se3_F64 leftToWorld = monoVisualOdometry.getCameraToWorld(); //Gets CameraToWorld data (Translation+Rotation)
			Vector3D_F64 T = leftToWorld.getT(); //Gets Translation data from CameraToWorld
			String Rotation = leftToWorld.R.toString();	//Gets Rotation data from CameraToWorld
		

			
			/** Input Video Rendering */
			
			//If the inputSource is Video file (or WebcamCapture Device): (V4L4J Device shows input video autonomously into the callback)			
			if(inputSource.equals(InputParameters.VIDEO_INPUT)
					||(inputSource.equals(InputParameters.DEVICE_INPUT) 
							&& storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)"))
					){
				if(parameters.getInputParameters().isInputPreviewEnabled()){ //If Input Preview is enabled
					if(!inputVideoFrame.isVisible())inputVideoFrame.setVisible(true); //If Input Video Frame isn't visible sets it to visible
						inputVideoPanel.setBufferedImage(inputImage); //Sets inputImage as Input Video Panel GUI Image to show
						inputVideoPanel.repaint(); //Forces Input Video Panel to repaint
				}else{ //If Input Preview is disabled
					if(inputVideoFrame.isVisible())inputVideoFrame.dispose(); //If Input Video Frame is visible, disposes it
				}
			}

			
			
			/** Output Video Rendering (Processed Video) */
				
			/*Feature Tracks Drawing on Output Video*/
			
			//Creates a graphic object on outputImage
			Graphics2D g2 = outputImage.createGraphics();
			
			if(parameters.getTrackerParameters().isTrackerShowActiveTracks()){ //If Tracker Show Active Tracks is enabled
				//Draws active tracks as blue dots
				for( PointTrack p : 
						((PointTracker<I>)parameters.getProcessingParameters().getTracker()).getActiveTracks(null) ) {
					VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.blue);
				}
			}
			
			if(parameters.getTrackerParameters().isTrackerShowNewTracks()){ //If Tracker Show New Tracks is enabled
				//Draws tracks which have just been spawned as green dots
				for( PointTrack p : 
						((PointTracker<I>)parameters.getProcessingParameters().getTracker()).getNewTracks(null) ) {
					VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.green);
				}
			}

			/*
			if(true){ //If VO Show Inliers is enabled
				//Draws inliers
				int trackNumber=0;
				for( Point2D_F64 p : 
						((AccessPointTracks3D)monoVisualOdometry).getAllTracks()) {
					if (((AccessPointTracks3D)monoVisualOdometry).isInlier(trackNumber))
						VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, Color.magenta);
					trackNumber++;
				}
			}*/

			/*Showing Output Video*/
			
			//If Output Video Frame is not visible, sets it to visible
			if(!outputVideoFrame.isVisible())outputVideoFrame.setVisible(true);
			//Sets outputImage as Output Video Panel GUI Image to show
			outputVideoPanel.setBufferedImage(outputImage);
			//Forces Output Video Panel to repaint
			outputVideoPanel.repaint();
			
	
			
			/** Informations Update */
			
			//Retrieves Time and Frames Information
			currentTime = System.currentTimeMillis(); //Sets currentTime to System current time
			totalSeconds = round_BigDecimal((float)(currentTime-startTime)/1000f,1); //Sets totalSeconds to total processing elapsed time from the beginning
			totalProcessedFrames++; //Increases totalProcessedFrames counter 
			
			//Makes Visible/Not Visible Info Components needed
			if(!chartInfoPanel.isInfoPanelVisible()) //If Info Panel was not visible sets it to visible 
				chartInfoPanel.setInfoPanelVisible(true); 
			if((inputSource.equals(InputParameters.VIDEO_INPUT) //If inputSource is Video Input or WebcamCapture and the Buffer
					||(inputSource.equals(InputParameters.DEVICE_INPUT) //ProgressBar is visible sets it to not visible
							&& storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)"))) 
					&& chartInfoPanel.progress_buffer_load.isVisible()){ 
				chartInfoPanel.setBufferInfoVisible(false);
			}

			
			//Sets Calibration File Label
			chartInfoPanel.lbl_calibration_file.setText("<html><b>Calibration file: </b>"+storedParameters.getInputParameters().getCalibrationPath()+"</html>");
			chartInfoPanel.lbl_calibration_file.setToolTipText("<html><b>Calibration file: </b>"+storedParameters.getInputParameters().getCalibrationPath()+"</html>");

			//Sets Processed Video Label to videoPath/devicePath
			if(inputSource.equals(InputParameters.VIDEO_INPUT)){
				chartInfoPanel.lbl_processed_file.setText("<html><b>Processed Video: </b>"+storedParameters.getInputParameters().getVideoPath()+"</html>");
				chartInfoPanel.lbl_processed_file.setToolTipText("<html><b>Processed Video: </b>"+storedParameters.getInputParameters().getVideoPath()+"</html>");
			}else{
				chartInfoPanel.lbl_processed_file.setText("<html><b>Processed Device: </b>"+storedParameters.getInputParameters().getDevicePath()+"</html>");
				chartInfoPanel.lbl_processed_file.setToolTipText("<html><b>Processed Device: </b>"+storedParameters.getInputParameters().getDevicePath()+"</html>");
			}
			
			//Sets Processed Frame Label to totalProcessedFrames (totalFrames-totalProcessedFrames skipped)
			chartInfoPanel.lbl_processed_frame.setText("<html><b>Processed Frame: </b>"+totalProcessedFrames+" ("+(totalFrames-totalProcessedFrames)+" skipped)</html>");
			
			//Updates Elapsed Time Label
			if(totalSeconds.floatValue()<=60){ //If totalSeconds are <= 60 (less than a minute)
				//Sets Elapsed Time Label to totalSeconds sec
				chartInfoPanel.lbl_elapsed_time.setText("<html><b>Elapsed Time: </b>"+totalSeconds+" sec</html>");
			}else{ //If totalSeconds > 60 (more than a minute)
				//Sets totalMinutes to the int part of totalSeconds/60
				int totalMinutes =(int)totalSeconds.floatValue()/60;
				//Sets partialSeconds to totalSeconds%60 (the decimal part of the previous, and so relative seconds)
				BigDecimal partialSeconds = round_BigDecimal(totalSeconds.floatValue()%60,1);
				//Sets Elapsed Time Label to totalMinutes min : partialSeonds sec
				chartInfoPanel.lbl_elapsed_time.setText("<html><b>Elapsed Time: </b>"+totalMinutes+" min : "+partialSeconds+" sec</html>");
			}
			
			//Sets X Position Label to T.x (X/sides Translation estimation)
			chartInfoPanel.lbl_xpos.setText("<html><b>X: </b>"+T.x+"</html>");
			
			//Sets Y Position Label to T.y (Y/altitude Translation estimation)
			chartInfoPanel.lbl_ypos.setText("<html><b>Y: </b>"+(-T.y)+"</html>");
			
			//Sets Z Position Label to T.z (Z/forward Translation estimation)
			chartInfoPanel.lbl_zpos.setText("<html><b>Z: </b>"+T.z+"</html>");
		
			
			//Update Rotation and Altitude Panel:
		
			if(totalProcessedFrames%10==0){ //Each 10 frames (to filter little variations)
			
				/*Rotation Panel*/
				
				double relativeX = (T.x-prevX); //Relative X-coordinate between current and previous point
				double relativeZ = (T.z-prevZ); //Relative Z-coordinate between current and previous point
			
				double m = relativeZ/relativeX; //Angular coefficient of the (prevX,prevZ)-(X,Z) passing line

				double pointAngle = 0; //Effective angle between the current Point and 
									   //the X Axis passing for the previous Point
				if(relativeX >= 0 && relativeZ >= 0){ //First quadrant (+ +)
					pointAngle = Math.atan(m); // 0°<=pointAngle<=90°
				}else if(relativeX < 0 && relativeZ >= 0){ //Second quadrant (- +)
					pointAngle = Math.PI + Math.atan(m); // 90°<pointAngle<=180°
				}else if(relativeX < 0 && relativeZ < 0){ //Third quadrant (- -)
					pointAngle = Math.PI + Math.atan(m); // 180°<pointAngle<270°
				}else if(relativeX >=0 && relativeZ < 0){ //Fourth quadrant (+ -)
					pointAngle = 2*Math.PI + Math.atan(m); // 270°<=pointAngle<360°
				}
			
				if(totalProcessedFrames==1){previousPointAngle=pointAngle;} //If we are in the first frame, sets previous = current angle 
																			//(so have no fake rotation on start)
			
				double rotationAngle = pointAngle - previousPointAngle; //Rotation angle as difference 
																		//between the current Point angle and the previous 
																		//Point angle:
																		//- Positive for counter-clockwise rotations
																		//- Negative for clockwise rotations
																		//- Zero for no rotation
			
				//Calculates coordinates to display on screen rotation angle:
				double vectorX = -Math.sin(rotationAngle);//As X-Axis coordinate the sine of the angle inverted 
														  //(because is used as X-Axis coord)
				double vectorY = Math.cos(rotationAngle); //As Y-Axis coordinate the cosine of the angle
														  //(remains the same also if used as Y-Axis coord)

				//Updates Rotation panel to the coordinates of the rotation angle
				chartInfoPanel.pnl_rotation.setDirection((int)(vectorX*10000), (int)(vectorY*10000));
				chartInfoPanel.pnl_rotation.repaint();
			
				//Updates Rotation panel ToolTipText to Rotation Angle value, sin value and cos value
				chartInfoPanel.pnl_rotation.setToolTipText("Rotation: "+round((float)(rotationAngle*180/Math.PI),2)+"° "
										+(rotationAngle>=0?
												"(counter-clockwise)"
												:"(clockwise)")
										+"(Sin: "+round((float)-vectorX,2)+", Cos: "+round((float)vectorY,2)+")");

				//Sets current pointAngle value, and current coordinates to previous, so that in the next
				//frame we can calculate the new pointAngle and compare it to the current
				previousPointAngle = pointAngle;
				prevX = T.x;
				prevZ = T.z;

				
				/*
				//Direction Information //This code follows the joining line between previous and current point (direction)
				  		double m = (T.z-previousZ)/(T.x-previousX);
						double vectorX = Math.signum(T.x-previousX)*1; //This is the current X direction vector
						double vectorY = Math.signum(T.x-previousX)*m; //This is the current Y direction vector
				 */

				
				/*Altitude Panel*/
				
				if(totalProcessedFrames==1){prevY=T.y;} //If we are in the first frame, sets previous = current Y 
														//(so have no fake altitude variation on start)

				vectorY = -(T.y-prevY); //As Y-Axis coordinate the difference between current and previous Altitude
										//(obtains increment/decrement in altitude value), inverted because VO algorithm
										//outputs inverted Y value
				
				//Updates Altitude panel to the signum of vectorY (positive for increment, negative for decrement)
				chartInfoPanel.pnl_altitude.setDirection(0, (int)Math.signum(vectorY)*100); 
				chartInfoPanel.pnl_altitude.repaint();
				
				//Updates Altitude panel ToolTipText to vectorY (Altitude variation) value
				chartInfoPanel.pnl_altitude.setToolTipText("Altitude variation: "+round((float)vectorY,2)+" "
															+(vectorY>=0?
																	"(increment)"
																   :"(decrement)")
															);
				//Sets current Y coordinate as previous, so that in the next frame we can calculate the new 
				//Altitude variation
				prevY = T.y;

			}

			if(totalProcessedFrames%10==0){
			//Update Distance Label (Incremental distance):
			if(totalProcessedFrames==1){previousX=T.x;previousZ=T.z;}
			distanceXZ += Math.sqrt(Math.pow((T.x-previousX), 2)+Math.pow((T.z-previousZ), 2));
			previousX = T.x;
			previousZ = T.z;
			chartInfoPanel.lbl_distanceXZ.setText("<html><b>Distance covered (X/Z):</b> "+distanceXZ+"</html>");
			if(totalProcessedFrames==1){previousY=T.y;}
			distanceY += Math.abs(T.y-previousY);
			previousY = T.y;
			chartInfoPanel.lbl_distanceY.setText("<html><b>Altitude covered (Y):</b> "+distanceY+"</html>");
			}
			
			//Update Rotation Matrix Labels:
			int rotationRow=0;
			while(Rotation.indexOf("\n")!=-1){ //While we have another row in Rotation Matrix
				
				//Sets current Row Label to current Row
				chartInfoPanel.lbl_rotation_row[rotationRow].setText(Rotation.substring(0, Rotation.indexOf("\n")));
				
				//Delete current Row from Rotation Matrix
				Rotation = Rotation.substring(Rotation.indexOf("\n")+1);

				//Increases Row count
				rotationRow++;
			}
			
			//Retrieves Visual Odometry inliers/new/total tracks informations
			int inliers = countInliers(monoVisualOdometry); //Counts current Inliers (Matched Tracks) in this Mono Visual Odometry
			int newTracks = countNewTracks(monoVisualOdometry); //Counts current New Tracks in this Mono Visual Odometry
			int totalTracks = countTotalTracks(monoVisualOdometry); //Counts current Total Tracks in this Mono Visual Odometry
			BigDecimal inliersPercent = new BigDecimal("0"); //Counts Inliers/TotalTracks perCent
			try{inliersPercent = round_BigDecimal(100.0f*inliers/totalTracks,3);}catch(Exception e){}
			
			//Sets Tracked Features Label to totalTracks (inliers: inliers, new tracks: newTracks)
			chartInfoPanel.lbl_tracks.setText("<html><b>Total tracked features:</b> "+totalTracks+" (inliers: "+inliers+" , new tracks: "+newTracks+")</html>");
			
			//Sets Inliers Label to inliersPercent (matches/total)
			chartInfoPanel.lbl_inliers.setText("<html><b>Inliers (matches):</b> "+inliersPercent+"%</html>");

			//Retrieves Processing Average FPS
			averageFps = 0;
			try{averageFps = round((totalProcessedFrames/totalSeconds.floatValue()),2);}catch(Exception e){}			 
			
			//If inputSource is Video Input
			if(inputSource.equals(InputParameters.VIDEO_INPUT)){
				//Sets Input Average FPS Label to Processing Average FPS: 
				//(because the Input Video is read at the same rate of the Output, inside the same Process; 
				//for Device instead we have different FPS rate between Input and Output because the Input Video 
				//is read in the device callback and is passed via Buffer, and the Input Average FPS Label is 
				//already updated in the same callback)
				chartInfoPanel.lbl_input_fps_average.setText("<html><b>Average FPS:</b> "+averageFps+ " fps</html>");
			}//If inputSource is WebcamCapture device
			else if(inputSource.equals(InputParameters.DEVICE_INPUT) && storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
				//Sets Input Average FPS Label
				chartInfoPanel.lbl_input_fps_average.setText("<html><b>Average FPS:</b> "+round((float)parameters.getDeviceParameters().getWebcam().getFPS(),2)+ " fps</html>");
			}
			
			//Sets Output(VO Processing) Average FPS to Processing Average FPS
			chartInfoPanel.lbl_vo_fps_average.setText("<html><b>Average FPS:</b> "+averageFps+ " fps</html>");


			
			/** CHARTS RENDERING / POINTS LOG / CURRENT FPS CALCULATION **/
			
			
			/*Charts Rendering*/
			
			/*
			//Sets Charts Background to White
			chartXZPanel.setBackgroundColor(Color.white);
			chartYPanel.setBackgroundColor(Color.white);
			*/
			
			//If XZ Chart scale factor is changed, updates it (from parameters, real-time modifiable)
			if(chartXZPanel.getChartScalingFactor()!=parameters.getChartOutputParameters().getChartXZ_Scale()){
				chartXZPanel.setChartScalingFactor(parameters.getChartOutputParameters().getChartXZ_Scale());
				chartXZPanel.resetSize();
			}

			//If Y Chart scale factor is changed, updates it (from parameters, real-time modifiable)
			if(chartYPanel.getChartScalingFactor()!=parameters.getChartOutputParameters().getChartY_Scale()){
				chartYPanel.setChartScalingFactor(parameters.getChartOutputParameters().getChartY_Scale());
				chartYPanel.resetSize();
			}
			
			//Adds estimated XZ point to XZ Chart (2D TRANSLATION)
	    	chartXZPanel.addPoint(T.x, T.z);
			
			//If chartType = ChartOutputParameters.YFRAMES_CHART:
			if(chartType.equals(ChartOutputParameters.YFRAMES_CHART)){
				
				//Adds (current frame,Y) point to Y Chart (ALTITUDE PER FRAME)
				chartYPanel.addPoint(totalProcessedFrames, -T.y);
			
				/*Points Log Update*/
				//Adds new Point Entry to Points List in Info Panel
				listData = "Frame: "+totalProcessedFrames+", "
						  +"El. Time: "+totalSeconds.floatValue()+" s, "
						  +"Location X: "+round_BigDecimal((float)T.x,2)+", "
						  +"Y: "+round_BigDecimal((float)-T.y,2)+", "
						  +"Z: "+round_BigDecimal((float)T.z,2)+", "
						  +"inliers: "+inliersPercent+"%, "
						  +"(Chart Type Y/f)";
				chartInfoPanel.addListData(listData);
			}
			
			
			//If 1 second of processing has passed (currentTime-partialTime>=1000 millisec)
			if((currentTime-partialTime)>=1000){
				
				//If chartType = ChartOutputParameters.YSECONDS_CHART:
				if(chartType.equals(ChartOutputParameters.YSECONDS_CHART)){
					
					//Adds (current second,Y) point to Y Chart (ALTITUDE PER SECOND)
					chartYPanel.addPoint(totalSeconds.floatValue(), -T.y);
				
					/*Points Log Update*/
					//Adds new Point Entry to Points List in Info Panel (With Y Location information)
					listData = "Frame: "+totalProcessedFrames+", "
							  +"El. Time: "+totalSeconds.floatValue()+" s, "
							  +"Location X: "+round_BigDecimal((float)T.x,2)+", "
							  +"Y: "+round_BigDecimal((float)-T.y,2)+", "
							  +"Z: "+round_BigDecimal((float)T.z,2)+", "
							  +"inliers: "+inliersPercent+"%, "
							  +"(Chart Type Y/s)";
					chartInfoPanel.addListData(listData);
				}

				
				/*Current FPS Update*/
				
				//Increases currentFps counter (retrieves Processing Current FPS)
				currentFps++;
				
				//If inputSource is Video Input
				if(inputSource.equals(InputParameters.VIDEO_INPUT) 
						||(inputSource.equals(InputParameters.DEVICE_INPUT) && storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)"))){
					//Sets Input Current FPS to Processing Current FPS (only for Video Input are the same)
					chartInfoPanel.lbl_input_fps_current.setText("<html><b>Current FPS:</b> "+currentFps+" fps\n");
				}
				
				//Sets Output(VO Processing) Current FPS to Processing Current FPS
				chartInfoPanel.lbl_vo_fps_current.setText("<html><b>Current FPS:</b> "+currentFps+" fps\n");
				
				//Resets currentFps counter and partial(Start)Time
				currentFps=0;		
				partialTime=System.currentTimeMillis();
				
			}else{ //If 1 second of processing hasn't passed yet (so for each frame enters here)
				
				//Increases currentFps counter
				currentFps++;
				
				//If chartType = ChartOutputParameters.YSECONDS_CHART:
				if(chartType.equals(ChartOutputParameters.YSECONDS_CHART)){
				
					/*Points Log Update*/
					//Adds new Point Entry to Points List in Info Panel (Without Y Location information) 
					listData = "Frame: "+totalProcessedFrames+", "
							  +"El. Time: "+totalSeconds.floatValue()+" s, "
							  +"Location X: "+round_BigDecimal((float)T.x,2)+", "
							  +"Z: "+round_BigDecimal((float)T.z,2)+", "
							  +"inliers: "+inliersPercent+"%, "
							  +"(Chart Type Y/s)";
					chartInfoPanel.addListData(listData);
				}
			}

		/**End of Current Frame Processing Cycle**/			
				
		}
		
    	
    	/**After Processing completion:**/
    	
    	/*Input Source Closing*/
		switch(inputSource){
		case InputParameters.VIDEO_INPUT://If Video input is selected:
			parameters.getProcessingParameters().getVideo().close(); //Closes Video file
			break;
		case InputParameters.DEVICE_INPUT://If Device input is selected: (theoretically if we have chosen Device 
							   //we never come here, but for safety we try to stop capture and clear buffer here)
			if(!parameters.getDeviceParameters().isStoppedCaptureFlag())//If Capture isn't stopped
					parameters.getDeviceParameters().setStopCaptureFlag(true);//Stops capture (Sets StopCapture Flag to true)				
			if(buffer.size()>0) //If Buffer isn't empty
					buffer.clear(); //Clears Buffer
			if(storedParameters.getInputParameters().getDevicePath().equalsIgnoreCase("WebcamCapture (BoofCv integrated)")){
				parameters.getDeviceParameters().getWebcam().close();
			}
			break;
		}

    	
		//Adds an end point to the XZ and Y Chart (that indicates current Chart end)
		chartXZPanel.addEndPoint();
		chartYPanel.addEndPoint();

		//Adds an end chart entry to the Info Panel Points List
		chartInfoPanel.addListData("End Chart "+chartXZPanel.getChartsCount());
		
		//Updates Status Label (to Processing completed.)
		chartInfoPanel.lbl_status.setText("<html><b>Status: </b>Processing completed.</html>");
		
		//If processing is successfully terminated returns true
		return true;
	}
	
	
	/**
	 * resizeAndRepositionVideoFrames
	 * - Prepares the size, position and title of Input Video Frame and Output Video Frame
	*/
	@SuppressWarnings("unchecked")
	private void resizeAndRepositionVideoFrames(Parameters storedParameters) {

		//Extracts Input/Output Video panels and frames from guiComponent
		JPanel inputVideoPanel = (JPanel)parameters.getGuiComponents().get("inputVideoPanel");
		JFrame inputVideoFrame = (JFrame)parameters.getGuiComponents().get("inputVideoFrame");
		JPanel outputVideoPanel = (JPanel)parameters.getGuiComponents().get("outputVideoPanel");
		JFrame outputVideoFrame = (JFrame)parameters.getGuiComponents().get("outputVideoFrame");
		
		//Extracts the first input image to process from Parameters (original, dynamic structure)
		while(parameters.getProcessingParameters().getLeftImg() == null){ //Wait until first image is set
			try {Thread.sleep(1);} catch (InterruptedException e) {}
		}
		I leftImg = (I)parameters.getProcessingParameters().getLeftImg();

		//Extracts other parameters for resize from storedParameters (copied, static structure)
		boolean isFullResolutionPreview = storedParameters.getChartOutputParameters().isFullResolutionPreview();
		boolean isImageKeepOriginal = storedParameters.getInternalImageParameters().isImageKeepOriginal();
		int imageResizeWidth = storedParameters.getInternalImageParameters().getImageResizeWidth();
		int imageResizeHeight = storedParameters.getInternalImageParameters().getImageResizeHeight();
	

		//Close Input/Output Video Frames
		inputVideoFrame.dispose();
		outputVideoFrame.dispose();

		//Sets Input and Output Video Frames dimensions
		Dimension inputVideoFrameDimension = (
				isFullResolutionPreview?
						new Dimension(leftImg.getWidth(), leftImg.getHeight()):
						new Dimension(400, 400));
		
		Dimension outputVideoFrameDimension = (
				isFullResolutionPreview?
						(isImageKeepOriginal?
								new Dimension(leftImg.getWidth(),leftImg.getHeight()):
								new Dimension(imageResizeWidth, imageResizeHeight))
						:new Dimension(400,400));
		
		//Resizes Input/Output Video Frames
		inputVideoPanel.setPreferredSize(inputVideoFrameDimension);
		outputVideoPanel.setPreferredSize(outputVideoFrameDimension);

		//Repositions Input/Output Video Frames
		inputVideoFrame.setLocationRelativeTo(null);
		inputVideoFrame.setLocation((UIGenerator.getFrameDefaultDimension().width*2)+65, 0);
		outputVideoFrame.setLocationRelativeTo(null);
		outputVideoFrame.setLocation((UIGenerator.getFrameDefaultDimension().width*2)+65, (UIGenerator.getFrameDefaultDimension().height/2));

		//Sets Title of Input/Output Video Frames
		inputVideoFrame.setTitle("Video Input: "+leftImg.getWidth()+"x"+leftImg.getHeight()
							   +" / Preview: "+inputVideoFrameDimension.width+"x"+inputVideoFrameDimension.height
							   +(isFullResolutionPreview?" (full resolution)":""));
		
		outputVideoFrame.setTitle("VO Processing: "+(isImageKeepOriginal?
														leftImg.getWidth()+"x"+leftImg.getHeight()
													   :imageResizeWidth+"x"+imageResizeHeight+" (resized)")
						    +" / Preview: "+outputVideoFrameDimension.width+"x"+outputVideoFrameDimension.height
						    +(isFullResolutionPreview?" (full resolution)":""));

		//Refresh Input/Output Video Frames (and applies actually the modified settings)
		inputVideoFrame.pack();
		outputVideoFrame.pack();
	}

	
	/**
	 * processStereoVisualOdometry
	 * - Not implemented
	 */
	private boolean processStereoVisualOdometry(StereoVisualOdometry<I> stereoVisualOdometry, Parameters storedParameters){
		
		//Returns false, not implemented
		return false;
	}
	
	
	/**
	 * processDepthVisualOdometry
	 * - Not implemented
	 */
	private boolean processDepthVisualOdometry(DepthVisualOdometry<I, Depth> depthVisualOdometry, Parameters storedParameters){

		//Returns false, not implemented
		return false;
	}
	
	
	/**
	 * Extracts an array of integer from strings formatted this way: 1,2,3,4 (comma separated numbers)
	 * @param string
	 * @return int[ ]
	 */
	private int[] extract_IntArray(String string){
				
		int[]	result=null;
				
		String stringpart=string;
				
		while(!stringpart.equalsIgnoreCase("")){
			int firstcomma = stringpart.indexOf(",");
			if(firstcomma!=-1){
				try{
					int value = Integer.parseInt(stringpart.substring(0,firstcomma).trim());
					if(result==null){
						result = new int[1];
						result[0] = value;
					}else{
						int[] tmp = result;
						result = new int[tmp.length+1];
						System.arraycopy(tmp, 0, result, 0, tmp.length);
						result[result.length-1] = value;
					}
					if(stringpart.length()==firstcomma+1) return null;
					stringpart = stringpart.substring(firstcomma+1,stringpart.length());
				}catch(Exception e){
					return null;
				}
			}else{
				try{
					int value = Integer.parseInt(stringpart.trim());
					if(result==null){
						result = new int[1];
						result[0] = value;
					}else{
						int[] tmp = result;
						result = new int[tmp.length+1];
						System.arraycopy(tmp, 0, result, 0, tmp.length);
						result[result.length-1] = value;
					}
					stringpart = "";
				}catch(Exception e){
				return null;
				}
			}
					
		}
				
		return result;
	}

	public static BufferedImage internalCopy(ImageSingleBand leftImg){
		BufferedImage destination = new BufferedImage ( leftImg.getWidth(), leftImg.getHeight(), BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = destination.createGraphics();
		g.setColor( new Color ( 0, 0, 0, 0 ));
		g.fillRect(0, 0, leftImg.getWidth(), leftImg.getHeight());
		g.dispose();
		ConvertBufferedImage.convertTo(leftImg,destination,true);
		return destination;
	}
	
	public static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	public static BufferedImage resizeBufferedImage(BufferedImage img, int newW, int newH) {  
	    if(img!=null){
			int w = img.getWidth();  
		    int h = img.getHeight();  
		    BufferedImage dimg = new BufferedImage(newW, newH, img.getType()==0 ? 5 : img.getType());  
		    Graphics2D g = dimg.createGraphics();  
		    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		    RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
		    g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
		    g.dispose();  
		    return dimg;
	    }else{
	    	return new BufferedImage(newW, newH, 5);
	    }
	}  
	
	/**
	 * If the algorithm implements AccessPointTracks3D, then count the number of inlier features
	 * and return a string.
	 */
	public static String inlierPercent(VisualOdometry<?> alg) {
		if(!(alg instanceof AccessPointTracks3D))
			return "";

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		int count = 0;
		int N = access.getAllTracks().size();
		for( int i = 0; i < N; i++ ) {
			if( access.isInlier(i) )
				count++;
		}
		
		return String.format("%%%5.3f", 100.0 * count / N);
	}

	public static int countInliers(VisualOdometry<?> alg) {
		if(!(alg instanceof AccessPointTracks3D))
			return 0;

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		int count = 0;
		int N = access.getAllTracks().size();
		for( int i = 0; i < N; i++ ) {
			if( access.isInlier(i) )
				count++;
		}
		
		return count;
	}
	
	public static int countNewTracks(VisualOdometry<?> alg) {
		if(!(alg instanceof AccessPointTracks3D))
			return 0;

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		int count = 0;
		int N = access.getAllTracks().size();
		for( int i = 0; i < N; i++ ) {
			if( access.isNew(i) )
				count++;
		}
		
		return count;
	}

	public static int countTotalTracks(VisualOdometry<?> alg) {
		if(!(alg instanceof AccessPointTracks3D))
			return 0;

		AccessPointTracks3D access = (AccessPointTracks3D)alg;
		
		return access.getAllTracks().size();
		
	}
	
	
	public static float round(float d, int decimalPlace) { //float return type hides decimal zeros (ex. 2.30 will be shown as 2.3), to preserve decimal zeros return type must be BigDecimal itself
		try{
			BigDecimal bd = new BigDecimal(Float.toString(d));
			bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
			return bd.floatValue();
		}catch(Exception exc){
			return 0;
		}
	}
	
	public static BigDecimal round_BigDecimal(float d, int decimalPlace) { 
		BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd;
	}

	
	public Parameters getParameters() {
		return parameters;
	}


	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	

	private final class BufferMonitorRunnable implements Runnable {

		//Extracts Images Buffer from Parameters
		final ArrayList<BufferedImage> buffer = parameters.getDeviceParameters().getBuffer();

		//Extracts needed components from guiComponents
		final InfoScrollPane chartInfoPanel = (InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel");

		@Override
		public void run() {
			
			/*Buffer Monitor Thread Begin*/
			chartInfoPanel.setBufferInfoVisible(true); //Sets Buffer Info visible
			
			while(!parameters.getDeviceParameters().isStoppedCaptureFlag() || 
				   parameters.getProcessingFlags().isProcessingVisualOdometry()){
				
				//Sleeps for 10 milliseconds
				try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
				//If buffer is not initialized repeats the cycle until it's initialized (or until capture/processing are terminated)
				if(buffer==null) continue;

				
				/*Update Buffer ProgressBar*/
				
				//Sets Buffer ProgressBar minimum value to 0:
				if(chartInfoPanel.progress_buffer_load.getMinimum()!=0) 
									chartInfoPanel.progress_buffer_load.setMinimum(0);
				
				//Sets Buffer ProgressBar maximum value:
				if(parameters.getInternalImageParameters().getImageBufferSize() //If we have Infinite Buffer
						== InternalImageParameters.INFINITEBUFFER){
					
					//Sets maximum value to 10000
					if(chartInfoPanel.progress_buffer_load.getMaximum()!=10000)//(3000 image elements in buffer is Java Heap Space limit)
									chartInfoPanel.progress_buffer_load.setMaximum(10000);
				}else{ //If we have limited Buffer
					
					//Sets maximum value to Image Buffer size
					if(chartInfoPanel.progress_buffer_load.getMaximum()
							!=parameters.getInternalImageParameters().getImageBufferSize())
						chartInfoPanel.progress_buffer_load.setMaximum(parameters.getInternalImageParameters().getImageBufferSize());
				}
				
				//Sets Buffer ProgressBar current value to Buffer size
				chartInfoPanel.progress_buffer_load.setValue(buffer.size());
				
				//Sets Buffer ProgressBar Load Percent String
				int loadPercent = (chartInfoPanel.progress_buffer_load.getValue()*100)/chartInfoPanel.progress_buffer_load.getMaximum();
				chartInfoPanel.progress_buffer_load.setString(loadPercent+"%");
				chartInfoPanel.progress_buffer_load.setStringPainted(true);


				
				/*Update Buffer Info Label*/
				
				if(buffer.size()==0){ //If Buffer is empty (occupied space == 0)
					
					//Sets Buffer Info Label to "0/maxBufferSize" (Buffer Underrun)
					//(if maxBufferSize is INFINITE, maxBufferSize is Inf. else is the specified size)
					chartInfoPanel.lbl_buffer_load.setText("<html>"+buffer.size()+"/"
							+(parameters.getInternalImageParameters().getImageBufferSize()
									==InternalImageParameters.INFINITEBUFFER?
											"Inf."
										    :parameters.getInternalImageParameters().getImageBufferSize())
							+" <b>Buffer Underrun</b></html>");
					
				}else if(buffer.size()>0 //If Buffer isn't empty but has still free space ( 0 < occupied space < maxBufferSize )   
						&& buffer.size()<parameters.getInternalImageParameters().getImageBufferSize()){
					
					//Sets Buffer Info Label to "buffer.size/maxBufferSize"
					//(if maxBufferSize is INFINITE, maxBufferSize is Inf. else is the specified size,
					//if INFINITE buffer constant is 0 it never comes here)
					chartInfoPanel.lbl_buffer_load.setText("<html>"+buffer.size()+"/"
							+(parameters.getInternalImageParameters().getImageBufferSize()
									==InternalImageParameters.INFINITEBUFFER?
											"Inf."
										    :parameters.getInternalImageParameters().getImageBufferSize())
							+"</html>");
					
				}else if(buffer.size()>0 //If Buffer isn't empty and is full ( occupied space >= maxBufferSize ) 
						&& buffer.size()>= parameters.getInternalImageParameters().getImageBufferSize()){
					
					//If maxBufferSize is INFINITE sets Buffer Info Label to "buffer.size/Inf."
					//else sets Buffer Info Label to "buffer.size/maxBufferSize" (Buffer Overrun)
					//(if INFINITE buffer constant is 0 it comes directly here)
					chartInfoPanel.lbl_buffer_load.setText("<html>"+buffer.size()+"/"
							+(parameters.getInternalImageParameters().getImageBufferSize()
									==InternalImageParameters.INFINITEBUFFER?
											"Inf."
										    :parameters.getInternalImageParameters().getImageBufferSize()+" <b>Buffer Overrun</b>")
							+"</html>");
				}
									
				//String infinite = Character.valueOf('\u221e').toString();
			}
			
			/*At the end of the Buffer Monitor Thread (when Capture and Processing are terminated)*/
			chartInfoPanel.setBufferInfoVisible(false); //Sets Buffer Info not visible
		}
	}

	private final class CustomDeviceCallback implements VideoCallBack<I> {
		
		//Images Buffer
		final ArrayList<BufferedImage> buffer;
		
		//Needed GUI components
		final ImagePanel inputVideoPanel;
		final JFrame inputVideoFrame;
		final JLabel lblInputFpsAverage;
		final JLabel lblInputFpsCurrent;
		
		//FPS Calculation Parameters
		int totalFrames;
		long startTime;
		long currentTime;
		BigDecimal totalSeconds;
		float averageFps;
		long partialTime;
		int currentFps;

		
		private CustomDeviceCallback(){
			
			//Extracts Buffer from Parameters
			this.buffer = parameters.getDeviceParameters().getBuffer();
			
			//Extracts needed components from guiComponents
			this.inputVideoPanel = (ImagePanel)parameters.getGuiComponents().get("inputVideoPanel");
			this.inputVideoFrame = (JFrame)parameters.getGuiComponents().get("inputVideoFrame");
			this.lblInputFpsAverage = ((InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel")).lbl_input_fps_average;
			this.lblInputFpsCurrent = ((InfoScrollPane)parameters.getGuiComponents().get("chartInfoPanel")).lbl_input_fps_current;

		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void init(int width, int height, ImageType<I> selImageType) {
		
			//Sets Stop Capture flag to false
			parameters.getDeviceParameters().setStopCaptureFlag(false);
			//Sets isStopped Capture flag to false
			parameters.getDeviceParameters().setStoppedCaptureFlag(false);
			
			//If Image Buffer isn't empty, clears it
			if(buffer.size()>0)buffer.clear();

			//Creates the first input device image (as first image to process)
			parameters.getProcessingParameters().setLeftImg(selImageType.createImage(width, height)); 

			//Resets FPS calculation parameters
			this.totalFrames = 0;
			this.startTime = 0;
			this.currentTime = 0;
			this.totalSeconds = new BigDecimal(0);
			this.averageFps = 0;
			this.partialTime = 0;
			this.currentFps = 0;

		}

		@Override
		public void nextFrame(I left, Object sourceData, long timeStamp) {
			
			//Gets a captured image from the Device
			BufferedImage capturedImage = (BufferedImage)sourceData;
			
			
			
			/*Buffer filling*/
			
			//Extracts Buffer size from Parameters (can be changed runtime)
			int imageBufferSize = parameters.getInternalImageParameters().getImageBufferSize();
			
			//Depending on Buffer size, Fill Buffer (triggering Buffer load info update via BufferMonitor)
			if(imageBufferSize == InternalImageParameters.INFINITEBUFFER){ //If we have Infinite Buffer
				
				//Always adds captured image to buffer 
				buffer.add(deepCopy(capturedImage));
				
			}else if(imageBufferSize>0 && buffer.size()<imageBufferSize){ //If we have limited Buffer
				
				//Adds captured image only if Buffer has free space available
				buffer.add(deepCopy(capturedImage));
			}

			
			
			/*Show image in Input Video Frame (only if input preview is enabled)*/
			
			if(parameters.getInputParameters().isInputPreviewEnabled()){ //If input preview is enabled (can be changed at runtime)
				
				if(!inputVideoFrame.isVisible())inputVideoFrame.setVisible(true);//Sets input video frame visible
				inputVideoPanel.setBufferedImage(capturedImage);//Sets capturedImage as background
				inputVideoPanel.repaint();//Repaint the panel
				
			}else{//If input preview is disabled
				if(inputVideoFrame.isVisible())inputVideoFrame.setVisible(false);//Sets input video frame not visible
			}

			
			
			/*Update average and current Device FPS info*/
			
			//At the first frame sets StartTime(for average fps estimation) 
			//and PartialTime(for current fps estimation) to System current time in milliseconds
			if(totalFrames==0){startTime = partialTime = System.currentTimeMillis();} 
			
			//For each frame sets CurrentTime to System current time in milliseconds
			currentTime = System.currentTimeMillis();
			
			//For each frame calculates total seconds elapsed from the start of the capture
			//(currentTime-startTime)/1000
			totalSeconds = round_BigDecimal((float)(currentTime-startTime)/1000f,1);

			//For each frame increases frame count
			totalFrames++;
			
			//Calculates and Show Average FPS Rate:
			try{averageFps = round((totalFrames/totalSeconds.floatValue()),2);}catch(Exception e){averageFps = 0;}			 
			lblInputFpsAverage.setText("<html><b>Average FPS:</b> "+averageFps+ " fps\n");
			
			//Calculates and Show Current FPS Rate:
			if(currentTime-partialTime>=1000){//When one second is elapsed since the last PartialTime sampling
				partialTime=System.currentTimeMillis(); //Samples PartialTime again (to System current time)
				currentFps++; //Increases currentFps counter (to include the current captured frame)
				//Shows Current FPS in the Info Panel
				lblInputFpsCurrent.setText("<html><b>Current FPS:</b> "+currentFps+ " fps\n");
				currentFps=0; //Resets currentFps counter		
			}else{
				//If one second isn't elapsed yet
				currentFps++; //Increases currentFps counter (to include the current captured frame)
			}

		}

		@Override
		public boolean stopRequested() {

			if(parameters.getDeviceParameters().isStopCaptureFlag()){
				parameters.getDeviceParameters().setStopCaptureFlag(false);
				return true;
			}else{
				return false;
			}
			
		}

		@Override
		public void stopped() {
			if(!parameters.getProcessingFlags().isProcessingVisualOdometry() 
					&& buffer.size()>0)buffer.clear();
			parameters.getDeviceParameters().setStoppedCaptureFlag(true);
		}
	}	
	
	
	public static class OSValidator {

		private static String OS = System.getProperty("os.name").toLowerCase();

		
		public static boolean isWindows() {

			return (OS.indexOf("win") >= 0);

		}

		public static boolean isMac() {

			return (OS.indexOf("mac") >= 0);

		}

		public static boolean isUnix() {

			return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
			
		}

		public static boolean isSolaris() {

			return (OS.indexOf("sunos") >= 0);

		}
	}
}


	//Old Code parts//

	
	//Main related:
	/*	 
	  System.out.println("Working Directory = " +
	  System.getProperty("user.dir")); 
	*/



	//ProcessMonoVisualOdometry related
	/*
	  //OLD PROCESS System.out.println
	
	  //DURING CYCLE (EACH FRAME/CONTINUOUS PRINT)
	  System.out.printf("Location %8.2f %8.2f %8.2f      inliers %s\n", T.x, T.y, T.z, inlierPercent(mono_vo));
	  //DURING CYCLE (EACH SECOND PRINT THIS)    	
	  System.out.printf("Current Framerate: " + current_fps + " fps\n");
	  System.out.printf("\nAverage Framerate: " + numframe/((current_time-start_time)/1000) + " fps\n");
	
	  //AFTER CYCLE PRINTS
	  float elapsed_seconds = round((float)(System.currentTimeMillis()-start_time)/1000,1);
	  float elapsed_minutes =round((float)(System.currentTimeMillis()-start_time)/1000/60,2);
	  System.out.printf("\n\nElapsed Time: "+ elapsed_seconds +" sec (="+ elapsed_minutes +" min)");
	  System.out.printf("\n\nTotal frames: "+ numframe);
	  System.out.printf("\n\nMedium framerate: "+ numframe/elapsed_seconds + " fps");
	*/



	//CustomDeviceCallback (VideoCallBack) related
	/* OLD TECHNIQUE TO SELECT IMGTYPE INTO DEVICE CALLBACK, CREATING A BARE NEW IMAGETYPE.
	 *  
	 * imgType.equals(ImageUInt8.class)?new ImageType<I>(ImageType.Family.SINGLE_BAND, ImageDataType.U8,1):
	 *	  new ImageType<I>(ImageType.Family.SINGLE_BAND, ImageDataType.F32,1)
	 */


	//Core related
	/*
	 * Directional/Orientation Panel implementation with radius and tangent:
	 */ 
/*			
			double rotationM = 0;	 //Angular coefficient of the line with rotationAngle angulation
			double vectorRadius = 0; //Y Axis coordinate to display, that describes the angle radius orientation (radius)
			double vectorTangent = 0;//X Axis coordinate to display, that describes the angle size (tangent)
			
			//Calculates coordinates to display:
			if(Math.abs(rotationAngle)>=0 && Math.abs(rotationAngle)<=Math.PI/2){ //First Quadrant (0°/90° or 0°/-90°)
				rotationM = Math.tan(rotationAngle); //Tangent of 0°/90° or 0°/-90°
				vectorRadius = 1; //(Y axis) = We are in the upper-left or upper-right quadrant of the panel, 
								  //so the radius is positive 
				vectorTangent = -rotationM; //(X axis) = rotationM goes from 0 to Infinity or from 0 to -Infinity, 
										    //so to display the angle in the correct direction (counter-clockwise or 
										    //clockwise), the vectorTangent must be inverted
			}else if(Math.abs(rotationAngle)>Math.PI/2 && Math.abs(rotationAngle)<=Math.PI){ //Second Quadrant 
																							 //(90°/180° or -90°/-180°)
				rotationM = rotationAngle>=0?
								Math.tan(rotationAngle-Math.PI)  //Tangent of (-90°/0°) if rotationAngle>=0 
																 //(counter-clockwise rotation)
							   :Math.tan(rotationAngle+Math.PI); //Tangent of (90°/0°) if rotationAngle<0 
							   									 //(clockwise rotation)
				vectorRadius = -1; //(Y Axis) = We are in the bottom-left or bottom-right quadrant of the panel, 
								   //so the radius it's negative
				vectorTangent = rotationM; //(X Axis) = rotationM goes from -Infinity to 0 or from Infinity to 0, 
										   //so to display the angle in the correct direction (counter-clockwise 
										   //or clockwise), the vectorTangent is good
			}else if(Math.abs(rotationAngle)>Math.PI && Math.abs(rotationAngle)<=(3*Math.PI/2)){//Third Quadrant 
																								//(180°/270° or -180°/-270°)
				rotationM = rotationAngle>=0?
								Math.tan(rotationAngle-Math.PI)  //Tangent of (0°/90°) if rotationAngle>=0 
																 //(counter-clockwise rotation)
							   :Math.tan(rotationAngle+Math.PI); //Tangent of (0°/-90°) if rotationAngle<0 
							   									 //(clockwise rotation)
				vectorRadius = -1; //(Y Axis) = We are in the bottom-right or bottom-left quadrant of the panel, 
								   //so the radius is negative
				vectorTangent = rotationM; //(X Axis) = rotationM goes from 0 to Infinity or from 0 to -Infinity, 
										   //so to display the angle in the correct direction (counter-clockwise 
										   //or clockwise), the vectorTangent is good
			}else if(Math.abs(rotationAngle)>(3*Math.PI/2) && Math.abs(rotationAngle)<(2*Math.PI)){ //Fourth Quadrant 
																									//(270°/360° or -270°/-360°)
				rotationM = rotationAngle>=0?
								Math.tan(rotationAngle-2*Math.PI) //Tangent of (-90°/0°) if rotationAngle>=0 
																  //(counter-clockwise rotation)
							   :Math.tan(rotationAngle+2*Math.PI);//Tangent of (90°/0°) if rotationAngle<0 
							   									  //(clockwise rotation)
				vectorRadius = 1; //(Y Axis) = We are in the upper-right or upper-left quadrant of the panel, 
								  //so the radius is positive
				vectorTangent = -rotationM; //(X Axis) = rotationM goes from -Infinity to 0 or from Infinity to 0, 
											//so to display the angle in the correct direction (counter-clockwise 
											//or clockwise), the vectorTangent must be inverted
			}
	*/		

