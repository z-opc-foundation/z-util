package com.zifang.util.aop.aopgen;

import com.zifang.util.aop.aspects.Aspect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 演示用计数切面：把 before/after 触达情况记录下来供测试断言。
 */
public class CountingAspect implements Aspect {

    public final List<String> beforeLog = new CopyOnWriteArrayList<>();
    public final List<String> afterLog = new CopyOnWriteArrayList<>();
    public final List<String> afterExceptionLog = new CopyOnWriteArrayList<>();

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        beforeLog.add(method.getName());
        return true;
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        afterLog.add(method.getName() + "=" + returnVal);
        return true;
    }

    @Override
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        afterExceptionLog.add(method.getName() + ":" + e.getMessage());
        return true;
    }
}