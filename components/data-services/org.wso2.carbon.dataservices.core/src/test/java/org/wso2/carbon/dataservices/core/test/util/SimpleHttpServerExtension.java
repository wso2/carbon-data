package org.wso2.carbon.dataservices.core.test.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.transport.http.server.HttpFactory;

/**
 * Created by rajith on 10/9/15.
 */
public class SimpleHttpServerExtension extends SimpleHTTPServer {
    /**
     * Create a SimpleHTTPServer using default HttpFactory settings
     */
    public SimpleHttpServerExtension(ConfigurationContext configurationContext, int port) throws AxisFault {
        super(new HttpFactory(configurationContext, port));
        DSComponentExtension dsComponentExtension = new DSComponentExtension();
        dsComponentExtension.activate();
    }
}
