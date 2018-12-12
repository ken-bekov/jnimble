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

import net.nimble.NbParams;
import net.nimble.NbQuery;
import net.nimble.Nimble;
import net.nimble.NbConnection;
import net.nimble.tests.config.TestConfig;
import net.nimble.tests.entities.Gender;
import net.nimble.tests.entities.Person;
import net.nimble.tests.utils.DbUtils;
import net.nimble.tests.utils.PeopleFactory;
import org.junit.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InsertQueriesTests {

    private DataSource dataSource = null;
    private Nimble nimble = null;

    @Before
    public void clearTestData() throws SQLException {
        dataSource = DbUtils.getDataSource();
        Person tyrion = PeopleFactory.createTyrion();
        DbUtils.deleteTestPeople(dataSource, tyrion.getFirstName(), tyrion.getLastName());
        nimble = new Nimble(dataSource, TestConfig.defaultDialect);
    }

    @Test
    public void beanSqlInsert() throws SQLException {
        Person tyrion = PeopleFactory.createTyrion();

        try (NbConnection connection = nimble.getConnection()) {
            NbQuery query = connection.query("insert into person " +
                            "(first_name, last_name, birth_date, gender, weight, height, cash_amount) values " +
                            "(:firstName, :lastName, :birthDate, :gender, :weight, :height, :cashAmount)")
                    .addParamsBean(tyrion);
            int affectedRows = query.execute();
            tyrion.setId(query.getGeneratedKey(Integer.class));

            Assert.assertEquals(1, affectedRows);
            Assert.assertTrue(tyrion.getId() > 0);
        }

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from person where id=?");
            preparedStatement.setInt(1, tyrion.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Assert.assertEquals(tyrion.getHeight(), resultSet.getFloat("height"), 0);
                Assert.assertEquals(tyrion.getCashAmount(), resultSet.getDouble("cash_amount"), 0);
            }
        }
    }

    @Test
    public void beanSimpleInsert() throws SQLException {
        Person tyrion = PeopleFactory.createTyrion();

        try (NbConnection connection = nimble.getConnection()) {
            int result = connection.insert(tyrion);
            Assert.assertEquals(1, result);
        }
        Assert.assertTrue(tyrion.getId() > 0);

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from person where id=?");
            statement.setInt(1, tyrion.getId());
            int count = 0;
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Assert.assertEquals(Gender.MALE.toString(), resultSet.getString("gender"));
                Assert.assertEquals(tyrion.getWeight(), resultSet.getDouble("weight"), 0);
                count++;
            }
            Assert.assertEquals(1, count);
        }
    }
}
