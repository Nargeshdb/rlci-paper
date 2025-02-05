/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.jute.BinaryInputArchive;
import org.apache.jute.Record;

import org.checkerframework.checker.mustcall.qual.MustCall;

@SuppressWarnings("mustcall:inconsistent.mustcall.subtype") // count-suppressions-ignore not a JDK class: (Also this @MustCall({}) annotation is changing the specification for this class, specifically)
@MustCall({})
public class ByteBufferInputStream extends InputStream {

    ByteBuffer bb;

    @SuppressWarnings("mustcall:super.invocation") // count-suppressions-ignore not a JDK class:  (Also, this input stream doesn't control a resource.) (validated)
    public ByteBufferInputStream(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public int read() throws IOException {
        if (bb.remaining() == 0) {
            return -1;
        }
        return bb.get() & 0xff;
    }

    @Override
    public int available() throws IOException {
        return bb.remaining();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bb.remaining() == 0) {
            return -1;
        }
        if (len > bb.remaining()) {
            len = bb.remaining();
        }
        bb.get(b, off, len);
        return len;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0L) {
            return 0;
        }
        n = Math.min(n, bb.remaining());
        bb.position(bb.position() + (int) n);
        return n;
    }

    public static void byteBuffer2Record(ByteBuffer bb, Record record) throws IOException {
        BinaryInputArchive ia;
        ia = BinaryInputArchive.getArchive(new ByteBufferInputStream(bb));
        record.deserialize(ia, "request");
    }

}
