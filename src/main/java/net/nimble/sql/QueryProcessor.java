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

package net.nimble.sql;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryProcessor {

    private static Pattern pattern = Pattern.compile("(?::)([\\w_]+)");

    public static QueryParamNames extractParamNames(String query) {
        QueryParamNames result = new QueryParamNames();
        Matcher matcher = pattern.matcher(query);
        while (matcher.find()) {
            result.getNames().add(matcher.group(1));
            result.getIndexes().add(new int[]{matcher.start(), matcher.end()});
        }
        return result;
    }

    public static String replaceParamNames(String query, QueryParamNames parsingResult, Map<String, Object> valueMap) {
        StringBuilder builder = new StringBuilder(query.length());
        int lastPosition = 0;
        for (int paramIndex = 0; paramIndex < parsingResult.getNames().size(); paramIndex++) {
            String paramName = parsingResult.getNames().get(paramIndex);
            int startPos = parsingResult.getIndexes().get(paramIndex)[0];
            int endPos = parsingResult.getIndexes().get(paramIndex)[1];
            builder.append(query, lastPosition, startPos);
            Object value = valueMap.get(paramName);
            if (value!=null && value.getClass().isArray()) {
                appendParamMarks(builder, Array.getLength(value));
            } else if (value!=null && Collection.class.isAssignableFrom(value.getClass())) {
                appendParamMarks(builder, ((Collection) value).size());
            } else {
                builder.append("?");
            }
            lastPosition = endPos;
        }

        builder.append(query, lastPosition, query.length());

        return builder.toString();
    }

    private static void appendParamMarks(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            if (i > 0) builder.append(",");
            builder.append("?");
        }
    }
}
