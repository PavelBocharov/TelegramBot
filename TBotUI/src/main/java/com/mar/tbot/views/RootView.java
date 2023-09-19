package com.mar.tbot.views;

import com.mar.tbot.service.ApiService;
import com.mar.tbot.service.MapperService;
import com.mar.tbot.views.post.SendPostView;
import com.mar.tbot.views.type.PostTypeListView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

import static com.vaadin.flow.component.icon.VaadinIcon.*;

@Route("")
public class RootView extends AppLayout {

    @Getter
    @Autowired
    private MapperService mapperService;
    @Getter
    @Autowired
    private ApiService apiService;

    @Getter
    @Value("${application.bot.admin.id}") //TODO NEED ?????
    private Long adminId;
    @Getter
    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Getter
    private final PostTypeListView postTypeListView;
    @Getter
    private final SendPostView sendPostView;

    private final StartPageView startPageView;

    public RootView() throws IOException {
        startPageView = new StartPageView();
        postTypeListView = new PostTypeListView(this);
        sendPostView = new SendPostView(this);

        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("'TelegramBot UI' by Marolok");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.add(getTab("Main page", HOME, startPageView));
        tabs.add(getTab("Send post", PIN_POST, sendPostView));
        tabs.add(getTab("Post type list", TASKS, postTypeListView));

        addToDrawer(tabs);
        addToNavbar(toggle, title);
        setContent(startPageView.getContent());
    }

    private Tab getTab(String title, VaadinIcon icon, ContentView contentView) {
        return new Tab(getButton(title, icon, btnClickEvent -> setContent(contentView.getContent())));
    }

    private Button getButton(String title, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(title, new Icon(icon));
        button.setHeightFull();
        button.addClickListener(listener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

}
