package com.zifang.util.devops.docker;

import com.zifang.util.core.lang.StringUtil;
import com.zifang.util.json.JsonUtil;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import com.zifang.util.devops.docker.dto.ContainerDTO;
import com.zifang.util.devops.docker.dto.ImageDTO;
import com.zifang.util.devops.docker.dto.NetworkDTO;
import com.zifang.util.devops.docker.dto.VolumeDTO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Docker 命令封装客户端
 * <p>
 * 通过 docker CLI 执行命令，解析 JSON 输出，
 * 提供容器、镜像、网络、卷等资源的完整管理操作。
 *
 * @author zifang
 * @version 1.0.0
 */
public class DockerClient {

    private final String dockerHost;

    /**
     * 使用默认 docker socket
     */
    public DockerClient() {
        this("unix:///var/run/docker.sock");
    }

    /**
     * 指定 docker host
     *
     * @param dockerHost unix:///var/run/docker.sock 或 tcp://host:2375
     */
    public DockerClient(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    // ==================== 内部工具方法 ====================

    private String[] dockerCmd(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        if (dockerHost != null && dockerHost.startsWith("unix://")) {
            cmd.add("-H");
            cmd.add(dockerHost);
        }
        cmd.addAll(Arrays.asList(args));
        return cmd.toArray(new String[0]);
    }

    private String execToString(String... cmd) {
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("执行 docker 命令失败: " + String.join(" ", cmd), e);
        }
        return sb.toString().trim();
    }

