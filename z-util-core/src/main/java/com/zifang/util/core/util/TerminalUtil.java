package com.zifang.util.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TerminalUtil类。
 */
public class TerminalUtil {

    /**
     * @param cmd the command that include pipeline executions such as >
     */
    public static void runCommandAsShScript(String cmd) {
        List<String> list = new ArrayList<>();
        list.add("sh");
        list.add("-c");
        list.add(cmd);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(list);
            Process process = processBuilder.start();
            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * runExec方法。
     * * @param cmdarray String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void runExec(String[] cmdarray) {
        System.out.println(Arrays.toString(cmdarray));
        Process pro1;
        try {
            pro1 = Runtime.getRuntime().exec(cmdarray, null);
            pro1.waitFor();
            pro1.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param command let os to execute the command and return the result of command
     *                if command do print something
     * @return String the result-print-out that when you execute in os
     */
    public static String runAndGetReturn(String command) throws IOException {
        StringBuffer sb = new StringBuffer();
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line + "\n");
        }
        return sb.toString();
    }

    /**
     * runAndGetPrint方法。
     * * @param command String类型参数
     *
     * @return static void类型返回值
     */
    public static void runAndGetPrint(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
    }

    /**
     * runAndGetPrint方法。
     * * @param command String类型参数
     *
     * @param timeout long类型参数
     * @return static void类型返回值
     */
    public static void runAndGetPrint(String command, Long timeout) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        boolean pass = false;
        try {
            pass = process.waitFor(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!pass) {
            throw new RuntimeException();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
    }


    /**
     * @param command let os to execute the command and return the result of command
     *                if command do print something
     * @return String the result-print-out that when you execute in os
     */
    public static Integer runAndGetReturnStatus(String command) throws IOException, InterruptedException {
        StringBuffer sb = new StringBuffer();
        Process process = Runtime.getRuntime().exec(command);
        return process.waitFor();
    }

    /**
     * @param command let os to execute the command and return the result of command
     *                if command do print something
     * @return String the result-print-out that when you execute in os,and in java console it will print out
     */
    public static String runAndPrintLog(String command) throws IOException {
        StringBuffer sb = new StringBuffer();
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line + "\n");
            System.out.println(line + "\n");
        }
        return sb.toString();
    }
}
