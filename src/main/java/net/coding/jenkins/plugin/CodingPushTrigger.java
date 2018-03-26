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
package net.coding.jenkins.plugin;

import com.google.gson.Gson;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.coding.jenkins.plugin.bean.WebHookTask;
import net.coding.jenkins.plugin.webhook.CodingWebHook;
import net.coding.jenkins.plugin.webhook.IWebHookHelper;
import net.coding.jenkins.plugin.webhook.TriggerHandler;
import net.coding.jenkins.plugin.webhook.filter.BranchFilterConfig;
import net.coding.jenkins.plugin.webhook.filter.BranchFilterFactory;
import net.coding.jenkins.plugin.webhook.filter.BranchFilterType;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ObjectStreamException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tsl0922
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class CodingPushTrigger extends Trigger<Job<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(CodingPushTrigger.class.getName());

    private String webHookToken;
    private String apiToken;
    private String personalToken;
    private boolean triggerOnPush;
    private boolean triggerOnMergeRequest;
    private String mergeRequestTriggerAction;
    private boolean addResultNote;
    private boolean ciSkip;
    private BranchFilterType branchFilterType;
    private String includeBranchesSpec;
    private String excludeBranchesSpec;
    private String targetBranchRegex;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient TriggerHandler triggerHandler;

    @DataBoundConstructor
    public CodingPushTrigger(String webHookToken, String apiToken, String personalToken,
                             boolean triggerOnMergeRequest, String mergeRequestTriggerAction,
                             boolean triggerOnPush, boolean addResultNote, boolean ciSkip,
                             BranchFilterType branchFilterType, String includeBranchesSpec,
                             String excludeBranchesSpec, String targetBranchRegex) {
        this.webHookToken = webHookToken;
        this.apiToken = apiToken;
        this.personalToken = personalToken;
        this.triggerOnPush = triggerOnPush;
        this.triggerOnMergeRequest = triggerOnMergeRequest;
        this.mergeRequestTriggerAction = mergeRequestTriggerAction;
        this.addResultNote = addResultNote;
        this.ciSkip = ciSkip;
        this.branchFilterType = branchFilterType;
        this.includeBranchesSpec = includeBranchesSpec;
        this.excludeBranchesSpec = excludeBranchesSpec;
        this.targetBranchRegex = targetBranchRegex;

        initializeTriggerHandler();
    }

    private void initializeTriggerHandler() {
        if (this.branchFilterType == null) {
            this.branchFilterType = BranchFilterType.All;
        }
        BranchFilterConfig branchFilterConfig = new BranchFilterConfig(
                branchFilterType, includeBranchesSpec, excludeBranchesSpec, targetBranchRegex);
        this.triggerHandler = new TriggerHandler(
                this.triggerOnPush, this.triggerOnMergeRequest, mergeRequestTriggerAction,
                BranchFilterFactory.newBranchFilter(branchFilterConfig)
        );
    }

    @Override
    protected Object readResolve() throws ObjectStreamException {
        initializeTriggerHandler();
        return super.readResolve();
    }

    public void onPost(WebHookTask task) {
        IWebHookHelper helper = CodingWebHook.webHookHelper(task.getVersion());
        if (helper == null) {
            throw hudson.util.HttpResponses.error(400, "Bad Request");
        }
        if (helper.isSignatureValid(task, webHookToken)) {
            triggerHandler.handle(job, task, ciSkip);
        } else {
            LOGGER.log(Level.INFO, "Skipping due to invalid Signature for webHookTask: {0}", new Gson().toJson(task));
            throw hudson.util.HttpResponses.error(401, "Signature Invalid");
        }
    }

    public static CodingPushTrigger getFromJob(Job<?, ?> job) {
        CodingPushTrigger trigger = null;
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob p = (ParameterizedJobMixIn.ParameterizedJob) job;
            for (Object t : p.getTriggers().values()) {
                if (t instanceof CodingPushTrigger) {
                    trigger = (CodingPushTrigger) t;
                }
            }
        }
        return trigger;
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof Job
                    && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null
                    && item instanceof ParameterizedJobMixIn.ParameterizedJob;
        }

        @Override
        public String getDisplayName() {
            Job<?, ?> project = retrieveCurrentJob();
            if (project != null) {
                try {
                    return Messages.coding_trigger_title(retrieveProjectUrl(project));
                } catch (IllegalStateException ignored) {
                }
            }
            return Messages.coding_trigger_title_unknown();
        }

        private StringBuilder retrieveProjectUrl(Job<?, ?> project) {
            return new StringBuilder()
                    .append(Jenkins.getInstance().getRootUrl())
                    .append(CodingWebHook.WEBHOOK_URL)
                    .append(retrieveParentUrl(project))
                    .append('/').append(Util.rawEncode(project.getName()));
        }

        private StringBuilder retrieveParentUrl(Item item) {
            if (item.getParent() instanceof Item) {
                Item parent = (Item) item.getParent();
                return retrieveParentUrl(parent).append('/').append(Util.rawEncode(parent.getName()));
            } else {
                return new StringBuilder();
            }
        }

        private Job<?, ?> retrieveCurrentJob() {
            StaplerRequest request = Stapler.getCurrentRequest();
            if (request != null) {
                Ancestor ancestor = request.findAncestor(Job.class);
                return ancestor == null ? null : (Job<?, ?>) ancestor.getObject();
            }
            return null;
        }
    }
}
