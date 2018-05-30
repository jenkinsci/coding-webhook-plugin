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

import hudson.ExtensionPoint;

import java.util.Collection;

/**
 * Extension point to be implemented by plugins to request additional scopes.
 * @author Kohsuke Kawaguchi
 */
public abstract class CodingOAuthScope implements ExtensionPoint {
    /**
     * Returns a collection of scopes to request.
     * See https://open.coding.net/references/personal-access-token/#%E8%AE%BF%E9%97%AE%E4%BB%A4%E7%89%8C%E7%9A%84%E6%9D%83%E9%99%90
     */
    public abstract Collection<String> getScopesToRequest();
}
