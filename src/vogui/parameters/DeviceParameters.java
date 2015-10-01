package vogui.parameters;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import com.github.sarxos.webcam.Webcam;

public class DeviceParameters implements Serializable {

	/** Device Parameters and Flags (Core App and Main Frame (GUI) manage these parameters)
	 *  
	 *	Parameters and flags used during capture from Camera/Device and during core processing
	 *
	 */

	private ArrayList<BufferedImage> buffer;
	private boolean 				 stopCaptureFlag;		
	private boolean					 isStoppedCaptureFlag;
	private Webcam 					 webcam; 

	private static final long 		 serialVersionUID = -6970520744542494166L;

	public DeviceParameters(){
		
		//Default initialization
		this.setBuffer(new ArrayList<BufferedImage>()); //By default initializes a new empty Buffer
		this.setStopCaptureFlag(false);					//By default sets Stop Capture Flag to false
		this.setStoppedCaptureFlag(true);				//By default sets isStopped Capture Flag to true
		this.setWebcam(null);							//By default sets webcam to null
		
	}

	public DeviceParameters(ArrayList<BufferedImage> buffer, boolean stopCaptureFlag, boolean isStoppedCaptureFlag, 
		   Webcam webcam){
		
		//Custom initialization
		this.setBuffer(buffer); 							
		this.setStopCaptureFlag(stopCaptureFlag);			
		this.setStoppedCaptureFlag(isStoppedCaptureFlag);	
		this.setWebcam(webcam);
		
	}
	
	/**
	 * Copy constructor
	 */
	public DeviceParameters(DeviceParameters anotherDeviceParameters){
		
		this(anotherDeviceParameters.getBuffer(), anotherDeviceParameters.isStopCaptureFlag(), 
			 anotherDeviceParameters.isStoppedCaptureFlag(), anotherDeviceParameters.getWebcam());
		
	}

	public ArrayList<BufferedImage> getBuffer() {
		return buffer;
	}

	public void setBuffer(ArrayList<BufferedImage> buffer) {
		this.buffer = buffer;
	}

	public boolean isStopCaptureFlag() {
		return stopCaptureFlag;
	}

	public void setStopCaptureFlag(boolean stopCaptureFlag) {
		this.stopCaptureFlag = stopCaptureFlag;
	}

	public boolean isStoppedCaptureFlag() {
		return isStoppedCaptureFlag;
	}

	public void setStoppedCaptureFlag(boolean isStoppedCaptureFlag) {
		this.isStoppedCaptureFlag = isStoppedCaptureFlag;
	}

	public Webcam getWebcam() {
		return webcam;
	}

	public void setWebcam(Webcam webcam) {
		this.webcam = webcam;
	}

}
