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

import com.google.gson.Gson;
import net.coding.jenkins.plugin.Utils;
import net.coding.jenkins.plugin.bean.WebHookTask;
import net.coding.jenkins.plugin.model.MergeRequest;
import net.coding.jenkins.plugin.model.PersonIdent;
import net.coding.jenkins.plugin.model.Ref;
import net.coding.jenkins.plugin.model.event.Push;
import net.coding.jenkins.plugin.v1.model.Commit;
import net.coding.jenkins.plugin.v1.model.Committer;
import net.coding.jenkins.plugin.v1.model.PullRequest;
import net.coding.jenkins.plugin.v1.model.Repository;
import net.coding.jenkins.plugin.v1.model.User;
import net.coding.jenkins.plugin.v1.model.WebHook;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WebHookHelperV1 implements IWebHookHelper {
    public static final String version = "v1";

    private static Logger LOGGER = Logger.getLogger(WebHookHelperV1.class.getName());

    @Override
    public WebHookTask parseTaskFromRequest(HttpServletRequest request) {
        String json = Utils.getRequestBody(request);
        LOGGER.log(Level.INFO, "WebHook payload: {0}", json);
        WebHook webHook = new Gson().fromJson(json, WebHook.class);
        WebHookTask task = new WebHookTask();
        task.setVersion(version);
        task.setRequestBody(json);
        switch (webHook.getEvent()) {
            case "push":
                Push push = toPush(webHook);
                task.setPush(push);
                task.setEvent("push");
                break;
            case "merge_request":
            case "pull_request":
                task.setMergeRequest(toMr(webHook));
                task.setEvent("merge request");
                break;
            default:
                break;
        }
        task.setSignature(webHook.getToken());
        if (task.getSender() != null && task.getRepository() != null) {
            task.setParseSuccess(true);
        }
        return task;
    }

    private net.coding.jenkins.plugin.model.event.MergeRequest toMr(WebHook webHook) {
        if (webHook == null) {
            return null;
        }
        net.coding.jenkins.plugin.model.event.MergeRequest mr =
                new net.coding.jenkins.plugin.model.event.MergeRequest();
        MergeRequest mergeRequest = to(webHook);
        if (mergeRequest == null) {
            return null;
        }
        mr.setMergeRequest(mergeRequest);
        mr.setRepository(to(webHook.getRepository()));
        mr.setSender(mergeRequest.getUser());
        mr.setAction(getMrAction(webHook));
        return mr;
    }

    private net.coding.jenkins.plugin.v1.model.MergeRequest getWebhookMr(WebHook webHook) {
        if (webHook == null) {
            return null;
        }
        net.coding.jenkins.plugin.v1.model.MergeRequest webHookMr = webHook.getMerge_request();
        if (webHookMr == null) {
            webHookMr = webHook.getPull_request();
        }
        return webHookMr;
    }

    private String getMrAction(WebHook webHook) {
        net.coding.jenkins.plugin.v1.model.MergeRequest webHookMr = getWebhookMr(webHook);
        if (webHookMr == null) {
            return null;
        }
        return webHookMr.getAction();
    }

    private MergeRequest to(WebHook webHook) {
        net.coding.jenkins.plugin.v1.model.MergeRequest webHookMr = getWebhookMr(webHook);
        if (webHookMr == null) {
            return null;
        }
        MergeRequest mergeRequest = new MergeRequest();
        mergeRequest.setId(webHookMr.getId());
        mergeRequest.setTitle(webHookMr.getTitle());
        mergeRequest.setBody(webHookMr.getBody());
        mergeRequest.setMerge_commit_sha(webHookMr.getMerge_commit_sha());
        mergeRequest.setState(webHookMr.getStatus());
        mergeRequest.setNumber(webHookMr.getNumber());
        mergeRequest.setBase(toRef(webHookMr.getTarget_branch(), webHook.getRepository()));
        mergeRequest.setHead(toRef(webHookMr.getSource_branch(),
                webHookMr instanceof PullRequest ?
                        ((PullRequest) webHookMr).getSource_repository() :
                        webHook.getRepository()));
        mergeRequest.setHtml_url(webHookMr.getWeb_url());
        mergeRequest.setUser(to(webHookMr.getUser()));
        return mergeRequest;
    }

    private Ref toRef(String refSha, Repository repository) {
        if (repository == null || refSha == null) {
            return null;
        }
        Ref ref = new Ref();
        ref.setRef(refSha);
        ref.setRepo(to(repository));
        return ref;
    }

    private Push toPush(WebHook webHook) {
        if (webHook == null) {
            return null;
        }
        Push push = new Push();
        push.setRef(webHook.getRef());
        push.setCommits(to(webHook.getCommits()));
        push.setBefore(webHook.getBefore());
        push.setAfter(webHook.getAfter());
        push.setRepository(to(webHook.getRepository()));
        push.setSender(to(webHook.getUser()));
        return push;
    }

    private net.coding.jenkins.plugin.model.User to(User user) {
        if (user == null) {
            return null;
        }
        net.coding.jenkins.plugin.model.User outputUser = new net.coding.jenkins.plugin.model.User();
        outputUser.setName(user.getName());
        outputUser.setLogin(user.getGlobal_key());
        outputUser.setAvatar_url(user.getAvatar());
        outputUser.setHtml_url(user.getWeb_url());
        return outputUser;
    }

    private net.coding.jenkins.plugin.model.Repository to(Repository repository) {
        if (repository == null) {
            return null;
        }
        net.coding.jenkins.plugin.model.Repository outputRepository =
                new net.coding.jenkins.plugin.model.Repository();
        outputRepository.setId(Integer.valueOf(repository.getProject_id()));
        outputRepository.setSsh_url(repository.getSsh_url());
        outputRepository.setClone_url(repository.getHttps_url());
        outputRepository.setName(repository.getName());
        outputRepository.setDescription(repository.getDescription());
        outputRepository.setHtml_url(repository.getWeb_url());
        outputRepository.setOwner(to(repository.getOwner()));
        outputRepository.setUrl(repository.projectApiUrl());
        outputRepository.setHtml_url(repository.getWeb_url());

        return outputRepository;
    }

    private List<net.coding.jenkins.plugin.model.Commit> to(List<Commit> commits) {
        if (commits == null) {
            return null;
        }
        return commits.stream().map(this::to).collect(Collectors.toList());
    }

    private net.coding.jenkins.plugin.model.Commit to(net.coding.jenkins.plugin.v1.model.Commit commit) {
        if (commit == null) {
            return null;
        }
        net.coding.jenkins.plugin.model.Commit outputCommit = new net.coding.jenkins.plugin.model.Commit();
        outputCommit.setCommitter(toCommiter(commit.getCommitter()));
        outputCommit.setMessage(commit.getShort_message());
        outputCommit.setId(commit.getSha());
        return outputCommit;
    }

    private PersonIdent toCommiter(Committer committer) {
        if (committer == null) {
            return null;
        }
        PersonIdent outputPerson = new PersonIdent();
        outputPerson.setName(committer.getName());
        outputPerson.setEmail(committer.getEmail());
        return outputPerson;
    }

    @Override
    public String parseEventHeader(String eventHeader) {
        return eventHeader;
    }

    @Override
    public boolean isSignatureValid(WebHookTask task, String webHookToken) {
        return StringUtils.isEmpty(webHookToken) || StringUtils.equals(webHookToken, task.getSignature());
    }
}
