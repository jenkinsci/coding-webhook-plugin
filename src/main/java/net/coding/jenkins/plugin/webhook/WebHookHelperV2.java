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

import net.coding.jenkins.plugin.Utils;
import net.coding.jenkins.plugin.bean.WebHookTask;
import net.coding.jenkins.plugin.common.gson.JSON;
import net.coding.jenkins.plugin.model.event.MergeRequest;
import net.coding.jenkins.plugin.model.event.Push;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebHookHelperV2 implements IWebHookHelper {
    public static final String version = "v2";

    private static Logger LOGGER = Logger.getLogger(WebHookHelperV2.class.getName());

    @Override
    public WebHookTask parseTaskFromRequest(HttpServletRequest request) {
        WebHookTask task = new WebHookTask();
        task.setVersion(version);
        String json = Utils.getRequestBody(request);
        LOGGER.log(Level.INFO, "WebHook payload: {0}", json);
        task.setRequestBody(json);
        String action = request.getHeader("X-Coding-Event");
        task.setEvent(action);
        switch (action) {
            case WebHookTask.EVENT_PUSH:
                Push push = JSON.fromJson(json, Push.class);
                task.setPush(push);
                task.setParseSuccess(true);
                break;
            case WebHookTask.EVENT_MERGE_REQUEST:
                MergeRequest mr = JSON.fromJson(json, MergeRequest.class);
                task.setMergeRequest(mr);
                task.setParseSuccess(true);
                break;
            default:
                break;
        }
        if (task.isParseSuccess()) {
            String signature = request.getHeader("X-Coding-Signature");
            task.setSignature(signature);
        }
        return task;
    }

    @Override
    public boolean isSignatureValid(WebHookTask task, String webHookToken) {
        if (StringUtils.isEmpty(webHookToken)) {
            return true;
        }
        String signature = HmacUtils.hmacSha1Hex(webHookToken, task.getRequestBody());
        String gotSignature = task.getSignature();
        if (StringUtils.isEmpty(gotSignature)) {
            LOGGER.log(Level.FINE, "Got Empty signature");
            return false;
        }
        if (!StringUtils.startsWith(gotSignature, "sha1=")) {
            LOGGER.log(Level.FINE,
                    MessageFormat.format("Invalid signature, should start with while sha1=, got {0}", gotSignature));
            return false;
        }
        boolean valid = StringUtils.equals("sha1=" + signature, gotSignature);
        if (!valid) {
            LOGGER.log(Level.FINE,
                    MessageFormat.format("Invalid signature, should be {0}, while got {1}", signature, gotSignature));
        }
        return valid;
    }

    @Override
    public String parseEventHeader(String eventHeader) {
        if ("merge request".equals(eventHeader)) {
            return "merge_request";
        }
        return eventHeader;
    }
}
