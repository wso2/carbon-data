/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.admin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.WSDLToDataService;

/**
 * Data Services admin service class used for file uploading.
 */
public class DataServiceFileUploader extends DataServiceAdmin {
	
    private static final Log log = LogFactory.getLog(DataServiceFileUploader.class);

    public String uploadService(String fileName, String serviceHierarchy, DataHandler dataHandler)
			throws Exception {
    	try {
    		byte[] data = this.getByteArrayFromInputStream(dataHandler.getInputStream());
    		String serviceContents = new String(data);
    		String serviceName = fileName;
    		int index = fileName.lastIndexOf("." + this.getDataServiceFileExtension());
    		if (index != -1) {
    			serviceName = serviceName.substring(0, index);
    		}    		
			this.saveDataService(serviceName, serviceHierarchy, serviceContents);
		} catch (Exception e) {
			String msg = "Error occured while uploading the service "
					+ fileName;
			log.error(msg, e);
			throw new Exception("Failed to upload the service archive "
					+ fileName, e);
		}

		return DBConstants.LABEL_SUCESSFULL;
	}

    public String uploadWSDL(String fileName, DataHandler dataHandler)
			throws Exception {
    	WSDLToDataService.deployDataService(this.getAxisConfig(), this
				.getByteArrayFromInputStream(dataHandler.getInputStream()));
    	return DBConstants.LABEL_SUCESSFULL;
	}

    public String urlWsdlUpload(String url)
			throws Exception {
        URLConnection conn = new URL(url).openConnection();
        WSDLToDataService.deployDataService(this.getAxisConfig(), this
				.getByteArrayFromInputStream(conn.getInputStream()));
        return DBConstants.LABEL_SUCESSFULL;
	}

    private byte[] getByteArrayFromInputStream(InputStream in) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] buff = new byte[512];
        int i;
        while ((i = in.read(buff)) > 0) {
            byteOut.write(buff, 0, i);
        }
        in.close();
        return byteOut.toByteArray();
    }

}