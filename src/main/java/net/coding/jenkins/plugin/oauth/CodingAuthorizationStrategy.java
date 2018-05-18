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
 The MIT License

Copyright (c) 2011 Michael O'Cleirigh

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.



 */
package net.coding.jenkins.plugin.oauth;

import com.google.common.collect.ImmutableList;
import hudson.Extension;
import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author mocleiri
 *
 * // TODO: 可以先注释掉这个 class，开放所有权限
 *
 */
public class CodingAuthorizationStrategy extends AuthorizationStrategy {

    @DataBoundConstructor
    public CodingAuthorizationStrategy(String adminUserNames,
                                       boolean authenticatedUserReadPermission,
                                       boolean useRepositoryPermissions,
                                       boolean authenticatedUserCreateJobPermission,
                                       String organizationNames,
                                       boolean allowCodingWebHookPermission,
                                       boolean allowCcTrayPermission,
                                       boolean allowAnonymousReadPermission,
                                       boolean allowAnonymousJobStatusPermission) {
        super();

        rootACL = new CodingRequireOrganizationMembershipACL(adminUserNames,
                organizationNames,
                authenticatedUserReadPermission,
                useRepositoryPermissions,
                authenticatedUserCreateJobPermission,
                allowCodingWebHookPermission,
                allowCcTrayPermission,
                allowAnonymousReadPermission,
                allowAnonymousJobStatusPermission);
    }

    private final CodingRequireOrganizationMembershipACL rootACL;

    /*
     * (non-Javadoc)
     * @return rootAcl
     * @see hudson.security.AuthorizationStrategy#getRootACL()
     */
    @Nonnull
    @Override
    public ACL getRootACL() {
        return rootACL;
    }

    @Nonnull
    public ACL getACL(@Nonnull AbstractItem item) {
        // skip repository right now
//        if(item instanceof MultiBranchProject) {
//            CodingRequireOrganizationMembershipACL codingACL = (CodingRequireOrganizationMembershipACL) getRootACL();
//            return codingACL.cloneForProject(item);
//        } else {
            return getRootACL();
//        }
    }

    @Nonnull
    public ACL getACL(@Nonnull Job<?,?> job) {
//        if(job instanceof WorkflowJob && job.getProperty(BranchJobProperty.class) != null || job instanceof AbstractProject) {
//            CodingRequireOrganizationMembershipACL codingACL = (CodingRequireOrganizationMembershipACL) getRootACL();
//            return codingACL.cloneForProject(job);
//        } else {
            return getRootACL();
//        }
    }

    /**
     * (non-Javadoc)
     * @return groups
     * @see hudson.security.AuthorizationStrategy#getGroups()
     */
    @Nonnull
    @Override
    public Collection<String> getGroups() {
        return ImmutableList.of();
    }

    private Object readResolve() {
        return this;
    }

    /**
     * @return organizationNames
     * @see CodingRequireOrganizationMembershipACL#getOrganizationNameList()
     */
    public String getOrganizationNames() {
        return StringUtils.join(rootACL.getOrganizationNameList().iterator(), ", ");
    }

    /**
     * @return adminUserNames
     * @see CodingRequireOrganizationMembershipACL#getAdminUserNameList()
     */
    public String getAdminUserNames() {
        return StringUtils.join(rootACL.getAdminUserNameList().iterator(), ", ");
    }

    /**
     * @return isUseRepositoryPermissions
     * @see CodingRequireOrganizationMembershipACL#isUseRepositoryPermissions()
     */
    public boolean isUseRepositoryPermissions() {
        return rootACL.isUseRepositoryPermissions();
    }

    /**
     * @return isAuthenticatedUserCreateJobPermission
     * @see CodingRequireOrganizationMembershipACL#isAuthenticatedUserCreateJobPermission()
     */
    public boolean isAuthenticatedUserCreateJobPermission() {
        return rootACL.isAuthenticatedUserCreateJobPermission();
    }

    /**
     * @return isAuthenticatedUserReadPermission
     * @see CodingRequireOrganizationMembershipACL#isAuthenticatedUserReadPermission()
     */
    public boolean isAuthenticatedUserReadPermission() {
        return rootACL.isAuthenticatedUserReadPermission();
    }

    /**
     * @return isAllowCodingWebHookPermission
     * @see CodingRequireOrganizationMembershipACL#isAllowCodingWebHookPermission()
     */
    public boolean isAllowCodingWebHookPermission() {
        return rootACL.isAllowCodingWebHookPermission();
    }

    /**
     * @return isAllowCcTrayPermission
     * @see CodingRequireOrganizationMembershipACL#isAllowCcTrayPermission()
     */
    public boolean isAllowCcTrayPermission() {
        return rootACL.isAllowCcTrayPermission();
    }


    /**
     * @return isAllowAnonymousReadPermission
     * @see CodingRequireOrganizationMembershipACL#isAllowAnonymousReadPermission()
     */
    public boolean isAllowAnonymousReadPermission() {
        return rootACL.isAllowAnonymousReadPermission();
    }

    /**
     * @return isAllowAnonymousJobStatusPermission
     * @see CodingRequireOrganizationMembershipACL#isAllowAnonymousJobStatusPermission()
     */
    public boolean isAllowAnonymousJobStatusPermission() {
        return rootACL.isAllowAnonymousJobStatusPermission();
    }

    /**
     * Compare an object against this instance for equivalence.
     * @param object An object to campare this instance to.
     * @return true if the objects are the same instance and configuration.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof CodingAuthorizationStrategy) {
            CodingAuthorizationStrategy obj = (CodingAuthorizationStrategy) object;
            return this.getOrganizationNames().equals(obj.getOrganizationNames()) &&
                this.getAdminUserNames().equals(obj.getAdminUserNames()) &&
                this.isUseRepositoryPermissions() == obj.isUseRepositoryPermissions() &&
                this.isAuthenticatedUserCreateJobPermission() == obj.isAuthenticatedUserCreateJobPermission() &&
                this.isAuthenticatedUserReadPermission() == obj.isAuthenticatedUserReadPermission() &&
                this.isAllowCodingWebHookPermission() == obj.isAllowCodingWebHookPermission() &&
                this.isAllowCcTrayPermission() == obj.isAllowCcTrayPermission() &&
                this.isAllowAnonymousReadPermission() == obj.isAllowAnonymousReadPermission() &&
                this.isAllowAnonymousJobStatusPermission() == obj.isAllowAnonymousJobStatusPermission();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return rootACL != null ? rootACL.hashCode() : 0;
    }

    @Extension
    public static final class DescriptorImpl extends
            Descriptor<AuthorizationStrategy> {

        public String getDisplayName() {
            return Messages.coding_oauth_codingAuthorizationStrategy();
        }

        public String getHelpFile() {
            return "/plugin/coding-webhook/help/oauth/help-authorization-strategy.html";
        }
    }
}
