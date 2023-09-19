package com.mar.tbot.mapper;

import com.mar.tbot.dto.sendMsg.CallbackQueryDto;
import com.mar.tbot.dto.sendMsg.MessageDto;
import com.mar.tbot.dto.sendMsg.PhotoSizeDto;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;

import java.util.Arrays;

import static org.springframework.util.ObjectUtils.isEmpty;

public class TelegramDataMapper {

    public static MessageDto toDto(Message msg) {
        if (msg == null) {
            return null;
        }
        MessageDto dto = new MessageDto();

        if (msg.from() != null) {
            dto.setFromUserId(msg.from().id());
        }

        dto.setText(msg.text());

        if (msg.chat() != null) {
            dto.setChatId(msg.chat().id());
        }

        if (msg.photo() != null) {
            dto.setPhotoSizeList(Arrays.stream(msg.photo())
                    .map(photo -> new PhotoSizeDto(photo.fileId(), photo.fileSize()))
                    .toList()
            );
        }

        if (msg.document() != null) {
            dto.setDocumentFileId(msg.document().fileId());
        }

        if (msg.video() != null) {
            dto.setVideoFileId(msg.video().fileId());
        }

        if (msg.animation() != null) {
            dto.setAnimationFileId(msg.animation().fileId());
        }

        return dto;
    }

    public static CallbackQueryDto toDto(CallbackQuery query) {
        if (query == null) {
            return null;
        }
        CallbackQueryDto dto = new CallbackQueryDto();
        dto.setActionCallbackData(query.data());
        if (query.from() != null) {
            dto.setFromUserId(query.from().id());
        }
        if (query.message() != null) {
            dto.setMessageId(query.message().messageId());
            dto.setMessageCaption(query.message().caption());
            if (query.message().chat() != null) {
                dto.setMsgChatId(query.message().chat().id());
            }
            if (query.message().video() != null) {
                dto.setVideoFieldId(query.message().video().fileId());
            }
            if (query.message().animation() != null) {
                dto.setAnimationFieldId(query.message().animation().fileId());
            }
            if (query.message().document() != null) {
                dto.setDocumentFieldId(query.message().document().fileId());
            }
            if (query.message().photo() != null) {
                PhotoSize ps = getMaxPhotoSize(query.message().photo());
                if (ps != null) {
                    dto.setPhotoFieldId(ps.fileId());
                }
            }
        }

        return dto;
    }

    public static PhotoSize getMaxPhotoSize(PhotoSize... photoSizes) {
        if (isEmpty(photoSizes)) return null;

        PhotoSize ps = null;
        for (PhotoSize photoSize : photoSizes) {
            if (ps == null) {
                ps = photoSize;
            } else {
                if (ps.fileSize() < photoSize.fileSize()) {
                    ps = photoSize;
                }
            }
        }
        return ps;
    }

}
