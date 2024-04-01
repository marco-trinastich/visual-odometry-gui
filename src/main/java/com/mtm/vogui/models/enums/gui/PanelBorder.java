package com.mtm.vogui.models.enums.gui;

import lombok.Getter;

@Getter
public enum PanelBorder {
    Rect("Rect"),
    Circle("Circle");

    private final String value;

    PanelBorder(String value) {
        this.value = value;
    }
}
