/**
 *  The MIT License
 *
 * Copyright (c) 2011 Michael O'Cleirigh
 * Copyright (c) 2016-present, Coding, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coding.jenkins.plugin.oauth;

import com.google.gson.Gson;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.Extension;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.security.AbstractPasswordBasedSecurityRealm;
import hudson.security.CliAuthenticator;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.security.UserMayOrMayNotExistException;
import hudson.tasks.Mailer;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import jenkins.security.SecurityListener;
import net.coding.api.CodingEmail;
import net.coding.api.CodingMyself;
import net.coding.api.CodingOrganization;
import net.coding.api.CodingTeam;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.kohsuke.args4j.Option;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Implementation of the AbstractPasswordBasedSecurityRealm that uses coding
 * oauth to verify the user can login.
 *
 * This is based on the MySQLSecurityRealm from the mysql-auth-plugin written by
 * Alex Ackerman.
 */
public class CodingSecurityRealm extends AbstractPasswordBasedSecurityRealm implements UserDetailsService {
    private static final String DEFAULT_WEB_URI = "https://coding.net";
    private static final String DEFAULT_API_URI = "https://coding.net";
    private static final String DEFAULT_ENTERPRISE_API_SUFFIX = "/api";
    private static final String DEFAULT_OAUTH_SCOPES = "user,user:email,team";

    private String codingWebUri;
    private String codingApiUri;
    private String clientID;
    private Secret clientSecret;
    private String oauthScopes;
    private String[] myScopes;

    /**
     * @param codingWebUri The URI to the root of the web UI for Coding or Coding Enterprise,
     *                     including the protocol (e.g. https).
     * @param codingApiUri The URI to the root of the API for Coding or Coding Enterprise,
     *                     including the protocol (e.g. https).
     * @param clientID The client ID for the created OAuth Application.
     * @param clientSecret The client secret for the created Coding OAuth Application.
     * @param oauthScopes A comma separated list of OAuth Scopes to request access to.
     */
    @DataBoundConstructor
    public CodingSecurityRealm(String codingWebUri,
                               String codingApiUri,
                               String clientID,
                               String clientSecret,
                               String oauthScopes) {
        super();

        this.codingWebUri = CodingUtil.fixEndwithSlash(Util.fixEmptyAndTrim(codingWebUri));
        this.codingApiUri = CodingUtil.fixEndwithSlash(Util.fixEmptyAndTrim(codingApiUri));
        this.clientID     = Util.fixEmptyAndTrim(clientID);
        setClientSecret(Util.fixEmptyAndTrim(clientSecret));
        this.oauthScopes  = Util.fixEmptyAndTrim(oauthScopes);
    }

    private CodingSecurityRealm() {    }

    /**
     * Tries to automatically determine the Coding API URI based on
     * a Coding Web URI.
     *
     * @param codingWebUri The URI to the root of the Web UI for Coding or Coding Enterprise.
     * @return The expected API URI for the given Web UI
     */
    private String determineApiUri(String codingWebUri) {
        if(codingWebUri.equals(DEFAULT_WEB_URI)) {
            return DEFAULT_API_URI;
        } else {
            return codingWebUri + DEFAULT_ENTERPRISE_API_SUFFIX;
        }
    }

    /**
     * @param codingWebUri
     *            the string representation of the URI to the root of the Web UI for
     *            Coding or Coding Enterprise.
     */
    private void setCodingWebUri(String codingWebUri) {
        this.codingWebUri = codingWebUri;
    }

    /**
     * @param clientID the clientID to set
     */
    private void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    private void setClientSecret(String clientSecret) {
        this.clientSecret = Secret.fromString(clientSecret);
    }

    /**
     * @param oauthScopes the oauthScopes to set
     */
    private void setOauthScopes(String oauthScopes) {
        this.oauthScopes = oauthScopes;
    }

    /**
     * Checks the security realm for a Coding OAuth scope.
     * @param scope A scope to check for in the security realm.
     * @return true if security realm has the scope or false if it does not.
     */
    public boolean hasScope(String scope) {
        if(this.myScopes == null) {
            this.myScopes = this.oauthScopes.split(",");
            Arrays.sort(this.myScopes);
        }
        return Arrays.binarySearch(this.myScopes, scope) >= 0;
    }

    /**
     *
     * @return the URI to the API root of Coding or Coding Enterprise.
     */
    public String getCodingApiUri() {
        return codingWebUri;
//        return codingApiUri;
    }

