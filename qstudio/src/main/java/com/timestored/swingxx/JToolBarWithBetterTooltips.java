package com.timestored.swingxx;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * Exact same as regular {@link JToolBar} but adds a tooltip
 * with the shortcut keys mentioned where possible.
 */
public class JToolBarWithBetterTooltips extends JToolBar {
	
	private static final long serialVersionUID = 1L;

	public JToolBarWithBetterTooltips(String title) { super(title);	}
	
	@Override public JButton add(Action a) {

		JButton b = super.add(a);
		
    	Object o = a.getValue(Action.ACCELERATOR_KEY);
    	Object deso = a.getValue(Action.SHORT_DESCRIPTION);
    	String t = "";
    	if(deso!=null && deso instanceof String) {
    		t = (String)deso;
    	}
    	if(o != null && o instanceof KeyStroke) {
    		KeyStroke ks = ((KeyStroke)o);
    		t += " (" + ks.toString().replace("pressed", "+") + ")";
    	}
    	b.setToolTipText(t);
    	
		return b;
	}
}