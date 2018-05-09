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
package net.coding.jenkins.plugin.webhook;

import net.coding.jenkins.plugin.bean.WebHookTask;
import net.coding.jenkins.plugin.cause.CauseData;
import net.coding.jenkins.plugin.cause.CodingWebHookCause;
import net.coding.jenkins.plugin.model.Commit;
import net.coding.jenkins.plugin.model.MergeRequest;
import net.coding.jenkins.plugin.model.Ref;
import net.coding.jenkins.plugin.model.event.Push;
import net.coding.jenkins.plugin.webhook.filter.BranchFilter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.URIish;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.plugins.git.RevisionParameterAction;
import jenkins.model.ParameterizedJobMixIn;

import static net.coding.jenkins.plugin.cause.CauseData.ActionType;
import static org.eclipse.jgit.lib.Repository.shortenRefName;

/**
 * @author tsl0922
 */
public class TriggerHandler {
    private final static Logger LOGGER = Logger.getLogger(TriggerHandler.class.getName());

    private static final String CI_SKIP = "[ci-skip]";

    private boolean triggerOnPush;
    private boolean triggerOnMergeRequest;
    private String mergeRequestTriggerAction;
    private BranchFilter branchFilter;

    public TriggerHandler(boolean triggerOnPush, boolean triggerOnMergeRequest, String mergeRequestTriggerAction, BranchFilter branchFilter) {
        this.triggerOnPush = triggerOnPush;
        this.triggerOnMergeRequest = triggerOnMergeRequest;
        this.mergeRequestTriggerAction = mergeRequestTriggerAction;
        this.branchFilter = branchFilter;
    }

    public void handle(Job<?, ?> job, WebHookTask task, boolean ciSkip) {
        boolean shouldTrigger = false;
        ActionType actionType = null;
        String branch = null;
        String event = task.getEvent();
        LOGGER.log(Level.FINEST, "handle web_hook for event: {0}", event);
        switch (event) {
            case WebHookTask.EVENT_PUSH:
                if (isNoRemoveBranchPush(task)) {
                    shouldTrigger = triggerOnPush;
                    actionType = ActionType.PUSH;
                    branch = shortenRef(task.getPush().getRef());
                }
                break;
            case WebHookTask.EVENT_MERGE_REQUEST:
                net.coding.jenkins.plugin.model.event.MergeRequest mrEvent = task.getMergeRequest();
                MergeRequest mergeRequest = mrEvent.getMergeRequest();
                if (!isTriggerAction(mrEvent.getAction())) {
                    LOGGER.log(Level.INFO, "Skipping action: {0}, MR #{1} {2}",
                            new Object[]{mrEvent.getAction(), mergeRequest.getNumber(), mergeRequest.getTitle()});
                    return;
                }
                shouldTrigger = triggerOnMergeRequest;
                actionType = ActionType.MR;
                branch = mergeRequest.getBase().getRef();
                break;
            default:
                break;
        }
        if (actionType == null) {
            return;
        }
        if (ciSkip && isCiSkip(task, actionType)) {
            LOGGER.log(Level.INFO, "Skipping due to ci-skip.");
            return;
        }
        if (!branchFilter.isBranchAllowed(branch)) {
            LOGGER.log(Level.INFO, "Branch {0} is not allowed", branch);
            return;
        }
        if (shouldTrigger) {
            LOGGER.log(Level.FINEST, "Schedule to build for branch: {0}", branch);
            scheduleBuild(job, createActions(job, task, actionType));
        }
    }

