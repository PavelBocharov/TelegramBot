package com.mar.telegram.db.jpa;

import com.mar.telegram.db.entity.PostInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PostInfoRepository extends JpaRepository<PostInfo, Long> {

    PostInfo getByIsSend(Boolean isSend);

    PostInfo getByChatIdAndMessageId(Long chatId, Integer messageId);

    @Query(value = "select count(*) from PostInfo pi where pi.isSend = true")
    Integer getCountPostInfo();

    @Query(value = """
            select
            	pi.id as id,
            	(select count(app.action) from ActionPost app where app.action = 0 and app.post.id = pi.id) as action0,
            	(select count(app.action) from ActionPost app where app.action = 1 and app.post.id = pi.id) as action1,
            	(select count(app.action) from ActionPost app where app.action = 2 and app.post.id = pi.id) as action2,
            	(select count(app.action) from ActionPost app where app.action = 3 and app.post.id = pi.id) as action3,
                pi.caption as caption,
            	(select
            		true
            	from ActionPost acp
            		left join UserInfo ui on ui.id = acp.userInfo.id
            	where ui.userId = :adminId and acp.post.id = pi.id order by acp.userInfo.id
            	) as admin_action
            from
                PostInfo pi
                left join ActionPost ap on ap.post.id = pi.id
            where
            	pi.isSend = true
            group by
            	pi.id
            """
    )
    List<Map<String, Object>> getPostInfo(long adminId, Pageable pageable);

    @Query(value = """
            select
            	pi.id as id,
            	(select count(app.action) from ActionPost app where app.action = 0 and app.post.id = pi.id) as action0,
            	(select count(app.action) from ActionPost app where app.action = 1 and app.post.id = pi.id) as action1,
            	(select count(app.action) from ActionPost app where app.action = 2 and app.post.id = pi.id) as action2,
            	(select count(app.action) from ActionPost app where app.action = 3 and app.post.id = pi.id) as action3,
                pi.caption as caption,
            	(select
            		true
            	from ActionPost acp
            		left join UserInfo ui on ui.id = acp.userInfo.id
            	where ui.userId = :adminId and acp.post.id = pi.id order by acp.userInfo.id
            	) as admin_action
            from
                PostInfo pi
                left join ActionPost ap on ap.post.id = pi.id
            where
            	pi.isSend = true
            	and upper(pi.caption) like upper(:likeCaption)
            group by
            	pi.id
            """
    )
    List<Map<String, Object>> getPostInfo(long adminId, String likeCaption, Pageable pageable);

}
