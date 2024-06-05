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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOUBLE_LEFT;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOUBLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_LEFT;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.ELLIPSIS_DOTS_H;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
@RequiredArgsConstructor
public class PostInfoListView implements ContentView {

    public static final int GRID_PAGE_ITEM_COUNT = 50;

    private final RootView rootView;

    private Grid<PostInfoActionRs> grid;
    private IntegerField pageField;
    private Label countPageLabel;

    private Select<PostInfoActionRq.OrderColumn> selectOrder;
    private TextField searchField;

    @Override
    public Component getContent() {
        H2 label = new H2("Post list");
        label.setWidthFull();

        selectOrder = new Select<>(PostInfoActionRq.OrderColumn.values());
        selectOrder.setTextRenderer(PostInfoActionRq.OrderColumn::getTitle
        );
        selectOrder.setLabel("Sort column");
        selectOrder.setPlaceholder("Select sort");
        selectOrder.addValueChangeListener(event -> initGridData(0));

        searchField = new TextField("Search caption");
        searchField.setWidth(20, Unit.PERCENTAGE);
        searchField.addKeyPressListener(Key.ENTER, event -> initGridData(0));

        HorizontalLayout top = new HorizontalLayout(label, selectOrder, searchField);
        top.setAlignSelf(FlexComponent.Alignment.START, label);
        top.setAlignSelf(FlexComponent.Alignment.END, selectOrder);
        top.setAlignSelf(FlexComponent.Alignment.END, searchField);
        top.setWidthFull();


        // TABLE
        grid = new Grid<>();
        // column
        grid.addColumn(PostInfoActionRs::getId)
                .setHeader("ID")
                .setWidth("5%");
        grid.addColumn(postInfoActionRs -> {
                    String caption = postInfoActionRs.getCaption();
                    if (isNotBlank(caption)) {
                        String[] lines = caption.split("\n");
                        if (lines.length > 0) {
                            return lines[0];
                        }
                        return "";
                    }
                    return "";
                })
                .setHeader("Caption")
                .setWidth("70%");
        grid.addColumn(PostInfoActionRs::getAction0)
                .setHeader("❤️")
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getAction1)
                .setHeader(ActionEnum.DEVIL.getCode())
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getAction2)
                .setHeader(ActionEnum.BORING.getCode())
                .setWidth("5%");
        grid.addColumn(PostInfoActionRs::getAction3)
                .setHeader(ActionEnum.BAD.getCode())
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
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, top);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, grid);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btns);
        verticalLayout.add(top, grid, btns);

        // init data
        initGridData(0);
        return verticalLayout;
    }

    private void initGridData(int page) {
        PostInfoActionListRs data = getData(page);
        List<PostInfoActionRs> typeList = data.getData();

        grid.setItems(typeList);

        pageField.setValue(page + 1);
        countPageLabel.setText(String.valueOf((int) Math.ceil(data.getTotalCount() * 1.0 / GRID_PAGE_ITEM_COUNT)));
    }

    private PostInfoActionListRs getData(int page) {
        PostInfoActionRq.OrderColumn column = selectOrder.getValue();
        if (column == null) {
            column = PostInfoActionRq.OrderColumn.ID;
        }
        PostInfoActionRq.OrderType orderType = column.getOrderType();
        String searchText = searchField.getValue();

        PostInfoActionRq rq = new PostInfoActionRq(
                rootView.getAdminId(),
                searchText,
                (long) page,
                (long) GRID_PAGE_ITEM_COUNT,
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
            int totalCntPage = saveParseInt(countPageLabel.getText(), -1);
            if (newPage > 0 && newPage <= totalCntPage) {
                initGridData(newPage - 1);
            } else {
                ViewUtils.showErrorMsg("Incorrect search page number.", new TbotException(Utils.rqUuid(), new Date(), "Need 'newPage > 0 && newPage <= totalCntPage'"));
            }
        });

        Icon dots = new Icon(ELLIPSIS_DOTS_H);
        countPageLabel = new Label("???");


        Button llBtn = ButtonBuilder.createButton()
                .icon(ANGLE_DOUBLE_LEFT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> initGridData(0))
                .build();

        Button lBtn = ButtonBuilder.createButton()
                .icon(ANGLE_LEFT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> {
                    int newPage = saveCastInt(pageField.getValue(), 1);
                    if (newPage > 1) {
                        newPage--;
                    }
                    initGridData(newPage - 1);
                })
                .build();

        Button rBtn = ButtonBuilder.createButton()
                .icon(ANGLE_RIGHT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> {
                    int totalCntPage = saveParseInt(countPageLabel.getText(), 1);
                    int newPage = saveCastInt(pageField.getValue(), totalCntPage);
                    if (newPage < totalCntPage) {
                        newPage++;
                    } else {
                        newPage = totalCntPage;
                    }
                    initGridData(newPage - 1);
                })
                .build();
        Button rrBtn = ButtonBuilder.createButton()
                .icon(ANGLE_DOUBLE_RIGHT)
                .color(ButtonBuilder.Color.BLACK)
                .clickListener(btnClick -> {
                    int totalCntPage = saveParseInt(countPageLabel.getText(), 1);
                    initGridData(totalCntPage - 1);
                })
                .build();

        btns.add(
                llBtn, lBtn,
                pageField, dots, countPageLabel,
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
