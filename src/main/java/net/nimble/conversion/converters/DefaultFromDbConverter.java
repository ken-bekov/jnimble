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

package net.nimble.conversion.converters;

import net.nimble.exceptions.NimbleException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultFromDbConverter {

    public Object getValueForType(Object value, Class type) {
        if (value == null) return null;
         if (type.isEnum()) {
            if (value instanceof String) {
                return Enum.valueOf(type, (String) value);
            } else {
                String message = String.format("Can't convert value of type %s to enum %s",
                        value.getClass().getName(), type.getName());
                throw new NimbleException(message);
            }
        } else if (type == Double.class || type == double.class) {
            if (value.getClass() == Integer.class) {
                return ((Integer) value).doubleValue();
            } else if (value.getClass() == BigDecimal.class) {
                return ((BigDecimal) value).doubleValue();
            } else if (value.getClass() == BigInteger.class) {
                return ((BigInteger) value).doubleValue();
            } else if (value.getClass() == Float.class) {
                return ((Float) value).doubleValue();
            } else if (value.getClass() == Long.class) {
                return ((Long) value).doubleValue();
            } else if (value.getClass() == Short.class) {
                return ((Short) value).doubleValue();
            } else if (value.getClass() == Byte.class) {
                return ((Byte) value).doubleValue();
            }
        } else if (type == Float.class || type == float.class) {
            if (value.getClass() == Double.class) {
                return ((Double) value).floatValue();
            } else if (value.getClass() == BigDecimal.class) {
                return ((BigDecimal) value).floatValue();
            } else if (value.getClass() == BigInteger.class) {
                return ((BigInteger) value).floatValue();
            } else if (value.getClass() == Integer.class) {
                return ((Integer) value).floatValue();
            } else if (value.getClass() == Long.class) {
                return ((Long) value).floatValue();
            } else if (value.getClass() == Short.class) {
                return ((Short) value).floatValue();
            } else if (value.getClass() == Byte.class) {
                return ((Byte) value).floatValue();
            }
        } else if (type == Long.class || type == long.class) {
            if (value.getClass() == Double.class) {
                return ((Double) value).longValue();
            } else if (value.getClass() == BigDecimal.class) {
                return ((BigDecimal) value).longValue();
            } else if (value.getClass() == BigInteger.class) {
                return ((BigInteger) value).longValue();
            } else if (value.getClass() == Integer.class) {
                return ((Integer) value).longValue();
            } else if (value.getClass() == Float.class) {
                return ((Float) value).longValue();
            } else if (value.getClass() == Short.class) {
                return ((Short) value).longValue();
            } else if (value.getClass() == Byte.class) {
                return ((Byte) value).longValue();
            }
        } else if (type == Integer.class || type == int.class) {
            if (value.getClass() == Double.class) {
                return ((Double) value).intValue();
            } else if (value.getClass() == BigDecimal.class) {
                return ((BigDecimal) value).intValue();
            } else if (value.getClass() == BigInteger.class) {
                return ((BigInteger) value).intValue();
            } else if (value.getClass() == Float.class) {
                return ((Float) value).intValue();
            } else if (value.getClass() == Long.class) {
                return ((Long) value).intValue();
            } else if (value.getClass() == Short.class) {
                return ((Short) value).intValue();
            } else if (value.getClass() == Byte.class) {
                return ((Byte) value).intValue();
            }
        } else if (type == Short.class || type == short.class) {
            if (value.getClass() == Double.class) {
                return ((Double) value).shortValue();
            } else if (value.getClass() == BigDecimal.class) {
                return ((BigDecimal) value).shortValue();
            } else if (value.getClass() == BigInteger.class) {
                return ((BigInteger) value).shortValue();
            } else if (value.getClass() == Integer.class) {
                return ((Integer) value).shortValue();
            } else if (value.getClass() == Float.class) {
                return ((Float) value).shortValue();
            } else if (value.getClass() == Long.class) {
                return ((Long) value).shortValue();
            } else if (value.getClass() == Byte.class) {
                return ((Byte) value).shortValue();
            }
        } else if (type == Byte.class || type == byte.class) {
            if (value.getClass() == Double.class) {
                return ((Double) value).shortValue();
            } else if (value.getClass() == BigDecimal.class) {
                return ((BigDecimal) value).shortValue();
            } else if (value.getClass() == BigInteger.class) {
                return ((BigInteger) value).shortValue();
            } else if (value.getClass() == Integer.class) {
                return ((Integer) value).shortValue();
            } else if (value.getClass() == Float.class) {
                return ((Float) value).shortValue();
            } else if (value.getClass() == Long.class) {
                return ((Long) value).shortValue();
            } else if (value.getClass() == Short.class) {
                return ((Short) value).byteValue();
            }
        } else if (type == Boolean.class || type == boolean.class) {
            if (Number.class.isAssignableFrom(value.getClass())) {
                return !value.equals(0);
            }
        } else if (type == String.class && value.getClass() != String.class) {
            return value.toString();
        }

        return value;
    }
}
