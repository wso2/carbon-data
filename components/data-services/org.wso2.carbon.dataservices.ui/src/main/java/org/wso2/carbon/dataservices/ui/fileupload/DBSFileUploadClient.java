/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dataservices.ui.fileupload;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.dataservices.ui.fileupload.stub.DataServiceFileUploaderStub;

import javax.activation.DataHandler;

public class DBSFileUploadClient {

    private DataServiceFileUploaderStub serviceUploaderStub;

    public DBSFileUploadClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        String serviceUploaderServiceEPR = serverURL + "DataServiceFileUploader";
        serviceUploaderStub = new DataServiceFileUploaderStub(ctx, serviceUploaderServiceEPR);
        ServiceClient client = serviceUploaderStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    public void uploadService(String fileName, String serviceHierarchy,
    		DataHandler dataHandler) throws java.lang.Exception {
        serviceUploaderStub.uploadService(fileName, serviceHierarchy, dataHandler);
    }

    public void uploadWsdl(String fileName, DataHandler dataHandler) throws java.lang.Exception {
        serviceUploaderStub.uploadWSDL(fileName, dataHandler);
    }

    public void uploadWSDLWithURL(String url) throws java.lang.Exception {
        serviceUploaderStub.urlWsdlUpload(url);
    }

}