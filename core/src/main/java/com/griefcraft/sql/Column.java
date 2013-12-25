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

/**
 * FIXME:
 */
class Column {

    /**
     * If the table should auto increment. Note: This is automatically set for SQLite with:
     * <p>
     * table.setPrimary(true);
     * </p>
     */
    private boolean autoIncrement = false;

    /**
     * The column name
     */
    private String name;

    /**
     * If this column is the primary column
     */
    private boolean primary = false;

    /**
     * The table this column is assigned to
     */
    private Table table;

    /**
     * The column type (INTEGER, BLOB, etc)
     */
    private String type;

    /**
     * The default value
     */
    private String defaultValue;

    public Column(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue == null ? "" : defaultValue;
    }

    public String getType() {
        return type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
        autoIncrement = primary;
    }

    public void setTable(Table table) {
        if (this.table == null) {
            this.table = table;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean shouldAutoIncrement() {
        return autoIncrement;
    }
}