    /**
     * @param codingApiUri the URI to the API root of Coding or Coding Enterprise.
     */
    private void setCodingApiUri(String codingApiUri) {
        this.codingApiUri = codingApiUri;
    }

    public static final class ConverterImpl implements Converter {

        public boolean canConvert(Class type) {
            return type == CodingSecurityRealm.class;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer,
                MarshallingContext context) {
            CodingSecurityRealm realm = (CodingSecurityRealm) source;

            writer.startNode("codingWebUri");
            writer.setValue(realm.getCodingWebUri());
            writer.endNode();

            writer.startNode("codingApiUri");
            writer.setValue(realm.getCodingApiUri());
            writer.endNode();

            writer.startNode("clientID");
            writer.setValue(realm.getClientID());
            writer.endNode();

            writer.startNode("clientSecret");
            writer.setValue(realm.getClientSecret().getEncryptedValue());
            writer.endNode();

            writer.startNode("oauthScopes");
            writer.setValue(realm.getOauthScopes());
            writer.endNode();

        }

        public Object unmarshal(HierarchicalStreamReader reader,
                UnmarshallingContext context) {

            CodingSecurityRealm realm = new CodingSecurityRealm();

            String node;
            String value;

            while (reader.hasMoreChildren()) {
                reader.moveDown();
                node = reader.getNodeName();
                value = reader.getValue();
                setValue(realm, node, value);
                reader.moveUp();
            }

            if (realm.getCodingWebUri() == null) {
                realm.setCodingWebUri(DEFAULT_WEB_URI);
            }

            if (realm.getCodingApiUri() == null) {
                realm.setCodingApiUri(DEFAULT_API_URI);
            }

            return realm;
        }

        private void setValue(CodingSecurityRealm realm, String node,
                              String value) {
            if (node.toLowerCase().equals("clientid")) {
                realm.setClientID(value);
            } else if (node.toLowerCase().equals("clientsecret")) {
                realm.setClientSecret(value);
            } else if (node.toLowerCase().equals("codingweburi")) {
                realm.setCodingWebUri(value);
//            } else if (node.toLowerCase().equals("codinguri")) { // backwards compatibility for old field
//                realm.setCodingWebUri(value);
//                String apiUrl = realm.determineApiUri(value);
//                realm.setCodingApiUri(apiUrl);
            } else if (node.toLowerCase().equals("codingapiuri")) {
                realm.setCodingApiUri(value);
            } else if (node.toLowerCase().equals("oauthscopes")) {
                realm.setOauthScopes(value);
            } else {
                throw new ConversionException("Invalid node value = " + node);
            }
        }

    }

    /**
     * @return the uri to the web root of Coding (varies for Coding Enterprise Edition)
     */
    public String getCodingWebUri() {
        return codingWebUri;
    }

    /**
     * @return the clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * @return the clientSecret
     */
    public Secret getClientSecret() {
        return clientSecret;
    }

    /**
     * @return the oauthScopes
     */
    public String getOauthScopes() {
        return oauthScopes;
    }

    public HttpResponse doCommenceLogin(StaplerRequest request, @Header("Referer") final String referer)
            throws IOException {
        request.getSession().setAttribute(REFERER_ATTRIBUTE,referer);

        Set<String> scopes = new HashSet<>();
        for (CodingOAuthScope s : getJenkins().getExtensionList(CodingOAuthScope.class)) {
            scopes.addAll(s.getScopesToRequest());
        }
        String suffix="&response_type=code";

        // TODO: can be removed when fix https://codingcorp.coding.net/p/coding-dev/task/85488
        String redirectUri = getJenkins().getRootUrl() + "securityRealm/finishLogin";
        suffix += "&redirect_uri=" + redirectUri;

        if (!scopes.isEmpty()) {
            suffix += "&scope="+ Util.join(scopes,",");
        } else {
            // We need repo scope in order to access private repos
            // see https://open.coding.net/references/personal-access-token/#%E8%AE%BF%E9%97%AE%E4%BB%A4%E7%89%8C%E7%9A%84%E6%9D%83%E9%99%90
            suffix += "&scope=" + oauthScopes;
        }

        //redirect_uri=https%3A%2F%2Fcoding.coding.me%2FComments%2F
        return new HttpRedirect(codingWebUri + "/oauth_authorize.html?client_id="
                + clientID + suffix);
    }

