/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.context.config.Config;
import com.mtm.vogui.models.context.settings.*;
import com.mtm.vogui.models.context.state.State;
import com.mtm.vogui.models.enums.settings.SettingsType;
import com.mtm.vogui.utilities.CommonUtils;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * AppContext
 * <p/>
 * The app-wide aggregate every part of this software depends on:<p/>
 * - settings: the persisted parameter tree, manipulated by the gui and used by the core
 * engine to set up a vo processing<p/>
 * - state: the shared runtime blackboard through which gui, core and devices exchange
 * messages during processing [ProcessingFlags, ProcessingParameters, DeviceParameters,
 * Images Buffer]<p/>
 * - config: the application configuration<p/>
 */

@Data
@Singleton
public class AppContext {

    // One mapper per persisted format (JSON primary, YAML alternative)
    private static final ObjectMapper JSON_MAPPER = configureMapper(JsonMapper.builder());
    private static final ObjectMapper YAML_MAPPER = configureMapper(YAMLMapper.builder());

    private final Settings settings;
    private final State state;
    private final Config config;

    @Inject
    public AppContext(State state, Config config) {
        // AppContext is the single settings bean: the pure-data settings tree is built
        // here, not resolved through CDI
        this(new Settings(), state, config);
    }

    public AppContext(Settings settings, State state, Config config) {
        this.settings = settings;
        this.state = state;
        this.config = config;

        loadFromDisk();
    }

    public AppContext(AppContext context) {
        this.settings = new Settings(context.settings());
        this.state = new State(context.state());
        this.config = context.config();
    }

    public void loadDefaults() {
        // Reset persisted parameters only
        this.settings().loadDefaults();
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
        return assignFrom(readSettings(jsonPath(), JSON_MAPPER));
    }

    public boolean loadFromYaml() {
        return assignFrom(readSettings(yamlPath(), YAML_MAPPER));
    }

    public boolean saveToJson() {
        return writeSettings(jsonPath(), JSON_MAPPER);
    }

    public boolean saveToYaml() {
        return writeSettings(yamlPath(), YAML_MAPPER);
    }

    public Path jsonPath() {
        return CommonUtils.getPath(config.settings().fileName(), SettingsType.JSON.value());
    }

    public Path yamlPath() {
        return CommonUtils.getPath(config.settings().fileName(), SettingsType.YAML.value());
    }

    private Settings readSettings(Path settingsPath, ObjectMapper mapper) {
        if (!Files.exists(settingsPath)) {
            return null;
        }

        try {
            return mapper.readValue(settingsPath.toFile(), Settings.class);
        } catch (Exception exc) {
            Log.warnf(Messages.LOAD_SETTINGS_EXCEPTION, settingsPath, exc);
            return null;
        }
    }

    private boolean writeSettings(Path settingsPath, ObjectMapper mapper) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(settingsPath.toFile(), this.settings());
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

    public AppContext deepClone() {
        return new AppContext(this);
    }

    private boolean assignFrom(Settings coreSettings) {
        if (coreSettings == null)
            return false;

        this.settings().autosave(coreSettings.autosave());
        this.settings().input(coreSettings.input());
        this.settings().image(coreSettings.image());
        this.settings().tracker(coreSettings.tracker());
        this.settings().visualOdometry(coreSettings.visualOdometry());
        this.settings().chart(coreSettings.chart());

        return true;
    }
}
