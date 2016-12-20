package io.transwarp.logCheck;

import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class LogCheckReport {

	private static Logger logger = Logger.getLogger(LogCheckReport.class);
	private String[] topics;
	private String hostname;
	
	public LogCheckReport(String[] topics, String hostname) {
		this.topics = topics;
		this.hostname = hostname;
	}
	
	public void getReport() {
		for(String topic : topics) {
			logger.info(topic);
			Element config = Constant.prop_logCheck.getElement("name", topic);
			String filePath = config.elementText("logpath");
			if(filePath.indexOf("*") != -1) {
				filePath.replaceAll("\\*", hostname);
			}
			logger.info(filePath);
			/* 存储文件名的栈，按文件名后的数字大小压入栈 */
			Stack<String> fileStack = new Stack<String>();
			for(int i = 0; ; i++) {
				StringBuffer pathBuffer = new StringBuffer(filePath);
				if(i != 0) {
					pathBuffer.append(".").append(i);
				}
				String path = pathBuffer.toString();
				File file = new File(path);
				if(file.exists()) {
					fileStack.add(path);
				}else {
					break;
				}
			}
			File dirFile = new File(Constant.LOGDIR_PATH);
			if(!dirFile.exists()) {
				logger.info("mkdir dir : " + Constant.LOGDIR_PATH);
				dirFile.mkdirs();
			}
			/* 对栈中的日志文件进行解析 */
			while(!fileStack.isEmpty()) {
				String path = fileStack.pop();
				this.analysisLog(path, config, Constant.LOGDIR_PATH);
			}
		}
	}

	/**
	 * 解析日志文件
	 * @param logPath 日志文件的完整路径
	 * @param config 日志文件解析的配置
	 */
	public void analysisLog(String logPath, Element config, String logDir) {
		logger.info("analysis file is : " + logPath);
		File file = new File(logPath);
		if(!file.exists()) {
			logger.error("the file " + logPath + " is not found");
			return ;
		}
		
		/* 获取日志解析的基本信息 */
		String serviceRole = config.elementText("ServiceRole");
		String configOfKey = config.elementText("key");

		/* 当期时间戳 */
		long nowTime = System.currentTimeMillis();
		
		/* 用于存储解析结果 */
		Map<String, List<String>> saveLogs = new HashMap<String, List<String>>();
		/* 用于存储日志关键字与生成文件的映射 */
		Map<String, String> keys = new HashMap<String, String>();
		
		/* 根据日志解析的关键字建立号存储容器 */
		String[] splitKeys = configOfKey.split(";");
		for(String splitKey : splitKeys) {
			String[] items = splitKey.split(":");
			keys.put(items[1], logDir + items[0] + "-" + serviceRole);
		}
		
		/* 分析日志，将结果存入容器 */
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean oneDay = false;
			StringBuffer logBuffer = null;
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] items = line.split(" ");		
				/* 判断为一条日志的开头，则处理该条日志，并新建一个StringBuffer用来存储下一条日志 */
				if(items.length >= 3 && items[0].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
					/* 获取到一条日志信息，对该条信息进行处理 */
					if(logBuffer != null && oneDay) {
						String log = logBuffer.toString();
						
						for(Iterator<String> logKeys = keys.keySet().iterator(); logKeys.hasNext(); ) {
							String logKey = logKeys.next();
							if(line.indexOf(logKey) == -1) {
								continue;
							}
							String fileName = keys.get(logKey);
							List<String> saveLog = saveLogs.get(fileName);
							if(saveLog == null) {
								saveLog = new ArrayList<String>();
							}
							saveLog.add(log);saveLog.add("\n");
							saveLogs.put(fileName, saveLog);
							break;
						}
					}
					/* 新建StringBuffer，并判断新的一条日志是否为一天内的日志 */
					logBuffer = new StringBuffer(line);
					oneDay = UtilTool.checkTime(items[0] + " " + items[1], nowTime);
				}else if(logBuffer != null){
					logBuffer.append("\n").append(line);
				}
			}
		}catch(Exception e) {
			logger.error("analysis log file error, error message is : " + e.getMessage());
			e.printStackTrace();
		}
		
		/* 将结果写入文件 */
		for(Iterator<String> logKeys = keys.keySet().iterator(); logKeys.hasNext(); ) {
			String logKey = logKeys.next();
			String fileName = keys.get(logKey);
			List<String> fileValue = saveLogs.get(fileName);
			if(fileValue == null) {
				fileValue = new ArrayList<String>();
			}
			FileWriter writer = null;
			try {
				fileName += ".log";
				logger.info("writer file is : " + fileName);
				writer = new FileWriter(fileName, true);
				int lineCount = fileValue.size();
				for(int i = 0; i < lineCount; i++) {
					writer.write(fileValue.get(i));
				}
			} catch(Exception e) {
				logger.error("writer result to file error, error message is " + e.getMessage());
			} finally {
				if(writer != null) {
					try {
						writer.flush();
						writer.close();
					}catch(IOException io_e) {
						logger.error("close writer file error, error message is : " + io_e.getMessage());
					}
				}
			}
		}
	}
}
