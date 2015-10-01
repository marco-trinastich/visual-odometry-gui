package vogui.parameters;

import java.io.Serializable;

import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.d3.VisualOdometry;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.struct.calib.*;
import boofcv.struct.image.ImageSingleBand;

@SuppressWarnings("rawtypes")
public class ProcessingParameters<I extends ImageSingleBand> implements Serializable{
	
	/** Processing Parameters (Core App manage these parameters)
	 *  
	 *	Parameters used to startup processing and during processing
	 *
	 */
	
	private SerializableMediaManager mediaManager;
	private MonoPlaneParameters 	 monoCalibration;
	private StereoParameters		 stereoCalibration;
	private VisualDepthParameters	 depthCalibration;
	private SimpleImageSequence<I> 	 video;
	private I						 leftImg;
	private PointTracker<I> 		 tracker;
	private VisualOdometry<I>		 visualOdometry;

	private static final long 		 serialVersionUID = -4211317157541124277L;

	public ProcessingParameters(){ //Default initialization

		this.setMediaManager(SerializableMediaManager.INSTANCE); //Default Media Manager (encapsulated to be serializable)
		this.setMonoCalibration(null);
		this.setStereoCalibration(null);
		this.setDepthCalibration(null);
		this.setVideo(null);
		this.setLeftImg(null);
		this.setTracker(null);
		this.setVisualOdometry(null);
		
	}
	
	public ProcessingParameters(SerializableMediaManager mediaManager, MonoPlaneParameters monoCalibration,
		   StereoParameters stereoCalibration, VisualDepthParameters depthCalibration, SimpleImageSequence<I> video,
		   I leftImg, PointTracker<I> tracker, VisualOdometry<I> visualOdometry){ 
		
		//Custom initialization
		this.setMediaManager(mediaManager); //Default Media Manager
		this.setMonoCalibration(monoCalibration);
		this.setStereoCalibration(stereoCalibration);
		this.setDepthCalibration(depthCalibration);
		this.setVideo(video);
		this.setLeftImg(leftImg);
		this.setTracker(tracker);
		this.setVisualOdometry(visualOdometry);
		
	}
	
	/**
	 * Copy constructor
	 */
	public ProcessingParameters(ProcessingParameters<I> anotherProcessingParameters){
		
		this(anotherProcessingParameters.getMediaManager(), anotherProcessingParameters.getMonoCalibration(), 
			 anotherProcessingParameters.getStereoCalibration(), anotherProcessingParameters.getDepthCalibration(), 
			 anotherProcessingParameters.getVideo(), anotherProcessingParameters.getLeftImg(), 
			 anotherProcessingParameters.getTracker(), anotherProcessingParameters.getVisualOdometry());
		
	}
	
	public SerializableMediaManager getMediaManager() {
		return mediaManager;
	}
	public void setMediaManager(SerializableMediaManager mediaManager) {
		this.mediaManager = mediaManager;
	}
	public MonoPlaneParameters getMonoCalibration() {
		return monoCalibration;
	}
	public void setMonoCalibration(MonoPlaneParameters monoCalibration) {
		this.monoCalibration = monoCalibration;
	}
	public StereoParameters getStereoCalibration() {
		return stereoCalibration;
	}
	public void setStereoCalibration(StereoParameters stereoCalibration) {
		this.stereoCalibration = stereoCalibration;
	}
	public VisualDepthParameters getDepthCalibration() {
		return depthCalibration;
	}
	public void setDepthCalibration(VisualDepthParameters depthCalibration) {
		this.depthCalibration = depthCalibration;
	}
	public SimpleImageSequence<I> getVideo() {
		return video;
	}
	public void setVideo(SimpleImageSequence<I> video) {
		this.video = video;
	}
	public I getLeftImg() {
		return leftImg;
	}
	public void setLeftImg(I leftImg) {
		this.leftImg = leftImg;
	}
	public PointTracker<I> getTracker() {
		return tracker;
	}
	public void setTracker(PointTracker<I> tracker) {
		this.tracker = tracker;
	}
	public VisualOdometry<I> getVisualOdometry() {
		return visualOdometry;
	}
	public void setVisualOdometry(VisualOdometry<I> visualOdometry) {
		this.visualOdometry = visualOdometry;
	}


	public static class SerializableMediaManager extends DefaultMediaManager implements Serializable{

		/**
		 * Serializable MediaManager implementation
		 */
		
		private static final SerializableMediaManager INSTANCE = new SerializableMediaManager();
		private static final long serialVersionUID = 1L;

	}

}
