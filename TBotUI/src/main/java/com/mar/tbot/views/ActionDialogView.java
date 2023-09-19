package com.mar.tbot.views;

import com.vaadin.flow.component.dialog.Dialog;

public abstract class ActionDialogView<T> extends Dialog {

    abstract void action(T entity);

}
