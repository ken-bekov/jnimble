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
import java.sql.SQLException;
import java.util.List;

public class UpdateQueriesTests {

    private Nimble nimble;

    @Before
    public void init() throws SQLException {
        DataSource dataSource = DbUtils.getDataSource();
        Person[] people = new Person[]{
                PeopleFactory.createJaime(),
                PeopleFactory.createCercei(),
                PeopleFactory.createTyrion()
        };
        for (Person person : people) {
            DbUtils.deleteTestPeople(dataSource, person.getFirstName(), person.getLastName());
        }

        nimble = new Nimble(dataSource, TestConfig.defaultDialect);
    }

    @Test
    public void beanSqlUpdate() throws SQLException {
        Person cercei = PeopleFactory.createCercei();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercei);
        }
        Assert.assertTrue(cercei.getId() > 0);
        cercei.setCashAmount(cercei.getCashAmount() + 100.22);
        cercei.setWeight(cercei.getWeight() + 5.83);
        try (NbConnection connection = nimble.getConnection()) {
            int result = connection.execute("update person set cash_amount=:cashAmount, " +
                    "weight=:weight where id=:id", cercei);
            Assert.assertEquals(1, result);
        }

        Person person;
        try (NbConnection connection = nimble.getConnection()) {
            person = connection.load(cercei.getId(), Person.class);
        }
        Assert.assertEquals(cercei.getWeight(), person.getWeight(), 0);
        Assert.assertEquals(cercei.getCashAmount(), person.getCashAmount(), 0);
    }

    @Test
    public void beanSimpleUpdate() throws SQLException {

        Person tyrion = PeopleFactory.createTyrion();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(tyrion);
        }
        Assert.assertTrue(tyrion.getId() > 0);
        tyrion.setCashAmount(33333.98);
        tyrion.setWeight(45.88);
        try (NbConnection connection = nimble.getConnection()) {
            connection.update(tyrion);
        }

        Person[] personList;
        try (NbConnection connection = nimble.getConnection()) {
            personList = connection.query("select * from person where id=:id", tyrion, Person.class);
        }
        Assert.assertEquals(1, personList.length);
        Person person = personList[0];
        Assert.assertEquals(tyrion.getWeight(), person.getWeight());
        Assert.assertEquals(tyrion.getCashAmount(), person.getCashAmount());
    }
}
