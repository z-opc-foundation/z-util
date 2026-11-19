package com.zifang.util.monitor.sampling.store;

import com.zifang.util.json.JsonUtil;
import com.zifang.util.monitor.sampling.Sample;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 磁盘采样存储。
 * <p>
 * 以 JSON Lines 落盘：一行一条样本，形如
 * <code>{"ts":1700000000000,"v":{"jvm.memory.heap.used":1048576},"at":{"host.name":"node-1"}}</code>。
 * 文件超过容量上限即轮转，保留最近 N 个文件 —— 磁盘侧同样实现了「只保留最近若干
 * 条 / 若干分钟」的语义，只是载体换成了文件。
 * </p>
 * <p>
 * 与内存存储可以同时挂载：内存负责实时绘图，磁盘负责长时间留痕。
 * </p>
 *
 * @author zifang
 */
public final class FileSampleStore implements SampleStore {

    private static final String FILE_SUFFIX = ".jsonl";

    private final Path directory;
    private final String filePrefix;
    private final long maxFileBytes;
    private final int maxFiles;
    private final Object lock = new Object();

    private BufferedWriter writer;
    private Path currentFile;
    private long currentFileBytes;
    private long totalAppended;
    private long totalQueried;
    private long totalEvicted;

    /**
     * FileSampleStore方法。
     *
     * @param directory    Path类型参数
     * @param filePrefix   String类型参数
     * @param maxFileBytes long类型参数
     * @param maxFiles     int类型参数
     */
    public FileSampleStore(Path directory, String filePrefix, long maxFileBytes, int maxFiles) {
        this.directory = directory;
        this.filePrefix = filePrefix;
        this.maxFileBytes = maxFileBytes;
        this.maxFiles = maxFiles;
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建采样存储目录: " + directory, e);
        }
    }

    /**
     * 创建构建器。
     *
     * @param directory Path类型参数
     *
     * @return Builder类型返回值
     */
    public static Builder builder(Path directory) {
        return new Builder(directory);
    }

    /**
     * getDirectory方法。
     *
     * @return Path类型返回值
     */
    public Path getDirectory() {
        return directory;
    }

    /**
     * 列出存储目录下的数据文件，按文件名排序（即按时间先后）。
     *
     * @return ListPath类型返回值
     */
    public List<Path> listFiles() {
        if (!Files.isDirectory(directory)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(p -> p.getFileName().toString().startsWith(filePrefix)
                            && p.getFileName().toString().endsWith(FILE_SUFFIX))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void append(Sample sample) {
        if (sample == null) {
            return;
        }
        String line = encode(sample);
        synchronized (lock) {
            try {
                ensureWriter();
                writer.write(line);
                writer.newLine();
                writer.flush();
                currentFileBytes += line.length() + 1L;
                totalAppended++;
                if (currentFileBytes >= maxFileBytes) {
                    rotate();
                }
            } catch (IOException e) {
                throw new UncheckedIOException("写入采样文件失败", e);
            }
        }
    }

    @Override
    public List<Sample> query(long from, long to) {
        List<Sample> result = new ArrayList<>();
        for (Path file : listFiles()) {
            for (Sample sample : readFile(file)) {
                long ts = sample.getTimestamp();
                if (ts >= from && ts <= to) {
                    result.add(sample);
                }
            }
        }
        synchronized (lock) {
            totalQueried += result.size();
        }
        return result;
    }

    @Override
    public List<Sample> query() {
        List<Sample> result = new ArrayList<>();
        for (Path file : listFiles()) {
            result.addAll(readFile(file));
        }
        synchronized (lock) {
            totalQueried += result.size();
        }
        return result;
    }

    @Override
    public Sample latest() {
        List<Path> files = listFiles();
        for (int i = files.size() - 1; i >= 0; i--) {
            List<Sample> samples = readFile(files.get(i));
            if (!samples.isEmpty()) {
                return samples.get(samples.size() - 1);
            }
        }
        return null;
    }

    @Override
    public void clear() {
        synchronized (lock) {
            closeWriter();
            for (Path file : listFiles()) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException ignored) {
                    // 删除失败不影响主流程
                }
            }
        }
    }

    @Override
    public int size() {
        int count = 0;
        for (Path file : listFiles()) {
            count += readFile(file).size();
        }
        return count;
    }

    @Override
    public long estimatedBytes() {
        long total = 0;
        for (Path file : listFiles()) {
            try {
                total += Files.size(file);
            } catch (IOException ignored) {
                // 统计失败按 0 计
            }
        }
        return total;
    }

    @Override
    public StoreStats stats() {
        synchronized (lock) {
            return new StoreStats(totalAppended, totalEvicted, totalQueried,
                    size(), estimatedBytes());
        }
    }

    @Override
    public void close() {
        synchronized (lock) {
            closeWriter();
        }
    }

    /**
     * 把一条样本编码成一行 JSON。
     *
     * @param sample Sample类型参数
     *
     * @return String类型返回值
     */
    private String encode(Sample sample) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("ts", sample.getTimestamp());
        root.put("v", sample.getValues());
        if (!sample.getAttributes().isEmpty()) {
            root.put("at", sample.getAttributes());
        }
        return JsonUtil.toJson(root);
    }

    private List<Sample> readFile(Path file) {
        List<Sample> result = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }
                Sample sample = decode(line);
                if (sample != null) {
                    result.add(sample);
                }
            }
        } catch (IOException ignored) {
            // 单个文件读失败不影响其它文件
        }
        return result;
    }

    private Sample decode(String line) {
        try {
            Object parsed = JsonUtil.toPlainValue(JsonUtil.parse(line));
            if (!(parsed instanceof Map)) {
                return null;
            }
            Map<?, ?> root = (Map<?, ?>) parsed;
            long ts = toLong(root.get("ts"));
            Map<String, Number> values = new LinkedHashMap<>();
            Object rawValues = root.get("v");
            if (rawValues instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawValues).entrySet()) {
                    Number value = toNumber(entry.getValue());
                    if (value != null) {
                        values.put(String.valueOf(entry.getKey()), value);
                    }
                }
            }
            Map<String, String> attributes = new LinkedHashMap<>();
            Object rawAttributes = root.get("at");
            if (rawAttributes instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawAttributes).entrySet()) {
                    if (entry.getValue() != null) {
                        attributes.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }
            }
            return new Sample(ts, values, attributes);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Number toNumber(Object value) {
        if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) <= Long.MAX_VALUE) {
                return (long) d;
            }
            return d;
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    private void ensureWriter() throws IOException {
        if (writer != null) {
            return;
        }
        currentFile = nextFile();
        writer = Files.newBufferedWriter(currentFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        currentFileBytes = Files.size(currentFile);
    }

    private Path nextFile() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
        Path candidate = directory.resolve(filePrefix + "-" + stamp + FILE_SUFFIX);
        int seq = 0;
        while (Files.exists(candidate)) {
            candidate = directory.resolve(filePrefix + "-" + stamp + "-" + (++seq) + FILE_SUFFIX);
        }
        return candidate;
    }

    private void rotate() throws IOException {
        closeWriter();
        List<Path> files = listFiles();
        // 下一个样本会另起一个文件，所以这里只留 maxFiles - 1 个，
        // 加上新文件刚好凑满 maxFiles。
        while (files.size() >= maxFiles) {
            Path oldest = files.remove(0);
            try {
                totalEvicted += countLines(oldest);
                Files.deleteIfExists(oldest);
            } catch (IOException ignored) {
                // 删除失败则跳过该文件
            }
        }
    }

    private long countLines(Path file) {
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines.count();
        } catch (IOException e) {
            return 0L;
        }
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // 关闭失败无需处理
            }
            writer = null;
        }
    }

    /**
     * 磁盘存储构建器。
     */
    public static final class Builder {

        private final Path directory;
        private String filePrefix = "samples";
        private long maxFileBytes = 16L * 1024 * 1024;
        private int maxFiles = 10;

        private Builder(Path directory) {
            this.directory = directory;
        }

        /**
         * filePrefix方法。
         *
         * @param filePrefix String类型参数
         *
         * @return Builder类型返回值
         */
        public Builder filePrefix(String filePrefix) {
            if (filePrefix != null && !filePrefix.isEmpty()) {
                this.filePrefix = filePrefix;
            }
            return this;
        }

        /**
         * maxFileBytes方法。
         *
         * @param maxFileBytes long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxFileBytes(long maxFileBytes) {
            if (maxFileBytes > 0) {
                this.maxFileBytes = maxFileBytes;
            }
            return this;
        }

        /**
         * maxFiles方法。
         *
         * @param maxFiles int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxFiles(int maxFiles) {
            if (maxFiles > 0) {
                this.maxFiles = maxFiles;
            }
            return this;
        }

        /**
         * build方法。
         *
         * @return FileSampleStore类型返回值
         */
        public FileSampleStore build() {
            return new FileSampleStore(directory, filePrefix, maxFileBytes, maxFiles);
        }
    }
}
