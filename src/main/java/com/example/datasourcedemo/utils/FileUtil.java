package com.example.datasourcedemo.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 主要用于非Spring环境下文件定位
 *
 * @author bxy
 * @version 1.0
 * @date 2023/10/16 18:10:38
 */
public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static URL locateFile(String fileName) throws IOException, URISyntaxException {
		//在Resource目录下查找文件
		String path = "";
		ClassLoader classLoader = FileUtil.class.getClassLoader();
		URL resource = classLoader.getResource(fileName);
		if (resource != null) {
			return resource;
		}

		//在运行目录下查找文件
		// 获取当前运行的目录
		String currentDir = System.getProperty("user.dir");
		// 构造文件对象
		File file = new File(currentDir, fileName);
		// 如果文件存在，则返回文件的绝对路径
		if (file.exists()) {
			return file.toURI().toURL();
		}
		// 在当前工具类所在的工程的JAR目录同级寻找
		String baseDir;
		String jarPath = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		try {
			baseDir = URLDecoder.decode(new File(jarPath).getParent(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			baseDir = new File(jarPath).getParent();
		}
		if (StringUtils.isNotBlank(baseDir)) {
			path = baseDir + File.separator + fileName;
			File fileTemp = new File(path);
			if (fileTemp.exists()) {
				return fileTemp.toURI().toURL();
			}
		}

		throw new IOException("找不到文件：" + fileName);

	}
}
