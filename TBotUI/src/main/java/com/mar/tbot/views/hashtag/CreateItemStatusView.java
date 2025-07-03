package com.mar.tbot.views.hashtag;

import com.mar.dto.rest.HashTagDto;
import com.mar.tbot.utils.ViewUtils;
import com.mar.utils.Utils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;

public class CreateItemStatusView {

    public CreateItemStatusView(HashtagsViewDialog hashtagListView) {
        Dialog createDialog = new Dialog();
        createDialog.setCloseOnEsc(true);
        createDialog.setCloseOnOutsideClick(false);

        TextField textField = new TextField();
        textField.setWidthFull();
        textField.setLabel("Tag name");

        Button createBtn = new Button("Create", new Icon(PLUS));
        createBtn.addClickListener(btnEvent -> {
            try {
                String tag = textField.getValue();
                hashtagListView.getRootView().getApiService().createHashtag(Utils.rqUuid(), new HashTagDto().withTag(tag));
            } catch (Exception ex) {
                ViewUtils.showErrorMsg("Create hashtag exception: ", ex);
                createBtn.setEnabled(true);
                return;
            }
            createDialog.close();
            hashtagListView.reloadData();
        });
        createBtn.setWidthFull();
        createBtn.setDisableOnClick(true);
        createBtn.addClickShortcut(Key.ENTER);

        createDialog.add(
                new Label("Create #Hashtag"),
                textField,
                new HorizontalLayout(createBtn, ViewUtils.getCloseButton(createDialog))
        );

        createDialog.open();
    }

}
