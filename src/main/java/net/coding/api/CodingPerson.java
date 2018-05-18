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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class CodingPerson extends CodingObject {

    /*package almost final*/ Coding root;

    protected String avatar_url, global_key;

    protected String location,blog,email,name,company;

    protected String html_url;

    /**
     * Gets the login ID of this user, like 'kohsuke'
     */
    public String getLogin() {
        return global_key;
    }

    /*package*/ CodingPerson wrapUp(Coding root) {
        this.root = root;
        return this;
    }

    /**
     * Gets the e-mail address of the user.
     */
    public String getEmail() throws IOException {
        populate();
        return email;
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * Depending on the original API call where this object is created, it may not contain everything.
     */
    protected synchronized void populate() throws IOException {
        if (created_at!=null)    return; // already populated

        root.retrieve().to(url, this);
    }

    /**
     * Lists up all the repositories using a 30 items page size.
     *
     * Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     */
    public PagedIterable<CodingRepository> listRepositories() {
        return listRepositories(30);
    }

    /**
     * Lists up all the repositories using the specified page size.
     *
     * @param pageSize size for each page of items returned by GitHub. Maximum page size is 100.
     *
     * Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     */
    public PagedIterable<CodingRepository> listRepositories(final int pageSize) {
        return new PagedIterable<CodingRepository>() {
            public PagedIterator<CodingRepository> _iterator(int pageSize) {
                return new PagedIterator<CodingRepository>(root.retrieve().asIterator("/users/" + getLogin() + "/repos?per_page=" + pageSize, CodingRepository[].class, pageSize)) {
                    @Override
                    protected void wrapUp(CodingRepository[] page) {
                        for (CodingRepository c : page)
                            c.wrap(root);
                    }
                };
            }
        };
    }

    /**
     * Gets the public repositories this user owns.
     *
     * <p>
     * To list your own repositories, including private repositories,
     * use {@link CodingMyself#listRepositories()}
     */
    public synchronized Map<String,CodingRepository> getRepositories() throws IOException {
        Map<String,CodingRepository> repositories = new TreeMap<String, CodingRepository>();
        for (CodingRepository r : listRepositories()) {
            repositories.put(r.getName(),r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Gets the human-readable name of the user, like "Kohsuke Kawaguchi"
     */
    public String getName() throws IOException {
        populate();
        return name;
    }
}
