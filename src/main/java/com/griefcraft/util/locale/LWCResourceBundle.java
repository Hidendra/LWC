/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.util.locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class LWCResourceBundle extends ResourceBundle {

    /**
     * Stores bundles that override the defaults
     */
    private List<ResourceBundle> extensionBundles = new ArrayList<ResourceBundle>();

    public LWCResourceBundle(ResourceBundle parent) {
        this.parent = parent;
    }

    /**
     * Add a ResourceBundle to the extras
     *
     * @param bundle
     */
    public void addExtensionBundle(ResourceBundle bundle) {
        if (bundle == null) {
            return;
        }

        extensionBundles.add(bundle);
    }

    @Override
    public Enumeration<String> getKeys() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(parent.keySet());

        // add the extension bundles' keys as well
        for (ResourceBundle bundle : extensionBundles) {
            keys.addAll(bundle.keySet());
        }

        return Collections.enumeration(keys);
    }

    /**
     * Get an object from one of the extension bundles
     *
     * @param key
     * @return
     */
    private Object getObjectFromExtensionBundles(String key) {
        if (extensionBundles.size() == 0) {
            return null;
        }

        try {
            for (ResourceBundle bundle : extensionBundles) {
                Object object = bundle.getObject(key);

                if (object != null) {
                    return object;
                }
            }
        } catch (MissingResourceException e) {
        }

        return null;
    }

    @Override
    protected Object handleGetObject(String key) {
        Object object;

        if ((object = getObjectFromExtensionBundles(key)) != null) {
            return object;
        }

        return parent.getObject(key);
    }

}
