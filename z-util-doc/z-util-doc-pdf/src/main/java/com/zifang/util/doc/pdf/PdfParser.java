package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocParseException;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 纯 Java PDF 文件解析：经典 xref 表 + xref 流（1.5+）+ 对象流 + Prev 链。
 * 产出惰性对象图（PdfRef 不主动解，取用时 resolve）。
 */
public final class PdfParser implements PdfDict.RefResolver {

    private final byte[] data;
    private final Map<Integer, long[]> offsets = new HashMap<>();
    private final Map<Integer, Object> cache = new HashMap<>();
    private final Map<Integer, Map<Integer, Object>> objectStreams = new HashMap<>();
    private PdfDict trailer;

    public PdfParser(byte[] data) {
        this.data = data;
    }

    /** 解析文件骨架，返回 trailer 字典（/Root /Info 等）。加密文件直接抛错。 */
    public PdfDict parse() {
        int startxref = findStartxref();
        long xrefPos = startxref;
        int guard = 0;
        while (xrefPos > 0 && guard++ < 64) {
            xrefPos = parseXrefSection(xrefPos);
        }
        PdfDict encrypt = getTrailerDict("Encrypt");
        if (encrypt != null) {
            throw new DocParseException("encrypted PDF is not supported");
        }
        return trailer;
    }

    private PdfDict getTrailerDict(String key) {
        if (trailer == null) {
            return null;
        }
        Object v = trailer.getRaw(key);
        return v instanceof PdfRef r ? asDict(resolve(r)) : v instanceof PdfDict d ? d : null;
    }

    public Object resolve(PdfRef ref) {
        return getObject(ref.obj);
    }

    /** 取对象；流对象返回 PdfStreamObj（已解 filter）。 */
    public Object getObject(int num) {
        if (cache.containsKey(num)) {
            return cache.get(num);
        }
        Object result = null;
        long[] entry = offsets.get(num);
        if (entry != null) {
            int type = (int) entry[0];
            long loc = entry[1];
            if (type == 1) {
                result = parseIndirectAt((int) loc);
            } else if (type == 2) {
                result = fromObjectStream((int) loc, (int) entry[2], num);
            }
        }
        cache.put(num, result);
        return result;
    }

    public PdfDict getDict(int num) {
        return asDict(getObject(num));
    }

    public PdfStreamObj getStream(int num) {
        return getObject(num) instanceof PdfStreamObj s ? s : null;
    }

    /** 解引用便捷方法（dict/array 内的 ref 一并展开一层）。 */
    public PdfDict dict(Object v) {
        return asDict(unref(v));
    }

    public Object unref(Object v) {
        while (v instanceof PdfRef r) {
            v = getObject(r.obj);
        }
        return v;
    }

    public byte[] streamData(PdfStreamObj stream) {
        PdfDict.RefResolver self = this;
        return PdfFilters.decode(stream.dict, stream.data, self);
    }

    private static PdfDict asDict(Object v) {
        return v instanceof PdfDict d ? d : null;
    }

    // ==================== 文件骨架 ====================

    private int findStartxref() {
        String tail = new String(data, Math.max(0, data.length - 2048),
                Math.min(2048, data.length), StandardCharsets.ISO_8859_1);
        int idx = tail.lastIndexOf("startxref");
        if (idx < 0) {
            throw new DocParseException("startxref not found: not a PDF file");
        }
        int p = idx + "startxref".length();
        return Integer.parseInt(readInt(tail, p).trim());
    }

