package org.wso2.carbon.dataservices.core.test.sql.oracle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OracleTestSuite extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.wso2.carbon.dataservices.core.test.sql.oracle");
		suite.addTestSuite(OracleInitTest.class);
		suite.addTestSuite(OracleBasicTest.class);
		suite.addTestSuite(OracleDMLServiceTest.class);
		suite.addTestSuite(OracleInputMappingServiceTest.class);
		suite.addTestSuite(OracleNestedQueryTest.class);
		suite.addTestSuite(OracleAdvancedStoredProcServiceTest.class);
		suite.addTestSuite(OracleBinaryDataServiceTest.class);
		suite.addTestSuite(OracleStoredProcedureServiceTest.class);
		suite.addTestSuite(OracleFinalizeTest.class);
		
		return suite;
	}

}
