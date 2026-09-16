package io.zifu.z.serialize.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 写 wire format 头部：magic + version + flags + schemaId + bodyLen。
 *
 * <p><b>跨语言契约：</b>所有语言必须按此顺序写入，且 varint bodyLen
 * 之前的部分为定长 8 字节。详见 {@code docs/wire-format.md}。</p>
 */
public final class Header {

    private Header() {}

    /**
     * 写入头部 + body 长度前缀 + 写入 body（调用方负责 body 内容）。
     * <p>如果开启压缩/加密，本方法负责压缩/加密 body 段。</p>
     */
    public static void writeHeader(OutputStream out, int schemaId, byte flags,
                                   byte[] body) throws IOException {
        // magic
        out.write(Magic.MAGIC_BYTE_0);
        out.write(Magic.MAGIC_BYTE_1);
        // version
        out.write(Magic.WIRE_VERSION);
        // flags
        out.write(flags);
        // schemaId (4B little-endian)
        out.write(schemaId & 0xFF);
        out.write((schemaId >>> 8) & 0xFF);
        out.write((schemaId >>> 16) & 0xFF);
        out.write((schemaId >>> 24) & 0xFF);
        // body length (varint)
        writeVarInt(out, body.length);
        // body
        out.write(body);
    }

    /**
     * 读取头部，返回 (schemaId, flags, bodyLen) 三元组。
     */
    public static HeaderInfo readHeader(InputStream in) throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        if (b0 != Magic.MAGIC_BYTE_0 || b1 != Magic.MAGIC_BYTE_1) {
            throw new IOException("Invalid magic: expected 0xBA 0xBE, got 0x"
                    + Integer.toHexString(b0) + " 0x" + Integer.toHexString(b1));
        }
        int version = in.read();
        if (version != Magic.WIRE_VERSION) {
            throw new IOException("Unsupported wire version: " + version);
        }
        int flags = in.read();
        int s0 = in.read(), s1 = in.read(), s2 = in.read(), s3 = in.read();
        int schemaId = (s0) | (s1 << 8) | (s2 << 16) | (s3 << 24);
        int bodyLen = readVarInt(in);
        return new HeaderInfo(schemaId, (byte) flags, bodyLen);
    }

    private static void writeVarInt(OutputStream out, int v) throws IOException {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    private static int readVarInt(InputStream in) throws IOException {
        int value = 0;
        int shift = 0;
        int b;
        do {
            b = in.read();
            if (b < 0) throw new IOException("Unexpected EOF in bodyLen varint");
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    /** 读取的 header 信息。 */
    public static final class HeaderInfo {
        public final int schemaId;
        public final byte flags;
        public final int bodyLen;

        public HeaderInfo(int schemaId, byte flags, int bodyLen) {
            this.schemaId = schemaId;
            this.flags = flags;
            this.bodyLen = bodyLen;
        }

        public boolean isCompressed() { return (flags & Magic.FLAG_COMPRESSED) != 0; }
        public boolean isEncrypted() { return (flags & Magic.FLAG_ENCRYPTED) != 0; }
        public boolean isIndexed() { return (flags & Magic.FLAG_INDEXED) != 0; }
    }
}
