package com.mtm.vogui.gui.components.common.combobox;

import com.mtm.vogui.gui.editors.ValueComboBoxEditor;
import com.mtm.vogui.gui.listeners.common.ValueComboBoxListener;
import com.mtm.vogui.gui.renderers.ValueComboBoxRenderer;
import com.mtm.vogui.models.interfaces.AdjustableComboBox;
import com.mtm.vogui.models.interfaces.WithValue;
import com.mtm.vogui.utilities.CommonUtils;

import java.util.function.Consumer;
import java.util.function.Function;

public class DisplayValueEditableComboBox<T extends WithValue> extends AdjustableComboBox<T> {

    /**
     * ComboBox editable implementation based on a displayable {@code WithValue} type
     *
     * @param model     type T values
     * @param setter    a function able to set selected item into settings
     * @param generator a function able to generate a T item from edited text
     */
    public DisplayValueEditableComboBox(T[] model,
                                        Consumer<T> setter,
                                        Function<String, T> generator) {
        this(model, setter, generator, null);
    }

    /**
     * ComboBox editable implementation based on a displayable {@code WithValue} type
     *
     * @param model               type T values
     * @param setter              a function able to set selected item into settings
     * @param generator           a function able to generate a T item from edited text
     * @param postSelectionAction an optional function to run after item selection
     */
    public DisplayValueEditableComboBox(T[] model,
                                        Consumer<T> setter,
                                        Function<String, T> generator,
                                        Consumer<ComboBoxSelection<T>> postSelectionAction) {
        super(model);

        // Safe-boxed functions
        var safeGenerator = CommonUtils.getSafeGenerator(generator);
        var safeSetter = CommonUtils.getSafeConsumer(setter);
        var safePostSelectionAction = CommonUtils.getSafeConsumer(postSelectionAction);

        this.setRenderer(new ValueComboBoxRenderer<>(this));
        this.setEditor(new ValueComboBoxEditor<>(safeGenerator));
        this.setListener(safeSetter, safePostSelectionAction);
        this.setEditable(true);
    }

    private void setListener(Consumer<T> setter, Consumer<ComboBoxSelection<T>> postSelectionAction) {
        ValueComboBoxListener<T> listener = new ValueComboBoxListener<>(
                item -> setter.accept(item.value()),
                postSelectionAction
        );
        this.addItemListener(listener);
    }
}
