/*
 * MIT License
 *
 * Copyright (c) 2018. Saken Sultanbekov, ken.bekov@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.nimble;

import net.nimble.conversion.ConverterManager;
import net.nimble.conversion.ConverterManagerImpl;
import net.nimble.exceptions.NimbleSQLException;
import net.nimble.meta.mappers.ObjectMapperFactory;
import net.nimble.meta.extracts.ValueExtractFactory;
import net.nimble.sql.SqlDialect;
import net.nimble.sql.readers.ResultSetReaderFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Nimble {

    private final DataSource dataSource;
    private final NbContext context;

    public Nimble(DataSource dataSource, SqlDialect dialect) {
        this.dataSource = dataSource;
        this.context = new NbContext();
        this.context.setDialect(dialect);
        fillContext();
    }

    private void fillContext() {
        context.setConverterManager(new ConverterManagerImpl());
        context.setObjectMapperFactory(new ObjectMapperFactory(context.getConverterManager()));
        context.setValueExtractFactory(new ValueExtractFactory());
        context.setResultSetReader(ResultSetReaderFactory.getReader(context.getDialect()));
    }

    public NbConnection getConnection() {
        try {
            return new NbConnection(dataSource.getConnection(), context);
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public NbConnection getConnection(boolean autoCommit) {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(autoCommit);
            return new NbConnection(connection, context);
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public NbConnection getConnection(String userName, String password) {
        try {
            return new NbConnection(dataSource.getConnection(userName, password), context);
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public NbConnection getConnection(String userName, String password, boolean autoCommit) {
        try {
            Connection connection = dataSource.getConnection(userName, password);
            connection.setAutoCommit(autoCommit);
            return new NbConnection(connection, context);
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public ConverterManager getConverterManager() {
        return context.getConverterManager();
    }

    public NbRow createRow(String[] columnNames, Object[] data) {
        return new NbRow(columnNames, data, context.getConverterManager());
    }

    public NbRow createRow(String[] columnNames) {
        return new NbRow(columnNames, context.getConverterManager());
    }
}
