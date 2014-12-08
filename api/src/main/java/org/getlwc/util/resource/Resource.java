package org.getlwc.util.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Resource {

    /**
     * The default directory to output files to
     */
    public static final String DEFAULT_OUTPUT_DIR = "lib";

    /**
     * The key for the resource
     */
    private final String key;

    /**
     * The list of files this resource includes
     */
    private final List<String> files = new ArrayList<>();

    /**
     * The class that can be used to test for this resource, if any
     */
    private String testClass = null;

    /**
     * The directory output to (relative to the data directory.)
     */
    private String outputDir = DEFAULT_OUTPUT_DIR;

    public Resource(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return String.format("Resource(key = %s, testClass = %s, outputDir = %s, files = %s)", key, testClass, outputDir, files);
    }

    public String getKey() {
        return key;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public List<String> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void addFile(String file) {
        files.add(file);
    }

}
