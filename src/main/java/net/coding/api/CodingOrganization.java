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
