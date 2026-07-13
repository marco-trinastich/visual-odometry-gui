/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.components;

import java.util.function.BiConsumer;

import org.fxmisc.flowless.Cell;
import org.fxmisc.flowless.VirtualFlow;
import org.fxmisc.flowless.VirtualFlowHit;
import org.fxmisc.flowless.VirtualizedScrollPane;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * A reusable, app-blind virtualized list with genuinely pixel-smooth scrolling — the modern
 * replacement for {@code ListView} wherever the wheel feel matters. It hides {@link VirtualFlow
 * Flowless} entirely: callers never touch the library, they pass an observable model and a row
 * renderer and get back a {@link #node()}.
 * <p>
 * Why not {@code ListView}: JavaFX's own {@code VirtualFlow} applies the whole wheel delta in one
 * step and its cells {@code snapToPixel}, so scrolling snaps cell-by-cell with no smooth mode and no
 * config to disable it (an easing hack on the native flow still leaves a residual sub-pixel stall on
 * short gestures). Flowless is a pixel-based virtual flow (the one behind RichTextFX): only the
 * visible cells are rendered, so thousands of rows stay cheap, and the scroll glides pixel-perfect.
 * <p>
 * Single-selection like a {@code ListView}: click a row to select it, or move the selection with the
 * keyboard once the list has focus (Up/Down, PageUp/PageDown, Home/End) — the selection always scrolls
 * into view. Selection is pure selection (no clipboard side effect): the selected row reads via
 * {@link #selectedItemProperty()} so a feature can react (navigate, enable actions) without the widget
 * knowing what a row means. Explicit actions are wired app-side: {@link #setContextMenu(ContextMenu)}
 * pops on right-click (Menu key / ctrl-click too), first selecting the row under the cursor, and
 * {@link #setOnCopy(Runnable)} fires on the platform copy shortcut ({@code Shortcut+C}). Selection
 * follows the row by identity across recycling/scroll, and clears if the selected row leaves the model.
 * <p>
 * Styled like an AtlantaFX list via {@code .smooth-list} / {@code .smooth-list-cell} in {@code app.css}
 * (theme-aware looked-up colours, subtle row hover, accent {@code :selected} row). The list follows the
 * tail as rows arrive, but yields the instant the user interacts (hovering to wheel-scroll, or keyboard
 * focus) so auto-scroll never fights a manual scroll — the same guard the native version needed.
 * <p>
 * Reusable-widget contract (see {@code gui.fx.shared} rules — no settings/context/sink here): the
 * {@code render} callback receives a recycled {@link Label}, so it must fully (re)set every mutable
 * aspect of the row it touches (text, style classes) on each call, not assume a blank label. The
 * {@code :selected} pseudo-class is owned by this widget, never by the callback.
 *
 * @param <T> the row model type
 */
public final class SmoothList<T> {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    /** {@code Shortcut+C} — Cmd+C on macOS, Ctrl+C elsewhere. */
    private static final KeyCombination COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);

    private final ObservableList<T> items;
    private final VirtualFlow<T, Cell<T, Node>> flow;
    private final VirtualizedScrollPane<VirtualFlow<T, Cell<T, Node>>> node;

    private final ReadOnlyObjectWrapper<T> selectedItem = new ReadOnlyObjectWrapper<>(this, "selectedItem");
    private int selectedIndex = -1;
    private ContextMenu contextMenu;
    private Runnable onCopy;
    /** Stable reference so the one-shot outside-click dismissal can remove itself (see {@link
     *  #showContextMenu(double, double)}). */
    private final EventHandler<MouseEvent> outsidePressDismiss = _ -> contextMenu.hide();

    /**
     * @param items      observable model; the list re-renders and follows the tail as it changes
     * @param rowHeight  fixed row height in px (uniform rows keep virtualization cheap)
     * @param render     fills a (recycled) row label from a model item — must reset all state it sets
     */
    public SmoothList(ObservableList<T> items, double rowHeight, BiConsumer<Label, T> render) {
        this.items = items;
        this.flow = VirtualFlow.createVertical(items, item -> cell(item, rowHeight, render));
        // No horizontal bar: rows clip like a ListView rather than growing a bottom scrollbar.
        this.node = new VirtualizedScrollPane<>(flow, ScrollBarPolicy.NEVER, ScrollBarPolicy.AS_NEEDED);
        this.node.getStyleClass().add("smooth-list");

        this.flow.setFocusTraversable(true);
        this.flow.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        this.flow.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, this::onContextMenuRequested);
        this.flow.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

        items.addListener((ListChangeListener<T>) _ -> {
            if (!items.isEmpty() && !node.isHover() && !node.isFocusWithin()) {
                flow.showAsLast(items.size() - 1);
            }
            reconcileSelection();
        });
    }

    /** The widget to add to the scene graph. */
    public Region node() {
        return this.node;
    }

    /**
     * The currently selected row, or {@code null} when nothing is selected (or the selected row has
     * left the model). Read-only: selection is driven by user interaction, not by callers.
     */
    public ReadOnlyObjectProperty<T> selectedItemProperty() {
        return this.selectedItem.getReadOnlyProperty();
    }

    /** Context menu shown on right-click (the row under the cursor is selected first). */
    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    /** Action fired on the platform copy shortcut ({@code Shortcut+C}) — typically "copy selected row". */
    public void setOnCopy(Runnable onCopy) {
        this.onCopy = onCopy;
    }

    private Cell<T, Node> cell(T item, double rowHeight, BiConsumer<Label, T> render) {
        Label label = new Label();
        label.getStyleClass().add("smooth-list-cell");
        label.setMinHeight(rowHeight);
        label.setPrefHeight(rowHeight);
        label.setMaxWidth(Double.MAX_VALUE); // fill the row so the hover highlight spans full width
        label.setMinWidth(0);                // let text clip instead of forcing a horizontal scrollbar
        renderRow(label, item, render);
        return new Cell<>() {
            @Override
            public Node getNode() {
                return label;
            }

            @Override
            public boolean isReusable() {
                return true;
            }

            @Override
            public void updateItem(T newItem) {
                renderRow(label, newItem, render);
            }
        };
    }

    /** Renders a row and (re)applies the widget-owned {@code :selected} state — identity-based, so it
     *  stays correct as cells recycle during scroll. */
    private void renderRow(Label label, T item, BiConsumer<Label, T> render) {
        render.accept(label, item);
        label.pseudoClassStateChanged(SELECTED, item != null && item == selectedItem.get());
    }

    private void onMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        flow.requestFocus();
        selectAt(event.getX(), event.getY());
    }

    private void onContextMenuRequested(ContextMenuEvent event) {
        if (contextMenu == null) {
            return;
        }
        flow.requestFocus();
        selectAt(event.getX(), event.getY()); // right-click acts on the row under the cursor
        showContextMenu(event.getScreenX(), event.getScreenY());
        event.consume();
    }

    /**
     * Shows the context menu and guarantees it dismisses on an outside click. {@code ContextMenu}'s
     * own auto-hide relies on the follow-up press reaching the owner window, but Flowless' {@link
     * VirtualFlow} swallows mouse presses, so an outside click could leave the menu lingering. A
     * one-shot scene-level press filter (self-removing when the menu hides) closes it reliably; a
     * press inside the menu lives in the popup's own window, so it never trips this filter.
     */
    private void showContextMenu(double screenX, double screenY) {
        Scene scene = node.getScene();
        if (scene == null) {
            return;
        }
        contextMenu.setOnHidden(_ -> scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressDismiss));
        contextMenu.show(flow, screenX, screenY);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressDismiss);
    }

    private void selectAt(double x, double y) {
        VirtualFlowHit<Cell<T, Node>> hit = flow.hit(x, y);
        if (hit.isCellHit()) {
            select(hit.getCellIndex());
        }
    }

    private void onKeyPressed(KeyEvent event) {
        if (onCopy != null && COPY.match(event)) {
            onCopy.run();
            event.consume();
            return;
        }
        if (items.isEmpty()) {
            return;
        }
        int last = items.size() - 1;
        switch (event.getCode()) {
            case UP -> select(selectedIndex < 0 ? last : Math.max(0, selectedIndex - 1));
            case DOWN -> select(selectedIndex < 0 ? 0 : Math.min(last, selectedIndex + 1));
            case HOME -> select(0);
            case END -> select(last);
            case PAGE_UP -> select(Math.max(0, (selectedIndex < 0 ? last : selectedIndex) - pageStep()));
            case PAGE_DOWN -> select(Math.min(last, (selectedIndex < 0 ? 0 : selectedIndex) + pageStep()));
            default -> {
                return;
            }
        }
        event.consume();
    }

    private int pageStep() {
        int visible = flow.getLastVisibleIndex() - flow.getFirstVisibleIndex();
        return Math.max(1, visible);
    }

    private void select(int index) {
        int clamped = (index < 0 || index >= items.size()) ? -1 : index;
        if (clamped == selectedIndex) {
            return;
        }
        selectedIndex = clamped;
        selectedItem.set(clamped < 0 ? null : items.get(clamped));
        if (clamped >= 0) {
            flow.show(clamped); // keep the selection on screen (keyboard nav past the viewport edge)
        }
        refreshSelectionStyles();
    }

    /** Keeps the selection consistent when the model changes underneath it (rows dropped or cleared). */
    private void reconcileSelection() {
        if (selectedIndex < 0) {
            return;
        }
        if (selectedIndex >= items.size() || items.get(selectedIndex) != selectedItem.get()) {
            selectedIndex = -1;
            selectedItem.set(null);
        }
    }

    /** Re-applies {@code :selected} to the visible cells after the selection moves without a re-render
     *  (click/keyboard leave the surrounding cells in place). */
    private void refreshSelectionStyles() {
        int first = flow.getFirstVisibleIndex();
        int last = flow.getLastVisibleIndex();
        if (first < 0) {
            return;
        }
        for (int i = first; i <= last; i++) {
            final int index = i;
            flow.getCellIfVisible(index).ifPresent(cell -> {
                Node cellNode = cell.getNode();
                if (cellNode != null) {
                    cellNode.pseudoClassStateChanged(SELECTED, index == selectedIndex);
                }
            });
        }
    }
}
