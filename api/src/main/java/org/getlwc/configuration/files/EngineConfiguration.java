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
package org.getlwc.configuration.files;

import org.getlwc.BlockType;
import org.getlwc.configuration.Configuration;
import org.getlwc.db.DatabaseConnectionDetails;
import org.getlwc.lang.Locale;

import javax.inject.Singleton;
import java.io.IOException;

/**
 * TODO name?
 *
 * A class for accessing core configurable properties that the engine uses.
 * This is preferred to performing direct access on the configuration.
 */
@Singleton
public class EngineConfiguration {

    private Configuration config;
    private Configuration protectablesView;

    public EngineConfiguration(Configuration config) {
        this.config = config;
        this.protectablesView = config.viewFor("protections");

        setDefaults();

        // flush the defaults
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.format("EngineConfiguration(impl = %s)", config.getClass().getCanonicalName());
    }

    /**
     * Sets the defaults for the config
     */
    private void setDefaults() {
        config.setDefault("locale", "en_US");

        // notifications
        config.setDefault("notifications.missingTranslation", true);
        config.setDefault("notifications.recentlyTranslated", true);

        // database
        config.setDefault("database.driver", "h2");
        config.setDefault("database.path", "%home%/lwc.db");
        config.setDefault("database.hostname", "127.0.0.1");
        config.setDefault("database.database", "lwd");
        config.setDefault("database.username", "");
        config.setDefault("database.password", "");
        config.setDefault("database.prefix", "lwc_");

        // global protectable defaults
        protectablesView.setDefault("enabled", false);
        protectablesView.setDefault("autoRegister", false);
    }

    /**
     * Gets the default locale. This is typically only used for e.g.
     * the console or players that are using a Minecraft locale that
     * LWC is not translated in.
     *
     * @return
     */
    public Locale getDefaultLocale() {
        return new Locale(config.getString("locale"));
    }

    /**
     * Gets the protection configuration for the given block.
     *
     * @param type
     * @return
     */
    public ProtectableConfiguration getProtectableBlockConfig(BlockType type) {
        Configuration view = protectablesView.viewFor("blocks." + type.getId());
        return new ProtectableConfiguration(view);
    }

    /**
     * Checks if players should be notified when they log in if
     * LWC does not have a translation for their language,
     * asking them if they would like to help translate it
     * officially.
     *
     * @return
     */
    public boolean shouldNotifyMissingTranslations() {
        return config.getBoolean("notifications.missingTranslation");
    }

    /**
     * Checks if the player is using a translation that has only
     * been translated recently, so that if they find issues with
     * it, they know where to go to submit changes.
     *
     * @return
     */
    public boolean shouldNotifyRecentlyTranslated() {
        return config.getBoolean("notifications.recentlyTranslated");
    }

    /**
     * Gets the connection details to the database
     *
     * @return
     */
    public DatabaseConnectionDetails getDatabaseConnectionDetails() {
        return new DatabaseConnectionDetails(
                config.getString("database.driver"),
                config.getString("database.path"),
                config.getString("database.hostname"),
                config.getString("database.database"),
                config.getString("database.username"),
                config.getString("database.password"),
                config.getString("database.prefix")
        );
    }

}
