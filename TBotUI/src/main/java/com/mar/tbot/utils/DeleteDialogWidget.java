package com.mar.tbot.utils;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import static com.vaadin.flow.component.icon.VaadinIcon.BAN;
import static com.vaadin.flow.component.icon.VaadinIcon.CHECK;

public class DeleteDialogWidget extends Dialog {

    public DeleteDialogWidget(Runnable deleteEvent) {
        Dialog deleteDialog = new Dialog();
        deleteDialog.setCloseOnEsc(true);
        deleteDialog.setCloseOnOutsideClick(false);

        Button yesBtn = ButtonBuilder.createButton()
                .text("Remove")
                .icon(CHECK)
                .color(ButtonBuilder.Color.RED)
                .clickListener(btnEvent -> {
                    try {
                        deleteEvent.run();
                    } catch (Exception ex) {
                        ViewUtils.showErrorMsg("In remove process has error: ", ex);
                    }
                    deleteDialog.close();
                })
                .build();


        Button noBtn = ButtonBuilder.createButton()
                .text("Close")
                .icon(BAN)
                .clickListener(btnEvent -> deleteDialog.close())
                .build();

        deleteDialog.add(new Text("Remove data?"), new HorizontalLayout(yesBtn, noBtn));
        deleteDialog.open();
    }

}
