/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings;

import com.mtm.vogui.models.config.Config;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.enums.settings.SettingsType;
import com.mtm.vogui.models.settings.core.*;
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
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        Path settingsXml = xmlPath();
        if (Files.exists(settingsXml)) {
            try {
                parameters = (CoreSettings) buildXStream().fromXML(settingsXml.toFile());
            } catch (Exception exc) {
                Log.warnf(Messages.LOAD_SETTINGS_EXCEPTION, settingsXml, exc);
            }
        }

        return assignFrom(parameters);
    }

    public boolean loadFromDat() {
        CoreSettings parameters = null;

        // If serialized settings exist, load parameters from dat file
        Path settingsDat = datPath();
        if (Files.exists(settingsDat)) {
            try (ObjectInputStream objectInputStream =
                         new ObjectInputStream(new FileInputStream(settingsDat.toFile()))) {
                parameters = (CoreSettings) objectInputStream.readObject();
            } catch (Exception exc) {
                Log.warnf(Messages.LOAD_SETTINGS_EXCEPTION, settingsDat, exc);
            }
        }

        return assignFrom(parameters);
    }

    public boolean saveToXml() {
        Path settingsXml = xmlPath();
        try {
            Files.writeString(settingsXml, buildXStream().toXML(this.core()));
            return true;
        } catch (Exception exc) {
            Log.warnf(Messages.SAVE_SETTINGS_EXCEPTION, settingsXml, exc);
            return false;
        }
    }

    public boolean saveToDat() {
        Path settingsDat = datPath();
        try (ObjectOutputStream objectOutputStream =
                     new ObjectOutputStream(new FileOutputStream(settingsDat.toFile()))) {
            objectOutputStream.writeObject(this.core());
            return true;
        } catch (Exception exc) {
            Log.warnf(Messages.SAVE_SETTINGS_EXCEPTION, settingsDat, exc);
            return false;
        }
    }

    public Path xmlPath() {
        return CommonUtils.getPath(config.settings().fileName(), SettingsType.XML.value());
    }

    public Path datPath() {
        return CommonUtils.getPath(config.settings().fileName(), SettingsType.DAT.value());
    }

    private XStream buildXStream() {
        XStream xstream = new XStream();

        // Honor XStream annotations (e.g. @XStreamOmitField), ignored by default
        xstream.processAnnotations(CoreSettings.class);

        // Set xml parser permissions
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES); // allow primitive types
        xstream.allowTypesByWildcard(config.settings().allowedXmlClasses());

        return xstream;
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
