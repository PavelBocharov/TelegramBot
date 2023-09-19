package com.mar.telegram.db.jpa;

import com.mar.telegram.db.entity.PostInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostInfoRepository  extends JpaRepository<PostInfo, Long> {

    PostInfo getByIsSend(Boolean isSend);
    PostInfo getByChatIdAndMessageId(Long chatId, Integer messageId);

}
