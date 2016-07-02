package net.coding.jenkins.plugin.cause;

import hudson.triggers.SCMTrigger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author tsl0922
 */
public class CodingWebHookCause extends SCMTrigger.SCMTriggerCause {
    private final CauseData data;

    public CodingWebHookCause(CauseData data) {
        super("");
        this.data = checkNotNull(data, "data must not be null");
    }

    public CauseData getData() {
        return data;
    }

    @Override
    public String getShortDescription() {
        return data.getShortDescription();
    }
}
