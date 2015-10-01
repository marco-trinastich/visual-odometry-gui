package vogui.parameters;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class ChartOutputParameters implements Serializable {

	/** Chart/Output Parameters (Chart Settings Panel, Input Settings Panel and 
	 * 	Internal Image Settings panel modify these variables)
	 * 
	 */
	
	private String							chartType;					//Modified by Chart Settings Panel
	
	//chartType Constants
	public static final String				YFRAMES_CHART = "y/frames";
	public static final String				YSECONDS_CHART = "y/seconds";
	
	private LinkedHashMap<String, String> 	chartTypeNames;				//Key is chartType, Value is displayed chartName

	private double							chartXZ_Scale;					
	private double							chartY_Scale;					
	private boolean							fullResolutionPreview;		//Modified by Input Settings Panel
	private boolean							internalImagePreview;		//Modified by Internal Image Settings Panel
	
	private static final long 				serialVersionUID = -5618734014617231940L;
		
	public ChartOutputParameters(){	//Sets default values
		
		this.setChartType(YFRAMES_CHART);				//Default Chart Type set to YFRAMES_CHART
		this.setChartTypeNames(defaultChartTypeNames());// YFRAMES_CHART -> X/Z (translation) and Y/frames (altitude per frame), 
														// YSECONDS_CHART-> X/Z (translation) and Y/seconds (altitude per second)
		this.setChartXZ_Scale(1.0);					//1:1 Scale for X/Z Chart
		this.setChartY_Scale(1.0);					//1:1 Scale for Y Chart
		this.setFullResolutionPreview(false);		//No Full Resolution Preview for Input/Output
		this.setInternalImagePreview(false);		//No Internal Image Preview for Output (boofCv processed image)
		
	}

	public ChartOutputParameters(String chartType, LinkedHashMap<String, String> chartTypeNames, double chartXZ_Scale, 
		   double chartY_Scale, boolean fullResolutionPreview, boolean internalImagePreview){
	
		//Custom initialization
		this.setChartType(chartType);
		this.setChartTypeNames(chartTypeNames);
		this.setChartXZ_Scale(chartXZ_Scale);
		this.setChartY_Scale(chartY_Scale);
		this.setFullResolutionPreview(fullResolutionPreview);
		this.setInternalImagePreview(internalImagePreview);
		
	}
	
	/**
	 * Copy constructor
	 */
	public ChartOutputParameters(ChartOutputParameters anotherChartOutputParameters){

		this(anotherChartOutputParameters.getChartType(), anotherChartOutputParameters.getChartTypeNames(), 
			 anotherChartOutputParameters.getChartXZ_Scale(), anotherChartOutputParameters.getChartY_Scale(), 
			 anotherChartOutputParameters.isFullResolutionPreview(), anotherChartOutputParameters.isInternalImagePreview());
		
	}
	
	public LinkedHashMap<String, String> defaultChartTypeNames(){
		
		LinkedHashMap<String, String> defChartTypeNames = new LinkedHashMap<String, String>();
		
		defChartTypeNames.put(YFRAMES_CHART, "X/Z (translation) and Y/frames (altitude per frame)");
		defChartTypeNames.put(YSECONDS_CHART, "X/Z (translation) and Y/seconds (altitude per second)");
		
		return defChartTypeNames;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public LinkedHashMap<String, String> getChartTypeNames() {
		return chartTypeNames;
	}

	public void setChartTypeNames(LinkedHashMap<String, String> chartTypeNames) {
		this.chartTypeNames = chartTypeNames;
	}

	public double getChartXZ_Scale() {
		return chartXZ_Scale;
	}

	public void setChartXZ_Scale(double chartXZ_Scale) {
		this.chartXZ_Scale = chartXZ_Scale;
	}

	public double getChartY_Scale() {
		return chartY_Scale;
	}

	public void setChartY_Scale(double chartY_Scale) {
		this.chartY_Scale = chartY_Scale;
	}

	public boolean isFullResolutionPreview() {
		return fullResolutionPreview;
	}

	public void setFullResolutionPreview(boolean fullResolutionPreview) {
		this.fullResolutionPreview = fullResolutionPreview;
	}

	public boolean isInternalImagePreview() {
		return internalImagePreview;
	}

	public void setInternalImagePreview(boolean internalImagePreview) {
		this.internalImagePreview = internalImagePreview;
	}
	
}
