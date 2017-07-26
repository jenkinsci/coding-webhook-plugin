package net.coding.jenkins.plugin;

import net.coding.jenkins.plugin.model.WebHook;
import net.coding.jenkins.plugin.webhook.CodingWebHook;
import net.coding.jenkins.plugin.webhook.TriggerHandler;
import net.coding.jenkins.plugin.webhook.filter.BranchFilterConfig;
import net.coding.jenkins.plugin.webhook.filter.BranchFilterFactory;
import net.coding.jenkins.plugin.webhook.filter.BranchFilterType;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.ObjectStreamException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * @author tsl0922
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class CodingPushTrigger extends Trigger<Job<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(CodingPushTrigger.class.getName());

    private String webHookToken;
    private String apiToken;
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
    public CodingPushTrigger(String webHookToken, String apiToken,
                             boolean triggerOnMergeRequest, String mergeRequestTriggerAction,
                             boolean triggerOnPush, boolean addResultNote, boolean ciSkip,
                             BranchFilterType branchFilterType, String includeBranchesSpec,
                             String excludeBranchesSpec, String targetBranchRegex) {
        this.webHookToken = webHookToken;
        this.apiToken = apiToken;
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

    public void onPost(WebHook webHook, String event) {
        if (StringUtils.isEmpty(webHookToken) || StringUtils.equals(webHookToken, webHook.getToken())) {
            triggerHandler.handle(job, webHook, event, ciSkip);
        } else {
            LOGGER.log(Level.INFO, "Skipping due to invalid webHook token: {0}", webHook.getToken());
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
                } catch (IllegalStateException e) {
                    // nothing to do
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
