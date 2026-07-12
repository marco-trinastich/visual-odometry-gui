/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.state;

import com.mtm.vogui.core.integration.camera.BufferedCamera;
import com.mtm.vogui.models.core.concurrency.Awaitable;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.settings.SettingsType;
import jakarta.inject.Singleton;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@Singleton
public class State {
    // processing state
    private Awaitable<ProcessingState> processing;
    private Awaitable<Boolean> resetRequest;
    private Awaitable<Boolean> failedEvent;
    private BufferedCamera device;

    // settings format in use: never persisted, derived at boot from the file that loaded
    // (the format choice survives reboots implicitly, as the settings file that exists)
    private SettingsType settingsFormat;

    public State() {
        this.processing = new Awaitable<>(ProcessingState.StandBy);
        this.resetRequest = new Awaitable<>(false);
        this.failedEvent = new Awaitable<>(false);
        this.device = null;
        this.settingsFormat = SettingsType.JSON;
    }

    public State(@NotNull State state) {
        // Copy processing parameters
        this.processing = new Awaitable<>(state.processing.get());
        this.resetRequest = new Awaitable<>(state.resetRequest.get());
        this.failedEvent = new Awaitable<>(state.failedEvent.get());
        this.device = state.device;
        this.settingsFormat = state.settingsFormat;
    }
}
