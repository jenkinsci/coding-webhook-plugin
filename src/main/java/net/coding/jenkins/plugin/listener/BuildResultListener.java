/**
 * Jenkins plugin for Coding https://coding.net
 *
 * Copyright (C) 2016-2017 Shuanglei Tao <tsl0922@gmail.com>
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
