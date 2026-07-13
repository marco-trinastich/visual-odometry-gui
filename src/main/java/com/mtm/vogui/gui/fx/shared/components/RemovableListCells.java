/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.function.Consumer;

/**
 * App-blind cell factory for a {@code ComboBox<String>} / {@code ListView<String>} drop-down whose
 * items each carry a trailing "✕" delete button (see {@code gui.fx.shared} package rules — no
 * settings/context/sink here, only the widget and an injected callback).
 * <p>
 * The "✕" is always visible (turning danger-red on hover) and consumes its own mouse-press, so
 * clicking it removes the entry without also selecting/committing it in the combo. Only drop-down
 * cells are decorated — the combo's button cell (the shown selection) is untouched. A one-click,
 * discoverable replacement for the Swing Shift+Delete on recent-path histories.
 */
public final class RemovableListCells {

    private RemovableListCells() {
    }

    /** Cell factory rendering each item as its text plus a trailing "✕" delete affordance wired to {@code onDelete}. */
    public static Callback<ListView<String>, ListCell<String>> withDeleteButton(Consumer<String> onDelete) {
        return _ -> new DeletableCell(onDelete);
    }

    private static final class DeletableCell extends ListCell<String> {

        private final Label label = new Label();
        // A Label, not a Button: an icon-styled Button needs a graphic node (no icon font here) and
        // renders as an empty square; a plain glyph Label is always visible and reliably shows "✕".
        private final Label deleteButton = new Label("✕");
        private final HBox layout;

        DeletableCell(Consumer<String> onDelete) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            deleteButton.setCursor(Cursor.HAND);
            deleteButton.setPadding(new Insets(0, 4, 0, 8));
            // Subtle by default, danger-red on hover (theme tokens — no stylesheet needed).
            deleteButton.getStyleClass().add(Styles.TEXT_SUBTLE);
            deleteButton.setOnMouseEntered(_ -> {
                deleteButton.getStyleClass().remove(Styles.TEXT_SUBTLE);
                deleteButton.getStyleClass().add(Styles.DANGER);
            });
            deleteButton.setOnMouseExited(_ -> {
                deleteButton.getStyleClass().remove(Styles.DANGER);
                deleteButton.getStyleClass().add(Styles.TEXT_SUBTLE);
            });
            // Consume the press so removing an entry never also selects/commits it in the combo.
            deleteButton.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                String item = getItem();
                if (item != null) {
                    onDelete.accept(item);
                }
                event.consume();
            });
            layout = new HBox(label, spacer, deleteButton);
            layout.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                label.setText(item);
                setText(null);
                setGraphic(layout);
            }
        }
    }
}