    private String execToStringSilently(String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            p.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private int execExitCode(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).start();
            p.waitFor();
            return p.exitValue();
        } catch (Exception e) {
            return -1;
        }
    }

    // ==================== 容器操作 ====================

    /**
     * 列出所有容器
     *
     * @param all true 包含已停止的容器
     */
    public List<ContainerDTO> listContainers(boolean all) {
        String[] cmd = dockerCmd("ps", "-a", "--format", "{{json .}}");
        String output = execToString(cmd);
        List<ContainerDTO> list = new ArrayList<>();
        if (StringUtil.isBlank(output)) {
            return list;
        }
        for (String line : output.split("\n")) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            ContainerDTO dto = JsonUtil.fromJson(line, ContainerDTO.class);
            list.add(dto);
        }
        if (!all) {
            list.removeIf(c -> !"running".equalsIgnoreCase(c.getState()));
        }
        return list;
    }

    /**
     * 列出所有容器（包括已停止）
     */
    public List<ContainerDTO> listAllContainers() {
        return listContainers(true);
    }

    /**
     * 列出运行中的容器
     */
    public List<ContainerDTO> listRunningContainers() {
        return listContainers(false);
    }

    /**
     * 创建并启动容器
     *
     * @param image 镜像
     * @param name  容器名
     * @param cmd   启动命令
     * @return 容器ID
     */
    public String createContainer(String image, String name, String... cmd) {
        List<String> cmdList = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "run", "-d", "--name", name));
        for (String c : cmd) {
            cmdList.add(c);
        }
        cmdList.add(image);
        String[] fullCmd = cmdList.toArray(new String[0]);
        return execToString(fullCmd).trim();
    }

    /**
     * 启动容器
     */
    public void startContainer(String containerIdOrName) {
        execToStringSilently(dockerCmd("start", containerIdOrName));
    }

    /**
     * 停止容器
     */
    public void stopContainer(String containerIdOrName) {
        execToStringSilently(dockerCmd("stop", containerIdOrName));
    }

    /**
     * 重启容器
     */
    public void restartContainer(String containerIdOrName) {
        execToStringSilently(dockerCmd("restart", containerIdOrName));
    }

    /**
     * 删除容器
     *
     * @param containerIdOrName 容器ID或名称
     * @param force             强制删除
     */
    public void removeContainer(String containerIdOrName, boolean force) {
        String[] cmd = force
                ? dockerCmd("rm", "-f", containerIdOrName)
                : dockerCmd("rm", containerIdOrName);
        execToStringSilently(cmd);
    }

    /**
     * 强制删除容器
     */
    public void forceRemoveContainer(String containerIdOrName) {
        removeContainer(containerIdOrName, true);
    }

    /**
     * 暂停容器
     */
    public void pauseContainer(String containerIdOrName) {
        execToStringSilently(dockerCmd("pause", containerIdOrName));
    }

    /**
     * 取消暂停
     */
    public void unpauseContainer(String containerIdOrName) {
        execToStringSilently(dockerCmd("unpause", containerIdOrName));
    }

    /**
     * 杀掉容器
     */
    public void killContainer(String containerIdOrName) {
        execToStringSilently(dockerCmd("kill", containerIdOrName));
    }

    /**
     * 重命名容器
     */
    public void renameContainer(String containerIdOrName, String newName) {
        execToStringSilently(dockerCmd("rename", containerIdOrName, newName));
    }

    /**
     * 获取容器详细信息
     */
    public ContainerDTO inspectContainer(String containerIdOrName) {
        String output = execToString(dockerCmd("inspect", containerIdOrName));
        if (StringUtil.isBlank(output)) {
            return null;
        }
        JsonArray array = JsonUtil.parseArray(output);
        if (array.size() == 0) {
            return null;
        }
        JsonObject obj = array.getJsonObject(0);
        ContainerDTO dto = new ContainerDTO();
        dto.setId(getJsonString(obj, "Id"));
        dto.setName(getJsonString(obj, "Name"));
        dto.setImage(getJsonString(obj, "Config", "Image"));
        JsonObject stateObj = obj.containsKey("State") ? obj.getJsonObject("State") : null;
        if (stateObj != null) {
            dto.setState(getJsonString(stateObj, "Status"));
            dto.setStatus(getJsonString(stateObj, "Status"));
        }
        JsonObject configObj = obj.containsKey("Config") ? obj.getJsonObject("Config") : null;
        if (configObj != null) {
            dto.setCommand(getJsonString(configObj, "Cmd"));
        }
        return dto;
    }

    /**
     * 获取容器日志
     *
     * @param containerIdOrName 容器ID或名称
     * @param tail              显示尾行数，null表示全部
     */
    public String getContainerLogs(String containerIdOrName, Integer tail) {
        String[] cmd;
        if (tail != null) {
            cmd = dockerCmd("logs", "--tail", String.valueOf(tail), containerIdOrName);
        } else {
            cmd = dockerCmd("logs", containerIdOrName);
        }
        return execToStringSilently(cmd);
    }

    /**
     * 获取容器日志（最后100行）
     */
    public String getContainerLogs(String containerIdOrName) {
        return getContainerLogs(containerIdOrName, 100);
    }

    /**
     * 容器内执行命令
     */
    public String execContainer(String containerIdOrName, String... command) {
        List<String> cmdList = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "exec", containerIdOrName));
        cmdList.addAll(Arrays.asList(command));
        return execToString(cmdList.toArray(new String[0]));
    }

    /**
     * 在容器内交互式执行命令
     */
    public String execContainerInteractive(String containerIdOrName, String... command) {
        List<String> cmdList = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "exec", "-i", containerIdOrName));
        cmdList.addAll(Arrays.asList(command));
        return execToString(cmdList.toArray(new String[0]));
    }

    /**
     * 提交容器为镜像
     */
    public String commitContainer(String containerIdOrName, String repository, String tag) {
        String[] cmd = dockerCmd("commit", containerIdOrName, repository + ":" + tag);
        return execToString(cmd);
    }

    /**
     * 容器文件变更
     */
    public List<String> containerDiff(String containerIdOrName) {
        String output = execToString(dockerCmd("diff", containerIdOrName));
        List<String> diffs = new ArrayList<>();
        for (String line : output.split("\n")) {
            if (StringUtil.isNotBlank(line)) {
                diffs.add(line);
            }
        }
        return diffs;
    }

    /**
     * 容器资源使用统计
     */
    public DockerClient.StatsDTO getContainerStats(String containerIdOrName) {
        StatsDTO stats = new StatsDTO();
        String output = execToString(dockerCmd("stats", "--no-stream", "--format", "{{json .}}", containerIdOrName));
        if (StringUtil.isBlank(output)) {
            return stats;
        }
        JsonObject obj = JsonUtil.parseObject(output);
        String cpuStr = getJsonString(obj, "CPUPerc");
        String memStr = getJsonString(obj, "MemPerc");
        String netStr = getJsonString(obj, "NetIO");
        String blockStr = getJsonString(obj, "BlockIO");

        if (cpuStr != null) {
            stats.setCpuPercent(parsePercent(cpuStr));
        }
        if (memStr != null) {
            stats.setMemoryPercent(parsePercent(memStr));
        }
        if (netStr != null) {
            String[] parts = netStr.split("/");
            if (parts.length == 2) {
                stats.setNetworkRx(parseSize(parts[0]));
                stats.setNetworkTx(parseSize(parts[1]));
            }
        }
        if (blockStr != null) {
            String[] parts = blockStr.split("/");
            if (parts.length == 2) {
                stats.setBlockRead(parseSize(parts[0]));
                stats.setBlockWrite(parseSize(parts[1]));
            }
        }
        return stats;
    }

    /**
     * 端口映射
     */
    public void portContainer(String containerIdOrName) {
        execToString(dockerCmd("port", containerIdOrName));
    }

    // ==================== 镜像操作 ====================

    /**
     * 列出所有镜像
     */
    public List<ImageDTO> listImages() {
        String output = execToString(dockerCmd("images", "--format", "{{json .}}"));
        List<ImageDTO> list = new ArrayList<>();
        if (StringUtil.isBlank(output)) {
            return list;
        }
        for (String line : output.split("\n")) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            ImageDTO dto = JsonUtil.fromJson(line, ImageDTO.class);
            list.add(dto);
        }
        return list;
    }

    /**
     * 拉取镜像
     */
    public void pullImage(String image, String tag) {
        execToStringSilently(dockerCmd("pull", image + ":" + tag));
    }

    /**
     * 推送镜像
     */
    public void pushImage(String image, String tag) {
        execToStringSilently(dockerCmd("push", image + ":" + tag));
    }

    /**
     * 删除镜像
     *
     * @param imageIdOrName 镜像ID或名称
     * @param force         强制删除
     */
    public void removeImage(String imageIdOrName, boolean force) {
        String[] cmd = force
                ? dockerCmd("rmi", "-f", imageIdOrName)
                : dockerCmd("rmi", imageIdOrName);
        execToStringSilently(cmd);
    }

    /**
     * 强制删除镜像
     */
    public void forceRemoveImage(String imageIdOrName) {
        removeImage(imageIdOrName, true);
    }

    /**
     * 标记镜像
     */
    public void tagImage(String imageIdOrName, String repository, String tag) {
        execToStringSilently(dockerCmd("tag", imageIdOrName, repository + ":" + tag));
    }

    /**
     * 获取镜像详细信息
     */
    public ImageDTO inspectImage(String imageIdOrName) {
        String output = execToString(dockerCmd("image", "inspect", imageIdOrName));
        if (StringUtil.isBlank(output)) {
            return null;
        }
        JsonArray array = JsonUtil.parseArray(output);
        if (array.size() == 0) {
            return null;
        }
        JsonObject obj = array.getJsonObject(0);
        ImageDTO dto = new ImageDTO();
        dto.setId(getJsonString(obj, "Id"));
        dto.setCreated(getJsonString(obj, "Created"));
        dto.setSize(getJsonString(obj, "Size"));
        if (obj.containsKey("RepoTags")) {
            JsonArray tags = obj.getJsonArray("RepoTags");
            List<String> repoTags = new ArrayList<>();
            for (Object e : tags) {
                repoTags.add(e == null ? null : String.valueOf(e));
            }
            dto.setRepoTags(repoTags);
        }
        return dto;
    }

    /**
     * 构建镜像
     *
     * @param dockerFilePath Dockerfile 路径
     * @param imageName      镜像名
     * @param tag            标签
     * @return 镜像ID
     */
    public String buildImage(String dockerFilePath, String imageName, String tag) {
        String[] cmd = dockerCmd("build", "-t", imageName + ":" + tag, "-f", dockerFilePath, ".");
        return execToString(cmd);
    }

    /**
     * 镜像历史
     */
    public String imageHistory(String imageIdOrName) {
        return execToString(dockerCmd("history", imageIdOrName));
    }

    /**
     * 登录镜像仓库
     */
    public int login(String registry, String username, String password) {
        return execExitCode("docker", "-H", dockerHost, "login", registry, "-u", username, "-p", password);
    }

    /**
     * 登出镜像仓库
     */
    public int logout(String registry) {
        return execExitCode("docker", "-H", dockerHost, "logout", registry);
    }

    /**
     * 清理悬空镜像
     */
    public void pruneImages() {
        execToStringSilently(dockerCmd("image", "prune", "-f"));
    }

    // ==================== 网络操作 ====================

    /**
     * 列出所有网络
     */
    public List<NetworkDTO> listNetworks() {
        String output = execToString(dockerCmd("network", "ls", "--format", "{{json .}}"));
        List<NetworkDTO> list = new ArrayList<>();
        if (StringUtil.isBlank(output)) {
            return list;
        }
        for (String line : output.split("\n")) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            JsonObject obj = JsonUtil.parseObject(line);
            NetworkDTO dto = new NetworkDTO();
            dto.setId(getJsonString(obj, "ID"));
            dto.setName(getJsonString(obj, "Name"));
            dto.setDriver(getJsonString(obj, "Driver"));
            dto.setScope(getJsonString(obj, "Scope"));
            list.add(dto);
        }
        return list;
    }

    /**
     * 创建网络
     *
     * @param name   网络名
     * @param driver 驱动（bridge/host/overlay）
     * @param subnet 子网
     */
    public String createNetwork(String name, String driver, String subnet) {
        List<String> cmdList = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "network", "create", "--driver", driver));
        if (StringUtil.isNotBlank(subnet)) {
            cmdList.add("--subnet");
            cmdList.add(subnet);
        }
        cmdList.add(name);
        return execToString(cmdList.toArray(new String[0]));
    }

    /**
     * 创建 bridge 网络
     */
    public String createBridgeNetwork(String name) {
        return createNetwork(name, "bridge", null);
    }

    /**
     * 删除网络
     */
    public void removeNetwork(String networkIdOrName) {
        execToStringSilently(dockerCmd("network", "rm", networkIdOrName));
    }

    /**
     * 连接容器到网络
     */
    public void connectContainerToNetwork(String networkIdOrName, String containerIdOrName) {
        execToStringSilently(dockerCmd("network", "connect", networkIdOrName, containerIdOrName));
    }

    /**
     * 断开容器与网络的连接
     */
    public void disconnectContainerFromNetwork(String networkIdOrName, String containerIdOrName) {
        execToStringSilently(dockerCmd("network", "disconnect", networkIdOrName, containerIdOrName));
    }

    /**
     * 获取网络详细信息
     */
    public NetworkDTO inspectNetwork(String networkIdOrName) {
        String output = execToString(dockerCmd("network", "inspect", networkIdOrName));
        if (StringUtil.isBlank(output)) {
            return null;
        }
        JsonArray array = JsonUtil.parseArray(output);
        if (array.size() == 0) {
            return null;
        }
        JsonObject obj = array.getJsonObject(0);
        NetworkDTO dto = new NetworkDTO();
        dto.setId(getJsonString(obj, "Id"));
        dto.setName(getJsonString(obj, "Name"));
        dto.setDriver(getJsonString(obj, "Driver"));
        dto.setScope(getJsonString(obj, "Scope"));
        if (obj.containsKey("IPAM")) {
            JsonObject ipam = obj.getJsonObject("IPAM");
            if (ipam.containsKey("Config")) {
                JsonArray configs = ipam.getJsonArray("Config");
                if (configs.size() > 0) {
                    JsonObject config = configs.getJsonObject(0);
                    dto.setSubnet(getJsonString(config, "Subnet"));
                    dto.setGateway(getJsonString(config, "Gateway"));
                }
            }
        }
        return dto;
    }

    // ==================== 卷操作 ====================

    /**
     * 列出所有卷
     */
    public List<VolumeDTO> listVolumes() {
        String output = execToString(dockerCmd("volume", "ls", "--format", "{{json .}}"));
        List<VolumeDTO> list = new ArrayList<>();
        if (StringUtil.isBlank(output)) {
            return list;
        }
        for (String line : output.split("\n")) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            JsonObject obj = JsonUtil.parseObject(line);
            VolumeDTO dto = new VolumeDTO();
            dto.setName(getJsonString(obj, "Name"));
            dto.setDriver(getJsonString(obj, "Driver"));
            dto.setMountpoint(getJsonString(obj, "Mountpoint"));
            dto.setScope(getJsonString(obj, "Scope"));
            list.add(dto);
        }
        return list;
    }

    /**
     * 创建卷
     */
    public VolumeDTO createVolume(String name, String driver) {
        String[] cmd = dockerCmd("volume", "create", "--driver", driver, name);
        execToStringSilently(cmd);
        return inspectVolume(name);
    }

    /**
     * 创建本地卷
     */
    public VolumeDTO createLocalVolume(String name) {
        return createVolume(name, "local");
    }

    /**
     * 删除卷
     */
    public void removeVolume(String volumeName) {
        execToStringSilently(dockerCmd("volume", "rm", volumeName));
    }

    /**
     * 获取卷详细信息
     */
    public VolumeDTO inspectVolume(String volumeName) {
        String output = execToString(dockerCmd("volume", "inspect", volumeName));
        if (StringUtil.isBlank(output)) {
            return null;
        }
        JsonArray array = JsonUtil.parseArray(output);
        if (array.size() == 0) {
            return null;
        }
        JsonObject obj = array.getJsonObject(0);
        VolumeDTO dto = new VolumeDTO();
        dto.setName(getJsonString(obj, "Name"));
        dto.setDriver(getJsonString(obj, "Driver"));
        dto.setMountpoint(getJsonString(obj, "Mountpoint"));
        dto.setScope(getJsonString(obj, "Scope"));
        dto.setCreated(getJsonString(obj, "CreatedAt"));
        return dto;
    }

    /**
     * 清理未使用卷
     */
    public void pruneVolumes() {
        execToStringSilently(dockerCmd("volume", "prune", "-f"));
    }

    // ==================== 系统操作 ====================

    /**
     * 获取 Docker 版本
     */
    public String getVersion() {
        String output = execToString(dockerCmd("version", "--format", "{{json .}}"));
        if (StringUtil.isBlank(output)) {
            return null;
        }
        JsonObject obj = JsonUtil.parseObject(output);
        return getJsonString(obj, "Server", "Version");
    }

    /**
     * 获取 Docker 系统信息
     */
    public String getInfo() {
        return execToString(dockerCmd("info", "--format", "{{json .}}"));
    }

    /**
     * 检查 docker 是否可用
     */
    public boolean isDockerAvailable() {
        return execExitCode(dockerCmd("version")) == 0;
    }

    /**
     * 清理构建缓存
     */
    public void pruneBuildCache() {
        execToStringSilently(dockerCmd("builder", "prune", "-f"));
    }

    /**
     * 清理所有未使用资源
     */
    public void pruneAll() {
        execToStringSilently(dockerCmd("system", "prune", "-f", "--volumes"));
    }

    // ==================== Compose 操作 ====================

    /**
     * 启动 compose 服务
     */
    public void composeUp(String composeFile, String projectName) {
        List<String> cmd = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "compose", "-f", composeFile));
        if (StringUtil.isNotBlank(projectName)) {
            cmd.add("-p");
            cmd.add(projectName);
        }
        cmd.add("up");
        cmd.add("-d");
        execToStringSilently(cmd.toArray(new String[0]));
    }

    /**
     * 停止 compose 服务
     */
    public void composeDown(String composeFile, String projectName) {
        List<String> cmd = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "compose", "-f", composeFile));
        if (StringUtil.isNotBlank(projectName)) {
            cmd.add("-p");
            cmd.add(projectName);
        }
        cmd.add("down");
        execToStringSilently(cmd.toArray(new String[0]));
    }

    /**
     * 查看 compose 服务状态
     */
    public String composePs(String composeFile, String projectName) {
        List<String> cmd = new ArrayList<>(Arrays.asList("docker", "-H", dockerHost, "compose", "-f", composeFile));
        if (StringUtil.isNotBlank(projectName)) {
            cmd.add("-p");
            cmd.add(projectName);
        }
        cmd.add("ps");
        return execToString(cmd.toArray(new String[0]));
    }

    // ==================== 工具方法 ====================

    private String getJsonString(JsonObject obj, String... keys) {
        JsonObject current = obj;
        for (int i = 0; i < keys.length - 1; i++) {
            if (!current.containsKey(keys[i]) || current.get(keys[i]) == null) {
                return null;
            }
            current = current.getJsonObject(keys[i]);
        }
        String lastKey = keys[keys.length - 1];
        if (!current.containsKey(lastKey) || current.get(lastKey) == null) {
            return null;
        }
        Object v = current.get(lastKey); return v == null ? null : String.valueOf(v);
    }

    private double parsePercent(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(str.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseSize(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        str = str.trim().toUpperCase();
        try {
            if (str.endsWith("GB")) {
                return (long) (Double.parseDouble(str.replace("GB", "").trim()) * 1024 * 1024 * 1024);
            } else if (str.endsWith("MB")) {
                return (long) (Double.parseDouble(str.replace("MB", "").trim()) * 1024 * 1024);
            } else if (str.endsWith("KB")) {
                return (long) (Double.parseDouble(str.replace("KB", "").trim()) * 1024);
            } else if (str.endsWith("B")) {
                return Long.parseLong(str.replace("B", "").trim());
            } else if (str.endsWith("G")) {
                return (long) (Double.parseDouble(str.replace("G", "").trim()) * 1024 * 1024 * 1024);
            } else if (str.endsWith("M")) {
                return (long) (Double.parseDouble(str.replace("M", "").trim()) * 1024 * 1024);
            } else if (str.endsWith("K")) {
                return (long) (Double.parseDouble(str.replace("K", "").trim()) * 1024);
            }
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ==================== 资源统计 DTO ====================

    /**
     * 容器资源使用统计
     * <p>
     * 用于封装容器的 CPU、内存、网络、磁盘使用情况
     */
    public static class StatsDTO {
        private double cpuPercent;
        private double memoryPercent;
        private long memoryUsage;
        private long memoryLimit;
        private long networkRx;
        private long networkTx;
        private long blockRead;
        private long blockWrite;

        /**
         * getCpuPercent方法。
         *
         * @return double类型返回值
         */
        public double getCpuPercent() {
            return cpuPercent;
        }

        /**
         * setCpuPercent方法。
         * * @param cpuPercent double类型参数
         */
        public void setCpuPercent(double cpuPercent) {
            this.cpuPercent = cpuPercent;
        }

        /**
         * getMemoryPercent方法。
         *
         * @return double类型返回值
         */
        public double getMemoryPercent() {
            return memoryPercent;
        }

        /**
         * setMemoryPercent方法。
         * * @param memoryPercent double类型参数
         */
        public void setMemoryPercent(double memoryPercent) {
            this.memoryPercent = memoryPercent;
        }

        /**
         * getMemoryUsage方法。
         *
         * @return long类型返回值
         */
        public long getMemoryUsage() {
            return memoryUsage;
        }

        /**
         * setMemoryUsage方法。
         * * @param memoryUsage long类型参数
         */
        public void setMemoryUsage(long memoryUsage) {
            this.memoryUsage = memoryUsage;
        }

        /**
         * getMemoryLimit方法。
         *
         * @return long类型返回值
         */
        public long getMemoryLimit() {
            return memoryLimit;
        }

        /**
         * setMemoryLimit方法。
         * * @param memoryLimit long类型参数
         */
        public void setMemoryLimit(long memoryLimit) {
            this.memoryLimit = memoryLimit;
        }

        /**
         * getNetworkRx方法。
         *
         * @return long类型返回值
         */
        public long getNetworkRx() {
            return networkRx;
        }

        /**
         * setNetworkRx方法。
         * * @param networkRx long类型参数
         */
        public void setNetworkRx(long networkRx) {
            this.networkRx = networkRx;
        }

        /**
         * getNetworkTx方法。
         *
         * @return long类型返回值
         */
        public long getNetworkTx() {
            return networkTx;
        }

        /**
         * setNetworkTx方法。
         * * @param networkTx long类型参数
         */
        public void setNetworkTx(long networkTx) {
            this.networkTx = networkTx;
        }

        /**
         * getBlockRead方法。
         *
         * @return long类型返回值
         */
        public long getBlockRead() {
            return blockRead;
        }

        /**
         * setBlockRead方法。
         * * @param blockRead long类型参数
         */
        public void setBlockRead(long blockRead) {
            this.blockRead = blockRead;
        }

        /**
         * getBlockWrite方法。
         *
         * @return long类型返回值
         */
        public long getBlockWrite() {
            return blockWrite;
        }

        /**
         * setBlockWrite方法。
         * * @param blockWrite long类型参数
         */
        public void setBlockWrite(long blockWrite) {
            this.blockWrite = blockWrite;
        }

        /**
         * getNetworkRxFmt方法。
         *
         * @return String类型返回值
         */
        public String getNetworkRxFmt() {
            return formatSize(networkRx);
        }

        /**
         * getNetworkTxFmt方法。
         *
         * @return String类型返回值
         */
        public String getNetworkTxFmt() {
            return formatSize(networkTx);
        }

        /**
         * getBlockReadFmt方法。
         *
         * @return String类型返回值
         */
        public String getBlockReadFmt() {
            return formatSize(blockRead);
        }

        /**
         * getBlockWriteFmt方法。
         *
         * @return String类型返回值
         */
        public String getBlockWriteFmt() {
            return formatSize(blockWrite);
        }

        private String formatSize(long size) {
            if (size >= 1024 * 1024 * 1024) {
                return String.format("%.2fGB", size / (1024.0 * 1024 * 1024));
            } else if (size >= 1024 * 1024) {
                return String.format("%.2fMB", size / (1024.0 * 1024));
            } else if (size >= 1024) {
                return String.format("%.2fKB", size / 1024.0);
            }
            return size + "B";
        }
    }
}
