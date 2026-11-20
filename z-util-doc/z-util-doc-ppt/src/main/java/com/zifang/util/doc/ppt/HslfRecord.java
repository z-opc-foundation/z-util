package com.zifang.util.doc.ppt;

import java.util.List;
import java.util.function.Consumer;

/**
 * HSLF 记录节点。
 *
 * <p>记录头 8 字节：2 字节 ver/inst（低 4 位是版本号，高 12 位是实例号）、
 * 2 字节记录类型、4 字节负载长度（均为小端）。版本号 0xF 表示容器记录，
 * 负载区里是子记录。</p>
 */
final class HslfRecord {

    static final int TYPE_DOCUMENT = 0x03E8;
    static final int TYPE_SLIDE = 0x03EE;
    static final int TYPE_SLIDE_ATOM = 0x03EF;
    static final int TYPE_NOTES = 0x03F0;
    static final int TYPE_NOTES_ATOM = 0x03F1;
    static final int TYPE_SLIDE_PERSIST_ATOM = 0x03F3;
    static final int TYPE_SLIDE_LIST_WITH_TEXT = 0x0FF0;
    static final int TYPE_PERSIST_PTR = 6002;
    static final int TYPE_USER_EDIT = 0x0FF5;

    static final int TYPE_TEXT_HEADER = 0x0F9F;
    static final int TYPE_TEXT_CHARS = 0x0FA0;
    static final int TYPE_TEXT_BYTES = 0x0FA8;
    static final int TYPE_TEXT_INFO = 0xF00D;

    /** 占位符类型：标题（TextHeaderAtom 的 textType）。 */
    static final int PLACEHOLDER_TITLE = 0;
    static final int PLACEHOLDER_CENTER_TITLE = 6;

    final int type;
    final int version;
    final int instance;
    /** 记录在流中的绝对偏移，用于和持久化目录对齐。 */
    final int offset;
    /** 负载长度（容器记录是子记录区总长）。 */
    final int length;
    final byte[] data;
    final List<HslfRecord> children;

    HslfRecord(int type, int version, int instance, int offset, int length,
               byte[] data, List<HslfRecord> children) {
        this.type = type;
        this.version = version;
        this.instance = instance;
        this.offset = offset;
        this.length = length;
        this.data = data;
        this.children = children;
    }

    boolean isContainer() {
        return version == 0x0F;
    }

    /** 记录结束位置（下一条记录的起点）。 */
    int endOffset() {
        return offset + 8 + length;
    }

    /** 后序遍历自身与全部后代。 */
    void walk(Consumer<HslfRecord> visitor) {
        visitor.accept(this);
        for (HslfRecord child : children) {
            child.walk(visitor);
        }
    }

    static int uint16(byte[] d, int offset) {
        return (d[offset] & 0xFF) | ((d[offset + 1] & 0xFF) << 8);
    }

    static int int32(byte[] d, int offset) {
        return (d[offset] & 0xFF) | ((d[offset + 1] & 0xFF) << 8)
                | ((d[offset + 2] & 0xFF) << 16) | ((d[offset + 3] & 0xFF) << 24);
    }
}
