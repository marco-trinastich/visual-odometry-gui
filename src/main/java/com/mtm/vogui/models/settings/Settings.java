/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings;

import com.mtm.vogui.models.config.Config;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.enums.settings.SettingsType;
import com.mtm.vogui.models.settings.core.*;
import com.mtm.vogui.models.settings.core.chart.ChartSettings;
import com.mtm.vogui.models.settings.core.image.ImageSettings;
import com.mtm.vogui.models.settings.core.input.InputSettings;
import com.mtm.vogui.models.settings.core.tracker.TrackerSettings;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;
import com.mtm.vogui.models.settings.state.State;
import com.mtm.vogui.utilities.CommonUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

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
    private final CoreSettings core;
    private final State state;
    private final Config config;

    @Inject
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
        if (!loadFromXml()) {
            loadFromDat();
        }
    }

    public boolean loadFromXml() {
        CoreSettings parameters = null;

        // If xml settings exist, load parameters from xml file
        Path settingsXml = CommonUtils.getPath(config.settings().fileName(), SettingsType.XML.value());
        if (Files.exists(settingsXml)) {
            try {
                XStream xstream = new XStream();

                // Set xml parser permissions
                xstream.addPermission(NoTypePermission.NONE);
                xstream.addPermission(NullPermission.NULL);
                xstream.addPermission(PrimitiveTypePermission.PRIMITIVES); // allow primitive types
                xstream.allowTypesByWildcard(config.settings().allowedXmlClasses());

                // Parse xml
                ArrayList<?> loadedArray = (ArrayList<?>) xstream.fromXML(settingsXml.toFile());

                // Rebuild parameters
                parameters = new CoreSettings(
                        (InputSettings) loadedArray.get(0),
                        (ImageSettings) loadedArray.get(1),
                        (TrackerSettings) loadedArray.get(2),
                        (VisualOdometrySettings) loadedArray.get(3),
                        (ChartSettings) loadedArray.get(4)
                );
            } catch (Exception exc) {
                Log.warnf(Messages.LOAD_SETTINGS_EXCEPTION, settingsXml.getFileName().toString());
            }
        }

        return assignFrom(parameters);
    }

    public void loadFromDat() {
        CoreSettings parameters = null;

        // If serialized settings exist, load parameters from dat file
        Path settingsDat = CommonUtils.getPath(config.settings().fileName(), SettingsType.DAT.value());
        if (Files.exists(settingsDat)) {
            try {
                // Initialize ObjectInputStream
                ObjectInputStream objectInputStream =
                        new ObjectInputStream(new FileInputStream(settingsDat.toFile()));

                // Rebuild parameters
                parameters = new CoreSettings(
                        (InputSettings) objectInputStream.readObject(),
                        (ImageSettings) objectInputStream.readObject(),
                        (TrackerSettings) objectInputStream.readObject(),
                        (VisualOdometrySettings) objectInputStream.readObject(),
                        (ChartSettings) objectInputStream.readObject()
                );
                objectInputStream.close();
            } catch (Exception exc) {
                Log.warnf(Messages.LOAD_SETTINGS_EXCEPTION, settingsDat.getFileName().toString());
            }
        }

        assignFrom(parameters);
    }

    public Settings deepClone() {
        return new Settings(this);
    }

    private boolean assignFrom(CoreSettings coreSettings) {
        if (coreSettings == null)
            return false;

        this.core().input(coreSettings.input());
        this.core().image(coreSettings.image());
        this.core().tracker(coreSettings.tracker());
        this.core().visualOdometry(coreSettings.visualOdometry());
        this.core().chart(coreSettings.chart());

        return true;
    }
}
