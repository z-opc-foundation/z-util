package com.zifang.util.http.sse;

/**
 * 一条解析出来的 SSE 事件。
 * <p>
 * 字段按规范原样保留：多行 {@code data:} 已用 \n 拼好；
 * 没有 {@code event:} 时 {@link #event()} 返回默认名 {@code message}。
 */
public final class SseEvent {

    /** 规范规定的默认事件名。 */
    public static final String DEFAULT_EVENT = "message";

    /** OpenAI 风格流结束标记。 */
    public static final String DONE = "[DONE]";

    private final String event;
    private final String data;
    private final String id;
    private final Long retryMillis;

    SseEvent(String event, String data, String id, Long retryMillis) {
        this.event = event == null || event.isEmpty() ? DEFAULT_EVENT : event;
        this.data = data == null ? "" : data;
        this.id = id;
        this.retryMillis = retryMillis;
    }

    public String event() {
        return event;
    }

    public String data() {
        return data;
    }

    /**
     * 事件 id，无 {@code id:} 行时返回 null。
     */
    public String id() {
        return id;
    }

    /**
     * 本帧携带的重连间隔（毫秒），无 {@code retry:} 行时返回 null。
     */
    public Long retryMillis() {
        return retryMillis;
    }

    /**
     * 是否是流结束标记（{@code data: [DONE]}）。
     */
    public boolean isDone() {
        return DONE.equals(data.trim());
    }

    @Override
    public String toString() {
        return "SseEvent{event=" + event + ", id=" + id + ", retry=" + retryMillis
                + ", data=" + data + '}';
    }
}
