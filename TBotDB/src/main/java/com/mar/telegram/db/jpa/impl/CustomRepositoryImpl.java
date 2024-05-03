package com.mar.telegram.db.jpa.impl;

import com.mar.dto.rest.PostInfoActionRq;
import com.mar.telegram.db.jpa.CustomRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class CustomRepositoryImpl implements CustomRepository {

    public static final String GET_POST_INFO = """
            select
            	pi.id as id,
            	(select count(app.action) from action_post app where app.action = 0 and app.post_id = pi.id) as action0,
            	(select count(app.action) from action_post app where app.action = 1 and app.post_id = pi.id) as action1,
            	(select count(app.action) from action_post app where app.action = 2 and app.post_id = pi.id) as action2,
            	(select count(app.action) from action_post app where app.action = 3 and app.post_id = pi.id) as action3,
            	pi.caption as caption,
            	(select
            		true
            	from action_post acp
            		left join user_info ui on ui.id = acp.user_info_id
            	where ui.user_id = %d and acp.post_id = pi.id order by acp.user_info_id
            	) as admin_action,
            	(
            		SELECT
            			sum(
            				CASE ord_ap.action
            					WHEN 0 THEN 1000
            					WHEN 1 THEN 100
            					WHEN 2 THEN 100
            					ELSE 0
            				END
            			) as sm
            		FROM action_post ord_ap
            	 	where ord_ap.post_id = pi.id
            		group by ord_ap.post_id
            	) as ord
            from
            	post_info pi
            	left join action_post ap on ap.post_id = pi.id
            where
            	pi.is_send = true %s
            group by
            	pi.id
            order by %s %s
            %s %s
            """;

    public static final String LIKE_CAPTION = " and upper(pi.caption) like upper(%s) ";

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Object[]> getPostInfo(
            long adminId, String likeCaption, Long offset, Long limit,
            PostInfoActionRq.OrderColumn orderColumn, PostInfoActionRq.OrderType orderType
    ) {

        String sql = GET_POST_INFO.formatted(
                adminId,
                isBlank(likeCaption) ? "" : LIKE_CAPTION.formatted("'%" + likeCaption.trim() + "%'"),
                orderColumn != null ?  orderColumn.getTableName() : "id",
                orderType != null ?  orderType.getSqlQuery() : PostInfoActionRq.OrderType.DESC.getSqlQuery(),
                limit != null ? "limit " + limit : "",
                offset != null ? "offset " + offset : ""
        );

        return entityManager.createNativeQuery(sql).getResultList();
    }
}
