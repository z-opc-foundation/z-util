package com.zifang.util.core.jwt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JWT Claims（自研，对标 JJWT 的 Claims）。
 * <p>
 * 保留 {@code iss/sub/aud/exp/iat/nbf/jti} 这些标准字段（getter），
 * 同时支持任意自定义 key（{@link #put}/{@link #get}）。
 */
public class Claims {

    private final Map<String, Object> data = new LinkedHashMap<>();

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }

    public Claims put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(data);
    }

    public String iss() {
        return (String) data.get("iss");
    }

    public String sub() {
        return (String) data.get("sub");
    }

    public String aud() {
        return (String) data.get("aud");
    }

    public Long exp() {
        return toLong(data.get("exp"));
    }

    public Long iat() {
        return toLong(data.get("iat"));
    }

    public Long nbf() {
        return toLong(data.get("nbf"));
    }

    public String jti() {
        return (String) data.get("jti");
    }

    public Claims iss(String s) {
        data.put("iss", s);
        return this;
    }

    public Claims sub(String s) {
        data.put("sub", s);
        return this;
    }

    public Claims aud(String s) {
        data.put("aud", s);
        return this;
    }

    public Claims exp(long epochSeconds) {
        data.put("exp", epochSeconds);
        return this;
    }

    public Claims iat(long epochSeconds) {
        data.put("iat", epochSeconds);
        return this;
    }

    public Claims nbf(long epochSeconds) {
        data.put("nbf", epochSeconds);
        return this;
    }

    public Claims jti(String s) {
        data.put("jti", s);
        return this;
    }

    /**
     * 在 now 之后多少秒过期。
     */
    public Claims expireIn(long secondsFromNow) {
        return exp(System.currentTimeMillis() / 1000 + secondsFromNow);
    }
}
