/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
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
     * Resources that this resource depends on
     */
    private final List<String> dependencies = new ArrayList<>();

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

    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public void addDependency(String dependency) {
        dependencies.add(dependency);
    }

}
