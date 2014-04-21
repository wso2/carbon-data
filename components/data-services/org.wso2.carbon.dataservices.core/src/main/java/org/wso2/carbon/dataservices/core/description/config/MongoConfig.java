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
package org.wso2.carbon.dataservices.core.description.config;


import com.mongodb.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jongo.Jongo;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.DataServiceFault;

import org.wso2.carbon.dataservices.core.engine.DataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This class represents a MongoDB based data source configuration.
 */

public class MongoConfig extends Config {

    private static final Log log = LogFactory.getLog(MongoConfig.class);

    private MongoClient mongoClient;

    private String [] servers;

    private  MongoClientOptions mongoClientOptions;

    private Jongo jongo;

    public MongoConfig(DataService dataService, String configId, Map<String, String> properties)
            throws DataServiceFault {
        super(dataService, configId, DBConstants.DataSourceTypes.MONGODB, properties);
        String serversParam = properties.get(DBConstants.MongoDB.SERVERS);
        if (DBUtils.isEmptyString(serversParam)) {
            throw new DataServiceFault("The data source param '" +
                    DBConstants.MongoDB.SERVERS + "' is required");
        }
        this.servers = serversParam.split(",");
        String database = properties.get(DBConstants.MongoDB.DATABASE);
        if (DBUtils.isEmptyString(database)) {
            throw new DataServiceFault("The data source param '" +
                    DBConstants.MongoDB.DATABASE + "' is required");
        }
        try {
            this.mongoClientOptions = extractMongoOptions(properties);
            this.mongoClient = createNewMongo();
            String writeConcern = properties.get(DBConstants.MongoDB.WRITE_CONCERN);
            if (!DBUtils.isEmptyString(writeConcern)) {
                this.getMongoClient().setWriteConcern(WriteConcern.valueOf(writeConcern));
            }
            String readPref = properties.get(DBConstants.MongoDB.READ_PREFERENCE);
            if (!DBUtils.isEmptyString(readPref)) {
                this.getMongoClient().setReadPreference(ReadPreference.valueOf(readPref));
            }
            this.jongo = new Jongo(this.getMongoClient().getDB(database));
        } catch (Exception e) {
            throw new DataServiceFault(e);
        }

    }

    public MongoClient createNewMongo() throws DataServiceFault{
        try {
            return new MongoClient(this.createServerAddresses(this.getServers()),
                getMongoClientOptions());
        } catch (Exception e) {
            throw new DataServiceFault(e);
        }
    }

    @Override
    public boolean isActive() {
        try {
            Mongo mon = this.createNewMongo();
            return mon != null;
        } catch (Exception e) {
            log.error("Error in checking Mongo config availability", e);
            return false;
        }
    }

    @Override
    public void close() {
         /* nothing to close */
    }

    private MongoClientOptions extractMongoOptions(Map<String, String> properties) {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        String autoConnectRetry = properties.get(DBConstants.MongoDB.AUTO_CONNECT_RETRY);
        if (!DBUtils.isEmptyString(autoConnectRetry)) {
            builder.autoConnectRetry(Boolean.parseBoolean(autoConnectRetry));
        }
        String connectionsPerHost = properties.get(DBConstants.MongoDB.CONNECTIONS_PER_HOST);
        if (!DBUtils.isEmptyString(connectionsPerHost)) {
            builder.connectionsPerHost(Integer.parseInt(connectionsPerHost));
        }
        String maxWaitTime = properties.get(DBConstants.MongoDB.MAX_WAIT_TIME);
        if (!DBUtils.isEmptyString(maxWaitTime)) {
            builder.maxWaitTime(Integer.parseInt(maxWaitTime));
        }
        String connectTimeout = properties.get(DBConstants.MongoDB.CONNECT_TIMEOUT);
        if (!DBUtils.isEmptyString(connectTimeout)) {
            builder.connectTimeout(Integer.parseInt(connectTimeout));
        }
        String socketTimeout = properties.get(DBConstants.MongoDB.SOCKET_TIMEOUT);
        if (!DBUtils.isEmptyString(socketTimeout)) {
            builder.socketTimeout(Integer.parseInt(socketTimeout));
        }
        String threadsAllowedToBlockForConnectionMultiplier = properties.get(
                DBConstants.MongoDB.THREADS_ALLOWED_TO_BLOCK_CONN_MULTIPLIER);
        if (!DBUtils.isEmptyString(threadsAllowedToBlockForConnectionMultiplier)) {
            builder.threadsAllowedToBlockForConnectionMultiplier(
                    Integer.parseInt(threadsAllowedToBlockForConnectionMultiplier));
        }
        return builder.build();
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    private List<ServerAddress> createServerAddresses(String[] servers) throws Exception {
        List<ServerAddress> result = new ArrayList<ServerAddress>();
        String[] tmpAddr;
        for (String server : servers) {
            tmpAddr = server.split(":");
            if (tmpAddr.length == 2) {
                result.add(new ServerAddress(tmpAddr[0], Integer.parseInt(tmpAddr[1])));
            } else {
                result.add(new ServerAddress(tmpAddr[0]));
            }
        }
        return result;
    }

    public String[] getServers() {
        return servers;
    }

    public  MongoClientOptions getMongoClientOptions() {
        return mongoClientOptions;
    }

    public Jongo getJongo() {
        return jongo;
    }
}
