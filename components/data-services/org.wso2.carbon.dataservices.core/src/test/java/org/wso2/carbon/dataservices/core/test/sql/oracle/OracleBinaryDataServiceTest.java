package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.sql.AbstractBinaryDataServiceTest;

public class OracleBinaryDataServiceTest extends AbstractBinaryDataServiceTest {

	public OracleBinaryDataServiceTest(String testName) {
		super(testName, "OracleBinaryDataService");
	}
	
	public void testMySQLBinaryDataStoreRetrieve() {
		this.binaryDataStoreRetrieve();
	}

}
