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
import net.nimble.meta.mappers.ObjectMapper;
import net.nimble.meta.extracts.ValueExtract;
import net.nimble.sql.ConnectionWrapper;
import net.nimble.sql.QueryParsingResult;
import net.nimble.sql.QueryProcessor;

import java.lang.reflect.Array;
import java.sql.*;
import java.util.*;

/**
 * This class is wrapper for {@code java.sql.Connection} class. It implements additional methods for simplify
 * the work with queries and their results. Additional methods may be divided into two groups: full methods that
 * need SQL query to be passed, and simplified methods that need no SQL query. First group that are {@code query} and
 * {@code execute}. Second group includes {@code insert}, {@code update} and {@code select} methods.
 */
public class NbConnection extends ConnectionWrapper {

    private final NbContext context;

    /**
     * @param connection The connection that will be used internally for execution of queries.
     * @param context    Context which contains all needed infrastructure objects
     */
    NbConnection(Connection connection, NbContext context) {
        super(connection);
        this.context = context;
    }

    /**
     * Method executes SELECT query and returns result as a List of items of specified type
     *
     * @param query the text of SQL query that returns ResultSet.
     * @param type  the type of items that must be returned as a result.
     * @param <T>   the type of list that must be returned as a result. As far as {@code type} parameter exists, this
     *              parameter doesn't need to be specified due to the Java type inference.
     * @return List of items of the type specified in the {@code type} parameter
     */
    public <T> List<T> query(String query, Class<T> type) {
        return query(query, (Object) null, type);
    }

    public <T> List<T> query(String query, Map<String, Object> params, Class<T> type) {
        return query(query, (Object) params, type);
    }

    public <T> List<T> query(String query, NbParams params, Class<T> type) {
        return query(query, (Object) params, type);
    }

