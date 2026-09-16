package com.zifang.util.devops.git.operations.shell;

import com.zifang.util.devops.git.operations.GitException;
import com.zifang.util.devops.git.operations.GitResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * git shell 命令执行器
 * <p>
 * 通过 ProcessBuilder 调系统 git 二进制，作为 JGit 的兜底方案。
 * 适用于 JGit 不支持或实现不完整的场景（如 worktree、bisect、archive 等）。
 *
 * @author zifang
 * @version 1.0.0
 */
public class ShellExecutor {

    /**
     * 默认超时：5 分钟
     */
    public static final long DEFAULT_TIMEOUT_SECONDS = 300L;
    /**
     * git 可执行文件名
     */
    public static final String GIT_CMD = "git";

    private final String gitBinary;
    private final long timeoutSeconds;

    /**
     * ShellExecutor方法。
     */
    public ShellExecutor() {
        this(GIT_CMD, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * ShellExecutor方法。
     * * @param gitBinary String类型参数
     */
    public ShellExecutor(String gitBinary) {
        this(gitBinary, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * ShellExecutor方法。
     * * @param gitBinary String类型参数
     *
     * @param timeoutSeconds long类型参数
     */
    public ShellExecutor(String gitBinary, long timeoutSeconds) {
        this.gitBinary = gitBinary;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 执行 git 命令（无工作目录）
     *
     * @param args git 子命令及参数（不含 "git" 本身）
     * @return 执行结果
     */
    public GitResult<String> exec(String... args) {
        return exec(null, args);
    }

    /**
     * 在指定工作目录执行 git 命令
     *
     * @param workDir 工作目录（可为 null 表示当前目录）
     * @param args    git 子命令及参数
     * @return 执行结果
     */
    public GitResult<String> exec(File workDir, String... args) {
        return exec(workDir, null, args);
    }

    /**
     * 在指定工作目录、带环境变量执行 git 命令
     *
     * @param workDir  工作目录
     * @param envExtra 额外环境变量（可空）
     * @param args     git 子命令及参数
     * @return 执行结果
     */
    public GitResult<String> exec(File workDir, Map<String, String> envExtra, String... args) {
        if (args == null || args.length == 0) {
            return GitResult.fail("git 参数不能为空");
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(gitBinary);
        cmd.addAll(Arrays.asList(args));
        return runProcess(workDir, envExtra, cmd);
    }

    /**
     * 执行通用命令（非 git）
     */
    public GitResult<String> execRaw(File workDir, List<String> command) {
        return runProcess(workDir, null, command);
    }

    private GitResult<String> runProcess(File workDir, Map<String, String> envExtra, List<String> command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workDir != null) {
            pb.directory(workDir);
        }
        pb.redirectErrorStream(false);
        // 强制 UTF-8，避免中文文件名乱码
        pb.environment().put("LC_ALL", "C.UTF-8");
        pb.environment().put("LANG", "C.UTF-8");
        // GIT_TERMINAL_PROMPT=0 让 git 在无法弹出凭证输入时直接失败而不是阻塞
        pb.environment().put("GIT_TERMINAL_PROMPT", "0");
        if (envExtra != null) {
            for (Map.Entry<String, String> e : envExtra.entrySet()) {
                pb.environment().put(e.getKey(), e.getValue());
            }
        }

        Process process = null;
        try {
            process = pb.start();
            StringBuilder out = new StringBuilder();
            StringBuilder err = new StringBuilder();
            Thread tOut = new Thread(new StreamCollector(process.getInputStream(), out, false));
            Thread tErr = new Thread(new StreamCollector(process.getErrorStream(), err, true));
            tOut.setDaemon(true);
            tErr.setDaemon(true);
            tOut.start();
            tErr.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new GitException("git 命令执行超时（" + timeoutSeconds + "s）: " + String.join(" ", command));
            }
            tOut.join(2000);
            tErr.join(2000);
            int exit = process.exitValue();
            if (exit == 0) {
                return GitResult.success(out.toString().trim(), exit);
            }
            return GitResult.<String>fail(exit, err.toString().trim());
        } catch (GitException e) {
            throw e;
        } catch (Exception e) {
            if (process != null) {
                process.destroyForcibly();
            }
            throw new GitException("执行 git 命令失败: " + String.join(" ", command), e);
        }
    }

    /**
     * 检查 git 二进制是否可用
     */
    public boolean isAvailable() {
        try {
            GitResult<String> r = exec("--version");
            return r.isSuccess() && r.getStdout() != null && r.getStdout().toLowerCase().contains("git version");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 流收集器
     */
    private static class StreamCollector implements Runnable {
        private final java.io.InputStream in;
        private final StringBuilder buf;
        private final boolean isErr;

        StreamCollector(java.io.InputStream in, StringBuilder buf, boolean isErr) {
            this.in = in;
            this.buf = buf;
            this.isErr = isErr;
        }

        @Override
        /**
         * run方法。
         */
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    synchronized (buf) {
                        if (buf.length() > 0) {
                            buf.append('\n');
                        }
                        buf.append(line);
                    }
                }
            } catch (Exception ignored) {
                // ignore stream close errors
            }
        }
    }
}
