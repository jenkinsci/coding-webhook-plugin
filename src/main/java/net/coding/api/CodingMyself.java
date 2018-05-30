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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CodingMyself extends CodingUser {

    /**
     * Type of repositories returned during listing.
     */
    public enum RepositoryListFilter {
        /**
         * All public and private repositories that current user has access or collaborates to
         */
        ALL,
        /**
         * Public and private repositories owned by current user
         */
        OWNER,
        /**
         * Public repositories that current user has access or collaborates to
         */
        PUBLIC,
        /**
         * Private repositories that current user has access or collaborates to
         */
        PRIVATE,
        /**
         * Public and private repositories that current user is a member
         */
        MEMBER;
    }

    /**
     * Lists up all repositories this user owns (public and private).
     *
     * Unlike {@link #getAllRepositories()}, this does not wait until all the repositories are returned.
     * Repositories are returned by GitHub API with a 30 items per page.
     */
    @Override
    public PagedIterable<CodingRepository> listRepositories() {
        return listRepositories(30);
    }

    /**
     * Gets the all repositories this user owns (public and private).
     */
    public synchronized Map<String,CodingRepository> getAllRepositories() throws IOException {
        Map<String,CodingRepository> repositories = new TreeMap<String, CodingRepository>();
        for (CodingRepository r : listAllRepositories()) {
            repositories.put(r.getName(),r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * @deprecated
     *      Use {@link #listRepositories()}
     */
    public PagedIterable<CodingRepository> listAllRepositories() {
        return listRepositories();
    }

    /**
     * Gets the organization that this user belongs to.
     */
    public CodingPersonSet<CodingOrganization> getAllOrganizations() throws IOException {
        CodingPersonSet<CodingOrganization> orgs = new CodingPersonSet<CodingOrganization>();
//        Set<String> names = new HashSet<String>();
//        for (CodingOrganization o : root.retrieve().to("/user/orgs", CodingOrganization[].class)) {
//            if (names.add(o.getLogin()))    // in case of rumoured duplicates in the data
//                orgs.add(root.getOrganization(o.getLogin()));
//        }

        orgs.add(root.getOrganization(null));
        return orgs;
    }

    /**
     * List repositories that are accessible to the authenticated user (public and private) using the specified page size.
     *
     * This includes repositories owned by the authenticated user, repositories that belong to other users
     * where the authenticated user is a collaborator, and other organizations' repositories that the authenticated
     * user has access to through an organization membership.
     *
     * @param pageSize size for each page of items returned by GitHub. Maximum page size is 100.
     *
     * Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     */
    public PagedIterable<CodingRepository> listRepositories(final int pageSize) {
        return listRepositories(pageSize, RepositoryListFilter.ALL);
    }

    /**
     * List repositories of a certain type that are accessible by current authenticated user using the specified page size.
     *
     * @param pageSize size for each page of items returned by GitHub. Maximum page size is 100.
     * @param repoType type of repository returned in the listing
     */
    public PagedIterable<CodingRepository> listRepositories(final int pageSize, final RepositoryListFilter repoType) {
        return new PagedIterable<CodingRepository>() {
            public PagedIterator<CodingRepository> _iterator(int pageSize) {
                return new PagedIterator<CodingRepository>(root.retrieve().with("type",repoType).asIterator("/user/repos", CodingRepository[].class, pageSize)) {
                    @Override
                    protected void wrapUp(CodingRepository[] page) {
                        for (CodingRepository c : page)
                            c.wrap(root);
                    }
                };
            }
        }.withPageSize(pageSize);
    }

    /**
     * Returns the read-only list of e-mail addresses configured for you.
     *
     * This corresponds to the stuff you configure in https://github.com/settings/emails,
     * and not to be confused with {@link #getEmail()} that shows your public e-mail address
     * set in https://github.com/settings/profile
     *
     * @return
     *      Always non-null.
     */
    public List<CodingEmail> getEmails2() throws IOException {
        String fetchEmail = root.retrieve().to("/api/account/email", String.class);
        email = fetchEmail;
        CodingEmail codingEmail = new CodingEmail();
        codingEmail.email = fetchEmail;
        codingEmail.primary = true;
        return Collections.unmodifiableList(Collections.singletonList(codingEmail));
    }

}
