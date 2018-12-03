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

package net.nimble.meta.finders;

import net.nimble.meta.MemberFinder;
import net.nimble.meta.MemberHandlerParams;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class FieldByNameFinder implements MemberFinder {

    private String columnName;
    private Field matchedField;

    @Override
    public void handle(AccessibleObject member, MemberHandlerParams event) {
        Field field = (Field) member;
        if (field == null) return;
        if (field.getName().toUpperCase().equals(columnName)) {
            matchedField = field;
        }
    }

    public Field getMatchedField() {
        return matchedField;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName.toUpperCase();
        this.matchedField = null;
    }
}