    /**
     * This is where the user comes back to at the end of the OAuth redirect
     * ping-pong.
     */
    public HttpResponse doFinishLogin(StaplerRequest request)
            throws IOException {
        String code = request.getParameter("code");

        if (code == null || code.trim().length() == 0) {
            LOGGER.info("doFinishLogin: missing code.");
            return HttpResponses.redirectToContextRoot();
        }

        LOGGER.finer("get code " + code);

        String accessToken = getAccessToken(code);

        LOGGER.fine("get accessToken " + accessToken);

        if (accessToken != null && accessToken.trim().length() > 0) {
            // only set the access token if it exists.
            CodingAuthenticationToken auth = new CodingAuthenticationToken(accessToken, getCodingApiUri());
            SecurityContextHolder.getContext().setAuthentication(auth);

            CodingMyself self = auth.getMyself();
            User u = User.current();
            if (u == null) {
                throw new IllegalStateException("Can't find user");
            }

            CodingSecretStorage.put(u, accessToken);

            u.setFullName(self.getName());
            // Set email from coding only if empty
            if (!u.getProperty(Mailer.UserProperty.class).hasExplicitlyConfiguredAddress()) {
                if(hasScope("user") || hasScope("user:email")) {
                    String primary_email = null;
                    for(CodingEmail e : self.getEmails2()) {
                        if(e.isPrimary()) {
                            primary_email = e.getEmail();
                        }
                    }
                    if(primary_email != null) {
                        u.addProperty(new Mailer.UserProperty(primary_email));
                    }
                } else {
                    u.addProperty(new Mailer.UserProperty(auth.getCoding().getMyself().getEmail()));
                }
            }

            SecurityListener.fireAuthenticated(new CodingOAuthUserDetails(self.getLogin(), auth.getAuthorities()));

            LOGGER.finer("get user " + u);

            // While LastGrantedAuthorities are triggered by that event, we cannot trigger it there
            // or modifications in organizations will be not reflected when using API Token, due to that caching
            // SecurityListener.fireLoggedIn(self.getLogin());
        } else {
            LOGGER.info("Coding did not return an access token.");
        }

        String referer = (String)request.getSession().getAttribute(REFERER_ATTRIBUTE);
        if (referer!=null)  return HttpResponses.redirectTo(referer);
        return HttpResponses.redirectToContextRoot();   // referer should be always there, but be defensive
    }

