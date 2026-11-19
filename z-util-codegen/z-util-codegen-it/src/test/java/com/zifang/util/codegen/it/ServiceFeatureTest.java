package com.zifang.util.codegen.it;

import com.zifang.util.codegen.it.spi.AlertChannel;
import com.zifang.util.codegen.it.spi.EmailChannel;
import com.zifang.util.codegen.it.spi.LogSink;
import com.zifang.util.codegen.it.spi.SmsChannel;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @ZAutoService：注册文件由处理器在编译期生成，ServiceLoader 必须能发现 provider，
 * 且同一 service 的多个 provider 合并进同一份文件、无重复行。
 */
class ServiceFeatureTest {

    @Test
    void serviceLoaderDiscoversAllProviders() {
        List<Class<?>> found = new ArrayList<>();
        for (AlertChannel channel : ServiceLoader.load(AlertChannel.class)) {
            found.add(channel.getClass());
        }
        assertTrue(found.contains(EmailChannel.class));
        assertTrue(found.contains(SmsChannel.class));
        assertEquals(2, found.size());
    }

    @Test
    void oneProviderCanRegisterMultipleServices() {
        List<Class<?>> found = new ArrayList<>();
        for (LogSink sink : ServiceLoader.load(LogSink.class)) {
            found.add(sink.getClass());
        }
        assertEquals(List.of(EmailChannel.class), found);
    }

    @Test
    void registrationFileIsMergedAndDeduplicated() throws Exception {
        URL resource = getClass().getClassLoader()
                .getResource("META-INF/services/" + AlertChannel.class.getName());
        assertTrue(resource != null, "META-INF/services 注册文件应已生成");
        List<String> lines = readAll(resource.openStream());
        assertEquals(List.of(EmailChannel.class.getName(), SmsChannel.class.getName()), lines);
    }

    private static List<String> readAll(InputStream in) throws Exception {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line.trim());
                }
            }
        }
        return lines;
    }
}
