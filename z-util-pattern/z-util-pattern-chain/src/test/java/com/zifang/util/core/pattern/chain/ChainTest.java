package com.zifang.util.core.pattern.chain;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 链执行模式测试
 */

/**
 * ChainTest类。
 */
public class ChainTest {

    @Test
    /**
     * testChainContextBasic方法。
     */
    public void testChainContextBasic() {
        ChainContext<String, Object> context = ChainContext.create();
        context.put("key1", "value1");
        context.put("key2", 123);

        assertEquals("value1", context.get("key1"));
        assertEquals(123, context.get("key2"));
        assertEquals(2, context.size());
        assertFalse(context.isExecuted());
    }

    @Test
    /**
     * testChainContextWithInitialData方法。
     */
    public void testChainContextWithInitialData() {
        Map<String, Object> initial = new HashMap<>();
        initial.put("name", "test");
        initial.put("count", 10);

        ChainContext<String, Object> context = ChainContext.create(initial);
        assertEquals("test", context.get("name"));
        assertEquals(10, context.get("count"));
    }

    @Test
    /**
     * testChainContextTypedGet方法。
     */
    public void testChainContextTypedGet() {
        ChainContext<String, Object> context = ChainContext.create();
        context.put("stringVal", "hello");
        context.put("intVal", 42);

        assertEquals("hello", context.get("stringVal", String.class));
        assertEquals(Integer.valueOf(42), context.get("intVal", Integer.class));
    }

    @Test
    /**
     * testProcessorFromConsumer方法。
     */
    public void testProcessorFromConsumer() {
        AtomicInteger counter = new AtomicInteger(0);
        Processor<ChainContext<String, Object>> processor = Processor.fromConsumer(ctx -> {
            counter.incrementAndGet();
            ctx.put("processed", true);
        });

        ChainContext<String, Object> context = ChainContext.create();
        ProcessorResult result = processor.process(context);

        assertEquals(ProcessorResult.CONTINUE, result);
        assertEquals(1, counter.get());
        assertTrue((Boolean) context.get("processed"));
    }

    @Test
    /**
     * testProcessorWhen方法。
     */
    public void testProcessorWhen() {
        Processor<ChainContext<String, Object>> processor = Processor.when(
                ctx -> ctx.containsKey("condition"),
                ctx -> {
                    ctx.put("processed", true);
                    return ProcessorResult.CONTINUE;
                }
        );

        ChainContext<String, Object> context1 = ChainContext.create();
        context1.put("condition", true);
        assertEquals(ProcessorResult.CONTINUE, processor.process(context1));
        assertTrue((Boolean) context1.get("processed"));

        ChainContext<String, Object> context2 = ChainContext.create();
        assertEquals(ProcessorResult.SKIP, processor.process(context2));
        assertNull(context2.get("processed"));
    }

    @Test
    /**
     * testProcessorAndThen方法。
     */
    public void testProcessorAndThen() {
        AtomicInteger counter = new AtomicInteger(0);

        Processor<ChainContext<String, Object>> p1 = ctx -> {
            counter.incrementAndGet();
            return ProcessorResult.CONTINUE;
        };
        Processor<ChainContext<String, Object>> p2 = ctx -> {
            counter.incrementAndGet();
            return ProcessorResult.CONTINUE;
        };

        Processor<ChainContext<String, Object>> combined = p1.andThen(p2);

        ChainContext<String, Object> context = ChainContext.create();
        combined.process(context);

        assertEquals(2, counter.get());
    }

