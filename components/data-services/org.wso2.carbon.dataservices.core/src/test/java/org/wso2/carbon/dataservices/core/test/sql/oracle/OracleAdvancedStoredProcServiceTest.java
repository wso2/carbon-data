package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.sql.AbstractAdvancedStoredProcServiceTest;

public class OracleAdvancedStoredProcServiceTest extends AbstractAdvancedStoredProcServiceTest {

	public OracleAdvancedStoredProcServiceTest(String testName) {
		super(testName, "OracleAdvancedStoredProcService");
	}
	
	public void testStoredProcWithOutParams() {
		this.storedProcWithOutParams();
	}
	
	public void testStoredProcWithOutParamsAndRS() {
		this.storedProcWithOutParamsAndRS();
	}
	
	public void testStoredProcWithInOutParamsAndRS() {
		this.storedProcWithInOutParamsAndRS();
	}

    public void testStoredProcWithRefCursors() {
        this.storedProcWithRefCursors();
    }

}
