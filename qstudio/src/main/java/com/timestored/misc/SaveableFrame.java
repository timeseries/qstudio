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
package com.timestored.misc;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/** Frame that can be saved as an image */
public class SaveableFrame extends JFrame {

	private static final Logger LOG = Logger.getLogger(SaveableFrame.class.getName());
	private static final long serialVersionUID = 1L;
	
	public SaveableFrame(Component cp, final int width, final int height) {
		setLayout(new BorderLayout());
		if(cp != null) {
			add(cp, BorderLayout.CENTER);
		}
		setSize(new Dimension(width, height));
		setVisible(true);
	}
	
	public void close() {
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}


	/**
	 * Save a .png image of the component to a selected file.
	 * @param width - the width of the component / image. 
	 */
	public static void saveComponentImage(Component a, final int width, final int height, 
			final File file, boolean includeWatermark) throws IOException {
		
		Dimension d = new Dimension(width, height);
		a.setSize(d);
		a.setPreferredSize(d);
        layoutComponent(a);
         
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
		Graphics2D g = bi.createGraphics();
		a.paint(g);  //this == JComponent
		
		if(includeWatermark) {
			BufferedImage waterMark = ImageIO.read(SaveableFrame.class
					.getResourceAsStream("/com/timestored/swingxx/timestored-small.png"));
			// draw in top left corner, 50% transp
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g.drawImage(waterMark, 50, 10, null);
		}
		
		g.dispose();
		
		ImageIO.write(bi,"png", file);
	}

	private static void layoutComponent(Component component) {
		synchronized (component.getTreeLock()) {
			component.doLayout();

			if (component instanceof Container) {
				synchronized(((Container) component).getTreeLock()) {
					for (Component child : ((Container) component).getComponents()) {
						layoutComponent(child);
					}
				}
			}
		}
	}
	

	/**
	 * Save a .png image of the component to a selected file.
	 */
	public static void saveFrame(final SaveableFrame f,  final File file) {
		saveFrame(f, file, false);
	}

	/**
	 * Save a .png image of the component to a selected file.
	 */
	public static void saveFrame(final SaveableFrame f,  final File file, boolean includeWatermark) {

		final AtomicBoolean finished = new AtomicBoolean(false);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				f.requestFocus();
				try {
					saveComponentImage(f, f.getSize().width, f.getSize().height, file, false);
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "eframe.saveToFile(filepath)", e);
				}
				f.close();
				synchronized (finished) {
					finished.set(true);
					finished.notifyAll();
				}
			}
		});
		
		while(!finished.get()) {
			try {
				synchronized (finished) {
					finished.wait();
				}
			} catch (InterruptedException e) {
				// ok to ignore
			}
		}
	}
}