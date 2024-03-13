package com.mar.tbot.utils;

import com.google.common.io.Resources;
import com.mar.tbot.service.TbotException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

import static com.vaadin.flow.component.icon.VaadinIcon.CLOSE_SMALL;
import static java.lang.String.format;

@UtilityClass
public class ViewUtils {

    public static final int DEFAULT_DURATION_ERROR_MSG = 15_000;

    public static void showErrorMsg(String title, Throwable ex) {
        showErrorMsg(title, ex, DEFAULT_DURATION_ERROR_MSG);
    }

    private static void showErrorMsg(String title, Throwable ex, int duration) {
        showMsg(title, getErrorHTML(ex), NotificationVariant.LUMO_ERROR, duration);
    }

    private static String getErrorHTML(Throwable ex) {
        String[] stackTrace = ExceptionUtils.getStackFrames(ex);
        StringBuilder sb = new StringBuilder();

        if (ex instanceof TbotException tbotException) {
            sb.append("RqUUID: ").append(tbotException.getRqUuid()).append("</br>");
            sb.append("RqTm: ").append(Utils.getISOFormat(tbotException.getRqTm())).append("</br>");
        }

        sb.append("Stack trace:<pre>");
        int countLine = 0;
        for (String s : stackTrace) {
            sb.append(s).append("</br>");
            if (++countLine > 15) {
                break;
            }
        }

        return sb.append("</pre>").toString();
    }

    public static void showSuccessMsg(String title, String msg) {
        showMsg(title, msg, NotificationVariant.LUMO_SUCCESS, DEFAULT_DURATION_ERROR_MSG);
    }

    private static void showMsg(String title, String msg, NotificationVariant theme, int duration) {
        Notification notification = new Notification();
        notification.addThemeVariants(theme);
        notification.setDuration(duration);
        notification.setPosition(Notification.Position.TOP_END);

        VerticalLayout layout = new VerticalLayout();
        Accordion accordion = new Accordion();
        Label msgLabel = new Label();
        msgLabel.getElement().setProperty("innerHTML", msg);
        accordion.add(title, msgLabel);
        accordion.close();

        Button clsBtn = new Button("Close");
        clsBtn.addClickListener(btnClick -> notification.close());

        layout.add(accordion, clsBtn);
        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, accordion, clsBtn);
        layout.getStyle().set("padding", "0px");

        notification.add(layout);
        notification.open();
    }

    public static Image getImageByResource(String pathInResource) throws IOException {
        URL imgUrl = Resources.getResource(pathInResource);
        byte[] img = Resources.asByteSource(imgUrl).read();
        return new Image(
                new StreamResource(
                        FileUtils.getFile(imgUrl.getFile()).getName(),
                        () -> new ByteArrayInputStream(img)), String.format("Not load image: %s", pathInResource)
        );
    }

    public static TextField getTextField(String text, boolean enable) {
        TextField textField = new TextField();
        textField.setReadOnly(true);
        textField.setValue(text);
        return textField;
    }

    public static Button getCloseButton(Dialog closeDialog) {
        return ButtonBuilder.createButton()
                .icon(CLOSE_SMALL)
                .color(ButtonBuilder.Color.RED)
                .clickListener(btnClick -> closeDialog.close())
                .build();
    }

//    public static <T extends HasId> void setSelectValue(Select<T> select, T value, List<T> initDataProviderList) {
//        if (isNull(select)
//                || isNull(value)
//                || isNull(initDataProviderList)
//                || initDataProviderList.isEmpty()
//        )
//            return;
//
//        T selectValue = initDataProviderList.stream()
//                .filter(hasId -> hasId.getId().equals(value.getId()))
//                .findFirst()
//                .orElse(null);
//        if (nonNull(selectValue)) {
//            select.setValue(selectValue);
//        }
//    }

    public static float getFloatValue(BigDecimalField field) {
        if (field == null || field.getValue() == null) return 0;
        return field.getValue().floatValue();
    }


    public static long getLongValue(BigDecimalField field) {
        if (field == null || field.getValue() == null) return 0;
        return field.getValue().longValue();
    }

    public static void setBigDecimalFieldValue(BigDecimalField field, Float value) {
        field.setValue(value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value));
    }

    public static void setBigDecimalFieldValue(BigDecimalField field, Long value) {
        field.setValue(value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value));
    }

    public static void setCheckbox(Checkbox checkbox, Boolean flag) {
        checkbox.setValue(flag == Boolean.TRUE);
    }

    public static String getTextFieldValue(TextField field) {
        if (field == null || field.getValue() == null || field.getValue().length() == 0) return null;
        return field.getValue();
    }

    public static void setTextFieldValue(TextField field, String text) {
        field.setValue(text == null ? "" : text);
    }

    public static void setTextFieldValue(TextArea field, String text) {
        field.setValue(text == null ? "" : text);
    }

    public static String getTextFieldValue(TextArea field) {
        if (field == null || field.getValue() == null || field.getValue().length() == 0) return null;
        return field.getValue();
    }


    /**
     * @param textArea
     * @param countWorldInLine
     * @return has error
     */
    public static boolean checkString(TextArea textArea, int countWorldInLine) {
        String str = textArea.getValue();

        String[] arrStr = str.split("\n");
        for (int i = 0; i < arrStr.length; i++) {
            if (arrStr[i].length() > countWorldInLine) {
                textArea.setErrorMessage(format("In %d line over count word (MAX %d)", i + 1, countWorldInLine));
                textArea.setInvalid(true);
                return true;
            }
        }
        textArea.setInvalid(false);
        textArea.setErrorMessage(null);
        return false;
    }

    public static VerticalLayout getAccordionContent(Component... components) {
        VerticalLayout content = new VerticalLayout(components);
        content.setPadding(false);
        content.setSpacing(false);
        return content;
    }

}
