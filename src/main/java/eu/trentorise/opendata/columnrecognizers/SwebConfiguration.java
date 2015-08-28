/*
 * Copyright 2015 Trento Rise.
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
package eu.trentorise.opendata.columnrecognizers;

import static com.google.common.base.Preconditions.checkNotNull;
import eu.trentorise.opendata.commons.OdtUtils;
import it.unitn.disi.sweb.webapi.model.Configuration;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a little hack to sweb client to allow loading configuration
 * from wherever we want. Copied from Disi Client. In the future it will become
 * the only SwebConfiguration for all odt-sweb related stuff. Todo make it
 * unique!
 *
 * @author David Leoni
 */
public class SwebConfiguration extends Configuration {

    public static final String PROPERTIES_PREFIX = "sweb.webapi";

    private static final Logger LOG = LoggerFactory.getLogger(SwebConfiguration.class);

    public static final String SWEB_PROPERTIES_FILENAME = "sweb-webapi-model.properties";

    public static final String SWEB_WEBAPI_ROOT = "sweb.webapi.root";
    public static final String SWEB_WEBAPI_TEST_USER = "sweb.webapi.test.user";
    public static final String SWEB_WEBAPI_KB_DEFAULT = "sweb.webapi.kb.default";
    public static final String SWEB_WEBAPI_HOST = "sweb.webapi.host";
    public static final String SWEB_WEBAPI_PORT = "sweb.webapi.port";
    public static final String SWEB_WEBAPI_PROXY = "sweb.webapi.proxy";
    public static final String SWEB_WEBAPI_PROXY_HOST = "sweb.webapi.proxy.host";
    public static final String SWEB_WEBAPI_PROXY_PORT = "sweb.webapi.proxy.port";
    public static final String SWEB_WEBAPI_IDLE_TIMEOUT = "sweb.webapi.idle.timeout";
    public static final String SWEB_WEBAPI_READ_TIMEOUT = "sweb.webapi.read.timeout";
    public static final String SWEB_WEBAPI_MAX_CONNECTIONS = "sweb.webapi.maxconnections";

    private static String host;
    private static int port;
    private static String root;

    private SwebConfiguration() {
        super(SWEB_PROPERTIES_FILENAME);
    }

    /**
     * Throws exception if client is not properly initialized
     */
    public static void checkInitialized() {
        OdtUtils.checkNotEmpty(getString(SWEB_WEBAPI_ROOT), SWEB_WEBAPI_ROOT + " is invalid!");
        OdtUtils.checkNotEmpty(getString(SWEB_WEBAPI_TEST_USER), SWEB_WEBAPI_TEST_USER + " is invalid!");
        OdtUtils.checkNotEmpty(getString(SWEB_WEBAPI_KB_DEFAULT), SWEB_WEBAPI_KB_DEFAULT + " is invalid!");
        OdtUtils.checkNotEmpty(getString(SWEB_WEBAPI_HOST), SWEB_WEBAPI_HOST + " is invalid!");
        OdtUtils.checkNotEmpty(getString(SWEB_WEBAPI_PORT), SWEB_WEBAPI_PORT + " is invalid!");
    }

    /**
     * Overrides existing configuration merging given properties to existing ones.
     */
    static public void init(Map<String, String> properties) {
        checkNotNull(properties);
        if (props.size() > 0) {
            LOG.info("Found " + props.size() + " sweb properties, updating " + properties.size() + " of them.");

            for (Entry<String, String> entry : properties.entrySet()) {
                if (entry.getKey().startsWith(PROPERTIES_PREFIX)) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        host = checkNotNull(SwebConfiguration.getString(SWEB_WEBAPI_HOST));
        port = Integer.parseInt(SwebConfiguration.getString(SWEB_WEBAPI_PORT));
        root = checkNotNull(SwebConfiguration.getString(SWEB_WEBAPI_ROOT));

    }

    public static String getBaseUrl() {
        checkInitialized();
        LOG.warn("TODO - ASSUMING HTTP AS PROTOCOL");
        return "http://" + host + ":" + port + root;
    }

}


