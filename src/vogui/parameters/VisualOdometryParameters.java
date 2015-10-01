package vogui.parameters;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class VisualOdometryParameters implements Serializable{

	/** Visual Odometry Parameters (Visual Odometry Settings Panel modifies these variables)
	 *  
	 *  These parameters set the visual odometry type and options
	 *  to be supplied to the algorithm
	 */

	private String							visualOdometryType;		
	//visualOdometryType Constants
	public static final String				MONOPLANEINFINITY = "monoPlaneInfinity";
	public static final String				MONOPLANEOVERHEAD = "monoPlaneOverhead";
	public static final String				STEREODEPTH = "stereoDepth";
	public static final String				STEREODUALTRACKERPNP = "stereoDualTrackerPnP";
	public static final String				STEREOQUADPNP = "stereoQuadPnP";
	public static final String				DEPTHDEPTHPNP = "depthDepthPnP";
	public static final String				DEFAULT_VISUALODOMETRY = "default";

	private LinkedHashMap<String, String>	visualOdometryTypeNames; 		//Key is visualOdometryType, Value is displayed visualOdometryName		
	
	//monoPlaneInfinity Parameters
	private int								monoPlaneInfinity_thresholdAdd;
	private int								monoPlaneInfinity_thresholdRetire;
	private double							monoPlaneInfinity_inlierPixelTol;
	private int								monoPlaneInfinity_ransacIterations;
	
	//monoPlaneOverhead Parameters
	private double							monoPlaneOverhead_cellSize;
	private double							monoPlaneOverhead_maxCellsPerPixel;
	private double							monoPlaneOverhead_mapHeightFraction;
	private double							monoPlaneOverhead_inlierGroundTol;
	private int								monoPlaneOverhead_ransacIterations;
	private int								monoPlaneOverhead_thresholdRetire;
	private int								monoPlaneOverhead_absoluteMinimumTracks;
	private double							monoPlaneOverhead_respawnTrackFraction;
	private double							monoPlaneOverhead_respawnCoverageFraction;

	private static final long 				serialVersionUID = -2246883520249161370L;

	
	public VisualOdometryParameters(){ //Default initialization
		
		this.setVisualOdometryType(MONOPLANEINFINITY);						//Sets default VisualOdometry Type to monoPlaneInfinity
		this.setVisualOdometryTypeNames(defaultVisualOdometryTypeNames());	//Sets default VisualOdometry Type Names (monoPlaneInfinity, monoPlaneOverhead, stereoDepth (not implemented), stereoDualTrackerPnP (not implemented), stereoQuadPnP (not implemented), depthDepthPnP (not implemented), default (monoPlaneInfinity)) 
		this.setMonoPlaneInfinity_thresholdAdd(75);							//Sets default monoPlaneInfinity thresholdAdd=75
		this.setMonoPlaneInfinity_thresholdRetire(2);						//Sets default monoPlaneInfinity thresholdRetire=2
		this.setMonoPlaneInfinity_inlierPixelTol(1.5);						//Sets default monoPlaneInfinity inlierPixelTol=1.5
		this.setMonoPlaneInfinity_ransacIterations(200);					//Sets default monoPlaneInfinity ransacIterations=200
		this.setMonoPlaneOverhead_cellSize(0.06);							//Sets default monoPlaneOverhead cellSize=0.06
		this.setMonoPlaneOverhead_maxCellsPerPixel(25);						//Sets default monoPlaneOverhead maxCellsPerPixel=25
		this.setMonoPlaneOverhead_mapHeightFraction(0.7);					//Sets default monoPlaneOverhead mapHeightFraction=0.7
		this.setMonoPlaneOverhead_inlierGroundTol(1.5);						//Sets default monoPlaneOverhead inlierGroundTol=1.5
		this.setMonoPlaneOverhead_ransacIterations(300);					//Sets default monoPlaneOverhead ransacIterations=300
		this.setMonoPlaneOverhead_thresholdRetire(2);						//Sets default monoPlaneOverhead thresholdRetire=2
		this.setMonoPlaneOverhead_absoluteMinimumTracks(100);				//Sets default monoPlaneOverhead absoluteMinimumTracks=100
		this.setMonoPlaneOverhead_respawnTrackFraction(0.5);				//Sets default monoPlaneOverhead respawnTrackFraction=0.5
		this.setMonoPlaneOverhead_respawnCoverageFraction(0.6);				//Sets default monoPlaneOverhead respawnCoverageFraction=0.6
		
	}
	
	public VisualOdometryParameters(String visualOdometryType, LinkedHashMap<String, String> visualOdometryTypeNames,
		   int monoPlaneInfinity_thresholdAdd, int monoPlaneInfinity_thresholdRetire, double monoPlaneInfinity_inlierPixelTol,
		   int monoPlaneInfinity_ransacIteration, double monoPlaneOverhead_cellSize, double monoPlaneOverhead_maxCellsPerPixel,
		   double monoPlaneOverhead_mapHeightFraction, double monoPlaneOverhead_inlierGroundTol, int monoPlaneOverhead_ransacIterations,
		   int monoPlaneOverhead_thresholdRetire, int monoPlaneOverhead_absoluteMinimumTracks, double monoPlaneOverhead_respawnTrackFraction,
		   double monoPlaneOverhead_respawnCoverageFraction){
		
		//Custom initialization
		this.setVisualOdometryType(visualOdometryType);
		this.setVisualOdometryTypeNames(visualOdometryTypeNames);
		this.setMonoPlaneInfinity_thresholdAdd(monoPlaneInfinity_thresholdAdd);
		this.setMonoPlaneInfinity_thresholdRetire(monoPlaneInfinity_thresholdRetire);
		this.setMonoPlaneInfinity_inlierPixelTol(monoPlaneInfinity_inlierPixelTol);
		this.setMonoPlaneInfinity_ransacIterations(monoPlaneInfinity_ransacIteration);
		this.setMonoPlaneOverhead_cellSize(monoPlaneOverhead_cellSize);
		this.setMonoPlaneOverhead_maxCellsPerPixel(monoPlaneOverhead_maxCellsPerPixel);
		this.setMonoPlaneOverhead_mapHeightFraction(monoPlaneOverhead_mapHeightFraction);
		this.setMonoPlaneOverhead_inlierGroundTol(monoPlaneOverhead_inlierGroundTol);
		this.setMonoPlaneOverhead_ransacIterations(monoPlaneOverhead_ransacIterations);
		this.setMonoPlaneOverhead_thresholdRetire(monoPlaneOverhead_thresholdRetire);
		this.setMonoPlaneOverhead_absoluteMinimumTracks(monoPlaneOverhead_absoluteMinimumTracks);
		this.setMonoPlaneOverhead_respawnTrackFraction(monoPlaneOverhead_respawnTrackFraction);
		this.setMonoPlaneOverhead_respawnCoverageFraction(monoPlaneOverhead_respawnCoverageFraction);
		
	}
	
	/**
	 * Copy constructor
	 */
	public VisualOdometryParameters(VisualOdometryParameters anotherVisualOdometryParameters){
		
		this(anotherVisualOdometryParameters.getVisualOdometryType(), 
			 anotherVisualOdometryParameters.getVisualOdometryTypeNames(), 
			 anotherVisualOdometryParameters.getMonoPlaneInfinity_thresholdAdd(), 
			 anotherVisualOdometryParameters.getMonoPlaneInfinity_thresholdRetire(), 
			 anotherVisualOdometryParameters.getMonoPlaneInfinity_inlierPixelTol(), 
			 anotherVisualOdometryParameters.getMonoPlaneInfinity_ransacIterations(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_cellSize(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_maxCellsPerPixel(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_mapHeightFraction(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_inlierGroundTol(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_ransacIterations(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_thresholdRetire(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_absoluteMinimumTracks(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_respawnTrackFraction(), 
			 anotherVisualOdometryParameters.getMonoPlaneOverhead_respawnCoverageFraction());
		
	}

	private LinkedHashMap<String, String> defaultVisualOdometryTypeNames() {
		
		LinkedHashMap<String, String> defVisualOdometryTypeNames = new LinkedHashMap<String, String>();
		
		defVisualOdometryTypeNames.put(MONOPLANEINFINITY, "monoPlaneInfinity");
		defVisualOdometryTypeNames.put(MONOPLANEOVERHEAD, "monoPlaneOverhead");
		defVisualOdometryTypeNames.put(STEREODEPTH, "stereoDepth (not implemented)");
		defVisualOdometryTypeNames.put(STEREODUALTRACKERPNP, "stereoDualTrackerPnP (not implemented)");
		defVisualOdometryTypeNames.put(STEREOQUADPNP, "stereoQuadPnP (not implemented)");
		defVisualOdometryTypeNames.put(DEPTHDEPTHPNP, "depthDepthPnP (not implemented)");
		defVisualOdometryTypeNames.put(DEFAULT_VISUALODOMETRY, "<html><b>Default VO (monoPlaneInfinity, standard param.)</b></html>");
		
		return defVisualOdometryTypeNames;
		
	}


	public String getVisualOdometryType() {
		return visualOdometryType;
	}


	public void setVisualOdometryType(String visualOdometryType) {
		this.visualOdometryType = visualOdometryType;
	}


	public LinkedHashMap<String, String> getVisualOdometryTypeNames() {
		return visualOdometryTypeNames;
	}


	public void setVisualOdometryTypeNames(LinkedHashMap<String, String> visualOdometryTypeNames) {
		this.visualOdometryTypeNames = visualOdometryTypeNames;
	}


	public int getMonoPlaneInfinity_thresholdAdd() {
		return monoPlaneInfinity_thresholdAdd;
	}


	public void setMonoPlaneInfinity_thresholdAdd(int monoPlaneInfinity_thresholdAdd) {
		this.monoPlaneInfinity_thresholdAdd = monoPlaneInfinity_thresholdAdd;
	}


	public int getMonoPlaneInfinity_thresholdRetire() {
		return monoPlaneInfinity_thresholdRetire;
	}


	public void setMonoPlaneInfinity_thresholdRetire(int monoPlaneInfinity_thresholdRetire) {
		this.monoPlaneInfinity_thresholdRetire = monoPlaneInfinity_thresholdRetire;
	}


	public double getMonoPlaneInfinity_inlierPixelTol() {
		return monoPlaneInfinity_inlierPixelTol;
	}


	public void setMonoPlaneInfinity_inlierPixelTol(double monoPlaneInfinity_inlierPixelTol) {
		this.monoPlaneInfinity_inlierPixelTol = monoPlaneInfinity_inlierPixelTol;
	}


	public int getMonoPlaneInfinity_ransacIterations() {
		return monoPlaneInfinity_ransacIterations;
	}


	public void setMonoPlaneInfinity_ransacIterations(int monoPlaneInfinity_ransacIterations) {
		this.monoPlaneInfinity_ransacIterations = monoPlaneInfinity_ransacIterations;
	}


	public double getMonoPlaneOverhead_cellSize() {
		return monoPlaneOverhead_cellSize;
	}


	public void setMonoPlaneOverhead_cellSize(double monoPlaneOverhead_cellSize) {
		this.monoPlaneOverhead_cellSize = monoPlaneOverhead_cellSize;
	}


	public double getMonoPlaneOverhead_maxCellsPerPixel() {
		return monoPlaneOverhead_maxCellsPerPixel;
	}


	public void setMonoPlaneOverhead_maxCellsPerPixel(double monoPlaneOverhead_maxCellsPerPixel) {
		this.monoPlaneOverhead_maxCellsPerPixel = monoPlaneOverhead_maxCellsPerPixel;
	}


	public double getMonoPlaneOverhead_mapHeightFraction() {
		return monoPlaneOverhead_mapHeightFraction;
	}


	public void setMonoPlaneOverhead_mapHeightFraction(double monoPlaneOverhead_mapHeightFraction) {
		this.monoPlaneOverhead_mapHeightFraction = monoPlaneOverhead_mapHeightFraction;
	}


	public double getMonoPlaneOverhead_inlierGroundTol() {
		return monoPlaneOverhead_inlierGroundTol;
	}


	public void setMonoPlaneOverhead_inlierGroundTol(double monoPlaneOverhead_inlierGroundTol) {
		this.monoPlaneOverhead_inlierGroundTol = monoPlaneOverhead_inlierGroundTol;
	}


	public int getMonoPlaneOverhead_ransacIterations() {
		return monoPlaneOverhead_ransacIterations;
	}


	public void setMonoPlaneOverhead_ransacIterations(int monoPlaneOverhead_ransacIterations) {
		this.monoPlaneOverhead_ransacIterations = monoPlaneOverhead_ransacIterations;
	}


	public int getMonoPlaneOverhead_thresholdRetire() {
		return monoPlaneOverhead_thresholdRetire;
	}


	public void setMonoPlaneOverhead_thresholdRetire(int monoPlaneOverhead_thresholdRetire) {
		this.monoPlaneOverhead_thresholdRetire = monoPlaneOverhead_thresholdRetire;
	}


	public int getMonoPlaneOverhead_absoluteMinimumTracks() {
		return monoPlaneOverhead_absoluteMinimumTracks;
	}


	public void setMonoPlaneOverhead_absoluteMinimumTracks(int monoPlaneOverhead_absoluteMinimumTracks) {
		this.monoPlaneOverhead_absoluteMinimumTracks = monoPlaneOverhead_absoluteMinimumTracks;
	}


	public double getMonoPlaneOverhead_respawnTrackFraction() {
		return monoPlaneOverhead_respawnTrackFraction;
	}


	public void setMonoPlaneOverhead_respawnTrackFraction(double monoPlaneOverhead_respawnTrackFraction) {
		this.monoPlaneOverhead_respawnTrackFraction = monoPlaneOverhead_respawnTrackFraction;
	}


	public double getMonoPlaneOverhead_respawnCoverageFraction() {
		return monoPlaneOverhead_respawnCoverageFraction;
	}


	public void setMonoPlaneOverhead_respawnCoverageFraction(double monoPlaneOverhead_respawnCoverageFraction) {
		this.monoPlaneOverhead_respawnCoverageFraction = monoPlaneOverhead_respawnCoverageFraction;
	}


	
}
