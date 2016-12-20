package io.transwarp.util;

import java.text.SimpleDateFormat;

public class Constant {

	/**
	 * 日期格式
	 */
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	/**
	 * 日期范围
	 */
	public static long logTimeRange = 24 * 60 * 60 * 1000;
	/**
	 * 配置文件路径
	 */
	public static final String LOGCHECK_PATH = "config/logCheckConfig.xml";
	/**
	 * 配置文件读取
	 */
	public static ConfigRead prop_logCheck;
	/**
	 * 输出日志解析结果的路径
	 */
	public static final String LOGDIR_PATH = "./result/";
	
	static {
		try {
			prop_logCheck = new ConfigRead(LOGCHECK_PATH);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
