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

package net.nimble.meta;

import jdk.nashorn.internal.ir.annotations.Ignore;
import net.nimble.annotations.Column;
import net.nimble.annotations.Id;
import net.nimble.annotations.Table;
import net.nimble.conversion.ConverterManagerImpl;
import net.nimble.exceptions.NimbleException;
import net.nimble.meta.finders.FieldByIdFinder;
import net.nimble.meta.finders.FieldByNameFinder;
import net.nimble.meta.finders.GetterByIdFinder;
import net.nimble.meta.finders.MethodByNameFinder;
import net.nimble.utils.StringUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MetaUtils {

    public static String getTableName(Class type) {
        Table table = (Table) type.getAnnotation(Table.class);
        if (table != null && table.value().length() > 0) {
            return table.value();
        } else {
            return type.getSimpleName();
        }
    }

    public static Map<String, Object> getColumnMap(Object object) {
        Map<String, Object> valueMap = new HashMap<>();
        Method[] methods = object.getClass().getMethods();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Method method : methods) {
            if (method.getDeclaringClass() != object.getClass() || !method.getName().startsWith("get")) continue;
            if (method.getAnnotation(Ignore.class) != null) continue;
            Id id = method.getAnnotation(Id.class);
            if (id != null && id.generate()) continue;

            String columnName = null;

            String fieldName = StringUtils.uncapitalize(method.getName().substring(3));
            int fieldIndex = ReflectionUtils.findFieldIndex(fields, fieldName, false);
            if (fieldIndex > -1) {
                Field field = fields[fieldIndex];
                fields[fieldIndex] = null;
                if (field.getAnnotation(Ignore.class) != null) continue;
                id = field.getAnnotation(Id.class);
                if (id != null && id.generate()) continue;
                columnName = MetaUtils.getColumnName(field);
            }

            if (columnName == null) {
                columnName = MetaUtils.getColumnName(method);
                if (columnName == null) {
                    columnName = fieldName;
                }
            }

            try {
                valueMap.put(columnName, method.invoke(object));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new NimbleException(e);
            }
        }

        return valueMap;
    }


    public static void applyId(Object id, Object object, ConverterManagerImpl converterManager) {
        Class objectClass = object.getClass();
        Field[] fields = object.getClass().getDeclaredFields();
        Method[] methods = object.getClass().getMethods();
        FieldByNameFinder fieldByNameFinder = new FieldByNameFinder();
        fieldByNameFinder.setFieldName("id");
        FieldByIdFinder fieldByIdFinder = new FieldByIdFinder();
        GetterByIdFinder getterByIdFinder = new GetterByIdFinder();
        MemberEnumerator memberEnumerator = new MemberEnumerator();
        memberEnumerator.enumerate(fields, new MemberFinder[]{fieldByNameFinder, fieldByIdFinder});
        String fieldName = null;
        if (fieldByIdFinder.getIdField() != null) {
            fieldName = fieldByIdFinder.getIdField().getName();
        } else {
            memberEnumerator.enumerate(methods, getterByIdFinder);
            if (getterByIdFinder.getIdGetter() != null) {
                fieldName = StringUtils.uncapitalize(getterByIdFinder.getIdGetter().getName().substring(3));
            } else if (fieldByNameFinder.getMatchedField() != null) {
                fieldName = fieldByNameFinder.getMatchedField().getName();
            }
        }

        if (fieldName == null) {
            throw new NimbleException("Can't find Id field for class " + objectClass.getName());
        }

        MethodByNameFinder methodByNameFinder = new MethodByNameFinder();
        methodByNameFinder.setMethodName("set" + StringUtils.capitalize(fieldName));
        memberEnumerator.enumerate(methods, methodByNameFinder);
        Method setter = methodByNameFinder.getMatchedMethod();

        if (setter == null) {
            throw new NimbleException("Can't find setter for field " + fieldName + " in the class " +
                    objectClass.getName());
        }

        try {
            setter.setAccessible(true);
            setter.invoke(object, converterManager.convertFromDb(id, setter.getParameterTypes()[0]));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NimbleException(e);
        }

    }

    public static Map<String, Object> getIdColumnMap(Object object) {
        Map<String, Object> result = new HashMap<>();
        int methodIndex = -1;
        Method[] methods = object.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (!method.getName().startsWith("get")) continue;
            if (method.getAnnotation(Id.class) != null) {
                methodIndex = i;
                break;
            } else if (methodIndex == -1 && method.getName().equals("getId")) {
                methodIndex = i;
            }
        }

        if (methodIndex == -1) {
            throw new NimbleException("Can't find getter method for object's field ID");
        }

        Method method = methods[methodIndex];
        String columnName = null;
        String fieldName = StringUtils.uncapitalize(method.getName().substring(3));
        int fieldIndex = ReflectionUtils.findFieldIndex(object.getClass().getDeclaredFields(),
                fieldName, true);
        if (fieldIndex > -1) {
            columnName = MetaUtils.getColumnName(object.getClass().getDeclaredFields()[fieldIndex]);
        }
        if (columnName == null) {
            columnName = MetaUtils.getColumnName(method);
        }
        if (columnName == null) {
            columnName = fieldName;
        }

        try {
            Object value = method.invoke(object);
            result.put(columnName, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NimbleException(e);
        }
        return result;
    }

    public static String getColumnName(AccessibleObject member) {
        Column column = member.getAnnotation(Column.class);
        if (column != null && column.value().length() > 0) {
            return column.value();
        }
        return null;
    }

    public static Field getIdField(Class type) {
        Field[] fields = type.getDeclaredFields();
        MemberEnumerator enumerator = new MemberEnumerator();
        FieldByIdFinder fieldByIdFinder = new FieldByIdFinder();
        FieldByNameFinder fieldByNameFinder = new FieldByNameFinder();
        fieldByNameFinder.setFieldName("id");
        enumerator.enumerate(fields, new MemberFinder[]{fieldByIdFinder, fieldByNameFinder});
        if (fieldByIdFinder.getIdField() != null) {
            return fieldByIdFinder.getIdField();
        } else {
            Method[] methods = type.getMethods();
            GetterByIdFinder getterByIdFinder = new GetterByIdFinder();
            enumerator.enumerate(methods, getterByIdFinder);
            if (getterByIdFinder.getIdGetter() != null) {
                Method getter = getterByIdFinder.getIdGetter();
                fieldByNameFinder.setFieldName(StringUtils.uncapitalize(getter.getName().substring(3)));
                enumerator.enumerate(fields, fieldByNameFinder);
            }
            return fieldByNameFinder.getMatchedField();
        }
    }

    public static String geIdColumnName(Class type) {
        Method[] methods = type.getMethods();
        Field[] fields = type.getDeclaredFields();
        Method idNameMethod = null;
        for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
            Method method = methods[methodIndex];
            String columnName = null;
            Field field = null;
            if (method == null || !method.getName().startsWith("get")) continue;
            String fieldName = StringUtils.uncapitalize(method.getName().substring(3));
            int fieldIndex = ReflectionUtils.findFieldIndex(fields, fieldName, true);
            if (fieldIndex > -1) {
                field = fields[fieldIndex];
            }

            if ((field != null && field.getAnnotation(Id.class) != null) || method.getAnnotation(Id.class) != null) {
                if (field != null) columnName = MetaUtils.getColumnName(field);
                if (columnName == null) columnName = MetaUtils.getColumnName(method);
                if (columnName == null) columnName = fieldName;
                return columnName;
            }

            if (idNameMethod == null && "ID".equals(fieldName.toUpperCase())) {
                idNameMethod = method;
            }
        }

        if (idNameMethod != null) {
            return idNameMethod.getName().substring(3);
        } else {
            return null;
        }
    }
}
