package com.griefcraft.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * A result set that will close the parent PreparedStatement and Connection
 */
public class AutoClosingResultSet implements ResultSet {

    /**
     * The statement this result set belongs to
     */
    private final PreparedStatement statement;

    /**
     * The {@link ResultSet} to proxy
     */
    private final ResultSet set;

    public AutoClosingResultSet(PreparedStatement statement, ResultSet set) {
        this.statement = statement;
        this.set = set;
    }

    public void close() throws SQLException {
        set.close();

        if (statement != null) {
            statement.close();
        }
    }

    public boolean next() throws SQLException {
        return set.next();
    }

    public InputStream getAsciiStream(int i) throws SQLException {
        return set.getAsciiStream(i);
    }

    public void updateFloat(String s, float v) throws SQLException {
        set.updateFloat(s, v);
    }

    public long getLong(int i) throws SQLException {
        return set.getLong(i);
    }

    public void updateClob(String s, Clob clob) throws SQLException {
        set.updateClob(s, clob);
    }

    public Timestamp getTimestamp(String s, Calendar calendar) throws SQLException {
        return set.getTimestamp(s, calendar);
    }

    public Clob getClob(int i) throws SQLException {
        return set.getClob(i);
    }

    public void insertRow() throws SQLException {
        set.insertRow();
    }

    public void moveToInsertRow() throws SQLException {
        set.moveToInsertRow();
    }

    public void updateClob(String s, Reader reader, long l) throws SQLException {
        set.updateClob(s, reader, l);
    }

    public boolean isBeforeFirst() throws SQLException {
        return set.isBeforeFirst();
    }

    public void updateBlob(String s, InputStream inputStream) throws SQLException {
        set.updateBlob(s, inputStream);
    }

    public Timestamp getTimestamp(int i, Calendar calendar) throws SQLException {
        return set.getTimestamp(i, calendar);
    }

    public void updateBinaryStream(int i, InputStream inputStream) throws SQLException {
        set.updateBinaryStream(i, inputStream);
    }

    public byte[] getBytes(String s) throws SQLException {
        return set.getBytes(s);
    }

    public boolean isAfterLast() throws SQLException {
        return set.isAfterLast();
    }

    public void updateDouble(String s, double v) throws SQLException {
        set.updateDouble(s, v);
    }

    public void updateSQLXML(String s, SQLXML sqlxml) throws SQLException {
        set.updateSQLXML(s, sqlxml);
    }

    public void updateBlob(int i, InputStream inputStream, long l) throws SQLException {
        set.updateBlob(i, inputStream, l);
    }

    public Object getObject(int i, Map<String, Class<?>> stringClassMap) throws SQLException {
        return set.getObject(i, stringClassMap);
    }

    public void setFetchDirection(int i) throws SQLException {
        set.setFetchDirection(i);
    }

    public void updateAsciiStream(int i, InputStream inputStream) throws SQLException {
        set.updateAsciiStream(i, inputStream);
    }

    public int getInt(String s) throws SQLException {
        return set.getInt(s);
    }

    public float getFloat(int i) throws SQLException {
        return set.getFloat(i);
    }

    public void updateInt(int i, int i2) throws SQLException {
        set.updateInt(i, i2);
    }

    public void updateTime(String s, Time time) throws SQLException {
        set.updateTime(s, time);
    }

    public int getConcurrency() throws SQLException {
        return set.getConcurrency();
    }

    public Ref getRef(String s) throws SQLException {
        return set.getRef(s);
    }

    public BigDecimal getBigDecimal(String s) throws SQLException {
        return set.getBigDecimal(s);
    }

    public Object getObject(int i) throws SQLException {
        return set.getObject(i);
    }

    public NClob getNClob(String s) throws SQLException {
        return set.getNClob(s);
    }

    public void updateNString(int i, String s) throws SQLException {
        set.updateNString(i, s);
    }

