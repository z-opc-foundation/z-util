package com.zifang.util.proxy.classloader.hotswap;

import java.lang.reflect.Method;

/**
 * TestHotSwap类。
 */
public class TestHotSwap {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        // 开启线程，如果class文件有修改，就热替换
        Thread t = new Thread(new MonitorHotSwap());
        t.start();
    }
}

class MonitorHotSwap implements Runnable {
    // Hot就是用于修改，用来测试热加载
    private String className = "cn.com.infcn.classloader.hotswap.Hot";
    private Class hotClazz = null;
    private HotSwapURLClassLoader hotSwapCL = null;

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            while (true) {
                initLoad();
                Object hot = hotClazz.newInstance();
                Method m = hotClazz.getMethod("hot");
                m.invoke(hot, null); // 打印出相关信息
                // 每隔10秒重新加载一次
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载class
     */
    void initLoad() throws Exception {
        hotSwapCL = HotSwapURLClassLoader.getClassLoader();
        // 如果Hot类被修改了，那么会重新加载，hotClass也会返回新的
        hotClazz = hotSwapCL.loadClass(className);
    }
}