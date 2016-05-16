/* 
 * The MIT License
 *
 * Copyright 2016 Jan Zípek <jan at zipek.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.zipek.minicloud.api.upload;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is needed to correctly use Cipher streams
 * @author Jan Zípek <jan at zipek.cz>
 */
public class NotClosingOutputStream extends OutputStream {

    /** The underlying output stream. */
    private final OutputStream out;

    /**
     * Creates a new output stream that does not close the given output stream on a call to {@link #close()}.
     * 
     * @param out
     *            the output stream
     */
    public NotClosingOutputStream(final OutputStream out) {
        this.out = out;
    }

    /*
     * DELEGATION TO OUTPUT STREAM
     */

    @Override
    public void close() throws IOException {
        // do nothing here, since we don't want to close the underlying input stream
    }

    @Override
    public void write(final int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }
	
}