    public RowId getRowId(int i) throws SQLException {
        return set.getRowId(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return set.getBlob(i);
    }

    public SQLWarning getWarnings() throws SQLException {
        return set.getWarnings();
    }

    public void updateInt(String s, int i) throws SQLException {
        set.updateInt(s, i);
    }

    public void updateNClob(int i, NClob nClob) throws SQLException {
        set.updateNClob(i, nClob);
    }

    public Statement getStatement() throws SQLException {
        return set.getStatement();
    }

    public Timestamp getTimestamp(String s) throws SQLException {
        return set.getTimestamp(s);
    }

    public boolean isLast() throws SQLException {
        return set.isLast();
    }

    public boolean rowUpdated() throws SQLException {
        return set.rowUpdated();
    }

    public int getHoldability() throws SQLException {
        return set.getHoldability();
    }

    public void updateRef(String s, Ref ref) throws SQLException {
        set.updateRef(s, ref);
    }

    public void updateLong(String s, long l) throws SQLException {
        set.updateLong(s, l);
    }

    public void updateBinaryStream(String s, InputStream inputStream) throws SQLException {
        set.updateBinaryStream(s, inputStream);
    }

    public void deleteRow() throws SQLException {
        set.deleteRow();
    }

    public void updateTimestamp(String s, Timestamp timestamp) throws SQLException {
        set.updateTimestamp(s, timestamp);
    }

    public void updateNCharacterStream(int i, Reader reader, long l) throws SQLException {
        set.updateNCharacterStream(i, reader, l);
    }

    public byte[] getBytes(int i) throws SQLException {
        return set.getBytes(i);
    }

    public Time getTime(int i) throws SQLException {
        return set.getTime(i);
    }

    public void updateSQLXML(int i, SQLXML sqlxml) throws SQLException {
        set.updateSQLXML(i, sqlxml);
    }

    public void updateCharacterStream(String s, Reader reader, long l) throws SQLException {
        set.updateCharacterStream(s, reader, l);
    }

    public Date getDate(int i) throws SQLException {
        return set.getDate(i);
    }

    public void updateFloat(int i, float v) throws SQLException {
        set.updateFloat(i, v);
    }

    public void updateBinaryStream(String s, InputStream inputStream, long l) throws SQLException {
        set.updateBinaryStream(s, inputStream, l);
    }

    public InputStream getBinaryStream(String s) throws SQLException {
        return set.getBinaryStream(s);
    }

    public String getString(String s) throws SQLException {
        return set.getString(s);
    }

    public void updateBytes(int i, byte[] bytes) throws SQLException {
        set.updateBytes(i, bytes);
    }

    public Time getTime(String s, Calendar calendar) throws SQLException {
        return set.getTime(s, calendar);
    }

    public String getString(int i) throws SQLException {
        return set.getString(i);
    }

    public Reader getNCharacterStream(int i) throws SQLException {
        return set.getNCharacterStream(i);
    }

    public void updateDate(String s, Date date) throws SQLException {
        set.updateDate(s, date);
    }

    public void updateLong(int i, long l) throws SQLException {
        set.updateLong(i, l);
    }

    public void updateCharacterStream(String s, Reader reader) throws SQLException {
        set.updateCharacterStream(s, reader);
    }

    public BigDecimal getBigDecimal(int i, int i2) throws SQLException {
        return set.getBigDecimal(i, i2);
    }

    public Time getTime(int i, Calendar calendar) throws SQLException {
        return set.getTime(i, calendar);
    }

    public void updateAsciiStream(String s, InputStream inputStream, long l) throws SQLException {
        set.updateAsciiStream(s, inputStream, l);
    }

    public void updateTime(int i, Time time) throws SQLException {
        set.updateTime(i, time);
    }

    public void updateNull(int i) throws SQLException {
        set.updateNull(i);
    }

    public void updateNull(String s) throws SQLException {
        set.updateNull(s);
    }

    public String getNString(int i) throws SQLException {
        return set.getNString(i);
    }

    public boolean isClosed() throws SQLException {
        return set.isClosed();
    }

    public Array getArray(String s) throws SQLException {
        return set.getArray(s);
    }

    public void updateObject(String s, Object o, int i) throws SQLException {
        set.updateObject(s, o, i);
    }

    public void updateBlob(int i, InputStream inputStream) throws SQLException {
        set.updateBlob(i, inputStream);
    }

    public void updateRowId(int i, RowId rowId) throws SQLException {
        set.updateRowId(i, rowId);
    }

    public void setFetchSize(int i) throws SQLException {
        set.setFetchSize(i);
    }

    public void updateClob(int i, Reader reader) throws SQLException {
        set.updateClob(i, reader);
    }

    public void beforeFirst() throws SQLException {
        set.beforeFirst();
    }

    public void moveToCurrentRow() throws SQLException {
        set.moveToCurrentRow();
    }

    public void updateAsciiStream(String s, InputStream inputStream, int i) throws SQLException {
        set.updateAsciiStream(s, inputStream, i);
    }

    public void afterLast() throws SQLException {
        set.afterLast();
    }

    public Clob getClob(String s) throws SQLException {
        return set.getClob(s);
    }

    public void updateObject(String s, Object o) throws SQLException {
        set.updateObject(s, o);
    }

    public void updateBinaryStream(int i, InputStream inputStream, long l) throws SQLException {
        set.updateBinaryStream(i, inputStream, l);
    }

    public String getNString(String s) throws SQLException {
        return set.getNString(s);
    }

    public void cancelRowUpdates() throws SQLException {
        set.cancelRowUpdates();
    }

    public BigDecimal getBigDecimal(String s, int i) throws SQLException {
        return set.getBigDecimal(s, i);
    }

    public void updateTimestamp(int i, Timestamp timestamp) throws SQLException {
        set.updateTimestamp(i, timestamp);
    }

    public void updateNCharacterStream(String s, Reader reader) throws SQLException {
        set.updateNCharacterStream(s, reader);
    }

    public URL getURL(String s) throws SQLException {
        return set.getURL(s);
    }

    public int getFetchSize() throws SQLException {
        return set.getFetchSize();
    }

    public int getRow() throws SQLException {
        return set.getRow();
    }

    public long getLong(String s) throws SQLException {
        return set.getLong(s);
    }

    public boolean rowInserted() throws SQLException {
        return set.rowInserted();
    }

    public void updateCharacterStream(String s, Reader reader, int i) throws SQLException {
        set.updateCharacterStream(s, reader, i);
    }

    public void updateNClob(int i, Reader reader, long l) throws SQLException {
        set.updateNClob(i, reader, l);
    }

    public void updateNString(String s, String s2) throws SQLException {
        set.updateNString(s, s2);
    }

    public byte getByte(String s) throws SQLException {
        return set.getByte(s);
    }

    public void updateRowId(String s, RowId rowId) throws SQLException {
        set.updateRowId(s, rowId);
    }

    public Date getDate(String s, Calendar calendar) throws SQLException {
        return set.getDate(s, calendar);
    }

    public double getDouble(int i) throws SQLException {
        return set.getDouble(i);
    }

    public boolean first() throws SQLException {
        return set.first();
    }

    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return set.unwrap(tClass);
    }

