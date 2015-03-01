package com.ibm.ServerWizard2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

public class MessagePopup implements ActionListener {

	public MessagePopup() {
		// TODO Auto-generated constructor stub
	}

	//private final int ONE_SECOND = 1000;

	//private Timer timer;
	private JFrame frame;
	private JLabel msgLabel;
	public JTextArea text;

	public MessagePopup(String str, int seconds) {
		frame = new JFrame("Test Message");
		msgLabel = new JLabel(str, SwingConstants.CENTER);
		text = new JTextArea(30,60);

		msgLabel.setPreferredSize(new Dimension(600, 400));

		//timer = new Timer(this.ONE_SECOND * seconds, this);
		// only need to fire up once to make the message box disappear
		//timer.setRepeats(false);
	}

	/**
	 * Start the timer
	 */
	public void open() {
		// make the message box appear and start the timer
		//frame.getContentPane().add(msgLabel, BorderLayout.CENTER);
		frame.getContentPane().add(text, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		//timer.start();
	}
	public void close() {
		frame.dispose();
	}
	/**
	 * Handling the event fired by the timer
	 */
	public void actionPerformed(ActionEvent event) {
		// stop the timer and kill the message box
		//timer.stop();
		//frame.dispose();
	}
}
