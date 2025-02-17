package com.example.datasourcedemo.utils.sqltranslate;

/**
 * @author 白秀远
 * @date 2025/2/17 11:16:29
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateFile {

    public static void main(String[] args) {
        // 指定要读取的目录路径
        String directoryPath = "C:/Users/tychen/Desktop/待转化的SQL/原始文件/运营"; // 请替换为实际目录路径
        String outputFilePath = "C:/Users/tychen/Desktop/待转化的SQL/生成的文件/运营"; // 输出文件路径

        try {
            // 读取并处理文件
            extractCreateTableStatements(directoryPath, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void extractCreateTableStatements(String directoryPath, String outputFilePath) throws IOException {
        // 创建输出文件流
        String split = "\"\"\"";
        // 获取指定目录下的所有文件
        Files.walk(Paths.get(directoryPath))
             .filter(Files::isRegularFile)
             .forEach(file -> {
                 try {
                     // 读取文件内容
                     String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                     String replace0 = content.replace("// 刘洋", "//白秀远");
                     String originalFileName = file.getFileName().toString();
                     String fileName = originalFileName.replace("2025-01-08", "2025-02-17");
                     fileName = fileName.replace("刘洋", "白秀远");
                     fileName = fileName.replace("TDSQL", "ORACLE");
                     // 使用 File.separator 处理文件路径拼接
                     File file1 = new File(outputFilePath + File.separator + fileName);
                     if (!file1.exists()) {
                         file1.createNewFile();
                     }
                     FileWriter fileWriter = new FileWriter(outputFilePath + File.separator + fileName);
                     fileWriter.write(replace0);
                     fileWriter.flush();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
    }
}
