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

package org.wso2.carbon.ndataservices.core;


import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DataServiceDeployer {

    private DataServiceXMLConfiguration loadDataServiceConfig(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        JAXBContext ctx;
        try {
            ctx = JAXBContext.newInstance(DataServiceXMLConfiguration.class);
            return (DataServiceXMLConfiguration) ctx
                    .createUnmarshaller().unmarshal(new FileInputStream(file));
        } catch (JAXBException e) {
             throw new Exception(e.getMessage(),e);
        } catch (FileNotFoundException e) {
            throw new Exception(e.getMessage(),e);
        }

    }
}
