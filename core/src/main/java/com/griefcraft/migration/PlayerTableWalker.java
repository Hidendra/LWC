package com.griefcraft.migration;

import com.griefcraft.migration.RowHandler;
import com.griefcraft.migration.TableWalker;
import com.griefcraft.sql.Database;

import java.sql.PreparedStatement;
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
                database.log("Pre-loading distinct player names. This might take a minute or two.");
                database.log("Once complete, everything else will run in the background");

                PreparedStatement statement = database.prepare("SELECT DISTINCT owner FROM " + database.getPrefix() + "protections");
                ResultSet set = statement.executeQuery();

                while (set.next()) {
                    names.add(set.getString("owner"));
                }

                set.close();

                iter = names.iterator();

                database.log("Converting " + names.size() + " names to normalized format (background task)");
            }

            int handled = 0;

            Map<String, Object> data = new HashMap<String, Object>();

            long start = System.currentTimeMillis();

            for (int i = 0; i < WALKS_PER_ROUND && iter.hasNext(); i++) {
                data.put("name", iter.next());
                handler.handle(this, data);
                handled ++;
            }

            long time = System.currentTimeMillis() - start;

            int end = offset + handled;
            int total = names.size();

            if (end > total) {
                end = total;
            }

            if (end % 500 == 0 || handled == 0) {
                database.log(String.format("[%.2f%% %d/%d] Converted %d players in %dms", ((float) end) / total * 100, end, total, handled, time));
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
