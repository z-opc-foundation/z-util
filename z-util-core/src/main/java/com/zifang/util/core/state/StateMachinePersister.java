package com.zifang.util.core.state;

/**
 * 状态机持久化 SPI：把状态机的运行时状态（{@link StateMachineSnapshot}）保存到
 * 任意外部存储（DB / Redis / 文件 / ZooKeeper 等），并能从存储恢复。
 * <p>
 * 典型用法：
 * <pre>{@code
 *   public class JdbcPersister<S, E, C, K> implements StateMachinePersister<S, E, C, K> {
 *       @Override public void persist(StateMachine<S, E, C> sm, K key) {
 *           StateMachineSnapshot<S> snap = sm.getSnapshot();
 *           // 把 snap 序列化成 JSON/字节，写入 orders 表
 *           ...
 *       }
 *       @Override public StateMachine<S, E, C> restore(StateMachine<S, E, C> template, K key) {
 *           // 从存储读出来，反序列化成 snapshot
 *           StateMachineSnapshot<S> snap = loadFromDb(key);
 *           sm.restoreFromSnapshot(snap);
 *           return sm;
 *       }
 *   }
 * }</pre>
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 * @param <K> 业务标识类型（如 orderId、processId）
 */
public interface StateMachinePersister<S, E, C, K> {

    /**
     * 把状态机的运行时快照保存到 key 对应的存储位置。
     */
    void persist(StateMachine<S, E, C> sm, K key);

    /**
     * 从 key 对应的存储位置恢复快照并应用到 sm（sm 是从 factory 拿到的空白实例）。
     * 如果没有持久化记录，业务可自行决定：保持初始状态 / 抛错 / 等等。
     */
    void restore(StateMachine<S, E, C> sm, K key);
}
