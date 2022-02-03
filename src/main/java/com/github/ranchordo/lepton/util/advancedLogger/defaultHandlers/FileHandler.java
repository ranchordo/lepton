package com.github.ranchordo.lepton.util.advancedLogger.defaultHandlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.ranchordo.lepton.util.advancedLogger.LogEntry;
import com.github.ranchordo.lepton.util.advancedLogger.LogHandler;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class FileHandler extends LogHandler {
	public static DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	private BufferedWriter fos;
	public FileHandler(String fname) {
		try {
			fos=new BufferedWriter(new FileWriter(fname+"-"+formatter.format(LocalDateTime.now())+".log"));
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
		}
	}
	@Override
	public void handle(LogEntry entry) {
		if(!entry.level.botherPrinting) {return;}
		try {
			fos.write(LogHandler.getLogString(entry)+"\n");
			fos.flush();
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
		}
	}
	@Override
	public void delete() {
		try {
			fos.close();
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
		}
	}
}
