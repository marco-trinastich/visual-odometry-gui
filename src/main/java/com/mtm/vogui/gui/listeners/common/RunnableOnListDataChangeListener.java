package com.mtm.vogui.gui.listeners.common;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class RunnableOnListDataChangeListener implements ListDataListener {

    private final Runnable runOnAdd;
    private final Runnable runOnDelete;
    private final Runnable runOnChange;

    public RunnableOnListDataChangeListener(Runnable runOnAdd) {
        this(runOnAdd, null);
    }

    public RunnableOnListDataChangeListener(Runnable runOnAdd, Runnable runOnDelete) {
        this(runOnAdd, runOnDelete, null);
    }

    public RunnableOnListDataChangeListener(Runnable runOnAdd, Runnable runOnDelete, Runnable runOnChange) {
        this.runOnAdd = runOnAdd;
        this.runOnDelete = runOnDelete;
        this.runOnChange = runOnChange;
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        if (runOnAdd != null) {
            runOnAdd.run();
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        if (runOnDelete != null) {
            runOnDelete.run();
        }
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        if (runOnChange != null) {
            runOnChange.run();
        }
    }
}
