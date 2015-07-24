/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.datasource.reader.cassandra.config;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "queryOptions")
public class QueryOptionsConfig {

    private QueryOptions queryOptions;

    public QueryOptionsConfig() {
        this.queryOptions = new QueryOptions();
    }

    public QueryOptions getQueryOptions() {
        return this.queryOptions;
    }

    @XmlElement(name = "consistencyLevel")
    public String getConsistencyLevel() {
        return this.queryOptions.getConsistencyLevel().toString();
    }

    public void setConsistencyLevel(String level) {
        this.queryOptions.setConsistencyLevel(ConsistencyLevel.valueOf(level));
    }

    @XmlElement(name = "serialConsistencyLevel")
    public String getSerialConsistencyLevel() {
        return this.queryOptions.getSerialConsistencyLevel().toString();
    }

    public void setSerialConsistencyLevel(String level) {
        this.queryOptions.setSerialConsistencyLevel(ConsistencyLevel.valueOf(level));
    }

    @XmlElement(name = "fetchSize")
    public int getFetchSize() {
        return this.queryOptions.getFetchSize();
    }

    public void setFetchSize(int fetchSize) {
        this.queryOptions.setFetchSize(fetchSize);
    }

}
