package com.zifang.util.core.state;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * StateMachine 单元测试。
 * 覆盖：flat 流、层级 composite、entry/exit 动作、guard、history、监听器、并发安全。
 */

/**
 * StateMachineTest类。
 */
public class StateMachineTest {

    // ==================== 基础 flat 流 ====================

    @Test
    /**
     * testFlat_linearFlow方法。
     */
    public void testFlat_linearFlow() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .from(S.B).on(E.GO_C).to(S.C)
                .from(S.C).on(E.GO_D).to(S.D)
                .build();
        assertEquals(S.A, sm.getCurrentState());
        sm.fire(E.GO_B, null);
        assertEquals(S.B, sm.getCurrentState());
        sm.fire(E.GO_C, null);
        assertEquals(S.C, sm.getCurrentState());
        sm.fire(E.GO_D, null);
        assertEquals(S.D, sm.getCurrentState());
    }

    @Test(expected = StateMachineException.class)
    /**
     * testFlat_noTransition_throws方法。
     */
    public void testFlat_noTransition_throws() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .build();
        sm.fire(E.GO_C, null); // A 没有 GO_C 的转移
    }

    @Test
    /**
     * testFlat_failFast_false_silent方法。
     */
    public void testFlat_failFast_false_silent() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .build();
        // failFast=false：无匹配转移静默忽略
        S result = sm.fire(E.GO_C, null, false);
        assertEquals(S.A, result);
    }

    @Test
    /**
     * testFlat_reset方法。
     */
    public void testFlat_reset() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .build();
        sm.fire(E.GO_B, null);
        sm.reset();
        assertEquals(S.A, sm.getCurrentState());
    }

    @Test(expected = StateMachineException.class)
    /**
     * testGuard_rejected_throws方法。
     */
    public void testGuard_rejected_throws() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).guard(ctx -> false).to(S.B)
                .build();
        sm.fire(E.GO_B, null);
    }

    @Test
    /**
     * testGuard_passed方法。
     */
    public void testGuard_passed() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).guard(ctx -> true).to(S.B)
                .build();
        sm.fire(E.GO_B, null);
        assertEquals(S.B, sm.getCurrentState());
    }

    // ==================== 守卫 ====================

    @Test
    /**
     * testGuard_combined_AND_semantics方法。
     */
    public void testGuard_combined_AND_semantics() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).guard(ctx -> true).guard(ctx -> false).to(S.B)
                .build();
        try {
            sm.fire(E.GO_B, null);
            fail("should throw");
        } catch (StateMachineException expected) {
            // guard true AND false = false
        }
    }

    @Test
    /**
     * testListener_endCalled方法。
     */
    public void testListener_endCalled() {
        AtomicInteger endCount = new AtomicInteger();
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .listener(new StateListener<S, E, Object>() {
                    @Override
                    public void onTransitionEnd(S from, S to, E event, Object ctx) {
                        endCount.incrementAndGet();
                    }
                })
                .build();
        sm.fire(E.GO_B, null);
        assertEquals(1, endCount.get());
    }

    @Test
    /**
     * testListener_refusedCalledOnGuardFailure方法。
     */
    public void testListener_refusedCalledOnGuardFailure() {
        List<String> events = new ArrayList<>();
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).guard(ctx -> false).to(S.B)
                .listener(new StateListener<S, E, Object>() {
                    @Override
                    public void onTransitionRefused(S s, E e, Object c, String r) {
                        events.add("refused");
                    }
                })
                .build();
        try {
            sm.fire(E.GO_B, null);
        } catch (StateMachineException ignored) {
        }
        assertEquals(Arrays.asList("refused"), events);
    }

    // ==================== Listener ====================

    @Test
    /**
     * testAction_throwsStateUnchanged方法。
     */
    public void testAction_throwsStateUnchanged() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B)
                .action((from, ctx) -> {
                    throw new RuntimeException("boom");
                })
                .to(S.B)
                .build();
        try {
            sm.fire(E.GO_B, null);
            fail("expected exception");
        } catch (StateMachineException ex) {
            assertTrue(ex.getCause() instanceof RuntimeException);
            assertEquals("boom", ex.getCause().getMessage());
        }
        // 状态未切换
        assertEquals(S.A, sm.getCurrentState());
    }

    @Test
    /**
     * testHierarchical_enterComposite_descendsToInitialChild方法。
     */
    public void testHierarchical_enterComposite_descendsToInitialChild() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                .build();
        sm.fire(OrderEvent.PAY, null);
        // 进入 PAID 应该自动下钻到 PENDING_INVOICE
        assertEquals(OrderState.PENDING_INVOICE, sm.getCurrentState());
        assertTrue(sm.isComposite(OrderState.PAID));
        assertFalse(sm.isComposite(OrderState.PENDING_INVOICE));
    }

    // ==================== Action 抛错处理 ====================

    @Test
    /**
     * testHierarchical_localTransition方法。
     */
    public void testHierarchical_localTransition() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                .build();
        sm.fire(OrderEvent.PAY, null);
        sm.fire(OrderEvent.ISSUE_INVOICE, null);
        // local transition：PENDING_INVOICE -> INVOICED
        assertEquals(OrderState.INVOICED, sm.getCurrentState());
    }

    // ==================== 层级状态：composite + 子态 ====================

    @Test
    /**
     * testHierarchical_externalTransition_exitsParent方法。
     */
    public void testHierarchical_externalTransition_exitsParent() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                // 外部转移：从 INVOICED（PAID 子态）出去到顶层 SHIPPED
                .from(OrderState.INVOICED).on(OrderEvent.CONFIRM).to(OrderState.SHIPPED)
                .build();
        sm.fire(OrderEvent.PAY, null);
        sm.fire(OrderEvent.ISSUE_INVOICE, null);
        sm.fire(OrderEvent.CONFIRM, null);
        assertEquals(OrderState.SHIPPED, sm.getCurrentState());
        // 不再在 PAID 里
        List<OrderState> ancestors = sm.ancestorsOf(OrderState.SHIPPED);
        assertFalse(ancestors.contains(OrderState.PAID));
    }

    @Test
    /**
     * testHierarchical_eventAtParent_appliesToAnyChild方法。
     */
    public void testHierarchical_eventAtParent_appliesToAnyChild() {
        // CANCEL 转移在 PAID 声明（没有具体 from），
        // 当子态收到 CANCEL 时，应该沿父链向上找到这条转移
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                // CANCEL 写在 PAID 上，子态的 CANCEL 会通过继承命中
                .from(OrderState.PAID).on(OrderEvent.CANCEL).to(OrderState.CANCELLED)
                .build();
        sm.fire(OrderEvent.PAY, null);
        // 当前在 PENDING_INVOICE
        sm.fire(OrderEvent.CANCEL, null);
        assertEquals(OrderState.CANCELLED, sm.getCurrentState());
    }

    @Test
    /**
     * testEntryExitActions_calledOnTransition方法。
     */
    public void testEntryExitActions_calledOnTransition() {
        List<String> log = new ArrayList<>();
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .state(OrderState.CREATED).exit((s, c) -> log.add("exit CREATED"))
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .state(OrderState.PAID).entry((s, c) -> log.add("enter PAID"))
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED))
                .state(OrderState.PENDING_INVOICE).entry((s, c) -> log.add("enter PENDING_INVOICE"))
                .build();
        sm.fire(OrderEvent.PAY, null);
        assertEquals(Arrays.asList("exit CREATED", "enter PAID", "enter PENDING_INVOICE"), log);
    }

    @Test
    /**
     * testTransitionAction_invokedAtLCA方法。
     */
    public void testTransitionAction_invokedAtLCA() {
        AtomicInteger lcaActionCalls = new AtomicInteger();
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                .from(OrderState.INVOICED).on(OrderEvent.CONFIRM)
                .action((from, ctx) -> {
                    // from 应该是 LCA，在 INVOICED 跟 SHIPPED 之间 LCA = null（顶层）
                    lcaActionCalls.incrementAndGet();
                    assertNull(from); // 跨顶层无 LCA
                })
                .to(OrderState.SHIPPED)
                .build();
        sm.fire(OrderEvent.PAY, null);
        sm.fire(OrderEvent.ISSUE_INVOICE, null);
        sm.fire(OrderEvent.CONFIRM, null);
        assertEquals(1, lcaActionCalls.get());
    }

    @Test
    /**
     * testAccept_returnsTargetWhenAllowed方法。
     */
    public void testAccept_returnsTargetWhenAllowed() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).guard(ctx -> true).to(S.B)
                .build();
        assertEquals(S.B, sm.accept(E.GO_B, null));
    }

    @Test
    /**
     * testAccept_returnsNullWhenGuardRejects方法。
     */
    public void testAccept_returnsNullWhenGuardRejects() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).guard(ctx -> false).to(S.B)
                .build();
        assertNull(sm.accept(E.GO_B, null));
    }

    // ==================== Entry / Exit 动作 ====================

    @Test
    /**
     * testHasTransition_walksParentChain方法。
     */
    public void testHasTransition_walksParentChain() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .from(S.B).on(E.GO_C).to(S.C)
                .build();
        assertTrue(sm.hasTransition(S.A, E.GO_B));
        assertTrue(sm.hasTransition(S.B, E.GO_C));
        assertFalse(sm.hasTransition(S.A, E.GO_C));
    }

    @Test
    /**
     * testHistory_restoresLastActiveSubstate方法。
     */
    public void testHistory_restoresLastActiveSubstate() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                        // 在 INVOICED 时收到 REENTER 跳到 H，H 恢复到上次激活的子态
                        .from(OrderState.INVOICED).on(OrderEvent.REENTER).to(OrderState.H)
                )
                .state(OrderState.H).history()
                .build();
        // 进入 PAID
        sm.fire(OrderEvent.PAY, null);
        // 推进到 INVOICED
        sm.fire(OrderEvent.ISSUE_INVOICE, null);
        assertEquals(OrderState.INVOICED, sm.getCurrentState());
        // 触发 REENTER，跳到 H。H 恢复到 PAID 上次激活的子态 = INVOICED
        sm.fire(OrderEvent.REENTER, null);
        assertEquals(OrderState.INVOICED, sm.getCurrentState());
        // 再来一次：从 INVOICED 跳到 H 仍然恢复到 INVOICED（同一个 leaf）
        sm.fire(OrderEvent.REENTER, null);
        assertEquals(OrderState.INVOICED, sm.getCurrentState());
    }

    // ==================== accept / hasTransition ====================

    @Test
    /**
     * testHistory_isHistoryFlagSet方法。
     */
    public void testHistory_isHistoryFlagSet() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .state(OrderState.H).history()
                .build();
        assertTrue(sm.isHistory(OrderState.H));
        assertFalse(sm.isHistory(OrderState.CREATED));
    }

    @Test
    /**
     * testHistory_shallow_restoresDirectSubstateOnly方法。
     */
    public void testHistory_shallow_restoresDirectSubstateOnly() {
        // 结构：OUT (composite, initial=MID) -> MID (composite, initial=A) -> {A, B}
        // H 是 OUT 的 history 状态 (SHALLOW)
        StateMachine<Sub, Evt2, Object> sm = StateMachine
                .<Sub, Evt2, Object>builder()
                .initial(Sub.OUT)
                .composite(Sub.OUT, Sub.MID, sub -> sub
                        .composite(Sub.MID, Sub.A, midSub -> midSub
                                .from(Sub.A).on(Evt2.A_TO_B).to(Sub.B)
                                .from(Sub.B).on(Evt2.B_TO_A).to(Sub.A)
                        )
                        .from(Sub.MID).on(Evt2.GO_HISTORY).to(Sub.H)
                )
                .state(Sub.H).history(HistoryType.SHALLOW)
                .build();
        // 初始：OUT -> MID -> A
        assertEquals(Sub.A, sm.getCurrentState());
        // A -> B
        sm.fire(Evt2.A_TO_B, null);
        assertEquals(Sub.B, sm.getCurrentState());
        // 此时 lastActiveSubstate[OUT] = B (深)；lastActiveSubstate[MID] = B
        // 触发 GO_HISTORY -> H（H 是 OUT 的 SHALLOW history）
        sm.fire(Evt2.GO_HISTORY, null);
        // SHALLOW：恢复到 OUT 的直接子态 = MID，再按 MID 的 initial 下钻 = A
        assertEquals(Sub.A, sm.getCurrentState());
    }

    @Test
    /**
     * testHistory_deep_restoresToLeaf方法。
     */
    public void testHistory_deep_restoresToLeaf() {
        // 同上结构，但 history 是 DEEP
        StateMachine<Sub, Evt2, Object> sm = StateMachine
                .<Sub, Evt2, Object>builder()
                .initial(Sub.OUT)
                .composite(Sub.OUT, Sub.MID, sub -> sub
                        .composite(Sub.MID, Sub.A, midSub -> midSub
                                .from(Sub.A).on(Evt2.A_TO_B).to(Sub.B)
                                .from(Sub.B).on(Evt2.B_TO_A).to(Sub.A)
                        )
                        .from(Sub.MID).on(Evt2.GO_HISTORY).to(Sub.H)
                )
                .state(Sub.H).history(HistoryType.DEEP)
                .build();
        // 初始：OUT -> MID -> A
        assertEquals(Sub.A, sm.getCurrentState());
        sm.fire(Evt2.A_TO_B, null);
        assertEquals(Sub.B, sm.getCurrentState());
        // DEEP：恢复到上次叶子 = B
        sm.fire(Evt2.GO_HISTORY, null);
        assertEquals(Sub.B, sm.getCurrentState());
    }

    // ==================== History（H）状态 ====================

    @Test
    /**
     * testFinal_rejectsAllEventsAfterEnter方法。
     */
    public void testFinal_rejectsAllEventsAfterEnter() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .end(S.D)
                .from(S.B).on(E.GO_C).to(S.C)
                .from(S.C).on(E.GO_D).to(S.D)
                .from(S.D).on(E.GO_B).to(S.B)
                .build();
        sm.fire(E.GO_B, null);
        sm.fire(E.GO_C, null);
        sm.fire(E.GO_D, null);
        assertTrue(sm.isCompleted());
        assertEquals(S.D, sm.getCurrentState());
        // 再发事件被拒收
        S result = sm.fire(E.GO_B, null, false);
        assertEquals(S.D, result);
    }

    @Test
    /**
     * testFinal_fireFastThrows方法。
     */
    public void testFinal_fireFastThrows() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .end(S.D)
                .from(S.A).on(E.GO_B).to(S.D)
                .build();
        sm.fire(E.GO_B, null);
        assertTrue(sm.isCompleted());
        try {
            sm.fire(E.GO_B, null);
            fail("expected exception");
        } catch (StateMachineException expected) { /* ok */ }
    }

    @Test
    /**
     * testFinal_listenerNotified方法。
     */
    public void testFinal_listenerNotified() {
        AtomicReference<Sub> completedAt = new AtomicReference<>();
        StateMachine<Sub, Evt2, Object> sm = StateMachine
                .<Sub, Evt2, Object>builder()
                .initial(Sub.OUT)
                .composite(Sub.OUT, Sub.MID, sub -> sub
                        .composite(Sub.MID, Sub.A, midSub -> midSub
                                .from(Sub.A).on(Evt2.A_TO_B).to(Sub.B)
                        )
                        .from(Sub.MID).on(Evt2.GO_HISTORY).to(Sub.H)
                )
                .state(Sub.H).end()
                .listener(new StateListener<Sub, Evt2, Object>() {
                    @Override
                    /**
                     * onStateMachineComplete方法。
                     *      * @param finalState Sub类型参数
                     * @param context Object类型参数
                     */
                    public void onStateMachineComplete(Sub finalState, Object context) {
                        completedAt.set(finalState);
                    }
                })
                .build();
        sm.fire(Evt2.A_TO_B, null);
        assertFalse(sm.isCompleted());
        sm.fire(Evt2.GO_HISTORY, null);
        assertTrue(sm.isCompleted());
        assertEquals(Sub.H, completedAt.get());
    }

    @Test
    /**
     * testFactory_createsIndependentInstances方法。
     */
    public void testFactory_createsIndependentInstances() {
        StateMachineFactory<S, E, Object> factory = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .from(S.B).on(E.GO_C).to(S.C)
                .buildFactory();

        StateMachine<S, E, Object> inst1 = factory.create();
        StateMachine<S, E, Object> inst2 = factory.create();

        assertEquals(S.A, inst1.getCurrentState());
        assertEquals(S.A, inst2.getCurrentState());

        inst1.fire(E.GO_B, null);
        assertEquals(S.B, inst1.getCurrentState());
        assertEquals(S.A, inst2.getCurrentState()); // inst2 不受影响

        inst2.fire(E.GO_B, null);
        inst2.fire(E.GO_C, null);
        assertEquals(S.C, inst2.getCurrentState());
        assertEquals(S.B, inst1.getCurrentState()); // inst1 不受影响
    }

    @Test
    /**
     * testFactory_independentLastActiveSubstate方法。
     */
    public void testFactory_independentLastActiveSubstate() {
        StateMachineFactory<Sub, Evt2, Object> factory = StateMachine
                .<Sub, Evt2, Object>builder()
                .initial(Sub.OUT)
                .composite(Sub.OUT, Sub.MID, sub -> sub
                        .composite(Sub.MID, Sub.A, midSub -> midSub
                                .from(Sub.A).on(Evt2.A_TO_B).to(Sub.B)
                        )
                        .from(Sub.MID).on(Evt2.GO_HISTORY).to(Sub.H)
                )
                .state(Sub.H).history(HistoryType.DEEP)
                .buildFactory();
        StateMachine<Sub, Evt2, Object> inst1 = factory.create();
        StateMachine<Sub, Evt2, Object> inst2 = factory.create();
        // inst1 推进到 B
        inst1.fire(Evt2.A_TO_B, null);
        // inst2 保持在 A
        inst1.fire(Evt2.GO_HISTORY, null);
        inst2.fire(Evt2.GO_HISTORY, null);
        assertEquals(Sub.B, inst1.getCurrentState()); // inst1 恢复到 B
        assertEquals(Sub.A, inst2.getCurrentState()); // inst2 恢复到 A
    }

    @Test
    /**
     * testChoice_picksFirstMatchingGuard方法。
     */
    public void testChoice_picksFirstMatchingGuard() {
        // CREATED 上 PAY 事件，按 ctx 大小分到不同目标
        StateMachine<OrderState, OrderEvent, Integer> sm = StateMachine
                .<OrderState, OrderEvent, Integer>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).choice()
                .when(ctx -> ctx > 1000, OrderState.PAID)
                .when(ctx -> ctx > 0, OrderState.CANCELLED)
                .otherwise(OrderState.SHIPPED)
                .end()
                .build();
        sm.fire(OrderEvent.PAY, 500);
        assertEquals(OrderState.CANCELLED, sm.getCurrentState());
    }

    // ==================== Final / End State ====================

    @Test
    /**
     * testChoice_factoryAcrossInstances方法。
     */
    public void testChoice_factoryAcrossInstances() {
        // 验证 choice 解析在 factory 创建的实例上独立工作
        StateMachineFactory<OrderState, OrderEvent, Integer> factory = StateMachine
                .<OrderState, OrderEvent, Integer>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).choice()
                .when(ctx -> ctx > 100, OrderState.PAID)
                .otherwise(OrderState.CANCELLED)
                .end()
                .buildFactory();
        StateMachine<OrderState, OrderEvent, Integer> inst1 = factory.create();
        StateMachine<OrderState, OrderEvent, Integer> inst2 = factory.create();
        inst1.fire(OrderEvent.PAY, 50);   // 否则
        inst2.fire(OrderEvent.PAY, 200);  // 命中 when
        assertEquals(OrderState.CANCELLED, inst1.getCurrentState());
        assertEquals(OrderState.PAID, inst2.getCurrentState());
    }

    @Test
    /**
     * testChoice_fallsThroughToOtherwise方法。
     */
    public void testChoice_fallsThroughToOtherwise() {
        StateMachine<OrderState, OrderEvent, Integer> sm = StateMachine
                .<OrderState, OrderEvent, Integer>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).choice()
                .when(ctx -> ctx > 10000, OrderState.PAID)
                .otherwise(OrderState.SHIPPED)
                .end()
                .build();
        sm.fire(OrderEvent.PAY, 100); // 都不命中 → otherwise
        assertEquals(OrderState.SHIPPED, sm.getCurrentState());
    }

    @Test(expected = StateMachineException.class)
    /**
     * testChoice_endWithoutOtherwiseThrows方法。
     */
    public void testChoice_endWithoutOtherwiseThrows() {
        StateMachine.<OrderState, OrderEvent, Integer>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).choice()
                .when(ctx -> ctx > 0, OrderState.PAID)
                // 故意漏掉 otherwise
                .end()
                .build();
    }

    // ==================== StateMachineFactory ====================

    @Test
    /**
     * testSnapshot_captureAndRestore方法。
     */
    public void testSnapshot_captureAndRestore() {
        StateMachineFactory<OrderState, OrderEvent, Object> factory = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                .buildFactory();
        StateMachine<OrderState, OrderEvent, Object> inst1 = factory.create();
        inst1.fire(OrderEvent.PAY, null);                    // -> PAID -> PENDING_INVOICE
        inst1.fire(OrderEvent.ISSUE_INVOICE, null);          // -> INVOICED
        StateMachineSnapshot<OrderState> snap = inst1.getSnapshot();
        assertEquals(OrderState.INVOICED, snap.getCurrentState());

        // 用一个全新的实例 + 同一个工厂恢复
        StateMachine<OrderState, OrderEvent, Object> inst2 = factory.create();
        assertEquals(OrderState.CREATED, inst2.getCurrentState());  // 新实例在初始
        inst2.restoreFromSnapshot(snap);
        assertEquals(OrderState.INVOICED, inst2.getCurrentState());
    }

    @Test
    /**
     * testSnapshot_inMemoryPersister方法。
     */
    public void testSnapshot_inMemoryPersister() {
        // 模拟一个内存版的 persister：用 HashMap 存快照
        StateMachineFactory<OrderState, OrderEvent, Object> factory = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .buildFactory();

        java.util.Map<String, StateMachineSnapshot<OrderState>> store = new java.util.HashMap<>();
        StateMachinePersister<OrderState, OrderEvent, Object, String> persister =
                new StateMachinePersister<OrderState, OrderEvent, Object, String>() {
                    @Override
                    /**
                     * persist方法。
                     *      * @param sm StateMachineOrderState,类型参数
                     * @param key String类型参数
                     */
                    public void persist(StateMachine<OrderState, OrderEvent, Object> sm, String key) {
                        store.put(key, sm.getSnapshot());
                    }

                    @Override
                    /**
                     * restore方法。
                     *      * @param sm StateMachineOrderState,类型参数
                     * @param key String类型参数
                     */
                    public void restore(StateMachine<OrderState, OrderEvent, Object> sm, String key) {
                        StateMachineSnapshot<OrderState> snap = store.get(key);
                        if (snap != null) sm.restoreFromSnapshot(snap);
                    }
                };

        // 模拟请求 1：创建实例 + fire + 持久化
        StateMachine<OrderState, OrderEvent, Object> req1 = factory.create();
        req1.fire(OrderEvent.PAY, null);
        assertEquals(OrderState.PAID, req1.getCurrentState());
        persister.persist(req1, "order-1");

        // 模拟请求 2：新建实例 + 恢复 + 继续
        StateMachine<OrderState, OrderEvent, Object> req2 = factory.create();
        assertEquals(OrderState.CREATED, req2.getCurrentState()); // 新实例
        persister.restore(req2, "order-1");
        assertEquals(OrderState.PAID, req2.getCurrentState());    // 恢复成功
    }

    // ==================== Choice State ====================

    @Test
    /**
     * testDot_containsStatesAndTransitions方法。
     */
    public void testDot_containsStatesAndTransitions() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .end(OrderState.DONE)
                .from(OrderState.PAID).on(OrderEvent.COMPLETE).to(OrderState.DONE)
                .build();
        String dot = sm.toDot();
        assertTrue(dot.contains("digraph StateMachine"));
        assertTrue(dot.contains("CREATED"));
        assertTrue(dot.contains("PAID"));
        assertTrue(dot.contains("DONE"));
        assertTrue(dot.contains("PAY"));
        assertTrue(dot.contains("COMPLETE"));
        // 终态应带 style=bold
        assertTrue(dot.contains("DONE\"") && dot.contains("style=bold"));
    }

    @Test
    /**
     * testDot_compositeUsesDoubleCircle方法。
     */
    public void testDot_compositeUsesDoubleCircle() {
        StateMachine<OrderState, OrderEvent, Object> sm = StateMachine
                .<OrderState, OrderEvent, Object>builder()
                .initial(OrderState.CREATED)
                .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
                .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
                        .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE).to(OrderState.INVOICED)
                )
                .build();
        String dot = sm.toDot();
        System.out.println("DOT:\n" + dot);
        assertTrue(dot.contains("\"PAID\"") && dot.contains("doublecircle"));
        assertTrue(dot.contains("\"PAID\" -> \"PENDING_INVOICE\""));
    }

    @Test
    /**
     * testDot_internalLabeled方法。
     */
    public void testDot_internalLabeled() {
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.TICK).internal()
                .build();
        String dot = sm.toDot();
        assertTrue(dot.contains("TICK (internal)"));
    }

    @Test
    /**
     * testParallelGroup_fireBothRegions方法。
     */
    public void testParallelGroup_fireBothRegions() {
        // 用统一 enum 表达所有 region 的状态；每个 region 只关心自己的子集
        ParallelStateGroup<UnifiedState, CommonEvent, Object> group = ParallelStateGroup
                .<UnifiedState, CommonEvent, Object>builder()
                .region("payment", StateMachine.<UnifiedState, CommonEvent, Object>builder()
                        .initial(UnifiedState.PAY_INIT)
                        .from(UnifiedState.PAY_INIT).on(CommonEvent.PAY).to(UnifiedState.PAY_DONE)
                        .end(UnifiedState.PAY_DONE)
                        .build())
                .region("shipping", StateMachine.<UnifiedState, CommonEvent, Object>builder()
                        .initial(UnifiedState.SHIP_INIT)
                        .from(UnifiedState.SHIP_INIT).on(CommonEvent.SHIP).to(UnifiedState.SHIP_DONE)
                        .end(UnifiedState.SHIP_DONE)
                        .build())
                .build();
        // fire PAY：payment region 响应，shipping region 忽略（无 PAY 转移）
        List<UnifiedState> payResult = group.fire(CommonEvent.PAY, null);
        assertEquals(UnifiedState.PAY_DONE, payResult.get(0));
        assertEquals(UnifiedState.SHIP_INIT, payResult.get(1));
        assertFalse(group.isAllCompleted()); // shipping 还没完成
        // fire SHIP：shipping 响应
        List<UnifiedState> shipResult = group.fire(CommonEvent.SHIP, null);
        assertEquals(UnifiedState.PAY_DONE, shipResult.get(0));
        assertEquals(UnifiedState.SHIP_DONE, shipResult.get(1));
        assertTrue(group.isAllCompleted());
    }

    // ==================== Persistence SPI ====================

    @Test
    /**
     * testParallelGroup_getCurrentStates方法。
     */
    public void testParallelGroup_getCurrentStates() {
        ParallelStateGroup<UnifiedState, CommonEvent, Object> group = ParallelStateGroup
                .<UnifiedState, CommonEvent, Object>builder()
                .region("r1", StateMachine.<UnifiedState, CommonEvent, Object>builder()
                        .initial(UnifiedState.OTHER_INIT)
                        .from(UnifiedState.OTHER_INIT).on(CommonEvent.OTHER).to(UnifiedState.OTHER_DONE)
                        .build())
                .region("r2", StateMachine.<UnifiedState, CommonEvent, Object>builder()
                        .initial(UnifiedState.SHIP_INIT)
                        .build())
                .build();
        assertEquals(2, group.regionCount());
        assertEquals(java.util.Arrays.asList("r1", "r2"), group.getRegionNames());
        List<UnifiedState> states = group.getCurrentStates();
        assertEquals(UnifiedState.OTHER_INIT, states.get(0));
        assertEquals(UnifiedState.SHIP_INIT, states.get(1));
    }

    @Test
    /**
     * testInternal_doesNotInvokeExitOrEntry方法。
     */
    public void testInternal_doesNotInvokeExitOrEntry() {
        List<String> log = new ArrayList<>();
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .state(S.A).exit((s, c) -> log.add("exit A"))
                .state(S.A).entry((s, c) -> log.add("enter A"))
                .from(S.A).on(E.TICK).action((from, c) -> log.add("tick action")).internal()
                .build();
        sm.fire(E.TICK, null);
        assertEquals(S.A, sm.getCurrentState());
        assertEquals(Arrays.asList("tick action"), log);
        sm.fire(E.TICK, null);
        assertEquals(S.A, sm.getCurrentState());
        assertEquals(Arrays.asList("tick action", "tick action"), log);
    }

    // ==================== DOT Export ====================

    @Test
    /**
     * testInternal_keepsLastActiveSubstate方法。
     */
    public void testInternal_keepsLastActiveSubstate() {
        // 内部转移不应覆盖 lastActiveSubstate，否则 H 恢复会失灵
        StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.TICK).internal()
                .from(S.A).on(E.GO_B).to(S.B)
                .build();
        sm.fire(E.TICK, null);
        sm.fire(E.GO_B, null);
        // 即使 internal 触发过，G 仍然能正常转移到 B
        assertEquals(S.B, sm.getCurrentState());
    }

    @Test
    /**
     * testConcurrent_fire_threadSafe方法。
     */
    public void testConcurrent_fire_threadSafe() throws InterruptedException {
        final StateMachine<S, E, Object> sm = StateMachine.<S, E, Object>builder()
                .initial(S.A)
                .from(S.A).on(E.GO_B).to(S.B)
                .from(S.B).on(E.GO_C).to(S.C)
                .from(S.C).on(E.GO_D).to(S.D)
                .from(S.D).on(E.RESET).to(S.A)
                .build();
        int threads = 16;
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    sm.fire(E.GO_B, null, false);
                    sm.fire(E.GO_C, null, false);
                    sm.fire(E.GO_D, null, false);
                    sm.fire(E.RESET, null, false);
                }
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        // 跑完所有 fire 之后状态必然是 A/B/C/D 之一（最终 RESET 把状态回到 A）
        S finalState = sm.getCurrentState();
        assertNotNull(finalState);
    }

    enum S {A, B, C, D}

    // ==================== Parallel State Group (fan-out) ====================

    enum E {GO_B, GO_C, GO_D, RESET, TICK}

    enum OrderState {
        CREATED, PAID,                 // 顶层：PAID 是 composite
        PENDING_INVOICE, INVOICED, H, // PAID 的子态：H 是 history 状态
        SHIPPED, DONE, CANCELLED       // 顶层叶子
    }

    enum OrderEvent {PAY, ISSUE_INVOICE, CONFIRM, SHIP, COMPLETE, CANCEL, REENTER}

    enum Sub {OUT, H, MID, A, B}

    // ==================== Internal Transition ====================

    enum Evt2 {A_TO_B, B_TO_A, GO_HISTORY}

    enum UnifiedState {PAY_INIT, PAY_DONE, SHIP_INIT, SHIP_DONE, OTHER_INIT, OTHER_DONE}

    // ==================== 并发安全 ====================

    enum CommonEvent {PAY, SHIP, OTHER}
}
