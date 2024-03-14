package com.mar.tbot.views.type;

import com.mar.tbot.dto.PostTypeDtoRq;
import com.mar.tbot.dto.PostTypeDtoRs;
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
import static com.mar.tbot.utils.ViewUtils.setTextFieldValue;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static com.vaadin.flow.component.icon.VaadinIcon.ROTATE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
public class UpdatePostTypeView extends Dialog {

    public UpdatePostTypeView(RootView rootView, PostTypeDtoRs postType) {
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(false);
        this.setWidth(80, Unit.PERCENTAGE);

        TextField titleField = new TextField();
        titleField.setWidthFull();
        titleField.setLabel("Title");
        setTextFieldValue(titleField, postType.getTitle());

        VerticalLayout typeLines = getPostTypeList(postType);

        Button updBtn = new Button("Update", new Icon(ROTATE_RIGHT));
        updBtn.addClickListener(btnEvent -> {
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

                postType.setTitle(title);
                postType.setLines(lines);
                log.debug("Update post type data - title: '{}', line: {} --> {}", title, lines, postType);

                PostTypeDtoRq rq = new PostTypeDtoRq(postType.getId(), title, lines);
                rq.setRqUuid(Utils.rqUuid());
                rq.setRqTm(new Date());
                rootView.getApiService().updatePostType(rq);
            } catch (Exception ex) {
                ViewUtils.showErrorMsg("Update post type exception: ", ex);
                updBtn.setEnabled(true);
                return;
            }
            this.close();
            rootView.setContent(rootView.getPostTypeListView().getContent());
        });
        updBtn.setWidthFull();
        updBtn.setDisableOnClick(true);
        updBtn.addClickShortcut(Key.ENTER);

        this.add(
                new Label("Update post type"),
                titleField,
                typeLines,
                new HorizontalLayout(updBtn, ViewUtils.getCloseButton(this))
        );

        this.open();
    }

    private VerticalLayout getPostTypeList(PostTypeDtoRs postType) {
        VerticalLayout typeLines = new VerticalLayout();
        typeLines.setSpacing(false);

        Label label = new Label("Post lines");
        label.setWidth(90, Unit.PERCENTAGE);

        Button addLineBtn = ButtonBuilder.createButton()
                .icon(PLUS)
                .color(ButtonBuilder.Color.GREEN)
                .clickListener(event -> typeLines.add(getLineLayout(null)))
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

        typeLines.add(hl);

        if (!isEmpty(postType.getLines())) {
            for (String lineTitle : postType.getLines()) {
                typeLines.add(getLineLayout(lineTitle));
            }
        }
        return typeLines;
    }

    private HorizontalLayout getLineLayout(String lineTitle) {
        HorizontalLayout lineLayout = new HorizontalLayout();
        lineLayout.setWidthFull();

        TextField line = new TextField();
        line.setWidthFull();
        line.setPlaceholder("Write line title");
        if (lineTitle != null) {
            line.setValue(lineTitle);
        }

        Button rmvLine = new Button(new Icon(TRASH));
        rmvLine.addClickListener(buttonClickEvent -> {
            lineLayout.removeAll();
        });

        lineLayout.add(line, rmvLine);
        return lineLayout;
    }
}
