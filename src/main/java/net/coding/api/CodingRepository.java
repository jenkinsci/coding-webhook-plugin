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

public class CodingRepository {
    /*package almost final*/ Coding root;

    private String description, homepage, name, full_name;
    private String html_url;    // this is the UI

    private CodingUser owner;   // not fully populated. beware.

    private boolean _private;
    private boolean permissions_pull;
    private boolean permissions_push;
    private boolean permissions_admin;

    /**
     * Short repository name without the owner. For example 'jenkins' in case of http://github.com/jenkinsci/jenkins
     */
    public String getName() {
        return name;
    }

    /*package*/ CodingRepository wrap(Coding root) {
        this.root = root;
        return this;
    }

    public CodingUser getOwner() throws IOException {
        return root.getUser(owner.getLogin());   // because 'owner' isn't fully populated
    }

    public boolean isPrivate() {
        return _private;
    }

    public boolean hasPullAccess() {
        return permissions_pull;
    }

    public boolean hasPushAccess() {
        return permissions_push;
    }

    public boolean hasAdminAccess() {
        return permissions_admin;
    }
}
