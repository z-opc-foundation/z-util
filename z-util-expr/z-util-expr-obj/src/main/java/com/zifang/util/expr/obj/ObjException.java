package com.zifang.util.expr.obj;

/**
 * 对象整形语言的语义错误（未知步骤、槽位缺失、聚合表达式不合法等）。
 * <p>
 * 报错一律带槽位名与实际值：spec 多数由 AI 生成，静默兜底成 null 会把错误推到渲染阶段。
 *
 * @author zifang
 */
public class ObjException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ObjException(String message) {
        super(message);
    }

    public ObjException(String message, Throwable cause) {
        super(message, cause);
    }
}
