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

package net.nimble.conversion;

import net.nimble.conversion.converters.DefaultFromDbConverter;
import net.nimble.conversion.converters.DefaultToDbConverter;
import net.nimble.conversion.converters.fromdb.SqlDateToDateTimeConverter;
import net.nimble.conversion.converters.fromdb.TimestampToDateTimeConverter;
import net.nimble.conversion.converters.todb.DateTimeToDbConverter;
import net.nimble.exceptions.NimbleException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ConverterManagerImpl implements ConverterManager {

    private final Map<Type, Map<Type, FromDbConverter<?, ?>>> fromDbConverterMap = new HashMap<>();
    private final Map<Type, ToDbConverter<?>> toDbConverterMap = new HashMap<>();
    private final DefaultFromDbConverter defaultFromDbConverter = new DefaultFromDbConverter();
    private final DefaultToDbConverter defaultToDbConverter = new DefaultToDbConverter();

    public ConverterManagerImpl() {
        addDefaultConverters();
    }

    private void addDefaultConverters() {
        addFromDbConverter(new TimestampToDateTimeConverter());
        addFromDbConverter(new SqlDateToDateTimeConverter());

        addToDbConverter(new DateTimeToDbConverter());
    }

    public void addFromDbConverter(FromDbConverter<?, ?> converter) {
        Type[] genericTypes = getConverterGenericTypes(converter);
        if (genericTypes == null || genericTypes.length < 2) {
            throw new NimbleException("Can't find generic params of converter of type " +
                    converter.getClass().getName());
        }
        Map<Type, FromDbConverter<?, ?>> typeMap;
        Type fromType = genericTypes[0];
        Type toType = genericTypes[1];
        if (fromDbConverterMap.containsKey(fromType)) {
            typeMap = fromDbConverterMap.get(fromType);
        } else {
            typeMap = new HashMap<>();
            fromDbConverterMap.put(genericTypes[0], typeMap);
        }
        typeMap.put(toType, converter);
    }

    public void addToDbConverter(ToDbConverter<?> converter) {
        Type[] genericTypes = getConverterGenericTypes(converter);
        if (genericTypes == null || genericTypes.length == 0) {
            throw new NimbleException("Can't find generic params of converter of type " +
                    converter.getClass().getName());
        }
        toDbConverterMap.put(genericTypes[0], converter);
    }

    public Object convertFromDb(Object value, Class destType) {
        if (value == null) return null;
        FromDbConverter<?, ?> converter = null;
        Class valueType = value.getClass();
        if (fromDbConverterMap.containsKey(valueType)) {
            Map<Type, FromDbConverter<?, ?>> typeMap = fromDbConverterMap.get(valueType);
            if (typeMap.containsKey(destType)) {
                converter = typeMap.get(destType);
            }
        }

        if (converter != null) {
            //noinspection unchecked
            return ((FromDbConverter) converter).convert(value);
        } else {
            return defaultFromDbConverter.getValueForType(value, destType);
        }
    }

    public Object convertToDb(Object value) {
        if (value == null) return null;
        ToDbConverter<?> converter = toDbConverterMap.get(value.getClass());
        if (converter != null) {
            //noinspection unchecked
            return ((ToDbConverter)converter).convert(value);
        } else {
            return defaultToDbConverter.getValueForQueryParam(value);
        }
    }

    private static Type[] getConverterGenericTypes(Object converter) {
        Type[] genericInterfaces = converter.getClass().getGenericInterfaces();
        ParameterizedType genericSuperclass = null;
        if (genericInterfaces == null || genericInterfaces.length == 0) {
            genericSuperclass = (ParameterizedType) converter.getClass().getGenericSuperclass();
        } else {
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType &&
                        (genericInterface.toString().contains(FromDbConverter.class.getName()) ||
                                genericInterface.toString().contains(ToDbConverter.class.getName()))) {
                    genericSuperclass = (ParameterizedType) genericInterface;
                }
            }
        }
        if (genericSuperclass == null) {
            return null;
        }
        return genericSuperclass.getActualTypeArguments();
    }

}
