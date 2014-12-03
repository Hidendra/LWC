package org.getlwc.forge.util.asm;

import org.getlwc.forge.asm.mappings.MappedClass;
import org.getlwc.forge.asm.mappings.MappedField;
import org.getlwc.forge.asm.mappings.MappedMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MappingLoader {

    private MappingLoader() {
    }

    /**
     * Load classes from the given {@link java.io.InputStream}
     * @param stream
     * @return
     * @throws IOException
     */
    public static List<MappedClass> loadClasses(InputStream stream) throws IOException {
        List<MappedClass> result = new ArrayList<>();
        JSONObject root = (JSONObject) JSONValue.parse(new InputStreamReader(stream));
        JSONObject classes = (JSONObject) root.get("classes");

        for (Object key : classes.keySet()) {
            JSONObject value = (JSONObject) classes.get(key);
            result.add(loadClass(value));
        }

        return result;
    }

    /**
     * Loads a field from the given root
     *
     * @param root
     * @return
     */
    private static MappedField loadField(JSONObject root) {
        return new MappedField(root.get("name").toString(), root.get("obfuscated_name").toString());
    }

    /**
     * Loads a method from the given root
     *
     * @param root
     * @return
     */
    private static MappedMethod loadMethod(JSONObject root) {
        return new MappedMethod(root.get("name").toString(), root.get("obfuscated_name").toString(), root.get("signature").toString());
    }

    /**
     * Loads a class from the given root
     *
     * @param root
     * @return
     */
    private static MappedClass loadClass(JSONObject root) {
        MappedClass clazz = new MappedClass(root.get("canonical_name").toString(), root.get("obfuscated_name").toString());

        for (Object object : (JSONArray) root.get("methods")) {
            clazz.addMethod(loadMethod((JSONObject) object));
        }

        for (Object object : (JSONArray) root.get("fields")) {
            clazz.addField(loadField((JSONObject) object));
        }

        return clazz;
    }

}
