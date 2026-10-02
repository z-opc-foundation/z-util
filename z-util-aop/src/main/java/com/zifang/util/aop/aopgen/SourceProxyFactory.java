package com.zifang.util.aop.aopgen;

import com.zifang.util.bc.compile.CFJavaCompiler;
import com.zifang.util.bc.compile.BytesJavaFileObject;
import com.zifang.util.bc.compile.MapClassLoader;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 源码级代理工厂：拿现成类当参考，生成子类源码并在每个可覆写方法前后织入
 * {@link MethodHook} 调用，异常路径走 {@link MethodHook#afterException(String, Throwable)}。
 * 内存编译后用自定义 ClassLoader 定义并实例化。
 * <p>
 * 链路 = 解析目标类 → 源码制造 → 字节码 → 加载执行（制造字节码能力的一等用户）。
 * <p>
 * 类定义注入三条路径（按优先级）：代理类必须与目标同一个定义 ClassLoader——
 * JVM 按加载器划运行时包，包私有目标只有同运行时包才允许被子类化（cglib 同理）。
 * <ol>
 *   <li>Lookup.defineClass（JDK 9+）：privateLookupIn 进目标包，定义进目标自己的加载器；</li>
 *   <li>反射 ClassLoader.defineClass（Java 8：无模块系统可直接 setAccessible）；</li>
 *   <li>MapClassLoader 兜底：独立加载器定义，仅 public 目标可用。</li>
 * </ol>
 * <p>
 * 范围：仅目标类自身声明的 public 非 static 非 final 非 synthetic 方法；
 * final/static/私有方法不可覆写，走继承原样生效。
 */
public class SourceProxyFactory {

    private static final AtomicLong COUNTER = new AtomicLong();

    private SourceProxyFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxy(T target, MethodHook hook) {
        Class<?> targetClass = target.getClass();
        String proxyFqn = targetClass.getName() + "$SourceProxy" + COUNTER.incrementAndGet();
        String source = buildSource(targetClass, proxyFqn);

        Map<String, BytesJavaFileObject> compiled;
        try {
            compiled = CFJavaCompiler.compile(proxyFqn, source, classpathFor(targetClass, MethodHook.class));
        } catch (Exception e) {
            throw new RuntimeException("代理源码编译失败: " + proxyFqn, e);
        }
        if (!compiled.containsKey(proxyFqn)) {
            throw new RuntimeException("代理源码编译无产物: " + proxyFqn + "，源码如下\n" + source);
        }
        byte[] classBytes = compiled.get(proxyFqn).getBytes();
        ClassLoader targetLoader = targetClass.getClassLoader() == null
                ? ClassLoader.getSystemClassLoader() : targetClass.getClassLoader();

        RuntimeException firstFailure = null;
        try {
            Class<?> proxyClass = defineViaLookup(targetClass, targetLoader, proxyFqn, classBytes);
            return (T) targetClass.cast(newInstance(proxyClass, hook));
        } catch (RuntimeException | ReflectiveOperationException e) {
            firstFailure = new RuntimeException("代理类定义失败(Lookup 注入): " + proxyFqn, e);
        }
        try {
            Class<?> proxyClass = defineViaLoaderReflection(targetLoader, proxyFqn, classBytes);
            return (T) targetClass.cast(newInstance(proxyClass, hook));
        } catch (RuntimeException | ReflectiveOperationException e) {
            firstFailure = firstFailure == null
                    ? new RuntimeException("代理类定义失败(加载器反射): " + proxyFqn, e) : firstFailure;
        }
        try {
            Class<?> proxyClass = new MapClassLoader(Collections.singletonMap(proxyFqn, classBytes))
                    .loadClass(proxyFqn);
            return (T) targetClass.cast(newInstance(proxyClass, hook));
        } catch (ReflectiveOperationException e) {
            throw firstFailure != null ? firstFailure
                    : new RuntimeException("代理类定义失败(MapClassLoader): " + proxyFqn, e);
        }
    }

    /**
     * JDK 9+ 注入：privateLookupIn 拿目标包的私有 Lookup，代理类定义进目标自己的
     * ClassLoader——同一定义加载器即同一运行时包，包私有目标也能子类化。
     * Java 8 无此 API，反射查找抛 NoSuchMethodException 走下一条路径。
     */
    private static Class<?> defineViaLookup(Class<?> targetClass, ClassLoader targetLoader,
                                            String proxyFqn, byte[] bytes)
            throws ReflectiveOperationException {
        Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
        Method privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, lookupClass);
        Object privateLookup = privateLookupIn.invoke(null, targetClass, MethodHandles.lookup());
        Method defineClass = lookupClass.getMethod("defineClass", byte[].class);
        defineClass.invoke(privateLookup, (Object) bytes);
        return targetLoader.loadClass(proxyFqn);
    }

    /**
     * Java 8 注入：无模块系统，反射 ClassLoader 的 defineClass 私有方法把代理类定义进
     * 目标加载器。JDK 9+ 上 setAccessible 抛 InaccessibleObjectException（RuntimeException），
     * 由调用方转下一条路径。
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static Class<?> defineViaLoaderReflection(ClassLoader loader, String proxyFqn, byte[] bytes)
            throws ReflectiveOperationException {
        Method defineClass = ClassLoader.class.getDeclaredMethod(
                "defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        defineClass.invoke(loader, proxyFqn, bytes, 0, bytes.length);
        return loader.loadClass(proxyFqn);
    }

    private static Object newInstance(Class<?> proxyClass, MethodHook hook) throws ReflectiveOperationException {
        return proxyClass.getConstructor(MethodHook.class).newInstance(hook);
    }

    static String buildSource(Class<?> targetClass, String proxyFqn) {
        String pkg = targetClass.getPackage() == null ? "" : "package " + targetClass.getPackage().getName() + ";\n\n";
        String simple = proxyFqn.substring(proxyFqn.lastIndexOf('.') + 1);

        StringBuilder sb = new StringBuilder();
        sb.append(pkg);
        sb.append("import com.zifang.util.aop.aopgen.MethodHook;\n\n");
        sb.append("public class ").append(simple).append(" extends ").append(targetClass.getCanonicalName()).append(" {\n\n");
        sb.append("    private final MethodHook hook;\n\n");
        sb.append("    public ").append(simple).append("(MethodHook hook) {\n");
        sb.append("        super();\n");
        sb.append("        this.hook = hook;\n");
        sb.append("    }\n\n");

        for (Method m : targetClass.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (!Modifier.isPublic(mod) || Modifier.isStatic(mod) || Modifier.isFinal(mod) || m.isSynthetic()) {
                continue;
            }
            appendOverride(sb, m);
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static void appendOverride(StringBuilder sb, Method m) {
        Class<?> ret = m.getReturnType();
        Class<?>[] pts = m.getParameterTypes();

        StringBuilder params = new StringBuilder();
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < pts.length; i++) {
            if (i > 0) {
                params.append(", ");
                args.append(", ");
            }
            params.append(pts[i].getCanonicalName()).append(" p").append(i);
            args.append("p").append(i);
        }

        sb.append("    @Override\n");
        sb.append("    public ").append(ret.getCanonicalName()).append(" ").append(m.getName())
                .append("(").append(params).append(") {\n");
        sb.append("        hook.before(\"").append(m.getName()).append("\", new Object[]{").append(args).append("});\n");
        sb.append("        try {\n");
        if (ret == void.class) {
            sb.append("            super.").append(m.getName()).append("(").append(args).append(");\n");
            sb.append("            hook.after(\"").append(m.getName()).append("\", null);\n");
        } else {
            sb.append("            ").append(ret.getCanonicalName()).append(" r = super.").append(m.getName()).append("(").append(args).append(");\n");
            sb.append("            hook.after(\"").append(m.getName()).append("\", r);\n");
            sb.append("            return r;\n");
        }
        sb.append("        } catch (Throwable t) {\n");
        sb.append("            hook.afterException(\"").append(m.getName()).append("\", t);\n");
        sb.append("            throw t;\n");
        sb.append("        }\n");
        sb.append("    }\n\n");
    }

    /**
     * 拼编译类路径：进程 java.class.path + 锚点类所在位置（surefire 下前者只有 booter jar）。
     */
    static String classpathFor(Class<?>... anchors) {
        Set<String> entries = new LinkedHashSet<>();
        String jcp = System.getProperty("java.class.path");
        if (jcp != null) {
            Collections.addAll(entries, jcp.split(java.io.File.pathSeparator));
        }
        for (Class<?> anchor : anchors) {
            CodeSource cs = anchor.getProtectionDomain().getCodeSource();
            if (cs == null || cs.getLocation() == null) {
                continue;
            }
            try {
                entries.add(new File(cs.getLocation().toURI()).getPath());
            } catch (Exception e) {
                entries.add(cs.getLocation().getPath());
            }
        }
        return String.join(java.io.File.pathSeparator, entries);
    }
}