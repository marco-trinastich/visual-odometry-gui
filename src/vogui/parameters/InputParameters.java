package vogui.parameters;

import java.io.Serializable;

public class InputParameters implements Serializable {

	/** Input Parameters (Input Settings Panel modifies these variables)
	 * 
	 */
	
	private String					calibrationPath;
	private String[]				calibrationsList;
	
	private String					inputSource;
	//inputSource Constants
	public static final String 		VIDEO_INPUT = "video";
	public static final String 		DEVICE_INPUT = "device";
	
	private String					videoPath;
	private String[]				videoPathsList;
	private String					devicePath;
	private String[]				devicePathsList;
	private int						deviceWidth;
	private int						deviceHeight;
	private boolean					device_Control_SustainFramerate_Enabled;
	private boolean					device_Control_TimeoutImageIO_Enabled;
	private boolean					device_Control_KeepFormat_Enabled;
	private boolean					inputPreviewEnabled;

	private static final long 		serialVersionUID = 1944908631405789497L;
	
	public InputParameters(){ //Sets default values
		
		this.setCalibrationPath("data/boofcv/applet/vo/drc/mono_plane.xml");				//Default calibration path (Boofcv example mono calibration)
		this.setCalibrationsList(new String[]{"data/boofcv/applet/vo/drc/mono_plane.xml",	//Default calibrations list (Some example calibration)
											  "data/vogui/monocalibrations/intrinsic_Asus_N56VZ_Webcam_1280_720.xml",
											  "data/vogui/monocalibrations/intrinsic_Microsoft_Kinect_640_480.xml",
											  "data/vogui/monocalibrations/intrinsic_Microsoft_Kinect_640_480_LowBaseheight.xml",
											  "data/vogui/monocalibrations/intrinsic_Samsung_GalaxyS2_Camera_320_240.xml",
											  "data/vogui/monocalibrations/intrinsic_Samsung_GalaxyS2_Camera_320_240_LowBaseheight.xml"});	
		this.setInputSource(VIDEO_INPUT);												//Video File as default Input Source
		this.setVideoPath("data/boofcv/applet/vo/drc/left.mjpeg");						//Default video file path (Boofcv example mono video) 
		this.setVideoPathsList(new String[]{"data/boofcv/applet/vo/drc/left.mjpeg", 	//Default Video Paths List
											"data/vogui/media/Smartspace/Smartspace_00_Inside_1280x720.avi",
											"data/vogui/media/Smartspace/Smartspace_01_Inside_1280x720.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_02_Inside_640x480.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_03_Inside_400x400.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_04_Inside_Medium_Pitch_0_1_640x480.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_05_Inside_Medium_Pitch_0_1_640x480.mp4",
											"data/vogui/media/Smartspace/Smartspace_06_Inside_Reverse_Medium_Pitch_0_2_640x480.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_07_Inside_Reverse_(20140411_134424)_640x480.mp4",
											"data/vogui/media/Smartspace/Smartspace_08_Inside_High_Pitch_(20140409)_1280x720.mp4",
											"data/vogui/media/Smartspace/Smartspace_09_Outside_640x480.mp4",
											"data/vogui/media/Smartspace/Smartspace_10_Outside_400x400.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_11_Inside_Prova_1280x720_Full_Frames.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_12_Inside_Prova_1280x720_Half_Frames.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_13_Inside_Prova_1280x720_Quarter_Frames.mjpeg",
											"data/vogui/media/Smartspace/Smartspace_14_Inside_Prova_640x360_Full_Frames.mjpeg",
											"data/vogui/media/Smartspace/Inside_132_frames_1280x720/",
											"data/vogui/media/Smartspace/Inside_456_frames_1280x720/",
											"data/vogui/media/Smartspace/Inside_660_frames_1280x720/",
											"data/vogui/media/LibViso/LibViso_01_Sample_Video_1344x372.avi",
											"data/vogui/media/LibViso/LibViso_02_Sample_Video_400x400.mjpeg",
											"data/vogui/media/LibViso/2010_03_09_drive_0019/"});	
		this.setDevicePath("/dev/video0 (V4L4J)");									//Default Device Path
		this.setDevicePathsList(new String[]{"/dev/video0 (V4L4J)",					//Default Device Paths List
											 "/dev/video1 (V4L4J)",
											 "WebcamCapture (BoofCv integrated)"});
		this.setDeviceWidth(320);														//Default Device Width = 320px
		this.setDeviceHeight(240);														//Default Device Height = 240 px
		this.setDevice_Control_SustainFramerate_Enabled(false);							//Sustain Framerate Device Control disabled by default
		this.setDevice_Control_TimeoutImageIO_Enabled(false);							//Timeout Image IO Device Control disabled by default
		this.setDevice_Control_KeepFormat_Enabled(false);								//Keep Format Device Control disabled by default
		this.setInputPreviewEnabled(true);												//Input Preview enabled by default
		
	}
	
