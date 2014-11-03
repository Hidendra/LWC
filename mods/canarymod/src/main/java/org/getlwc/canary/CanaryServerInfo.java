package org.getlwc.canary;

import net.canarymod.Canary;
import org.getlwc.ServerInfo;

public class CanaryServerInfo implements ServerInfo {

    public String getSoftwareName() {
        return Canary.getImplementationTitle();
    }

    public String getServerVersion() {
        return Canary.getImplementationVersion();
    }

}
