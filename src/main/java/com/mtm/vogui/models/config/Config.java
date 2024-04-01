package com.mtm.vogui.models.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "config")
public interface Config {
    Settings settings();

    interface Settings {
        String fileName();

        String[] allowedXmlClasses();
    }
}
