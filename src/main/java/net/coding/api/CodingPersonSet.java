/**
 * Jenkins plugin for Coding https://coding.net
 *
 * Copyright (c) 2016-2018 Shuanglei Tao <tsl0922@gmail.com>
 * Copyright (c) 2016-present, Coding, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.coding.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class CodingPersonSet<T extends CodingPerson> extends HashSet<T> {
    private static final long serialVersionUID = 1L;

    public CodingPersonSet() {
    }

    public CodingPersonSet(Collection<? extends T> c) {
        super(c);
    }

    public CodingPersonSet(T... c) {
        super(Arrays.asList(c));
    }

    public CodingPersonSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CodingPersonSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Finds the item by its login.
     */
    public T byLogin(String login) {
        for (T t : this)
            if (t.getLogin().equals(login))
                return t;
        return null;
    }
}
