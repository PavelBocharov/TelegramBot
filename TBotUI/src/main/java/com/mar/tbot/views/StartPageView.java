package com.mar.tbot.views;

import com.mar.tbot.utils.ViewUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class StartPageView implements ContentView {


    @SneakyThrows
    public Component getContent() {
        Image gitHubImage = ViewUtils.getImageByResource("static/img/github_qr.png");
        gitHubImage.setWidth(100, Unit.PERCENTAGE);
        Anchor anchor = new Anchor("https://github.com/PavelBocharov/TelegramBot", gitHubImage);

        VerticalLayout verticalLayout = new VerticalLayout(
                new H3("Welcome to 'TelegramBot'"),
                new Text("Small project for learn etc. and actual skills."),
                anchor
        );
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return verticalLayout;
    }

}
