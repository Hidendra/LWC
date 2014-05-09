/*
 * Copyright (c) 2011-2013 Tyler Blair
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

/*
 * Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 */
/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.getlwc.db.jdbc;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Tool to run database scripts
 */
public class JDBCScriptRunner {

    private static final String DEFAULT_DELIMITER = ";";

    private Connection connection;

    private boolean stopOnError;

    private boolean autoCommit;

    private PrintWriter logWriter = new PrintWriter(System.out);

    private PrintWriter errorLogWriter = new PrintWriter(System.err);

    private String delimiter = DEFAULT_DELIMITER;

    private boolean fullLineDelimiter = false;

    /**
     * Default constructor
     */
    public JDBCScriptRunner(Connection connection, boolean autoCommit,
                            boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Setter for logWriter property
     *
     * @param logWriter - the new value of the logWriter property
     */
    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * Setter for errorLogWriter property
     *
     * @param errorLogWriter - the new value of the errorLogWriter property
     */
    public void setErrorLogWriter(PrintWriter errorLogWriter) {
        this.errorLogWriter = errorLogWriter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader - the source of the script
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    connection.setAutoCommit(this.autoCommit);
                }
                runScript(connection, reader);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn   - the connection to use for the script
     * @param reader - the source of the script
     * @throws SQLException if any SQL errors occur
     * @throws IOException  if there is an error reading from the Reader
     */
    private void runScript(Connection conn, Reader reader) throws IOException,
            SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    println(trimmedLine);
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {
                    command.append(line.substring(0, line
                            .lastIndexOf(getDelimiter())));
                    command.append(" ");
                    Statement statement = conn.createStatement();

                    println(command);

                    boolean hasResults = false;
                    if (stopOnError) {
                        hasResults = statement.execute(command.toString());
                    } else {
                        try {
                            statement.execute(command.toString());
                        } catch (SQLException e) {
                            e.fillInStackTrace();
                            printlnError("Error executing: " + command);
                            printlnError(e);
                        }
                    }

                    if (autoCommit && !conn.getAutoCommit()) {
                        conn.commit();
                    }

                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        // Ignore to workaround a bug in Jakarta DBCP
                    }
                    Thread.yield();
                } else {
                    command.append(line);
                    command.append(" ");
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } finally {
            conn.rollback();
            flush();
        }
    }

    private String getDelimiter() {
        return delimiter;
    }

    private void print(Object o) {
        if (logWriter != null) {
            System.out.print(o);
        }
    }

    private void println(Object o) {
        if (logWriter != null) {
            logWriter.println(o);
        }
    }

    private void printlnError(Object o) {
        if (errorLogWriter != null) {
            errorLogWriter.println(o);
        }
    }

    private void flush() {
        if (logWriter != null) {
            logWriter.flush();
        }
        if (errorLogWriter != null) {
            errorLogWriter.flush();
        }
    }
}