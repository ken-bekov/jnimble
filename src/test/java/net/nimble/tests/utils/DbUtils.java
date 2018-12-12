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

package net.nimble.tests.utils;

import net.nimble.tests.config.TestConfig;
import net.nimble.tests.utils.providers.MariaDbDataSourceProvider;
import net.nimble.tests.utils.providers.MysqlDataSourceProvider;
import net.nimble.tests.utils.providers.PostgresDataSourceProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbUtils {

    private static DataSource dataSource;

    public static void deleteTestPeople(DataSource dataSource, String firstName, String lastName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement =
                    connection.prepareStatement("delete from person where first_name=? and last_name=?");
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.executeUpdate();
        }
    }

    public static void deleteTestPeople(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement =
                    connection.prepareStatement("delete from person where last_name=?");
            statement.setString(1, "Lannister");
            statement.executeUpdate();
        }
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            switch (TestConfig.defaultDialect) {
                case MYSQL:
                    dataSource = new MysqlDataSourceProvider().createDataSource(TestConfig.serverName,
                            TestConfig.user, TestConfig.password, TestConfig.database);
                    break;
                case POSTGRES:
                    dataSource = new PostgresDataSourceProvider().createDataSource(TestConfig.serverName,
                            TestConfig.user, TestConfig.password, TestConfig.database);
                    break;
                case MARIADB:
                    dataSource = new MariaDbDataSourceProvider().createDataSource(TestConfig.serverName,
                            TestConfig.user, TestConfig.password, TestConfig.database);
                default:
                    dataSource = new MysqlDataSourceProvider().createDataSource(TestConfig.serverName,
                            TestConfig.user, TestConfig.password, TestConfig.database);
            }
        }
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        DbUtils.dataSource = dataSource;
    }
}
