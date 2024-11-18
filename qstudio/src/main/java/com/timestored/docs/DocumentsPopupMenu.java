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
package com.timestored.docs;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.timestored.swingxx.AAction;

import lombok.NonNull;
import lombok.extern.java.Log;

/**
 * Popup menu for document that allows closing it or all others.
 */
@Log public class DocumentsPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	
	public DocumentsPopupMenu(final DocumentActions documentActions, @NonNull final Document document) {
        setName("DocumentsPopupMenu");
        Action closeOthersAction = documentActions.getCloseOtherFilesAction(document);
		add(closeOthersAction);
		closeOthersAction.setEnabled(document!=null);
		add(documentActions.getCloseFileAction(document));
		add(documentActions.getCloseAllToRightFileAction(document));
		add(documentActions.getCloseAllFileAction());

		// copy filepath to clipboard if possible
		String fp = document!=null ? document.getFilePath() : null;
		String p = fp!=null ? new File(fp).getParentFile().getAbsolutePath() : null;

		final String parentPath = p;
		Action reloadDoc = new AbstractAction("Reload") {
			@Override public void actionPerformed(ActionEvent e) {
				String message = "Are you sure you want to reload the current file and lose the changes made?";
				int choice = JOptionPane.showConfirmDialog(null, message, 
						"Reload File", JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE);
				
				if(choice == JOptionPane.YES_OPTION) {
					try {
						document.reloadFromFile();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Error reloading content from disk.");
					}
				}
			}
		};
		Action openParentPath = new AbstractAction("Open Containing Folder") {
			@Override public void actionPerformed(ActionEvent e) {
				File parentFolder = new File(parentPath);
				if(parentFolder.exists()) {
					try {
						Desktop.getDesktop().open(parentFolder);
					} catch (IOException ioe) {
						String message = "Could not open folder" ;
						log.log(Level.WARNING, message, ioe);
						JOptionPane.showMessageDialog(null, message);
					}
				} else {
					JOptionPane.showMessageDialog(null, "Folder no longer exists");
				}
			}
		};
		Action copyFilePathToClipboard = new AAction("Full File Path to Clipboard", e -> {
				StringSelection sel = new StringSelection(p);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
			});
		Action copyFileNameToClipboard = new AAction("Filename to Clipboard", e -> {
				StringSelection sel = new StringSelection(document.getTitle());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
			});
		reloadDoc.setEnabled(p != null);
		copyFilePathToClipboard.setEnabled(p != null);
		openParentPath.setEnabled(parentPath!=null);

		addSeparator();
		add(reloadDoc);
		addSeparator();
		add(openParentPath);
		add(copyFilePathToClipboard);
		add(copyFileNameToClipboard);
	}

}