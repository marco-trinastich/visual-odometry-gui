package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum SettingsType implements WithValue, Comparable {
    XML("xml"),
    YAML("yaml"),
    DAT("dat");

    private final String value;

    SettingsType(String value) {
        this.value = value;
    }
}
