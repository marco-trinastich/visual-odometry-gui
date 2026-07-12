/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import com.mtm.vogui.models.constants.AppConstants;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX entry point, launched by {@code VisualOdometryGui} AFTER the Quarkus container is up,
 * so CDI is available here. Not a bean itself (the FX toolkit instantiates it reflectively);
 * FXML controllers ARE beans, resolved through the CDI controller factory.
 */
public class FxApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Theme following the OS color scheme, live (Platform preferences API, JavaFX 22+)
        var preferences = Platform.getPreferences();
        applyTheme(preferences.getColorScheme());
        preferences.colorSchemeProperty().addListener((_, _, scheme) -> applyTheme(scheme));

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("/gui/fx/shell/main-shell.fxml")));
        loader.setControllerFactory(type -> CDI.current().select(type).get());
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.setTitle(AppConstants.APP_TITLE);
        FxUtils.applyAppIcon(stage);
        stage.show();
    }

    private static void applyTheme(ColorScheme scheme) {
        Application.setUserAgentStylesheet(ColorScheme.DARK == scheme
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
    }
}
