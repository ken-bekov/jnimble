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

import net.nimble.NbRow;
import net.nimble.conversion.ConverterManagerImpl;
import net.nimble.exceptions.NimbleException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NbRowMapper implements ObjectMapper {

    private List<String> columnNames = null;
    private final ConverterManagerImpl converterManager;

    public NbRowMapper(ConverterManagerImpl converterManager) {
        this.converterManager = converterManager;
    }

    @Override
    public Object create(ResultSet resultSet) {
        if (columnNames == null) {
            extractColumnNames(resultSet);
        }
        Object[] data = extractData(resultSet);
        return new NbRow(columnNames.toArray(new String[0]), data, converterManager);
    }

    private Object[] extractData(ResultSet resultSet) {
        try {
            int columnCount = resultSet.getMetaData().getColumnCount();
            Object[] data = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                data[i - 1] = resultSet.getObject(i);
            }
            return data;
        } catch (SQLException e) {
            throw new NimbleException("Can't extract ResultSet data", e);
        }
    }

    private void extractColumnNames(ResultSet resultSet) {
        try {
            int columnCount = resultSet.getMetaData().getColumnCount();
            columnNames = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(resultSet.getMetaData().getColumnName(i));
            }
        } catch (SQLException e) {
            throw new NimbleException("Can't extract column names", e);
        }
    }
}
