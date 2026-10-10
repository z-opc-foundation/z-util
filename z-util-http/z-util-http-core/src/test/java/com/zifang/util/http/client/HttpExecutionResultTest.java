package com.zifang.util.http.client;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpExecutionResultTest {

    @Test
    public void isHttpStatusOk_covers2xxOnly() {
        HttpExecutionResult ok = HttpExecutionResult.ok(200, null, "body", 1L);
        assertTrue(ok.isHttpStatusOk());
        ok.setStatus(204);
        assertTrue(ok.isHttpStatusOk());

        // isSuccess 是"调用完成"语义：4xx/5xx 也为 true，但 isHttpStatusOk 为 false
        HttpExecutionResult notFound = HttpExecutionResult.ok(404, null, "body", 1L);
        assertTrue(notFound.isSuccess());
        assertFalse(notFound.isHttpStatusOk());

        HttpExecutionResult serverError = HttpExecutionResult.ok(502, null, "body", 1L);
        assertFalse(serverError.isHttpStatusOk());

        HttpExecutionResult failure = HttpExecutionResult.fail("boom", "IO");
        assertFalse(failure.isSuccess());
        assertFalse(failure.isHttpStatusOk());
    }
}
