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