    public void updateArray(String s, Array array) throws SQLException {
        set.updateArray(s, array);
    }

    public NClob getNClob(int i) throws SQLException {
        return set.getNClob(i);
    }

    public Blob getBlob(String s) throws SQLException {
        return set.getBlob(s);
    }

    public URL getURL(int i) throws SQLException {
        return set.getURL(i);
    }

    public Date getDate(int i, Calendar calendar) throws SQLException {
        return set.getDate(i, calendar);
    }

    public void updateClob(String s, Reader reader) throws SQLException {
        set.updateClob(s, reader);
    }

    public void updateCharacterStream(int i, Reader reader) throws SQLException {
        set.updateCharacterStream(i, reader);
    }

    public BigDecimal getBigDecimal(int i) throws SQLException {
        return set.getBigDecimal(i);
    }

    public Object getObject(String s, Map<String, Class<?>> stringClassMap) throws SQLException {
        return set.getObject(s, stringClassMap);
    }

    public Reader getCharacterStream(String s) throws SQLException {
        return set.getCharacterStream(s);
    }

    public SQLXML getSQLXML(int i) throws SQLException {
        return set.getSQLXML(i);
    }

    public void updateClob(int i, Clob clob) throws SQLException {
        set.updateClob(i, clob);
    }

    public void updateBinaryStream(int i, InputStream inputStream, int i2) throws SQLException {
        set.updateBinaryStream(i, inputStream, i2);
    }

