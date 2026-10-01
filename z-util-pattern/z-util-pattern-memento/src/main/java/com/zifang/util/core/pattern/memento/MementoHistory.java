package com.zifang.util.core.pattern.memento;

import java.util.*;
import java.util.function.Predicate;

/**
 * 备忘录历史管理器
 * <p>
 * 提供高级的撤销/重做历史管理功能：
 * <ul>
 *   <li>多级历史记录</li>
 *   <li>按条件过滤历史</li>
 *   <li>历史状态统计</li>
 *   <li>批量操作</li>
 *   <li>与Originator联动</li>
 * </ul>
 *
 * @param <T> 状态类型
 * @author zifang
 */
public class MementoHistory<T> {

    private final Originator<T> originator;
    private final MementoContext<T> context;
    private final Deque<T> undoStack = new ArrayDeque<>();
    private final Deque<T> redoStack = new ArrayDeque<>();
    private final List<HistoryListener<T>> listeners = new ArrayList<>();

    /**
     * MementoHistory方法。
     * * @param originator OriginatorT类型参数
     */
    public MementoHistory(Originator<T> originator) {
        this.originator = originator;
        this.context = new MementoContext<>();
    }

    /**
     * MementoHistory方法。
     * * @param originator OriginatorT类型参数
     *
     * @param maxHistorySize int类型参数
     */
    public MementoHistory(Originator<T> originator, int maxHistorySize) {
        this.originator = originator;
        this.context = new MementoContext<>(maxHistorySize);
    }

    private MementoHistory() {
        this.originator = null;
        this.context = new MementoContext<>();
    }

    /**
     * 执行操作并保存历史
     */
    public void execute(T newState) {
        T oldState = originator.getState();
        originator.setState(newState);
        context.save(newState);
        undoStack.push(oldState);
        redoStack.clear();
        notifyListeners(HistoryEvent.EXECUTE, oldState, newState);
    }

    /**
     * 执行操作（带标签）
     */
    public void execute(T newState, String label) {
        T oldState = originator.getState();
        originator.setState(newState);
        context.save(newState, label);
        undoStack.push(oldState);
        redoStack.clear();
        notifyListeners(HistoryEvent.EXECUTE, oldState, newState);
    }

    /**
     * 撤销
     */
    public boolean undo() {
        if (!canUndo()) {
            return false;
        }
        T currentState = originator.getState();
        T previousState = context.previous();
        originator.setState(previousState);
        redoStack.push(currentState);
        undoStack.pop();
        notifyListeners(HistoryEvent.UNDO, currentState, previousState);
        return true;
    }

    /**
     * 重做
     */
    public boolean redo() {
        if (!canRedo()) {
            return false;
        }
        T currentState = originator.getState();
        T nextState = context.next();
        originator.setState(nextState);
        undoStack.push(currentState);
        redoStack.pop();
        notifyListeners(HistoryEvent.REDO, currentState, nextState);
        return true;
    }

    /**
     * 是否可以撤销
     */
    public boolean canUndo() {
        return context.hasPrevious();
    }

    /**
     * 是否可以重做
     */
    public boolean canRedo() {
        return context.hasNext();
    }

    /**
     * 获取撤销栈大小
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }

    /**
     * 获取重做栈大小
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }

    /**
     * 清空历史
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        context.clear();
        notifyListeners(HistoryEvent.CLEAR, null, null);
    }

    /**
     * 获取历史快照列表
     */
    public List<MementoContext.SnapshotMeta> getHistory() {
        return context.getSnapshotMetas();
    }

    /**
     * 按条件过滤历史
     */
    public List<MementoContext.SnapshotMeta> filterHistory(Predicate<MementoContext.SnapshotMeta> predicate) {
        List<MementoContext.SnapshotMeta> result = new ArrayList<>();
        for (MementoContext.SnapshotMeta meta : context.getSnapshotMetas()) {
            if (predicate.test(meta)) {
                result.add(meta);
            }
        }
        return result;
    }

    /**
     * 跳转到指定索引
     */
    public boolean goTo(int index) {
        T state = context.moveTo(index);
        if (state != null) {
            originator.setState(state);
            return true;
        }
        return false;
    }

    /**
     * 跳转到指定标签
     */
    public Optional<T> goToLabel(String label) {
        Optional<T> state = context.moveToLabel(label);
        state.ifPresent(originator::setState);
        return state;
    }

    /**
     * 创建检查点
     */
    public String createCheckpoint(String name) {
        return context.createCheckpoint(name);
    }

    /**
     * 跳转到检查点
     */
    public Optional<T> goToCheckpoint(String name) {
        Optional<T> state = context.getCheckpoint(name);
        state.ifPresent(originator::setState);
        return state;
    }

    /**
     * 添加历史监听器
     */
    public void addListener(HistoryListener<T> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 移除历史监听器
     */
    public void removeListener(HistoryListener<T> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(HistoryEvent event, T fromState, T toState) {
        for (HistoryListener<T> listener : listeners) {
            try {
                listener.onEvent(event, fromState, toState);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 获取Originator
     */
    public Originator<T> getOriginator() {
        return originator;
    }

    /**
     * 获取Context
     */
    public MementoContext<T> getContext() {
        return context;
    }

    /**
     * 历史事件类型
     */
    public enum HistoryEvent {
        EXECUTE, UNDO, REDO, CLEAR
    }

    /**
     * 历史监听器
     */
    @FunctionalInterface
/**
 * HistoryListener接口。
 */
    public interface HistoryListener<T> {
        void onEvent(HistoryEvent event, T fromState, T toState);
    }
}