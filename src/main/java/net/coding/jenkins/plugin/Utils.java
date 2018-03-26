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
package net.coding.jenkins.plugin;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.HttpResponses;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    public static String getRequestBody(HttpServletRequest request) {
        String requestBody;
        try {
            Charset charset = request.getCharacterEncoding() == null ? UTF_8 : Charset.forName(request.getCharacterEncoding());
            requestBody = IOUtils.toString(request.getInputStream(), charset);
        } catch (IOException e) {
            throw HttpResponses.error(500, "Failed to read request body");
        }
        return requestBody;
    }
}
