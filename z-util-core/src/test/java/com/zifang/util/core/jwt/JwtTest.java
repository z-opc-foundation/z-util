package com.zifang.util.core.jwt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 自研 JWT 工具测试（无 JJWT 依赖）。
 */
public class JwtTest {

    @Test
    public void testRoundTrip_hs256() {
        Claims claims = new Claims()
                .sub("alice")
                .iss("z-util")
                .expireIn(3600)
                .put("role", "admin");
        String token = Jwt.builder()
                .algorithm(Jwt.HS256)
                .secret("my-secret-key-256bit")
                .claims(claims)
                .build();
        assertEquals(3, token.split("\\.").length);

        Claims parsed = Jwt.parser()
                .algorithm(Jwt.HS256)
                .secret("my-secret-key-256bit")
                .parse(token);
        assertEquals("alice", parsed.sub());
        assertEquals("z-util", parsed.iss());
        assertEquals("admin", parsed.get("role"));
        assertNotNull(parsed.exp());
        assertTrue(parsed.exp() > System.currentTimeMillis() / 1000);
    }

    @Test(expected = JwtException.class)
    public void testWrongSecret_throws() {
        String token = Jwt.builder()
                .algorithm(Jwt.HS256).secret("right-secret").claims(new Claims().sub("bob"))
                .build();
        Jwt.parser().algorithm(Jwt.HS256).secret("wrong-secret").parse(token);
    }

    @Test(expected = JwtException.class)
    public void testExpired_throws() {
        Claims c = new Claims().sub("x").exp(System.currentTimeMillis() / 1000 - 100);
        String token = Jwt.builder().algorithm(Jwt.HS256).secret("k").claims(c).build();
        Jwt.parser().algorithm(Jwt.HS256).secret("k").parse(token);
    }

    @Test
    public void testLeeway_clockSkew() {
        Claims c = new Claims().sub("x").exp(System.currentTimeMillis() / 1000 - 5);
        String token = Jwt.builder().algorithm(Jwt.HS256).secret("k").claims(c).build();
        // 没有 leeway → 失败
        try {
            Jwt.parser().algorithm(Jwt.HS256).secret("k").parse(token);
            fail("expected expiration");
        } catch (JwtException expected) { /* ok */ }
        // 有 10s leeway → 通过
        Claims parsed = Jwt.parser().algorithm(Jwt.HS256).secret("k").leeway(10).parse(token);
        assertEquals("x", parsed.sub());
    }

    @Test(expected = JwtException.class)
    public void testRequireExp_missing_throws() {
        Claims c = new Claims().sub("x");   // no exp
        String token = Jwt.builder().algorithm(Jwt.HS256).secret("k").claims(c).build();
        Jwt.parser().algorithm(Jwt.HS256).secret("k").requireExp().parse(token);
    }

    @Test
    public void testAlgMismatch_throws() {
        String token = Jwt.builder().algorithm(Jwt.HS256).secret("k").claims(new Claims().sub("x")).build();
        // 解析时用 HS512（虽然 secret 相同但算法不匹配） → 验签失败
        try {
            Jwt.parser().algorithm(Jwt.HS512).secret("k").parse(token);
            fail("expected mismatch");
        } catch (JwtException expected) { /* ok */ }
    }

    @Test
    public void testMalformed_throws() {
        try {
            Jwt.parser().algorithm(Jwt.HS256).secret("k").parse("not-a-jwt");
            fail("expected");
        } catch (JwtException expected) { /* ok */ }
    }

    @Test
    public void testHs512() {
        String token = Jwt.builder().algorithm(Jwt.HS512).secret("k").claims(new Claims().sub("x")).build();
        assertEquals("x", Jwt.parser().algorithm(Jwt.HS512).secret("k").parse(token).sub());
    }
}
