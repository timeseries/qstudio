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
package com.timestored.misc;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import com.google.common.base.Preconditions;

/**
 * Allows watching a specific directory and being notified if it or anything below it changes. 
 * For graceful shutdown call {@link #stop()} as DirWatch adds monitors to the file system.
 * Recommended to check using {{@link IOUtils#containsMoreThanMaxFiles(File, int)} beforehand that
 * folder is not too large to monitor.
 */
public class DirWatch {

	private static final Logger LOG = Logger.getLogger(DirWatch.class.getName());
	private final List<DirWatchListener> listeners = new CopyOnWriteArrayList<DirWatchListener>();
	private final FileFilter fileFilter;
	private final ChangedFileAlterationListener fileAlterationListener;
	private final long refreshTimer;
	
	private FileAlterationMonitor monitor;
	private FileAlterationObserver fao;
	
	public DirWatch(long refreshTimer, FileFilter fileFilter) {
		this.fileFilter = fileFilter;
		this.fileAlterationListener = new ChangedFileAlterationListener();
		this.refreshTimer = refreshTimer;
	}

	public void setRoot(File root) throws Exception {
		Preconditions.checkNotNull(root);
		stop();
		
	    fao = new FileAlterationObserver(root, fileFilter);
	    fao.addListener(fileAlterationListener);
	    monitor = new FileAlterationMonitor(refreshTimer, fao);
	    monitor.start();
	}

	
	/**
	 * 
	 */
	public void stop() {
		if(fao != null) {
			fao.removeListener(fileAlterationListener);
		}
		if(monitor != null) {
			monitor.removeObserver(fao);
			fao = null;
			try {
				monitor.stop();
			} catch (Exception e) {
				LOG.log(Level.WARNING, "problem stopping", e);
			}
			monitor = null;
		}
	}
	
	public static interface DirWatchListener {
		public void changeOccurred();
	}

	public void addListener(DirWatchListener listener) {
		listeners.add(listener);
	}

	public void removeListener(DirWatchListener listener) {
		listeners.remove(listener);
	}

	/*
	 * Notify listeners on any change or subfolders / files to folder
	 */
	private class ChangedFileAlterationListener implements FileAlterationListener {
	    
		@Override public void onStart(final FileAlterationObserver observer) {
	    	LOG.fine("The WindowsFileListener has started on " + observer.getDirectory().getAbsolutePath());
	    }
	 
	    @Override public void onDirectoryCreate(final File directory) { n(); }
	 
	    private void n() {
	    	for(DirWatchListener l : listeners) {
	    		l.changeOccurred();
	    	}
		}

		@Override public void onDirectoryChange(final File directory) { n(); }
	 
	    @Override public void onDirectoryDelete(final File directory) { n(); }
	 
	    @Override public void onFileCreate(final File file) { n(); }
	 
	    @Override public void onFileChange(final File file) { n(); }
	 
	    @Override public void onFileDelete(final File file) { n(); }
	 
	    @Override public void onStop(final FileAlterationObserver observer) {
	    	LOG.fine("The WindowsFileListener has stopped on " + observer.getDirectory().getAbsolutePath());
	    }
	}
}
