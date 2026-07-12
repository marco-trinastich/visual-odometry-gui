/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shell;

import atlantafx.base.theme.Styles;
import boofcv.BoofVersion;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import com.mtm.vogui.models.constants.AppConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.Year;

/**
 * Modern About dialog for the JavaFX UI: mirrors the Swing About content (app icon, title,
 * version, description, java/quarkus + BoofCV build info, license, author) using AtlantaFX
 * typography so it stays consistent with the active theme. Values come from
 * {@link AppConstants}; version is passed in (Quarkus config, {@code quarkus.application.version}).
 */
final class AboutDialog {

    private static final double LOGO_SIZE = 128;
    private static final double CONTENT_WIDTH = 340;

    private AboutDialog() {
    }

    static void show(String appVersion, Window owner) {
        Alert about = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        about.setTitle(AppConstants.ABOUT_TITLE);
        about.setHeaderText(null);
        about.setGraphic(null);
        if (owner != null) {
            about.initOwner(owner);
        }
        about.getDialogPane().setContent(buildContent(appVersion));

        // Give the About window the app icon (harmless where the OS ignores dialog title-bar icons)
        Image logo = FxUtils.appImage();
        if (logo != null) {
            about.setOnShowing(_ -> {
                if (about.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                    stage.getIcons().add(logo);
                }
            });
        }

        about.showAndWait();
    }

    private static VBox buildContent(String appVersion) {
        // fillWidth=false + Pos.CENTER: every child keeps its preferred width and is centered on the
        // same axis, so short and long lines share one exact centre (no per-child left/right drift).
        VBox content = new VBox(6);
        content.setAlignment(Pos.CENTER);
        content.setFillWidth(false);
        content.setPadding(new Insets(10, 24, 4, 24));

        Image logo = FxUtils.appImage();
        if (logo != null) {
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(LOGO_SIZE);
            logoView.setFitHeight(LOGO_SIZE);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            VBox.setMargin(logoView, new Insets(4, 0, 6, 0));
            content.getChildren().add(logoView);
        }

        Label title = new Label(AppConstants.APP_TITLE);
        title.getStyleClass().add(Styles.TITLE_3);

        Label version = new Label(String.format("Version %s", appVersion));
        version.getStyleClass().add(Styles.TEXT_MUTED);

        Label description = new Label(AppConstants.APP_DESCRIPTION);
        description.getStyleClass().add(Styles.TEXT_MUTED);
        description.setWrapText(true);
        description.setTextAlignment(TextAlignment.CENTER);
        description.setMaxWidth(CONTENT_WIDTH);

        Separator separator = new Separator();
        separator.setPrefWidth(CONTENT_WIDTH);
        separator.setMaxWidth(CONTENT_WIDTH);
        VBox.setMargin(separator, new Insets(8, 0, 8, 0));

        VBox tech = new VBox(2,
                techLabel(String.format(AppConstants.JAVA_INFO, System.getProperty(AppConstants.JAVA_VERSION))),
                techLabel(String.format(AppConstants.BOOFCV_INFO, BoofVersion.VERSION, BoofVersion.BUILD_DATE)),
                techLabel(AppConstants.LICENSE_INFO));
        tech.setAlignment(Pos.CENTER);

        Label author = new Label(String.format(AppConstants.AUTHOR_INFO, Year.now().getValue()));
        author.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_BOLD);

        content.getChildren().addAll(title, version, description, separator, tech, author);
        return content;
    }

    private static Label techLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED, Styles.TEXT_ITALIC);
        return label;
    }
}
