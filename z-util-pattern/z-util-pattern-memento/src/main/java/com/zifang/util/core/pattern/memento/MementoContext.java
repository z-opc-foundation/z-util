package com.zifang.util.core.pattern.memento;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 备忘录上下文（CareTaker）- 增强版
 * <p>
 * 功能特性：
 * <ul>
 *   <li>支持状态快照的标签和描述</li>
 *   <li>支持最大历史记录限制</li>
 *   <li>支持状态分支管理</li>
 *   <li>支持变更监听器</li>
 *   <li>支持批量操作</li>
 *   <li>支持检查点管理</li>
 *   <li>支持状态对比</li>
 * </ul>
 *
 * @param <T> 状态类型
 * @author zifang
 */
public class MementoContext<T> {

    private final List<Snapshot<T>> snapshots = new ArrayList<>();
    private final int maxSize;
    private final List<MementoListener<T>> listeners = new ArrayList<>();
    private int pointer = -1;
    private String currentBranch = "main";

    /**
     * MementoContext方法。
     */
    public MementoContext() {
        this(100);
    }

    /**
     * MementoContext方法。
     * * @param maxSize int类型参数
     */
    public MementoContext(int maxSize) {
        this.maxSize = maxSize > 0 ? maxSize : 100;
    }

    /**
     * 保存当前状态（简单模式）
     */
    public void save(T state) {
        save(state, null, null);
    }

    /**
     * 保存当前状态（带标签）
     */
    public void save(T state, String label) {
        save(state, label, null);
    }

    /**
     * 保存当前状态（完整模式）
     */
    public synchronized void save(T state, String label, String description) {
        Objects.requireNonNull(state, "State cannot be null");

        // 清除指针后的所有快照（重做历史）
        if (pointer < snapshots.size() - 1) {
            snapshots.subList(pointer + 1, snapshots.size()).clear();
        }

        // 如果超过最大限制，移除最早的快照
        while (snapshots.size() >= maxSize) {
            snapshots.remove(0);
            pointer--;
        }

        Snapshot<T> snapshot = new Snapshot<>(state, label, description, currentBranch);
        snapshots.add(snapshot);
        pointer = snapshots.size() - 1;

        notifyListeners(EventType.SAVE, snapshot);
    }

    /**
     * 前进到下一个状态（重做）
     */
    public synchronized T next() {
        if (!hasNext()) {
            throw new IndexOutOfBoundsException("Already at the last state, cannot move forward");
        }
        pointer++;
        Snapshot<T> snapshot = snapshots.get(pointer);
        notifyListeners(EventType.REDO, snapshot);
        return snapshot.getState();
    }

    /**
     * 后退到上一个状态（撤销）
     */
    public synchronized T previous() {
        if (!hasPrevious()) {
            throw new IndexOutOfBoundsException("Already at the first state, cannot move backward");
        }
        pointer--;
        Snapshot<T> snapshot = snapshots.get(pointer);
        notifyListeners(EventType.UNDO, snapshot);
        return snapshot.getState();
    }

