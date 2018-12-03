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
import net.nimble.meta.extracts.ValueExtractFactory;
import net.nimble.meta.mappers.ObjectMapperFactory;
import net.nimble.sql.SqlDialect;
import net.nimble.sql.readers.ResultSetReader;

class NbContext {
    private ObjectMapperFactory objectMapperFactory;
    private ValueExtractFactory valueExtractFactory;
    private ConverterManagerImpl converterManager;
    private ResultSetReader resultSetReader;
    private SqlDialect dialect;

    public ObjectMapperFactory getObjectMapperFactory() {
        return objectMapperFactory;
    }

    public void setObjectMapperFactory(ObjectMapperFactory objectMapperFactory) {
        this.objectMapperFactory = objectMapperFactory;
    }

    public ConverterManagerImpl getConverterManager() {
        return converterManager;
    }

    public void setConverterManager(ConverterManagerImpl converterManager) {
        this.converterManager = converterManager;
    }

    public ValueExtractFactory getValueExtractFactory() {
        return valueExtractFactory;
    }

    public void setValueExtractFactory(ValueExtractFactory valueExtractFactory) {
        this.valueExtractFactory = valueExtractFactory;
    }

    public ResultSetReader getResultSetReader() {
        return resultSetReader;
    }

    public void setResultSetReader(ResultSetReader resultSetReader) {
        this.resultSetReader = resultSetReader;
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }
}
