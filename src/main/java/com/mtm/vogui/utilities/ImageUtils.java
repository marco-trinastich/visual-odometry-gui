package com.mtm.vogui.utilities;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.*;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import lombok.SneakyThrows;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ImageUtils {

    // Image resources getters

    public static BufferedImage getResourceImage(String imageRes) {
        return getResourceImage(imageRes, null, null);
    }

    public static BufferedImage getResourceImage(String imageRes, Integer width, Float alpha) {
        return getResourceImage(imageRes, width, null, alpha);
    }

    @SneakyThrows
    public static BufferedImage getResourceImage(String imageRes, Integer width, Integer height, Float alpha) {
        BufferedImage image = null;
        try (InputStream imageStream = CoreUtils.class.getResourceAsStream(imageRes)) {
            if (imageStream != null) {
                // Load image
                image = ImageIO.read(imageStream);

                // Apply alpha
                if (alpha != null && alpha < 1.0) {
                    image = modifyBufferedImageAlpha(image, alpha);
                }

                // Scale
                if (width != null) {
                    image = resizeBufferedImage(image, width, height);
                }
            }
        }

        return image;
    }


    // Image converters

    /**
     * BoofCv ImageBase to Buffered Image converter
     *
     * @param source source BoofCv ImageBase
     * @return converted buffered image
     */
    public static @NotNull BufferedImage getBufferedFromBoofCv(@NotNull ImageBase<?> source) {
        // Create target image
        BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, source.getWidth(), source.getHeight());
        g2.dispose();

        // Deep copy and conversion of source image
        ConvertBufferedImage.convertTo(source, image, true);
        return image;
    }

    /**
     * Buffered Image to BoofCv ImageBase converter
     *
     * @param source source buffered image
     * @return converted BoofCv ImageBase
     */
    public static @NotNull ImageBase<?> getBoofCvFromBuffered(@NotNull BufferedImage source,
                                                              Class<? extends ImageBase<?>> type) {
        // Convert source to target image
        return ConvertBufferedImage.convertFrom(source, type, true);
    }

    @SuppressWarnings("rawtypes")
    public static ImageType<? extends ImageBase> getImageType(ImageTypeDescriptor descriptor) {
        return getParametrizedImageType(descriptor);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static @Nullable ImageType<? extends ImageBase<?>> getParametrizedImageType(@NotNull ImageTypeDescriptor descriptor) {
        if (ImageGray.class.isAssignableFrom(descriptor.type()) && descriptor.bands() == 1) {
            // Grayscale image
            return ImageType.single((Class<? extends ImageGray>) descriptor.type());
        } else if (ImageInterleaved.class.isAssignableFrom(descriptor.type())) {
            // Interleaved color image (standard)
            return ImageType.il(descriptor.bands(), (Class<? extends ImageInterleaved>) descriptor.type());
        } else if (ImageGray.class.isAssignableFrom(descriptor.type()) && descriptor.bands() > 1) {
            // Planar color image (grayscale based)
            return ImageType.pl(descriptor.bands(), (Class<? extends ImageGray>) descriptor.type());
        }
        return null;
    }

    // Image copy

    /**
     * Creates a clone of the source buffered image
     *
     * @param source source buffered image
     * @return deep copied buffered image
     */
    public static @NotNull BufferedImage deepCopyBufferedImage(@NotNull BufferedImage source) {
        // Get components and copy raster
        ColorModel colorModel = source.getColorModel();
        boolean isAlphaPreMultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = source.copyData(null);

        // Build new image
        return new BufferedImage(colorModel, raster, isAlphaPreMultiplied, null);
    }


    // Image manipulation

    /**
     * Creates new buffered image resized with native jdk 2d graphics
     *
     * @param source       source buffered image
     * @param targetWidth  new width
     * @param targetHeight new height
     * @return resized buffered image
     */
    public static @NotNull BufferedImage resizeBufferedImageJdk(BufferedImage source,
                                                                int targetWidth,
                                                                int targetHeight) {
        if (source == null)
            return new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_3BYTE_BGR);

        // Create target image
        BufferedImage resizedImage = new BufferedImage(
                targetWidth,
                targetHeight,
                source.getType() != BufferedImage.TYPE_CUSTOM ? source.getType() : BufferedImage.TYPE_3BYTE_BGR
        );

        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        // Draw resized image
        Graphics2D g2 = resizedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(source, 0, 0, targetWidth, targetHeight, 0, 0, srcWidth, srcHeight, null);
        g2.dispose();

        return resizedImage;
    }

    /**
     * Creates new buffered image resized with imgscalr
     *
     * @param source       source buffered image
     * @param targetWidth  new width
     * @param targetHeight new height
     * @return resized buffered image
     */
    public static @NotNull BufferedImage resizeBufferedImage(BufferedImage source,
                                                             int targetWidth,
                                                             Integer targetHeight) {

        Scalr.Mode resizeMode = targetHeight != null ? Scalr.Mode.FIT_EXACT : Scalr.Mode.AUTOMATIC;
        targetHeight = targetHeight != null ? targetHeight : targetWidth;

        return Scalr.resize(source, Scalr.Method.AUTOMATIC, resizeMode, targetWidth, targetHeight,
                Scalr.OP_ANTIALIAS);
    }

    /**
     * Creates new buffered image with modified alpha
     *
     * @param image source buffered image
     * @param alpha wanted opacity value
     * @return new buffered image with modified alpha
     */
    public static BufferedImage modifyBufferedImageAlpha(BufferedImage image, Float alpha) {
        if (alpha != null && alpha < 1.0f) {
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.drawImage(image, null, 0, 0);
            g2.dispose();
            image = bufferedImage;
        }

        return image;
    }


    // Hi-res images

    public static @NotNull BaseMultiResolutionImage getMultiResImageFromBuffered(@NotNull BufferedImage image,
                                                                                 @NotNull List<Integer> resolutions) {
        Image[] variants = new Image[resolutions.size()];

        for (int i = 0; i < resolutions.size(); i++) {
            int resolution = resolutions.get(i);
            if (image.getWidth() != resolution) {
                variants[i] = resizeBufferedImage(image, resolution, resolution);
                //image.getScaledInstance(resolution, resolution, Image.SCALE_SMOOTH);
            } else {
                variants[i] = image;
            }
        }

        return new BaseMultiResolutionImage(variants);
    }

    public static GraphicsConfiguration getGraphicsConfiguration() {
        GraphicsConfiguration graphicsConfiguration;

        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
        graphicsConfiguration = graphicsDevice.getDefaultConfiguration();

        return graphicsConfiguration;
    }

    @SuppressWarnings("unused")
    public static VolatileImage getVolatileFromBuffered(BufferedImage image) {
        VolatileImage vImg = null;
        GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();

        if (graphicsConfiguration != null) {
            vImg = graphicsConfiguration.createCompatibleVolatileImage(image.getWidth(), image.getHeight());
            do {
                if (vImg.validate(graphicsConfiguration) == VolatileImage.IMAGE_INCOMPATIBLE) {
                    // old vImg doesn't work with new GraphicsConfig; re-create it
                    vImg = graphicsConfiguration.createCompatibleVolatileImage(image.getWidth(), image.getHeight());
                }
                Graphics2D g2 = vImg.createGraphics();
                g2.setRenderingHints(getHighQualityRenderingHints());
                g2.drawImage(image, null, 0, 0);
                g2.dispose();
            } while (vImg.contentsLost());
        }

        return vImg;
    }

    public static @NotNull RenderingHints getHighQualityRenderingHints() {
        return new RenderingHints(Map.of(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON,

                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON,

                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON,

                RenderingHints.KEY_RESOLUTION_VARIANT,
                RenderingHints.VALUE_RESOLUTION_VARIANT_SIZE_FIT,

                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY,

                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC,

                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,

                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE,

                RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY,

                RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_DISABLE));
    }

    @SuppressWarnings("unchecked")
    public static Map<RenderingHints.Key, Object> getDesktopHints() {
        return (Map<RenderingHints.Key, Object>) Toolkit.getDefaultToolkit()
                .getDesktopProperty(GuiConstants.AWT_DESKTOP_HINTS);
    }
}
