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
package org.wso2.carbon.dataservices.core.test.sql;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.dataservices.core.test.DataServiceBaseTestCase;
import org.wso2.carbon.dataservices.core.test.util.TestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to represent advanced stored procedure functionalities, i.e. IN/OUT parameters,
 * mixing result sets with OUT parameters.
 */
public class AbstractAdvancedStoredProcServiceTest extends DataServiceBaseTestCase {

	private String epr;
	
	public AbstractAdvancedStoredProcServiceTest(String testName, String serviceName) {
		super(testName);
		this.epr = this.baseEpr + serviceName;
	}
	
	/**
	 * Test with a stored procedure call with OUT parameters.
	 */
	protected void storedProcWithOutParams() {
		TestUtils.showMessage(this.epr + " - storedProcWithOutParams");
		try {
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_with_out_params_op", null);
			String customerNumber = TestUtils.getFirstValue(result, 
					"/Customers/Customer/customerNumber", TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(customerNumber) > 0);
			String customerName = TestUtils.getFirstValue(result, 
					"/Customers/Customer/customerName", TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(customerName.length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * Test with a stored procedure call with OUT parameters and a result set.
	 */
	protected void storedProcWithOutParamsAndRS() {
		TestUtils.showMessage(this.epr + " - storedProcWithOutParamsAndRS");
		try {
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_with_out_params_result_set_op", null);
			String customerNumber = TestUtils.getFirstValue(result, 
					"/Customers/Customer/customerNumber", TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(customerNumber) > 0);
			TestUtils.validateResultStructure(result, TestUtils.CUSTOMER_XSD_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test with a stored procedure call with OUT/INOUT parameters and a result set.
	 */
	protected void storedProcWithInOutParamsAndRS() {
		TestUtils.showMessage(this.epr + " - storedProcWithInOutParamsAndRS");
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("customerNumber", "103");
			OMElement result = TestUtils.callOperation(this.epr,
					"stored_procedure_with_inout_params_result_set_op", params);
			String customerNumber = TestUtils.getFirstValue(result, 
					"/Customers/Customer/customerNumber", TestUtils.DEFAULT_DS_WS_NAMESPACE);
			assertTrue(Integer.parseInt(customerNumber) > 0);
			TestUtils.validateResultStructure(result, TestUtils.CUSTOMER_XSD_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

    /**
     * Test with oracle Ref-Cursors. This test used to verify the "ORA-01000: maximum open cursors exceeded" issue.
     */
    protected void storedProcWithRefCursors() {
        TestUtils.showMessage(this.epr + " - storedProcWithRefCursors");
        try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", "480");
            for (int i = 0; i <= 1000; i++) {
                OMElement result = TestUtils.callOperation(this.epr,
                        "stored_procedure_with_ref_cursors_op", params);
                String customerFirstName = TestUtils.getFirstValue(result,
                        "/Customers/Customer/contactFirstName", TestUtils.DEFAULT_DS_WS_NAMESPACE);
                assertTrue(customerFirstName.length() > 0);
            }
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

    }
	
}
