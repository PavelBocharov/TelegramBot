package com.mar.tbot.views;

import com.mar.tbot.service.ApiService;
import com.mar.tbot.service.MapperService;
import com.mar.utils.Utils;
import com.mar.tbot.utils.ViewUtils;
import com.mar.tbot.views.post.SendPostView;
import com.mar.tbot.views.type.PostTypeListView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.io.IOException;
import java.util.Properties;

import static com.vaadin.flow.component.icon.VaadinIcon.EXIT_O;
import static com.vaadin.flow.component.icon.VaadinIcon.HOME;
import static com.vaadin.flow.component.icon.VaadinIcon.PIN_POST;
import static com.vaadin.flow.component.icon.VaadinIcon.TASKS;

@Route("")
@PageTitle("TelegramBot UI")
@PWA(name = "TelegramBot UI",
        shortName = "TBot UI",
        description = "Small project for learn etc. and actual skills.",
        iconPath = "icons/icon.png"
)
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

    @SneakyThrows
    public RootView() throws IOException {
        startPageView = new StartPageView();
        postTypeListView = new PostTypeListView(this);
        sendPostView = new SendPostView(this);

        DrawerToggle toggle = new DrawerToggle();
        H1 title = new H1("'TelegramBot UI' by Marolok");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        Properties properties = Utils.loadProperties("application.properties");
        String versions = properties.getProperty("version");
        Label version = new Label(versions);
        version.getStyle().set("font-size", "xx-small");

        HorizontalLayout headTitle = new HorizontalLayout(title, version);
        headTitle.getStyle().set("margin-left", "auto");
        headTitle.getStyle().set("padding", "15px");

        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.add(getTab("Main page", HOME, startPageView));
        tabs.add(getTab("Send post", PIN_POST, sendPostView));
        tabs.add(getTab("Post type list", TASKS, postTypeListView));
        tabs.add(getLogoutButton());

        addToDrawer(tabs);
        addToNavbar(toggle, headTitle);
        setContent(startPageView.getContent());
    }

    private Tab getTab(String title, VaadinIcon icon, ContentView contentView) {
        return new Tab(getButton(title, icon, btnClickEvent -> {
            try {
                setContent(contentView.getContent());
            } catch (Exception ex) {
                ViewUtils.showErrorMsg(String.format("Not load '%s'", title), ex);
            }
        }));
    }

    private Button getButton(String title, VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(title, new Icon(icon));
        button.setHeightFull();
        button.addClickListener(listener);
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    private Tab getLogoutButton() {
        Icon logo = new Icon(EXIT_O);
        logo.setColor("red");

        Button button = new Button("Logout", logo);
        button.setHeightFull();
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(buttonClickEvent -> {
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
        });

        return new Tab(button);
    }

}
