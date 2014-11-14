package org.getlwc.canary;

import net.canarymod.Canary;
import org.getlwc.ServerInfo;

public class CanaryServerInfo implements ServerInfo {

    @Override
    public String getServerImplementationTitle() {
        return Canary.getImplementationTitle();
    }

    @Override
    public String getServerImplementationVersion() {
        return Canary.getImplementationVersion();
    }

}
