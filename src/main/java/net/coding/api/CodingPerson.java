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
