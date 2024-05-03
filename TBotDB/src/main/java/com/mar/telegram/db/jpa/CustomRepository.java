package com.mar.telegram.db.jpa;

import com.mar.dto.rest.PostInfoActionRq;

import java.util.List;

public interface CustomRepository {

    List<Object[]> getPostInfo(
            long adminId, String likeCaption, Long offset, Long limit,
            PostInfoActionRq.OrderColumn orderColumn, PostInfoActionRq.OrderType orderType
    );

}
