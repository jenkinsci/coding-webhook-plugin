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
package net.coding.jenkins.plugin.oauth;

import hudson.ExtensionPoint;

import java.util.Collection;

/**
 * Extension point to be implemented by plugins to request additional scopes.
 * @author Kohsuke Kawaguchi
 */
public abstract class CodingOAuthScope implements ExtensionPoint {
    /**
     * Returns a collection of scopes to request.
     * See https://open.coding.net/references/personal-access-token/#%E8%AE%BF%E9%97%AE%E4%BB%A4%E7%89%8C%E7%9A%84%E6%9D%83%E9%99%90
     */
    public abstract Collection<String> getScopesToRequest();
}
