package com.example.datasourcedemo.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertiesYamlConverter {

    // YAML 转 Properties
    public static Map<String, Object> yamlToProperties(String fileName) {
        Yaml yaml = new Yaml();
        try {
            URL url = FileUtil.locateFile(fileName);
            LinkedHashMap<String, Object> map = yaml.loadAs(url.openStream(), LinkedHashMap.class);
            Map<String, Object> propertiesMap = new LinkedHashMap<>();
            mapToProperties("", map, propertiesMap);
            return propertiesMap;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void mapToProperties(String prefix, Map<String, Object> map, Map<String, Object> map1) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                mapToProperties(key, (Map<String, Object>) entry.getValue(), map1);
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                for (int i = 0; i < list.size(); i++) {
                    map1.put(key + "[" + i + "]", list.get(i).toString());
                }
            } else {
                map1.put(key, entry.getValue().toString());
            }
        }
    }

    // 将 Properties 写入文件
    public static void writePropertiesToFile(Map<String, Object> properties, String filePath) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        writer.write(properties.entrySet().stream()
                               .map(entry -> entry.getKey() + "=" + entry.getValue())
                               .collect(Collectors.joining("\n")));
        writer.flush();
        writer.close();

    }

    public static void main(String[] args) {
        try {
            // 转换为 properties 对象
            Map<String, Object> propertiesMap = PropertiesYamlConverter.yamlToProperties("application.yml");
            // 写入 properties 文件
            PropertiesYamlConverter.writePropertiesToFile(propertiesMap, "config.properties");
            System.out.println("YAML 文件成功转换为 Properties 文件！");

        } catch (IOException e) {
            System.err.println("转换过程中出现错误：" + e.getMessage());
        }
    }

}
