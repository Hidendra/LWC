/**
 * This file is part of LWC (https://github.com/Hidendra/LWC)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.griefcraft.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
        return parent.getKeys();
    }

    /**
     * Get an object from one of the extension bundles
     *
     * @param key
     * @return
     */
    private Object getObjectFromExtensionBundles(String key) {
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
        Object object = null;

        if ((object = getObjectFromExtensionBundles(key)) != null) {
            return object;
        }

        return parent.getObject(key);
    }

}
