package com.griefcraft.migration;

import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.sql.Database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PlayerTableWalker extends TableWalker {

    /**
     * The set of names we are walking through
     */
    private final Set<String> names = new HashSet<String>();

    /**
     * The local iterator we are using for the names
     */
    private Iterator<String> iter;

    public PlayerTableWalker(Database database, RowHandler handler) {
        super(database, handler);
    }

    @Override
    public void run() {
        try {
            if (names.size() == 0) {
                Statement statement = database.getConnection().createStatement();
                ResultSet set = statement.executeQuery(String.format("SELECT DISTINCT owner FROM " + database.getPrefix() + "protections"));

                while (set.next()) {
                    names.add(set.getString("owner"));
                }

                iter = names.iterator();

                statement.close();
            }

            int handled = 0;

            Map<String, Object> data = new HashMap<String, Object>();

            for (int i = 0; i < WALKS_PER_ROUND && iter.hasNext(); i++) {
                data.put("name", iter.next());
                handler.handle(this, data);
                handled ++;
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
