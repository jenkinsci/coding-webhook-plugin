package net.coding.jenkins.plugin.webhook.filter;

/**
 * @author Robin Müller
 */
public interface BranchFilter {

    boolean isBranchAllowed(String branchName);
}
