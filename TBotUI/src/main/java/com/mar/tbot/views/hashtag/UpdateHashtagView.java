package com.mar.tbot.views.hashtag;

import com.mar.tbot.dto.HashTagDto;
import com.mar.tbot.utils.ViewUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import static com.vaadin.flow.component.icon.VaadinIcon.ROTATE_RIGHT;

public class UpdateHashtagView {

    public UpdateHashtagView(HashtagsViewDialog itemStatusView, HashTagDto hashTag) {
        Dialog updateDialog = new Dialog();
        updateDialog.setCloseOnEsc(true);
        updateDialog.setCloseOnOutsideClick(false);

        TextField idField = new TextField();
        idField.setWidthFull();
        idField.setAutofocus(false);
        idField.setEnabled(false);
        idField.setLabel("ID");
        idField.setValue(String.valueOf(hashTag.getId()));

        TextField textField = new TextField();
        textField.setWidthFull();
        textField.setLabel("Tag name");
        textField.setValue(hashTag.getTag());

        Button uptBtn = new Button("Update", new Icon(ROTATE_RIGHT));
        uptBtn.addClickListener(btnEvent -> {
            try {
                String tag = textField.getValue();
                itemStatusView.getRootView().getApiService().updateHashtag(new HashTagDto(hashTag.getId(), tag));
            } catch (Exception ex) {
                ViewUtils.showErrorMsg("Update hashtag exception:", ex);
                uptBtn.setEnabled(true);
                return;
            }
            updateDialog.close();
            itemStatusView.reloadData();
        });
        uptBtn.setWidthFull();
        uptBtn.setDisableOnClick(true);
        uptBtn.addClickShortcut(Key.ENTER);

        updateDialog.add(
                new Label("Update #Hashtag"),
                idField,
                textField,
                new HorizontalLayout(uptBtn, ViewUtils.getCloseButton(updateDialog))
        );

        updateDialog.open();
    }
}
