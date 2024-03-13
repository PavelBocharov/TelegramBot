package com.mar.tbot.views;

import com.mar.tbot.utils.ViewUtils;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("TelegramBot UI: Login")
public class LoginPage extends AppLayout {

    private final LoginForm login = new LoginForm();

    public LoginPage() {
        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.addClassName("login-view");
        verticalLayout.setSizeFull();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        login.setAction("login");
        login.addForgotPasswordListener(forgotPasswordEvent -> {
            ViewUtils.showSuccessMsg(
                    "Forgot password?",
                    """
                            <ol>
                            <li>Add in config file '<strong>application.bot.admin.login</strong>' and '<strong>application.bot.admin.pwd.md5</strong>'.</li>
                            <li>Restart UI application.</li>
                            </ol>
                            """
            );
        });

        verticalLayout.add(new H1("TelegramBot UI"), login);

        setContent(verticalLayout);
    }

}
