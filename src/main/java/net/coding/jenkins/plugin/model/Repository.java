package net.coding.jenkins.plugin.model;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class Repository {
    private String project_id;
    private String ssh_url;
    private String https_url;
    private String git_url;
    private String name;
    private String description;
    private String web_url;
    private User owner;
}
