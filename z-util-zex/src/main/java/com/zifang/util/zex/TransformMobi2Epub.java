package com.zifang.util.zex;

import java.io.File;

/**
 * Mobipocket转Epub格式转换工具。
 * <p>
 * 此类使用Calibre工具将Mobi格式的电子书转换为Epub格式。
 * 支持批量转换指定目录下的所有mobi文件。
 *
 * @author zifang
 * @version 1.0
 */
public class TransformMobi2Epub {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // cd /Applications/calibre.app/Contents/MacOS && ./ebook-convert a  b
        String command = "cd /Applications/calibre.app/Contents/MacOS && ./ebook-convert '%s' '%s' && rm -rf '%s'";
        String base = "/Volumes/Elements/全部";
        int i = 0;
        for (File f : new File(base).listFiles()) {
            if (f.getName().endsWith(".mobi")) {
                String aa = f.getParent();
                String name = f.getName().split("[.]")[0];
                String o = name + ".epub";
                String commands = String.format(command, f.getAbsoluteFile(), aa + "/" + o, f.getAbsoluteFile());
                System.out.println(commands);
                i++;
            }
        }
        System.out.println(i);
    }
}
