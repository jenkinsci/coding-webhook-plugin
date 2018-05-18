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

public class CodingRepository {
    /*package almost final*/ Coding root;

    private String description, homepage, name, full_name;
    private String html_url;    // this is the UI

    private CodingUser owner;   // not fully populated. beware.

    private boolean _private;
    private boolean permissions_pull;
    private boolean permissions_push;
    private boolean permissions_admin;

    /**
     * Short repository name without the owner. For example 'jenkins' in case of http://github.com/jenkinsci/jenkins
     */
    public String getName() {
        return name;
    }

    /*package*/ CodingRepository wrap(Coding root) {
        this.root = root;
        return this;
    }

    public CodingUser getOwner() throws IOException {
        return root.getUser(owner.getLogin());   // because 'owner' isn't fully populated
    }

    public boolean isPrivate() {
        return _private;
    }

    public boolean hasPullAccess() {
        return permissions_pull;
    }

    public boolean hasPushAccess() {
        return permissions_push;
    }

    public boolean hasAdminAccess() {
        return permissions_admin;
    }
}
