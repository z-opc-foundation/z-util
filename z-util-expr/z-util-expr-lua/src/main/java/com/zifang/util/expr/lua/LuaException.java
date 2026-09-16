package com.zifang.util.expr.lua;

/**
 * Lua 脚本执行异常，当 Lua 脚本执行错误时抛出。
 *
 * @author zifang
 */
public class LuaException extends RuntimeException {

    public LuaException(String message) {
        super(message);
    }

    public LuaException(String message, Throwable cause) {
        super(message, cause);
    }
}
