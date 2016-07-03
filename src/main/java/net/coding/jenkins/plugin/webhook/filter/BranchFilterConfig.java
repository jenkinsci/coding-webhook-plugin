package net.coding.jenkins.plugin.webhook.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Robin Müller
 */
@Data
@AllArgsConstructor
public class BranchFilterConfig {
    private BranchFilterType type;
    private String includeBranchesSpec;
    private String excludeBranchesSpec;
    private String targetBranchRegex;
}