    private void scheduleBuild(Job<?, ?> job, Action[] actions) {
        int projectBuildDelay = 0;
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob abstractProject = (ParameterizedJobMixIn.ParameterizedJob) job;
            if (abstractProject.getQuietPeriod() > projectBuildDelay) {
                projectBuildDelay = abstractProject.getQuietPeriod();
            }
        }
        asParameterizedJobMixIn(job).scheduleBuild2(projectBuildDelay, actions);
    }

    private Action[] createActions(Job<?, ?> job, WebHookTask task, ActionType actionType) {
        List<Action> actions = new ArrayList<>();
        actions.add(new CauseAction(new CodingWebHookCause(buildCauseData(task, actionType))));
        return actions.toArray(new Action[actions.size()]);
    }

    private CauseData buildCauseData(WebHookTask task, ActionType actionType) {

        CauseData data = new CauseData();
        data.setActionType(actionType);
        data.setToken(task.getSignature());
        if (task.getSender() != null) {
            data.setUserGK(task.getSender().getLogin());
            data.setUserName(task.getSender().getName());
            data.setUserUrl(task.getSender().getHtml_url());
        }
        data.setRepoUrl(task.getRepository().getSsh_url());
        data.setProjectHtmlUrl(task.getRepository().getHtml_url());
        data.setFullName(task.getRepository().getFull_name());

        switch (actionType) {
            case PUSH:
                Push push = task.getPush();
                data.setRef(push.getRef());
                data.setBefore(push.getBefore());
                data.setAfter(push.getAfter());
                data.setCommitId(push.getAfter());
                break;
            case MR:
                MergeRequest mr = task.getMergeRequest().getMergeRequest();
                data.setMergeRequestId(mr.getId());
                data.setCommitId(mr.getMerge_commit_sha());
                data.setMergeRequestIid(mr.getNumber());
                data.setMergeRequestTitle(mr.getTitle());
                data.setMergeRequestBody(mr.getBody());
                data.setMergeRequestUrl(mr.getHtml_url());
                data.setSourceBranch(shortenRef(mr.getHead().getRef()));
                data.setTargetBranch(shortenRef(mr.getBase().getRef()));
                if (mr.getUser() != null) {
                    data.setUserName(mr.getUser().getName());
                    data.setUserUrl(mr.getUser().getHtml_url());
                }
            case PR:
                mr = task.getMergeRequest().getMergeRequest();
                Ref head = mr.getHead();
                data.setSourceProjectPath(head.getRepo().getFull_name());
                data.setSourceRepoUrl(head.getRepo().getSsh_url());
                data.setSourceUser(head.getRepo().getOwner().getLogin());
            default:
                break;
        }
        return data;
    }

    private String shortenRef(String ref) {
        return ref == null ? null : shortenRefName(ref);
    }

    private static <T extends Job> ParameterizedJobMixIn asParameterizedJobMixIn(final T job) {
        return new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return job;
            }
        };
    }

    private URIish createUrIish(WebHookTask task) {
        try {
            if (task.getRepository() != null) {
                return new URIish(task.getRepository().getSsh_url());
            }
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "could not parse URL");
        }
        return null;
    }

    private boolean isTriggerAction(String action) {
        return StringUtils.isEmpty(mergeRequestTriggerAction) || StringUtils.contains(mergeRequestTriggerAction, action);
    }

    private boolean isNoRemoveBranchPush(WebHookTask task) {
        return task.getPush().getAfter() != null
                && !task.getPush().getAfter().equals(ObjectId.zeroId().name());
    }

    private boolean isCiSkip(WebHookTask task, ActionType actionType) {
        switch (actionType) {
            case PUSH:
                List<Commit> commits = task.getPush().getCommits();
                return commits != null &&
                        !commits.isEmpty() &&
                        commits.get(commits.size() - 1).getMessage() != null &&
                        StringUtils.containsIgnoreCase(commits.get(commits.size() - 1).getMessage(), CI_SKIP);
            case MR:
            case PR:
                net.coding.jenkins.plugin.model.event.MergeRequest mrEvent = task.getMergeRequest();
                return StringUtils.containsIgnoreCase(mrEvent.getMergeRequest().getTitle(), CI_SKIP);
            default:
                return false;
        }
    }
}
