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
package org.wso2.carbon.dataservices.google.tokengen.servlet.internal;

import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Declarative service component GoogleTokengen component.
 */
@Component(
        name = "org.wso2.carbon.dataservices.google.tokengen.servlet.internal.GoogleTokenGenDSComponent",
        immediate = true)
public class GoogleTokenGenDSComponent {

    private static final Log log = LogFactory.getLog(GoogleTokenGenDSComponent.class);

    private static HazelcastInstance hazelcastInstance;

    @Activate
    protected void activate(ComponentContext context) {

        log.info("Activating GoogleTokengen DS component");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.info("Deactivating Google Tokengen DS component");
    }

    @Reference(
            name = "hazelcast.instance.service",
            service = com.hazelcast.core.HazelcastInstance.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHazelcastInstance")
    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {

        if (log.isDebugEnabled()) {
            log.debug("Setting Hazelcast instance");
        }
        GoogleTokenGenDSComponent.hazelcastInstance = hazelcastInstance;
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {

        if (log.isDebugEnabled()) {
            log.debug("Un-setting Hazelcast instance");
        }
        GoogleTokenGenDSComponent.hazelcastInstance = null;
    }

    public static HazelcastInstance getHazelcastInstance() {

        return hazelcastInstance;
    }
}
