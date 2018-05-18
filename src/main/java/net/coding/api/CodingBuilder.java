/**
 *  Copyright (c) 2011- Kohsuke Kawaguchi and other contributors
 *  Copyright (c) 2016-present, Coding, Inc.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
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
