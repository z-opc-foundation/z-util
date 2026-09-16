package com.zifang.util.core.jwt;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自研轻量 JWT 工具（无 JJWT / nimbus-jose-jwt 依赖）。
 * <p>
 * 支持 HS256 / HS512；只解析/序列化三段式 token，不做高级特性。
 * <p>
 * 用法：
 * <pre>{@code
 *   String token = Jwt.builder()
 *       .algorithm(Jwt.HS256)
 *       .secret("my-secret")
 *       .claims(new Claims().sub("alice").expireIn(3600))
 *       .build();
 *
 *   Claims claims = Jwt.parser()
 *       .algorithm(Jwt.HS256)
 *       .secret("my-secret")
 *       .parse(token);
 * }</pre>
 */
public final class Jwt {

    /**
     * 算法标识常量（静态使用）。
     */
    public static final SigningAlgorithm HS256 = HmacSha256.INSTANCE;
    public static final SigningAlgorithm HS512 = HmacSha512.INSTANCE;

    private Jwt() {
    }

    // ===== Builder =====

    public static Builder builder() {
        return new Builder();
    }

    public static Parser parser() {
        return new Parser();
    }

    // ===== Parser =====

    static String toJson(Object o) {
        if (o == null) return "null";
        if (o instanceof Boolean || o instanceof Number) return o.toString();
        if (o instanceof CharSequence) return "\"" + escape(o.toString()) + "\"";
        if (o instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(e.getKey()))).append("\":");
                sb.append(toJson(e.getValue()));
            }
            return sb.append("}").toString();
        }
        if (o instanceof Iterable) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Iterable<?>) o) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toJson(item));
            }
            return sb.append("]").toString();
        }
        return "\"" + escape(o.toString()) + "\"";
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 4);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u005Cu%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    // ===== 极简 JSON 序列化（仅支持 JWT 场景：string/number/boolean/null） =====
    // 不引 jackson/gson/fastjson 这种三方，纯 JDK 实现。

    @SuppressWarnings("unchecked")
    static Map<String, Object> fromJson(byte[] bytes) {
        return (Map<String, Object>) JsonParser.parse(new String(bytes, StandardCharsets.UTF_8));
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }

    public static class Builder {
        private SigningAlgorithm algorithm = HS256;
        private String secret;
        private Claims claims = new Claims();

        public Builder algorithm(SigningAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder claims(Claims claims) {
            this.claims = claims;
            return this;
        }

        public String build() {
            if (algorithm == null || secret == null || claims == null) {
                throw new IllegalStateException("algorithm/secret/claims must be set");
            }
            try {
                Map<String, Object> header = new LinkedHashMap<>();
                header.put("alg", algorithm.name());
                header.put("typ", "JWT");

                String headerB64 = Base64Url.encode(toJson(header));
                String payloadB64 = Base64Url.encode(toJson(claims.asMap()));
                String signingInput = headerB64 + "." + payloadB64;

                byte[] sig = algorithm.sign(signingInput.getBytes(StandardCharsets.UTF_8), secret);
                return signingInput + "." + Base64Url.encode(sig);
            } catch (GeneralSecurityException e) {
                throw new JwtException("sign failed", e);
            }
        }
    }

    public static class Parser {
        private SigningAlgorithm algorithm = HS256;
        private String secret;
        private long leewaySeconds = 0;
        private boolean requireExp = false;

        public Parser algorithm(SigningAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        public Parser secret(String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * 校验 exp/nbf 时允许的容差（应对时钟偏移）。
         */
        public Parser leeway(long seconds) {
            this.leewaySeconds = seconds;
            return this;
        }

        public Parser requireExp() {
            this.requireExp = true;
            return this;
        }

        public Claims parse(String token) {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new JwtException("malformed JWT, expected 3 parts, got " + parts.length);
            try {
                // 1) 验证签名
                String signingInput = parts[0] + "." + parts[1];
                byte[] sig = Base64Url.decode(parts[2]);
                if (secret == null) throw new JwtException("secret not set");
                if (!algorithm.verify(signingInput.getBytes(StandardCharsets.UTF_8), sig, secret)) {
                    throw new JwtException("signature mismatch");
                }
                // 2) 解析 header（验证 alg 匹配）
                Map<String, Object> header = fromJson(Base64Url.decode(parts[0]));
                String alg = (String) header.get("alg");
                if (alg != null && !alg.equalsIgnoreCase(algorithm.name())) {
                    throw new JwtException("alg mismatch: token=" + alg + " parser=" + algorithm.name());
                }
                // 3) 解析 claims
                Map<String, Object> data = fromJson(Base64Url.decode(parts[1]));
                long now = System.currentTimeMillis() / 1000;
                Long exp = toLong(data.get("exp"));
                if (exp != null && now - leewaySeconds > exp) {
                    throw new JwtException("token expired at " + exp);
                }
                Long nbf = toLong(data.get("nbf"));
                if (nbf != null && now + leewaySeconds < nbf) {
                    throw new JwtException("token not yet valid (nbf=" + nbf + ")");
                }
                if (requireExp && exp == null) {
                    throw new JwtException("exp claim required but missing");
                }
                Claims c = new Claims();
                data.forEach(c::put);
                return c;
            } catch (JwtException e) {
                throw e;
            } catch (Exception e) {
                throw new JwtException("parse failed", e);
            }
        }
    }
}
