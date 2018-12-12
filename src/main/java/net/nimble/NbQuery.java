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

package net.nimble;

import net.nimble.exceptions.NimbleException;
import net.nimble.exceptions.NimbleSQLException;
import net.nimble.meta.MetaUtils;
import net.nimble.meta.extracts.ValueExtract;
import net.nimble.meta.mappers.BeanMapper;
import net.nimble.meta.mappers.MapMapper;
import net.nimble.meta.mappers.NbRowMapper;
import net.nimble.meta.mappers.ObjectMapper;
import net.nimble.sql.QueryParamNames;
import net.nimble.sql.QueryProcessor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class NbQuery {

    private final String query;
    private final NbContext context;
    private final Connection connection;
    private Object generatedKey;
    private final Map<String, Object> valueMap = new HashMap<>();

    NbQuery(String query, NbConnection connection, NbContext context) {
        this.query = query;
        this.connection = connection;
        this.context = context;
    }

    public NbQuery addParam(String name, Object value) {
        valueMap.put(name, value);
        return this;
    }

    public NbQuery addParamsMap(Map<String, Object> map) {
        valueMap.putAll(map);
        return this;
    }

    public <T> T fetchValue(Class<T> type) {
        Object value = fetchValue();
        return (T) context.getConverterManager().convertFromDb(value, type);
    }

    public Object fetchValue() {
        try {
            PreparedStatement statement = createStatement(false);
            Object value = null;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.getMetaData().getColumnCount() > 1) {
                    throw new NimbleException(String.format("Result is not singular. There are %d columns in the result.",
                            resultSet.getMetaData().getColumnCount()));
                }

                int counter = 0;
                while (resultSet.next()) {
                    counter++;
                    if (counter > 1) {
                        throw new NimbleException("Result is not singular. There are more than one row in the result.");
                    }
                    value = resultSet.getObject(1);
                }
            }
            return value;
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public Map<String, Object>[] fetchMapList() {
        try {
            PreparedStatement statement = createStatement(false);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Map> resultList = new LinkedList<>();
                ObjectMapper mapper = new MapMapper();
                while (resultSet.next()) {
                    Map map = (Map) mapper.create(resultSet);
                    resultList.add(map);
                }
                Map[] result = (Map[]) Array.newInstance(Map.class, resultList.size());
                return resultList.toArray(result);
            }
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public NbRow[] fetchRowList() {
        try {
            PreparedStatement statement = createStatement(false);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<NbRow> resultList = new LinkedList<>();
                ObjectMapper mapper = new NbRowMapper(context.getConverterManager());
                while (resultSet.next()) {
                    NbRow object = (NbRow) mapper.create(resultSet);
                    resultList.add(object);
                }
                return resultList.toArray(new NbRow[0]);
            }
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public <T> T[] fetchList(Class<T> type) {
        try {
            PreparedStatement statement = createStatement(false);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> resultList = new LinkedList<>();
                ObjectMapper mapper = new BeanMapper(type, context.getConverterManager());
                while (resultSet.next()) {
                    Object object = mapper.create(resultSet);
                    resultList.add((T) object);
                }
                T[] result = (T[]) Array.newInstance(type, resultList.size());
                return resultList.toArray(result);
            }
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    public int execute() {
        try {
            generatedKey = null;
            PreparedStatement statement = createStatement(true);
            int result = statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.getMetaData().getColumnCount() > 0) {
                if (resultSet.next()) {
                    generatedKey = resultSet.getObject(1);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    private PreparedStatement createStatement(boolean returnGeneratedKey) throws SQLException {
        QueryParamNames parsingResult = null;
        String preparedQuery;
        parsingResult = QueryProcessor.extractParamNames(query);
        preparedQuery = QueryProcessor.replaceParamNames(query, parsingResult, valueMap);

        PreparedStatement statement = returnGeneratedKey ?
                connection.prepareStatement(preparedQuery, Statement.RETURN_GENERATED_KEYS) :
                connection.prepareStatement(preparedQuery);
        if (parsingResult != null) {
            setParamsToStatement(statement, parsingResult.getNames(), valueMap);
        }

        return statement;
    }

    private void setParamsToStatement(PreparedStatement statement, List<String> nameList,
                                      Map<String, Object> valueMap) throws SQLException {
        int index = 1;
        for (String name : nameList) {
            if (!valueMap.containsKey(name)) {
                throw new NimbleException(String.format("Value for parameter :%s is not provided", name));
            }

            Object value = valueMap.get(name);

            if (value == null) {
                statement.setObject(index, null);
                index++;
                continue;
            }

            Class valueType = value.getClass();
            if (valueType.isArray()) {
                for (int i = 0; i < Array.getLength(value); i++) {
                    Object arrayItem = context.getConverterManager().convertToDb(Array.get(value, i));
                    statement.setObject(index, arrayItem);
                    index++;
                }
            } else if (Collection.class.isAssignableFrom(valueType)) {
                for (Object item : ((Collection) value)) {
                    Object collectionItem = context.getConverterManager().convertToDb(item);
                    statement.setObject(index, collectionItem);
                    index++;
                }
            } else {
                value = context.getConverterManager().convertToDb(value);
                statement.setObject(index, value);
                index++;
            }
        }
    }

    public <T> T getGeneratedKey(Class<T> type) {
        if (generatedKey == null) return null;
        return (T) context.getConverterManager().convertFromDb(generatedKey, type);
    }

    public NbQuery addParamsBean(Object bean) {
        if (bean == null) {
            throw new NimbleException("Null value is not allowed");
        }
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                valueMap.put(field.getName(), field.get(bean));
            } catch (IllegalAccessException e) {
                throw new NimbleException(String.format("Can't read field value of field %s of class %s",
                        field.getName(), bean.getClass().getName()));
            }
        }
        return this;
    }
}
