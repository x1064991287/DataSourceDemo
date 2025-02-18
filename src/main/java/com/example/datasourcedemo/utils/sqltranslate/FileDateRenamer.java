package com.example.datasourcedemo.utils.sqltranslate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDateRenamer {

    /**
     * 批量重命名指定文件夹下，文件名符合特定格式的文件，修改文件名中的日期字段。
     * 文件名格式示例: 2025-02-17_白秀远_01__create_cert_tag_ORACLE_[1.1.0].groovy
     *
     * @param folderPath  文件夹路径
     * @param oldDateStr  需要替换的旧日期字符串 (例如 "2025-02-17")
     * @param newDateStr  替换为的新日期字符串 (例如 "2025-02-18")
     * @return 重命名结果信息
     */
    public static String batchRenameFilesByDate(String folderPath, String oldDateStr, String newDateStr) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            return "Error: Folder does not exist or is not a directory: " + folderPath;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return "Error: Failed to list files in folder: " + folderPath;
        }

        StringBuilder resultMessage = new StringBuilder();
        int renameCount = 0;
        Pattern filenamePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})_(.*)_(\\d+)__(.*)_([A-Z]+)_\\[([\\d.]+)\\]\\.groovy$");

        for (File oldFile : files) {
            if (oldFile.isFile()) {
                String filename = oldFile.getName();
                Matcher matcher = filenamePattern.matcher(filename);

                if (matcher.matches()) {
                    String currentDate = matcher.group(1); // 获取文件名中的日期部分

                    if (currentDate.equals(oldDateStr)) { // 仅当日期匹配旧日期时才进行替换
                        String newFilename = filename.replaceFirst(oldDateStr, newDateStr);
                        File newFile = new File(folderPath, newFilename);

                        try {
                            Path oldFilePath = Paths.get(oldFile.getAbsolutePath());
                            Path newFilePath = Paths.get(newFile.getAbsolutePath());

                            Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                            renameCount++;
                            resultMessage.append("Renamed: ").append(filename).append(" to ").append(newFilename).append("\n");

                        } catch (IOException e) {
                            resultMessage.append("Error renaming: ").append(filename).append(" - ").append(e.getMessage()).append("\n");
                        }
                    }
                }
            }
        }

        if (renameCount > 0) {
            resultMessage.insert(0, "Successfully renamed " + renameCount + " files in folder: " + folderPath + "\n");
        } else {
            if (resultMessage.length() == 0) {
                return "No files found matching the date pattern '" + oldDateStr + "' in folder: " + folderPath;
            } else {
                resultMessage.insert(0, "Processed folder: " + folderPath + ", but no files were successfully renamed.\n");
            }
        }

        return resultMessage.toString();
    }


    public static void main(String[] args) {
        String folderPath = "C:\\Users\\tychen\\Desktop\\待转化的SQL\\生成的文件\\运营"; // 请替换为你要操作的文件夹路径
        String oldDateStr = "2025-02-17";
        String newDateStr = "2025-02-18";

        // 1. 创建测试文件夹 (如果不存在)
        File testFolder = new File(folderPath);
        if (!testFolder.exists()) {
            testFolder.mkdirs();
        }

        String result = batchRenameFilesByDate(folderPath, oldDateStr, newDateStr);
        System.out.println(result);
    }
}
