package io.transwarp.util;

import java.io.InputStream;
import java.text.ParseException;

import org.apache.log4j.Logger;

public class UtilTool {
	
	private static Logger logger = Logger.getLogger(UtilTool.class);
	
	public static boolean checkTime(String logTime, long nowTime) {
		long logTime1 = 0;
		try {
			logTime1 = Constant.dateFormat.parse(logTime).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(logTime1 == 0) {
			throw new RuntimeException("date change error");
		}
		double result = nowTime - logTime1 - Constant.logTimeRange;
		if(result > 0) return false;
		else return true;
	}
	
	public static String changeInputStreamToString(InputStream input) throws Exception{
		StringBuffer answer = new StringBuffer();
		byte[] buffer = new byte[1024];
		int len = -1;
		while((len = input.read(buffer)) != -1) {
			String value = new String(buffer, 0, len);
			answer.append(value);
		}
		return answer.toString();
	}
	
	public static String executeLocal(String command) throws Exception {
		Process process = Runtime.getRuntime().exec(command);
		int exitValue = process.waitFor();
		if(exitValue != 0) {
			logger.error("execute command : \"" + command + "\" is error, error code is " + exitValue);
		}
		InputStream inputStream = process.getInputStream();
		String result = UtilTool.changeInputStreamToString(inputStream);
		inputStream.close();
		logger.debug("command : \"" + command + "\" , result is \"" + result);
		return result;
	}
	
	public static String getDirectory(String filePath) {
		int point = filePath.lastIndexOf("/");
		if(point == -1) {
			point = 0;
		}
		String dname = filePath.substring(0, point);
		return dname + "/";
	}
	
	public static String getFileName(String filePath) {
		String[] items = filePath.split("/");
		if(items.length < 1) return null;
		else return items[items.length - 1];
	}
}
