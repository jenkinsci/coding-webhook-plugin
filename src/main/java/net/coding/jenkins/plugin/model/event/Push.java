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
import net.coding.jenkins.plugin.model.Commit;
import net.coding.jenkins.plugin.model.PersonIdent;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Push extends CodingBaseEvent implements Serializable {

    private static final long serialVersionUID = 3750761687807284636L;

    private String ref;
    private String before;
    private String after;
    private String base_ref;
    private String compare;
    private List<Commit> commits;
    private Commit head_commit;
    private PersonIdent pusher;

    @Override
    public String action() {
        return "push";
    }
}
