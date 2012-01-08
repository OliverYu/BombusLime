package org.bombusim.lime.logger;

import java.util.ArrayList;

import org.bombusim.lime.Lime;

import android.util.Log;

public class LoggerData {
	public static final String UPDATE_LOG = "org.bombusim.lime.data.UPDATE_ROSTER";

	public LoggerData() {
		log = new ArrayList<LoggerEvent>();
	}
	
	private ArrayList<LoggerEvent> log;
	
	int verboseLevel = LoggerEvent.XML;

	public void clear() {
		synchronized (this) {
			log.clear();
		}
	}

	private void add(int eventType, String message, String details) {
		synchronized (this) {
			log.add(new LoggerEvent(eventType, message, details));
		}
	}
	
	public void addLogEvent(int eventType, String message, String details) {
		if (eventType >= verboseLevel) {
			add(eventType, message, details);
		}
		
		//TODO: broadcast update
	}
	
	public void addLogStreamingEvent(int eventType, String prefix, byte[] data, int length) {
		boolean localXml = Lime.getInstance().localXmlEnabled;
		boolean adbXml = Lime.getInstance().prefs.adbXmlLog;

		if (!localXml && !adbXml) return;
		
		StringBuilder sb = new StringBuilder(data.length);
		
		for (int i=0; i<length; i++) {
			sb.append((char) data[i]);
		}
		
		String ds=sb.toString();
		
		String prefixEx="["+prefix+((eventType==LoggerEvent.XMLIN)?"]<<" : "]>>");
		
		if (localXml) 	add(eventType, prefixEx, ds);
		if (adbXml)     Log.d(prefixEx, ds);
	}
	
	public ArrayList<LoggerEvent> getLogRecords() { return log; }

}