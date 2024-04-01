package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum DeviceType implements WithValue, Comparable {
    BoofCv("BoofCv"),
    V4L4J("V4L4J");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }
}