    /**
     * 移动到指定索引位置
     */
    public synchronized T moveTo(int index) {
        if (index < 0 || index >= snapshots.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        pointer = index;
        Snapshot<T> snapshot = snapshots.get(pointer);
        notifyListeners(EventType.MOVE_TO, snapshot);
        return snapshot.getState();
    }

    /**
     * 移动到指定标签的快照
     */
    public synchronized Optional<T> moveToLabel(String label) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (Objects.equals(label, snapshots.get(i).getLabel())) {
                return Optional.of(moveTo(i));
            }
        }
        return Optional.empty();
    }

    /**
     * 获取当前状态
     */
    public synchronized T current() {
        if (pointer < 0 || pointer >= snapshots.size()) {
            return null;
        }
        return snapshots.get(pointer).getState();
    }

    /**
     * 获取当前快照
     */
    public synchronized Snapshot<T> currentSnapshot() {
        if (pointer < 0 || pointer >= snapshots.size()) {
            return null;
        }
        return snapshots.get(pointer);
    }

    /**
     * 是否可以前进
     */
    public boolean hasNext() {
        return pointer < snapshots.size() - 1;
    }

    /**
     * 是否可以后退
     */
    public boolean hasPrevious() {
        return pointer > 0;
    }

    /**
     * 获取快照总数
     */
    public int size() {
        return snapshots.size();
    }

    /**
     * 获取当前指针位置
     */
    public int getPointer() {
        return pointer;
    }

    /**
     * 获取最大容量
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 获取已用容量
     */
    public int getUsedSize() {
        return snapshots.size();
    }

    /**
     * 清空所有快照
     */
    public synchronized void clear() {
        snapshots.clear();
        pointer = -1;
        notifyListeners(EventType.CLEAR, null);
    }

    /**
     * 获取所有快照
     */
    public synchronized List<Snapshot<T>> getAllSnapshots() {
        return new ArrayList<>(snapshots);
    }

    /**
     * 获取快照列表（不包含状态）
     */
    public synchronized List<SnapshotMeta> getSnapshotMetas() {
        List<SnapshotMeta> metas = new ArrayList<>();
        for (int i = 0; i < snapshots.size(); i++) {
            Snapshot<T> s = snapshots.get(i);
            metas.add(new SnapshotMeta(i, s.getLabel(), s.getDescription(), s.getBranch(), s.getTimestamp(), i == pointer));
        }
        return metas;
    }

    /**
     * 跳转到第一个状态
     */
    public synchronized T first() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return moveTo(0);
    }

    /**
     * 跳转到最后一个状态
     */
    public synchronized T last() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return moveTo(snapshots.size() - 1);
    }

    /**
     * 前进N步
     */
    public synchronized T stepForward(int steps) {
        int target = Math.min(pointer + steps, snapshots.size() - 1);
        return moveTo(target);
    }

    /**
     * 后退N步
     */
    public synchronized T stepBackward(int steps) {
        int target = Math.max(pointer - steps, 0);
        return moveTo(target);
    }

    /**
     * 创建检查点
     */
    public synchronized String createCheckpoint(String name) {
        T state = current();
        if (state == null) {
            throw new IllegalStateException("No current state to create checkpoint");
        }
        save(state, "[Checkpoint] " + name, "Checkpoint: " + name);
        return name;
    }

    /**
     * 获取检查点
     */
    public synchronized Optional<T> getCheckpoint(String name) {
        return moveToLabel("[Checkpoint] " + name);
    }

    /**
     * 添加监听器
     */
    public void addListener(MementoListener<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 移除监听器
     */
    public void removeListener(MementoListener<T> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(EventType type, Snapshot<T> snapshot) {
        for (MementoListener<T> listener : listeners) {
            try {
                listener.onEvent(type, snapshot, pointer);
            } catch (Exception e) {
                // 防止监听器异常影响主流程
            }
        }
    }

    /**
     * 获取当前分支
     */
    public String getCurrentBranch() {
        return currentBranch;
    }

    /**
     * 切换分支
     */
    public synchronized void switchBranch(String branch) {
        this.currentBranch = branch;
    }

    /**
     * 事件类型
     */
    public enum EventType {
        SAVE, UNDO, REDO, MOVE_TO, CLEAR
    }

    /**
     * 备忘录监听器
     */
    @FunctionalInterface
/**
 * MementoListener接口。
 */
    public interface MementoListener<T> {
        void onEvent(EventType event, Snapshot<T> snapshot, int pointer);
    }

    /**
     * 快照元数据（不包含状态）
     */
    public static class SnapshotMeta {
        private final int index;
        private final String label;
        private final String description;
        private final String branch;
        private final long timestamp;
        private final boolean isCurrent;

        /**
         * SnapshotMeta方法。
         * * @param index int类型参数
         *
         * @param label       String类型参数
         * @param description String类型参数
         * @param branch      String类型参数
         * @param timestamp   long类型参数
         * @param isCurrent   boolean类型参数
         */
        public SnapshotMeta(int index, String label, String description, String branch, long timestamp, boolean isCurrent) {
            this.index = index;
            this.label = label;
            this.description = description;
            this.branch = branch;
            this.timestamp = timestamp;
            this.isCurrent = isCurrent;
        }

        /**
         * getIndex方法。
         *
         * @return int类型返回值
         */
        public int getIndex() {
            return index;
        }

        /**
         * getLabel方法。
         *
         * @return String类型返回值
         */
        public String getLabel() {
            return label;
        }

        /**
         * getDescription方法。
         *
         * @return String类型返回值
         */
        public String getDescription() {
            return description;
        }

        /**
         * getBranch方法。
         *
         * @return String类型返回值
         */
        public String getBranch() {
            return branch;
        }

        /**
         * getTimestamp方法。
         *
         * @return long类型返回值
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * isCurrent方法。
         *
         * @return boolean类型返回值
         */
        public boolean isCurrent() {
            return isCurrent;
        }
    }

    /**
     * 快照（备忘录）
     */
    public static class Snapshot<T> {
        private final T state;
        private final String label;
        private final String description;
        private final String branch;
        private final long timestamp;

        private Snapshot(T state, String label, String description, String branch) {
            this.state = state;
            this.label = label;
            this.description = description;
            this.branch = branch;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * getState方法。
         *
         * @return T类型返回值
         */
        public T getState() {
            return state;
        }

        /**
         * getLabel方法。
         *
         * @return String类型返回值
         */
        public String getLabel() {
            return label;
        }

        /**
         * getDescription方法。
         *
         * @return String类型返回值
         */
        public String getDescription() {
            return description;
        }

        /**
         * getBranch方法。
         *
         * @return String类型返回值
         */
        public String getBranch() {
            return branch;
        }

        /**
         * getTimestamp方法。
         *
         * @return long类型返回值
         */
        public long getTimestamp() {
            return timestamp;
        }
    }
}
