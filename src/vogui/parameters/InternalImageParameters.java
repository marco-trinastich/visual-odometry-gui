package vogui.parameters;

import java.io.Serializable;

import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;


@SuppressWarnings("rawtypes")
public class InternalImageParameters implements Serializable{

	/** Internal Image Parameters (Internal Image Settings Panel modifies these variables)
	 *  
	 *  These parameters set the image format, size and options
	 *  of the images to be supplied to the algorithm
	 */
	
	private ImageTypeDescriptor		imageType;
	
	//imageType Constants
	public static final ImageTypeDescriptor IMAGEUINT8 =  ImageTypeDescriptor.generate(ImageUInt8.class);
	public static final ImageTypeDescriptor IMAGEFLOAT32 = ImageTypeDescriptor.generate(ImageFloat32.class);

	private ImageTypeDescriptor[]	imageTypesList;		//Array of supported imageTypeDescriptors to display and use
														//(Each imageTypeDescriptor contains display name and image type class)
	
	private boolean					imageKeepOriginal;
	private int						imageResizeWidth;
	private int						imageResizeHeight;
	
	private int						imageBufferSize;
	
	//imageBufferSize Constants
	public static final int			INFINITEBUFFER = 0; 

	private boolean					frameDecimateEnabled;
	private int						frameDecimateValue;

	private static final long 		serialVersionUID = 1021885581219652670L;
	
	public InternalImageParameters(){ //Sets default values
		
		this.setImageType(IMAGEFLOAT32);					//Default image type = ImageFloat32
		this.setImageTypesList( 							//Default image types list (ImageUInt8 and ImageFloat32)
				new ImageTypeDescriptor[]{IMAGEUINT8, IMAGEFLOAT32});
		this.setImageKeepOriginal(true);					//Default processed image size: Keep Original
		this.setImageResizeWidth(400);						//Default processed image resize width: 400
		this.setImageResizeHeight(400);						//Default processed image resize height: 400
		this.setImageBufferSize(INFINITEBUFFER);			//Default acquired images buffer size: Infinite buffer = 0
		this.setFrameDecimateEnabled(false);				//Acquired frames decimate (frame skip) disabled
		this.setFrameDecimateValue(1);						//Acquired frames decimate (frame skip) value = 1 (no frameskip)
	}
	
	public InternalImageParameters(ImageTypeDescriptor imageType, ImageTypeDescriptor[] imageTypesList,
		   boolean imageKeepOriginal, int imageResizeWidth, int imageResizeHeight, int imageBufferSize, 
		   boolean frameDecimateEnabled, int frameDecimateValue){ 

		//Custom initialization
		this.setImageType(imageType);
		this.setImageTypesList(imageTypesList);
		this.setImageKeepOriginal(imageKeepOriginal);
		this.setImageResizeWidth(imageResizeWidth);
		this.setImageResizeHeight(imageResizeHeight);
		this.setImageBufferSize(imageBufferSize);
		this.setFrameDecimateEnabled(frameDecimateEnabled);
		this.setFrameDecimateValue(frameDecimateValue);
		
	}
	
	/**
	 * Copy constructor
	 */
	public InternalImageParameters(InternalImageParameters anotherInternalImageParameters){

		this(anotherInternalImageParameters.getImageType(), anotherInternalImageParameters.getImageTypesList(), 
			 anotherInternalImageParameters.isImageKeepOriginal(), anotherInternalImageParameters.getImageResizeWidth(), 
			 anotherInternalImageParameters.getImageResizeHeight(), anotherInternalImageParameters.getImageBufferSize(), 
			 anotherInternalImageParameters.isFrameDecimateEnabled(), anotherInternalImageParameters.getFrameDecimateValue());
	
	}

	public ImageTypeDescriptor getImageType() {
		return imageType;
	}

	public void setImageType(ImageTypeDescriptor imageType) {
		this.imageType = imageType;
	}

	public ImageTypeDescriptor[] getImageTypesList() {
		return imageTypesList;
	}

	public void setImageTypesList(ImageTypeDescriptor[] imageTypesList) {
		this.imageTypesList = imageTypesList;
	}
	
	public boolean isImageKeepOriginal() {
		return imageKeepOriginal;
	}

	public void setImageKeepOriginal(boolean imageKeepOriginal) {
		this.imageKeepOriginal = imageKeepOriginal;
	}

	public int getImageResizeWidth() {
		return imageResizeWidth;
	}

	public void setImageResizeWidth(int imageResizeWidth) {
		this.imageResizeWidth = imageResizeWidth;
	}

	public int getImageResizeHeight() {
		return imageResizeHeight;
	}

	public void setImageResizeHeight(int imageResizeHeight) {
		this.imageResizeHeight = imageResizeHeight;
	}

	public int getImageBufferSize() {
		return imageBufferSize;
	}

	public void setImageBufferSize(int imageBufferSize) {
		this.imageBufferSize = imageBufferSize;
	}

	public boolean isFrameDecimateEnabled() {
		return frameDecimateEnabled;
	}

	public void setFrameDecimateEnabled(boolean imageDecimateEnabled) {
		this.frameDecimateEnabled = imageDecimateEnabled;
	}

	public int getFrameDecimateValue() {
		return frameDecimateValue;
	}

	public void setFrameDecimateValue(int imageDecimateValue) {
		this.frameDecimateValue = imageDecimateValue;
	}


	/**
	 * Custom Sub-Type: ImageTypeDescriptor
	 * 
	 *  This nested class simply associate a name string to a generic ImageType Class,
	 *  so that we can eventually identify which display name the Class has
	 *  
	 **/
	public static class ImageTypeDescriptor<T extends ImageSingleBand> implements Serializable{
		
		private String imageTypeName;
		private Class<T> imageTypeClass;

		private static final long serialVersionUID = -3685717697813650055L;
		
		public ImageTypeDescriptor(String imageTypeName, Class<T> imageTypeClass){
			
			this.imageTypeName = imageTypeName;
			this.imageTypeClass = imageTypeClass;

		}

		public String getImageTypeName() {
			return imageTypeName;			
		}

		public void setImageTypeName(String imageTypeName) {
			this.imageTypeName = imageTypeName;
		}

		public Class<T> getImageTypeClass() {
			return imageTypeClass;
		}

		public void setImageTypeClass(Class<T> imageTypeClass) {
			this.imageTypeClass = imageTypeClass;
		}
		
		public static ImageTypeDescriptor generate(Class imageTypeClass){
			
			//If imageTypeClass doesn't extend ImageSingleBand return null
			if(!ImageSingleBand.class.isAssignableFrom(imageTypeClass)) return null;
			
			//Else generates an ImageTypeDescriptor
			if(imageTypeClass.equals(ImageUInt8.class)){
				return new ImageTypeDescriptor<ImageUInt8>("ImageUInt8 (8bit Int Unsigned)", (Class<ImageUInt8>) ImageUInt8.class);
			}else if(imageTypeClass.equals(ImageFloat32.class)){
				return new ImageTypeDescriptor<ImageFloat32>("ImageFloat32 (32bit Float)", (Class<ImageFloat32>) ImageFloat32.class);
			}else{
				return null;
			}
		}	
	}
}
