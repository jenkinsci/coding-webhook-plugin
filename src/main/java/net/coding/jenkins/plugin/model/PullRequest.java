package net.coding.jenkins.plugin.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author tsl0922
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PullRequest extends MergeRequest {
    private Repository source_repository;
}
