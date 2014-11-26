package org.getlwc.granite;

import org.getlwc.ServerInfo;
import org.granitemc.granite.api.Granite;

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
