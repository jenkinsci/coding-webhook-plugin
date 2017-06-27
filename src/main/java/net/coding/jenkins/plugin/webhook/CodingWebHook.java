package net.coding.jenkins.plugin.webhook;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gson.Gson;

import net.coding.jenkins.plugin.CodingPushTrigger;
import net.coding.jenkins.plugin.model.WebHook;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.UnprotectedRootAction;
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.security.csrf.CrumbExclusion;
import jenkins.model.Jenkins;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author tsl0922
 */
@Extension
public class CodingWebHook implements UnprotectedRootAction {
    private static final Logger LOGGER = Logger.getLogger(CodingWebHook.class.getName());

    public static final String WEBHOOK_URL = "coding";
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
                    return;
                }
                if (StringUtils.equals(eventHeader, "ping")) {
                    LOGGER.log(Level.INFO, "Received coding webHook ping: {0}", getRequestBody(request));
                    return;
                }
                String json = getRequestBody(request);
                LOGGER.log(Level.INFO, "WebHook payload: {0}", json);
                WebHook webHook = new Gson().fromJson(json, WebHook.class);
                ACL.impersonate(ACL.SYSTEM, () -> {
                    CodingPushTrigger trigger = CodingPushTrigger.getFromJob(project);
                    if (trigger != null) {
                        trigger.onPost(webHook, eventHeader);
                    }
                });
                throw hudson.util.HttpResponses.ok();
            case "GET":
            default:
                LOGGER.log(Level.FINE, "Unsupported HTTP method: {0}", method);
                break;
        }
    }

    private String getRequestBody(StaplerRequest request) {
        String requestBody;
        try {
            Charset charset = request.getCharacterEncoding() == null ?  UTF_8 : Charset.forName(request.getCharacterEncoding());
            requestBody = IOUtils.toString(request.getInputStream(), charset);
        } catch (IOException e) {
            throw HttpResponses.error(500, "Failed to read request body");
        }
        return requestBody;
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
