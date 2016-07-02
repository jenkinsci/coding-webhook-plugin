package net.coding.jenkins.plugin.model;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class Commit {
    private Committer committer;
    private String web_url;
    private String short_message;
    private String sha;
}
