/*
 * Copyright (c) 2011, 2012, Tyler Blair
 * All rights reserved.
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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * A prepared statement that will close the parent {@link Connection} object once it is closed
 */
public class AutoClosingPreparedStatement implements PreparedStatement {

    /**
     * The connection this statement is bound to
     */
    private final Connection connection;

    /**
     * The prepared statement that will be a single use
     */
    private final PreparedStatement statement;

    public AutoClosingPreparedStatement(Connection connection, PreparedStatement statement) {
        this.connection = connection;
        this.statement = statement;
    }

    public void close() throws SQLException {
        statement.close();

        if (connection != null) {
            connection.close();
        }
    }

    public int executeUpdate() throws SQLException {
        return executeUpdate(true);
    }

    public int executeUpdate(boolean closeStatement) throws SQLException {
        int ret = statement.executeUpdate();
        if (closeStatement) {
            close();
        }
        return ret;
    }

    public ResultSet executeQuery() throws SQLException {
        return new AutoClosingResultSet(this, statement.executeQuery());
    }

    public void setAsciiStream(int i, InputStream inputStream, int i2) throws SQLException {
        statement.setAsciiStream(i, inputStream, i2);
    }

    public boolean isPoolable() throws SQLException {
        return statement.isPoolable();
    }

    public void setURL(int i, URL url) throws SQLException {
        statement.setURL(i, url);
    }

    public void setMaxFieldSize(int i) throws SQLException {
        statement.setMaxFieldSize(i);
    }

    public boolean isClosed() throws SQLException {
        return statement.isClosed();
    }

    public void setMaxRows(int i) throws SQLException {
        statement.setMaxRows(i);
    }

    public void setBinaryStream(int i, InputStream inputStream, long l) throws SQLException {
        statement.setBinaryStream(i, inputStream, l);
    }

    public void setFloat(int i, float v) throws SQLException {
        statement.setFloat(i, v);
    }

