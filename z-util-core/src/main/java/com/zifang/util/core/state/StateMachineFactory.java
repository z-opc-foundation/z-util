package com.zifang.util.core.state;

/**
 * 状态机工厂：基于同一个不可变"配置模板"批量创建独立实例。
 * <p>
 * 业务上一个对象（如一笔订单）对应一个状态机实例。多个实例共享转移表/层级结构，
 * 但各自拥有独立的 currentState / lastActiveSubstate / listeners。
 * <p>
 * 典型用法：
 * <pre>{@code
 *   StateMachineFactory<OrderState, OrderEvent, OrderCtx> factory =
 *       StateMachine.<OrderState, OrderEvent, OrderCtx>builder()
 *           .initial(OrderState.CREATED)
 *           .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
 *           ...
 *           .buildFactory();
 *
 *   StateMachine<OrderState, OrderEvent, OrderCtx> order1 = factory.create();
 *   StateMachine<OrderState, OrderEvent, OrderCtx> order2 = factory.create();
 *   order1.fire(OrderEvent.PAY, ctx1);   // 不影响 order2
 *   order2.fire(OrderEvent.PAY, ctx2);
 * }</pre>
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public class StateMachineFactory<S, E, C> {

    private final StateMachine<S, E, C> template;

    StateMachineFactory(StateMachine<S, E, C> template) {
        this.template = template;
    }

    /**
     * 用默认初始状态创建一个新实例。
     */
    public StateMachine<S, E, C> create() {
        return template.newInstance();
    }

    /**
     * 取得模板（第一个被创建出来的实例，保留所有原始 listener）。
     * <p>
     * 注意：模板的 currentState 可能不是初始值（如果 build 之后被 fire 过）。
     * 若需要纯净模板，请直接用 {@link #create()}。
     */
    public StateMachine<S, E, C> getTemplate() {
        return template;
    }
}
