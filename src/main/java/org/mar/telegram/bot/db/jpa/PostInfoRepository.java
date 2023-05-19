package org.mar.telegram.bot.db.jpa;

import org.mar.telegram.bot.db.entity.PostInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostInfoRepository  extends JpaRepository<PostInfo, Long> {

    PostInfo getByIsSend(Boolean isSend);
    PostInfo getByChatIdAndMessageId(Long chatId, Integer messageId);

}
