/**
 *  Copyright (c) 2011- Kohsuke Kawaguchi and other contributors
 *  Copyright (c) 2016-present, Coding, Inc.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */
package net.coding.api;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.lang.reflect.Field;

public class CodingObject {

    protected String url;
    protected int id;
    protected String created_at;
    protected String updated_at;

    /**
     * String representation to assist debugging and inspection. The output format of this string
     * is not a committed part of the API and is subject to change.
     */
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, TOSTRING_STYLE, null, null, false, false) {
            @Override
            protected boolean accept(Field field) {
                return super.accept(field) && !field.isAnnotationPresent(SkipFromToString.class);
            }
        }.toString();
    }

    private static final ToStringStyle TOSTRING_STYLE = new ToStringStyle() {
        {
            this.setUseShortClassName(true);
        }

        @Override
        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            // skip unimportant properties. '_' is a heuristics as important properties tend to have short names
            if (fieldName.contains("_"))
                return;
            // avoid recursing other GHObject
            if (value instanceof CodingObject)
                return;
            // likewise no point in showing root
            if (value instanceof Coding)
                return;

            super.append(buffer,fieldName,value,fullDetail);
        }
    };
}
