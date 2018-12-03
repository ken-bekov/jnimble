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

package net.nimble.meta.extracts;

import net.nimble.exceptions.NimbleException;
import net.nimble.exceptions.NimbleReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectValueExtract implements ValueExtract {

    @Override
    public Map<String, Object> getValueMap(List<String> valueNames, Object object) {
        Map<String, Object> valueMap = new HashMap<>();
        if (object == null || valueNames.size() == 0) return valueMap;

        Class type = object.getClass();
        Method[] methods = type.getMethods();
        for (String valueName : valueNames) {

            String valueNameUpper = valueName.toUpperCase();
            Method getter = null;
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method == null) continue;

                String methodName = method.getName();
                if (methodName.length() <= 3 || !methodName.startsWith("get")) {
                    methods[i] = null;
                    continue;
                }

                if (methodName.substring(3).toUpperCase().equals(valueNameUpper)) {
                    methods[i] = null;
                    getter = method;
                    break;
                }
            }

            if (getter == null) {
                throw new NimbleException("Can't find getter method for parameter :" + valueName);
            }
            try {
                getter.setAccessible(true);
                Object value = getter.invoke(object);
                valueMap.put(valueName, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new NimbleReflectionException(e);
            }
        }

        return valueMap;
    }
}
