package com.zifang.util.cache;

import com.zifang.util.cache.Cache;
import com.zifang.util.cache.CacheBuilder;
import com.zifang.util.core.jwt.Claims;
import com.zifang.util.core.jwt.Jwt;
import com.zifang.util.core.jwt.JwtException;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * 端到端场景 3：JWT 鉴权完整流程。
 * <p>
 * 模拟 z-opc z-ctc-audit 的真实使用：
 * <ul>
 *   <li>登录：签发 token</li>
 *   <li>访问 API：每次校验签名 + 过期</li>
 *   <li>注销：把 token 加入黑名单（短期缓存 7 天）</li>
 *   <li>刷新：过期前用 refresh_token 换新 access_token</li>
 * </ul>
 * <p>
 * 之前 z-ctc-audit 用 {@code io.jsonwebtoken}，现在用 z-util-jwt 自研。
 */
public class JwtAuthIT {

    @Test
    public void testFullJwtLifecycle() {
        AuthService auth = new AuthService();
        Map<String, String> tokens = auth.login("alice", "admin");
        String access = tokens.get("access");
        String refresh = tokens.get("refresh");

        // 1) 验证
        Claims claims = auth.verify(access);
        assertEquals("alice", claims.sub());
        assertEquals("admin", claims.get("role"));

        // 2) 刷新
        String newAccess = auth.refresh(refresh);
        assertNotEquals(access, newAccess);
        Claims c2 = auth.verify(newAccess);
        assertEquals("alice", c2.sub());

        // 3) 注销
        auth.logout(access);
        // 注销后验证应失败（jti 在黑名单）
        try {
            auth.verify(access);
            fail("revoked token should be rejected");
        } catch (JwtException expected) { /* ok */ }
        // 但新 access（不同 jti）仍能用
        assertEquals("alice", auth.verify(newAccess).sub());
    }

    @Test
    public void testTokenTampering_fails() {
        AuthService auth = new AuthService();
        String access = auth.login("bob", "user").get("access");
        String[] parts = access.split("\\.");
        String tampered = parts[0] + "." +
                com.zifang.util.core.jwt.Base64Url.encode("{\"sub\":\"bob\",\"role\":\"admin\",\"exp\":9999999999}".getBytes()) +
                "." + parts[2];
        try {
            auth.verify(tampered);
            fail("signature should not match tampered payload");
        } catch (JwtException expected) { /* ok */ }
    }

    @Test
    public void testExpiredToken_fails() {
        String expired = Jwt.builder()
                .algorithm(Jwt.HS256)
                .secret(AuthService.SECRET)
                .claims(new Claims().sub("x").expireIn(-100))
                .build();
        try {
            Jwt.parser().algorithm(Jwt.HS256).secret(AuthService.SECRET).parse(expired);
            fail();
        } catch (JwtException expected) { /* ok */ }
    }

    @Test
    public void testRefreshRejectsAccessToken() {
        AuthService auth = new AuthService();
        String access = auth.login("carol", "user").get("access");
        // 用 access token 去 refresh → 应失败
        try {
            auth.refresh(access);
            fail();
        } catch (JwtException expected) { /* ok */ }
    }

    public static class AuthService {
        public static final String SECRET = "z-util-jwt-secret-256bit-test";
        private final Cache<String, String> blacklist;

        public AuthService() {
            this.blacklist = CacheBuilder.<String, String>newBuilder()
                    .name("jwt-blacklist")
                    .maximumSize(10_000)
                    .expireAfterWrite(Duration.ofDays(7))
                    .build();
        }

        public Map<String, String> login(String userId, String role) {
            String access = Jwt.builder()
                    .algorithm(Jwt.HS256)
                    .secret(SECRET)
                    .claims(new Claims()
                            .sub(userId)
                            .iss("z-opc-ctc")
                            .put("role", role)
                            .jti(UUID.randomUUID().toString())   // 用 jti 让 logout 可标记
                            .expireIn(3600))
                    .build();
            String refresh = Jwt.builder()
                    .algorithm(Jwt.HS256)
                    .secret(SECRET)
                    .claims(new Claims()
                            .sub(userId)
                            .put("type", "refresh")
                            .jti(UUID.randomUUID().toString())
                            .expireIn(7 * 24 * 3600))
                    .build();
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access", access);
            tokens.put("refresh", refresh);
            return tokens;
        }

        public Claims verify(String token) {
            Claims c = Jwt.parser()
                    .algorithm(Jwt.HS256)
                    .secret(SECRET)
                    .leeway(30)
                    .requireExp()
                    .parse(token);
            if (c.jti() != null && blacklist.get(c.jti()) != null) {
                throw new JwtException("token revoked");
            }
            return c;
        }

        public void logout(String token) {
            Claims c = Jwt.parser().algorithm(Jwt.HS256).secret(SECRET).parse(token);
            if (c.jti() != null) {
                blacklist.put(c.jti(), "revoked");
            }
        }

        public String refresh(String refreshToken) {
            Claims c = Jwt.parser().algorithm(Jwt.HS256).secret(SECRET).parse(refreshToken);
            if (!"refresh".equals(c.get("type"))) {
                throw new JwtException("not a refresh token");
            }
            return Jwt.builder()
                    .algorithm(Jwt.HS256)
                    .secret(SECRET)
                    .claims(new Claims()
                            .sub(c.sub())
                            .iss("z-opc-ctc")
                            .put("role", c.get("role"))
                            .jti(UUID.randomUUID().toString())
                            .expireIn(3600))
                    .build();
        }
    }
}
