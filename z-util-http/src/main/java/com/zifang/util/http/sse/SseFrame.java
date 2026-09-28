package com.zifang.util.http.sse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Server-Sent Events 帧构造与原子写出。
 * <p>
 * SSE 的帧边界是"一个空行"，因此<b>一帧必须落在同一次 write+flush 里</b>：
 * 把 {@code event:} 行和 {@code data:} 行分两次写出，客户端（以及中间代理）
 * 就可能先收到半帧，重排或粘包后整个流就废了。全站现状是三种写法并存——
 * 一次写完的、{@code event:} 与 {@code data:} 分两次写的、以及按 part 类型
 * 交给框架自动分片的。本类把"先在内存里拼成完整字节，再一次 write 一次 flush"
 * 固定下来。
 *
 * <h3>字段顺序</h3>
 * 按 SSE 规范惯例输出 {@code comment -> retry -> id -> event -> data}，
 * 与 z-mcp 现有字节序一致（其帧原子性测试断言
 * {@code :open\nretry:100\nid:1\ndata:\n\n}）。
 *
 * <h3>冒号后的空格</h3>
 * 默认写 {@code data: xxx}（OpenAI/Anthropic 风格，多数自研解析器按
 * {@code startsWith("data: ")} 硬取第 6 个字符之后）。
 * 需要与既有 z-mcp 字节完全一致时改用 {@link #compact()} 切到无空格形式。
 *
 * <h3>换行</h3>
 * 只用 LF。数据值里的 {@code \r} 直接丢弃，{@code \n} 拆成多条 {@code data:} 行
 * （规范如此，客户端用 \n 重新拼接）。
 */
public final class SseFrame {

    private String event;
    private String id;
    private Long retryMillis;
    private String comment;
    private final List<String> dataLines = new ArrayList<String>();
    private boolean spaceAfterColon = true;

    private SseFrame() {
    }

    public static SseFrame create() {
        return new SseFrame();
    }

    /**
     * 一条纯 {@code data:} 帧（最常见的 JSON 增量推送）。
     *
     * @param data 数据体，含换行时自动拆成多条 data 行
     * @return 构造好的帧
     */
    public static SseFrame data(String data) {
        return create().appendData(data);
    }

    /**
     * 事件名（客户端 addEventListener 的键）。
     */
    public SseFrame event(String name) {
        this.event = name;
        return this;
    }

    /**
     * 事件 id，客户端会记住它用于 Last-Event-ID 重连。
     */
    public SseFrame id(String id) {
        this.id = id;
        return this;
    }

    /**
     * 告知客户端重连间隔。
     */
    public SseFrame retry(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("retry millis must not be negative: " + millis);
        }
        this.retryMillis = Long.valueOf(millis);
        return this;
    }

    /**
     * 注释行。以 {@code :} 开头的行是心跳/注释，客户端必须忽略。
     */
    public SseFrame comment(String text) {
        this.comment = text;
        return this;
    }

    /**
     * 追加数据，值中的换行拆成多条 {@code data:} 行。
     */
    public SseFrame appendData(String data) {
        if (data == null) {
            dataLines.add("");
            return this;
        }
        String normalized = data.replace("\r", "");
        int start = 0;
        int nl;
        while ((nl = normalized.indexOf('\n', start)) >= 0) {
            dataLines.add(normalized.substring(start, nl));
            start = nl + 1;
        }
        dataLines.add(normalized.substring(start));
        return this;
    }

    /**
     * 切换为"冒号后不带空格"的紧凑写法。
     */
    public SseFrame compact() {
        this.spaceAfterColon = false;
        return this;
    }

    /**
     * 序列化成完整的一帧文本（以空行结束）。
     *
     * @return 帧文本，至少包含结束空行
     */
    public String wire() {
        StringBuilder sb = new StringBuilder();
        String colon = spaceAfterColon ? ": " : ":";
        if (comment != null) {
            sb.append(':').append(stripNewlines(comment)).append('\n');
        }
        if (retryMillis != null) {
            sb.append("retry").append(colon).append(retryMillis.longValue()).append('\n');
        }
        if (id != null) {
            sb.append("id").append(colon).append(stripNewlines(id)).append('\n');
        }
        if (event != null) {
            sb.append("event").append(colon).append(stripNewlines(event)).append('\n');
        }
        if (!dataLines.isEmpty()) {
            for (String line : dataLines) {
                sb.append("data").append(colon).append(line).append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    /**
     * 原子写出：一次 write 整帧字节 + 一次 flush。
     *
     * @param out 输出流
     * @throws IOException 写失败
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(wire().getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * 原子写出到字符流（调用方需保证该流不会被二次编码）。
     *
     * @param writer 字符流
     * @throws IOException 写失败
     */
    public void writeTo(Writer writer) throws IOException {
        writer.write(wire());
        writer.flush();
    }

    /**
     * 便捷方法：把一段 JSON/文本作为一条 data 帧原子写出。
     *
     * @param out 输出流
     * @param data 数据体
     * @throws IOException 写失败
     */
    public static void writeData(OutputStream out, String data) throws IOException {
        data(data).writeTo(out);
    }

    private static String stripNewlines(String value) {
        return value.replace("\r", "").replace("\n", " ");
    }
}
