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

public class CodingTeam {

    private String name,permission,slug;

    private CodingOrganization organization;

    protected /*final*/ CodingOrganization org;

    private String global_key, avatar;

    private Integer id;

    public CodingOrganization getOrganization() {
        return org;
    }

    /*package*/ CodingTeam wrapUp(CodingOrganization owner) {
        this.org = owner;
        return this;
    }

    /*package*/ CodingTeam wrapUp(Coding root) { // auto-wrapUp when organization is known from GET /user/teams
        if (this.organization == null) {
            this.organization = new CodingOrganization();
            this.organization.global_key = "coding_dot_net";
        }
        this.organization.wrapUp(root);
        return wrapUp(organization);
    }

    public String getName() {
        return name;
    }
}
