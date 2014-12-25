package org.getlwc.configuration.json;

import org.getlwc.configuration.Configuration;
import org.getlwc.configuration.ConfigurationLoader;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class JSONConfigurationLoader implements ConfigurationLoader {

    @Override
    public Configuration load(File file) {
        try (InputStream stream = new FileInputStream(file)) {
            return new JSONConfiguration(loadFromStream(stream), file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Configuration load(InputStream stream) {
        try {
            return new JSONConfiguration(loadFromStream(stream));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a JSONObject from the given stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private JSONObject loadFromStream(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return (JSONObject) JSONValue.parse(reader);
        }
    }

}
