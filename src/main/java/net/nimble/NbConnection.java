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
import net.nimble.meta.MetaUtils;
import net.nimble.meta.mappers.ObjectMapper;
import net.nimble.sql.ConnectionWrapper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public NbQuery query(String query) {
        return new NbQuery(query, this, context);
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

    public int delete(Object id, Class type) throws SQLException {
        Field idField = MetaUtils.getIdField(type);
        if (idField == null) {
            throw new NimbleException("Can't find id field for type " + type.getName());
        }

        idField.setAccessible(true);
        String idColumnName = MetaUtils.getColumnName(idField);
        if (idColumnName == null) {
            idColumnName = idField.getName();
        }

        return delete(id, idColumnName, type);
    }

    public int delete(Object object) throws SQLException {
        Field idField = MetaUtils.getIdField(object.getClass());
        if (idField == null) {
            throw new NimbleException("Can't find id field for type " + object.getClass().getName());
        }

        idField.setAccessible(true);
        String idColumnName = MetaUtils.getColumnName(idField);
        if (idColumnName == null) {
            idColumnName = idField.getName();
        }
        try {
            Object idValue = idField.get(object);
            return delete(idValue, idColumnName, object.getClass());
        } catch (IllegalAccessException e) {
            throw new NimbleException(e);
        }
    }

    private int delete(Object idValue, String idColumnName, Class type) throws SQLException {
        String tableName = MetaUtils.getTableName(type);
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ")
                .append(tableName)
                .append(" where ")
                .append(idColumnName)
                .append("=?");

        PreparedStatement statement = connection.prepareStatement(builder.toString());
        Object value = context.getConverterManager().convertToDb(idValue);
        statement.setObject(1, value);
        return statement.executeUpdate();
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
        super.close();
    }
}
