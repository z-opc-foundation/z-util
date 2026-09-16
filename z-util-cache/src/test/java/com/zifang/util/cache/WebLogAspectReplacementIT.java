package com.zifang.util.cache;

import com.zifang.util.aop.Advise;
import com.zifang.util.aop.Intercept;
import com.zifang.util.aop.ProxyFactory;
import com.zifang.util.cache.Cache;
import com.zifang.util.cache.CacheBuilder;
import com.zifang.util.cache.decorator.MeteredCache;
import com.zifang.util.core.trace.TraceContextHolder;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 端到端场景 1：模拟 z-opc 的 WebLogAspect（替换 z-wf / z-config 里的 aspectj）。
 * <p>
 * 原 z-opc 用 {@code @Aspect} + aspectj 织入；现在用 {@code @Intercept} + z-util-aop 自研代理。
 * 配套：z-util-trace（traceId 日志上下文）+ z-util-cache.MeteredCache（统计 QPS）。
 *
 * <h3>原 z-opc 写法（aspectj）</h3>
 * <pre>{@code
 *   @Aspect
 *   @Component
 *   public class WebLogAspect {
 *       @Pointcut("execution(* com.zifang..*Controller.*(..))")
 *       public void log() {}
 *       @Around("log()")
 *       public Object around(ProceedingJoinPoint pjp) throws Throwable {
 *           long t = System.currentTimeMillis();
 *           Object r = pjp.proceed();
 *           log.info("{} {}ms", pjp.getSignature(), System.currentTimeMillis() - t);
 *           return r;
 *       }
 *   }
 * }</pre>
 *
 * <h3>z-util 自研版（无 aspectj）</h3>
 * 业务方法加 {@code @Intercept(WebLogAdvise.class)}，再 {@code ProxyFactory.wrap} 即可。
 */
public class WebLogAspectReplacementIT {

    @Test
    public void testWebLogAspect_withTraceAndMeter() {
        WebLogAdvise.LOG.clear();
        UserController controller = ProxyFactory.wrap(new UserControllerImpl());
        Cache<String, String> tokenCache = new MeteredCache<>(
                CacheBuilder.<String, String>newBuilder()
                        .name("session-cache")
                        .maximumSize(100)
                        .expireAfterAccess(java.time.Duration.ofMinutes(30))
                        .recordStats()
                        .build());

        String token = controller.login("alice", "p@ss");
        assertNotNull(token);
        tokenCache.put(token, "alice");
        String alice = tokenCache.get(token);
        assertEquals("alice", alice);

        assertEquals(1, WebLogAdvise.LOG.size());
        String log = WebLogAdvise.LOG.get(0);
        assertTrue("log should contain method signature: " + log, log.contains("UserController.login"));
        assertTrue("log should contain traceId: " + log, log.contains("trace="));
        assertTrue("log should contain elapsed ms: " + log, log.contains("ms="));

        MeteredCache<String, String> metered = (MeteredCache<String, String>) tokenCache;
        assertEquals(1, metered.meter().putCount());
        assertEquals(1, metered.meter().hitCount());
        assertEquals(0, metered.meter().missCount());
    }

    public interface UserController {
        String login(String username, String password);
    }

    public static class UserControllerImpl implements UserController {
        @Override
        @Intercept(WebLogAdvise.class)
        public String login(String username, String password) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) { /* */ }
            return "session-" + username + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    /**
     * 自研 advice：等价于原 aspectj @Around。
     */
    public static class WebLogAdvise implements Advise<UserController> {
        static final List<String> LOG = new ArrayList<>();

        @Override
        public Object around(UserController target, Method method, Object[] args, Advise.Chain chain) throws Throwable {
            TraceContextHolder.startNew();
            long t0 = System.nanoTime();
            String signature = method.getDeclaringClass().getSimpleName() + "." + method.getName();
            try {
                Object r = chain.proceed();
                long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                LOG.add(String.format("%s ms=%d trace=%s",
                        signature, elapsed, TraceContextHolder.currentTraceId()));
                return r;
            } finally {
                TraceContextHolder.stop();
            }
        }
    }
}
