package net.coding.jenkins.plugin.listener;

import net.coding.jenkins.plugin.cause.CodingWebHookCause;

import java.io.IOException;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildStepListener;
import hudson.model.Result;
import hudson.tasks.BuildStep;

/**
 * @author tsl0922
 */
@Extension
public class BuildResultListener extends BuildStepListener {
    @Override
    public void started(AbstractBuild build, BuildStep bs, hudson.model.BuildListener listener) {

    }

    @Override
    public void finished(AbstractBuild build, BuildStep bs, hudson.model.BuildListener listener, boolean canContinue) {
        if (build.getCause(CodingWebHookCause.class) != null
                && build.getResult() == Result.FAILURE
                && isCommitAmbiguous(build)) {
            build.setResult(Result.NOT_BUILT);
        }
    }

    private boolean isCommitAmbiguous(AbstractBuild build) {
        try {
            for (Object log : build.getLog(500)) {
                String str = log.toString();
                if (str.contains("stderr: fatal: ambiguous argument")
                        && str.contains("unknown revision or path not in the working tree")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
