package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.sql.AbstractDMLServiceTest;

public class OracleDMLServiceTest extends AbstractDMLServiceTest {

	public OracleDMLServiceTest(String testName) {
		super(testName, "OracleDMLService");
	}
	
	public void testOracleDMLOperations() {
		this.doDMLOperations();
	}
	
	public void testOracleDMLOperationsWithNoResultNestedQuery() {
		this.doDMLOperationsWithNoResultNestedQuery();
	}
	
	public void testOracleDMLOperationsWithNoResultStoredProcNestedQuery() {
		this.doDMLOperationsWithNoResultStoredProcNestedQuery();
	}
	
	public void testOracleDMLOperationsVal1() {
		this.doDMLOperationsVal1();
	}
	
	public void testOracleDMLOperationsVal2() {
		this.doDMLOperationsVal2();
	}
	
	public void testOracleDMLOperationsVal3() {
		this.doDMLOperationsVal3();
	}
	
	public void testOracleDMLOperationsVal4() {
		this.doDMLOperationsVal4();
	}
	
	public void testOracleDMLOperationsVal5() {
		this.doDMLOperationsVal5();
	}
	
	public void testOracleDMLOperationsVal6() {
		this.doDMLOperationsVal6();
	}

}
