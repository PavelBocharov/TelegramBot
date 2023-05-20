package org.mar.telegram.bot.db.jpa;

import org.mar.telegram.bot.db.entity.ActionEnum;
import org.mar.telegram.bot.db.entity.ActionPost;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ActionPostRepository extends JpaRepository<ActionPost, Long> {

    ActionPost getByPostIdAndUserInfoId(Long postId, Long userInfoId);
    Long countByPostIdAndAction(Long postId, ActionEnum action);

}
