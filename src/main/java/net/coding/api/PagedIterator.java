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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class PagedIterator<T> implements Iterator<T> {
    private final Iterator<T[]> base;

    /**
     * Current batch that we retrieved but haven't returned to the caller.
     */
    private T[] current;
    private int pos;

    /*package*/ PagedIterator(Iterator<T[]> base) {
        this.base = base;
    }

    protected abstract void wrapUp(T[] page);

    public boolean hasNext() {
        fetch();
        return current!=null;
    }

    public T next() {
        fetch();
        if (current==null)  throw new NoSuchElementException();
        return current[pos++];
    }

    private void fetch() {
        while (current==null || current.length<=pos) {
            if (!base.hasNext()) {// no more to retrieve
                current = null;
                pos = 0;
                return;
            }

            current = base.next();
            wrapUp(current);
            pos = 0;
        }
        // invariant at the end: there's some data to retrieve
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the next page worth of data.
     */
    public List<T> nextPage() {
        fetch();
        List<T> r = Arrays.asList(current);
        r = r.subList(pos,r.size());
        current = null;
        pos = 0;
        return r;
    }
}