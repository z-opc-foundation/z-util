package com.zifang.util.http.net.bookdemo;

import java.util.concurrent.Callable;

class FindMaxTask implements Callable<Integer> {

    private int[] data;
    private int start;
    private int end;

    FindMaxTask(int[] data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
    }

    /**
     * call方法。
     *
     * @return int类型返回值
     */
    public Integer call() {
        int max = Integer.MIN_VALUE;
        for (int i = start; i < end; i++) {
            if (data[i] > max)
                max = data[i];
        }
        return max;
    }
}