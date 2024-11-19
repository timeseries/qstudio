package com.timestored.swingxx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 * Text area that appends new text to the bottom and scrolls to that
 * positions automatically. The text is trimmed to a maxLength that can be configured.
 */
public class ScrollingTextArea extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_MAX_LENGTH = 8000;
	
	private final JTextArea textArea;
	private final JScrollPane scrollpane;
	private int maxLength = DEFAULT_MAX_LENGTH;
	
	
	public ScrollingTextArea(Color fgColor, Color bgColor) {
		setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setName("consolePanel-textArea");
		textArea.setEditable(false);
		textArea.setBackground(bgColor);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		textArea.setForeground(fgColor);
		scrollpane = new JScrollPane(textArea);
		add(scrollpane, BorderLayout.CENTER);
	}

	public void setTextareaFont(Font f) { textArea.setFont(f); }
	public Font getTextareaFont() { return textArea.getFont(); }
	public String getText() { return textArea.getText(); }

	/** 
	 * Set the maximum size of the console in characters.
	 * takes effect on the next append. 
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public void clear() {
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				textArea.setText("");
			}
		});
	}
		
	@Override public synchronized void addMouseWheelListener(MouseWheelListener l) {
		textArea.addMouseWheelListener(l);
	}
	
	public void appendMessage(final String msg) {
		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				String t = textArea.getText();
				if(t.length()>maxLength) {
					int startOffset = Math.abs(maxLength-t.length());
					textArea.setText(t.substring(startOffset));
				}
				textArea.append(msg + (msg.endsWith("\n") ? "" : "\r\n"));

				textArea.revalidate();
				textArea.setCaretPosition(textArea.getDocument().getLength());
				JScrollBar vertical = scrollpane.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());	
			}
		});
	}
	
}