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
package net.coding.jenkins.plugin.listener;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.git.RevisionParameterAction;
import jenkins.model.Jenkins;
import net.coding.jenkins.plugin.CodingPushTrigger;
import net.coding.jenkins.plugin.cause.CauseData;
import net.coding.jenkins.plugin.cause.CodingWebHookCause;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jgit.transport.URIish;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.coding.jenkins.plugin.webhook.CodingWebHook.API_TOKEN_PARAM;
import static net.coding.jenkins.plugin.webhook.CodingWebHook.PERSONAL_TOKEN_HEADER;

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
        if (trigger == null || cause == null
                || build.getResult() == Result.ABORTED
                || build.getResult() == Result.NOT_BUILT) {
            return;
        }
        String personalToken = trigger.getPersonalToken();
        String apiToken = trigger.getApiToken();
        String projectWebUrl = cause.getData().getProjectHtmlUrl();
        if ((Strings.isNullOrEmpty(personalToken) && Strings.isNullOrEmpty(apiToken))
                || Strings.isNullOrEmpty(projectWebUrl)) {
            return;
        }

        String targetType;
        long targetId = 0;
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

        boolean success = build.getResult() == Result.SUCCESS;

        if (trigger.isAddResultNote()) {
            addResultNote(
                    personalToken, apiToken, getBuildUrl(build),
                    cause.getData(),
                    success,
                    targetType, targetId
            );
        }
    }

    private void addResultNote(String personalToken, String apiToken, String buildUrl, CauseData causeData, boolean success,
                               String targetType, long targetId) {
        String commitId = causeData.getCommitId();
        String postUrl = String.format("%s/git/mark", projectApiUrl(causeData));
        HttpPost httpPost = new HttpPost(postUrl);
        LOGGER.log(Level.FINEST, "Result Note to {0}", postUrl);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("icon", "jenkins"));
        nvps.add(new BasicNameValuePair("name", "Jenkins"));
        nvps.add(new BasicNameValuePair("url", buildUrl));
        nvps.add(new BasicNameValuePair("status", success ? "1" : "2"));
        nvps.add(new BasicNameValuePair("markable_type", targetType));
        if (StringUtils.equals(targetType, "MergeRequestBean")) {
            nvps.add(new BasicNameValuePair("markable_id", String.valueOf(targetId)));
            String template = "build %s for merge request %s";
            String content = String.format(template, success ? "SUCCESS" : "FAILURE", causeData.getMergeRequestTitle());
            nvps.add(new BasicNameValuePair("description", content));
        } else if (StringUtils.equals(targetType, "Commit")) {
            nvps.add(new BasicNameValuePair("sha", commitId));
            String template = "build %s for commit %s";
            String content = String.format(template, success ? "SUCCESS" : "FAILURE", commitId);
            nvps.add(new BasicNameValuePair("description", content));
        }
        if (!Strings.isNullOrEmpty(personalToken)) {
            httpPost.setHeader(PERSONAL_TOKEN_HEADER, "token " + personalToken);
        } else {
            nvps.add(new BasicNameValuePair(API_TOKEN_PARAM, apiToken));
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String json = IOUtils.toString(response.getEntity().getContent());
            LOGGER.log(Level.FINEST, "Result Note response {0}", json);
            JsonObject o = new JsonParser().parse(json).getAsJsonObject();
            if (code != HttpStatus.SC_OK || o.get("code").getAsInt() != 0) {
                LOGGER.log(Level.INFO, "Failed to add note, code: {0}, text: {1}", new Object[]{code, json});
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to add commit note: " + e.getMessage());
        }
    }

    public String projectApiUrl(CauseData causeData) {
        String htmlUrl = causeData.getProjectHtmlUrl();
        String fullName = causeData.getFullName();
        Pattern pattern = Pattern.compile("(https?://[^/]+)/[ut]/([^/]+)/p/([^/]+).*");
        Matcher matcher = pattern.matcher(htmlUrl);
        if (matcher.matches()) {
            // is professional
            return String.format("%s/api/user/%s/project/%s", matcher.group(1), matcher.group(2), matcher.group(3));
        }
        pattern = Pattern.compile("(https?://[^/]+)/p/([^/]+).*");
        matcher = pattern.matcher(htmlUrl);
        if (matcher.matches()) {
            // is enterprise
            String host = matcher.group(1);
            String projectName = matcher.group(2);
            String teamName = fullName.split("/")[0];
            return String.format("%s/api/user/%s/project/%s", host, teamName, projectName);
        }
        throw new IllegalArgumentException("Invalid project api url: " + htmlUrl);
    }

    private String getBuildUrl(Run<?, ?> build) {
        String rootUrl = Jenkins.getInstance().getRootUrl();
        if (StringUtils.isBlank(rootUrl)) {
            return "";
        }
        return rootUrl + build.getUrl();
    }

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        CodingWebHookCause cause = run.getCause(CodingWebHookCause.class);
        if (cause != null) {
            RevisionParameterAction revisionParameterAction = run.getAction(RevisionParameterAction.class);
            if (revisionParameterAction != null) {
                LOGGER.log(Level.INFO, "Already existing a RevisionParameterAction");
            } else {
                run.addAction(createRevisionParameter(cause));
            }
        }
        super.onStarted(run, listener);
    }

    private Action createRevisionParameter(CodingWebHookCause cause) {
        CauseData causeData = cause.getData();
        String revisionToBuild = causeData.getCommitId();
        URIish urIish = null;
        try {
            urIish = new URIish(causeData.getRepoUrl());
        } catch (URISyntaxException e) {
            LOGGER.log(Level.WARNING, "could not parse URL");
        }
        return new RevisionParameterAction(revisionToBuild, urIish);
    }
}
