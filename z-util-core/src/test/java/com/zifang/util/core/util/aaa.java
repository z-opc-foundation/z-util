package com.zifang.util.core.util;
//
//public class aaa {
//}
//import java.io.File;
//        import java.io.IOException;
//
//public class EbookConvertDynamicPath {
//    public static void main(String[] args) {
//        try {
//            String baseDir = "/Volumes/Elements SE";
//            String inputPath = new File(baseDir, "书籍合集/【罗辑思维】1 - 5季书籍/『罗辑思维』第五季/《第二次世界大战回忆录_全六卷》.mobi").getAbsolutePath();
//            String outputPath = new File(baseDir, "书籍合集/【罗辑思维】1 - 5季书籍/『罗辑思维』第五季/《第二次世界大战回忆录_全六卷》.pdf").getAbsolutePath();
//
//            ProcessBuilder processBuilder = new ProcessBuilder(
//                    "ebook-convert",
//                    inputPath,
//                    outputPath
//            );
//
//            Process process = processBuilder.start();
//            int exitCode = process.waitFor();
//
//            if (exitCode == 0) {
//                System.out.println("命令执行成功");
//            } else {
//                System.err.println("命令执行失败，退出码: " + exitCode);
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}