	public InputParameters(String calibrationPath, String inputSource, String videoPath, String devicePath, 
		   int deviceWidth, int deviceHeight, boolean device_Control_SustainFramerate_Enabled, 
		   boolean device_Control_TimeoutImageIO_Enabled, boolean device_Control_KeepFormat_Enabled, 
		   boolean inputPreviewEnabled){ 

		//Custom initialization
		this.setCalibrationPath(calibrationPath);
		this.setCalibrationsList(new String[]{calibrationPath});
		this.setInputSource(inputSource);
		this.setVideoPath(videoPath);
		this.setVideoPathsList(new String[]{videoPath});
		this.setDevicePath(devicePath);
		this.setDevicePathsList(new String[]{devicePath});
		this.setDeviceWidth(deviceWidth);
		this.setDeviceHeight(deviceHeight);
		this.setDevice_Control_SustainFramerate_Enabled(device_Control_SustainFramerate_Enabled);
		this.setDevice_Control_TimeoutImageIO_Enabled(device_Control_TimeoutImageIO_Enabled);
		this.setDevice_Control_KeepFormat_Enabled(device_Control_KeepFormat_Enabled);
		this.setInputPreviewEnabled(inputPreviewEnabled);
		
	}
	
	/**
	 * Copy constructor
	 */
	public InputParameters(InputParameters anotherInputParameters){
		
		this(anotherInputParameters.getCalibrationPath(), anotherInputParameters.getInputSource(), 
			 anotherInputParameters.getVideoPath(), anotherInputParameters.getDevicePath(), 
			 anotherInputParameters.getDeviceWidth(), anotherInputParameters.getDeviceHeight(), 
			 anotherInputParameters.isDevice_Control_SustainFramerate_Enabled(), 
			 anotherInputParameters.isDevice_Control_TimeoutImageIO_Enabled(), 
			 anotherInputParameters.isDevice_Control_KeepFormat_Enabled(), 
			 anotherInputParameters.isInputPreviewEnabled());
		
		this.setCalibrationsList(anotherInputParameters.getCalibrationsList());
		this.setVideoPathsList(anotherInputParameters.getVideoPathsList());
		this.setDevicePathsList(anotherInputParameters.getDevicePathsList());
		
	}

	public String getCalibrationPath() {
		return calibrationPath;
	}

	public void setCalibrationPath(String calibrationPath) {
		this.calibrationPath = calibrationPath;
	}

	public String[] getCalibrationsList() {
		return calibrationsList;
	}

	public void setCalibrationsList(String[] calibrationsList) {
		this.calibrationsList = calibrationsList;
	}

	public String getInputSource() {
		return inputSource;
	}

	public void setInputSource(String inputSource) {
		this.inputSource = inputSource;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String[] getVideoPathsList() {
		return videoPathsList;
	}

	public void setVideoPathsList(String[] videoPathsList) {
		this.videoPathsList = videoPathsList;
	}

	public String getDevicePath() {
		return devicePath;
	}

	public void setDevicePath(String devicePath) {
		this.devicePath = devicePath;
	}

	public String[] getDevicePathsList() {
		return devicePathsList;
	}

	public void setDevicePathsList(String[] devicePathsList) {
		this.devicePathsList = devicePathsList;
	}

	public int getDeviceWidth() {
		return deviceWidth;
	}

	public void setDeviceWidth(int deviceWidth) {
		this.deviceWidth = deviceWidth;
	}

	public int getDeviceHeight() {
		return deviceHeight;
	}

	public void setDeviceHeight(int deviceHeight) {
		this.deviceHeight = deviceHeight;
	}

	public boolean isDevice_Control_SustainFramerate_Enabled() {
		return device_Control_SustainFramerate_Enabled;
	}

	public void setDevice_Control_SustainFramerate_Enabled(boolean device_Control_SustainFramerate_Enabled) {
		this.device_Control_SustainFramerate_Enabled = device_Control_SustainFramerate_Enabled;
	}

	public boolean isDevice_Control_TimeoutImageIO_Enabled() {
		return device_Control_TimeoutImageIO_Enabled;
	}

	public void setDevice_Control_TimeoutImageIO_Enabled(boolean device_Control_TimeoutImageIO_Enabled) {
		this.device_Control_TimeoutImageIO_Enabled = device_Control_TimeoutImageIO_Enabled;
	}

	public boolean isDevice_Control_KeepFormat_Enabled() {
		return device_Control_KeepFormat_Enabled;
	}

	public void setDevice_Control_KeepFormat_Enabled(boolean device_Control_KeepFormat_Enabled) {
		this.device_Control_KeepFormat_Enabled = device_Control_KeepFormat_Enabled;
	}

	public boolean isInputPreviewEnabled() {
		return inputPreviewEnabled;
	}

	public void setInputPreviewEnabled(boolean inputPreviewEnabled) {
		this.inputPreviewEnabled = inputPreviewEnabled;
	}

}
