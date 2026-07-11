/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mtm.vogui.models.config.Config;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.enums.settings.SettingsType;
import com.mtm.vogui.models.settings.core.*;
import com.mtm.vogui.models.settings.state.State;
import com.mtm.vogui.utilities.CommonUtils;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Settings
 * <p/>
 * All the parts of this software depend on this structure:<p/>
 * - gui interface manipulates settings<p/>
 * - core engine uses settings to set up a vo processing<p/>
 * - gui, core and devices exchange messages during processing through this class [ProcessingFlags,
 * ProcessingParameters, DeviceParameters, Images Buffer]<p/>
 */

@Data
@Singleton
public class Settings {

    // One mapper per persisted format (JSON primary, YAML alternative)
    private static final ObjectMapper JSON_MAPPER = configureMapper(JsonMapper.builder());
    private static final ObjectMapper YAML_MAPPER = configureMapper(YAMLMapper.builder());

    private final CoreSettings core;
    private final State state;
    private final Config config;

    @Inject
    public Settings(State state, Config config) {
        // Settings is the single settings bean: the pure-data core tree is built here,
        // not resolved through CDI
        this(new CoreSettings(), state, config);
    }

    public Settings(CoreSettings core, State state, Config config) {
        this.core = core;
        this.state = state;
        this.config = config;

        loadFromDisk();
    }

    public Settings(Settings settings) {
        this.core = new CoreSettings(settings.core());
        this.state = new State(settings.state());
        this.config = settings.config();
    }

    public void loadDefaults() {
        // Reset core parameters only
        this.core().loadDefaults();
    }

    public void loadFromDisk() {
        // JSON wins when both formats exist on disk
        if (loadFromJson()) {
            rememberFormat(SettingsType.JSON);
        } else if (loadFromYaml()) {
            rememberFormat(SettingsType.YAML);
        }
    }

    /**
     * The settings format currently in use (JSON by default, YAML when only a
     * yaml file was found at boot or after an in-app format switch).
     */
    public SettingsType currentFormat() {
        return this.state() != null ? this.state().settingsFormat() : SettingsType.JSON;
    }

    public boolean saveToCurrentFormat() {
        return switch (currentFormat()) {
            case JSON -> saveToJson();
            case YAML -> saveToYaml();
        };
    }

    private void rememberFormat(SettingsType format) {
        if (this.state() != null) {
            this.state().settingsFormat(format);
        }
    }

    public boolean loadFromJson() {
        return assignFrom(readCore(jsonPath(), JSON_MAPPER));
    }

    public boolean loadFromYaml() {
        return assignFrom(readCore(yamlPath(), YAML_MAPPER));
    }

    public boolean saveToJson() {
        return writeCore(jsonPath(), JSON_MAPPER);
    }

    public boolean saveToYaml() {
        return writeCore(yamlPath(), YAML_MAPPER);
    }

    public Path jsonPath() {
        return CommonUtils.getPath(config.settings().fileName(), SettingsType.JSON.value());
    }

    public Path yamlPath() {
        return CommonUtils.getPath(config.settings().fileName(), SettingsType.YAML.value());
    }

    private CoreSettings readCore(Path settingsPath, ObjectMapper mapper) {
        if (!Files.exists(settingsPath)) {
            return null;
        }

        try {
            return mapper.readValue(settingsPath.toFile(), CoreSettings.class);
        } catch (Exception exc) {
            Log.warnf(Messages.LOAD_SETTINGS_EXCEPTION, settingsPath, exc);
            return null;
        }
    }

    private boolean writeCore(Path settingsPath, ObjectMapper mapper) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(settingsPath.toFile(), this.core());
            return true;
        } catch (Exception exc) {
            Log.warnf(Messages.SAVE_SETTINGS_EXCEPTION, settingsPath, exc);
            return false;
        }
    }

    private static ObjectMapper configureMapper(MapperBuilder<?, ?> builder) {
        return builder
                // Field-based access: settings expose Lombok fluent accessors, not bean getters
                .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                // Files written by other versions stay loadable: unknown fields are skipped,
                // missing fields keep the defaults built by the no-arg constructors
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // Liberal in what we accept (hand-edited "klt"/"KLT" load fine), conservative
                // in what we produce (enums are written as their CamelCase constant names)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    public Settings deepClone() {
        return new Settings(this);
    }

    private boolean assignFrom(CoreSettings coreSettings) {
        if (coreSettings == null)
            return false;

        this.core().autosave(coreSettings.autosave());
        this.core().input(coreSettings.input());
        this.core().image(coreSettings.image());
        this.core().tracker(coreSettings.tracker());
        this.core().visualOdometry(coreSettings.visualOdometry());
        this.core().chart(coreSettings.chart());

        return true;
    }
}
