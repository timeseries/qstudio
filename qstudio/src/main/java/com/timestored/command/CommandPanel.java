/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2024 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timestored.command;

import static com.timestored.swingxx.SwingUtils.ESC_KEYSTROKE;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



/**
 * Displays a list of {@link Command} and allows listening to
 * which is currently highlighted and if one is selected.  
 */
class CommandPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(CommandPanel.class.getName());
	private static final long serialVersionUID = 1L;
	
	private final JList list;
	private ChangeListener changeListener;
	private List<Command> docsShown = new ArrayList<Command>();

	public CommandPanel() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createRaisedBevelBorder());
		
		list = new JList(new Object[] { "" });
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(CommandRenderer.getInstance());
		
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override public void valueChanged(ListSelectionEvent e) {
		        if (!e.getValueIsAdjusting() && !list.isSelectionEmpty()) {
		            if(changeListener!=null) {
		            	changeListener.changedTo((Command) list.getSelectedValue());
		            }
		        } 
			}
		});
	}



	void setCloseAction(Action action) {
		list.getActionMap().put("closeAll", action);
		list.getInputMap().put(ESC_KEYSTROKE, "closeAll");
	}

	public void moveDown() { move(1); }

	public void moveUp() { move(-1); }
	
	private void move(int direction) {
		int sel = list.getSelectedIndex() + direction;
		int cSize = list.getModel().getSize();
		if(cSize>0) {
			list.setSelectedIndex(sel<0 ? 0 : (sel>=cSize ? cSize-1 : sel));
			list.ensureIndexIsVisible(list.getSelectedIndex());
		} 
	}

	public void setCommands(Collection<Command> docsShown) {
		LOG.info("setDocsShown");
		this.docsShown  = new ArrayList<Command>(docsShown);
		removeAll();
		list.setModel(new DefaultComboBoxModel(docsShown.toArray()));
		if(!docsShown.isEmpty()) {
			list.setSelectedIndex(0);
			add(new JScrollPane(list), BorderLayout.CENTER);
		} else {
			JLabel l = new JLabel("No matches found");
			add(l, BorderLayout.CENTER);
		}
		revalidate();
		repaint();
	}

	public void setSelectedCommand(Command command) {
		int p = docsShown.indexOf(command);
		if(p>-1 && p<list.getModel().getSize()) {
			list.setSelectedIndex(p);
			LOG.info("setSelectedCommand setSelectedIndex " + p);
		}
	}
	
	public Command getSelectedCommand() {
		Object o = list.getSelectedValue();
		return (o instanceof Command) ?  (Command)o : null;
	}
	

	/**
	 * Set action for when user either presses enter or double clicks on list.
	 * @param action The action to take
	 */
	void setSelectAction(Action action) {

		final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		// set action on pressing enter
		list.getInputMap().put(enter, enter);
		list.getActionMap().put(enter, action);

		// Handle mouse double click
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Action action = list.getActionMap().get(enter);

					if (action != null) {
						ActionEvent ae;
						ae = new ActionEvent(list, ActionEvent.ACTION_PERFORMED, "");
						action.actionPerformed(ae);
					}
				}
			}
		});
	}
	
	void setChangeListener(ChangeListener changeListener) {
		this.changeListener = changeListener;
	}
	
}
