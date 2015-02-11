package com.ibm.ServerWizard2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogFormatter extends Formatter {
	//
	// Create a DateFormat to format the logger timestamp.
	//
	private static final DateFormat df2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS");
	private static final DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");

	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(1000);
		if (record.getLevel()==Level.CONFIG) {
			builder.append(df2.format(new Date(record.getMillis()))).append(" - ");
			builder.append(formatMessage(record));
			builder.append("\n");
			
		} else {
			builder.append(df.format(new Date(record.getMillis()))).append(" - ");
			//builder.append("[").append(record.getSourceClassName()).append(".");
			//builder.append(record.getSourceMethodName()).append("] - ");
			builder.append("[").append(record.getLevel()).append("] - ");
			builder.append(formatMessage(record));
			builder.append("\n");
		}
		return builder.toString();
	}

	public String getHead(Handler h) {
		return super.getHead(h);
	}

	public String getTail(Handler h) {
		return super.getTail(h);
	}
}
