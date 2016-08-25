package com.ibm.ServerWizard2.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MessagePopup implements ActionListener {

	public MessagePopup() {

	}

	//private final int ONE_SECOND = 1000;

	//private Timer timer;
	private JFrame frame;
	//private JLabel msgLabel;
	public JTextArea text;
	private JScrollPane sp;

	public MessagePopup(String str, int seconds) {
		frame = new JFrame("ServerWiz2 Launcher Console");
		//msgLabel = new JLabel(str, SwingConstants.CENTER);
		text = new JTextArea(10,80);
		sp = new JScrollPane(text);
		//msgLabel.setPreferredSize(new Dimension(600, 400));

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
		frame.getContentPane().add(sp, BorderLayout.CENTER);
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
