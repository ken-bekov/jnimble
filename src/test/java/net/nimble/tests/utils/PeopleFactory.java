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

import net.nimble.tests.entities.Gender;
import net.nimble.tests.entities.Person;
import org.joda.time.DateTime;

public class PeopleFactory {

    public static Person createTyrion() {
        Person person = new Person();
        person.setFirstName("Tyrion");
        person.setLastName("Lannister");
        person.setBirthDate(DateTime.parse("1955-06-07"));
        person.setGender(Gender.MALE);
        person.setHeight(1.3f);
        person.setWeight(45.99);
        person.setCashAmount(99999.99);
        return person;
    }

    public static Person createJaime() {
        Person person = new Person();
        person.setFirstName("Jaime");
        person.setLastName("Lannister");
        person.setBirthDate(new DateTime(1950, 11, 20, 0,0,0));
        person.setGender(Gender.MALE);
        person.setHeight(1.85f);
        person.setWeight(94.99);
        person.setCashAmount(88888.99);
        return person;
    }

    public static Person createCercei() {
        Person person = new Person();
        person.setFirstName("Cercei");
        person.setLastName("Lannister");
        person.setBirthDate(new DateTime(1950, 11, 20, 0,0,0));
        person.setGender(Gender.FEMALE);
        person.setHeight(1.70f);
        person.setWeight(59.89);
        person.setCashAmount(88888.99);
        return person;
    }
}
