/**
 * Jenkins plugin for Coding https://coding.net
 *
 * Copyright (C) 2016-2018 Shuanglei Tao <tsl0922@gmail.com>
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
package net.coding.jenkins.plugin.model;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class MergeRequest {
    private Integer id;
    private String title;
    private String body;
    private String merge_commit_sha;
    private String status;
    private String action;
    private Integer number;
    private String target_branch;
    private String source_branch;
    private String web_url;
    private User user;
}
