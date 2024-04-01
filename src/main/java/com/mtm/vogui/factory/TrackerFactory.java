package com.mtm.vogui.factory;


import boofcv.abst.feature.detect.interest.PointDetectorTypes;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.factory.feature.associate.ConfigAssociate;
import boofcv.factory.feature.describe.ConfigDescribeRegion;
import boofcv.factory.feature.detect.interest.ConfigDetectInterestPoint;
import boofcv.factory.feature.detect.selector.SelectLimitTypes;
import boofcv.factory.tracker.ConfigPointTracker;
import boofcv.factory.tracker.FactoryPointTracker;
import boofcv.struct.image.ImageGray;
import boofcv.struct.pyramid.ConfigDiscreteLevels;
import com.mtm.vogui.models.core.processing.tracking.Tracker;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.core.exceptions.InvalidImageFormatException;
import org.jetbrains.annotations.NotNull;

/**
 * Tracker algorithms factory
 * </p>
 *
 * @author Marco Trinastich
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TrackerFactory {
    Class<? extends ImageGray> imageType;
    Class<? extends ImageGray> derivativeType;

    private TrackerFactory(Class<? extends ImageGray> imageType) {
        this.imageType = imageType;
        this.derivativeType = GImageDerivativeOps.getDerivativeType(imageType);
    }

    /**
     * Creates default feature tracker (KLT)
     */
    public Tracker createDefault() {
        // return create_default_KLT_TwoPass();
        return createDefaultKLT();
    }

    /**
     * Creates a Kanade-Lucas-Tomasi (KLT) tracker based on a Shi-Tomasi point detector.
     */
    public Tracker createKLT(int templateRadius, int pyramidLevels, int maxFeatures, int radius, float threshold) {
        ConfigPointTracker config = new ConfigPointTracker();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.KLT;
        config.klt.templateRadius = templateRadius;
        config.klt.pyramidLevels = ConfigDiscreteLevels.levels(pyramidLevels);

        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.POINT;
        config.detDesc.detectPoint.type = PointDetectorTypes.SHI_TOMASI;
        config.detDesc.detectPoint.general.maxFeatures = maxFeatures;
        config.detDesc.detectPoint.general.radius = radius;
        config.detDesc.detectPoint.general.threshold = threshold;

        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Creates default KLT feature tracker.
     */
    public Tracker createDefaultKLT() {
        ConfigPointTracker config = new ConfigPointTracker();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.KLT;
        config.klt.templateRadius = 3;
        config.klt.pyramidLevels = ConfigDiscreteLevels.levels(4); // pyramidScaling = new int[]{1, 2, 4, 8};

        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.POINT;
        config.detDesc.detectPoint.type = PointDetectorTypes.SHI_TOMASI;
        config.detDesc.detectPoint.general.maxFeatures = 600; //200, 3, 1
        config.detDesc.detectPoint.general.radius = 3;
        config.detDesc.detectPoint.general.threshold = 1;

        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Creates a DDA feature tracker based on Fast-hessian detector, SURF fast describe and Greedy associate.
     */
    public Tracker createSURF(int maxFeaturesPerScale, int extractRadius, int initialSampleSize) {
        ConfigPointTracker config = new ConfigPointTracker();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.DDA;
        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.FAST_HESSIAN;
        config.detDesc.detectFastHessian.maxFeaturesPerScale = maxFeaturesPerScale;
        config.detDesc.detectFastHessian.extract.radius = extractRadius;
        config.detDesc.detectFastHessian.initialSize = initialSampleSize;
        // Describe
        config.detDesc.typeDescribe = ConfigDescribeRegion.Type.SURF_FAST;
        // Associate
        config.associate.type = ConfigAssociate.AssociationType.GREEDY;
        config.associate.greedy.maxErrorThreshold = 5;


        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Creates default DDA feature tracker based on Fast-hessian detector, SURF fast descriptor and Greedy association.
     */
    public Tracker createDefaultSURF() {
        ConfigPointTracker config = new ConfigPointTracker();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.DDA;
        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.FAST_HESSIAN;
        config.detDesc.detectFastHessian.maxFeaturesPerScale = 200;
        config.detDesc.detectFastHessian.extract.radius = 3;
        config.detDesc.detectFastHessian.initialSize = 2;
        // Describe
        config.detDesc.typeDescribe = ConfigDescribeRegion.Type.SURF_FAST;
        // Associate
        config.associate.type = ConfigAssociate.AssociationType.GREEDY;
        config.associate.greedy.maxErrorThreshold = 5;

        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Creates a modern KLT feature tracker based on Shi-Tomasi point detector.
     */
    public Tracker createKLTModern() {
        ConfigPointTracker config = getModernTrackerBaseConfig();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.KLT;
        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.POINT;

        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Creates a DDA feature tracker based on POINT detector, BRIEF describe and GREEDY associate.
     */
    public Tracker createDDAModern() {
        ConfigPointTracker config = getModernTrackerBaseConfig();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.DDA;
        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.POINT;
        // Describe
        config.detDesc.typeDescribe = ConfigDescribeRegion.Type.BRIEF;

        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Creates a Hybrid feature tracker based on POINT detector, SURF stable describe and GREEDY associate.
     */
    public Tracker createHybridModern() {
        ConfigPointTracker config = getModernTrackerBaseConfig();

        // Tracker
        config.typeTracker = ConfigPointTracker.TrackerType.HYBRID;
        // Detector
        config.detDesc.typeDetector = ConfigDetectInterestPoint.Type.POINT;
        // Describe
        config.detDesc.typeDescribe = ConfigDescribeRegion.Type.SURF_STABLE;

        return Tracker.from(config, FactoryPointTracker.tracker(config, imageType, derivativeType));
    }

    /**
     * Get modern trackers base config: Shi Tomasi point detector + Select N selector + Greedy association
     */
    private ConfigPointTracker getModernTrackerBaseConfig() {
        ConfigPointTracker configTracker = new ConfigPointTracker();

        configTracker.klt.templateRadius = 3;
        configTracker.klt.pyramidLevels = ConfigDiscreteLevels.levels(4);

        configTracker.detDesc.detectPoint.type = PointDetectorTypes.SHI_TOMASI;
        configTracker.detDesc.detectPoint.scaleRadius = 12;
        configTracker.detDesc.detectPoint.shiTomasi.radius = 3;
        configTracker.detDesc.detectPoint.general.threshold = 1.0f;
        configTracker.detDesc.detectPoint.general.radius = 4;
        configTracker.detDesc.detectPoint.general.maxFeatures = 400;
        configTracker.detDesc.detectPoint.general.selector.type = SelectLimitTypes.SELECT_N;

        configTracker.associate.type = ConfigAssociate.AssociationType.GREEDY;
        configTracker.associate.greedy.forwardsBackwards = true;
        configTracker.associate.greedy.scoreRatioThreshold = 0.8;

        return configTracker;
    }

    public static @NotNull TrackerFactory from(@NotNull ImageTypeDescriptor imageType)
            throws InvalidImageFormatException {
        if(!ImageGray.class.isAssignableFrom(imageType.type()))
            throw new InvalidImageFormatException();

        return new TrackerFactory((Class<? extends ImageGray>) imageType.type());
    }
}
