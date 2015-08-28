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
package eu.trentorise.opendata.columnrecognizers.test;

import eu.trentorise.opendata.columnrecognizers.SwebConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Loads configuration for tests. Todo it should produce an IEkb, will do when
 * dependency hell will be resolved.
 *
 * @author David Leoni
 */
public class ConfigLoader {

    /**
     * Loads configuration from conf/sweb-webapi-model-override.properties file
     */
    public static void init() {
        File file = new File("conf/sweb-webapi-model-override.properties");
        try {
            Properties props = new Properties();
            InputStream in = new FileInputStream(file);
            props.load(in);
            SwebConfiguration.init((Map) props);
        }
        catch (Exception ex) {
            throw new RuntimeException("Error while loading " + file.getAbsolutePath(), ex);
        }
    }
}
