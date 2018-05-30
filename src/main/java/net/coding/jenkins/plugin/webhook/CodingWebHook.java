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
package net.coding.jenkins.plugin.webhook;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.UnprotectedRootAction;
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.security.csrf.CrumbExclusion;
import jenkins.model.Jenkins;
import net.coding.jenkins.plugin.CodingPushTrigger;
import net.coding.jenkins.plugin.Utils;
import net.coding.jenkins.plugin.bean.WebHookTask;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tsl0922
 */
@Extension
public class CodingWebHook implements UnprotectedRootAction {
    private static final Logger LOGGER = Logger.getLogger(CodingWebHook.class.getName());

    public static final String WEBHOOK_URL = "coding";
    public static final String PERSONAL_TOKEN_HEADER = "Authorization";
    public static final String API_TOKEN_PARAM = "private_token";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return WEBHOOK_URL;
    }

    public static IWebHookHelper webHookHelper(String version) {
        switch (version) {
            case WebHookHelperV2.version:
                return new WebHookHelperV2();
            case WebHookHelperV1.version:
                return new WebHookHelperV1();
            default:
                LOGGER.log(Level.WARNING, "Unsupported WebHook Version: {0}", version);
                return null;
        }
    }

    private static String version(final StaplerRequest request) {
        String version = request.getHeader("X-Coding-WebHook-Version");
        if (StringUtils.isNotEmpty(version)) {
            return version;
        }
        String delivery = request.getHeader("X-Coding-Delivery");
        if (StringUtils.isNotEmpty(delivery)) {
            return WebHookHelperV2.version;
        }
        return WebHookHelperV1.version;
    }

    public void getDynamic(final String projectName, final StaplerRequest request, StaplerResponse response) {
        LOGGER.log(Level.INFO, "WebHook called with url: {0}", request.getRequestURIWithQueryString());
        Iterator<String> restOfPathParts = Splitter.on('/').omitEmptyStrings().split(request.getRestOfPath()).iterator();
        Job<?, ?> project = resolveProject(projectName, restOfPathParts);
        if (project == null) {
            throw HttpResponses.notFound();
        }
        LOGGER.log(Level.INFO, "Resolved project: {0}", project.getName());

        String method = request.getMethod();
        switch (method) {
            case "POST":
                String eventHeader = request.getHeader("X-Coding-Event");
                if (eventHeader == null) {
                    LOGGER.log(Level.INFO, "Missing X-Coding-Event header");
                    throw hudson.util.HttpResponses.status(HttpServletResponse.SC_BAD_REQUEST);
                }
                if (StringUtils.equals(eventHeader, "ping")) {
                    LOGGER.log(Level.INFO, "Received coding webHook ping: {0}", Utils.getRequestBody(request));
                    throw hudson.util.HttpResponses.ok();
                }
                IWebHookHelper helper = webHookHelper(version(request));
                if (helper == null) {
                    return;
                }
                WebHookTask task;
                try {
                    task = helper.parseTaskFromRequest(request);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected Exception occurred while parsing task from request");
                    LOGGER.log(Level.FINEST, "Exception is " + e);
                    throw hudson.util.HttpResponses.error(500, "Unexpected Exception occurred " +
                            "while parsing task from request");
                }

                LOGGER.log(Level.FINEST, "Task is " + task);

                if (!task.isParseSuccess()) {
                    LOGGER.log(Level.WARNING, "Fail to parse request: {0}", task.getRequestBody());
                }

                ACL.impersonate(ACL.SYSTEM, new PostTaskRunnable(project, task));
                throw hudson.util.HttpResponses.ok();
            case "GET":
                throw hudson.util.HttpResponses.errorWithoutStack(400, "This url is not intend" +
                        " to be visited by human, please test it on your webhook settings page.");
            default:
                LOGGER.log(Level.FINE, "Unsupported HTTP method: {0}", method);
                break;
        }
    }

    public static class PostTaskRunnable implements Runnable {

        private final Job<?, ?> project;
        private final WebHookTask task;

        public PostTaskRunnable(Job<?, ?> project, WebHookTask task) {
            this.project = project;
            this.task = task;
        }

        @Override
        public void run() {
            LOGGER.log(Level.FINEST, "Finding CodingPushTrigger");
            CodingPushTrigger trigger = CodingPushTrigger.getFromJob(project);
            if (trigger == null) {
                LOGGER.log(Level.WARNING, "CodingPushTrigger not found");
                return;
            }
            LOGGER.log(Level.FINEST, "CodingPushTrigger going to posting");
            trigger.onPost(task);
        }
    }

    private Job<?, ?> resolveProject(final String projectName, final Iterator<String> restOfPathParts) {
        return ACL.impersonate(ACL.SYSTEM, new Callable<Job<?, ?>, RuntimeException>() {
            @Override
            public Job<?, ?> call() throws RuntimeException {
                final Jenkins jenkins = Jenkins.getInstance();
                if (jenkins != null) {
                    Item item = jenkins.getItemByFullName(projectName);
                    while (item instanceof ItemGroup<?> && !(item instanceof Job<?, ?>) && restOfPathParts.hasNext()) {
                        item = jenkins.getItem(restOfPathParts.next(), (ItemGroup<?>) item);
                    }
                    if (item instanceof Job<?, ?>) {
                        return (Job<?, ?>) item;
                    }
                }
                LOGGER.log(Level.INFO, "No project found: {0}, {1}",
                        new String[]{projectName, Joiner.on('/').join(restOfPathParts)});
                return null;
            }

            @Override
            public void checkRoles(RoleChecker checker) throws SecurityException {

            }
        });
    }

    @Extension
    public static class CodingWebHookCrumbExclusion extends CrumbExclusion {
        @Override
        public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.startsWith('/' + WEBHOOK_URL + '/')) {
                chain.doFilter(req, resp);
                return true;
            }
            return false;
        }
    }
}
