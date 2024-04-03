package com.mar.tbot.views.post;

import com.mar.dto.rest.PostInfoActionListRs;
import com.mar.dto.rest.PostInfoActionRq;
import com.mar.dto.rest.PostInfoActionRs;
import com.mar.dto.tbot.ActionEnum;
import com.mar.exception.TbotException;
import com.mar.tbot.utils.ButtonBuilder;
import com.mar.tbot.utils.ViewUtils;
import com.mar.tbot.views.ContentView;
import com.mar.tbot.views.RootView;
import com.mar.utils.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOUBLE_LEFT;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOUBLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_LEFT;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.ELLIPSIS_DOTS_H;

@Slf4j
@RequiredArgsConstructor
public class PostInfoListView implements ContentView {

    public static final int GRID_PAGE_ITEM_COUNT = 50;

    private final RootView rootView;

    private Grid<PostInfoActionRs> grid;
    private IntegerField pageField;
    private Label countPageLable;

    @Override
    public Component getContent() {
        H2 label = new H2("Post list");
        // TABLE
        grid = new Grid<>();
        // column
        grid.addColumn(PostInfoActionRs::getId)
                .setHeader("ID")
                .setKey(PostInfoActionRq.OrderColumn.ID.getTableName())
                .setAutoWidth(false)
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getCaption)
                .setHeader("Caption")
                .setKey(PostInfoActionRq.OrderColumn.CAPTION.getTableName())
                .setWidth("70%");
        grid.addColumn(PostInfoActionRs::getAction0)
                .setHeader("❤️")
                .setKey(PostInfoActionRq.OrderColumn.ACTION_0.getTableName())
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getAction1)
                .setHeader(ActionEnum.DEVIL.getCode())
                .setKey(PostInfoActionRq.OrderColumn.ACTION_1.getTableName())
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getAction2)
                .setHeader(ActionEnum.COOL.getCode())
                .setKey(PostInfoActionRq.OrderColumn.ACTION_2.getTableName())
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getAction3)
                .setHeader(ActionEnum.BORING.getCode())
                .setKey(PostInfoActionRq.OrderColumn.ACTION_3.getTableName())
                .setWidth("5%");
        grid.addColumn(postInfoAction -> Boolean.TRUE.equals(postInfoAction.getAdminAction()) ? "✓" : "")
                .setHeader("\uD83D\uDC51")
                .setWidth("5%");
        // settings
        grid.setWidthFull();
        grid.setHeight(75, Unit.PERCENTAGE);
        grid.setPageSize(GRID_PAGE_ITEM_COUNT);
        grid.setVerticalScrollingEnabled(true);

        // create view
        HorizontalLayout btns = getPaginationButtons();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.START, label);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, grid);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btns);
        verticalLayout.add(label, grid, btns);

        // init data
        initGridData(0, PostInfoActionRq.OrderColumn.ID, PostInfoActionRq.OrderType.DESC);
        return verticalLayout;
    }

    private void initGridData(int page, PostInfoActionRq.OrderColumn column, PostInfoActionRq.OrderType orderType) {
        PostInfoActionListRs data = getData(page, column, orderType);
        List<PostInfoActionRs> typeList = data.getData();

        grid.setItems(typeList);

        pageField.setValue(page + 1);
        countPageLable.setText(String.valueOf((int) Math.ceil(data.getTotalCount() * 1.0 / GRID_PAGE_ITEM_COUNT)));
    }

    private PostInfoActionListRs getData(int page, PostInfoActionRq.OrderColumn column, PostInfoActionRq.OrderType orderType) {
        PostInfoActionRq rq = new PostInfoActionRq(
                rootView.getAdminId(),
                "",
                page,
                GRID_PAGE_ITEM_COUNT,
                column,
                orderType
        );
        rq.setRqUuid(Utils.rqUuid());
        rq.setRqTm(new Date());
        return rootView.getApiService().getPostInfoActionList(rq);
    }

    private HorizontalLayout getPaginationButtons() {
        HorizontalLayout btns = new HorizontalLayout();
        btns.setAlignItems(FlexComponent.Alignment.CENTER);

        pageField = new IntegerField();
        pageField.addKeyPressListener(Key.ENTER, event -> {
            int newPage = saveCastInt(pageField.getValue(), -1);
            int totalCntPage = saveParseInt(countPageLable.getText(), -1);
            if (newPage > 0 && newPage <= totalCntPage) {
                initGridData(
                        newPage - 1,
                        PostInfoActionRq.OrderColumn.ID,
                        PostInfoActionRq.OrderType.DESC
                );
            } else {
                ViewUtils.showErrorMsg("Incorrect search page number.", new TbotException(Utils.rqUuid(), new Date(), "Need 'newPage > 0 && newPage <= totalCntPage'"));
            }
        });

        Icon dots = new Icon(ELLIPSIS_DOTS_H);
        countPageLable = new Label("???");


        Button llBtn = ButtonBuilder.createButton()
                .icon(ANGLE_DOUBLE_LEFT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> initGridData(0, PostInfoActionRq.OrderColumn.ID, PostInfoActionRq.OrderType.DESC))
                .build();

        Button lBtn = ButtonBuilder.createButton()
                .icon(ANGLE_LEFT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> {
                    int newPage = saveCastInt(pageField.getValue(), 1);
                    if (newPage > 1) {
                        newPage--;
                    }
                    initGridData(newPage - 1, PostInfoActionRq.OrderColumn.ID, PostInfoActionRq.OrderType.DESC);
                })
                .build();

        Button rBtn = ButtonBuilder.createButton()
                .icon(ANGLE_RIGHT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> {
                    int totalCntPage = saveParseInt(countPageLable.getText(), 1);
                    int newPage = saveCastInt(pageField.getValue(), totalCntPage);
                    if (newPage < totalCntPage) {
                        newPage++;
                    } else {
                        newPage = totalCntPage;
                    }
                    initGridData(newPage - 1, PostInfoActionRq.OrderColumn.ID, PostInfoActionRq.OrderType.DESC);
                })
                .build();
        Button rrBtn = ButtonBuilder.createButton()
                .icon(ANGLE_DOUBLE_RIGHT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> {
                    int totalCntPage = saveParseInt(countPageLable.getText(), 1);
                    initGridData(totalCntPage - 1, PostInfoActionRq.OrderColumn.ID, PostInfoActionRq.OrderType.DESC);
                })
                .build();

        btns.add(
                llBtn, lBtn,
                pageField, dots, countPageLable,
                rBtn, rrBtn
        );
        return btns;
    }

    private int saveParseInt(String number, int defNumb) {
        try {
            return Integer.parseInt(number);
        } catch (Exception ex) {
            return defNumb;
        }
    }

    private int saveCastInt(Integer number, int defNumb) {
        if (number == null) {
            return defNumb;
        }
        return number.intValue();
    }
}
