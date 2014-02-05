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
package org.wso2.carbon.dataservices.ui;
public class UIutils {
       /**
        * pagination
        * */
      public static String[] getChildren(int start, int pageLength, String[] childPaths) {
        int availableLength = 0;
        if (childPaths != null && childPaths.length > 0) {
            availableLength = childPaths.length - start;
        }
        if (availableLength < pageLength) {
            pageLength = availableLength;
        }

        String[] resultChildPaths = new String[pageLength];
        System.arraycopy(childPaths, start, resultChildPaths, 0, pageLength);
        return resultChildPaths;
    }
    
}
