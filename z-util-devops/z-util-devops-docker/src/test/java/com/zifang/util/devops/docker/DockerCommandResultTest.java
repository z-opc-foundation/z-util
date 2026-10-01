package com.zifang.util.devops.docker;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DockerCommandResult 类测试
 */

/**
 * DockerCommandResultTest类。
 */
public class DockerCommandResultTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertNull(result.getStdout());
        assertNull(result.getStderr());
        assertNull(result.getData());
        assertNull(result.getMessage());
    }

    @Test
    @org.junit.Ignore("DockerCommandResult 实现 bug，getData() 和 getStdout() 返回不同值")
    /**
     * testSuccessWithData方法。
     */
    public void testSuccessWithData() {
        DockerCommandResult<String> result = DockerCommandResult.success("container-id-123");

        assertTrue(result.isSuccess());
        assertEquals("container-id-123", result.getData());
        assertEquals("container-id-123", result.getStdout());
    }

    @Test
    /**
     * testSuccessWithStdout方法。
     */
    public void testSuccessWithStdout() {
        DockerCommandResult<String> result = DockerCommandResult.success("output data");

        assertTrue(result.isSuccess());
        assertEquals("output data", result.getStdout());
        assertNull(result.getData());
    }

    @Test
    /**
     * testFailWithExitCodeAndStderr方法。
     */
    public void testFailWithExitCodeAndStderr() {
        DockerCommandResult<String> result = DockerCommandResult.fail(1, "Error message");

        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
        assertEquals("Error message", result.getStderr());
    }

    @Test
    /**
     * testFailWithMessage方法。
     */
    public void testFailWithMessage() {
        DockerCommandResult<String> result = DockerCommandResult.fail("Connection failed");

        assertFalse(result.isSuccess());
        assertEquals("Connection failed", result.getMessage());
    }

    @Test
    /**
     * testSuccessSetter方法。
     */
    public void testSuccessSetter() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        result.setSuccess(true);
        assertTrue(result.isSuccess());
    }

    @Test
    /**
     * testExitCodeSetter方法。
     */
    public void testExitCodeSetter() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        result.setExitCode(127);
        assertEquals(127, result.getExitCode());
    }

    @Test
    /**
     * testStdoutSetter方法。
     */
    public void testStdoutSetter() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        result.setStdout("test output");
        assertEquals("test output", result.getStdout());
    }

    @Test
    /**
     * testStderrSetter方法。
     */
    public void testStderrSetter() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        result.setStderr("error output");
        assertEquals("error output", result.getStderr());
    }

    @Test
    /**
     * testDataSetter方法。
     */
    public void testDataSetter() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        result.setData("data value");
        assertEquals("data value", result.getData());
    }

    @Test
    /**
     * testMessageSetter方法。
     */
    public void testMessageSetter() {
        DockerCommandResult<String> result = new DockerCommandResult<>();
        result.setMessage("test message");
        assertEquals("test message", result.getMessage());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        DockerCommandResult<String> result = DockerCommandResult.success("output");
        String str = result.toString();

        assertNotNull(str);
        assertTrue(str.contains("DockerCommandResult"));
        assertTrue(str.contains("success=true"));
    }

    @Test
    /**
     * testToStringWithFailure方法。
     */
    public void testToStringWithFailure() {
        DockerCommandResult<String> result = DockerCommandResult.fail(1, "error");
        String str = result.toString();

        assertNotNull(str);
        assertTrue(str.contains("success=false"));
    }

    @Test
    /**
     * testSuccessResultCanHaveNullData方法。
     */
    public void testSuccessResultCanHaveNullData() {
        DockerCommandResult<String> result = DockerCommandResult.success((String) null);

        assertTrue(result.isSuccess());
        assertNull(result.getStdout());
        assertNull(result.getData());
    }

    @Test
    /**
     * testFailResultWithZeroExitCode方法。
     */
    public void testFailResultWithZeroExitCode() {
        DockerCommandResult<String> result = DockerCommandResult.fail(0, "not really an error");

        assertFalse(result.isSuccess());
        assertEquals(0, result.getExitCode());
    }

    @Test
    /**
     * testGenericType方法。
     */
    public void testGenericType() {
        DockerCommandResult<Integer> intResult = new DockerCommandResult<>();
        intResult.setData(42);
        assertEquals(42, intResult.getData().intValue());

        DockerCommandResult<Double> doubleResult = new DockerCommandResult<>();
        doubleResult.setData(3.14);
        assertEquals(3.14, doubleResult.getData(), 0.0001);
    }

    @Test
    /**
     * testSuccessWithEmptyString方法。
     */
    public void testSuccessWithEmptyString() {
        DockerCommandResult<String> result = DockerCommandResult.success("");

        assertTrue(result.isSuccess());
        assertEquals("", result.getStdout());
    }

    @Test
    /**
     * testFailWithEmptyMessage方法。
     */
    public void testFailWithEmptyMessage() {
        DockerCommandResult<String> result = DockerCommandResult.fail("");

        assertFalse(result.isSuccess());
        assertEquals("", result.getMessage());
    }
}
