package org.getlwc.canary;

import net.canarymod.Canary;
import org.getlwc.ServerInfo;

public class CanaryServerInfo implements ServerInfo {

    public String getServerImplementationTitle() {
        return Canary.getImplementationTitle();
    }

    public String getServerImplementationVersion() {
        return Canary.getImplementationVersion();
    }

}
