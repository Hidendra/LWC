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

public class TimeUtil {

    /**
     * Parse a time string (e.g 2 hours)
     *
     * @param time
     * @return the result in seconds
     */
    public static long parseTime(String time) {
        long seconds = 0L;

        String[] split = time.split(" ");

        for (int index = 0; index < split.length; index++) {
            if (index == 0) {
                continue;
            }

            String sub = split[index].toLowerCase();
            int multiplier = 0; // e.g 2 hours = 2
            long unit = 0; // e.g hours = 3600

            try {
                multiplier = Integer.parseInt(split[index - 1]);
            } catch (NumberFormatException e) {
                continue;
            }

            // attempt to match the unit time
            if (sub.startsWith("second")) {
                unit = 1;
            } else if (sub.startsWith("minute")) {
                unit = 60;
            } else if (sub.startsWith("hour")) {
                unit = 3600;
            } else if (sub.startsWith("day")) {
                unit = 86400;
            } else if (sub.startsWith("week")) {
                unit = 604800;
            } else if (sub.startsWith("month")) {
                unit = 2629743;
            } else if (sub.startsWith("year")) {
                unit = 31556926;
            } // i do not think decade, century, millenium, etc is necessary beyond here :3

            seconds += multiplier * unit;
        }

        return seconds;
    }

    /**
     * Convert a given time in seconds to a more readable format
     *
     * @param time
     * @return the time in a more readable format (e.g 2 days 5 hours 1 minute 34  seconds)
     */
    public static String timeToString(long time) {
        String str = "";

        if ((System.currentTimeMillis() / 1000L) - time <= 0) {
            return "Not yet known";
        }

        long days = time / 86400;
        time -= days * 86400;

        long hours = time / 3600;
        time -= hours * 3600;

        long minutes = time / 60;
        time -= minutes * 60;

        long seconds = time;

        if (days > 0) {
            str += days + " day" + (days == 1 ? "" : "s") + " ";
        }

        if (hours > 0) {
            str += hours + " hour" + (hours == 1 ? "" : "s") + " ";
        }

        if (minutes > 0) {
            str += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
        }

        if (seconds > 0) {
            str += seconds + " second" + (seconds == 1 ? "" : "s") + " ";
        }

        if (str.equals("")) {
            return "less than a second";
        }

        return str.trim();
    }

}
