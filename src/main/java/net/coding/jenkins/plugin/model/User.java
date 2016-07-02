package net.coding.jenkins.plugin.model;

import lombok.Data;

/**
 * @author tsl0922
 */
@Data
public class User {
    private String name;
    private String global_key;
    private String path;
    private String avatar;
    private String web_url;
}
