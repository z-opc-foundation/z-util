package com.zifang.util.core.concurrency.packages;

/**
 * MultiThreadShareData类。
 */
public class MultiThreadShareData {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ShareData myData = new ShareData();
        for (int i = 0; i < 2; i++) {
            new Thread(new MyRunnable1(myData)).start();
            new Thread(new MyRunnable2(myData)).start();
        }
    }


}

class MyRunnable1 implements Runnable {
    private ShareData myData;

    /**
     * MyRunnable1方法。
     * * @param myData ShareData类型参数
     */
    public MyRunnable1(ShareData myData) {
        this.myData = myData;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        for (int i = 0; i < 100; i++) {
            myData.increment();
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
        }
    }
}

class MyRunnable2 implements Runnable {
    private ShareData myData;

    /**
     * MyRunnable2方法。
     * * @param myData ShareData类型参数
     */
    public MyRunnable2(ShareData myData) {
        this.myData = myData;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        for (int i = 0; i < 100; i++) {
            myData.decrement();
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
        }
    }
}

class ShareData {
    private int i = 0;

    /**
     * increment方法。
     *
     * @return synchronized void类型返回值
     */
    public synchronized void increment() {
        i++;
        System.out.println("i++" + i);
    }

    /**
     * decrement方法。
     *
     * @return synchronized void类型返回值
     */
    public synchronized void decrement() {
        i--;
        System.out.println("i--" + i);
    }
}
