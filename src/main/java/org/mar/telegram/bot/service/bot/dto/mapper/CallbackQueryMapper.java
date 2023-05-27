package org.mar.telegram.bot.service.bot.dto.mapper;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.PhotoSize;
import org.mar.telegram.bot.service.bot.dto.CallbackQueryDto;

import static org.mar.telegram.bot.utils.Utils.getMaxPhotoSize;

public class CallbackQueryMapper {

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

}
