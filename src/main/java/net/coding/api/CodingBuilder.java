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
package net.coding.api;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class CodingBuilder {

    /* private */ String endpoint = Coding.CODING_URL;
    /* private */ String user;
    /* private */ String password;
    /* private */ String oauthToken;

    private HttpConnector connector;

    public static CodingBuilder fromEnvironment() throws IOException {
        Properties props = new Properties();
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            String name = e.getKey().toLowerCase(Locale.ENGLISH);
            if (name.startsWith("github_")) name=name.substring(7);
            props.put(name,e.getValue());
        }
        return fromProperties(props);
    }

    public static CodingBuilder fromProperties(Properties props) {
        CodingBuilder self = new CodingBuilder();
        self.withOAuthToken(props.getProperty("net/coding/jenkins/plugin/oauth"), props.getProperty("login"));
        self.withPassword(props.getProperty("login"), props.getProperty("password"));
        self.withEndpoint(props.getProperty("endpoint", Coding.CODING_URL));
        return self;
    }

    public CodingBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
    public CodingBuilder withPassword(String user, String password) {
        this.user = user;
        this.password = password;
        return this;
    }
    public CodingBuilder withOAuthToken(String oauthToken) {
        return withOAuthToken(oauthToken, null);
    }
    public CodingBuilder withOAuthToken(String oauthToken, String user) {
        this.oauthToken = oauthToken;
        this.user = user;
        return this;
    }

    public CodingBuilder withConnector(HttpConnector connector) {
        this.connector = connector;
        return this;
    }

    public Coding build() throws IOException {
        return new Coding(endpoint, user, oauthToken, password, connector);
    }
}
