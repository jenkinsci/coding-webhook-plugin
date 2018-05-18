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

public class CodingTeam {

    private String name,permission,slug;

    private CodingOrganization organization;

    protected /*final*/ CodingOrganization org;

    private String global_key, avatar;

    private Integer id;

    public CodingOrganization getOrganization() {
        return org;
    }

    /*package*/ CodingTeam wrapUp(CodingOrganization owner) {
        this.org = owner;
        return this;
    }

    /*package*/ CodingTeam wrapUp(Coding root) { // auto-wrapUp when organization is known from GET /user/teams
        if (this.organization == null) {
            this.organization = new CodingOrganization();
            this.organization.global_key = "coding_dot_net";
        }
        this.organization.wrapUp(root);
        return wrapUp(organization);
    }

    public String getName() {
        return name;
    }
}
