package net.coding.jenkins.plugin.model;

import java.util.List;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class WebHook {
    private String ref;
    private List<Commit> commits;
    private String before;
    private String after;
    private Repository repository;
    private String event;
    private String token;
    private MergeRequest merge_request;
    private PullRequest pull_request;
    private User user;
}