    private static String readInt(String s, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (!sb.isEmpty() || !Character.isWhitespace(c)) {
                break;
            }
        }
        return sb.toString();
    }

    /** 解析一段 xref（经典表或 xref 流），返回 /Prev 位置；无 Prev 返回 -1。 */
    private long parseXrefSection(long pos) {
        if (matchKeyword(pos, "xref")) {
            return parseClassicXref((int) (pos + 4));
        }
        // xref 流：形如 "N 0 obj <<...>> stream ... endstream"
        Object obj = parseIndirectAt((int) pos);
        if (obj instanceof PdfStreamObj stream) {
            return parseXrefStream(stream);
        }
        throw new DocParseException("bad xref at offset " + pos);
    }

    private boolean matchKeyword(long pos, String kw) {
        if (pos + kw.length() > data.length) {
            return false;
        }
        for (int i = 0; i < kw.length(); i++) {
            if (data[(int) pos + i] != kw.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private long parseClassicXref(int pos) {
        Reader r = new Reader(data, pos);
        while (true) {
            r.skipWs();
            if (r.peek() == 't' && r.matchFrom("trailer")) {
                r.skip(7);
                break;
            }
            long start = r.readInt();
            long count = r.readInt();
            for (long i = 0; i < count; i++) {
                long offset = r.readInt();
                r.readInt(); // gen
                char type = r.readTypeChar();
                if (type == 'n' && !offsets.containsKey((int) (start + i))) {
                    offsets.put((int) (start + i), new long[]{1, offset});
                }
            }
        }
        r.skipWs();
        mergeTrailer(r.readDictOrThrow());
        return prevPos(trailer);
    }

    private long parseXrefStream(PdfStreamObj stream) {
        byte[] raw = PdfFilters.decode(stream.dict, stream.data, this);
        int w0 = wField(stream.dict, 0);
        int w1 = wField(stream.dict, 1);
        int w2 = wField(stream.dict, 2);
        int entryLen = w0 + w1 + w2;
        long size = (long) stream.dict.getNumberOr("Size", 0);
        List<Object> indexArr = stream.dict.getArray("Index");
        List<long[]> ranges = new ArrayList<>();
        if (indexArr != null && indexArr.size() % 2 == 0) {
            for (int i = 0; i < indexArr.size(); i += 2) {
                ranges.add(new long[]{num(indexArr.get(i)), num(indexArr.get(i + 1))});
            }
        } else {
            ranges.add(new long[]{0, size});
        }
        int p = 0;
        for (long[] range : ranges) {
            for (long i = 0; i < range[1]; i++) {
                if (p + entryLen > raw.length) {
                    break;
                }
                int type = w0 == 0 ? 1 : intAt(raw, p, w0);
                long f2 = intAt(raw, p + w0, w1);
                long f3 = intAt(raw, p + w0 + w1, w2);
                p += entryLen;
                int objNum = (int) (range[0] + i);
                if (!offsets.containsKey(objNum)) {
                    if (type == 1) {
                        offsets.put(objNum, new long[]{1, f2});
                    } else if (type == 2) {
                        offsets.put(objNum, new long[]{2, f2, f3});
                    }
                }
            }
        }
        mergeTrailer(stream.dict);
        return prevPos(stream.dict);
    }

    private static int wField(PdfDict dict, int i) {
        List<Object> w = dict.getArray("W");
        if (w == null || i >= w.size()) {
            throw new DocParseException("bad /W in xref stream");
        }
        return (int) num(w.get(i));
    }

    private static long num(Object o) {
        return o instanceof Number n ? n.longValue() : 0;
    }

    private static int intAt(byte[] raw, int pos, int len) {
        int v = 0;
        for (int i = 0; i < len; i++) {
            if (pos + i < raw.length) {
                v = (v << 8) | (raw[pos + i] & 0xFF);
            }
        }
        return v;
    }

    private void mergeTrailer(PdfDict dict) {
        if (trailer == null) {
            trailer = dict;
        } else {
            for (Map.Entry<PdfName, Object> e : dict.asMap().entrySet()) {
                if (e.getKey().name.equals("Prev")) {
                    continue;
                }
                trailer.asMap().putIfAbsent(e.getKey(), e.getValue());
            }
        }
    }

    private long prevPos(PdfDict dict) {
        Double prev = dict.getNumber("Prev");
        return prev == null ? -1 : prev.longValue();
    }

    // ==================== 对象解析 ====================

    private Object parseIndirectAt(int offset) {
        Reader r = new Reader(data, offset);
        r.skipWs();
        r.readInt();
        r.readInt();
        r.skipWs();
        if (!r.matchFrom("obj")) {
            return null;
        }
        r.skip(3);
        r.skipWs();
        Object value = r.readValue();
        r.skipWs();
        if (r.matchFrom("stream")) {
            r.skip(6);
            // stream 后必须是 EOL（CRLF/LF），跳过它才到数据
            if (r.peek() == '\r') {
                r.skip(1);
            }
            if (r.peek() == '\n') {
                r.skip(1);
            }
            return readStreamBody(r, value);
        }
        return value;
    }

    private Object readStreamBody(Reader r, Object dictObj) {
        if (!(dictObj instanceof PdfDict dict)) {
            return null;
        }
        int length = -1;
        Object lenV = dict.getRaw("Length");
        if (lenV instanceof Number n) {
            length = n.intValue();
        } else if (lenV instanceof PdfRef ref) {
            long[] entry = offsets.get(ref.obj);
            if (entry != null && entry[0] == 1) {
                Object o = parseIndirectAt((int) entry[1]);
                if (o instanceof Number n) {
                    length = n.intValue();
                }
            }
        }
        int start = r.pos;
        int end;
        if (length >= 0 && start + length <= data.length) {
            end = start + length;
        } else {
            end = indexOfEndstream(start);
        }
        byte[] raw = new byte[Math.max(0, end - start)];
        System.arraycopy(data, start, raw, 0, raw.length);
        return new PdfStreamObj(dict, raw);
    }

    private int indexOfEndstream(int from) {
        byte[] kw = "endstream".getBytes(StandardCharsets.ISO_8859_1);
        outer:
        for (int i = from; i <= data.length - kw.length; i++) {
            for (int j = 0; j < kw.length; j++) {
                if (data[i + j] != kw[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return data.length;
    }

    private Object fromObjectStream(int streamNum, int indexInStream, int targetNum) {
        Map<Integer, Object> objs = objectStreams.get(streamNum);
        if (objs == null) {
            objs = new HashMap<>();
            objectStreams.put(streamNum, objs);
            PdfStreamObj stream = getStream(streamNum);
            if (stream == null) {
                return null;
            }
            byte[] raw = streamData(stream);
            Reader r = new Reader(raw, 0);
            // 对象流的 /N /First 在外层字典
            PdfDict outer = stream.dict;
            int count = (int) outer.getNumberOr("N", 0);
            int first = (int) outer.getNumberOr("First", 0);
            int[] nums = new int[count];
            int[] poss = new int[count];
            for (int i = 0; i < count; i++) {
                nums[i] = (int) r.readInt();
                r.readInt(); // gen
                poss[i] = (int) r.readInt();
            }
            for (int i = 0; i < count; i++) {
                Reader or = new Reader(raw, first + poss[i]);
                objs.put(nums[i], or.readValue());
            }
        }
        return objs.get(targetNum);
    }

    // ==================== 字节级 Reader ====================

    /** 在全文件或独立字节数组上的 token reader。 */
    static final class Reader {
        private final byte[] buf;
        private int pos;

        Reader(byte[] buf, int pos) {
            this.buf = buf;
            this.pos = pos;
        }

        void skipWs() {
            while (pos < buf.length) {
                char c = (char) (buf[pos] & 0xFF);
                if (c == '%') {
                    while (pos < buf.length && buf[pos] != '\n' && buf[pos] != '\r') {
                        pos++;
                    }
                } else if (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == 0 || c == 12) {
                    pos++;
                } else {
                    return;
                }
            }
        }

        int peek() {
            return pos < buf.length ? buf[pos] & 0xFF : -1;
        }

        void skip(int n) {
            pos += n;
        }

        boolean matchFrom(String kw) {
            if (pos + kw.length() > buf.length) {
                return false;
            }
            for (int i = 0; i < kw.length(); i++) {
                if (buf[pos + i] != kw.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        long readInt() {
            skipWs();
            StringBuilder sb = new StringBuilder();
            while (pos < buf.length) {
                char c = (char) (buf[pos] & 0xFF);
                if (Character.isDigit(c) || (sb.isEmpty() && (c == '-' || c == '+'))) {
                    sb.append(c);
                    pos++;
                } else {
                    break;
                }
            }
            return sb.isEmpty() ? 0 : Long.parseLong(sb.toString());
        }

        char readTypeChar() {
            while (pos < buf.length && Character.isWhitespace(buf[pos] & 0xFF)) {
                pos++;
            }
            return pos < buf.length ? (char) (buf[pos++] & 0xFF) : 'f';
        }

        Object readValue() {
            skipWs();
            int c = peek();
            if (c == '<') {
                if (pos + 1 < buf.length && buf[pos + 1] == '<') {
                    return readDict();
                }
                return readHexString();
            }
            if (c == '[') {
                return readArray();
            }
            if (c == '(') {
                return readLiteralString();
            }
            if (c == '/') {
                return readName();
            }
            return readNumberOrKeyword();
        }

        private PdfDict readDict() {
            PdfDict dict = new PdfDict();
            pos += 2;
            while (true) {
                skipWs();
                if (pos + 1 < buf.length && buf[pos] == '>' && buf[pos + 1] == '>') {
                    pos += 2;
                    return dict;
                }
                if (pos >= buf.length) {
                    return dict;
                }
                if (peek() != '/') {
                    // 非法序列，跳过
                    pos++;
                    continue;
                }
                PdfName key = readName();
                Object value = readValueWithRef();
                dict.put(key, value);
            }
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            while (true) {
                skipWs();
                if (peek() == ']') {
                    pos++;
                    return list;
                }
                if (pos >= buf.length) {
                    return list;
                }
                list.add(readValueWithRef());
            }
        }

        private PdfName readName() {
            pos++;
            StringBuilder sb = new StringBuilder();
            while (pos < buf.length) {
                char c = (char) (buf[pos] & 0xFF);
                if (isDelimiterOrWs(c)) {
                    break;
                }
                if (c == '#' && pos + 2 < buf.length) {
                    sb.append((char) ((hexVal(buf[pos + 1]) << 4) | hexVal(buf[pos + 2])));
                    pos += 3;
                } else {
                    sb.append(c);
                    pos++;
                }
            }
            return new PdfName(sb.toString());
        }

        private byte[] readLiteralString() {
            pos++;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int depth = 1;
            while (pos < buf.length) {
                char c = (char) (buf[pos] & 0xFF);
                if (c == '\\') {
                    pos++;
                    if (pos >= buf.length) {
                        break;
                    }
                    char e = (char) (buf[pos] & 0xFF);
                    switch (e) {
                        case 'n' -> out.write('\n');
                        case 'r' -> out.write('\r');
                        case 't' -> out.write('\t');
                        case 'b' -> out.write('\b');
                        case 'f' -> out.write(12);
                        case '(' -> out.write('(');
                        case ')' -> out.write(')');
                        case '\\' -> out.write('\\');
                        case '\n' -> {
                        }
                        default -> {
                            if (e >= '0' && e <= '7') {
                                int val = 0;
                                int digits = 0;
                                while (pos < buf.length && digits < 3
                                        && (char) (buf[pos] & 0xFF) >= '0' && (char) (buf[pos] & 0xFF) <= '7') {
                                    val = val * 8 + (buf[pos] & 0xF);
                                    pos++;
                                    digits++;
                                }
                                out.write(val & 0xFF);
                                continue;
                            }
                            out.write(e & 0xFF);
                        }
                    }
                    pos++;
                } else if (c == '(') {
                    depth++;
                    out.write('(');
                    pos++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        pos++;
                        return out.toByteArray();
                    }
                    out.write(')');
                    pos++;
                } else {
                    out.write(buf[pos] & 0xFF);
                    pos++;
                }
            }
            return out.toByteArray();
        }

        private byte[] readHexString() {
            pos++;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int hi = -1;
            while (pos < buf.length) {
                char c = (char) (buf[pos] & 0xFF);
                if (c == '>') {
                    pos++;
                    break;
                }
                int v = hexVal((byte) c);
                if (v >= 0) {
                    if (hi < 0) {
                        hi = v;
                    } else {
                        out.write((hi << 4) | v);
                        hi = -1;
                    }
                }
                pos++;
            }
            if (hi >= 0) {
                out.write(hi << 4);
            }
            return out.toByteArray();
        }

        private Object readNumberOrKeyword() {
            int start = pos;
            StringBuilder sb = new StringBuilder();
            while (pos < buf.length) {
                char c = (char) (buf[pos] & 0xFF);
                if (isDelimiterOrWs(c)) {
                    break;
                }
                sb.append(c);
                pos++;
            }
            String token = sb.toString();
            if (token.isEmpty()) {
                pos++;
                return null;
            }
            if (token.equals("true")) {
                return Boolean.TRUE;
            }
            if (token.equals("false") || token.equals("null")) {
                return token.equals("true") ? Boolean.TRUE : null;
            }
            if (token.matches("-?\\d+")) {
                try {
                    long l = Long.parseLong(token);
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return (int) l;
                    }
                    return l;
                } catch (NumberFormatException e) {
                    return token;
                }
            }
            if (token.matches("-?\\d*\\.\\d+([eE][-+]?\\d+)?")) {
                try {
                    return Double.parseDouble(token);
                } catch (NumberFormatException e) {
                    // fallthrough
                }
            }
            // obj/endobj/R 等关键词作为名字返回，调用方按上下文处理
            if (pos == start) {
                return null;
            }
            return new PdfName(token);
        }

        /** 读值后做 "N G R" 间接引用前瞻，未成引用则回退。 */
        Object readValueWithRef() {
            Object v = readValue();
            if (!(v instanceof Integer) && !(v instanceof Long)) {
                return v;
            }
            long first = ((Number) v).longValue();
            int save = pos;
            skipWs();
            StringBuilder sb = new StringBuilder();
            while (pos < buf.length && Character.isDigit((char) (buf[pos] & 0xFF))) {
                sb.append((char) (buf[pos] & 0xFF));
                pos++;
            }
            if (sb.isEmpty()) {
                pos = save;
                return v;
            }
            long second = Long.parseLong(sb.toString());
            skipWs();
            if (matchFrom("R")) {
                boolean delimited = pos + 1 >= buf.length
                        || isDelimiterOrWs((char) (buf[pos + 1] & 0xFF));
                if (delimited) {
                    pos++;
                    return new PdfRef((int) first, (int) second);
                }
            }
            pos = save;
            return v;
        }

        private PdfDict readDictOrThrow() {
            skipWs();
            if (peek() == '<' && pos + 1 < buf.length && buf[pos + 1] == '<') {
                return readDict();
            }
            throw new DocParseException("trailer dict not found");
        }
    }

    static boolean isDelimiterOrWs(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == 0 || c == 12
                || c == '(' || c == ')' || c == '<' || c == '>' || c == '[' || c == ']'
                || c == '{' || c == '}' || c == '/' || c == '%';
    }

    static int hexVal(byte b) {
        char c = (char) (b & 0xFF);
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return 0;
    }
}
