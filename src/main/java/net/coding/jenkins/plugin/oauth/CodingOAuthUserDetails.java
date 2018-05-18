/**
 * /**
 *  The MIT License
 * Copyright (c) 2011 Michael O'Cleirigh
 * Copyright (c) 2016-present, Coding, Inc.
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
 *  */
 */
/**
 *
 */
package net.coding.jenkins.plugin.oauth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.coding.api.CodingUser;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Mike
 *
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class CodingOAuthUserDetails extends User implements UserDetails {

    private static final long serialVersionUID = 1L;

    private boolean hasGrantedAuthorities;

    private final CodingAuthenticationToken authenticationToken;

    public CodingOAuthUserDetails(@Nonnull String login, @Nonnull GrantedAuthority[] authorities) {
        super(login, "", true, true, true, true, authorities);
        this.authenticationToken = null;
        this.hasGrantedAuthorities = true;
    }

    public CodingOAuthUserDetails(@Nonnull String login, @Nonnull CodingAuthenticationToken authenticationToken) {
        super(login, "", true, true, true, true, new GrantedAuthority[0]);
        this.authenticationToken = authenticationToken;
        this.hasGrantedAuthorities = false;
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        if (!hasGrantedAuthorities) {
            try {
                CodingUser user = authenticationToken.loadUser(getUsername());
                if(user != null) {
                    setAuthorities(authenticationToken.getAuthorities());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getAuthorities();
    }
}
