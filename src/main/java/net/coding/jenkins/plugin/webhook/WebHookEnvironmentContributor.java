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
    private static final String WEBHOOK_PUSH_BEFORE = "WEBHOOK_PUSH_BEFORE";
    private static final String WEBHOOK_PUSH_AFTER = "WEBHOOK_PUSH_AFTER";

    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        CodingWebHookCause cause = (CodingWebHookCause) r.getCause(CodingWebHookCause.class);
        if (cause == null) {
            return;
        }
        CauseData data = cause.getData();
        if (data.getActionType() == CauseData.ActionType.PUSH) {
            envs.put(WEBHOOK_PUSH_BEFORE, data.getBefore());
            envs.put(WEBHOOK_PUSH_AFTER, data.getAfter());
        }
        super.buildEnvironmentFor(r, envs, listener);
    }
}