    public <T> List<T> query(String query, Object params, Class<T> type) {

        QueryParsingResult parsingResult = null;
        Map<String, Object> valueMap = null;
        if (params != null) {
            parsingResult = QueryProcessor.extractParamNames(query);
            ValueExtract valueExtract = context.getValueExtractFactory().getExtractor(params);
            valueMap = valueExtract.getValueMap(parsingResult.getNames(), params);
            query = QueryProcessor.prepareForStatement(query, parsingResult, valueMap);
        }

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            if (parsingResult != null && valueMap != null) {
                setParamsToStatement(statement, parsingResult.getNames(), valueMap);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                List<T> resultList = new LinkedList<>();
                ObjectMapper objectCreator = context.getObjectMapperFactory().getObjectCreator(type);
                while (resultSet.next()) {
                    Object object = objectCreator.create(resultSet);
                    resultList.add((T) object);
                }
                return resultList;
            }
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    private Object generatedKey;

    public <T>  T getGeneratedKeyAs(Class<T> type) {
        return (T)context.getConverterManager().convertFromDb(generatedKey, type);
    }

    public int execute(String query, Object params) {
        try {
            generatedKey = null;

            QueryParsingResult parsingResult = QueryProcessor.extractParamNames(query);
            ValueExtract valueExtract = context.getValueExtractFactory().getExtractor(params);
            Map<String, Object> valueMap = valueExtract.getValueMap(parsingResult.getNames(), params);
            query = QueryProcessor.prepareForStatement(query, parsingResult, valueMap);

            PreparedStatement statement = isBean(params) ?
                    connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS) :
                    connection.prepareStatement(query);
            setParamsToStatement(statement, parsingResult.getNames(), valueMap);

            int result = statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                generatedKey = resultSet.getObject(1);
            }

            if (isBean(params) && generatedKey != null) {
                MetaUtils.applyId(generatedKey, params, context.getConverterManager());
            }
            return result;
        } catch (SQLException e) {
            throw new NimbleSQLException(e);
        }
    }

    private boolean isBean(Object params) {
        return !(params instanceof NbRow) && !(params instanceof Map);
    }

    public int insert(Object object) throws SQLException {
        String tableName = MetaUtils.getTableName(object.getClass());
        Map<String, Object> valueMap = MetaUtils.getColumnMap(object);
        List<String> columnList = new ArrayList<>(valueMap.size());

        StringBuilder builder = new StringBuilder();
        builder.append("insert into ");
        builder.append(tableName);
        builder.append("(");
        for (String columnName : valueMap.keySet()) {
            if (columnList.size() > 0) builder.append(",");
            builder.append(columnName);
            columnList.add(columnName);
        }
        builder.append(") values (");
        for (int i = 0; i < valueMap.size(); i++) {
            if (i > 0) builder.append(",");
            builder.append("?");
        }
        builder.append(")");

        PreparedStatement statement = connection.prepareStatement(builder.toString(), Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < columnList.size(); i++) {
            Object value =
                    context.getConverterManager().convertToDb(valueMap.get(columnList.get(i)));
            statement.setObject(i + 1, value);
        }

        int result = statement.executeUpdate();

        ResultSet resultSet = statement.getGeneratedKeys();
        while (resultSet.next()) {
            Object id = resultSet.getObject(1);
            MetaUtils.applyId(id, object, context.getConverterManager());
        }

        return result;
    }

    public int update(Object object) throws SQLException {
        String tableName = MetaUtils.getTableName(object.getClass());
        Map<String, Object> valueMap = MetaUtils.getColumnMap(object);
        List<String> columnNameList = new ArrayList<>(valueMap.size());
        List<String> idNameList = new ArrayList<>(valueMap.size());
        Map<String, Object> idMap = MetaUtils.getIdColumnMap(object);

        if (idMap.size() == 0) {
            throw new NimbleException("Id field value is NULL");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("update ");
        builder.append(tableName);
        builder.append(" set ");
        for (String columnName : valueMap.keySet()) {
            if (columnNameList.size() > 0) builder.append(",");
            builder.append(columnName);
            builder.append("=?");
            columnNameList.add(columnName);
        }
        builder.append(" where ");
        addWhereConditions(builder, idMap, idNameList);

        PreparedStatement statement = connection.prepareStatement(builder.toString());
        for (int i = 0; i < columnNameList.size(); i++) {
            String columnName = columnNameList.get(i);
            Object value = context.getConverterManager().convertToDb(valueMap.get(columnName));
            statement.setObject(i + 1, value);
        }
        for (int i = 0; i < idNameList.size(); i++) {
            String idName = idNameList.get(i);
            Object value = context.getConverterManager().convertToDb(idMap.get(idName));
            statement.setObject(i + 1 + columnNameList.size(), value);
        }

        return statement.executeUpdate();
    }

    public <T> T load(int id, Class<T> type) throws SQLException {
        String tableName = MetaUtils.getTableName(type);
        String idColumnName = MetaUtils.geIdColumnName(type);
        if (idColumnName == null) {
            throw new NimbleException("Can't find ID field");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("select * from ");
        builder.append(tableName);
        builder.append(" where ");
        builder.append(idColumnName);
        builder.append("=?");

        PreparedStatement statement = connection.prepareStatement(builder.toString());
        statement.setObject(1, id);
        ResultSet resultSet = statement.executeQuery();
        ObjectMapper applier = context.getObjectMapperFactory().getObjectCreator(type);
        int count = 0;
        T object = null;
        while (resultSet.next()) {
            if (count > 0) {
                throw new NimbleException("The query returned more than one row");
            }
            object = (T) applier.create(resultSet);
            count++;
        }
        return object;
    }

    public int delete(Object object) throws SQLException {
        String tableName = MetaUtils.getTableName(object.getClass());
        Map<String, Object> idValueMap = MetaUtils.getIdColumnMap(object);
        List<String> idNameList = new ArrayList<>(idValueMap.size());
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ")
                .append(tableName)
                .append(" where ");

        addWhereConditions(builder, idValueMap, idNameList);

        PreparedStatement statement = connection.prepareStatement(builder.toString());
        for (int i = 0; i < idNameList.size(); i++) {
            String columnName = idNameList.get(i);
            Object value = context.getConverterManager().convertToDb(idValueMap.get(columnName));
            statement.setObject(i + 1, value);
        }
        return statement.executeUpdate();
    }

    private void setParamsToStatement(PreparedStatement statement,
                                      List<String> nameList, Map<String, Object> valueMap) throws SQLException {
        int index = 1;
        for (String name : nameList) {
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

    private void addWhereConditions(StringBuilder builder, Map<String, Object> valueMap,
                                    List<String> columnNameList) {
        for (String columnName : valueMap.keySet()) {
            if (columnNameList.size() > 0) builder.append(" and ");
            builder.append(columnName);
            builder.append("=?");
            columnNameList.add(columnName);
        }
    }

    @Override
    public void close() throws SQLException {
        generatedKey = null;
        super.close();
    }
}