    @Nullable
    private String getAccessToken(@Nonnull String code) throws IOException {
        String content;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // https://coding.net/api
            HttpPost httpost = new HttpPost(codingWebUri + "/api/oauth/access_token?");
            ArrayList<NameValuePair> entityList = new ArrayList<>(3);
            entityList.add(new BasicNameValuePair("client_id", clientID));
            entityList.add(new BasicNameValuePair("client_secret", clientSecret.getPlainText()));
            entityList.add(new BasicNameValuePair("code", code));
            httpost.setEntity(new UrlEncodedFormEntity(entityList));
            HttpHost proxy = getProxy(httpost);
            if (proxy != null) {
                RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
                httpost.setConfig(requestConfig);
            }
            org.apache.http.HttpResponse response = httpClient.execute(httpost);
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(entity);

        }
        try {
            Map json = new Gson().fromJson(content, Map.class);
            return (String) json.get("access_token");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "fail to parse json from " + content, e);
        }
        return null;
    }

    /**
     * Returns the proxy to be used when connecting to the given URI.
     */
    private HttpHost getProxy(HttpUriRequest method) throws URIException {
        ProxyConfiguration proxy = getJenkins().proxy;
        if (proxy==null)    return null;    // defensive check

        Proxy p = proxy.createProxy(method.getURI().getHost());
        switch (p.type()) {
        case DIRECT:
            return null;        // no proxy
        case HTTP:
            InetSocketAddress sa = (InetSocketAddress) p.address();
            return new HttpHost(sa.getHostName(),sa.getPort());
        case SOCKS:
        default:
            return null;        // not supported yet
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.security.SecurityRealm#allowsSignup()
     */
    @Override
    public boolean allowsSignup() {
        return false;
    }

    @Override
    public SecurityComponents createSecurityComponents() {
        return new SecurityComponents(new AuthenticationManager() {

            public Authentication authenticate(Authentication authentication)
                    throws AuthenticationException {
                if (authentication instanceof CodingAuthenticationToken)
                    return authentication;
                if (authentication instanceof UsernamePasswordAuthenticationToken)
                    try {
                        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
                        CodingAuthenticationToken coding = new CodingAuthenticationToken(token.getCredentials().toString(), getCodingApiUri());
                        SecurityContextHolder.getContext().setAuthentication(coding);

                        User user = User.getById(token.getName(), false);
                        if(user != null){
                            CodingSecretStorage.put(user, token.getCredentials().toString());
                        }

                        SecurityListener.fireAuthenticated(new CodingOAuthUserDetails(token.getName(), coding.getAuthorities()));

                        return coding;
                    } catch (IOException e) {
                            throw new RuntimeException(e);
                    }
                throw new BadCredentialsException(
                        "Unexpected authentication type: " + authentication);
            }
        }, new UserDetailsService() {
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException, DataAccessException {
                return CodingSecurityRealm.this.loadUserByUsername(username);
            }
        });
    }

    @Override
    protected CodingOAuthUserDetails authenticate(String username, String password) throws AuthenticationException {
        try {
            CodingAuthenticationToken coding = new CodingAuthenticationToken(password, getCodingApiUri());
            if(username.equals(coding.getPrincipal())) {
                SecurityContextHolder.getContext().setAuthentication(coding);
                return coding.getUserDetails(username);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new BadCredentialsException("Invalid Coding username or personal access token: " + username);
    }

    @Override
    public CliAuthenticator createCliAuthenticator(final CLICommand command) {
        return new CliAuthenticator() {
            @Option(name="--username",usage="Coding username to authenticate yourself to Jenkins.")
            public String userName;

            @Option(name="--password",usage="Coding personal access token. Note that passing a password in arguments is insecure.")
            public String password;

            @Option(name="--password-file",usage="File that contains the personal access token.")
            public String passwordFile;

            public Authentication authenticate() throws AuthenticationException, IOException, InterruptedException {
                if(userName == null) {
                    // no authentication parameter. fallback to the transport
                    return command.getTransportAuthentication();
                }
                if(passwordFile != null) {
                    try {
                        password = new FilePath(command.channel, passwordFile).readToString().trim();
                    } catch (IOException e) {
                        throw new BadCredentialsException("Failed to read " + passwordFile, e);
                    }
                }
                if(password == null) {
                    password = command.channel.call(new InteractivelyAskForPassword());
                }

                if(password == null) {
                    throw new BadCredentialsException("No Coding personal access token specified.");
                }
                CodingSecurityRealm.this.authenticate(userName, password);
                return new CodingAuthenticationToken(password, getCodingApiUri());
            }
        };
    }

    @Override
    public String getLoginUrl() {
        return "securityRealm/commenceLogin";
    }

    @Override
    protected String getPostLogOutUrl(StaplerRequest req, Authentication auth) {
        // if we just redirect to the root and anonymous does not have Overall read then we will start a login all over again.
        // we are actually anonymous here as the security context has been cleared
        Jenkins j = Jenkins.getInstance();
        assert j != null;
        if (j.hasPermission(Jenkins.READ)) {
            return super.getPostLogOutUrl(req, auth);
        }
        return req.getContextPath()+ "/" + CodingLogoutAction.POST_LOGOUT_URL;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        @Override
        public String getHelpFile() {
            return "/plugin/coding-webhook/help/oauth/help-security-realm.html";
        }

        @Override
        public String getDisplayName() {
            return Messages.coding_oauth_codingAuthenticationPlugin();
        }

        public String getDefaultCodingWebUri() {
            return DEFAULT_WEB_URI;
        }

        public String getDefaultCodingApiUri() {
            return DEFAULT_API_URI;
        }

        public String getDefaultOauthScopes() {
            return DEFAULT_OAUTH_SCOPES;
        }

        public DescriptorImpl() {
            super();
            // TODO Auto-generated constructor stub
        }

        public DescriptorImpl(Class<? extends SecurityRealm> clazz) {
            super(clazz);
            // TODO Auto-generated constructor stub
        }

    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     *
     * @param username username to lookup
     * @return userDetails
     * @throws UserMayOrMayNotExistException
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        //username is in org*team format
        if(username.contains(CodingOAuthGroupDetails.ORG_TEAM_SEPARATOR)) {
            throw new UsernameNotFoundException("Using org*team format instead of username: " + username);
        }

        User localUser = User.getById(username, false);

        Authentication token = SecurityContextHolder.getContext().getAuthentication();

        if (token == null) {
            if(localUser != null && CodingSecretStorage.contains(localUser)){
                String accessToken = CodingSecretStorage.retrieve(localUser);
                try {
                    token = new CodingAuthenticationToken(accessToken, getCodingApiUri());
                } catch (IOException e) {
                    throw new UserMayOrMayNotExistException("Could not connect to Coding API server, target URL = " + getCodingApiUri(), e);
                }
                SecurityContextHolder.getContext().setAuthentication(token);
            }else{
                throw new UserMayOrMayNotExistException("Could not get auth token.");
            }
        }

        CodingAuthenticationToken authToken;

        if (token instanceof CodingAuthenticationToken) {
            authToken = (CodingAuthenticationToken) token;
        } else {
            throw new UserMayOrMayNotExistException("Unexpected authentication type: " + token);
        }

        /**
         * Always lookup the local user first. If we can't resolve it then we can burn an API request to Coding for this user
         * Taken from hudson.security.HudsonPrivateSecurityRealm#loadUserByUsername(java.lang.String)
         */
        if (localUser != null) {
            return new CodingOAuthUserDetails(username, authToken);
        }

        try {
            CodingOAuthUserDetails userDetails = authToken.getUserDetails(username);
            if (userDetails == null)
                throw new UsernameNotFoundException("Unknown user: " + username);

            // Check the username is not an homonym of an organization
            CodingOrganization ghOrg = authToken.loadOrganization(username);
            if (ghOrg != null) {
                throw new UsernameNotFoundException("user(" + username + ") is also an organization");
            }

            return userDetails;
        } catch (IOException | Error e) {
            throw new DataRetrievalFailureException("loadUserByUsername (username=" + username +")", e);
        }
    }

    /**
     * Compare an object against this instance for equivalence.
     * @param object An object to campare this instance to.
     * @return true if the objects are the same instance and configuration.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof CodingSecurityRealm) {
            CodingSecurityRealm obj = (CodingSecurityRealm) object;
            return this.getCodingWebUri().equals(obj.getCodingWebUri()) &&
                this.getCodingApiUri().equals(obj.getCodingApiUri()) &&
                this.getClientID().equals(obj.getClientID()) &&
                this.getClientSecret().equals(obj.getClientSecret()) &&
                this.getOauthScopes().equals(obj.getOauthScopes());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getCodingWebUri())
                .append(this.getCodingApiUri())
                .append(this.getClientID())
                .append(this.getClientSecret())
                .append(this.getOauthScopes())
                .toHashCode();
    }

    /**
     *
     * @param groupName groupName to look up
     * @return groupDetails
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    @Override
    public GroupDetails loadGroupByGroupname(String groupName)
            throws UsernameNotFoundException, DataAccessException {
        CodingAuthenticationToken authToken =  (CodingAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if(authToken == null)
            throw new UsernameNotFoundException("No known group: " + groupName);

        try {
            int idx = groupName.indexOf(CodingOAuthGroupDetails.ORG_TEAM_SEPARATOR);
            if (idx > -1 && groupName.length() > idx + 1) { // groupName = "CodingOrganization*CodingTeam"
                String orgName = groupName.substring(0, idx);
                String teamName = groupName.substring(idx + 1);
                LOGGER.config(String.format("Lookup for team %s in organization %s", teamName, orgName));
                CodingTeam ghTeam = authToken.loadTeam(orgName, teamName);
                if (ghTeam == null) {
                    throw new UsernameNotFoundException("Unknown Coding team: " + teamName + " in organization "
                            + orgName);
                }
                return new CodingOAuthGroupDetails(ghTeam);
            } else { // groupName = "CodingOrganization"
                CodingOrganization ghOrg = authToken.loadOrganization(groupName);
                if (ghOrg == null) {
                    throw new UsernameNotFoundException("Unknown Coding organization: " + groupName);
                }
                return new CodingOAuthGroupDetails(ghOrg);
            }
        } catch (Error e) {
            throw new DataRetrievalFailureException("loadGroupByGroupname (groupname=" + groupName + ")", e);
        }
    }

    static Jenkins getJenkins() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins not started");
        }
        return jenkins;
    }

    /**
     * Logger for debugging purposes.
     */
    private static final Logger LOGGER = Logger.getLogger(CodingSecurityRealm.class.getName());

    private static final String REFERER_ATTRIBUTE = CodingSecurityRealm.class.getName()+".referer";

    /**
     * Asks for the password.
     */
    private static class InteractivelyAskForPassword extends MasterToSlaveCallable<String,IOException> {
        public String call() throws IOException {
            Console console = System.console();
            if(console == null) {
                return null;    // no terminal
            }
            char[] w = console.readPassword("Coding Personal Access Token: ");
            if(w==null) {
                return null;
            }
            return new String(w);
        }
        private static final long serialVersionUID = 1L;
    }
}
