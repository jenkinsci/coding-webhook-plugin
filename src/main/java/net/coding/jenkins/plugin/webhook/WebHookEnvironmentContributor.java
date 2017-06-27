package net.coding.jenkins.plugin.webhook;

import net.coding.jenkins.plugin.cause.CauseData;
import net.coding.jenkins.plugin.cause.CodingWebHookCause;

import java.io.IOException;

import javax.annotation.Nonnull;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * @author tsl0922
 */
@Extension
public class WebHookEnvironmentContributor extends EnvironmentContributor {
    private static final String WEBHOOK_USER = "WEBHOOK_USER";
    private static final String WEBHOOK_USER_URL = "WEBHOOK_USER_URL";
    private static final String WEBHOOK_REPO_URL = "WEBHOOK_REPO_URL";
    private static final String WEBHOOK_COMMIT = "WEBHOOK_COMMIT";

    private static final String WEBHOOK_PUSH_REF = "WEBHOOK_PUSH_REF";
    private static final String WEBHOOK_PUSH_BEFORE = "WEBHOOK_PUSH_BEFORE";
    private static final String WEBHOOK_PUSH_AFTER = "WEBHOOK_PUSH_AFTER";

    private static final String WEBHOOK_MR_ID = "WEBHOOK_MR_ID";
    private static final String WEBHOOK_MR_IID = "WEBHOOK_MR_IID";
    private static final String WEBHOOK_MR_URL = "WEBHOOK_MR_URL";
    private static final String WEBHOOK_MR_TITLE = "WEBHOOK_MR_TITLE";
    private static final String WEBHOOK_MR_SOURCE_REPO_URL = "WEBHOOK_MR_SOURCE_REPO_URL";
    private static final String WEBHOOK_MR_SOURCE_BRANCH = "WEBHOOK_MR_SOURCE_BRANCH";
    private static final String WEBHOOK_MR_TARGET_BRANCH = "WEBHOOK_MR_TARGET_BRANCH";

    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        CodingWebHookCause cause = (CodingWebHookCause) r.getCause(CodingWebHookCause.class);
        if (cause == null) {
            return;
        }

        CauseData data = cause.getData();

        envs.putIfNotNull(WEBHOOK_USER, data.getUserGK());
        envs.putIfNotNull(WEBHOOK_USER_URL, data.getUserUrl());
        envs.putIfNotNull(WEBHOOK_REPO_URL, data.getRepoUrl());
        envs.putIfNotNull(WEBHOOK_COMMIT, data.getCommitId());

        switch (data.getActionType()) {
            case PUSH:
                envs.putIfNotNull(WEBHOOK_PUSH_REF, data.getRef());
                envs.putIfNotNull(WEBHOOK_PUSH_BEFORE, data.getBefore());
                envs.putIfNotNull(WEBHOOK_PUSH_AFTER, data.getAfter());
                break;
            case MR:
            case PR:
                envs.putIfNotNull(WEBHOOK_MR_ID, String.valueOf(data.getMergeRequestId()));
                envs.putIfNotNull(WEBHOOK_MR_IID, String.valueOf(data.getMergeRequestIid()));
                envs.putIfNotNull(WEBHOOK_MR_URL, data.getMergeRequestUrl());
                envs.putIfNotNull(WEBHOOK_MR_TITLE, data.getMergeRequestTitle());
                envs.putIfNotNull(WEBHOOK_MR_SOURCE_BRANCH, data.getSourceBranch());
                envs.putIfNotNull(WEBHOOK_MR_TARGET_BRANCH, data.getTargetBranch());
                if (data.getActionType() == CauseData.ActionType.PR) {
                    envs.putIfNotNull(WEBHOOK_MR_SOURCE_REPO_URL, data.getSourceProjectPath());
                }
                break;
            default:break;
        }

        super.buildEnvironmentFor(r, envs, listener);
    }
}
