package com.mar.tbot.views.type;

import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.tbot.utils.ButtonBuilder;
import com.mar.tbot.utils.DeleteDialogWidget;
import com.mar.utils.Utils;
import com.mar.tbot.views.ContentView;
import com.mar.tbot.views.RootView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.BAN;
import static com.vaadin.flow.component.icon.VaadinIcon.PENCIL;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
public class PostTypeListView implements ContentView {

    private final RootView rootView;

    @Override
    public Component getContent() {
        H2 label = new H2("Post type list");
        // TABLE
        Grid<PostTypeDtoRs> grid = new Grid<>();
        // column
        grid.addColumn(PostTypeDtoRs::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(PostTypeDtoRs::getTitle).setHeader("Title").setAutoWidth(true);
        grid.addColumn(type -> isEmpty(type.getLines()) ? 0 : type.getLines().size()).setHeader("Count lines").setAutoWidth(true);
        // settings
        grid.setWidthFull();
        // edit
        grid.addItemDoubleClickListener(
                event -> new UpdatePostTypeView(rootView, event.getItem())
        );
        grid.addComponentColumn(postType -> {
            Button edtBtn = new Button(new Icon(PENCIL), clk -> {
                new UpdatePostTypeView(rootView, postType);
            });
            edtBtn.addThemeVariants(LUMO_TERTIARY);

            Button dltBtn = ButtonBuilder.createButton()
                    .icon(BAN)
                    .color(ButtonBuilder.Color.RED)
                    .clickListener(clk -> {
                        new DeleteDialogWidget(() -> {
                            rootView.getApiService().removePostType(Utils.rqUuid(), postType.getId());
                            rootView.setContent(rootView.getPostTypeListView().getContent());
                        });
                    })
                    .build();
            return new HorizontalLayout(edtBtn, dltBtn);
        });

        // value
        List<PostTypeDtoRs> typeList = rootView.getApiService().getAllPostType(Utils.rqUuid()).getPostTypeList();
        grid.setItems(typeList);

        // down buttons
        Button crtBtn = ButtonBuilder.createButton()
                .text("Create post type")
                .icon(PLUS)
                .clickListener(click -> new CreatePostTypeView(rootView))
                .build();
        crtBtn.setWidthFull();

        HorizontalLayout btns = new HorizontalLayout(crtBtn);
        btns.setWidthFull();
        // create view
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.START, label);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, grid);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, btns);
        verticalLayout.add(label, grid, btns);
        return verticalLayout;
    }

}
