/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dataservices.core.test;

import junit.framework.TestCase;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ServerConstants;

public abstract class DataServiceBaseTestCase extends TestCase {
	
	protected String repository = "./target/repository";
	protected String axis2Conf = "./src/test/resources/axis2.xml";
	protected String baseEpr = "http://localhost:5555/axis2/services/";
       protected String carbonHome = "./target/carbonHome";

	public DataServiceBaseTestCase(String testName) {
		super(testName);
	}

       protected void startTenantFlow() {
               System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
               PrivilegedCarbonContext.startTenantFlow();
               PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID, true);
       }

       protected void endTenantFlow() {
               PrivilegedCarbonContext.endTenantFlow();
       }
	
}
