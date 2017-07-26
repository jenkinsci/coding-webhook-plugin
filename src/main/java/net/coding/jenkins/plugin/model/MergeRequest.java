package net.coding.jenkins.plugin.model;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class MergeRequest {
    private Integer id;
    private String title;
    private String body;
    private String merge_commit_sha;
    private String status;
    private String action;
    private Integer number;
    private String target_branch;
    private String source_branch;
    private String web_url;
    private User user;
}
