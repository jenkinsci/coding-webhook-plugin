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
package net.coding.jenkins.plugin.model.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class MergeRequest extends CodingBaseEvent implements Serializable {

    private static final long serialVersionUID = -101752450737183314L;

    private String action;
    private long number;
    private net.coding.jenkins.plugin.model.MergeRequest mergeRequest;

    @Override
    public String action() {
        return "merge request";
    }
}
