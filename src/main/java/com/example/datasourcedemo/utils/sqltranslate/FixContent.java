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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixContent {

    public static void main(String[] args) {
        // 指定要读取的目录路径
        //        String filePath = "C:\\Users\\tychen\\Desktop\\oracle脚本\\表结构.txt";
        //        String filePath2 = "C:\\Users\\tychen\\Desktop\\oracle脚本\\主外键.txt";

//        String filePath = "C:\\Users\\tychen\\Desktop\\oracle脚本\\单位.sql"; // 请替换为实际输出文件路径
//        String filePath2 = "C:\\Users\\tychen\\Desktop\\oracle脚本\\单位.sql"; // 请替换为实际输出文件路径
//        String outputFilePath = "C:/Users/tychen/Desktop/待转化的SQL/生成的文件/单位"; // 输出文件路径

        String filePath = "C:\\Users\\tychen\\Desktop\\oracle脚本\\运营1.sql"; // 请替换为实际输出文件路径
        String filePath2 = "C:\\Users\\tychen\\Desktop\\oracle脚本\\运营1.sql"; // 请替换为实际输出文件路径
        String outputFilePath = "C:/Users/tychen/Desktop/待转化的SQL/生成的文件/运营"; // 输出文件路径
        Map<String, String> structMap = new HashMap<>();
        Map<String, String> pkMap = new HashMap<>();
        // 读取文件内容
        test2(filePath, filePath2, structMap, pkMap);
        //修复名称不对应问题
        test(outputFilePath, structMap, pkMap);
    }

    static void test3(Map<String, String> map, List<String> list) {
        for (String s : list) {
            if (s.contains("CREATE TABLE")) {
                String tableName = s.substring(s.indexOf("CREATE TABLE"), s.indexOf("("));
                map.put(tableName.trim(), s);
            }
        }
    }

    static void test(String directoryPath, Map<String,String> struct, Map<String,String> pk) {
        //2025-02-17_白秀远_01__create_cert_tag_ORACLE_[1.1.0]
        String split = "\"\"\"";
        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .forEach(
                         file -> {
                             String fileName = file.getFileName().toString();
                             String tagFileName = fileName.substring(26, fileName.indexOf("_ORACLE_[1.1.0]"));

                             try {
                                 String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                                 String[] split1 = content.split(split);
                                 sb.append(split1[0]);
                                 sb.append(split);
                                 sb.append("\n");
                                 sb.append(struct.get(tagFileName));
                                 sb.append(pk.get(tagFileName));
                                 sb.append("\n");
                                 sb.append(split);
                                 sb.append(split1[split1.length - 1]);
                                 FileWriter fw = new FileWriter(file.toAbsolutePath().toString(), false);
                                 fw.write(sb.toString());
                                 fw.flush();
                                 sb.setLength(0);
                             } catch (IOException e) {
                                 throw new RuntimeException(e);
                             }

                         }
                 );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static void test2(String filePath, String filePath2, Map<String, String> structMap, Map<String, String> pkMap) {

        try {
            List<String> allLines = Files.readAllLines(new File(filePath).toPath());
            StringBuilder sb = new StringBuilder();
            int k = 0;
            for (int i = 0; i < allLines.size(); i++) {
                if (allLines.get(i).startsWith("CREATE TABLE")) {
                    String tableName=allLines.get(i).substring(allLines.get(i).indexOf("CREATE TABLE")+12, allLines.get(i).indexOf("("));
                    tableName=tableName.trim();
                    sb.append(allLines.get(i));
                    sb.append("\n");
                    for (k = i + 1; k < allLines.size(); k++) {
                        String linStr = allLines.get(k);
                        if (linStr.equals(";")) {
                            sb.delete(sb.length() - 1, sb.length());
                            sb.append("tablespace TAX_BASIC_TBS;");
                            sb.append("\n");
                            continue;
                        }
                        if (linStr.isEmpty()) {
                            structMap.put(tableName, sb.toString());
                            sb.setLength(0);
                            i = k;
                            break;
                        }
                        sb.append(linStr);
                        sb.append("\n");
                    }
                }
            }
            StringBuilder sb1 = new StringBuilder();
            String targetTableName="";
            try {
                String start = "-- Primary Key";
                List<String> allLines1 = Files.readAllLines(new File(filePath2).toPath());
                k = 0;

                for (int i = 0; i < allLines1.size(); i++) {
                    if (allLines1.get(i).startsWith(start)) {
                        String[] split = allLines1.get(i).split(" ");
                        targetTableName=split[split.length - 1];
                        k = i + 1;
                        while (!allLines1.get(k).startsWith(start)) {
                            if (!allLines1.get(k).startsWith("--")) {
                                String s = allLines1.get(k);
                                if (s.endsWith(";")&& !s.contains("ADD PRIMARY KEY")) {
                                    s = s.substring(0, s.length() - 1);
                                    s += " tablespace TAX_BASIC_INDEX_TBS;";
                                    sb1.append(s);
                                    sb1.append("\n");
                                } else {
                                    sb1.append(s);
                                    sb1.append("\n");
                                }
                            }
                            k++;
                        }
                        pkMap.put(targetTableName, sb1.toString());
                        sb1.setLength(0);
                        i = k - 2;
                    }
                }

            } catch (Exception e) {

            }
            pkMap.put(targetTableName, sb1.toString());
            sb1.setLength(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("xx");
    }

}
