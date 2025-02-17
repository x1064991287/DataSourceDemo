package com.example.datasourcedemo.utils.sqltranslate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixContent1 {

    public static void main(String[] args) {
        // Input file paths and output file path
        String filePath = "C:\\Users\\tychen\\Desktop\\oracle脚本\\运营1.sql";
        String filePath2 = "C:\\Users\\tychen\\Desktop\\oracle脚本\\运营1.sql";
        String outputFilePath = "C:/Users/tychen/Desktop/待转化的SQL/生成的文件/运营";

        // Maps to store structure and primary key information
        Map<String, String> structMap = new HashMap<>();
        Map<String, String> pkMap = new HashMap<>();

        // Read input files and process them
        readFileAndProcess(filePath, filePath2, structMap, pkMap);

        // Process the output files
        processOutputFiles(outputFilePath, structMap, pkMap);
    }

    static void processOutputFiles(String directoryPath, Map<String,String> structMap, Map<String,String> pkMap) {
        String delimiter = "\"\"\"";
        StringBuilder sb = new StringBuilder();

        try {
            Files.walk(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .forEach(file -> processFile(file.toFile(), sb, structMap, pkMap, delimiter));
        } catch (IOException e) {
            throw new RuntimeException("Error processing files in directory: " + directoryPath, e);
        }
    }

    private static void processFile(File file, StringBuilder sb, Map<String,String> structMap, Map<String,String> pkMap, String delimiter) {
        String fileName = file.getName();
        String tagFileName = fileName.substring(26, fileName.indexOf("_ORACLE_[1.1.0]"));

        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            String[] parts = content.split(delimiter);
            sb.append(parts[0])
              .append(delimiter)
              .append("\n")
              .append(structMap.get(tagFileName))
              .append(pkMap.get(tagFileName))
              .append("\n")
              .append(delimiter)
              .append(parts[parts.length - 1]);

            try (FileWriter fw = new FileWriter(file.getPath(), false)) {
                fw.write(sb.toString());
                fw.flush();
            }
            sb.setLength(0); // Reset StringBuilder for next file
        } catch (IOException e) {
            throw new RuntimeException("Error processing file: " + file.getAbsolutePath(), e);
        }
    }

    static void readFileAndProcess(String filePath, String filePath2, Map<String, String> structMap, Map<String, String> pkMap) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get(filePath));
            StringBuilder sb = new StringBuilder();
            int lineIndex = 0;

            for (int i = 0; i < allLines.size(); i++) {
                if (allLines.get(i).startsWith("CREATE TABLE")) {
                    String tableName = extractTableName(allLines.get(i));
                    sb.append(allLines.get(i)).append("\n");

                    for (lineIndex = i + 1; lineIndex < allLines.size(); lineIndex++) {
                        String line = allLines.get(lineIndex);
                        if (line.equals(";")) {
                            sb.delete(sb.length() - 1, sb.length())
                              .append("tablespace TAX_BASIC_TBS;")
                              .append("\n");
                            continue;
                        }
                        if (line.isEmpty()) {
                            structMap.put(tableName, sb.toString());
                            sb.setLength(0); // Clear StringBuilder for next table
                            i = lineIndex;
                            break;
                        }
                        sb.append(line).append("\n");
                    }
                }
            }

            processPrimaryKeyFile(filePath2, pkMap);
        } catch (IOException e) {
            throw new RuntimeException("Error reading files: " + filePath + " or " + filePath2, e);
        }
    }

    private static String extractTableName(String line) {
        return line.substring(line.indexOf("CREATE TABLE") + 12, line.indexOf("(")).trim();
    }

    private static void processPrimaryKeyFile(String filePath2, Map<String, String> pkMap) {
        String start = "-- Primary Key";
        StringBuilder sb1 = new StringBuilder();
        String targetTableName = "";

        try {
            List<String> allLines1 = Files.readAllLines(Paths.get(filePath2));
            int lineIndex = 0;

            for (int i = 0; i < allLines1.size(); i++) {
                if (allLines1.get(i).startsWith(start)) {
                    String[] split = allLines1.get(i).split(" ");
                    targetTableName = split[split.length - 1];
                    lineIndex = i + 1;

                    // Ensure lineIndex does not go out of bounds
                    while (lineIndex < allLines1.size() && !allLines1.get(lineIndex).startsWith(start)) {
                        if (!allLines1.get(lineIndex).startsWith("--")) {
                            String line = allLines1.get(lineIndex);
                            if (line.endsWith(";")) {
                                line = line.substring(0, line.length() - 1) + " tablespace TAX_BASIC_INDEX_TBS;";
                            }
                            sb1.append(line).append("\n");
                        }
                        lineIndex++;
                    }
                    pkMap.put(targetTableName, sb1.toString());
                    sb1.setLength(0); // Clear StringBuilder for next primary key block
                    i = lineIndex - 2;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing primary key file: " + filePath2, e);
        }
    }
}
