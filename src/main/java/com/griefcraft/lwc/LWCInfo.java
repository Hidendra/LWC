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

package com.griefcraft.lwc;

import com.griefcraft.util.Version;

/**
 * Temporary, just need to get version info, etc into a packaged class
 */
public class LWCInfo {

    /**
     * Full LWC version
     */
    public static Version FULL_VERSION;

    /**
     * LWC's version.
     * <p/>
     * Initialized to bogus value, but it will be set properly once the plugin starts up based
     * on the version listed in plugin.xml.
     */
    public static double VERSION;

    /**
     * Rather than managing the version in multiple spots, I added this method which will be
     * invoked from Plugin startup to set the version, which is pulled from the plugin.xml file.
     *
     * @param version
     * @author morganm
     */
    public static void setVersion(String version) {
        String implementationVersion = LWCPlugin.class.getPackage().getImplementationVersion();

        // if it's not a manual build, prepend a b
        if(!implementationVersion.equals("MANUAL")) {
            implementationVersion = "b" + implementationVersion;
        }

        FULL_VERSION = new Version(version + " " + implementationVersion);
    }
}
