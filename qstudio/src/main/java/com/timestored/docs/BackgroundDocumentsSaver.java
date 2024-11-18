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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.timestored.misc.IOUtils;

import lombok.extern.java.Log;

/**
 * Saves a scratch version of the currently open documents on a regular schedule and only when Documents modified. 
 */
@Log public class BackgroundDocumentsSaver implements OpenDocumentsModel.Listener {

	private static final String CONTENT_MARKER = "\r\nCONTENT:\r\n";
	private static final String PATH_MARKER = "\r\nPATH:\r\n";
	
	private final OpenDocumentsModel openDocumentsModel;
	private final File scratchDir;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);	 
	
	private Date previousSave = new Date(0);
	private boolean anyDocModified;
	private int secondsBetweenSaves;
	
	
	public BackgroundDocumentsSaver(OpenDocumentsModel openDocumentsModel, File scratchDir, int secondsBetweenSaves) {
		
		this.openDocumentsModel = Preconditions.checkNotNull(openDocumentsModel);
		this.scratchDir = Preconditions.checkNotNull(scratchDir);
		Preconditions.checkArgument(secondsBetweenSaves > 0);
		this.secondsBetweenSaves = secondsBetweenSaves;
		
		scratchDir.mkdirs();
		
		Runnable runnable = new Runnable() {
			@Override public void run() {
				requestSave();
			}
		};
		scheduler.scheduleAtFixedRate(runnable, secondsBetweenSaves, secondsBetweenSaves, TimeUnit.SECONDS);
	}

	/**
	 * Requests that any currently opened files and their modifications be saved to a "scratch" directory.
	 * THis may or may not actually be carried out depending on if there have been document modifications and time since last save.
	 * this directory can later be used to restore the unsaved changes using {@link #restoreDocuments()}
	 */
	synchronized public void requestSave() {
 		if(anyDocModified) {
			Date now = new Date();
			// dont bother saving if less than 20 seconds since last save
			if (now.getTime() - previousSave .getTime() >= (secondsBetweenSaves-1)*1000) {
				log.info("store currently open documents to scratch.");
				saveDocumentsScratch();
				previousSave = now;
				anyDocModified = false;
				return;
			}
		}
		log.fine("skipping persistOpenDocuments");
	}

	@Override public void docContentModified() {
		synchronized (this) {
			anyDocModified = true;
		}
	}

	@Override public void docCaratModified() { }
	@Override public void docSaved() { }
	@Override public void docAdded(Document document) { }
	@Override public void docClosed(Document document) { }
	@Override public void docSelected(Document document) { }
	@Override public void folderSelected(File selectedFolder) { }
	@Override public void ignoredFolderPatternSelected(Pattern ignoredFolderPattern) { }


	/**
	 * Saves any currently opened files and their modifications to a "scratch" directory
	 * this directory can later be used to restore the unsaved changes using {@link #restoreDocuments()}
	 */
	synchronized public void saveDocumentsScratch() {
		log.log(Level.INFO, "Saving docs to scratch");
		int i = 0;

		File[] existingFiles = scratchDir.listFiles();
		if(existingFiles != null) {
			for(File ef : existingFiles) {
				ef.delete();
			}
		}
		List<Document> documents = openDocumentsModel.getDocuments();
		for(Document d : documents) {
			// generate non-existent name
			File f = null;
			do {
				i++;
				f = new File(scratchDir, i + "-" + d.getTitle());
			} while(f.exists() && i++<1000);
			String firstLine = d.getFilePath() == null ? "" : d.getFilePath();
			try {
				StringBuilder sb = new StringBuilder(d.getTitle() + PATH_MARKER + firstLine);;
				if(d.hasUnsavedChanges()) {
					sb.append(CONTENT_MARKER).append(d.getContent());	
				}
				IOUtils.writeStringToFile(sb.toString(), f);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Could not save scratch files.", e);
			}
		}
	}

	
	/**
	 * Allows restoring scratch'ed files, i.e. documents that had unsaved changed to that state.
	 */
	public void restoreDocuments() {
		
		final List<Document> startingDocuments = Lists.newArrayList(openDocumentsModel.getDocuments());
		
		/*
		 * Open the previously opened files.
		 */
		File[] files = scratchDir.listFiles();
		List<File> scratchFiles = Collections.emptyList();
		if(files != null) {
			scratchFiles = Lists.newArrayList(files);
		}
		
		for(File f : scratchFiles) {
			try {
				String c = IOUtils.toString(f);
				int pathPos = c.indexOf(PATH_MARKER);
				int contentPos = c.indexOf(CONTENT_MARKER);
				if(pathPos > 0) {
					String title = c.substring(0, pathPos);
					String path = c.substring(pathPos + PATH_MARKER.length());
					String content = null;
					if(contentPos > pathPos) {
						path = c.substring(pathPos + PATH_MARKER.length(), contentPos);
						content = c.substring(contentPos + CONTENT_MARKER.length());
					}
					
					Document d = null;
					if(path.trim().length() > 0) {
						log.info("attempting to restore existing known document");
						File pFile = new File(path);
						if(pFile.exists() && pFile.canRead()) {
							try {
								d = openDocumentsModel.openDocument(pFile);
								if(content != null) {
									d.setContent(content);
								}
							} catch(IOException ioe) {
								// ignore, maybe it moved
							}
						}
					} else {
						d = openDocumentsModel.addDocument();
						d.setTitle(title);
						if(content != null) {
							d.setContent(content);
						}
					}
				} else {
					log.log(Level.WARNING, "found scratch file I dont know how to restore: " + f.getAbsolutePath());
				}
			} catch (IOException e) {
				log.log(Level.WARNING, "error restoring files from scratch: " + e);
			}
		}

		/*
		 * openDocumentsModel - automatically starts with empty doc,
		 * we must remember to close it if/when restoring all previous.
		 */
		if(startingDocuments.size() == 1) {
			Document onlyDoc = startingDocuments.get(0);
			if(onlyDoc.getContent().trim().isEmpty()) {
				openDocumentsModel.closeDocument(onlyDoc);	
			}
		}
	}

	public void shutdownNow() {
		scheduler.shutdownNow();
	}

}
