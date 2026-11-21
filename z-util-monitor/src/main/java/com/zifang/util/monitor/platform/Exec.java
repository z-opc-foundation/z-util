package com.zifang.util.monitor.platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 平台探针包内部的外部命令执行器。
 * <p>
 * macOS 侧的探针靠 {@code sysctl} / {@code netstat} 取数，这里统一封装：
 * 只读 stdout、带超时、任何失败都返回空列表 —— 探针的契约是「拿不到就不产出」，
 * 命令执行失败不该变成采样异常。
 * </p>
 *
 * @author zifang
 */
final class Exec {

    private static final int TIMEOUT_SECONDS = 5;

    private Exec() {
    }

    /**
     * 执行外部命令并按行返回 stdout。
     *
     * @param command StringArray类型参数，命令与参数
     *
     * @return ListString类型返回值，失败或超时返回空列表
     */
    static List<String> lines(String... command) {
        Process process = null;
        try {
            process = new ProcessBuilder(command).start();
            List<String> out = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.add(line);
                }
            }
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return java.util.Collections.emptyList();
            }
            return out;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            return java.util.Collections.emptyList();
        } catch (IOException e) {
            if (process != null) {
                process.destroyForcibly();
            }
            return java.util.Collections.emptyList();
        }
    }
}
