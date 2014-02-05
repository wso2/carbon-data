package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.sql.AbstractNestedQueryServiceTest;

public class OracleNestedQueryTest extends AbstractNestedQueryServiceTest {

	public OracleNestedQueryTest(String testName) {
		super(testName, "OracleNestedQueryStoredProcService");
	}
	
	public void testOracleNestedQuery2ForDateTime() {
    	this.nestedQuery2ForDateTime();
	}

}
