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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The version class is an immutable object representation of a semantic version (semver.org)
 * <p/>
 * Some (valid) test cases:
 * 1.0.1 > 1.0.0
 * 1.0.0 > 1.0.0-beta2
 * 1.0.0-beta2 > 1.0.0-alpha3
 */
public final class Version implements Comparable {

    /**
     * Release levels that can be used
     */
    public final static String[] releaseLevels = new String[]{
            "release", "alpha", "beta", "rc"
    };

    /**
     * The regex to match the build number
     */
    private final static Pattern REGEX_BUILD_NUMBER = Pattern.compile(".*b(\\d+).*");

    /**
     * The raw version string
     */
    private final String rawVersion;

    /**
     * Major level version
     */
    private int major;

    /**
     * Minor level version
     */
    private int minor;

    /**
     * Patch level version
     */
    private int patch;

    /**
     * Release level weight. 0 is assumed to be greater than non-zero (e.g no -alpha/-beta designation)
     * e.g alpha = 1, beta = 2, rc = 3
     */
    private int releaseLevelWeight;

    /**
     * Release level version, for example where -alpha2, releaseLevelWeight = 1 and releaseLevel = 2
     */
    private int releaseLevel;

    /**
     * If the build number is available, store it as well. Updating schemes such as bleeding will use the build
     * number to know if it should update or not
     */
    private int buildNumber;

    public Version(String version) {
        if (version.contains("-git-")) {
            rawVersion = version.substring(0, version.indexOf("-git-"));
        } else {
            rawVersion = version;
        }

        calculateWeights();
    }

    /**
     * Check if this version is newer than the given version
     *
     * @param version
     * @return
     */
    public boolean newerThan(Version version) {
        return compareTo(version) > 0;
    }

    /**
     * Check i this version is older than the given version
     *
     * @param version
     * @return
     */
    public boolean olderThan(Version version) {
        return compareTo(version) < 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // add the initial version - major.minor.patch
        if (major > 0) {
            builder.append(major);
            builder.append(".");
            builder.append(minor);
            builder.append(".");
            builder.append(patch);
        }

        // are we using a different release level than release?
        if (releaseLevel > 0) {
            String level = releaseLevels[releaseLevel];

            builder.append("-");
            builder.append(level);
            builder.append(releaseLevelWeight);
        }

        // How about a build number ?
        if (buildNumber > 0) {
            if (major > 0) {
                builder.append(" ");
                builder.append("(b");
                builder.append(buildNumber);
                builder.append(")");
            } else {
                builder.append("b");
                builder.append(buildNumber);
            }
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Version)) {
            return false;
        }

        Version version = (Version) o;
        return version.compareTo(this) == 0;
    }

    public int compareTo(Object o) {
        if (!(o instanceof Version)) {
            return 0;
        }

        Version version = (Version) o;
        return compareTo(version);
    }

    public int compareTo(Version o) {
        // Easiest way to know immediately if they are different is if their build number differs!
        if (buildNumber > 0 && o.getBuildNumber() > 0) {
            if (buildNumber > o.getBuildNumber()) {
                return 1;
            } else if (buildNumber < o.getBuildNumber()) {
                return -1;
            }
        }

        // check for major version changes
        if (major > o.getMajor()) {
            return 1;
        } else if (major < o.getMajor()) {
            return -1;
        }

        // major versions are equal.. check minor level
        if (minor > o.getMinor()) {
            return 1;
        } else if (minor < o.getMinor()) {
            return -1;
        }

        // minor versions are equal, too! ..check patch level
        if (patch > o.getPatch()) {
            return 1;
        } else if (patch < o.getPatch()) {
            return -1;
        }

        // should we take into account release level?
        if (releaseLevel != 0 || o.getReleaseLevel() != 0) {
            // check for release status
            if (releaseLevel == 0 || o.getReleaseLevel() == 0) {
                return releaseLevel == 0 ? 1 : -1;
            }

            // they are both an alpha, beta, etc etc, so check for more mature state
            if (releaseLevel > o.getReleaseLevel()) {
                return 1;
            } else if (releaseLevel < o.getReleaseLevel()) {
                return -1;
            }

            // they are both the same release state, ...!
            if (releaseLevelWeight > o.getReleaseLevelWeight()) {
                return 1;
            } else if (releaseLevelWeight < o.getReleaseLevelWeight()) {
                return -1;
            }
        }

        // the two versions are identical! wee
        return 0;
    }

    /**
     * @return
     */
    public int getMajor() {
        return major;
    }

    /**
     * @return
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @return
     */
    public int getPatch() {
        return patch;
    }

    /**
     * @return
     */
    public int getReleaseLevelWeight() {
        return releaseLevelWeight;
    }

    /**
     * @return
     */
    public int getReleaseLevel() {
        return releaseLevel;
    }

    /**
     * @return
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    /**
     * @return
     */
    public String getRawVersion() {
        return rawVersion;
    }

    /**
     * Calculate the version weights
     */
    private void calculateWeights() {
        if (rawVersion.isEmpty()) {
            return;
        }

        // parse the major version
        this.major = parseNumber(rawVersion);

        // parse the minor version
        int minorIndex = rawVersion.indexOf(".") + 1;
        this.minor = parseNumber(rawVersion.substring(minorIndex));

        // parse the patch version, which is optional
        int patchIndex = minorIndex + Integer.toString(this.minor).length() + 1;

        if (patchIndex < rawVersion.length()) {
            this.patch = parseNumber(rawVersion.substring(patchIndex));
        } else {
            this.patch = 0;
        }

        // search for release level
        for (int index = 0; index < releaseLevels.length; index++) {
            String level = releaseLevels[index];

            if (!rawVersion.contains(level)) {
                continue;
            }

            // literally translate the level to the corresponding index
            this.releaseLevel = index;

            // check for a weighting (e.g version) after it (e.g alpha2)
            int levelIndex = rawVersion.indexOf(level) + level.length();

            if (levelIndex < rawVersion.length()) {
                this.releaseLevelWeight = parseNumber(rawVersion.substring(levelIndex));
            } else {
                this.releaseLevelWeight = 1;
            }

            break;
        }

        // search for build number
        Matcher matcher = REGEX_BUILD_NUMBER.matcher(rawVersion);
        if (matcher.matches()) {
            this.buildNumber = Integer.parseInt(matcher.group(1));
        }
    }

    /**
     * Pull a number from the front of the given string
     *
     * @param str
     * @return
     */
    private int parseNumber(String str) {
        if (str.isEmpty()) {
            return 0;
        }

        // is it actually a number?
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
        }

        int found = 0;
        int index = 1;

        while (index < str.length()) {
            String peek = str.substring(0, index);

            try {
                found = Integer.parseInt(peek);
                index++;
            } catch (NumberFormatException e) {
                return found;
            }
        }

        return found;
    }

}
