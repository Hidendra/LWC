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

package com.griefcraft.util;

public class UpdaterFile {

    /**
     * The local url location
     */
    private String localLocation;

    /**
     * The remote url location
     */
    private String remoteLocation;

    public UpdaterFile(String location) {
        this.remoteLocation = location;
        this.localLocation = location;
    }

    public UpdaterFile(String localLocation, String remoteLocation) {
        this.localLocation = localLocation;
        this.remoteLocation = remoteLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UpdaterFile)) {
            return false;
        }

        UpdaterFile other = (UpdaterFile) obj;

        return other.getLocalLocation().equals(localLocation) && other.getRemoteLocation().equals(remoteLocation);
    }

    /**
     * @return the local file location
     */
    public String getLocalLocation() {
        return localLocation;
    }

    /**
     * @return the remote url location
     */
    public String getRemoteLocation() {
        return remoteLocation;
    }

    /**
     * Set the local file location
     *
     * @param localLocation
     */
    public void setLocalLocation(String localLocation) {
        this.localLocation = localLocation;
    }

    /**
     * Set the remote url location
     *
     * @param remoteLocation
     */
    public void setRemoteLocation(String remoteLocation) {
        this.remoteLocation = remoteLocation;
    }

}
