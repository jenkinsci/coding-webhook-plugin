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
/**
 The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.



 */
package net.coding.jenkins.plugin.oauth;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uniquely identifies a repository on Coding.
 *
 * This is a simplified version of the file
 * https://github.com/jenkinsci/github-plugin/blob/master/src/main/java/com/cloudbees/jenkins/GitHubRepositoryName.java
 *
 * It has been duplicated to avoid introducing a dependency on "coding-plugin"
 *
 * @author Kohsuke Kawaguchi
 */
public class CodingRepositoryName {

    private static final Pattern[] URL_PATTERNS = {
        /**
         * The first set of patterns extract the host, owner and repository names
         * from URLs that include a '.git' suffix, removing the suffix from the
         * repository name.
         */
        Pattern.compile("git@(.+):([^/]+)/([^/]+)\\.git"),
        Pattern.compile("https?://[^/]+@([^/]+)/([^/]+)/([^/]+)\\.git"),
        Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)\\.git"),
        Pattern.compile("git://([^/]+)/([^/]+)/([^/]+)\\.git"),
        Pattern.compile("ssh://git@([^/]+)/([^/]+)/([^/]+)\\.git"),
        /**
         * The second set of patterns extract the host, owner and repository names
         * from all other URLs. Note that these patterns must be processed *after*
         * the first set, to avoid any '.git' suffix that may be present being included
         * in the repository name.
         */
        Pattern.compile("git@(.+):([^/]+)/([^/]+)/?"),
        Pattern.compile("https?://[^/]+@([^/]+)/([^/]+)/([^/]+)/?"),
        Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)/?"),
        Pattern.compile("git://([^/]+)/([^/]+)/([^/]+)/?"),
        Pattern.compile("ssh://git@([^/]+)/([^/]+)/([^/]+)/?")
    };

    /**
     * Create {@link CodingRepositoryName} from URL
     *
     * @param url
     *            must be non-null
     * @return parsed {@link CodingRepositoryName} or null if it cannot be
     *         parsed from the specified URL
     */
    public static CodingRepositoryName create(final String url) {
        LOGGER.log(Level.FINE, "Constructing from URL {0}", url);
        for (Pattern p : URL_PATTERNS) {
            Matcher m = p.matcher(url.trim());
            if (m.matches()) {
                LOGGER.log(Level.FINE, "URL matches {0}", m);
                CodingRepositoryName ret = new CodingRepositoryName(m.group(1), m.group(2),
                        m.group(3));
                LOGGER.log(Level.FINE, "Object is {0}", ret);
                return ret;
            }
        }
        LOGGER.log(Level.WARNING, "Could not match URL {0}", url);
        return null;
    }

    public final String host, userName, repositoryName;

    public CodingRepositoryName(String host, String userName, String repositoryName) {
        this.host           = host;
        this.userName       = userName;
        this.repositoryName = repositoryName;
    }

    @Override
    public String toString() {
        return "CodingRepository[host="+host+",username="+userName+",repository="+repositoryName+"]";
    }

    private static final Logger LOGGER = Logger.getLogger(CodingRepositoryName.class.getName());

}
