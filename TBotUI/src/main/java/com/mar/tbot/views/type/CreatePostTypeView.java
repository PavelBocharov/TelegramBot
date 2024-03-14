package com.mar.tbot.views.type;

import com.mar.tbot.dto.PostTypeDtoRq;
import com.mar.tbot.utils.ButtonBuilder;
import com.mar.tbot.utils.Utils;
import com.mar.tbot.utils.ViewUtils;
import com.mar.tbot.views.RootView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.mar.tbot.utils.ViewUtils.getTextFieldValue;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;

@Slf4j
public class CreatePostTypeView extends Dialog {

    public CreatePostTypeView(RootView rootView) {
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(false);
        this.setWidth(80, Unit.PERCENTAGE);

        TextField titleField = new TextField();
        titleField.setWidthFull();
        titleField.setLabel("Title");

        VerticalLayout typeLines = getPostTypeList();

        Button createBtn = new Button("Create", new Icon(PLUS));
        createBtn.addClickListener(btnEvent -> {
            try {
                String title = getTextFieldValue(titleField);
                List<String> lines = new LinkedList<>();

                typeLines.getChildren().forEach(
                        component -> {
                            if (component instanceof HorizontalLayout) {
                                HorizontalLayout hl = (HorizontalLayout) component;
                                hl.getChildren().forEach(
                                        hlElm -> {
                                            if (hlElm instanceof TextField) {
                                                lines.add(getTextFieldValue((TextField) hlElm));
                                            }
                                        }
                                );
                            }
                        }
                );

                log.debug("Create post type data - title: '{}', line: {} ", title, lines);
                PostTypeDtoRq rq = new PostTypeDtoRq(title, lines);
                rq.setRqUuid(Utils.rqUuid());
                rq.setRqTm(new Date());
                rootView.getApiService().createPostType(rq);
            } catch (Exception ex) {
                ViewUtils.showErrorMsg("Create post type exception: ", ex);
                createBtn.setEnabled(true);
                return;
            }
            this.close();
            rootView.setContent(rootView.getPostTypeListView().getContent());
        });
        createBtn.setWidthFull();
        createBtn.setDisableOnClick(true);
        createBtn.addClickShortcut(Key.ENTER);

        this.add(
                new Label("Create post type"),
                titleField,
                typeLines,
                new HorizontalLayout(createBtn, ViewUtils.getCloseButton(this))
        );

        this.open();
    }

    private VerticalLayout getPostTypeList() {
        VerticalLayout typeLines = new VerticalLayout();
        typeLines.setSpacing(false);

        Label label = new Label("Post lines");
        label.setWidth(90, Unit.PERCENTAGE);

        Button addLineBtn = ButtonBuilder.createButton()
                .icon(PLUS)
                .color(ButtonBuilder.Color.GREEN)
                .clickListener(event -> typeLines.add(getLineLayout()))
                .build();
        addLineBtn.setWidth(5, Unit.PERCENTAGE);

        Button helpBtn = ButtonBuilder.createButton()
                .icon(VaadinIcon.QUESTION_CIRCLE_O)
                .clickListener(clkEvent -> {
                    ViewUtils.showSuccessMsg(
                            "Formatting post line",
                            """
                                    You can use HTML tags. More in <a href="https://core.telegram.org/bots/api#html-style" target="_blank">'Telegram bot API Documentation'.</a>
                                    """
                    );
                })
                .build();
        helpBtn.setWidth(5, Unit.PERCENTAGE);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidthFull();
        hl.add(label, addLineBtn, helpBtn);

        typeLines.add(hl, getLineLayout());
        return typeLines;
    }

    private HorizontalLayout getLineLayout() {
        HorizontalLayout lineLayout = new HorizontalLayout();
        lineLayout.setWidthFull();

        TextField line = new TextField();
        line.setWidthFull();
        line.setPlaceholder("Write line title");

        Button rmvLine = new Button(new Icon(TRASH));
        rmvLine.addClickListener(buttonClickEvent -> {
            lineLayout.removeAll();
        });

        lineLayout.add(line, rmvLine);
        return lineLayout;
    }
}