    @Test
    /**
     * testSimpleChain方法。
     */
    public void testSimpleChain() {
        AtomicInteger counter = new AtomicInteger(0);
        Chain<ChainContext<String, Object>> chain = Chain.<ChainContext<String, Object>>builder()
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.FINISHED;
                })
                .build();

        ChainContext<String, Object> context = ChainContext.create();
        ProcessorResult result = chain.process(context);

        assertEquals(ProcessorResult.FINISHED, result);
        assertEquals(3, counter.get());
    }

    @Test
    /**
     * testSimpleChainContinue方法。
     */
    public void testSimpleChainContinue() {
        AtomicInteger counter = new AtomicInteger(0);
        Chain<ChainContext<String, Object>> chain = Chain.<ChainContext<String, Object>>builder()
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .build();

        ChainContext<String, Object> context = ChainContext.create();
        ProcessorResult result = chain.process(context);

        assertEquals(ProcessorResult.CONTINUE, result);
        assertEquals(2, counter.get());
    }

    @Test
    /**
     * testChainBuilderBasic方法。
     */
    public void testChainBuilderBasic() {
        AtomicInteger counter = new AtomicInteger(0);
        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create("testChain")
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .build();

        ChainContext<String, Object> context = ChainContext.create();
        chain.process(context);

        assertEquals(2, counter.get());
    }

    @Test
    /**
     * testChainBuilderWithCondition方法。
     */
    public void testChainBuilderWithCondition() {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean conditionMet = new AtomicBoolean(false);

        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .addWhen(ctx -> conditionMet.get(), ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .add(ctx -> {
                    counter.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .build();

        ChainContext<String, Object> context = ChainContext.create();

        // First, condition is false
        chain.process(context);
        assertEquals(1, counter.get());

        // Now set condition to true
        conditionMet.set(true);
        counter.set(0);
        chain.process(context);
        assertEquals(2, counter.get());
    }

    @Test
    /**
     * testChainBuilderWithFilter方法。
     */
    public void testChainBuilderWithFilter() {
        AtomicInteger beforeCounter = new AtomicInteger(0);
        AtomicInteger afterCounter = new AtomicInteger(0);
        AtomicInteger processCounter = new AtomicInteger(0);

        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .addFilter(
                        ctx -> beforeCounter.incrementAndGet(),
                        ctx -> afterCounter.incrementAndGet(),
                        (ctx, result) -> {
                            processCounter.incrementAndGet();
                            return ProcessorResult.CONTINUE;
                        }
                )
                .build();

        ChainContext<String, Object> context = ChainContext.create();
        chain.process(context);

        assertEquals(1, beforeCounter.get());
        assertEquals(1, processCounter.get());
        assertEquals(1, afterCounter.get());
    }

    @Test
    /**
     * testChainBuilderBranch方法。
     */
    public void testChainBuilderBranch() {
        AtomicBoolean isAdmin = new AtomicBoolean(false);
        AtomicInteger adminCounter = new AtomicInteger(0);
        AtomicInteger userCounter = new AtomicInteger(0);

        BranchChain<ChainContext<String, Object>> branchChain = new BranchChain<>(
                ctx -> Boolean.TRUE.equals(ctx.get("isAdmin"))
        );
        branchChain.whenTrue(ctx -> {
            adminCounter.incrementAndGet();
            return ProcessorResult.CONTINUE;
        });
        branchChain.whenFalse(ctx -> {
            userCounter.incrementAndGet();
            return ProcessorResult.CONTINUE;
        });

        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .add(branchChain)
                .build();

        // Test user path
        ChainContext<String, Object> userContext = ChainContext.create();
        userContext.put("isAdmin", false);
        chain.process(userContext);
        assertEquals(0, adminCounter.get());
        assertEquals(1, userCounter.get());

        // Test admin path
        userCounter.set(0);
        ChainContext<String, Object> adminContext = ChainContext.create();
        adminContext.put("isAdmin", true);
        chain.process(adminContext);
        assertEquals(1, adminCounter.get());
        assertEquals(0, userCounter.get());
    }

    @Test
    /**
     * testChainExecutor方法。
     */
    public void testChainExecutor() {
        ChainExecutor<ChainContext<String, Object>> executor = new ChainExecutor<>();

        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    ctx.put("step1", "done");
                    return ProcessorResult.CONTINUE;
                })
                .add(ctx -> {
                    ctx.put("step2", "done");
                    return ProcessorResult.CONTINUE;
                })
                .build();

        executor.register("testChain", chain);

        ChainContext<String, Object> context = ChainContext.create();
        ProcessorResult result = executor.execute("testChain", context);

        assertEquals(ProcessorResult.CONTINUE, result);
        assertEquals("done", context.get("step1"));
        assertEquals("done", context.get("step2"));
    }

    @Test
    /**
     * testChainExecutorWithListener方法。
     */
    public void testChainExecutorWithListener() {
        AtomicBoolean beforeCalled = new AtomicBoolean(false);
        AtomicBoolean afterCalled = new AtomicBoolean(false);

        ChainExecutor<ChainContext<String, Object>> executor = new ChainExecutor<>();
        executor.addListener(new ChainExecutor.ChainListenerAdapter<ChainContext<String, Object>>() {
            @Override
            /**
             * onBeforeExecution方法。
             *      * @param chain ChainChainContextString,类型参数
             * @param context ChainContextString,类型参数
             */
            public void onBeforeExecution(Chain<ChainContext<String, Object>> chain, ChainContext<String, Object> context) {
                beforeCalled.set(true);
            }

            @Override
            /**
             * onAfterExecution方法。
             *      * @param chain ChainChainContextString,类型参数
             * @param context ChainContextString,类型参数
             * @param result ProcessorResult类型参数
             * @param duration long类型参数
             */
            public void onAfterExecution(Chain<ChainContext<String, Object>> chain,
                                         ChainContext<String, Object> context,
                                         ProcessorResult result, long duration) {
                afterCalled.set(true);
            }
        });

        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    return ProcessorResult.CONTINUE;
                })
                .build();
        executor.execute(chain, ChainContext.create());

        assertTrue(beforeCalled.get());
        assertTrue(afterCalled.get());
    }

    @Test
    /**
     * testChainExecutorMetrics方法。
     */
    public void testChainExecutorMetrics() {
        ChainExecutor.MetricsChainListener<ChainContext<String, Object>> metrics =
                new ChainExecutor.MetricsChainListener<>();

        ChainExecutor<ChainContext<String, Object>> executor = new ChainExecutor<>();
        executor.addListener(metrics);

        String chainName = "testChain";
        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create(chainName)
                .add(ctx -> {
                    return ProcessorResult.CONTINUE;
                })
                .build();
        executor.register(chainName, chain);
        executor.execute(chainName, ChainContext.create());
        executor.execute(chainName, ChainContext.create());

        assertEquals(2, metrics.getExecutionCount(chainName));
        assertEquals(0, metrics.getFailureCount(chainName));
        assertTrue(metrics.getAverageDuration(chainName) >= 0);
    }

    @Test
    /**
     * testProcessorResult方法。
     */
    public void testProcessorResult() {
        assertTrue(ProcessorResult.CONTINUE.shouldContinue());
        assertTrue(ProcessorResult.SKIP.shouldContinue());
        assertFalse(ProcessorResult.FINISHED.shouldContinue());
        assertFalse(ProcessorResult.FAILURE.shouldContinue());
        assertFalse(ProcessorResult.ERROR.shouldContinue());

        assertTrue(ProcessorResult.CONTINUE.isSuccess());
        assertTrue(ProcessorResult.SKIP.isSuccess());
        assertTrue(ProcessorResult.FINISHED.isSuccess());
        assertFalse(ProcessorResult.FAILURE.isSuccess());
        assertFalse(ProcessorResult.ERROR.isSuccess());

        assertTrue(ProcessorResult.FINISHED.isFinished());
        assertFalse(ProcessorResult.CONTINUE.isFinished());
    }

    @Test
    /**
     * testFilterProcessor方法。
     */
    public void testFilterProcessor() {
        AtomicInteger before = new AtomicInteger(0);
        AtomicInteger after = new AtomicInteger(0);
        AtomicInteger process = new AtomicInteger(0);

        FilterProcessor<ChainContext<String, Object>> filter = FilterProcessor.of(
                ctx -> before.incrementAndGet(),
                ctx -> after.incrementAndGet()
        );

        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    process.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .add(filter)
                .build();

        ChainContext<String, Object> context = ChainContext.create();
        chain.process(context);

        assertEquals(1, before.get());
        assertEquals(1, after.get());
        assertEquals(1, process.get());
    }

    @Test
    /**
     * testChainAppend方法。
     */
    public void testChainAppend() {
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);

        Chain<ChainContext<String, Object>> chain1 = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    counter1.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .build();
        Chain<ChainContext<String, Object>> chain2 = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    counter2.incrementAndGet();
                    return ProcessorResult.CONTINUE;
                })
                .build();

        Chain<ChainContext<String, Object>> combined = chain1.append(chain2);

        ChainContext<String, Object> context = ChainContext.create();
        combined.process(context);

        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    /**
     * testChainExecuteWithResult方法。
     */
    public void testChainExecuteWithResult() {
        Chain<ChainContext<String, Object>> chain = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    ctx.put("result", "success");
                    return ProcessorResult.FINISHED;
                })
                .build();

        ChainContext<String, Object> context = ChainContext.create();
        Chain.ChainResult<ChainContext<String, Object>> result = chain.executeWithResult(context);

        assertTrue(result.isSuccess());
        assertTrue(result.isFinished());
        assertFalse(result.isContinued());
        assertEquals("success", context.get("result"));
    }

    @Test
    /**
     * testChainToString方法。
     */
    public void testChainToString() {
        Chain<ChainContext<String, Object>> chain = Chain.named("myChain");
        assertTrue(chain.toString().contains("myChain"));
    }

    @Test
    /**
     * testChainIsEmpty方法。
     */
    public void testChainIsEmpty() {
        Chain<ChainContext<String, Object>> emptyChain = Chain.empty();
        assertTrue(emptyChain.isEmpty());
        assertEquals(0, emptyChain.size());

        Chain<ChainContext<String, Object>> nonEmptyChain = ChainBuilder.<ChainContext<String, Object>>create()
                .add(ctx -> {
                    return ProcessorResult.CONTINUE;
                })
                .build();
        assertFalse(nonEmptyChain.isEmpty());
        assertEquals(1, nonEmptyChain.size());
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testChainExecutorThrowsOnMissingChain方法。
     */
    public void testChainExecutorThrowsOnMissingChain() {
        ChainExecutor<ChainContext<String, Object>> executor = new ChainExecutor<>();
        executor.execute("nonExistent", ChainContext.create());
    }

    @Test
    /**
     * testNamedProcessor方法。
     */
    public void testNamedProcessor() {
        AtomicBoolean executed = new AtomicBoolean(false);
        NamedProcessor<ChainContext<String, Object>> named = new NamedProcessor<>(
                "myProcessor",
                ctx -> {
                    executed.set(true);
                    return ProcessorResult.CONTINUE;
                }
        );

        assertEquals("myProcessor", named.getName());

        ChainContext<String, Object> context = ChainContext.create();
        named.process(context);

        assertTrue(executed.get());
    }
}
