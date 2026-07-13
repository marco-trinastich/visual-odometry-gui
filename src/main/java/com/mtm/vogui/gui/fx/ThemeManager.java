/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.gui.ThemeMode;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;

/**
 * Owns the AtlantaFX theme choice for the whole FX app. The theme is a JavaFX-global user-agent
 * stylesheet ({@link Application#setUserAgentStylesheet}), so a single shared owner keeps startup
 * ({@link FxApplication}) and the Settings menu ({@code ShellController}) in agreement.
 * <p>
 * Three modes ({@link ThemeMode}): {@code AUTO} tracks the OS colour scheme live (Platform preferences
 * API, JavaFX 22+); {@code LIGHT}/{@code DARK} pin a fixed theme and ignore the OS. The choice is
 * persisted in {@link AppContext#settings()} so it survives a restart, and saved immediately on change
 * (so it sticks even with autosave-on-exit off — same rationale as the autosave toggle itself).
 * <p>
 * {@code @Unremovable}: resolved programmatically ({@code CDI.current()} in {@link FxApplication}) and
 * injected into the shell controller, so Arc would otherwise not see an injection point at build time.
 */
@ApplicationScoped
@Unremovable
public class ThemeManager {

    @Inject
    AppContext context;

    /**
     * Wires the live OS colour-scheme listener (effective only in {@code AUTO}) and applies the theme
     * persisted in settings. Call once from the FX entry point, on the FX Application Thread.
     */
    public void install() {
        Platform.getPreferences().colorSchemeProperty().addListener((_, _, _) -> {
            if (mode() == ThemeMode.AUTO) {
                apply();
            }
        });
        apply();
    }

    /** The current mode, read from persisted settings (defaults to {@code AUTO} when absent). */
    public ThemeMode mode() {
        ThemeMode mode = context.settings().theme();
        return mode == null ? ThemeMode.AUTO : mode;
    }

    /** Switches mode, applies it, and persists immediately so it survives a restart. */
    public void setMode(ThemeMode mode) {
        context.settings().theme(mode);
        apply();
        context.saveToCurrentFormat();
    }

    /** Re-applies the theme from current settings without persisting (after a settings load/reset). */
    public void refresh() {
        apply();
    }

    private void apply() {
        Application.setUserAgentStylesheet(resolveDark()
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }

    private boolean resolveDark() {
        return switch (mode()) {
            case DARK -> true;
            case LIGHT -> false;
            case AUTO -> ColorScheme.DARK == Platform.getPreferences().getColorScheme();
        };
    }
}
