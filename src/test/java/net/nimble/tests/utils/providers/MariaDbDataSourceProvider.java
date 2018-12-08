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

package net.nimble.tests.utils.providers;

import net.nimble.tests.config.TestConfig;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import javax.sql.DataSource;
import java.sql.SQLException;

public class MariaDbDataSourceProvider implements DataSourceProvider {
    @Override
    public DataSource createDataSource(String serverName, String user, String password, String database) {
        try {
            MariaDbPoolDataSource pool = new MariaDbPoolDataSource();
            pool.setServerName(serverName);
            pool.setPort(3307);
            pool.setUser(user);
            pool.setPassword(password);
            pool.setDatabaseName(database);
            return pool;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
