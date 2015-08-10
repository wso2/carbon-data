package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.carbon.dataservices.core.test.util.UtilServer;

public class OracleInitTest extends DataServiceBaseTestCase{

	public OracleInitTest() {
		super("OracleInitTest");
		System.setProperty("user.timezone", "CDT");
	}
	
	public void testOracleStartup() throws Exception {
               startTenantFlow();
		UtilServer.start(repository, axis2Conf);
	}

}
