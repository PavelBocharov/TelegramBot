package com.mar.telegram.db.jpa;

import com.mar.telegram.db.entity.ActionEnum;
import com.mar.telegram.db.entity.ActionPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionPostRepository extends JpaRepository<ActionPost, Long> {

    ActionPost findByPostIdAndUserInfoId(Long postId, Long userInfoId);
    Long countByPostIdAndAction(Long postId, ActionEnum action);

}
