package org.getlwc.configuration;

import org.getlwc.configuration.json.JSONConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;

public class JSONConfigurationTest extends AbstractConfigurationTest {

    @Before
    public void setup() {
        emptyConfiguration = new JSONConfiguration();
        preloadedConfiguration = new JSONConfiguration((JSONObject) JSONValue.parse("{ \"map\": { \"key\": \"value\", \"map\": { \"list\": [ 1, 2, 3 ] } }, \"primitives\": { \"int\": 1, \"bool\": true } }"));
    }

}
