package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

public class V1_4_1__CopyDataFromOldTableHashTag extends BaseJavaMigration {

    public static final String checkTable = "SELECT * FROM pg_tables where tablename = 'hashtag_old'";
    public static final String sql = "SELECT id, tag FROM hashtag_old";
    public static final String getId = "SELECT nextval('hashtag_seq')";
    public static final String insertSql = "INSERT INTO hashtag(id, tag) VALUES (%d, '%s')";

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {

            ResultSet result = statement.executeQuery(checkTable);
            if (!result.next()) {
                return;
            }

            result = statement.executeQuery(sql);

            List<String> tags = new LinkedList<>();

            while (result.next()) {
                String tag = result.getString(2);
                tags.add(tag);
            }

            for (String tag : tags) {
                result = statement.executeQuery(getId);
                result.next();
                Long id = result.getLong(1);

                statement.execute(String.format(insertSql, id, tag));
            }
        }
    }

}
