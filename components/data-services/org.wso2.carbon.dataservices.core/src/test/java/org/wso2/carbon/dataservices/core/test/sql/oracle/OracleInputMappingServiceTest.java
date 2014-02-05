package org.wso2.carbon.dataservices.core.test.sql.oracle;

import org.wso2.carbon.dataservices.core.test.sql.AbstractInputMappingServiceTest;

public class OracleInputMappingServiceTest extends AbstractInputMappingServiceTest {

	public OracleInputMappingServiceTest(String testName) {
		super(testName, "OracleInputMappingService");
	}
	
	public void testOracleInputMappings1ForDateTime() {
		this.inputMappings1ForDateTime();
	}
	
    public void testOracleInputMappings2ForDateTime() {
    	this.inputMappings2ForDateTime();
	}
    
    public void testOracleInputMappingsCallQueryMergeForDateTime() {
    	this.inputMappingsCallQueryMergeForDateTime();
    }

    public void testInputMappingsWithDefValueForDateTime() {
        this.inputMappingsWithDefValueForDateTime();
    }
	
}
