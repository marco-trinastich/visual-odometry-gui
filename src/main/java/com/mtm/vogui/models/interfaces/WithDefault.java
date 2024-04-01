package com.mtm.vogui.models.interfaces;

import jakarta.enterprise.inject.spi.CDI;

@SuppressWarnings("unchecked")
public interface WithDefault<T> {

    default T getDefault() {
        return (T) CDI.current().select(this.getClass()).get();
    }
}
