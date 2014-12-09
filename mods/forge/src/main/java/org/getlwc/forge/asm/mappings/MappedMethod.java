/**
 * Copyright (c) 2011-2014 Tyler Blair
 * All rights reserved.
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
package org.getlwc.forge.asm.mappings;

public class MappedMethod {

    /**
     * Human name of the method
     */
    private String name;

    /**
     * MCP SRG name of the method
     */
    private String srgName;

    /**
     * MCP SRG signature of the method
     */
    private String srgSignature;

    /**
     * Obfuscated name of the method
     */
    private String obfuscatedName;

    /**
     * Signature of the method (using obf. class names)
     */
    private String obfuscatedSignature;

    public MappedMethod(String name, String srgName, String srgSignature, String obfuscatedName, String obfuscatedSignature) {
        this.name = name;
        this.srgName = srgName;
        this.srgSignature = srgSignature;
        this.obfuscatedName = obfuscatedName;
        this.obfuscatedSignature = obfuscatedSignature;
    }

    @Override
    public String toString() {
        return String.format("Method('%s')", name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappedMethod that = (MappedMethod) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (obfuscatedName != null ? !obfuscatedName.equals(that.obfuscatedName) : that.obfuscatedName != null)
            return false;
        if (obfuscatedSignature != null ? !obfuscatedSignature.equals(that.obfuscatedSignature) : that.obfuscatedSignature != null)
            return false;
        if (srgName != null ? !srgName.equals(that.srgName) : that.srgName != null) return false;
        if (srgSignature != null ? !srgSignature.equals(that.srgSignature) : that.srgSignature != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (srgName != null ? srgName.hashCode() : 0);
        result = 31 * result + (srgSignature != null ? srgSignature.hashCode() : 0);
        result = 31 * result + (obfuscatedName != null ? obfuscatedName.hashCode() : 0);
        result = 31 * result + (obfuscatedSignature != null ? obfuscatedSignature.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getSrgName() {
        return srgName;
    }

    public String getSrgSignature() {
        return srgSignature;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public String getObfuscatedSignature() {
        return obfuscatedSignature;
    }

}
