/*
 * Copyright 2011 Tyler Blair. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.griefcraft.sql;

import com.griefcraft.lwc.LWC;
import com.griefcraft.sql.Database.Type;
import com.griefcraft.util.Statistics;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Table {

    /**
     * The columns in the table
     */
    private List<Column> columns;

    /**
     * The database object associated with this table
     */
    private Database database;

    /**
     * If this table is to be stored in memory
     */
    private boolean memory;

    /**
     * The table's name
     */
    private String name;

    public Table(Database database, String name) {
        this.database = database;
        this.name = name;

        columns = new ArrayList<Column>();
    }

    /**
     * Add a column to the table
     *
     * @param column
     */
    public void add(Column column) {
        column.setTable(this);

        columns.add(column);
    }

    /**
     * Create the table
     */
    public void execute() {
        StringBuilder buffer = new StringBuilder("CREATE TABLE IF NOT EXISTS ");

        // do the prefix, if we're using MySQL
        String prefix = LWC.getInstance().getConfiguration().getString("database.prefix", "");

        if (prefix == null) {
            prefix = "";
        }

        // the table name
        buffer.append(prefix).append(name);
        buffer.append(" ( ");

        // add the columns
        for (int index = 0; index < columns.size(); index++) {
            Column column = columns.get(index);

            buffer.append(column.getName());
            buffer.append(" ");
            buffer.append(column.getType());
            buffer.append(" ");

            if (column.isPrimary()) {
                buffer.append("PRIMARY KEY ");
            }

            if (column.shouldAutoIncrement() && database.getType() == Type.MySQL) {
                buffer.append("AUTO_INCREMENT ");
            }

            if (!column.getDefaultValue().isEmpty()) {
                buffer.append("DEFAULT ");
                buffer.append(column.getDefaultValue());
                buffer.append(" ");
            }

            // check if there's more columns in the stack
            if (index != (columns.size() - 1)) {
                buffer.append(",");
                buffer.append(" ");
            }
        }

        // finalize
        buffer.append(" ) ");

        // if we're using mysql, check if we're in memory
        if (memory && database.getType() == Type.MySQL) {
            buffer.append("ENGINE = MEMORY");
        }

        // end it
        buffer.append(";");

        // execute it directly to the database
        Statement statement = null;
        try {
            statement = database.getConnection().createStatement();
            statement.executeUpdate(buffer.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }

        // database.log("Synched table " + prefix + name + " (" + columns.size() + " columns)");

        // add the query to performance
        Statistics.addQuery();
    }

    /**
     * @return
     */
    public boolean isInMemory() {
        return memory;
    }

    /**
     * Set if the table is in memory
     *
     * @param memory
     */
    public void setMemory(boolean memory) {
        this.memory = memory;
    }

}
