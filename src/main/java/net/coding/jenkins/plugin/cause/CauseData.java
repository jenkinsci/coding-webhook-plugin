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
package net.coding.jenkins.plugin.cause;

import org.apache.commons.lang.StringUtils;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class CauseData {
    private ActionType actionType;

    private String token;
    private String userGK;
    private String userName;
    private String userUrl;

    private String projectPath;
    private String ref;
    private String before;
    private String after;
    private String repoUrl;
    private String commitId;

    private Integer mergeRequestId;
    private Integer mergeRequestIid;
    private String mergeRequestTitle;
    private String mergeRequestUrl;
    private String mergeRequestBody;

    private String sourceProjectPath;
    private String sourceBranch;
    private String sourceRepoUrl;
    private String sourceUser;
    private String targetBranch;

    String getShortDescription() {
        return actionType.getShortDescription(this);
    }

    public enum ActionType {
        PUSH {
            @Override
            String getShortDescription(CauseData data) {
                String pushedBy = data.getUserName();
                if (pushedBy == null) {
                    return Messages.coding_action_push();
                } else {
                    return String.format(Messages.coding_action_push_by(), pushedBy);
                }
            }
        }, MR {
            @Override
            String getShortDescription(CauseData data) {
                String user = StringUtils.isEmpty(data.getUserName()) ? "Unknown" : data.getUserName();
                return String.format(Messages.coding_action_merge_request(),
                        user, data.getMergeRequestIid(), data.getMergeRequestTitle(),
                        data.getSourceBranch(), data.getTargetBranch());
            }
        }, PR {
            @Override
            String getShortDescription(CauseData data) {
                String user = StringUtils.isEmpty(data.getUserName()) ? "Unknown" : data.getUserName();
                return String.format(Messages.coding_action_pull_request(),
                        user, data.getMergeRequestIid(), data.getMergeRequestTitle(),
                        data.getSourceUser(), data.getSourceBranch(), data.getTargetBranch());
            }
        };

        abstract String getShortDescription(CauseData data);
    }

}
