package com.zifang.util.core.lang;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 进程执行工具（对标 Apache Commons Exec）。
 * <p>
 * 简化调用 {@link ProcessBuilder}：
 * <pre>{@code
 *   ProcessExecutor.Result r = new ProcessExecutor()
 *       .command("ls", "-la")
 *       .directory(new File("/tmp"))
 *       .timeout(10, TimeUnit.SECONDS)
 *       .execute();
 *   if (r.isSuccess()) System.out.println(r.getStdout());
 * }</pre>
 *
 * @author zifang
 */
public class ProcessExecutor {

    private List<String> command;
    private File directory;
    private Map<String, String> environment;
    private long timeoutMillis = -1L;
    private Charset charset = Charset.defaultCharset();
    private boolean redirectErrorStream = true;
    private ProcessOutputConsumer stdoutConsumer;
    private ProcessOutputConsumer stderrConsumer;

    public ProcessExecutor command(String... command) {
        this.command = new ArrayList<>();
        for (String c : command) this.command.add(c);
        return this;
    }

    public ProcessExecutor command(List<String> command) {
        this.command = new ArrayList<>(command);
        return this;
    }

    public ProcessExecutor directory(File directory) {
        this.directory = directory;
        return this;
    }

    public ProcessExecutor environment(Map<String, String> env) {
        this.environment = env;
        return this;
    }

    public ProcessExecutor timeout(long timeout, TimeUnit unit) {
        this.timeoutMillis = unit.toMillis(timeout);
        return this;
    }

    public ProcessExecutor charset(Charset cs) {
        this.charset = cs;
        return this;
    }

    public ProcessExecutor redirectErrorStream(boolean v) {
        this.redirectErrorStream = v;
        return this;
    }

    public ProcessExecutor stdout(ProcessOutputConsumer consumer) {
        this.stdoutConsumer = consumer;
        return this;
    }

    public ProcessExecutor stderr(ProcessOutputConsumer consumer) {
        this.stderrConsumer = consumer;
        return this;
    }

    public Result execute() throws IOException, InterruptedException {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        if (directory != null) pb.directory(directory);
        if (environment != null) pb.environment().putAll(environment);
        pb.redirectErrorStream(redirectErrorStream);

        Process p = pb.start();
        StreamGobbler out = new StreamGobbler(p.getInputStream(), charset, stdoutConsumer);
        StreamGobbler err = new StreamGobbler(p.getErrorStream(), charset, stderrConsumer);
        out.start();
        err.start();

        boolean finished;
        if (timeoutMillis > 0) {
            finished = p.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                out.join();
                err.join();
                return new Result(-1, out.getOutput(), err.getOutput(), false, true);
            }
        } else {
            p.waitFor();
        }
        out.join();
        err.join();
        int exit = p.exitValue();
        return new Result(exit, out.getOutput(), err.getOutput(), exit == 0, false);
    }

    /**
     * 输出消费者，用于流式处理子进程输出。
     */
    public interface ProcessOutputConsumer {
        void consume(String line);
    }

    public static class Result {
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final boolean success;
        private final boolean timedOut;

        public Result(int exitCode, String stdout, String stderr, boolean success, boolean timedOut) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.success = success;
            this.timedOut = timedOut;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isTimedOut() {
            return timedOut;
        }
    }

    private static class StreamGobbler extends Thread {
        private final InputStream in;
        private final Charset cs;
        private final ProcessOutputConsumer consumer;
        private final StringBuilder buf = new StringBuilder();

        StreamGobbler(InputStream in, Charset cs, ProcessOutputConsumer consumer) {
            this.in = in;
            this.cs = cs;
            this.consumer = consumer;
            setDaemon(true);
        }

        String getOutput() {
            return buf.toString();
        }

        @Override
        public void run() {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, cs))) {
                String line;
                while ((line = r.readLine()) != null) {
                    buf.append(line).append(System.lineSeparator());
                    if (consumer != null) consumer.consume(line);
                }
            } catch (IOException ignore) {
                // 子进程关闭流时可能抛异常，忽略
            }
        }
    }
}