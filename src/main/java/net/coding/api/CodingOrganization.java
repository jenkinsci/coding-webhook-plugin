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

public class CodingOrganization extends CodingPerson {
    protected String avatar_url, gravatar_id;

    /*package*/ CodingOrganization wrapUp(Coding root) {
        return (CodingOrganization)super.wrapUp(root);
    }

    /**
     * Finds a team that has the given name in its {@link CodingTeam#getName()}
     */
    public CodingTeam getTeamByName(String name) throws IOException {
        for (CodingTeam t : listTeams()) {
            if(t.getName().equals(name))
                return t;
        }
        return null;
    }

    /**
     * List up all the teams.
     */
    public PagedIterable<CodingTeam> listTeams() throws IOException {
        return new PagedIterable<CodingTeam>() {
            public PagedIterator<CodingTeam> _iterator(int pageSize) {
                return new PagedIterator<CodingTeam>(root.retrieve().asIterator(String.format("/orgs/%s/teams", getLogin()), CodingTeam[].class, pageSize)) {
                    @Override
                    protected void wrapUp(CodingTeam[] page) {
                        for (CodingTeam c : page)
                            c.wrapUp(CodingOrganization.this);
                    }
                };
            }
        };
    }
}