    public void updateBinaryStream(String s, InputStream inputStream, int i) throws SQLException {
        set.updateBinaryStream(s, inputStream, i);
    }

    public boolean previous() throws SQLException {
        return set.previous();
    }

    public void updateString(String s, String s2) throws SQLException {
        set.updateString(s, s2);
    }

    public void updateAsciiStream(String s, InputStream inputStream) throws SQLException {
        set.updateAsciiStream(s, inputStream);
    }

    public void updateArray(int i, Array array) throws SQLException {
        set.updateArray(i, array);
    }

    public void updateNClob(String s, Reader reader) throws SQLException {
        set.updateNClob(s, reader);
    }

    public void updateBoolean(int i, boolean b) throws SQLException {
        set.updateBoolean(i, b);
    }

    public double getDouble(String s) throws SQLException {
        return set.getDouble(s);
    }

    public byte getByte(int i) throws SQLException {
        return set.getByte(i);
    }

    public String getCursorName() throws SQLException {
        return set.getCursorName();
    }

    public InputStream getBinaryStream(int i) throws SQLException {
        return set.getBinaryStream(i);
    }

    public void updateShort(int i, short i2) throws SQLException {
        set.updateShort(i, i2);
    }

    public int findColumn(String s) throws SQLException {
        return set.findColumn(s);
    }

    public void updateClob(int i, Reader reader, long l) throws SQLException {
        set.updateClob(i, reader, l);
    }

    public void updateBlob(String s, Blob blob) throws SQLException {
        set.updateBlob(s, blob);
    }

    public void updateDouble(int i, double v) throws SQLException {
        set.updateDouble(i, v);
    }

    public void updateByte(String s, byte b) throws SQLException {
        set.updateByte(s, b);
    }

    public boolean wasNull() throws SQLException {
        return set.wasNull();
    }

    public RowId getRowId(String s) throws SQLException {
        return set.getRowId(s);
    }

    public Time getTime(String s) throws SQLException {
        return set.getTime(s);
    }

    public float getFloat(String s) throws SQLException {
        return set.getFloat(s);
    }

    public void updateBlob(String s, InputStream inputStream, long l) throws SQLException {
        set.updateBlob(s, inputStream, l);
    }

    public void updateAsciiStream(int i, InputStream inputStream, int i2) throws SQLException {
        set.updateAsciiStream(i, inputStream, i2);
    }

    public void updateCharacterStream(int i, Reader reader, long l) throws SQLException {
        set.updateCharacterStream(i, reader, l);
    }

    public boolean isFirst() throws SQLException {
        return set.isFirst();
    }

    public Reader getNCharacterStream(String s) throws SQLException {
        return set.getNCharacterStream(s);
    }

    public boolean absolute(int i) throws SQLException {
        return set.absolute(i);
    }

    public int getType() throws SQLException {
        return set.getType();
    }

    public InputStream getAsciiStream(String s) throws SQLException {
        return set.getAsciiStream(s);
    }

    public boolean rowDeleted() throws SQLException {
        return set.rowDeleted();
    }

    public void refreshRow() throws SQLException {
        set.refreshRow();
    }

    public void updateRow() throws SQLException {
        set.updateRow();
    }

