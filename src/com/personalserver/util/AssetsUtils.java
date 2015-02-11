package com.personalserver.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

public class AssetsUtils {

	public static final String NULL = new String();
	public static final String HTML_DIR = "html";
	public static final String HTML_SUFFIX = ".html";
	
	/**
	 * 不用加.html后缀<br/>
	 * 
	 * 1, 读取配置<br/>
	 * 2, html文件放置占位符<br/>
	 * 3, 格式化html, 显示已有设置<br/>
	 * 
	 * 4, 重启对讲服务
	 */
	public static String readHtmlForName(Context context, String fileName) {
		if(context == null || isNull(fileName)) return NULL;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(HTML_DIR + File.separator + fileName + HTML_SUFFIX)));
			
			StringBuffer sb = new StringBuffer();
			String line = null;
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return NULL;
	}
	
	private static boolean isNull(String str) {
		return str == null || "".equals(str);
	}
}
