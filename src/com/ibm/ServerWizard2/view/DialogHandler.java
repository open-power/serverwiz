package com.ibm.ServerWizard2.view;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;

public class DialogHandler extends ConsoleHandler {
	private JTextArea text;
	public DialogHandler(JTextArea text) {
		this.text=text;
	}

	@Override
	public void publish(LogRecord record) {
		text.append(record.getMessage()+"\n");
		text.setCaretPosition(text.getText().length()-1);
		super.publish(record);
	}
}
