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

import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DataServiceFault;

import org.apache.axis2.context.MessageContext;

import java.util.Map;

/**
 * This is the abstract interface which we can use to connect to third party authorisation provider in order to do role based
 * filtering in DSS. This will have default getUserName method implemented which gets username from messageContext,
 * if needs that can be overridden too
 */
public abstract class AuthorizationRoleRetriever {

    /**
     * Method used to get the roles of the user.
     *
     * @param msgContext to be used in retrieving roles.
     * @return String array of user roles assigned to that particular user.
     * @throws DataServiceFault
     */
    public abstract String[] getRolesForUser(MessageContext msgContext) throws DataServiceFault;

    /**
     * Method used to get all the user roles in order to display in data service design phase.
     *
     * @return String array of all user roles.
     * @throws DataServiceFault
     */
    public abstract String[] getAllRoles() throws DataServiceFault;

    /**
     * Default getUsername method which will get username from the messageContext.
     *
     * @param msgContext
     * @return username.
     */
    public String getUsernameFromMessageContext(MessageContext msgContext) {
        String userName = (String) msgContext.getProperty(
                DBConstants.MSG_CONTEXT_USERNAME_PROPERTY);
        return userName;
    }

    /**
     * To set the properties specific to role retriever, if no properties specified, empty map will be passed
     * format to specify property "<Property name="userName">admin</Property>"
     *
     * @param authenticatorProperties
     */
    public abstract void setProperties(Map<String, String> authenticatorProperties);

    /**
     * This method will be invoked after instantiating the class, So operation which needs to be carried out with
     * specified properties needs to be done in this method.
     *
     * @throws DataServiceFault
     */
    public abstract void init() throws DataServiceFault;
}
