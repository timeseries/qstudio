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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Handle any file drops by notifying listeners.
 */
public class FileDropDocumentHandler extends TransferHandler {
	
	private static final long serialVersionUID = 1L;
	private static final DataFlavor MY_DATA_FLAVOR = DataFlavor.javaFileListFlavor;
	private final TransferHandler fallbackHandler;
	private final List<Listener> listeners = new CopyOnWriteArrayList<FileDropDocumentHandler.Listener>();

	@FunctionalInterface
	public static interface Listener {
		void filesDropped(List<File> files);
	}
	
	public FileDropDocumentHandler() { this(null); }
	public FileDropDocumentHandler(TransferHandler fallbackHandler) { this.fallbackHandler = fallbackHandler; }

	public FileDropDocumentHandler addListener(Listener listener) { 
		listeners.add(listener); 
		return this;
	}
	public FileDropDocumentHandler removeListener(Listener listener) { 
		listeners.remove(listener); 
		return this; 
	}
	
	@Override
	public boolean canImport(TransferSupport tSupp) {
		return (fallbackHandler!=null && fallbackHandler.canImport(tSupp)) || 
				(tSupp.isDrop() && tSupp.isDataFlavorSupported(MY_DATA_FLAVOR));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport tSupp) {

		if(!canImport(tSupp)) {
			return false;
		}
		
		if((tSupp.isDrop() && tSupp.isDataFlavorSupported(MY_DATA_FLAVOR))) {
			Transferable t = tSupp.getTransferable();
			List<File> files;
			try {
				files = (List<File>) t.getTransferData(MY_DATA_FLAVOR);
				for(Listener l : listeners) {
					l.filesDropped(files);
				}
			} catch (IOException e1) {
				return false;
			} catch (UnsupportedFlavorException e) {
				return false;
			}
		}
		if(fallbackHandler!=null) {
			return fallbackHandler.importData(tSupp);
		}

		return true;
	}
	
	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		if(fallbackHandler!=null) {
			fallbackHandler.exportToClipboard(comp, clip, action);
		}
	}
}
