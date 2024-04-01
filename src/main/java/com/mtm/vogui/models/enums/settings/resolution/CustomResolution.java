package com.mtm.vogui.models.enums.settings.resolution;

import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.utilities.CommonUtils;
import lombok.Builder;
import lombok.Data;
import org.javatuples.Pair;

@Data
@Builder
public class CustomResolution implements Resolution {
    private int width;
    private int height;

    public static Resolution from(String resolution) {
        Pair<Integer, Integer> components = CommonUtils.getResolutionComponents(resolution);
        int width = components != null ? components.getValue0() : 0;
        int height = components != null ? components.getValue1() : 0;
        return CustomResolution.from(width, height);
    }

    public static Resolution from(int width, int height) {
        return CustomResolution.builder()
                .width(width)
                .height(height)
                .build();
    }

    @Override
    public String value() {
        return CommonUtils.getResolution(this.width, this.height);
    }
}
