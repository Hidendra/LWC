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
        try {
            Statement statement = database.getConnection().createStatement();
            ResultSet set = statement.executeQuery(String.format("SELECT * FROM %s LIMIT %d, %d", tableName, offset, WALKS_PER_ROUND));
            ResultSetMetaData metaData = set.getMetaData();

            int columnCount = metaData.getColumnCount();
            int handled = 0;

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

            statement.close();

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
