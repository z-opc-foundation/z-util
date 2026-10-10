package com.zifang.util.http.sse;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 容错的增量 SSE 解析器：把任意切分的字节块喂进去，吐出已经完整的帧。
 * <p>
 * 全站现有解析写法都缺一块：按 {@code split("\r?\n")} 整本正文切的（无法处理半帧）、
 * 只认 {@code data: } 前缀硬取第 6 位之后的（丢 {@code event:}/{@code id:}/{@code retry:}
 * 和注释行）、或者 {@code trim()} 掉数据首尾空白的。本类按规范处理：
 *
 * <ul>
 *   <li>字节进、事件出，不要求调用方凑齐整帧；断在半个 UTF-8 字符上也安全
 *       ——先按 LF 切行再解码整行，UTF-8 的多字节序列里不会出现 0x0A</li>
 *   <li>冒号后恰好一个空格被吃掉（规范），其余首尾空白保留（不 trim 数据）</li>
 *   <li>多行 {@code data:} 用 \n 拼回</li>
 *   <li>以 {@code :} 开头的注释/心跳行忽略，不构成事件</li>
 *   <li>{@code id:}/{@code retry:} 按规范在<b>解析到该行时</b>即写入流状态，
 *       因此即使该帧没有派发事件，{@link #lastEventId()}/{@link #lastRetryMillis()} 也已更新</li>
 *   <li>未知字段忽略；无冒号的行按"字段名 + 空值"处理</li>
 *   <li>数据缓冲为空时<b>不发事件</b>（规范如此），因此 {@code data:} 空帧与
 *       只带 {@code id:}/{@code retry:} 的帧都只更新状态，不会产生回调</li>
 *   <li>流结束时 {@link #finish()} 会把没有以空行收尾的残帧补发出来</li>
 * </ul>
 *
 * <h3>线程模型</h3>
 * 有状态且非线程安全，一条连接一个实例。
 */
public final class SseParser {

    private final ByteArrayOutputStream partialLine = new ByteArrayOutputStream();
    private final StringBuilder dataBuffer = new StringBuilder();

    private String eventName;
    private String lastEventId;
    private Long lastRetryMillis;
    private boolean dataDirty;

    /**
     * 喂入一段字节（可以是半帧、可以是任意切分点）。
     *
     * @param chunk 字节块，null 或空时返回空列表
     * @return 本次调用凑齐的事件列表，按出现顺序
     */
    public List<SseEvent> feed(byte[] chunk) {
        if (chunk == null || chunk.length == 0) {
            return new ArrayList<SseEvent>();
        }
        return feed(chunk, 0, chunk.length);
    }

    /**
     * 喂入字节数组的一段。
     *
     * @param chunk 字节数组
     * @param off   起始偏移
     * @param len   长度
     * @return 本次调用凑齐的事件列表
     */
    public List<SseEvent> feed(byte[] chunk, int off, int len) {
        List<SseEvent> events = new ArrayList<SseEvent>();
        int end = off + len;
        for (int i = off; i < end; i++) {
            byte b = chunk[i];
            if (b == '\r') {
                continue;
            }
            if (b == '\n') {
                byte[] lineBytes = partialLine.toByteArray();
                partialLine.reset();
                handleLine(new String(lineBytes, StandardCharsets.UTF_8), events);
            } else {
                partialLine.write(b);
            }
        }
        return events;
    }

    /**
     * 喂入一段已解码的文本（等价于按 UTF-8 取字节后 {@link #feed(byte[])}）。
     *
     * @param chunk 文本块
     * @return 本次调用凑齐的事件列表
     */
    public List<SseEvent> feed(String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return new ArrayList<SseEvent>();
        }
        return feed(chunk.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 流结束：补发未以空行收尾的残帧，并丢弃未凑齐的行。
     *
     * @return 补发出的事件列表
     */
    public List<SseEvent> finish() {
        List<SseEvent> events = new ArrayList<SseEvent>();
        if (partialLine.size() > 0) {
            byte[] lineBytes = partialLine.toByteArray();
            partialLine.reset();
            handleLine(new String(lineBytes, StandardCharsets.UTF_8), events);
        }
        if (dataBuffer.length() > 0) {
            events.add(dispatch());
        }
        reset();
        return events;
    }

    /**
     * 丢弃当前缓冲，复用实例解析另一条流。
     */
    public void reset() {
        partialLine.reset();
        dataBuffer.setLength(0);
        eventName = null;
        dataDirty = false;
    }

    /**
     * 一次性解析整本正文（适合已经把响应体全量读进内存的传输层）。
     *
     * @param body 完整正文
     * @return 全部事件，按出现顺序
     */
    public static List<SseEvent> parse(String body) {
        SseParser parser = new SseParser();
        List<SseEvent> events = parser.feed(body);
        events.addAll(parser.finish());
        return events;
    }

    /**
     * 最近一帧的 {@code id:} 值（客户端重连时要回带 Last-Event-ID）。
     */
    public String lastEventId() {
        return lastEventId;
    }

    /**
     * 最近一帧的 {@code retry:} 值（毫秒），从未出现时为 null。
     */
    public Long lastRetryMillis() {
        return lastRetryMillis;
    }

    private void handleLine(String line, List<SseEvent> events) {
        if (line.isEmpty()) {
            if (dataBuffer.length() > 0) {
                events.add(dispatch());
            } else {
                reset();
            }
            return;
        }
        if (line.charAt(0) == ':') {
            return;
        }
        int colon = line.indexOf(':');
        String field;
        String value;
        if (colon < 0) {
            field = line;
            value = "";
        } else {
            field = line.substring(0, colon);
            value = line.substring(colon + 1);
            if (value.startsWith(" ")) {
                value = value.substring(1);
            }
        }
        if ("data".equals(field)) {
            if (dataDirty) {
                dataBuffer.append('\n');
            }
            dataBuffer.append(value);
            dataDirty = true;
        } else if ("event".equals(field)) {
            eventName = value;
        } else if ("id".equals(field)) {
            lastEventId = value;
        } else if ("retry".equals(field)) {
            Long millis = parseDigits(value);
            if (millis != null) {
                lastRetryMillis = millis;
            }
        }
    }

    private SseEvent dispatch() {
        SseEvent event = new SseEvent(eventName, dataBuffer.toString(), lastEventId, lastRetryMillis);
        reset();
        return event;
    }

    private static Long parseDigits(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return null;
            }
        }
        try {
            return Long.valueOf(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
