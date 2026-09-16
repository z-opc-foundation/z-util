package com.zifang.util.core;

import org.junit.Test;

import java.lang.reflect.Field;

public class UnsafeDemo {

    private static long offset;
    private volatile int count = 0;

    @Test
    public void createInstanceByUnsafe() throws InstantiationException {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            
            java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            User user2 = (User) allocateInstance.invoke(unsafe, User.class);
            System.out.println(user2.age);
        } catch (Exception e) {
            System.out.println("sun.misc.Unsafe not available: " + e.getMessage());
        }
    }

    @Test
    public void modifyInstanceFieldByUnsafe() throws InstantiationException, NoSuchFieldException {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);

            User user = new User();
            Field age = user.getClass().getDeclaredField("age");
            
            java.lang.reflect.Method objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
            long fieldOffset = (Long) objectFieldOffset.invoke(unsafe, age);
            
            java.lang.reflect.Method putInt = unsafeClass.getMethod("putInt", Object.class, long.class, int.class);
            putInt.invoke(unsafe, user, fieldOffset, 20);

            System.out.println(user.getAge());
        } catch (Exception e) {
            System.out.println("sun.misc.Unsafe not available: " + e.getMessage());
        }
    }

    @Test
    public void cas() throws InstantiationException, NoSuchFieldException {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            
            java.lang.reflect.Method objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
            offset = (Long) objectFieldOffset.invoke(unsafe, UnsafeDemo.class.getDeclaredField("count"));
        } catch (Exception e) {
            System.out.println("sun.misc.Unsafe not available: " + e.getMessage());
        }
    }

    public void increment(Object unsafe) {
        try {
            Class<?> unsafeClass = unsafe.getClass();
            int before = count;
            java.lang.reflect.Method compareAndSwapInt = unsafeClass.getMethod("compareAndSwapInt", Object.class, long.class, int.class, int.class);
            while (!(Boolean) compareAndSwapInt.invoke(unsafe, this, offset, before, before + 1)) {
                before = count;
            }
        } catch (Exception e) {
            System.out.println("sun.misc.Unsafe not available: " + e.getMessage());
        }
    }

    public static class User {
        int age;

        public User() {
            this.age = 10;
        }

        public int getAge() {
            return age;
        }
    }
}