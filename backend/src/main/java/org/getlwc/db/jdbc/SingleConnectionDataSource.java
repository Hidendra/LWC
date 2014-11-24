package org.getlwc.db.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Maintains only a single connection from a given data source.
 */
public class SingleConnectionDataSource implements DataSource {

    private DataSource source;
    private Connection connection;

    public SingleConnectionDataSource(DataSource source) {
        this.source = source;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = source.getConnection();
        }

        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = source.getConnection();
        }

        return connection;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return source.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        source.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        source.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return source.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return source.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return source.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return source.isWrapperFor(iface);
    }

}
