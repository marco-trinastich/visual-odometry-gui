/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx;

import com.mtm.vogui.gui.fx.features.shell.ShellView;
import com.mtm.vogui.gui.fx.shared.behaviors.Editors;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import com.mtm.vogui.models.constants.AppConstants;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point, launched by {@code VisualOdometryGui} AFTER the Quarkus container is up,
 * so CDI is available here. Not a bean itself (the FX toolkit instantiates it reflectively);
 * FXML controllers ARE beans, resolved through the CDI controller factory.
 */
public class FxApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Theme (persisted mode; AUTO follows the OS colour scheme live). Shared owner so the shell's
        // Settings menu and this entry point stay in agreement.
        CDI.current().select(ThemeManager.class).get().install();

        Scene scene = new Scene(new ShellView().content());
        FxUtils.applyAppStylesheet(scene);
        stage.setScene(scene);
        stage.setTitle(AppConstants.APP_TITLE);
        FxUtils.applyAppIcon(stage);
        // Closing via the window button doesn't fire focus-lost on a focused Spinner/combo editor, so
        // a pending typed edit would miss the autosave-on-exit — commit it here, while the scene lives.
        stage.setOnCloseRequest(_ -> Editors.commitFocused(scene));
        stage.show();
    }
}
