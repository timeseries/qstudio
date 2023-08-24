/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2023 TimeStored
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
package com.timestored.swingxx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileSystemView;

import com.timestored.command.Command;
import com.timestored.command.CommandProvider;
import com.timestored.docs.DocumentActions;
import com.timestored.messages.Msg;
import com.timestored.messages.Msg.Key;

/**
 * takes the files that are displayed in the {@link FileTreePanel} and provides commands
 * to open them via {@link DocumentActions}. 
 */
public class FileOpenCommandProvider implements CommandProvider {

	private final DocumentActions documentActions;
	private final FileTreePanel fileTreePanel;
	private static final FileSystemView FSV = FileSystemView.getFileSystemView();

	public FileOpenCommandProvider(DocumentActions documentActions, FileTreePanel fileTreePanel) {
		this.documentActions = documentActions;
		this.fileTreePanel = fileTreePanel;
	}

	/**
	 * @return A list of commands for opening known files.
	 * This includes all files within the selected folder.
	 */
	@Override public Collection<Command> getCommands() {
		Collection<File> fcache = fileTreePanel.getFileCache();
		if(fcache.size() > 0) {
			List<Command> cmds = new ArrayList<Command>(fcache.size());
			for(File f : fcache) {
				cmds.add(new FileOpenCommand(f));
			}
			return cmds;
		}
		return Collections.emptyList();
	}

	
	private class FileOpenCommand implements Command {
		
		private final File f;
		
		private FileOpenCommand(File f) { this.f = f; }

		@Override public javax.swing.Icon getIcon() { return FSV.getSystemIcon(f); }
		
		@Override public String getTitle() { 
			return Msg.get(f.isDirectory() ? Key.OPEN_FOLDER : Key.OPEN_FILE) + ": " + f.getName(); 
		}
		
		@Override public String getDetailHtml() { return f.getAbsolutePath(); }
		@Override public KeyStroke getKeyStroke() { return null; }
		@Override public String toString() { return getTitle(); };
		@Override public String getTitleAdditional() { return f.getParentFile().getAbsolutePath(); }

		@Override public void perform() {
			if(f.isFile()) {
				documentActions.openFile(f);
			} else {
				try {
					Desktop.getDesktop().open(f);
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(null, "Could not open folder");
				}
			}
		}
		
	}
}
