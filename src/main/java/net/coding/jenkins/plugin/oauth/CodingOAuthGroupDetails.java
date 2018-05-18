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

import hudson.security.GroupDetails;
import net.coding.api.CodingOrganization;
import net.coding.api.CodingTeam;

/**
 * @author Mike
 *
 */
public class CodingOAuthGroupDetails extends GroupDetails {

    private final CodingOrganization org;
    private final CodingTeam team;
    static final String ORG_TEAM_SEPARATOR = "*";

    /**
    * Group based on organization name
    * @param org the coding organization
    */
    public CodingOAuthGroupDetails(CodingOrganization org) {
        super();
        this.org = org;
        this.team = null;
    }

    /**
    * Group based on team name
     * @param team the coding team
     */
    public CodingOAuthGroupDetails(CodingTeam team) {
        super();
        this.org = team.getOrganization();
        this.team = team;
    }

    /* (non-Javadoc)
    * @see hudson.security.GroupDetails#getName()
    */
    @Override
    public String getName() {
        if (team != null)
            return org.getLogin() + ORG_TEAM_SEPARATOR + team.getName();
        if (org != null)
            return org.getLogin();
        return null;
    }

}
