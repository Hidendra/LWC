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

import java.util.HashMap;
import java.util.Map;

public class Colors {
    public static final String Black = "\u00A70";

    public static final String Blue = "\u00A73";
    public static final String DarkPurple = "\u00A79";
    public static final String Gold = "\u00A76";
    public static final String Gray = "\u00A78";
    public static final String Green = "\u00A72";
    public static final String LightBlue = "\u00A7b";
    public static final String LightGray = "\u00A77";
    public static final String LightGreen = "\u00A7a";
    public static final String LightPurple = "\u00A7d";
    // contains colors for locales
    public static final Map<String, String> localeColors = new HashMap<String, String>();
    public static final String Navy = "\u00A71";
    public static final String Purple = "\u00A75";
    public static final String Red = "\u00A74";
    public static final String Rose = "\u00A7c";
    public static final String White = "\u00A7f";
    public static final String Yellow = "\u00A7e";


    public static final String Bold = "\u00A7l";
    public static final String Strikethrough = "\u00A7m";
    public static final String Italic = "\u00A7o";
    public static final String Underline = "\u00A7n";
    public static final String Reset = "\u00A7r";

    static {
        localeColors.put("%black%", "\u00A70");
        localeColors.put("%navy%", "\u00A71");
        localeColors.put("%green%", "\u00A72");
        localeColors.put("%blue%", "\u00A73");
        localeColors.put("%red%", "\u00A74");
        localeColors.put("%purple%", "\u00A75");
        localeColors.put("%gold%", "\u00A76");
        localeColors.put("%lightgray%", "\u00A77");
        localeColors.put("%gray%", "\u00A78");
        localeColors.put("%darkpurple%", "\u00A79");
        localeColors.put("%lightgreen%", "\u00A7a");
        localeColors.put("%lightblue%", "\u00A7b");
        localeColors.put("%rose%", "\u00A7c");
        localeColors.put("%lightpurple%", "\u00A7d");
        localeColors.put("%yellow%", "\u00A7e");
        localeColors.put("%white%", "\u00A7f");
    }

}
