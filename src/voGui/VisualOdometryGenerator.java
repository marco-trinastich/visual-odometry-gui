package voGui;

import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.d3.DepthVisualOdometry;
import boofcv.abst.sfm.d3.MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.StereoVisualOdometry;
import boofcv.factory.sfm.FactoryVisualOdometry;
import boofcv.struct.calib.MonoPlaneParameters;
import boofcv.struct.calib.StereoParameters;
import boofcv.struct.calib.VisualDepthParameters;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageType;


@SuppressWarnings("rawtypes")
public class VisualOdometryGenerator <I extends ImageSingleBand, Depth extends ImageSingleBand>{

	private Class<I> 									imgType;
	private MonoPlaneParameters 						calibration;
	private StereoParameters 							calibration_stereo;
	private VisualDepthParameters 						calibration_depth;
	private PointTracker<I> 							tracker;
	
	
	
	
	public VisualOdometryGenerator(PointTracker<I> tracker, Class<I> imgType, Class<Depth> depthType){
		this.imgType = imgType;
		this.calibration = null;
		this.calibration_stereo = null;
		this.calibration_depth = null;
		this.tracker = tracker;
	}
	
	public VisualOdometryGenerator(MonoPlaneParameters calibration, StereoParameters calibration_stereo, VisualDepthParameters calibration_depth, PointTracker<I> tracker, Class<I> imgType, Class<Depth> depthType){
		this.imgType = imgType;
		this.calibration = calibration;
		this.calibration_stereo = calibration_stereo;
		this.calibration_depth = calibration_depth;
		this.tracker = tracker;
	}
	
	
	
	public MonocularPlaneVisualOdometry<I> create_monoPlaneInfinity(int thresholdAdd, int thresholdRetire, double inlierPixelTol, int ransacIterations){
		//MONOPLANEINFINITY (MONO VISUAL ODOMETRY) PARAMS 
		//thresholdAdd, thresholdRetire, inlierPixelTol, ransacIterations, tracker, imageType
		
		
		// declares the algorithm and returns the visual odometry		
		MonocularPlaneVisualOdometry<I> visualOdometry;
		visualOdometry = FactoryVisualOdometry.monoPlaneInfinity(thresholdAdd, thresholdRetire, inlierPixelTol, ransacIterations,   //75, 2, 1.5, 200, 
																 tracker, ImageType.single(imgType));
		//Pass in intrinsic/extrinsic calibration.  This can be changed in the future.
		visualOdometry.setCalibration(calibration);
		
		return visualOdometry;
	}
	
	
	
	public MonocularPlaneVisualOdometry<I> create_monoPlaneOverhead(){
		//MONOPLANEOVERHEAD (MONO VISUAL ODOMETRY) PARAMS 
		//monoPlaneOverhead(cellSize, maxCellsPerPixel, mapHeightFraction, inlierGroundTol, ransacIterations, thresholdRetire, absoluteMinimumTracks, respawnTrackFraction, respawnCoverageFraction, tracker, imageType)
		
		return null;
	}

	
	
	public StereoVisualOdometry<I> create_stereoDepth(){
		//STEREODEPTH PARAMS
		//FactoryVisualOdometry.stereoDepth(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations, refineIterations, doublePass, sparseDisparity, tracker, imageType)
		return null;
	}
	
	
	
	public StereoVisualOdometry<I> create_stereoDualTrackerPnP(){
		//STEREODUALTRACKERPNP
		//FactoryVisualOdometry.stereoDualTrackerPnP(thresholdAdd, thresholdRetire, inlierPixelTol, epipolarPixelTol, ransacIterations, refineIterations, trackerLeft, trackerRight, descriptor, imageType)
		return null;
	}
	
	
	
	public StereoVisualOdometry<I> create_stereoQuadPnP(){
		//STEREOQUADPNP
		//FactoryVisualOdometry.stereoQuadPnP(inlierPixelTol, epipolarPixelTol, maxDistanceF2F, maxAssociationError, ransacIterations, refineIterations, detector, imageType)
		return null;
	}
	
	
	
	public DepthVisualOdometry<I, Depth> create_depthDepthPnP(){
		//DEPTHDEPTHPNP
		//FactoryVisualOdometry.depthDepthPnP(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations, refineIterations, doublePass, sparseDepth, tracker, visualType, depthType)
		return null;
	}
		
	
	public void setMonoCalibration(MonoPlaneParameters calibration){
		this.calibration = calibration;
	}
	
	public void setStereoCalibration(StereoParameters calibration){
		this.calibration_stereo = calibration;
	}
	
	public void setDepthCalibration(VisualDepthParameters calibration){
		this.calibration_depth = calibration;
	}
	
}
