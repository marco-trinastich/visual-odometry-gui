package vogui.parameters;

import java.awt.Component;
import java.util.HashMap;

@SuppressWarnings("rawtypes")
public class Parameters {

	/** Parameters Class
	 *  All the parts of this software depends on this structure:
	 *  - All panels and components of GUI Interface modify these parameters (all visual odometry settings)
	 *  - The core(algorithm) uses these parameters to setup an elaboration
	 *  - GUI and Core exchange messages during elaboration(Pause,Stop,Clear,etc) through this class 
	 *    (ProcessingFlags, ProcessingParameters)
	 *  - GUI and Device and Core and Device exchange messages through this class (DeviceParameters, Images Buffer)
	 *  
	 */
	
	private InputParameters inputParameters;
	private InternalImageParameters internalImageParameters;
	private TrackerParameters trackerParameters;
	private VisualOdometryParameters visualOdometryParameters;
	private ChartOutputParameters chartOutputParameters;
	private ProcessingParameters processingParameters;
	private ProcessingFlags processingFlags;
	private DeviceParameters deviceParameters;
	private HashMap<String, Component> guiComponents;

	
	public Parameters(){ //Default Parameters initialization
		
		this.setInputParameters(new InputParameters());					//Initialize default InputParameters
		this.setInternalImageParameters(new InternalImageParameters()); //Initialize default InternalImageParameters
		this.setTrackerParameters(new TrackerParameters());				//Initialize default TrackerParameters
		this.setVisualOdometryParameters(new VisualOdometryParameters());//Initialize default VisualOdometryParameters
		this.setChartOutputParameters(new ChartOutputParameters());		//Initialize default ChartOutputParameters
		this.setProcessingParameters(new ProcessingParameters());
		this.setProcessingFlags(new ProcessingFlags());
		this.setDeviceParameters(new DeviceParameters());
		this.setGuiComponents(new HashMap<String, Component>());
		 
	}
	
	public Parameters(InputParameters inputParameters, InternalImageParameters internalImageParameters, 
		   TrackerParameters trackerParameters, VisualOdometryParameters visualOdometryParameters, 
		   ChartOutputParameters chartOutputParameters, ProcessingParameters processingParameters,
		   ProcessingFlags processingFlags, DeviceParameters deviceParameters, HashMap<String, Component> guiComponents){
		
		//Custom Parameters initialization
		this.setInputParameters(inputParameters);
		this.setInternalImageParameters(internalImageParameters);
		this.setTrackerParameters(trackerParameters);
		this.setVisualOdometryParameters(visualOdometryParameters);
		this.setChartOutputParameters(chartOutputParameters);
		this.setProcessingParameters(processingParameters);
		this.setProcessingFlags(processingFlags);
		this.setDeviceParameters(deviceParameters);
		this.setGuiComponents(guiComponents);
	
	}
	
	/**
	 * Copy constructor
	 */
	@SuppressWarnings("unchecked")
	public Parameters (Parameters anotherParameters){
		
		this(new InputParameters(anotherParameters.getInputParameters()), 
			 new InternalImageParameters(anotherParameters.getInternalImageParameters()), 
			 new TrackerParameters(anotherParameters.getTrackerParameters()), 
			 new VisualOdometryParameters(anotherParameters.getVisualOdometryParameters()), 
			 new ChartOutputParameters(anotherParameters.getChartOutputParameters()), 
			 new ProcessingParameters(anotherParameters.getProcessingParameters()), 
			 new ProcessingFlags(anotherParameters.getProcessingFlags()), 
			 new DeviceParameters (anotherParameters.getDeviceParameters()), 
				 anotherParameters.getGuiComponents());

	}
	
	public InputParameters getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(InputParameters inputParameters) {
		this.inputParameters = inputParameters;
	}

	public InternalImageParameters getInternalImageParameters() {
		return internalImageParameters;
	}

	public void setInternalImageParameters(InternalImageParameters internalImageParameters) {
		this.internalImageParameters = internalImageParameters;
	}

	public TrackerParameters getTrackerParameters() {
		return trackerParameters;
	}

	public void setTrackerParameters(TrackerParameters trackerParameters) {
		this.trackerParameters = trackerParameters;
	}

	public VisualOdometryParameters getVisualOdometryParameters() {
		return visualOdometryParameters;
	}

	public void setVisualOdometryParameters(VisualOdometryParameters visualOdometryParameters) {
		this.visualOdometryParameters = visualOdometryParameters;
	}

	public ChartOutputParameters getChartOutputParameters() {
		return chartOutputParameters;
	}

	public void setChartOutputParameters(ChartOutputParameters chartOutputParameters) {
		this.chartOutputParameters = chartOutputParameters;
	}

	public ProcessingParameters getProcessingParameters() {
		return processingParameters;
	}

	public void setProcessingParameters(ProcessingParameters processingParameters) {
		this.processingParameters = processingParameters;
	}

	public ProcessingFlags getProcessingFlags() {
		return processingFlags;
	}

	public void setProcessingFlags(ProcessingFlags processingFlags) {
		this.processingFlags = processingFlags;
	}

	public DeviceParameters getDeviceParameters() {
		return deviceParameters;
	}

	public void setDeviceParameters(DeviceParameters deviceParameters) {
		this.deviceParameters = deviceParameters;
	}

	public HashMap<String, Component> getGuiComponents() {
		return guiComponents;
	}

	public void setGuiComponents(HashMap<String, Component> guiComponents) {
		this.guiComponents = guiComponents;
	}

}