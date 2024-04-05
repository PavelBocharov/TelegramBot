package db.migration;

import com.mar.dto.tbot.ContentType;
import com.mar.telegram.db.entity.ActionEnum;
import com.mar.telegram.db.entity.ActionPost;
import com.mar.telegram.db.entity.PostInfo;
import com.mar.telegram.db.entity.UserInfo;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class V1_4_3__CopyDataFromOldTablePostInfo extends BaseJavaMigration {

    public static final String checkTable = "SELECT * FROM pg_tables where tablename = 'post_info_old'";
    public static final String postInfo_sql = """
            SELECT
                id,
                caption,
                chat_id,
                create_date,
                is_send,
                media_path,
                message_id,
                media_type,
                update_date,
                schedule_send_time,
                schedule_send_flag
            FROM
                post_info_old
            """;
    public static final String postInfo_getId = "SELECT nextval('post_info_seq')";
    public static final String postInfo_insertSql = """
            INSERT INTO post_info(
                id,
                caption,
                chat_id,
                create_date,
                is_send,
                media_path,
                message_id,
                media_type,
                update_date,
                schedule_send_time,
                schedule_send_flag
            ) VALUES (
                %d,
                '%s',
                %d,
                '%s',
                %s,
                '%s',
                %d,
                %d,
                %s,
                %s,
                %s
            )
            """;

    public static final String user_sql = "SELECT id, user_id FROM user_info_old";
    public static final String user_getId = "SELECT nextval('user_info_seq')";
    public static final String user_insertSql = "INSERT INTO user_info(id, user_id) VALUES (%d, %d)";

    public static final String action_post_sql =
            "SELECT id, action, create_date, update_date, post_id, user_info_id FROM action_post_old;";
    public static final String action_post_getId = "SELECT nextval('action_post_seq')";
    public static final String action_post_insertSql = """
            INSERT INTO action_post(
                id,
                action,
                create_date,
                update_date,
                post_id,
                user_info_id
            ) VALUES (%d, %d, %s, %s, %d, %d)
            """;

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {

            ResultSet result = statement.executeQuery(checkTable);
            if (!result.next()) {
                return;
            }

            // Post info
            List<PostInfo> postInfoList = new LinkedList<>();
            result = statement.executeQuery(postInfo_sql);
            while (result.next()) {
                PostInfo pi = PostInfo.builder()
                        .id(result.getLong(1))
                        .caption(result.getString(2))
                        .chatId(result.getLong(3))
                        .createDate(result.getDate(4))
                        .isSend(result.getBoolean(5))
                        .mediaPath(result.getString(6))
                        .messageId(result.getInt(7))
                        .updateDate(result.getTimestamp(9))
                        .scheduleSendTime(result.getTimestamp(10))
                        .sendFlag(result.getBoolean(11))
                        .build();

                Integer mediaType = result.getInt(8);
                pi.setType(ContentType.values()[mediaType]);

                postInfoList.add(pi);
            }

            Map<Long, Long> postInfoIdLinks = new HashMap<>();
            for (PostInfo postInfo : postInfoList) {
                result = statement.executeQuery(postInfo_getId);
                result.next();
                Long newId = result.getLong(1);

                postInfoIdLinks.put(postInfo.getId(), newId);

                statement.execute(String.format(postInfo_insertSql,
                                newId,
                                postInfo.getCaption() != null
                                        ? postInfo.getCaption().replace("'", "''")
                                        : null,
                                postInfo.getChatId(),
                                postInfo.getCreateDate(),
                                postInfo.getIsSend(),
                                postInfo.getMediaPath(),
                                postInfo.getMessageId(),
                                getNumbType(postInfo.getType()),
                                convertTimestamp(postInfo.getUpdateDate()),
                                convertTimestamp(postInfo.getScheduleSendTime()),
                                postInfo.getSendFlag()
                        )
                );
            }

            // Users
            result = statement.executeQuery(user_sql);
            Map<Long, Long> userIdLinks = new HashMap<>();
            while (result.next()) {
                Long id = result.getLong(1);
                Long uId = result.getLong(2);
                userIdLinks.put(id, uId);
            }

            for (Long id : userIdLinks.keySet()) {
                result = statement.executeQuery(user_getId);
                result.next();
                Long newId = result.getLong(1);
                Long uId = userIdLinks.get(id);

                statement.execute(String.format(user_insertSql, newId, uId));
                userIdLinks.put(id, newId);
            }

            // Actions
            List<ActionPost> actionPostList = new LinkedList<>();
            result = statement.executeQuery(action_post_sql);
            while (result.next()) {
                ActionPost actionPost = ActionPost.builder()
                        .id(result.getLong(1))
                        .action(ActionEnum.values()[result.getInt(2)])
                        .createDate(result.getDate(3))
                        .updateDate(result.getTimestamp(4))
                        .post(PostInfo.builder().id(result.getLong(5)).build())
                        .userInfo(UserInfo.builder().id(result.getLong(6)).build())
                        .build();

                actionPostList.add(actionPost);
            }

            for (ActionPost actionPost : actionPostList) {
                result = statement.executeQuery(action_post_getId);
                result.next();
                Long id = result.getLong(1);

                statement.execute(String.format(action_post_insertSql,
                                id,
                                getNumbAction(actionPost.getAction()),
                                convertTimestamp(actionPost.getCreateDate()),
                                convertTimestamp(actionPost.getUpdateDate()),
                                postInfoIdLinks.get(actionPost.getPost().getId()),
                                userIdLinks.get(actionPost.getUserInfo().getId())
                        )
                );
            }
        }
    }

    private String convertTimestamp(Date date) {
        return date == null
                ? "NULL"
                : "to_timestamp(" + date.getTime() / 1000 + ")";
    }

    private int getNumbType(ContentType type) {
        int i = 0;
        for (ContentType value : ContentType.values()) {
            if (value.equals(type)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int getNumbAction(ActionEnum type) {
        int i = 0;
        for (ActionEnum value : ActionEnum.values()) {
            if (value.equals(type)) {
                return i;
            }
            i++;
        }
        return -1;
    }

}
