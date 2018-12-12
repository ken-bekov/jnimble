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

import net.nimble.conversion.ConverterManagerImpl;
import net.nimble.exceptions.NimbleException;
import org.joda.time.DateTime;

public class NbRow {
    private final String[] columnNames;
    private final Object[] data;
    private final ConverterManagerImpl converterManager;

    public NbRow(String[] columnNames, Object[] data, ConverterManagerImpl converterManager) {
        this.columnNames = columnNames;
        this.data = data;
        this.converterManager = converterManager;
        if (columnNames.length != data.length) {
            throw new NimbleException("Column count doesn't match data length");
        }
    }

    public NbRow(String[] columnNames, ConverterManagerImpl converterManager) {
        this.columnNames = columnNames;
        this.data = new Object[columnNames.length];
        this.converterManager = converterManager;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int columnIndex) {
        checkColumnIndex(columnIndex);
        return columnNames[columnIndex];
    }

    public Object getObject(int columnIndex) {
        checkColumnIndex(columnIndex);
        return data[columnIndex];
    }

    public Object getObject(String columnName) {
        int columnIndex = getColumnIndex(columnName);
        return getObject(columnIndex);
    }

    public String getString(int columnIndex) {
        return getValue(columnIndex, String.class);
    }

    public String getString(String columnName) {
        return getValue(columnName, String.class);
    }

    public Short getShort(int columnIndex) {
        return getValue(columnIndex, Short.class);
    }

    public Short getShort(String columnName) {
        return getValue(columnName, Short.class);
    }

    public Integer getInt(int columnIndex) {
        return getValue(columnIndex, Integer.class);
    }

    public Integer getInt(String columnName) {
        return getValue(columnName, Integer.class);
    }

    public Float getFloat(int columnIndex) {
        return getValue(columnIndex, Float.class);
    }

    public Float getFloat(String columnName) {
        return getValue(columnName, Float.class);
    }

    public Double getDouble(int columnIndex) {
        return getValue(columnIndex, Double.class);
    }

    public Double getDouble(String columnName) {
        return getValue(columnName, Double.class);
    }

    public DateTime getDateTime(String columnName) {
        return getValue(columnName, DateTime.class);
    }

    public DateTime getDateTime(int columnIndex) {
        return getValue(columnIndex, DateTime.class);
    }

    public <T> T getValue(int columnIndex, Class<T> type) {
        checkColumnIndex(columnIndex);
        return (T) converterManager.convertFromDb(data[columnIndex], type);
    }

    public <T> T getValue(String columnName, Class<T> type) {
        int columnIndex = getColumnIndex(columnName);
        return (T) converterManager.convertFromDb(data[columnIndex], type);
    }

    private void setValue(int columnIndex, Object value) {
        checkColumnIndex(columnIndex);
        data[columnIndex] = value;
    }

    private int getColumnIndex(String columnName) {
        int columnIndex = -1;
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(columnName)) {
                columnIndex = i;
                break;
            }
        }
        if (columnIndex == -1) {
            throw new NimbleException("Can't find column with name " + columnName);
        }
        return columnIndex;
    }

    private void checkColumnIndex(int columnIndex) {
        if (columnIndex < 0 || columnIndex > data.length - 1) {
            throw new NimbleException("Column index " + columnIndex + " is out of bound");
        }
    }
}
