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

package net.nimble.meta.mappers;

import net.nimble.conversion.ConverterManagerImpl;
import net.nimble.exceptions.NimbleException;
import net.nimble.meta.MemberEnumerator;
import net.nimble.meta.MemberFinder;
import net.nimble.meta.finders.FieldByColumnFinder;
import net.nimble.meta.finders.FieldByNameFinder;
import net.nimble.meta.finders.GetterByColumnFinder;
import net.nimble.meta.finders.MethodByNameFinder;
import net.nimble.utils.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BeanMapper implements ObjectMapper {

    private final ConverterManagerImpl converterManager;
    private final Class objectClass;

    public BeanMapper(Class objectClass, ConverterManagerImpl converterManager) {
        this.objectClass = objectClass;
        this.converterManager = converterManager;
    }

    @Override
    public Object create(ResultSet resultSet) {
        Object object;
        try {
            object = objectClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new NimbleException("Can't create a new instance of class " + objectClass.getName(), e);
        }
        Map<String, Object> valueMap;
        try {
            valueMap = getRowValueMap(resultSet);
        } catch (SQLException e) {
            throw new NimbleException("Can't build value map for the passed RowSet");
        }
        applyByColumnNames(valueMap, object);
        return object;
    }

    private void applyByColumnNames(Map<String, Object> valueMap, Object object) {
        Class objectType = object.getClass();
        Method[] methods = objectType.getMethods();
        Field[] fields = objectType.getDeclaredFields();
        MemberEnumerator memberEnumerator = new MemberEnumerator();
        FieldByColumnFinder fieldByColumnFinder = new FieldByColumnFinder();
        FieldByNameFinder fieldByNameFinder = new FieldByNameFinder();
        GetterByColumnFinder getterByColumnFinder = new GetterByColumnFinder();
        MethodByNameFinder methodByNameFinder = new MethodByNameFinder();
        MemberFinder[] fieldFinders = new MemberFinder[]{fieldByColumnFinder, fieldByNameFinder};
        for (String columnName : valueMap.keySet()) {
            fieldByColumnFinder.setColumnName(columnName);
            fieldByNameFinder.setFieldName(columnName);
            memberEnumerator.enumerate(fields, fieldFinders);

            String fieldName = null;
            if (fieldByColumnFinder.getMatchedField() != null) {
                fieldName = fieldByColumnFinder.getMatchedField().getName();
            } else {
                getterByColumnFinder.setColumnName(columnName);
                memberEnumerator.enumerate(methods, getterByColumnFinder);
                if (getterByColumnFinder.getMatchedMethod() != null) {
                    fieldName = getterByColumnFinder.getMatchedMethod().getName().substring(3);
                    fieldName = StringUtils.uncapitalize(fieldName);
                } else if (fieldByNameFinder.getMatchedField() != null) {
                    fieldName = fieldByNameFinder.getMatchedField().getName();
                }
            }

            if (fieldName == null) {
                throw new NimbleException("Can't find field for column '" + columnName + "' in the class " +
                        objectType.getName());
            }

            String setterName = "set" + StringUtils.capitalize(fieldName);
            methodByNameFinder.setMethodName(setterName);
            memberEnumerator.enumerate(methods, methodByNameFinder);
            Method setter = methodByNameFinder.getMatchedMethod();
            if (setter == null) {
                throw new NimbleException("Can't find setter method for the field " + fieldName + " in the class " +
                        objectType.getName());
            }

            try {
                setter.invoke(object, converterManager.convertFromDb(valueMap.get(columnName),
                        setter.getParameterTypes()[0]));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new NimbleException(e);
            } catch (IllegalArgumentException e) {
                throw new NimbleException(String.format("Can't apply value of type %s to field %s",
                        valueMap.get(columnName).getClass().getName(), fieldName), e);
            }
        }
    }

    private Map<String, Object> getRowValueMap(ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        HashMap<String, Object> result = new HashMap<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            String columnName = resultSet.getMetaData().getColumnName(columnIndex);
            Object resultValue = resultSet.getObject(columnIndex);
            result.put(columnName, resultValue);
        }
        return result;
    }
}
