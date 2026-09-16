package com.zifang.util.office.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CreatePdfTest2类。
 */
public class CreatePdfTest2 {

    /**
     * transform方法。
     * * @param output String类型参数
     *
     * @param imageFolder String类型参数
     * @return static void类型返回值
     */
    public static void transform(String output, String imageFolder) throws IOException {
        PDDocument document = new PDDocument();

        List<File> fileList = Arrays.asList(new File(imageFolder).listFiles());
        fileList = fileList.stream().filter(e -> !e.getName().startsWith("."))
                .filter(e -> !e.getName().equals("Thumbs.db") && !e.getName().endsWith(".txt") && !e.getName().toLowerCase().endsWith(".url")
                        && !e.getName().endsWith(".html"))
                .filter(e -> !e.getName().contains("脸肿汉化组"))
                .filter(e -> !e.getName().contains(".doc") && !e.getName().contains(".ion") && !e.getName().contains(".tmp") && !e.getName().contains(".ico"))
                .filter(e -> !e.getName().contains(".doc") && !e.getName().contains(".ion") && !e.getName().contains(".tmp") && !e.getName().contains(".ico"))

                .collect(Collectors.toList());
        fileList.sort(Comparator.naturalOrder());
        for (File image : fileList) {

            // 创建图片
            PDImageXObject pdImage = null;
            try {
                pdImage = PDImageXObject.createFromFile(image.getAbsolutePath(), document);
            } catch (Exception e) {
                System.out.println(image.getAbsolutePath());
//                continue;
                throw e;
            }

            int weight = pdImage.getWidth();
            int height = pdImage.getHeight();

            // 设置页大小
            PDRectangle rectangle = new PDRectangle(weight, height);
            PDPage page = new PDPage(rectangle);

            // 填充
            PDPageContentStream contents = new PDPageContentStream(document, page);
            contents.drawImage(pdImage, 0, 0, weight, height);

            // 添加到文档
            document.addPage(page);
            contents.close();
        }

        document.save(output);
        document.close();

    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        String output = "/Volumes/zifang/工口/超电磁炮/(C78) [たくみなむち (たくみなむち)] 超電磁砲のまもりかた　上 (とある魔術の禁書目録) [無修正].pdf";
        String imageFolder = "/Volumes/zifang/工口/超电磁炮/(C78) [たくみなむち (たくみなむち)] 超電磁砲のまもりかた　上 (とある魔術の禁書目録) [無修正]";
        for (File file : new File("/Volumes/element/エロ/Coser星之迟迟写真合集（2014-2019）").listFiles()) {
            if (file.isDirectory()) {
                String folder = file.getAbsolutePath();
                String pdf = folder + ".pdf";
                try {
                    transform(pdf, folder);
                    deleteFolder(folder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deleteFolder(String folder) {
        for (File file : new File(folder).listFiles()) {
            file.delete();
        }
        new File(folder).delete();
    }

    @Test
    /**
     * test0方法。
     */
    public void test0() {

        String baseFolder = "/Volumes/element/エロ/20200301";

        doAnalysisFolder(baseFolder);
    }

    private void doAnalysisFolder(String baseFolder) {
        File file = new File(baseFolder);
        List<File> subFiles = Arrays.asList(Objects.requireNonNull(file.listFiles()));

        subFiles = subFiles.stream().filter(e -> !e.isHidden()).collect(Collectors.toList());
        subFiles.sort(Comparator.naturalOrder());

        List<File> folders = subFiles.stream().filter(File::isDirectory).collect(Collectors.toList());
        List<File> files = subFiles.stream().filter(File::isFile).collect(Collectors.toList());

        if (files.size() > 0) {
            // 处理文件
            // 压缩当前文件夹下的image到一个pdf
            String pdfTargetFilePath = file.getParent() + "/" + file.getName() + ".pdf";
            List<File> successFile = PdfUtil.fillImages(
                    pdfTargetFilePath,
                    files,
                    false
            );

            if (successFile.size() == 0) {
                if (new File(pdfTargetFilePath).length() < 1000) {
                    new File(pdfTargetFilePath).delete();
                }
            } else {
                successFile.stream().forEach(e -> {
                    System.out.println("        compact file : " + e.getAbsolutePath());
                });
                successFile.stream().forEach(e -> {
                    System.out.println("            start deleting-file : " + e.getAbsolutePath());
                    // e.delete();
                    System.out.println("            after deleting-file : " + e.getAbsolutePath());
                });
            }
        }

        // 处理文件夹
        for (File subFolder : folders) {
            // 如果没有文件则删除
            if (Arrays.stream(subFolder.listFiles()).filter(e -> !e.isHidden()).collect(Collectors.toList()).size() == 0) {
                subFolder.delete();
                continue;
            }

            System.out.println("start analysis folder: " + subFolder);
            doAnalysisFolder(subFolder.getAbsolutePath());

            // 如果没有文件则删除
            if (Arrays.stream(subFolder.listFiles()).filter(e -> !e.isHidden()).collect(Collectors.toList()).size() == 0) {
                subFolder.delete();
                continue;
            }

            System.out.println("end   analysis folder: " + subFolder);

        }
    }
}
