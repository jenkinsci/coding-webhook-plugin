package net.coding.jenkins.plugin.cause;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class CauseData {
    private ActionType actionType;

    private String token;
    private String userName;
    private String userUrl;

    private String ref;
    private String before;
    private String after;
    private String repoUrl;

    private Integer mergeRequestIid;
    private String mergeRequestTitle;
    private String mergeRequestUrl;
    private String mergeRequestBody;

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
                return String.format(Messages.coding_action_merge_request(), data.getMergeRequestIid(),
                        data.getSourceBranch(), data.getTargetBranch());
            }
        }, PR {
            @Override
            String getShortDescription(CauseData data) {
                return String.format(Messages.coding_action_pull_request(), data.getMergeRequestIid(),
                        data.getSourceUser(), data.getSourceBranch(), data.getTargetBranch());
            }
        };

        abstract String getShortDescription(CauseData data);
    }

}
