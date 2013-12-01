package org.getlwc.sql;

import org.getlwc.util.Tuple;
import org.getlwc.util.TwoWayHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps JDBC by providing name -> id resolution (and also in reverse)
 */
public class JDBCLookupService {

    /**
     * Enum that identifies a lookup type when looking up a name.
     * The order is very important as the ordinal is used internally
     */
    public enum LookupType {

        /**
         * An attribute's name
         */
        ATTRIBUTE_NAME("attribute_name"),

        /**
         * A role's type
         */
        ROLE_TYPE("role_type"),

        /**
         * A role's name (e.g. player name)
         */
        ROLE_NAME("role_name"),

        /**
         * A world's uuid or name
         */
        WORLD_NAME("world_name");

        String suffix;

        LookupType(String suffix) {
            this.suffix = suffix;
        }

        public String getSuffix() {
            return suffix;
        }

    }

    /**
     * The lookup table for each type
     */
    private final Map<LookupType, TwoWayHashMap<String, Integer>> lookup = new HashMap<LookupType, TwoWayHashMap<String, Integer>>();

    /**
     * The database being used
     */
    private JDBCDatabase database;

    public JDBCLookupService(JDBCDatabase database) {
        this.database = database;
    }

    /**
     * Populate the service
     */
    public void populate() {
        for (LookupType type : LookupType.values()) {
            TwoWayHashMap<String, Integer> lookupValues = new TwoWayHashMap<String, Integer>();
            lookup.put(type, lookupValues);

            int size = 0;
            for (Tuple<String, Integer> tuple : database.getLookupAssociations(type)) {
                lookupValues.put(tuple.first(), tuple.second());
                size ++;
            }

            System.out.println(type + ": " + size + " lookup values");
        }
    }

    /**
     * Get the id for the given name. If it does not exist then it will be created
     *
     * @param type
     * @param name
     * @return
     */
    public int get(LookupType type, String name) {
        int id;

        TwoWayHashMap<String, Integer> table = lookup.get(type);

        if (!table.getForward().containsKey(name)) {
            id = database.createLookup(type, name);
            table.put(name, id);
        } else {
            id = table.getForward(name);
        }

        return id;
    }

    /**
     * Get an entry by its id
     *
     * @param type
     * @param id
     * @return
     */
    public String get(LookupType type, int id) {
        return lookup.get(type).getBackward(id);
    }

}
