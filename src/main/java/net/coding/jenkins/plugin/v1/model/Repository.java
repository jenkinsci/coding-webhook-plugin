/**
 * Jenkins plugin for Coding https://coding.net
 *
 * Copyright (c) 2016-2018 Shuanglei Tao <tsl0922@gmail.com>
 * Copyright (c) 2016-present, Coding, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.coding.jenkins.plugin.v1.model;

import java.net.URI;
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
        Pattern pattern = Pattern.compile("https?://[^/]+/[ut]/([^/]+)/p/([^/]+).*");
        Matcher matcher = pattern.matcher(web_url);
        if (matcher.matches()) {
            // is professional
            return String.format("%s/%s", matcher.group(1), matcher.group(2));
        }
        pattern = Pattern.compile("https?://([^./]+)\\.[^/]+/p/([^/]+).*");
        matcher = pattern.matcher(web_url);
        if (matcher.matches()) {
            // is enterprise
            return String.format("%s/%s", matcher.group(1), matcher.group(2));
        }
        return "";
    }

    public String projectApiUrl() {
        Pattern pattern = Pattern.compile("(https?://[^/]+)/[ut]/([^/]+)/p/([^/]+).*");
        Matcher matcher = pattern.matcher(web_url);
        if (matcher.matches()) {
            // is professional
            return String.format("%s/api/user/%s/project/%s", matcher.group(1), matcher.group(2), matcher.group(3));
        }
        pattern = Pattern.compile("(https?://[^/]+)/p/([^/]+).*");
        matcher = pattern.matcher(web_url);
        if (matcher.matches()) {
            // is enterprise
            String host = matcher.group(1);
            String projectName = matcher.group(2);
            try {
                String path = new URI(https_url).getPath();
                path = path.substring(1).replace(".git", "");
                String teamName = path.split("/")[0];
                return String.format("%s/api/user/%s/project/%s", host, teamName, projectName);
            } catch (Exception ignored) {
                // Fallback
                Matcher enterprise = Pattern.compile("https?://([^.]+).*").matcher(host);
                if (enterprise.matches()) {
                    return String.format("%s/api/user/%s/project/%s", host, enterprise.group(1), projectName);
                }
            }
        }
        return "";
    }
}
