/*
 * Copyright (c) 2011-2013 Tyler Blair
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

package org.getlwc;

import org.getlwc.attribute.ProtectionAttributeFactory;
import org.getlwc.configuration.Configuration;
import org.getlwc.model.AbstractAttribute;
import org.getlwc.model.Protection;
import org.getlwc.roles.PlayerProtectionRole;
import org.yaml.snakeyaml.scanner.ScannerImpl;

import java.util.HashMap;
import java.util.Map;

public class SimpleProtectionManager implements ProtectionManager {

    /**
     * The LWC engine instance
     */
    private Engine engine;

    /**
     * ProtectAttributeFactory storage
     */
    private final Map<String, ProtectionAttributeFactory> protectionFactories = new HashMap<String, ProtectionAttributeFactory>();

    public SimpleProtectionManager(Engine engine) {
        this.engine = engine;
    }

    /**
     * Clear all attributes
     */
    public void clearAttributes() {
        protectionFactories.clear();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBlockProtectable(Block block) {
        String enabled = getProtectionConfiguration("enabled", block.getName(), Integer.toString(block.getType()));
        return enabled.equalsIgnoreCase("true") || enabled.equalsIgnoreCase("yes");
    }

    /**
     * {@inheritDoc}
     */
    public Protection findProtection(Location location) {
        ProtectionMatcher matcher = new SimpleProtectionMatcher(engine);

        // Get the block at the location
        // this will be our base block -- or reference point -- of where the protection is matched from
        Block base = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // attempt to match the protection set
        ProtectionSet blocks = matcher.matchProtection(base);

        // return the matched protection
        return blocks.getResultant();
    }

    /**
     * {@inheritDoc}
     */
    public Protection createProtection(String owner, Location location) {
        // First create the protection
        Protection protection = engine.getDatabase().createProtection(location);

        // ensure it was created
        if (protection == null) {
            System.out.println("createProtection returned null!");
            return null;
        }

        // add the Owner role to the database for the player
        protection.addRole(new PlayerProtectionRole(engine, protection, owner, ProtectionRole.Access.OWNER));
        protection.save();

        return protection;
    }

    /**
     * {@inheritDoc}
     */
    public void registerAttributeFactory(ProtectionAttributeFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }

        protectionFactories.put(factory.getName().toLowerCase(), factory);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractAttribute createProtectionAttribute(String name) {
        ProtectionAttributeFactory factory = protectionFactories.get(name.toLowerCase());
        return factory == null ? null : factory.createAttribute();
    }

    /**
     * Get protection configuration
     *
     * @param node
     * @param match a list of strings that can be matched. e.g [ chest, 54 ] -> will match protections.protectables.chest and protections.protectables.54
     * @return
     */
    private String getProtectionConfiguration(String node, String... match) {
        Configuration configuration = engine.getConfiguration();

        String value = null;

        // try highest nodes first
        for (String m : match) {
            // period (.) in the match name is a special case
            if (m.contains(".")) {
                Map<String, Object> map = (Map<String, Object>) configuration.get("protections.protectables");

                if (map.containsKey(m)) {
                    Map<String, Object> sub = (Map<String, Object>) map.get(m);

                    if (sub.containsKey(node)) {
                        value = sub.get(node).toString();
                    }
                }
            } else {
                value = configuration.getString("protections.protectables." + m + "." + node, null);
            }

            if (value != null) {
                break;
            }
        }

        if (value == null) {
            // try the defaults
            value = configuration.getString("protections." + node, "");
        }

        return value;
    }

}