    public void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
        statement.setBigDecimal(i, bigDecimal);
    }

    public void setBinaryStream(int i, InputStream inputStream) throws SQLException {
        statement.setBinaryStream(i, inputStream);
    }

    public void setBlob(int i, InputStream inputStream) throws SQLException {
        statement.setBlob(i, inputStream);
    }

    public void setObject(int i, Object o, int i2, int i3) throws SQLException {
        statement.setObject(i, o, i2, i3);
    }

    public boolean execute(String s, int i) throws SQLException {
        return statement.execute(s, i);
    }

    public void setArray(int i, Array array) throws SQLException {
        statement.setArray(i, array);
    }

    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }

    public int getResultSetHoldability() throws SQLException {
        return statement.getResultSetHoldability();
    }

    public void setShort(int i, short i2) throws SQLException {
        statement.setShort(i, i2);
    }

    public void setAsciiStream(int i, InputStream inputStream) throws SQLException {
        statement.setAsciiStream(i, inputStream);
    }

    public void setNull(int i, int i2, String s) throws SQLException {
        statement.setNull(i, i2, s);
    }

    public void setUnicodeStream(int i, InputStream inputStream, int i2) throws SQLException {
        statement.setUnicodeStream(i, inputStream, i2);
    }

    public int executeUpdate(String s) throws SQLException {
        return statement.executeUpdate(s);
    }

    public void setCharacterStream(int i, Reader reader, long l) throws SQLException {
        statement.setCharacterStream(i, reader, l);
    }

    public void setRef(int i, Ref ref) throws SQLException {
        statement.setRef(i, ref);
    }

    public void setNCharacterStream(int i, Reader reader, long l) throws SQLException {
        statement.setNCharacterStream(i, reader, l);
    }

    public void setDouble(int i, double v) throws SQLException {
        statement.setDouble(i, v);
    }

    public void setClob(int i, Reader reader) throws SQLException {
        statement.setClob(i, reader);
    }

    public void setNull(int i, int i2) throws SQLException {
        statement.setNull(i, i2);
    }

    public void setBytes(int i, byte[] bytes) throws SQLException {
        statement.setBytes(i, bytes);
    }

    public void setBinaryStream(int i, InputStream inputStream, int i2) throws SQLException {
        statement.setBinaryStream(i, inputStream, i2);
    }

    public boolean execute(String s, int[] ints) throws SQLException {
        return statement.execute(s, ints);
    }

    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }

    public void setDate(int i, Date date) throws SQLException {
        statement.setDate(i, date);
    }

    public void setClob(int i, Reader reader, long l) throws SQLException {
        statement.setClob(i, reader, l);
    }

    public void setDate(int i, Date date, Calendar calendar) throws SQLException {
        statement.setDate(i, date, calendar);
    }

    public void setNClob(int i, Reader reader) throws SQLException {
        statement.setNClob(i, reader);
    }

    public void clearParameters() throws SQLException {
        statement.clearParameters();
    }

    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }

    public int executeUpdate(String s, String[] strings) throws SQLException {
        return statement.executeUpdate(s, strings);
    }

    public void setPoolable(boolean b) throws SQLException {
        statement.setPoolable(b);
    }

    public void setTime(int i, Time time) throws SQLException {
        statement.setTime(i, time);
    }

    public boolean execute(String s, String[] strings) throws SQLException {
        return statement.execute(s, strings);
    }

    public int executeUpdate(String s, int[] ints) throws SQLException {
        return statement.executeUpdate(s, ints);
    }

    public boolean execute() throws SQLException {
        return statement.execute();
    }

    public void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException {
        statement.setTimestamp(i, timestamp, calendar);
    }

    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }

    public void setLong(int i, long l) throws SQLException {
        statement.setLong(i, l);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return statement.getParameterMetaData();
    }

    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return statement.unwrap(tClass);
    }

    public void setTime(int i, Time time, Calendar calendar) throws SQLException {
        statement.setTime(i, time, calendar);
    }

    public void setClob(int i, Clob clob) throws SQLException {
        statement.setClob(i, clob);
    }

    public void setBoolean(int i, boolean b) throws SQLException {
        statement.setBoolean(i, b);
    }

    public void addBatch() throws SQLException {
        statement.addBatch();
    }

    public void setSQLXML(int i, SQLXML sqlxml) throws SQLException {
        statement.setSQLXML(i, sqlxml);
    }

    public void setTimestamp(int i, Timestamp timestamp) throws SQLException {
        statement.setTimestamp(i, timestamp);
    }

    public int getFetchDirection() throws SQLException {
        return statement.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    public void setBlob(int i, InputStream inputStream, long l) throws SQLException {
        statement.setBlob(i, inputStream, l);
    }

    public void setString(int i, String s) throws SQLException {
        statement.setString(i, s);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return statement.getMetaData();
    }

    public void setNString(int i, String s) throws SQLException {
        statement.setNString(i, s);
    }

    public void setObject(int i, Object o) throws SQLException {
        statement.setObject(i, o);
    }

    public void setCursorName(String s) throws SQLException {
        statement.setCursorName(s);
    }

    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }

    public void cancel() throws SQLException {
        statement.cancel();
    }

    public void setFetchDirection(int i) throws SQLException {
        statement.setFetchDirection(i);
    }

    public void setNClob(int i, Reader reader, long l) throws SQLException {
        statement.setNClob(i, reader, l);
    }

    public void addBatch(String s) throws SQLException {
        statement.addBatch(s);
    }

    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }

    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }

    public void setObject(int i, Object o, int i2) throws SQLException {
        statement.setObject(i, o, i2);
    }

    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }

    public void setCharacterStream(int i, Reader reader, int i2) throws SQLException {
        statement.setCharacterStream(i, reader, i2);
    }

    public void setEscapeProcessing(boolean b) throws SQLException {
        statement.setEscapeProcessing(b);
    }

    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    public void setAsciiStream(int i, InputStream inputStream, long l) throws SQLException {
        statement.setAsciiStream(i, inputStream, l);
    }

    public void setNClob(int i, NClob nClob) throws SQLException {
        statement.setNClob(i, nClob);
    }

    public boolean getMoreResults(int i) throws SQLException {
        return statement.getMoreResults(i);
    }

    public void setFetchSize(int i) throws SQLException {
        statement.setFetchSize(i);
    }

    public ResultSet executeQuery(String s) throws SQLException {
        return statement.executeQuery(s);
    }

    public void setByte(int i, byte b) throws SQLException {
        statement.setByte(i, b);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }

    public void setBlob(int i, Blob blob) throws SQLException {
        statement.setBlob(i, blob);
    }

    public boolean execute(String s) throws SQLException {
        return statement.execute(s);
    }

    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return statement.isWrapperFor(aClass);
    }

    public void setCharacterStream(int i, Reader reader) throws SQLException {
        statement.setCharacterStream(i, reader);
    }

    public void setNCharacterStream(int i, Reader reader) throws SQLException {
        statement.setNCharacterStream(i, reader);
    }

    public void setInt(int i, int i2) throws SQLException {
        statement.setInt(i, i2);
    }

    public void setRowId(int i, RowId rowId) throws SQLException {
        statement.setRowId(i, rowId);
    }

    public int executeUpdate(String s, int i) throws SQLException {
        return statement.executeUpdate(s, i);
    }

    public void setQueryTimeout(int i) throws SQLException {
        statement.setQueryTimeout(i);
    }

    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }
}
