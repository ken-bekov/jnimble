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
import net.nimble.NbRow;
import net.nimble.Nimble;
import net.nimble.tests.config.TestConfig;
import net.nimble.tests.entities.Gender;
import net.nimble.tests.entities.Person;
import net.nimble.tests.utils.DbUtils;
import net.nimble.tests.utils.PeopleFactory;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectQueriesTests {

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
    public void noParamSelect() throws SQLException {
        Person tyrion = PeopleFactory.createTyrion();
        Person jaime = PeopleFactory.createJaime();
        Person cercei = PeopleFactory.createCercei();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(tyrion);
            connection.insert(jaime);
            connection.insert(cercei);
            Assert.assertTrue(tyrion.getId() > 0);
            Assert.assertTrue(jaime.getId() > 0);
            Assert.assertTrue(cercei.getId() > 0);
        }

        Person[] personList;
        try (NbConnection connection = nimble.getConnection()) {
            personList = connection.query("select * from person p " +
                    "where first_name in ('Tyrion', 'Jaime', 'Cercei')").fetchList(Person.class);
        }

        Assert.assertNotNull(personList);
        Assert.assertEquals(3, personList.length);
        for (Person person : personList) {
            if (person.getFirstName().equals("Tyrion")) {
                Assert.assertEquals(Gender.MALE, person.getGender());
            } else if (person.getFirstName().equals("Jaime")) {
                Assert.assertEquals(Gender.MALE, person.getGender());
            } else if (person.getFirstName().equals("Cercei")) {
                Assert.assertEquals(Gender.FEMALE, person.getGender());
            }
        }
    }

    @Test
    public void paramQuery() throws SQLException {
        final Person cercie = PeopleFactory.createCercei();
        final Person jaime = PeopleFactory.createJaime();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercie);
            connection.insert(jaime);
        }

        Person[] personList;
        try (NbConnection connection = nimble.getConnection(false)) {
            personList = connection.query("select * from person where id = :id")
                    .addParam("id", cercie.getId())
                    .addParam("gender", cercie.getGender())
                    .fetchList(Person.class);
        }

        Assert.assertNotNull(personList);
        Assert.assertEquals(1, personList.length);

        Person person = personList[0];
        Assert.assertEquals(1, personList.length);
        Assert.assertEquals(cercie.getHeight(), person.getHeight(), 0);
        Assert.assertEquals(cercie.getCashAmount(), person.getCashAmount(), 0);
        Assert.assertEquals(cercie.getGender(), person.getGender());
        Assert.assertEquals(cercie.getBirthDate(), person.getBirthDate());
    }

    @Test
    public void mapParamQuery() throws SQLException {
        final Person cercie = PeopleFactory.createCercei();
        final Person jaime = PeopleFactory.createJaime();
        final Person tyrion = PeopleFactory.createTyrion();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercie);
            connection.insert(jaime);
            connection.insert(tyrion);
        }

        List<String> nameList = new ArrayList<>(3);
        nameList.add("Tyrion");
        nameList.add("Jaime");
        nameList.add("Cercei");
        Map<String, Object> params = new HashMap<>();
        params.put("firstName", nameList);
        params.put("gender", Gender.MALE);

        Person[] personList;
        try (NbConnection connection = nimble.getConnection()) {
            personList = connection.query("select * from person where first_name in (:firstName) and " +
                    "gender=:gender").addParamsMap(params).fetchList(Person.class);
        }

        Assert.assertNotNull(personList);
        Assert.assertEquals(2, personList.length);
        for (Person person : personList) {
            if (person.getFirstName().equals("Jaime")) {
                Assert.assertEquals(jaime.getBirthDate(), person.getBirthDate());
                Assert.assertEquals(jaime.getCashAmount(), person.getCashAmount(), 0);
            } else if (person.getFirstName().equals("Tyrion")) {
                Assert.assertEquals(tyrion.getBirthDate(), person.getBirthDate());
                Assert.assertEquals(tyrion.getCashAmount(), person.getCashAmount(), 0);
            }
        }
    }

    @Test
    public void listParamQuery() throws SQLException {
        final Person cercie = PeopleFactory.createCercei();
        final Person jaime = PeopleFactory.createJaime();
        final Person tyrion = PeopleFactory.createTyrion();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercie);
            connection.insert(jaime);
            connection.insert(tyrion);
        }

        Person[] personList;
        try (NbConnection connection = nimble.getConnection()) {
            personList = connection.query(
                    "select * from person where id in (:id) and " +
                            "(first_name=:name1 or first_name=:name2)")
                    .addParam("id", new int[]{jaime.getId(), cercie.getId()})
                    .addParam("name1", "Jaime")
                    .addParam("name2", "Cercei")
                    .fetchList(Person.class);
        }

        Assert.assertNotNull(personList);
        Assert.assertEquals(2, personList.length);
        for (Person person : personList) {
            if (person.getFirstName().equals("Jaime")) {
                Assert.assertEquals(jaime.getBirthDate(), person.getBirthDate());
                Assert.assertEquals(jaime.getCashAmount(), person.getCashAmount(), 0);
            } else if (person.getFirstName().equals("Cercei")) {
                Assert.assertEquals(cercie.getBirthDate(), person.getBirthDate());
                Assert.assertEquals(cercie.getCashAmount(), person.getCashAmount(), 0);
            }
        }
    }

    @Test
    public void nbRowAsResult() throws SQLException {
        Person cercie = PeopleFactory.createCercei();
        Person jaime = PeopleFactory.createJaime();
        Person tyrion = PeopleFactory.createTyrion();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(cercie);
            connection.insert(jaime);
            connection.insert(tyrion);
        }

        NbRow[] rowList;
        try (NbConnection connection = nimble.getConnection()) {
            rowList = connection.query("select * from person where first_name in (:names)")
                    .addParam("names", new String[]{"Jaime", "Tyrion", "Cercei"})
                    .fetchRowList();
        }
        Assert.assertNotNull(rowList);
        Assert.assertEquals(3, rowList.length);
        for (NbRow row : rowList) {
            switch (row.getString("first_name")) {
                case "Jaime":
                    Assert.assertEquals(jaime.getWeight(), row.getDouble("weight"), 0);
                    Assert.assertEquals(jaime.getGender(), row.getValue("gender", Gender.class));
                    break;
                case "Tyrion":
                    Assert.assertEquals(tyrion.getWeight(), row.getDouble("weight"), 0);
                    Assert.assertEquals(tyrion.getGender(), row.getValue("gender", Gender.class));
                    break;
                case "Cercei":
                    Assert.assertEquals(cercie.getWeight(), row.getDouble("weight"), 0);
                    Assert.assertEquals(cercie.getGender(), row.getValue("gender", Gender.class));
                    break;
            }
        }
    }

    @Test
    public void mapAsResult() throws SQLException {
        Person tyrion = PeopleFactory.createTyrion();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(tyrion);
        }

        Map<String, Object>[] personList;
        try (NbConnection connection = nimble.getConnection()) {
            personList = connection.query("select * from person where birth_date=:birthDate")
                    .addParam("birthDate", tyrion.getBirthDate())
                    .fetchMapList();
        }

        Assert.assertEquals(1, personList.length);
        Assert.assertEquals(tyrion.getWeight(), personList[0].get("weight"));
    }

    @Test
    public void singularResult() throws SQLException {
        Person jaime = PeopleFactory.createJaime();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(jaime);
        }

        try (NbConnection connection = nimble.getConnection()) {
            int id = connection.query("select id from person where first_name=:firstName and birth_date=:birthDate")
                    .addParam("firstName", jaime.getFirstName())
                    .addParam("birthDate", jaime.getBirthDate())
                    .fetchValue(Integer.class);

            DateTime date = connection.query("select birth_date from person " +
                    "where first_name=:firstName and birth_date=:birthDate")
                    .addParam("firstName", jaime.getFirstName())
                    .addParam("birthDate", jaime.getBirthDate())
                    .fetchValue(DateTime.class);
         
            Assert.assertEquals(jaime.getId(), id);
            Assert.assertEquals(jaime.getBirthDate(), date);
        }
    }

    @Test
    public void beanSimpleLoad() throws SQLException {
        Person jaime = PeopleFactory.createJaime();
        try (NbConnection connection = nimble.getConnection()) {
            connection.insert(jaime);
        }

        Person person;
        try (NbConnection connection = nimble.getConnection()) {
            person = connection.load(jaime.getId(), Person.class);
        }

        Assert.assertEquals(jaime.getWeight(), person.getWeight(), 0);
        Assert.assertEquals(jaime.getBirthDate(), person.getBirthDate());
        Assert.assertEquals(jaime.getCashAmount(), person.getCashAmount());
        Assert.assertEquals(jaime.getGender(), person.getGender());
    }
}
