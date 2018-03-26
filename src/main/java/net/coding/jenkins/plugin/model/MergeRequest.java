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
package net.coding.jenkins.plugin.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MergeRequest implements Serializable {

    private static final long serialVersionUID = -8632601797141226063L;

    private long id;
    private String url;
    private String html_url;
    private String patch_url;
    private String diff_url;
    private long number;
    private String state;
    private String title;
    private String body;
    private User user;
    private String merge_commit_sha;

    private Ref head;
    private Ref base;
    private boolean merged;
    private User merged_by;
    private int comments;
    private int commits;
    private int additions;
    private int deletions;
    private int changed_files;
}
