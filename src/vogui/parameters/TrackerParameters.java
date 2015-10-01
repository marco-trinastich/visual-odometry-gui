package vogui.parameters;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class TrackerParameters implements Serializable{

	/** Tracker Parameters (Tracker Settings Panel modifies these variables)
	 *  
	 *  These parameters set the tracker type and tracker options
	 *  to be supplied to the algorithm
	 */

	private String							trackerType;
	//trackerType Constants
	public static final String				KLT = "klt";
	public static final String				KLT2 = "klt2";
	public static final String				SURF = "surf";
	public static final String				SURF2 = "surf2";
	public static final String				DEFAULT_TRACKER = "default"; //KLT 2-pass Tracker
	
	private LinkedHashMap<String, String> 	trackerTypeNames; //Key is trackerType, Value is displayed trackerName		
	
	//KLT Parameters
	private int								kltTracker_templateRadius;
	private String							kltTracker_pyramidScaling;
	private int								kltTracker_maxFeatures;
	private int								kltTracker_radius;
	private float							kltTracker_threshold;
	
	//SURF Parameters
	private int								surfTracker_maxFeaturesPerScale;
	private int								surfTracker_extractRadius;
	private int								surfTracker_initialSampleSize;
	
	private boolean							trackerShowActiveTracks;
	private boolean							trackerShowNewTracks;

	private static final long 				serialVersionUID = -4351575446151780166L;

	
	public TrackerParameters(){//Default values
		
		this.setTrackerType(KLT);							//Sets default tracker type to KLT
		this.setTrackerTypeNames(defaultTrackerTypeNames());//Sets default tracker types names (KLT,KLT2,SURF,SURF2,Default(KLT2))
		this.setKltTracker_templateRadius(3);				//Sets default klt templateRadius=3
		this.setKltTracker_pyramidScaling("1,2,4,8");		//Sets default klt pyramidScaling=[1,2,4,8]
		this.setKltTracker_maxFeatures(600);				//Sets default klt maxFeatures=600 (Also 200 is a good value)
		this.setKltTracker_radius(3);						//Sets default klt radius=3
		this.setKltTracker_threshold(1.00f);				//Sets default klt threshold=1.00(float)
		this.setSurfTracker_maxFeaturesPerScale(200);		//Sets default surf maxFeaturesPerScale=200
		this.setSurfTracker_extractRadius(3);				//Sets default surf extractRadius=3
		this.setSurfTracker_initialSampleSize(2);			//Sets default surf initialSampleSize=2
		this.setTrackerShowActiveTracks(true);				//Tracker Show Active Tracks enabled by default
		this.setTrackerShowNewTracks(false);				//Tracker Show New Tracks disabled by default
		
	}
	
	public TrackerParameters(String trackerType, LinkedHashMap<String, String> trackerTypeNames, 
		   int kltTracker_templateRadius, String kltTracker_pyramidScaling, int kltTracker_maxFeatures,
		   int kltTracker_radius, float kltTracker_threshold, int surfTracker_maxFeaturesPerScale,
		   int surfTracker_extractRadius, int surfTracker_initialSampleSize, boolean trackerShowActiveTracks,
		   boolean trackerShowNewTracks){
		
		//Custom initialization
		this.setTrackerType(trackerType);
		this.setTrackerTypeNames(trackerTypeNames);
		this.setKltTracker_templateRadius(kltTracker_templateRadius);
		this.setKltTracker_pyramidScaling(kltTracker_pyramidScaling);
		this.setKltTracker_maxFeatures(kltTracker_maxFeatures);
		this.setKltTracker_radius(kltTracker_radius);
		this.setKltTracker_threshold(kltTracker_threshold);
		this.setSurfTracker_maxFeaturesPerScale(surfTracker_maxFeaturesPerScale);
		this.setSurfTracker_extractRadius(surfTracker_extractRadius);
		this.setSurfTracker_initialSampleSize(surfTracker_initialSampleSize);
		this.setTrackerShowActiveTracks(trackerShowActiveTracks);
		this.setTrackerShowNewTracks(trackerShowNewTracks);
		
	}
	
	/**
	 * Copy constructor
	 */
	public TrackerParameters(TrackerParameters anotherTrackerParameters){
		
		this(anotherTrackerParameters.getTrackerType(), anotherTrackerParameters.getTrackerTypeNames(), 
			 anotherTrackerParameters.getKltTracker_templateRadius(), anotherTrackerParameters.getKltTracker_pyramidScaling(), 
			 anotherTrackerParameters.getKltTracker_maxFeatures(), anotherTrackerParameters.getKltTracker_radius(), 
			 anotherTrackerParameters.getKltTracker_threshold(), anotherTrackerParameters.getSurfTracker_maxFeaturesPerScale(), 
			 anotherTrackerParameters.getSurfTracker_extractRadius(), anotherTrackerParameters.getSurfTracker_initialSampleSize(), 
			 anotherTrackerParameters.isTrackerShowActiveTracks(), anotherTrackerParameters.isTrackerShowNewTracks());
		
	}
	
	public LinkedHashMap<String, String> defaultTrackerTypeNames(){
		
		LinkedHashMap<String, String> defTrackerTypeNames = new LinkedHashMap<String, String>();
		
		defTrackerTypeNames.put(KLT, "KLT (Standard)");
		defTrackerTypeNames.put(KLT2, "KLT (Two Pass)");
		defTrackerTypeNames.put(SURF, "Surf (Standard)");
		defTrackerTypeNames.put(SURF2, "Surf (Dda Two Pass)");
		defTrackerTypeNames.put(DEFAULT_TRACKER, "<html><b>Default Tracker (KLT-2P, standard param.)</b></html>");
		
		return defTrackerTypeNames;
		
	}
	
	public String getTrackerType() {
		return trackerType;
	}

	public void setTrackerType(String trackerType) {
		this.trackerType = trackerType;
	}

	public LinkedHashMap<String, String> getTrackerTypeNames() {
		return trackerTypeNames;
	}


	public void setTrackerTypeNames(LinkedHashMap<String, String> trackerTypeNames) {
		this.trackerTypeNames = trackerTypeNames;
	}

	public int getKltTracker_templateRadius() {
		return kltTracker_templateRadius;
	}

	public void setKltTracker_templateRadius(int kltTracker_templateRadius) {
		this.kltTracker_templateRadius = kltTracker_templateRadius;
	}

	public String getKltTracker_pyramidScaling() {
		return kltTracker_pyramidScaling;
	}

	public void setKltTracker_pyramidScaling(String kltTracker_pyramidScaling) {
		this.kltTracker_pyramidScaling = kltTracker_pyramidScaling;
	}

	public int getKltTracker_maxFeatures() {
		return kltTracker_maxFeatures;
	}

	public void setKltTracker_maxFeatures(int kltTracker_maxFeatures) {
		this.kltTracker_maxFeatures = kltTracker_maxFeatures;
	}

	public int getKltTracker_radius() {
		return kltTracker_radius;
	}

	public void setKltTracker_radius(int kltTracker_radius) {
		this.kltTracker_radius = kltTracker_radius;
	}

	public float getKltTracker_threshold() {
		return kltTracker_threshold;
	}

	public void setKltTracker_threshold(float kltTracker_threshold) {
		this.kltTracker_threshold = kltTracker_threshold;
	}

	public int getSurfTracker_maxFeaturesPerScale() {
		return surfTracker_maxFeaturesPerScale;
	}

	public void setSurfTracker_maxFeaturesPerScale(int surfTracker_maxFeaturesPerScale) {
		this.surfTracker_maxFeaturesPerScale = surfTracker_maxFeaturesPerScale;
	}

	public int getSurfTracker_extractRadius() {
		return surfTracker_extractRadius;
	}

	public void setSurfTracker_extractRadius(int surfTracker_extractRadius) {
		this.surfTracker_extractRadius = surfTracker_extractRadius;
	}

	public int getSurfTracker_initialSampleSize() {
		return surfTracker_initialSampleSize;
	}

	public void setSurfTracker_initialSampleSize(int surfTracker_initialSampleSize) {
		this.surfTracker_initialSampleSize = surfTracker_initialSampleSize;
	}

	public boolean isTrackerShowActiveTracks() {
		return trackerShowActiveTracks;
	}

	public void setTrackerShowActiveTracks(boolean trackerShowActiveTracks) {
		this.trackerShowActiveTracks = trackerShowActiveTracks;
	}

	public boolean isTrackerShowNewTracks() {
		return trackerShowNewTracks;
	}

	public void setTrackerShowNewTracks(boolean trackerShowNewTracks) {
		this.trackerShowNewTracks = trackerShowNewTracks;
	}

	
}
