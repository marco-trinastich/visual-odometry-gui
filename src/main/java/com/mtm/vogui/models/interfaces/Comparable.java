package com.mtm.vogui.models.interfaces;

public interface Comparable {
    default boolean is(Object other) {
        return this.equals(other);
    }
}
