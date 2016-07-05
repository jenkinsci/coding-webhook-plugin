package net.coding.jenkins.plugin.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String projectPath() {
        Pattern pattern = Pattern.compile("https?://[^/]+/(?:u|t)/([^/]+)/p/([^/]+).*");
        Matcher matcher = pattern.matcher(web_url);
        if (matcher.matches()) {
            return String.format("%s/%s", matcher.group(1), matcher.group(2));
        }
        return "";
    }
}