    public void updateNClob(int i, Reader reader) throws SQLException {
        set.updateNClob(i, reader);
    }

    public void updateByte(int i, byte b) throws SQLException {
        set.updateByte(i, b);
    }

    public void updateNCharacterStream(String s, Reader reader, long l) throws SQLException {
        set.updateNCharacterStream(s, reader, l);
    }

    public void updateObject(int i, Object o, int i2) throws SQLException {
        set.updateObject(i, o, i2);
    }

    public void updateBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
        set.updateBigDecimal(i, bigDecimal);
    }

    public boolean getBoolean(String s) throws SQLException {
        return set.getBoolean(s);
    }

    public boolean last() throws SQLException {
        return set.last();
    }

    public void updateBoolean(String s, boolean b) throws SQLException {
        set.updateBoolean(s, b);
    }

    public void updateShort(String s, short i) throws SQLException {
        set.updateShort(s, i);
    }

    public void updateCharacterStream(int i, Reader reader, int i2) throws SQLException {
        set.updateCharacterStream(i, reader, i2);
    }

    public void updateObject(int i, Object o) throws SQLException {
        set.updateObject(i, o);
    }

    public void updateString(int i, String s) throws SQLException {
        set.updateString(i, s);
    }

    public void updateNClob(String s, Reader reader, long l) throws SQLException {
        set.updateNClob(s, reader, l);
    }

    public boolean relative(int i) throws SQLException {
        return set.relative(i);
    }

    public boolean getBoolean(int i) throws SQLException {
        return set.getBoolean(i);
    }

    public int getInt(int i) throws SQLException {
        return set.getInt(i);
    }

    public short getShort(String s) throws SQLException {
        return set.getShort(s);
    }

    public void updateBigDecimal(String s, BigDecimal bigDecimal) throws SQLException {
        set.updateBigDecimal(s, bigDecimal);
    }

    public void updateBytes(String s, byte[] bytes) throws SQLException {
        set.updateBytes(s, bytes);
    }

    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return set.isWrapperFor(aClass);
    }

    public InputStream getUnicodeStream(String s) throws SQLException {
        return set.getUnicodeStream(s);
    }

    public void updateDate(int i, Date date) throws SQLException {
        set.updateDate(i, date);
    }

    public void updateAsciiStream(int i, InputStream inputStream, long l) throws SQLException {
        set.updateAsciiStream(i, inputStream, l);
    }

    public Reader getCharacterStream(int i) throws SQLException {
        return set.getCharacterStream(i);
    }

    public void updateBlob(int i, Blob blob) throws SQLException {
        set.updateBlob(i, blob);
    }

    public int getFetchDirection() throws SQLException {
        return set.getFetchDirection();
    }

    public InputStream getUnicodeStream(int i) throws SQLException {
        return set.getUnicodeStream(i);
    }

    public Object getObject(String s) throws SQLException {
        return set.getObject(s);
    }

    public void clearWarnings() throws SQLException {
        set.clearWarnings();
    }

    public Timestamp getTimestamp(int i) throws SQLException {
        return set.getTimestamp(i);
    }

    public short getShort(int i) throws SQLException {
        return set.getShort(i);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return set.getMetaData();
    }

    public void updateNClob(String s, NClob nClob) throws SQLException {
        set.updateNClob(s, nClob);
    }

    public void updateRef(int i, Ref ref) throws SQLException {
        set.updateRef(i, ref);
    }

    public Array getArray(int i) throws SQLException {
        return set.getArray(i);
    }

    public void updateNCharacterStream(int i, Reader reader) throws SQLException {
        set.updateNCharacterStream(i, reader);
    }

    public Ref getRef(int i) throws SQLException {
        return set.getRef(i);
    }

    public SQLXML getSQLXML(String s) throws SQLException {
        return set.getSQLXML(s);
    }

    public Date getDate(String s) throws SQLException {
        return set.getDate(s);
    }
}
