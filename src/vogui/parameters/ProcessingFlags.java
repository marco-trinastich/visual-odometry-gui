package vogui.parameters;

import java.io.Serializable;

public class ProcessingFlags implements Serializable{

	/** Processing Flags (Core App and Main Frame(GUI) manage these parameters)
	 *  
	 *	Flags used during processing
	 *
	 */

	private boolean					processingVisualOdometry;
	private boolean					pauseVisualOdometry;
	private boolean					resetVisualOdometry;
	private boolean					stopVisualOdometry;
	private boolean					clearVisualOdometry;

	private static final long 		serialVersionUID = 3115050608780956354L;
	
	public ProcessingFlags(){ //Default initialization
		
		this.setProcessingVisualOdometry(false); //By default, false Processing flag 
		this.setPauseVisualOdometry(false);		 //By default, false Pause flag
		this.setResetVisualOdometry(false);		 //By default, false Reset flag
		this.setStopVisualOdometry(false);		 //By default, false Stop flag
		this.setClearVisualOdometry(false);		 //By default, false Clear flag
		
	}

	public ProcessingFlags(boolean processingVisualOdometry, boolean pauseVisualOdometry,
		   boolean resetVisualOdometry, boolean stopVisualOdometry, boolean clearVisualOdometry){ 
		
		//Custom initialization
		this.setProcessingVisualOdometry(processingVisualOdometry);  
		this.setPauseVisualOdometry(pauseVisualOdometry);		 	
		this.setResetVisualOdometry(resetVisualOdometry);		 	
		this.setStopVisualOdometry(stopVisualOdometry);		 		
		this.setClearVisualOdometry(clearVisualOdometry);		 	
		
	}
	
	/**
	 * Copy constructor
	 */
	public ProcessingFlags(ProcessingFlags anotherProcessingFlags){
		
		this(anotherProcessingFlags.isProcessingVisualOdometry(), anotherProcessingFlags.isPauseVisualOdometry(), 
			 anotherProcessingFlags.isResetVisualOdometry(), anotherProcessingFlags.isStopVisualOdometry(), 
			 anotherProcessingFlags.isClearVisualOdometry());
				
	}

	public boolean isProcessingVisualOdometry() {
		return processingVisualOdometry;
	}

	public void setProcessingVisualOdometry(boolean processingVisualOdometry) {
		this.processingVisualOdometry = processingVisualOdometry;
	}

	public boolean isPauseVisualOdometry() {
		return pauseVisualOdometry;
	}

	public void setPauseVisualOdometry(boolean pauseVisualOdometry) {
		this.pauseVisualOdometry = pauseVisualOdometry;
	}

	public boolean isResetVisualOdometry() {
		return resetVisualOdometry;
	}

	public void setResetVisualOdometry(boolean resetVisualOdometry) {
		this.resetVisualOdometry = resetVisualOdometry;
	}

	public boolean isStopVisualOdometry() {
		return stopVisualOdometry;
	}

	public void setStopVisualOdometry(boolean stopVisualOdometry) {
		this.stopVisualOdometry = stopVisualOdometry;
	}

	public boolean isClearVisualOdometry() {
		return clearVisualOdometry;
	}

	public void setClearVisualOdometry(boolean clearVisualOdometry) {
		this.clearVisualOdometry = clearVisualOdometry;
	}

}
