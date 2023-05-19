package org.mar.telegram.bot.db.service;

import org.mar.telegram.bot.db.entity.ActionEnum;
import org.mar.telegram.bot.db.entity.ActionPost;
import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.db.entity.UserInfo;
import org.mar.telegram.bot.db.jpa.ActionPostRepository;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@Profile("local")
public class ActionPostService implements ActionService {

    @Autowired
    private ActionPostRepository actionPostRepository;

    public ActionPost getByPostIdAndUserInfoId(PostInfo postInfo, UserInfo user) {
        ActionPost actionPost = actionPostRepository.getByPostIdAndUserInfoId(postInfo.getId(), user.getId());
        if (isNull(actionPost)) {
            actionPost = ActionPost.builder()
                    .userInfo(user)
                    .post(postInfo)
                    .build();
        }
        return actionPost;
    }

    public ActionPost save(ActionPost actionPost) {
        return actionPostRepository.save(actionPost);
    }

    public long countByPostIdAndAction(Long postId, ActionEnum actionEnum) {
        return actionPostRepository.countByPostIdAndAction(postId, actionEnum);
    }

}
