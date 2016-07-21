package net.coding.jenkins.plugin.listener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.coding.jenkins.plugin.CodingPushTrigger;
import net.coding.jenkins.plugin.cause.CodingWebHookCause;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

/**
 * @author tsl0922
 */
@Extension
public class MergeRequestRunListener extends RunListener<Run<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(MergeRequestRunListener.class.getName());

    @Override
    public void onCompleted(Run<?, ?> build, @Nonnull TaskListener listener) {
        CodingPushTrigger trigger = CodingPushTrigger.getFromJob(build.getParent());
        CodingWebHookCause cause = build.getCause(CodingWebHookCause.class);
        if (trigger != null && cause != null && trigger.isAddResultNote()) {
            String targetType;
            int targetId = 0;
            switch (cause.getData().getActionType()) {
                case PUSH:
                    targetType = "Commit";
                    break;
                case MR:
                    targetType = "MergeRequestBean";
                    targetId = cause.getData().getMergeRequestId();
                    break;
                case PR:
                    targetType = "PullRequestBean";
                    targetId = cause.getData().getMergeRequestId();
                    break;
                default:
                    return;
            }
            String apiToken = trigger.getApiToken();
            String projectPath = cause.getData().getProjectPath();
            if (!apiToken.isEmpty() && !projectPath.isEmpty() && build.getResult() != Result.ABORTED) {
                addResultNote(
                        apiToken, getBuildUrl(build),
                        cause.getData().getCommitId(),
                        build.getResult() == Result.SUCCESS,
                        projectPath, targetType, targetId
                );
            }
        }
    }

    private void addResultNote(String apiToken, String buildUrl, String commitId, boolean success,
                                      String projectPath, String targetType, int targetId) {
        String template = "Jenkins build **%s** for commit %s, Result: %s";
        String content = String.format(template, success ? "SUCCESS" : "FAILURE", commitId, buildUrl);
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(String.format("%s/git/line_notes", getApiUrl(projectPath)));
        httpPost.setHeader(new BasicHeader("Cookie", String.format("sid=%s", apiToken)));
        List<NameValuePair> nvps = new ArrayList<>();
        if (StringUtils.equals(targetType, "Commit")) {
            nvps.add(new BasicNameValuePair("commitId", commitId));
        }
        nvps.add(new BasicNameValuePair("noteable_type", targetType));
        nvps.add(new BasicNameValuePair("noteable_id", String.valueOf(targetId)));
        nvps.add(new BasicNameValuePair("content", content));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String json = IOUtils.toString(response.getEntity().getContent());
            JsonObject o = new JsonParser().parse(json).getAsJsonObject();
            if (code != HttpStatus.SC_OK || o.get("code").getAsInt() != 0) {
                LOGGER.info(String.format("Failed to add note, code: %d, text: %s", code, json));
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to add commit note: " + e.getMessage());
        }
    }

    private String getApiUrl(String projectPath) {
        String[] parts = projectPath.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid project path: " + projectPath);
        }
        return String.format("https://coding.net/api/user/%s/project/%s", parts[0], parts[1]);
    }

    private String getBuildUrl(Run<?, ?> build) {
        return Jenkins.getInstance().getRootUrl() + build.getUrl();
    }
}
