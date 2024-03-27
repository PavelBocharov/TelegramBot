package com.mar.tbot.views.post;

import com.google.common.collect.ImmutableMap;
import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.HashTagDto;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.exception.TbotException;
import com.mar.tbot.utils.ButtonBuilder;
import com.mar.tbot.utils.ViewUtils;
import com.mar.tbot.views.ContentView;
import com.mar.tbot.views.RootView;
import com.mar.tbot.views.hashtag.HashtagsViewDialog;
import com.mar.utils.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.vaadin.gatanaso.MultiselectComboBox;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.vaadin.flow.component.icon.VaadinIcon.PLUS;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.IMAGE_GIF_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@Slf4j
@RequiredArgsConstructor
public class SendPostView implements ContentView {

    private final RootView rootView;

    private VerticalLayout listLines;
    private MultiselectComboBox<String> hashTagSelect;
    private byte[] uploadFileData;
    private String uploadFileName;
    private int contentLength;
    private String mimeType;

    @Override
    public Component getContent() {
        listLines = new VerticalLayout();
        listLines.setWidth(80, Unit.PERCENTAGE);

        Button sendBtn = ButtonBuilder.createButton()
                .text("Send post")
                .icon(VaadinIcon.PAPERPLANE)
                .color(ButtonBuilder.Color.GREEN)
                .clickListener(clkEvent -> {
                    try {
                        SendPostRq rq = getPostInfoDto();
                        BaseRs msg = rootView.getApiService().sendPost(rq);
                        ViewUtils.showSuccessMsg("Send post success.", msg.getHTML());
                    } catch (Exception ex) {
                        log.warn("Send post error", ex);
                        ViewUtils.showErrorMsg("Send post error", ex);
                    }
                })
                .build();

        Button helpBtn = ButtonBuilder.createButton()
                .text("Help")
                .icon(VaadinIcon.QUESTION_CIRCLE_O)
                .clickListener(clkEvent -> {
                    ViewUtils.showSuccessMsg(
                            "Send post documentation",
                            """
                                    <h3>Formatting post lines</h3>
                                    You can use HTML tags. More in <a href="https://core.telegram.org/bots/api#html-style" target="_blank">'Telegram bot API Documentation'.</a>
                                    <h3>Max file size</h3>
                                    Max file size: 10MB</br>
                                    Admin can update this parameter in configuration:
                                    <pre>
                                    spring.servlet.multipart.max-file-size=10MB
                                    spring.servlet.multipart.max-request-size=10MB
                                    </pre>
                                    """
                    );
                })
                .build();

        VerticalLayout verticalLayout = new VerticalLayout(
                new H3("Send post"),
                getSelectPostType(listLines),
                getUploadView(),
                listLines,
                getHashtagView(),
                new HorizontalLayout(sendBtn, helpBtn)
        );
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        return verticalLayout;
    }

    private Select<PostTypeDtoRs> getSelectPostType(VerticalLayout listLines) {
        // status
//        List<PostType> itemStatusList = rootView.getRepositoryService().getItemStatusRepository().findAll();
        List<PostTypeDtoRs> typeList = rootView.getApiService().getAllPostType(Utils.rqUuid()).getPostTypeList();
        Select<PostTypeDtoRs> postTypeSelect = new Select<PostTypeDtoRs>();
        postTypeSelect.setLabel("Post type");
        postTypeSelect.setPlaceholder("Select post type...");
        postTypeSelect.setTextRenderer(PostTypeDtoRs::getTitle);
        postTypeSelect.setDataProvider(new ListDataProvider<>(typeList));
        postTypeSelect.setWidth(80, Unit.PERCENTAGE);
        postTypeSelect.addValueChangeListener(event -> {
            PostTypeDtoRs selectType = event.getValue();
            listLines.removeAll();
            int lineNumb = 1;
            for (String line : selectType.getLines()) {
                TextField lineField = new TextField(line);
                lineField.setId("post_line_number_" + lineNumb++);
                lineField.setWidthFull();
                listLines.add(lineField);
            }
        });
        return postTypeSelect;
    }

    private Upload getUploadView() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload singleFileUpload = new Upload(memoryBuffer);

