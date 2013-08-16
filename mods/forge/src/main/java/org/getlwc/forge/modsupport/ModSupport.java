package org.getlwc.forge.modsupport;

import org.getlwc.util.ClassUtils;

public class ModSupport {

    public enum Mod {

        BUILDCRAFT("buildcraft.api.core.BuildCraftAPI"),

        FORGE_ESSENTIALS("com.ForgeEssentials.api.permissions.PermissionsAPI");

        String className;

        Mod(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

    }

    /**
     * Check if the given mod is installed and can be referenced.
     *
     * @param mod
     * @return true if the mod is installed
     */
    public static boolean isModInstalled(Mod mod) {
        if (mod == null) {
            return false;
        }

        return ClassUtils.isClassLoaded(mod.getClassName());
    }

}
