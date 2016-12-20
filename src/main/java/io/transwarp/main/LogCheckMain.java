package io.transwarp.main;

import io.transwarp.logCheck.LogCheckReport;

import org.apache.log4j.Logger;

public class LogCheckMain {
	
	private static Logger logger = Logger.getLogger(LogCheckMain.class);

	public static void main(String[] args) {
		/* 读取传入参数 */
		int length = args.length;
		if(length < 2) {
			logger.error("you need input more then 2 params");
			System.exit(1);
		}
		String hostname = args[0];
		String[] topics = new String[length - 1];
		for(int i = 1; i < length; i++) {
			topics[i - 1] = args[i];
		}
		
		LogCheckReport report = new LogCheckReport(topics, hostname);
		report.getReport();
		System.out.println("analysis final");
	}
}
