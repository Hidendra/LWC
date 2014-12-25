package org.getlwc.configuration.json;

import org.getlwc.configuration.Configuration;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class JSONConfiguration implements Configuration {

    /**
     * The root node
     */
    private JSONObject root;

    /**
     * The file the configuration is being serialized to.
     * If null, the configuration won't be saved
     */
    private File file = null;

    public JSONConfiguration(JSONObject root, File file) {
        this.root = root;
        this.file = file;
    }

    public JSONConfiguration(JSONObject root) {
        this.root = root;
    }

    public JSONConfiguration() {
        this.root = new JSONObject();
    }

    @Override
    public boolean contains(String path) {
        JSONObject node = getNode(getNodePath(path));

        if (node == null) {
            return false;
        }

        return node.containsKey(getNodeKey(path));
    }

    @Override
    public void set(String path, Object value) {
        JSONObject node = getOrCreateNode(getNodePath(path));

        if (node == null) {
            throw new IllegalStateException("Tried to set() on an unitialized path " + Arrays.toString(getNodePath(path)) + "!");
        }

        node.put(getNodeKey(path), value);
    }

    @Override
    public Object get(String path) {
        JSONObject node = getNode(getNodePath(path));

        if (node == null) {
            return null;
        }

        return node.get(getNodeKey(path));
    }

    @Override
    public String getString(String path) {
        Object value = get(path);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    @Override
    public boolean getBoolean(String path) {
        return castBoolean(get(path));
    }

    @Override
    public int getInt(String path) {
        return castInt(get(path));
    }

    @Override
    public double getDouble(String path) {
        return castDouble(get(path));
    }

    @Override
    public void save() throws IOException {
        root.writeJSONString(new FileWriter(file));
    }

    /**
     * Gets the node for the given path
     *
     * @param path
     * @return
     */
    private JSONObject getNode(String[] path) {
        JSONObject node = root;

        for (String key : path) {
            Object o = node.get(key);

            if (o == null || !(o instanceof JSONObject)) {
                return null;
            }

            node = (JSONObject) o;
        }

        return node;
    }

    /**
     * Gets the node for the given path. If nodes along the path do not exist,
     * the will be created.
     *
     * @param path
     * @return
     */
    private JSONObject getOrCreateNode(String[] path) {
        JSONObject node = root;

        for (String key : path) {
            Object o = node.get(key);

            if (o == null || !(o instanceof JSONObject)) {
                o = new JSONObject();
                node.put(key, o);
            }

            node = (JSONObject) o;
        }

        return node;
    }

    /**
     * Gets the path to a given node.
     * e.g. key = String[0], some.sub.key = { "some", "sub" }
     *
     * @param path
     * @return
     */
    private String[] getNodePath(String path) {
        String[] split = path.split("\\.");

        if (split.length == 1) {
            return new String[0];
        } else {
            String[] result = new String[split.length - 1];
            System.arraycopy(split, 0, result, 0, result.length);
            return result;
        }
    }

    /**
     * Gets the key of final node that is accessed with.
     * e.g. key = key, some.sub.key = key
     *
     * @param path
     * @return
     */
    private String getNodeKey(String path) {
        String[] split = path.split("\\.");

        if (split.length == 1) {
            return split[0];
        } else {
            return split[split.length - 1];
        }
    }

    /**
     * Casts a value to an integer.
     *
     * @param o
     * @return the casted integer; if unknown it returns 0
     */
    private static int castInt(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Byte) {
            return (int) (Byte) o;
        } else if (o instanceof Integer) {
            return (int) o;
        } else if (o instanceof Double) {
            return (int) (double) (Double) o;
        } else if (o instanceof Float) {
            return (int) (float) (Float) o;
        } else if (o instanceof Long) {
            return (int) (long) (Long) o;
        } else {
            return 0;
        }
    }

    /**
     * Casts a value to a double.
     *
     * @param o
     * @return the casted integer; if unknown it returns 0
     */
    private static double castDouble(Object o) {
        if (o == null) {
            return 0;
        } else if (o instanceof Float) {
            return (double) (Float) o;
        } else if (o instanceof Double) {
            return (double) o;
        } else if (o instanceof Byte) {
            return (double) (Byte) o;
        } else if (o instanceof Integer) {
            return (double) (Integer) o;
        } else if (o instanceof Long) {
            return (double) (Long) o;
        } else {
            return 0;
        }
    }

    /**
     * Casts a value to a boolean.
     *
     * @param o
     * @return the casted integer; if unknown it returns 0
     */
    private static boolean castBoolean(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof Boolean) {
            return (boolean) o;
        } else {
            return false;
        }
    }

}
