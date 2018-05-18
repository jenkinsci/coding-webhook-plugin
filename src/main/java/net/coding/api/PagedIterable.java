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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class PagedIterable<T> implements Iterable<T> {
    /**
     * Page size. 0 is default.
     */
    private int size = 0;

    /**
     * Sets the pagination size.
     *
     * <p>
     * When set to non-zero, each API call will retrieve this many entries.
     */
    public PagedIterable<T> withPageSize(int size) {
        this.size = size;
        return this;
    }

    public final PagedIterator<T> iterator() {
        return _iterator(size);
    }

    public abstract PagedIterator<T> _iterator(int pageSize);

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     */
    public List<T> asList() {
        List<T> r = new ArrayList<T>();
        for(PagedIterator<T> i = iterator(); i.hasNext();) {
            r.addAll(i.nextPage());
        }
        return r;
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     */
    public Set<T> asSet() {
        LinkedHashSet<T> r = new LinkedHashSet<T>();
        for(PagedIterator<T> i = iterator(); i.hasNext();) {
            r.addAll(i.nextPage());
        }
        return r;
    }
}