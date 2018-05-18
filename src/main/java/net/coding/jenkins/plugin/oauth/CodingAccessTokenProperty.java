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
package net.coding.jenkins.plugin.oauth;

import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.util.Secret;
import org.jenkinsci.Symbol;

import javax.annotation.Nonnull;

/**
 * Remembers the access token used to connect to the Coding server
 *
 * @since TODO
 */
public class CodingAccessTokenProperty extends UserProperty {
    private final Secret accessToken;

    public CodingAccessTokenProperty(String accessToken) {
        this.accessToken = Secret.fromString(accessToken);
    }

    public @Nonnull
    Secret getAccessToken() {
        return accessToken;
    }

    @Extension
    @Symbol("codingAccessToken")
    public static final class DescriptorImpl extends UserPropertyDescriptor {
        @Override
        public boolean isEnabled() {
            // does not show elements in /<user>/configure/
            return false;
        }

        @Override
        public UserProperty newInstance(User user) {
            // no default property
            return null;
        }
    }
}
