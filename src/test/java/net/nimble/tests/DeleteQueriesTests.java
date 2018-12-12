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

package net.nimble.tests;

import net.nimble.NbConnection;
import net.nimble.Nimble;
import net.nimble.tests.config.TestConfig;
import net.nimble.tests.entities.Person;
import net.nimble.tests.utils.DbUtils;
import net.nimble.tests.utils.PeopleFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteQueriesTests {

    private DataSource dataSource = null;
    private Nimble nimble = null;

    @Before
    public void clearTestData() throws SQLException {
        DataSource dataSource = DbUtils.getDataSource();
        DbUtils.deleteTestPeople(dataSource);
        nimble = new Nimble(dataSource, TestConfig.defaultDialect);
    }

    @Test
    public void deleteObjectQuery() throws SQLException {
        Person cercei = PeopleFactory.createCercei();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercei);
        }
        checkIdInserted(cercei.getId());

        try (NbConnection connection = nimble.getConnection()) {
            connection.delete(cercei);
        }
        checkIdDeleted(cercei.getId());
    }

    private void checkIdDeleted(int id) throws SQLException {
        try (NbConnection connection = nimble.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from person where id=?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            int counter = 0;
            while(resultSet.next()){
                counter++;
            }
            Assert.assertEquals(0, counter);
        }
    }

    private void checkIdInserted(int id) throws SQLException {
        try (NbConnection connection = nimble.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from person where id=?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            int counter = 0;
            while(resultSet.next()){
                counter++;
            }
            Assert.assertEquals(1, counter);
        }
    }

    @Test
    public void deleteByIdQuery() throws SQLException {
        Person cercei = PeopleFactory.createCercei();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercei);
        }
        checkIdInserted(cercei.getId());

        try (NbConnection connection = nimble.getConnection()) {
            connection.delete(cercei.getId(), Person.class);
        }
        checkIdDeleted(cercei.getId());
    }
}
