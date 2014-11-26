package org.getlwc.granite;

import org.getlwc.ServerInfo;

public class GraniteServerInfo implements ServerInfo {

    @Override
    public String getServerImplementationTitle() {
        return "Granite";
    }

    @Override
    public String getServerImplementationVersion() {
        // TODO
        return "Unknown";
    }

}
