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
package org.getlwc.util;

import java.io.IOException;
import java.io.Writer;

/**
 * Writes JSON prettily
 */
public class JSONPrettyWriter extends Writer {

    private Writer writer;
    private int indent = 0;

    public JSONPrettyWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(int c) throws IOException {
        char chr = (char) c;

        if (chr == '[' || chr == '{') {
            writer.write(c);
            writer.write('\n');
            indent++;
            writeIndentation();
        } else if (chr == ',') {
            writer.write(c);
            writer.write('\n');
            writeIndentation();
        } else if (chr == ']' || chr == '}') {
            writer.write('\n');
            indent--;
            writeIndentation();
            writer.write(c);
        } else {
            writer.write(c);
        }
    }

    @Override
    public void write(char[] buffer, int off, int len) throws IOException {
        for (int i = off; i < off + len; i ++) {
            write(buffer[i]);
        }
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Writes the indention level
     */
    private void writeIndentation() throws IOException {
        for (int i = 0; i < indent; i++) {
            writer.write("   ");
        }
    }

}
