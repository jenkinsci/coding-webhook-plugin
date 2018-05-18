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

import hudson.model.User;
import org.jfree.util.Log;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

public class CodingSecretStorage {

    private CodingSecretStorage(){
        // no accessible constructor
    }

    public static boolean contains(@Nonnull User user) {
        return user.getProperty(CodingAccessTokenProperty.class) != null;
    }

    public static @CheckForNull String retrieve(@Nonnull User user) {
        CodingAccessTokenProperty property = user.getProperty(CodingAccessTokenProperty.class);
        if (property == null) {
            Log.debug("Cache miss for username: " + user.getId());
            return null;
        } else {
            Log.debug("Token retrieved using cache for username: " + user.getId());
            return property.getAccessToken().getPlainText();
        }
    }

    public static void put(@Nonnull User user, @Nonnull String accessToken) {
        Log.debug("Populating the cache for username: " + user.getId());
        try {
            user.addProperty(new CodingAccessTokenProperty(accessToken));
        } catch (IOException e) {
            Log.warn("Received an exception when trying to add the Coding access token to the user: " + user.getId(), e);
        }
    }
}
