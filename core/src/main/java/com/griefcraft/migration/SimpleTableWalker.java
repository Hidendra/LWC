package com.griefcraft.migration;

import com.griefcraft.sql.Database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SimpleTableWalker extends TableWalker {

    /**
     * The table to walk over
     */
    private String tableName;

    /**
     * The number of rows in the table
     */
    private int rowCount = -1;

    public SimpleTableWalker(Database database, RowHandler handler, String tableName) {
        super(database, handler);
        this.tableName = database.getPrefix() + tableName;
    }

    public SimpleTableWalker(Database database, RowHandler handler, String tableName, int startOffset) {
        super(database, handler, startOffset);
        this.tableName = database.getPrefix() + tableName;
    }

    /**
     * Get the name of the table being walked
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    @Override
    public void run() {
        if (rowCount == -1) {
            try {
                Statement statement = database.getConnection().createStatement();
                ResultSet set = statement.executeQuery(String.format("SELECT COUNT(*) AS cnt FROM %s", tableName));
                if (set.next()) {
                    rowCount = set.getInt("cnt");
                }
                set.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            Statement statement = database.getConnection().createStatement();
            ResultSet set = statement.executeQuery(String.format("SELECT * FROM %s LIMIT %d, %d", tableName, offset, WALKS_PER_ROUND));
            ResultSetMetaData metaData = set.getMetaData();

            int columnCount = metaData.getColumnCount();
            int handled = 0;

            long start = System.currentTimeMillis();

            while (set.next()) {
                Map<String, Object> data = new HashMap<String, Object>();

                for (int i = 1; i < columnCount; i++) {
                    String name = metaData.getColumnName(i);
                    Object value = set.getObject(i);

                    data.put(name, value);
                }

                if (data.isEmpty()) {
                    break;
                }

                handler.handle(this, data);
                handled ++;
            }

            set.close();
            statement.close();

            long time = System.currentTimeMillis() - start;

            int end = offset + handled;

            if (end > rowCount) {
                end = rowCount;
            }

            if (end % 1000 == 0 || handled == 0) {
                database.log(String.format("[%.2f%% %d/%d] Converted %d rows in the table %s in %dms", ((float) end) / rowCount * 100, end, rowCount, handled, tableName, time));
            }

            // finished walking
            if (handled == 0) {
                task.cancel();
                setChanged();
                notifyObservers("complete");
            } else {
                offset += handled;
                setChanged();
                notifyObservers(offset);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            task.cancel();
        }
    }

}
