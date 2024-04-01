package com.mtm.vogui.models.enums.settings;

import boofcv.struct.image.*;
import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

/**
 * ImageTypeDescriptor
 * <p/>
 * Image type/name/bands mappings
 **/
@Getter
public enum ImageTypeDescriptor implements WithValue, Comparable {
    GrayU8("GrayU8 (grayscale, 1-band)", GrayU8.class, 1),
    GrayF32("GrayF32 (grayscale, 1-band)", GrayF32.class, 1),
    InterleavedU8("InterleavedU8 (color, 3-bands)", InterleavedU8.class, 3),
    InterleavedF32("InterleavedF32 (color, 3-bands)", InterleavedF32.class, 3),
    PlanarU8("PlanarU8 (gray-based color, 3-bands)", GrayU8.class, 3),
    PlanarF32("PlanarF32 (gray-based color, 32bit)", GrayF32.class, 3);

    private final String id;
    private final Class<? extends ImageBase<?>> type;
    private final int bands;

    ImageTypeDescriptor(String id, Class<? extends ImageBase<?>> type, int bands) {
        this.id = id;
        this.type = type;
        this.bands = bands;
    }

    public String value() {
        return this.id;
    }
}
