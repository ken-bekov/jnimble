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

package net.nimble.meta;

import jdk.nashorn.internal.ir.annotations.Ignore;

import java.lang.reflect.AccessibleObject;

public class MemberEnumerator {

    private MemberFinder[] oneUnitArray = new MemberFinder[1];

    public void enumerate(AccessibleObject[] memberList, MemberFinder[] handlerList) {
        MemberHandlerParams params = new MemberHandlerParams();
        for (int i = 0; i < memberList.length; i++) {
            AccessibleObject member = memberList[i];
            if (member == null || member.getAnnotation(Ignore.class) != null) continue;
            for (MemberFinder handler : handlerList) {
                params.setIndex(i);
                params.setStopEnumeration(false);
                params.setNullifyMember(false);
                handler.handle(member, params);
                if (params.isNullifyMember()) {
                    memberList[i] = null;
                }
                if (params.isStopEnumeration()) return;
            }
        }
    }

    public void enumerate(AccessibleObject[] memberList, MemberFinder memberFinder) {
        oneUnitArray[0] = memberFinder;
        enumerate(memberList, oneUnitArray);
    }

}
