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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;


/**
 *	Displays a list of {@link Command} using a splitpane with list of 
 * {@link Command} on the left and the html for the 
 * currently highlighted entity on the right. 
 */
public class CommandSplitPane extends JSplitPane {

	private static final long serialVersionUID = 1L;
	private final DetailsPanel detailsPanel;
	private final CommandPanel commandPanel;
	
	private Color fgColor;
	private Color bgColor;
	
	public CommandSplitPane() {
		super(JSplitPane.HORIZONTAL_SPLIT, true);

		commandPanel = new CommandPanel();
		detailsPanel = new DetailsPanel();

		JScrollPane selectionScroll = new JScrollPane(commandPanel);
		selectionScroll.setMinimumSize(new Dimension(75, 50));
		
		
		setLeftComponent(selectionScroll);
		setRightComponent(detailsPanel);
		setResizeWeight(0.33);
//		splitPane.setDividerLocation(150);

		/* add listeners */
		commandPanel.setChangeListener(new ChangeListener() {
			@Override public void changedTo(Command command) {
				detailsPanel.displayDoc(command);
			}
		});
	}

	public void moveDown() {commandPanel.moveDown(); }
	public void moveUp() { commandPanel.moveUp(); }

	
	public void setDocsShown(List<Command> docsShown) {
		commandPanel.setCommands(docsShown);
		if(docsShown.size()>0) {
			detailsPanel.displayDoc(docsShown.get(0));
		}
	}

	public Command getSelectedCommand() { return commandPanel.getSelectedCommand(); }


	public void setSelectAction(AbstractAction action) {
		commandPanel.setSelectAction(action);
	}

	public void setCloseAction(AbstractAction action) {
		commandPanel.setCloseAction(action);
	}
	
	
	/**
	 * Displays documentation for a single Suggestion 
	 */
	private class DetailsPanel extends JPanel {
		private static final long serialVersionUID = 1L;
	
		public DetailsPanel() {
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createRaisedBevelBorder());
			setMinimumSize(new Dimension(100, 50));
		}
	
		void displayDoc(Command c) {
			removeAll();
			if(c!=null) {
				add(getCommandPanel(c), BorderLayout.CENTER);
			}
			revalidate();
			repaint();
		}
	}
	
	private JPanel getCommandPanel(Command command) {

		JXPanel p = new JXPanel(new BorderLayout());
		JTextPane txtPane = new JTextPane();
		txtPane.setContentType("text/html");
		txtPane.addHyperlinkListener(new HyperlinkListener()
	    {
	        @Override public void hyperlinkUpdate(HyperlinkEvent e) {
	            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
	            	Utils.browse(e.getURL().toString());
	        }
	    });
		JPanel header = Utils.getSubHeader(command.getTitle(), fgColor, bgColor);
		p.add(header, BorderLayout.NORTH);
		txtPane.setText(command.getDetailHtml());
		
		txtPane.setEditable(false);
		
		p.setBorder(BorderFactory.createRaisedBevelBorder());
		p.setScrollableTracksViewportWidth(true);
		p.setScrollableWidthHint(ScrollableSizeHint.FIT);

		JScrollPane scrollPane = new JScrollPane(txtPane, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		txtPane.setCaretPosition(0);
		
		p.add(scrollPane, BorderLayout.CENTER);
		return p;
	}
	
	public void setFgColor(Color fgColor) {
		this.fgColor = fgColor;
	}
	
	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}
}
