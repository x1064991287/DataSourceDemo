package com.example.datasourcedemo.utils.sqltranslate;

/**
 * @author 白秀远
 * @date 2025/2/17 11:16:29
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlExtractor {

    // 正则表达式，用于匹配 CREATE TABLE 语句
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?s)CREATE TABLE `[^`]+` \\((.*?)\\) ENGINE=InnoDB[^;]*", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {
        // 指定要读取的目录路径
        String directoryPath = "C:/Users/tychen/Desktop/待转化的SQL/原始文件/运营"; // 请替换为实际目录路径
        String outputFilePath = "C:\\Users\\tychen\\Desktop\\oracle脚本\\运营.sql"; // 请替换为实际输出文件路径

        try {
            // 读取并处理文件
            extractCreateTableStatements(directoryPath, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void extractCreateTableStatements(String directoryPath, String outputFilePath) throws IOException {
        // 创建输出文件流
        File file1 =new File(outputFilePath);
        if(!file1.exists()){
            file1.createNewFile();
        }
        String split="\"\"\"";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            // 获取指定目录下的所有文件
            Files.walk(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .forEach(file -> {
                     try {
                         // 读取文件内容
                         String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);

                         // 提取 CREATE TABLE 语句
                         Matcher matcher = CREATE_TABLE_PATTERN.matcher(content);
                         while (matcher.find()) {
                             String createTableSql = matcher.group();
                             createTableSql=createTableSql.split(split)[0];
                             createTableSql+=";";
                             String replace = createTableSql.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS");
                             writer.write(replace);
                             writer.newLine();
                             writer.newLine(); // 添加空行分隔不同的建表语句
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 });
        }
    }
}

