package org.mar.telegram.bot.db.service;

import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.db.jpa.PostInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@Scope("singleton")
public class PostInfoService {

    @Autowired
    private PostInfoRepository postInfoRepository;

    public PostInfo getNotSendPost() {
        PostInfo postInfo = postInfoRepository.getByIsSend(false);
        if (isNull(postInfo)) {
            return postInfoRepository.save(PostInfo.builder().isSend(false).build());
        }
        return postInfo;
    }

    public PostInfo save(PostInfo postInfo) {
        return postInfoRepository.save(postInfo);
    }

}
