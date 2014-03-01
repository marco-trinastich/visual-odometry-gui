package voGui;


import boofcv.abst.feature.associate.AssociateDescription2D;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.feature.tracker.PointTrackerTwoPass;
import boofcv.alg.feature.associate.AssociateMaxDistanceNaive;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.factory.feature.tracker.FactoryPointTrackerTwoPass;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.image.ImageSingleBand;

@SuppressWarnings("rawtypes")
public class TrackerGenerator<T extends ImageSingleBand, D extends ImageSingleBand> {

	Class<T> imageType;
	Class<D> derivType;
	
	
	
	public TrackerGenerator(Class<T> imageType){
		this.imageType = imageType;
		this.derivType = GImageDerivativeOps.getDerivativeType(imageType);
	}
	
	

	/**
	 * A simple way to create a Kanade-Lucas-Tomasi (KLT) tracker.
	 */	
	public PointTracker<T> createKLT(int templateRadius, int[] pyramidScaling, int maxFeatures, int radius, float threshold) {
		
		PkltConfig config = new PkltConfig();
		config.templateRadius = templateRadius; //3
		config.pyramidScaling = pyramidScaling; //new int[]{1,2,4,8};

		PointTracker<T> tracker = FactoryPointTracker.klt(config, new ConfigGeneralDetector(maxFeatures,radius,threshold),	//200, 3, 1
				imageType, derivType);
	
		return tracker;
	}
	
	
	public PointTrackerTwoPass<T> createKLT_TwoPass(int templateRadius, int[] pyramidScaling, int maxFeatures, int radius, float threshold) {
		
		PkltConfig config = new PkltConfig();
		config.templateRadius = templateRadius; //3
		config.pyramidScaling = pyramidScaling; //new int[]{1,2,4,8};

		PointTrackerTwoPass<T> tracker = FactoryPointTrackerTwoPass.klt(config, new ConfigGeneralDetector(maxFeatures,radius,threshold),	//200, 3, 1
				imageType, derivType);
	
		return tracker;		
	}
	
	
	public PointTracker<T> create_default_KLT() {
		
		PkltConfig config = new PkltConfig();
		config.templateRadius = 3;
		config.pyramidScaling = new int[]{1,2,4,8};

		PointTracker<T> tracker = FactoryPointTracker.klt(config, new ConfigGeneralDetector(600, 3, 1),
				imageType, derivType);
	
		return tracker;
	}
	
	
	public PointTrackerTwoPass<T> create_default_KLT_TwoPass() {
		
		PkltConfig config = new PkltConfig();
		config.templateRadius = 3;
		config.pyramidScaling = new int[]{1,2,4,8};

		PointTrackerTwoPass<T> tracker = FactoryPointTrackerTwoPass.klt(config, new ConfigGeneralDetector(600, 3, 1),
				imageType, derivType);
	
		return tracker;
	}
	

	

	/**
	 * Creates a SURF feature tracker.
	 */
	public PointTracker<T> createSURF(int maxFeaturesPerScale, int extractRadius, int initialSampleSize) {
		ConfigFastHessian configDetector = new ConfigFastHessian();
		configDetector.maxFeaturesPerScale = maxFeaturesPerScale;
		configDetector.extractRadius = extractRadius;
		configDetector.initialSampleSize = initialSampleSize;
		
		PointTracker<T> tracker = FactoryPointTracker.dda_FH_SURF_Fast(configDetector, null, null, imageType);
		return tracker;
	}
	
	public PointTrackerTwoPass<T> createSURF_TwoPass(int maxFeaturesPerScale, int extractRadius, int initialSampleSize) {
		ConfigFastHessian configDetector = new ConfigFastHessian();
		configDetector.maxFeaturesPerScale = maxFeaturesPerScale;
		configDetector.extractRadius = extractRadius;
		configDetector.initialSampleSize = initialSampleSize;
		
		
		AssociateDescription2D<SurfFeature> associate1,associate2;
		ScoreAssociation<SurfFeature> score1,score2;
		//score = FactoryAssociation.defaultScore(SurfFeature.class);
		//score = FactoryAssociation.scoreEuclidean(SurfFeature.class, true);
		//score = FactoryAssociation.scoreSad(SurfFeature.class);
		
		score1 = FactoryAssociation.defaultScore(SurfFeature.class);
		score2 = FactoryAssociation.scoreEuclidean(SurfFeature.class, true);
		
		associate1 = new AssociateMaxDistanceNaive<SurfFeature>(score1,false,Double.MAX_VALUE);
		associate2 = new AssociateMaxDistanceNaive<SurfFeature>(score2,false,Double.MAX_VALUE);		 
		
		PointTrackerTwoPass<T> tracker = FactoryPointTrackerTwoPass.dda(FactoryDetectDescribe
				.surfStable(configDetector, null, null, imageType), associate1, associate2, true);
		return tracker;
	}
	
	
	public PointTracker<T> create_default_SURF() {
		ConfigFastHessian configDetector = new ConfigFastHessian();
		configDetector.maxFeaturesPerScale = 200;
		configDetector.extractRadius = 3;
		configDetector.initialSampleSize = 2;
		
		return FactoryPointTracker.dda_FH_SURF_Fast(configDetector, null, null, imageType);
		
	}
	
	
	public PointTrackerTwoPass<T> create_default_SURF_TwoPass() {
		ConfigFastHessian configDetector = new ConfigFastHessian();
		configDetector.maxFeaturesPerScale = 200;
		configDetector.extractRadius = 3;
		configDetector.initialSampleSize = 2;
		
	
		AssociateDescription2D<SurfFeature> associate1,associate2;
		ScoreAssociation<SurfFeature> score1,score2;
		
		score1 = FactoryAssociation.defaultScore(SurfFeature.class);
		score2 = FactoryAssociation.defaultScore(SurfFeature.class);
		
		associate1 = new AssociateMaxDistanceNaive<SurfFeature>(score1,false,Double.MAX_VALUE);
		associate2 = new AssociateMaxDistanceNaive<SurfFeature>(score2,false,Double.MAX_VALUE);		 
		
		PointTrackerTwoPass<T> tracker = FactoryPointTrackerTwoPass.dda(FactoryDetectDescribe
				.surfStable(configDetector, null, null, imageType), associate1, associate2, true);
		return tracker;
	}
	
	
	
	public PointTracker<T> create_default(){
		return create_default_KLT_TwoPass();
	}
	
	
	
	//extracts an array of integer from strings formatted this way: 1,2,3,4 (comma separated numbers) [useful for pyramidScaling extraction]
	@SuppressWarnings("unused")
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
}
