package com.example.datasourcedemo.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 建表语句处理        似乎只需要去掉模式名称
 */
public class DDLProcessor {

    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\tychen\\Desktop\\BID_DECRYPT_TABLE.sql";
        String outputFilePath = "C:\\Users\\tychen\\Desktop\\BID_DECRYPT_TABLE1.sql";
        String schemaName = "SAAS0104";
        try {
            String ddlContent = readFile(inputFilePath);
            String processedDDL = removeSchemaNames(ddlContent, schemaName);
            writeFile(outputFilePath, processedDDL);
            System.out.println("Processed DDL has been written to " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String removeSchemaNames(String ddlContent, String schemaName) {
        // This regex matches schema names in double quotes followed by a dot, e.g., "SAAS0104".
        schemaName = "\""+schemaName+"\"" + ".";
        return ddlContent.replaceAll(schemaName, "");
    }

    private static void writeFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}
