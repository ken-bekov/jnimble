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

import net.nimble.annotations.Column;
import net.nimble.meta.MemberFinder;
import net.nimble.meta.MemberHandlerParams;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public class GetterByColumnFinder implements MemberFinder {

    private String columnName;
    private Method matchedMethod;

    @Override
    public void handle(AccessibleObject member, MemberHandlerParams event) {
        Method method = (Method) member;
        if (method == null || !method.getName().startsWith("get")) return;
        Column column = method.getAnnotation(Column.class);
        if (column == null || column.value().length() == 0) return;
        if (column.value().toUpperCase().equals(columnName)) {
            matchedMethod = method;
            event.setNullifyMember(true);
            event.setStopEnumeration(true);
        }
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName.toUpperCase();
        this.matchedMethod = null;
    }

    public Method getMatchedMethod() {
        return matchedMethod;
    }
}
