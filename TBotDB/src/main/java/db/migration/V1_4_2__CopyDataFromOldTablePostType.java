package db.migration;

import org.apache.commons.lang3.tuple.Pair;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class V1_4_2__CopyDataFromOldTablePostType extends BaseJavaMigration {

    public static final String checkTable = "SELECT * FROM pg_tables where tablename = 'post_type_old'";
    public static final String postType_sql = "SELECT id, title FROM post_type_old";
    public static final String postType_getId = "SELECT nextval('post_type_seq')";
    public static final String postType_insertSql = "INSERT INTO post_type(id, title) VALUES (%d, '%s')";

    public static final String lines_sql = "SELECT line_id, lines FROM line_old";
    public static final String lines_insertSql = "INSERT INTO line(line_id, lines) VALUES (%d, '%s')";

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {

            ResultSet result = statement.executeQuery(checkTable);
            if (!result.next()) {
                return;
            }

            result = statement.executeQuery(postType_sql);
            Map<Long, String> postTypes = new HashMap<>();
            while (result.next()) {
                Long id = result.getLong(1);
                String title = result.getString(2);
                postTypes.put(id, title);
            }

            List<Pair<Long, String>> lines = new LinkedList<>();
            result = statement.executeQuery(lines_sql);
            while (result.next()) {
                Long id = result.getLong(1);
                String line = result.getString(2);
                lines.add(Pair.of(id, line));
            }

            for (Long postTypeId : postTypes.keySet()) {
                List<String> lineList = lines.stream()
                        .filter(pair -> postTypeId.equals(pair.getKey()))
                        .map(pair -> pair.getValue())
                        .toList();

                result = statement.executeQuery(postType_getId);
                result.next();
                Long id = result.getLong(1);

                statement.execute(String.format(postType_insertSql, id, postTypes.get(postTypeId)));
                for (String lineTitle : lineList) {
                    statement.execute(String.format(lines_insertSql, id, lineTitle));
                }
            }

        }
    }

}
