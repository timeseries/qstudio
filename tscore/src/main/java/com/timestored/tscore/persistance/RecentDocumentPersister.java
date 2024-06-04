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
package com.timestored.tscore.persistance;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.timestored.docs.Document;
import com.timestored.docs.OpenDocumentsModel;
import com.timestored.misc.FifoBuffer;

/**
 * Save a list of open and recent documents each time one is opened/closed.
 * Also provides a method to save all currently open documents to a scratch area for later restoration.
 * If you want to read the recent/opened docs previously saved be careful to read before
 * adding this as a listener. As any events would overwrite previous values.
 */
public class RecentDocumentPersister implements OpenDocumentsModel.Listener {

	/*
	 * Note some tricky issues:
	 * Documents may be saved to disk or may not yet be saved to disk
	 * File names may not be unique
	 * In particular in-memory "new files" may not be unique
	 * We also only receive add/close/save notifications, some files may have changed outside of that individual file.
	 */
	
	private final FifoBuffer<String> recentFilePaths = new FifoBuffer<String>(9);
	private final PersistanceInterface persistance;
	private final KeyInterface recentDocsKey;
	private final KeyInterface lastOpenedFolderKey;

	public RecentDocumentPersister(PersistanceInterface persistance, 
			KeyInterface recentDocsKey, 
			KeyInterface lastOpenedFolderKey) {
		
		this.persistance = Preconditions.checkNotNull(persistance);
		this.recentDocsKey = Preconditions.checkNotNull(recentDocsKey);
		this.lastOpenedFolderKey = Preconditions.checkNotNull(lastOpenedFolderKey);
		
		recentFilePaths.addAll(getFilePaths(persistance, recentDocsKey));
	
	}

	private static List<String> getFilePaths(PersistanceInterface persistance, KeyInterface filekey) {
		List<String> filepaths = Lists.newArrayList();
		String recent = persistance.get(filekey, "");
		String[] recDocs = recent.split(PersistanceInterface.PATH_SPLIT);
		for(String filepath : recDocs) {
			if(!filepath.trim().isEmpty()) {
				filepaths.add(filepath);
			}
		}
		return filepaths;
	}
	
	private void persistRecentDocuments() {
		String recent = Joiner.on(PersistanceInterface.PATH_SPLIT).join(recentFilePaths.getAll());
		persistance.put(recentDocsKey, recent);
	}
	
	public List<String> getRecentFilePaths() {
		return recentFilePaths.getAll();
	}

	/** @return most recently opened folder or null if none was set **/
	public File getOpenFolder(PersistanceInterface persistance) {
		
		String path = persistance.get(lastOpenedFolderKey, "");
		if(!path.equals("")) {
			File f = new File(path);
			if(f.isDirectory()) {
				return f;
			}
		}
		return null;
	}
	
	/*
	 * Any time there is an add/close or save persist list of open docs
	 */
	@Override public void docClosed(Document document) {
		if(document.getFilePath()!=null) {
			recentFilePaths.add(document.getFilePath());
		}
		persistRecentDocuments();
	}
	
	@Override public void docAdded(Document document) {
		if(document.getFilePath()!=null) {
			recentFilePaths.add(document.getFilePath());
		}
		persistRecentDocuments();
	}

	@Override public void docSaved() { }
	@Override public void docSelected(Document document) {}
	@Override public void docContentModified() {}
	@Override public void docCaratModified() {}
	
	@Override public void folderSelected(File selectedFolder) { 
		String path = selectedFolder == null ? "" : selectedFolder.getAbsolutePath();
		persistance.put(lastOpenedFolderKey, path);
	}
}

