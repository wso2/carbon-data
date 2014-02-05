package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.sql.AbstractStoredProcedureServiceTest;

public class OracleStoredProcedureServiceTest extends AbstractStoredProcedureServiceTest{

	public OracleStoredProcedureServiceTest(String testName) {
		super(testName, "OracleNestedQueryStoredProcService");
	}
	
	public void testOracleStoredProcNoParams() {
		this.storedProcNoParams();
	}
	
	public void testOracleStoredProcWithParams() {
		this.storedProcWithParams();
	}
	
	public void testStoredProcNested1ForDateTime() {
		this.storedProcNested1ForDateTime();
	}
	
	public void testStoredProcNested2ForDateTime() {
		this.storedProcNested2ForDateTime();
	}
	
	public void testStoredProcNested3ForDateTime() {
		this.storedProcNested3ForDateTime();
	}
	
	public void testOracleStoredFuncNoParams() {
		this.storedFuncNoParams();
	}
	
	public void testOracleStoredFuncWithParams() {
		this.storedFuncWithParams();
	}

}
