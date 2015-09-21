/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.auth;

import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;

import org.apache.axis2.context.MessageContext;

import java.util.Map;

/**
 * Implementation class for role retrieval for role based content filtering.
 */
public class UserStoreAuthorizationRoleRetriever implements AuthorizationRoleRetriever {
    @Override
    public String[] getRolesForUser(MessageContext msgContext) throws DataServiceFault {
        return DBUtils.getUserRoles(DBUtils.getUsername(msgContext));
    }

    @Override
    public String[] getAllRoles(int tenantId) throws DataServiceFault {
        return DBUtils.getAllRoles(tenantId);
    }

    @Override
    public void setProperties(Map<String, String> authenticatorProperties) {
        //nothing to do
    }

    @Override
    public void init() throws DataServiceFault {
        //nothing to do
    }
}
