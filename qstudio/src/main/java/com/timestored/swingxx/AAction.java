package com.timestored.swingxx;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

import lombok.RequiredArgsConstructor;

/**
 * Class to allow short lambda constructor for AbstractAction
 */
@RequiredArgsConstructor public class AAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final ActionListener myaction;
    public AAction(String name, ActionListener myaction) {
		this(name, null, myaction);
	}
    public AAction(String name, javax.swing.Icon icon, ActionListener myaction) {
		super(name,icon);
		this.myaction = myaction;
	}
    
	public void actionPerformed(ActionEvent e) {
		myaction.actionPerformed(e);
	}
}