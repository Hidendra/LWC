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
        return new MappedField(root.get("name").toString(), root.get("srg_name").toString(), root.get("obfuscated_name").toString());
    }

    /**
     * Loads a method from the given root
     *
     * @param root
     * @return
     */
    private static MappedMethod loadMethod(JSONObject root) {
        String name = root.get("name").toString();
        String srgName = root.get("srg_name").toString();
        String srgSignature = root.get("srg_signature").toString();
        String obfName = root.get("obfuscated_name").toString();
        String obfSignature = root.get("obfuscated_signature").toString();

        return new MappedMethod(name, srgName, srgSignature, obfName, obfSignature);
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