        singleFileUpload.addSucceededListener(event -> {
            InputStream fileData = memoryBuffer.getInputStream();
            uploadFileName = event.getFileName();
            contentLength = (int) event.getContentLength();
            mimeType = event.getMIMEType();

            // Do something with the file data
            // processFile(fileData, fileName, contentLength, mimeType);

            try {
                BufferedInputStream bis = new BufferedInputStream(fileData);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                for (int result = bis.read(); result != -1; result = bis.read()) {
                    buf.write((byte) result);
                }
                uploadFileData = buf.toByteArray();

                log.debug("SucceededListener --> filename: {}, size: {} byte, MIME: {}, byteLength: {}", uploadFileName, contentLength, mimeType, uploadFileData.length);

                FileUtils.writeByteArrayToFile(
                        new File(rootView.getDownloadPath() + uploadFileName),
                        uploadFileData
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // TODO: SEND ERROR MSG
        singleFileUpload.addFileRejectedListener(fileRejectedEvent -> {
                    log.debug("FileRejectedListener -->  {}", fileRejectedEvent.getErrorMessage());
                    ViewUtils.showErrorMsg("Send post exception: ", new Exception(fileRejectedEvent.getErrorMessage()));
                }
        );
        singleFileUpload.addFailedListener(failedEvent -> log.warn("FailedListener --> {}", failedEvent.getReason().getMessage()));
        singleFileUpload.addStartedListener(event -> log.debug("StartedListener --> filename: {}, MIME: {}", event.getFileName(), event.getMIMEType()));
        singleFileUpload.addProgressListener(progressUpdateEvent -> log.debug("ProgressListener: --> length: {}", progressUpdateEvent.getContentLength()));

        singleFileUpload.setMaxFileSize(10 * 1024 * 1024);
        singleFileUpload.setAcceptedFileTypes(
                IMAGE_GIF_VALUE,
                IMAGE_JPEG_VALUE,
                IMAGE_PNG_VALUE,
                "video/mp4"
        );

        singleFileUpload.setWidth(80, Unit.PERCENTAGE);
        Button uploadBtn = new Button("Upload file...", new Icon(VaadinIcon.UPLOAD));
        uploadBtn.setWidth(100, Unit.PERCENTAGE);
        singleFileUpload.setUploadButton(uploadBtn);
        return singleFileUpload;
    }

    private Component getHashtagView() {
        HorizontalLayout hashtagView = new HorizontalLayout();
        hashtagView.setWidth(80, Unit.PERCENTAGE);

        reloadHashtagView();

        hashtagView.add(
                hashTagSelect,
                ButtonBuilder.createButton()
                        .icon(PLUS)
                        .color(ButtonBuilder.Color.GREEN)
                        .clickListener(buttonClickEvent -> new HashtagsViewDialog(rootView, this))
                        .build()
        );

        return hashtagView;
    }

    private SendPostRq getPostInfoDto() {
        SendPostRq info = new SendPostRq();
        info.setRqUuid(Utils.rqUuid());
        info.setRqTm(new Date());

        info.setUserId(rootView.getAdminId());

        if (isNull(uploadFileName)) {
            throw new TbotException(info.getRqUuid(), info.getRqTm(), String.format("[%s] Upload file", info.getRqUuid()));
        }
        info.setFilePath(rootView.getDownloadPath() + uploadFileName);

        Map<String, Pair<String, String>> caption = new HashMap<>();
        listLines.getChildren().forEach(
                component -> {
                    if (component instanceof TextField) {
                        TextField line = (TextField) component;
                        String id = line.getId().orElse(null);
                        if (nonNull(id)) {
                            String text = ViewUtils.getTextFieldValue(line);
                            log.info("READ line: id - {}, label - {}, text - {}", id, line.getLabel(), text);
                            caption.put(id, Pair.of(line.getLabel(), text));
                        }
                    }
                }
        );
        log.info("ALL caption: {}", caption);
        log.info("ALL id: {}", caption.keySet());

        Set<String> idSet = new TreeSet<>(caption.keySet());
        log.info("ALL sorted id: {}", idSet);
        ImmutableMap.Builder<String, String> sortedCaption = new ImmutableMap.Builder<String, String>();
        for (String s : idSet) {
            Pair<String, String> pair = caption.get(s);
            sortedCaption.put(pair.getKey(), pair.getValue());
        }
        info.setCaption(sortedCaption.build());
        log.info("ALL sort caption: {}", info.getCaption());

        info.setHashTags(new ArrayList<>(hashTagSelect.getSelectedItems()));

        return info;
    }

    public void reloadHashtagView() {
        Set<String> selectedData = null;
        if (hashTagSelect == null) {
            hashTagSelect = new MultiselectComboBox<>();
            hashTagSelect.setPlaceholder("Select hashtags...");
            hashTagSelect.setItemLabelGenerator(s -> s);
            hashTagSelect.setWidthFull();
            hashTagSelect.setAllowCustomValues(false);
        } else {
            selectedData = hashTagSelect.getSelectedItems();
            hashTagSelect.deselectAll();
        }

        List<String> hashtagList = rootView.getApiService().getHashtagList(Utils.rqUuid()).getTags().stream().map(HashTagDto::getTag).toList();
        hashTagSelect.setItems(hashtagList);

        if (isNotEmpty(selectedData)) {
            Set<String> newSelected = new HashSet<>();

            for (String tag : selectedData) {
                if (hashtagList.contains(tag)) {
                    newSelected.add(tag);
                }
            }

            hashTagSelect.select(newSelected);
        }
    }
}
