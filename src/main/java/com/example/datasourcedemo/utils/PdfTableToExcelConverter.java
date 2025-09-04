package com.example.datasourcedemo.utils;

/**
 * @author 白秀远
 * @date 2025/7/23 16:41:00
 */
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * 一个简单的工具，用于从PDF中提取文本并尝试识别表格结构，然后将其转换为Excel文件。
 * 注意：这是一个基础版本，对于复杂的表格布局可能效果不佳。
 */
public class PdfTableToExcelConverter {

    public static void main(String[] args) {
        String pdfPath = "C:\\Users\\tychen\\Desktop\\测试文件.pdf"; // 替换为你的PDF文件路径
        String excelPath = "C:\\Users\\tychen\\Desktop\\测试文件.xlsx"; // 替换为你想保存的Excel文件路径

        try {
            convertPdfTableToExcel(pdfPath, excelPath);
            System.out.println("PDF表格已成功转换为Excel: " + excelPath);
        } catch (IOException e) {
            System.err.println("处理过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将PDF中的表格转换为Excel文件。
     *
     * @param pdfPath    PDF文件路径
     * @param excelPath  输出Excel文件路径
     * @throws IOException 如果读取或写入文件时出错
     */
    public static void convertPdfTableToExcel(String pdfPath, String excelPath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            // 创建Excel工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("PDF Tables");

            // 使用自定义的TextStripper来获取文本位置信息
            CustomPDFTextStripper stripper = new CustomPDFTextStripper();
            stripper.setSortByPosition(true); // 按位置排序很重要
            stripper.getText(document);

            // 处理提取到的文本行
            List<TextLine> textLines = stripper.getTextLines();
            List<List<String>> tableData = processTextLines(textLines);

            // 将数据写入Excel
            int rowNum = 0;
            for (List<String> rowData : tableData) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData.get(i));
                }
            }

            // 自动调整列宽
            for (int i = 0; i < tableData.stream().mapToInt(List::size).max().orElse(0); i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(excelPath)) {
                workbook.write(fileOut);
            }
            workbook.close();

        }
    }

    /**
     * 处理文本行，尝试识别表格结构。
     * 这是一个简化的实现，通过Y坐标分组来识别行。
     *
     * @param textLines 提取的文本行列表
     * @return 表格数据，每个内部列表代表一行
     */
    private static List<List<String>> processTextLines(List<TextLine> textLines) {
        List<List<String>> tableData = new ArrayList<>();

        // 按照Y坐标（页面顶部为0，向下为正）对文本行进行分组，以识别表格行
        Map<Float, List<TextLine>> rowsMap = new TreeMap<>(Collections.reverseOrder()); // PDF Y坐标从上到下递减，所以用逆序

        for (TextLine line : textLines) {
            // 使用一个阈值来判断是否在同一行，这里简单地取字体大小的一半作为阈值
            float threshold = line.getHeight() / 2.0f;
            boolean foundRow = false;

            // 查找现有的行
            for (Float yKey : new ArrayList<>(rowsMap.keySet())) {
                if (Math.abs(line.getY() - yKey) < threshold) {
                    rowsMap.get(yKey).add(line);
                    foundRow = true;
                    break;
                }
            }

            // 如果没找到现有行，则创建新行
            if (!foundRow) {
                List<TextLine> newRow = new ArrayList<>();
                newRow.add(line);
                rowsMap.put(line.getY(), newRow);
            }
        }

        // 对每一行内的文本块按X坐标排序，以确定列的顺序
        for (List<TextLine> rowLines : rowsMap.values()) {
            rowLines.sort(Comparator.comparingDouble(TextLine::getX));

            List<String> rowData = new ArrayList<>();
            for (TextLine tl : rowLines) {
                rowData.add(tl.getText());
            }
            tableData.add(rowData);
        }

        return tableData;
    }


    /**
     * 自定义PDFTextStripper，用于捕获文本及其位置信息。
     */
    static class CustomPDFTextStripper extends PDFTextStripper {
        private final List<TextLine> textLines = new ArrayList<>();

        public CustomPDFTextStripper() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            super.writeString(text, textPositions);
            if (!text.trim().isEmpty() && !textPositions.isEmpty()) {
                // 获取第一个字符的位置作为整个字符串的起始位置
                TextPosition firstPos = textPositions.get(0);
                // 获取最后一个字符的位置，用于计算宽度
                TextPosition lastPos = textPositions.get(textPositions.size() - 1);

                float x = firstPos.getX();
                float y = firstPos.getY();
                float width = lastPos.getEndX() - firstPos.getX();
                float height = firstPos.getHeight();

                textLines.add(new TextLine(text.trim(), x, y, width, height));
            }
        }

        public List<TextLine> getTextLines() {
            return textLines;
        }
    }

    /**
     * 代表一个文本块的简单数据类。
     */
    static class TextLine {
        private final String text;
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public TextLine(String text, float x, float y, float width, float height) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public String getText() { return text; }
        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }

        @Override
        public String toString() {
            return "TextLine{" +
                   "text='" + text + '\'' +
                   ", x=" + x +
                   ", y=" + y +
                   ", width=" + width +
                   ", height=" + height +
                   '}';
        }
    }
}
