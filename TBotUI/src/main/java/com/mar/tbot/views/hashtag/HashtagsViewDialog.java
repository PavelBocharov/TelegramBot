package com.mar.tbot.views.hashtag;

import com.mar.dto.rest.HashTagDto;
import com.mar.tbot.utils.ButtonBuilder;
import com.mar.tbot.utils.DeleteDialogWidget;
import com.mar.utils.Utils;
import com.mar.tbot.utils.ViewUtils;
import com.mar.tbot.views.RootView;
import com.mar.tbot.views.post.SendPostView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.CLOSE_SMALL;

public class HashtagsViewDialog {
    @Getter
    private final RootView rootView;
    @Getter
    private final SendPostView parentView;
    private Dialog dialog;
    private VerticalLayout itemStatusList;
    private Button crtBtn;

    public HashtagsViewDialog(RootView rootView, SendPostView parent) {
        this.rootView = rootView;
        parentView = parent;
        dialog = new Dialog();
        reloadData();
        dialog.open();
    }


    private void initDialogView() {
        crtBtn = new Button("Create hashtag", new Icon(VaadinIcon.PLUS));
        crtBtn.setWidthFull();
        crtBtn.addClickListener(btnClick -> new CreateItemStatusView(this));

        itemStatusList = new VerticalLayout();

        List<HashTagDto> hashTagDtoList = rootView.getApiService().getHashtagList(Utils.rqUuid()).getTags();

        for (HashTagDto hashtag : hashTagDtoList) {
            TextField name = new TextField();
            name.setTitle("Name");
            name.setEnabled(false);
            name.setWidthFull();
            name.setValue(hashtag.getTag());

            Button dltBtn = new Button(new Icon(VaadinIcon.BAN), buttonClickEvent -> {
                try {
                    new DeleteDialogWidget(() -> {
                        rootView.getApiService().removeHashtag(Utils.rqUuid(), hashtag.getId());
                        reloadData();
                    });
                } catch (Exception ex) {
                    ViewUtils.showErrorMsg("Remove hashtag exception:", ex);
                    return;
                }
            });
            dltBtn.getStyle().set("color", "red");

            Button uptBtn = new Button(
                    new Icon(VaadinIcon.PENCIL),
                    buttonClickEvent -> new UpdateHashtagView(this, hashtag)
            );
            this.itemStatusList.add(new HorizontalLayout(name, uptBtn, dltBtn));
        }
    }

    public void reloadData() {
        try {
            initDialogView();
        } catch (Exception ex) {
            ViewUtils.showErrorMsg("Create Hashtag view exception: ", ex);
            crtBtn.setEnabled(true);
            return;
        }
        dialog.removeAll();
        dialog.add(
                new Label("#Hashtag list"),
                itemStatusList,
                new HorizontalLayout(
                        crtBtn,
                        ButtonBuilder.createButton()
                                .color(ButtonBuilder.Color.RED)
                                .icon(CLOSE_SMALL)
                                .clickListener((buttonClickEvent) -> {
                                    dialog.close();
                                    parentView.reloadHashtagView();
                                })
                                .build()
                )
        );
    }

}
