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
package net.coding.jenkins.plugin.v1.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class RepositoryTest {
    @Test
    public void projectApiUrl() throws Exception {
        Repository repository = new Repository();
        repository.setWeb_url("https://codingcorp.coding.net/p/Demo");
        assertEquals("https://codingcorp.coding.net/api/user/codingcorp/project/Demo", repository.projectApiUrl());
        repository.setWeb_url("https://coding.net/u/wusisu/p/screeps");
        assertEquals("https://coding.net/api/user/wusisu/project/screeps", repository.projectApiUrl());
    }
}
