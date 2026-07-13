/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.shell;

import com.mtm.vogui.gui.fx.utils.FxUtils;
import javafx.scene.layout.Region;

import java.io.IOException;

/**
 * Shell feature facade (entrypoint), uniform with every other feature's {@code XxxView}: loads the
 * main-shell FXML and exposes its {@link #root()} so {@code FxApplication} mounts the shell exactly
 * like a feature. The shell is the root feature — its job is to compose the child feature facades
 * (settings, video, ...) and host the app-level menu/toolbar/status. The MVVM binding lives in the
 * internal {@link ShellController}; app-level dialogs ({@link AboutDialog}) are feature-internal.
 */
public class ShellView {

    private static final String SHELL_FXML = "/gui/fx/features/shell/shell.fxml";

    private final Region content;

    public ShellView() {
        try {
            this.content = FxUtils.newFxmlLoader(SHELL_FXML).load();
        } catch (IOException e) {
            // A missing/broken shell FXML is a fatal wiring error: fail fast (never a swallowed catch).
            throw new IllegalStateException("Failed to load shell FXML", e);
        }
    }

    /**
     * The shell root, for {@code FxApplication} to place in the {@code Scene}. Same
     * {@code content()} contract as every other feature facade (the shell being the root feature):
     * the shell root is a {@code BorderPane}, i.e. a {@link Region}.
     */
    public Region content() {
        return content;
    }
}
