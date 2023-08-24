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
package com.timestored.qstudio;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.SplashScreen;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.timestored.TimeStored;
import com.timestored.connections.ConnectionManager;
import com.timestored.connections.ServerConfig;
import com.timestored.docs.OpenDocumentsModel;
import com.timestored.messages.Msg;
import com.timestored.messages.Msg.Key;
import com.timestored.misc.AppLaunchHelper;
import com.timestored.misc.ErrorReporter;
import com.timestored.misc.IOUtils;
import com.timestored.plugins.PluginLoader;
import com.timestored.qstudio.model.AdminModel;
import com.timestored.qstudio.model.QueryAdapter;
import com.timestored.qstudio.model.QueryManager;
import com.timestored.qstudio.model.QueryResult;
import com.timestored.swingxx.ApplicationInstanceListener;
import com.timestored.swingxx.ApplicationInstanceManager;
import com.timestored.swingxx.SwingUtils;
import com.timestored.theme.Theme;

import bibliothek.gui.DockController;

/**
 * Launches the qStudio Application. Checks licensing.
 */
public class QStudioLauncher  {

	private static final Color BLUE_LOGO_BG = new Color(0, 124, 195);
	private static final Color GREEN_LOGO_BG = new Color(0, 146, 63);
	
	private static QStudioFrame appFrame;
	private static final Logger LOG = Logger.getLogger(QStudioLauncher.class.getName());

	// catch all errors and allow user to report
	private static final String ERR_URL = TimeStored.getContactUrl("qStudio Error Report");
	private static final int MINS_BETWEEN = 60*12;
	
	public static final ErrorReporter ERR_REPORTER = new ErrorReporter(ERR_URL, 
			TimeStored.TECH_EMAIL_ADDRESS, "qStudio Bug Report " + QStudioFrame.VERSION, MINS_BETWEEN);

	
	public static void main(final String... args) throws InterruptedException, InvocationTargetException {

		DockController.disableCoreWarning(); // prevent advert
		try {
			boolean foundPlugins = PluginLoader.loadPlugins(QStudioFrame.APP_TITLE.toLowerCase());
		} catch(Throwable ucve) {
			LOG.severe("Could not load a plugin due to " + ucve.getLocalizedMessage());
		}

    	try {
			String curDir = System.getProperty("user.dir");
    		System.out.println(curDir);
	        File licenseText = new File(curDir, "license.txt");
	        if(licenseText.canRead()) {
				String signedLicense = IOUtils.toString(licenseText);
				if(signedLicense != null && signedLicense.length()>0) {
					QLicenser.setSignedLicense(signedLicense);
				}
	        }
		} catch (IOException e) {
			LOG.warning(e.toString());
		}
        
		
		/*
		 * If filename passed, send to existing instance where possible.
		 * Otherwise start a new instance
		 */
		boolean firstInstance = ApplicationInstanceManager.registerInstance(args);
		
		if(!firstInstance) {
			System.out.println("Not the first instance.");
			if (args.length>0) {
				System.out.println("I had arguments, they were handled so EXIT.");
				return;
			}
		}
		
		// if appframe already send it args to process
		ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationInstanceListener() {
				@Override public void newInstanceCreated(List<String> args) {
					System.out.println("New instance detected...");
					if(args.size()>0 && appFrame!= null) {
						appFrame.handleArgs(args);
						EventQueue.invokeLater(new Runnable() {
							@Override public void run() {
								SwingUtils.forceToFront(appFrame);
							}
						});
					}
				}
			});
		
        SwingUtilities.invokeAndWait(new Runnable(){
            @Override public void run(){
        		launch(args);
            }



			private void launch(final String... args) {

				OpenDocumentsModel openDocumentsModel = OpenDocumentsModel.newInstance();
				String title = QStudioFrame.APP_TITLE;
                Persistance persistance = Persistance.INSTANCE;
                
                // persist first ever run date for license purposes
                // they get 4 weeks trial then forced to buy
                long firstEverRun = persistance.getLong(Persistance.Key.FERDB, -1);
                if(firstEverRun == -1) {
                	// In case someone is deleting FERD or Blocking registry writes in areas, remove their conns
    				ConnectionManager.wipePreferences(persistance.getPref(), Persistance.Key.CONNECTIONS.name());
                	firstEverRun = System.currentTimeMillis();
                	persistance.putLong(Persistance.Key.FERDB, firstEverRun);
                }
                QLicenser.setFirstEverRanDate(new Date(firstEverRun));
                // Use saved license if available
                String lic = persistance.get(Persistance.Key.SIGNED_LICENSE, null);
                if(lic != null) {
                	try {
                		QLicenser.setSignedLicense(lic);
                	} catch(IllegalAccessError iae) {
                		QStudioLauncher.showJava17LicenseErrorToUser();
                	}
                }


                // If during trial = pester ELSE force.
        		if (QLicenser.getCurrentState().equals(QLicenser.STATE.FREE_TRIAL)) {
        			askUserToRegister(persistance);
        		} else if(!QLicenser.isPermissioned()) {
        			askUserToRegister(persistance);
        			if(!QLicenser.isPermissioned()) {
    					LOG.warning("You must now register qStudio to use it.");
    					JOptionPane.showMessageDialog(appFrame, "You must register qStudio.");
        				System.exit(0);
        			}
        		}

				AppLaunchHelper.setMacAndWindowsAppearance(title);
				AppLaunchHelper.setTheme(MyPreferences.INSTANCE.getCodeTheme());
				AppLaunchHelper.logToUsersFolder(QStudioFrame.APP_TITLE);
				LOG.info("Starting QStudioLauncher  launch() ###################################");
				LOG.info("System.getProperty(\"java.version\")=" + System.getProperty("java.version"));

				Thread.setDefaultUncaughtExceptionHandler(ERR_REPORTER.getUncaughtExceptionHandler());

				// if not using the built-in java SE6 splash, create our own
				JDialog dialog = null;
				if(SplashScreen.getSplashScreen() == null) {
					Color c = GREEN_LOGO_BG;
		        	URL r = QStudioLauncher.class.getResource("splash-green.png");
		        	if(QLicenser.getCurrentState().equals(QLicenser.STATE.PRO)) {
			        	r = QStudioLauncher.class.getResource("splash.png");
			        	c = BLUE_LOGO_BG;
		        	} else if(QLicenser.getCurrentState().equals(QLicenser.STATE.UNLICENSED)) {
			        	r = QStudioLauncher.class.getResource("splash-red.png");
		        	}
	                String licTxt = "  Registered to: " + QLicenser.getLicenseUser() + "  Days: " + QLicenser.getDaysLicenseLeft();
					dialog = SwingUtils.showSplashDialog(r, c, licTxt + "  Version: " + QStudioFrame.VERSION);
				}

        		ConnectionManager conMan = ConnectionManager.newInstance();
        		
        		// only set a default if one was truly set.
        		MyPreferences my = MyPreferences.INSTANCE;
        		String u = my.getDefaultLoginUsername();
        		String p = my.getDefaultLoginPassword();
        		if(u != null || p != null) {
        			conMan.setDefaultLogin(u, p);
        		}
        		
        		conMan.setPreferenceStore(persistance.getPref(), Persistance.Key.CONNECTIONS.name());
                QueryManager queryManager = new QueryManager(conMan);
            	
                // Randomly show adverts during user actions if NOT registered.
                final Random R = new Random();
                queryManager.addQueryListener(new QueryAdapter() {
                	@Override
                	public void queryResultReturned(ServerConfig sc, QueryResult queryResult) {
                		if(queryResult.k != null && queryManager.getCommercialDBqueries() > 5 && R.nextInt(15) > 9) {
                			encouragePurchase(persistance);
                		}
                	}
				});
                AdminModel adminModel = new AdminModel(conMan, queryManager);
                
            	appFrame = new QStudioFrame( conMan, openDocumentsModel, 
            			adminModel, queryManager, persistance);
            	appFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
            	appFrame.setVisible(true);

            	// if we showed our own dialog, hide it now
				if(dialog != null) {
		            dialog.setVisible(false);
		            dialog.dispose();
				}
				
            	appFrame.handleArgs(Arrays.asList(args));

				LOG.info("Ending QStudioLauncher launch()");
			}
        });
        
	}

	/**
	 * Force a user to register on the qstudio website and enter a license key to use qStudio
	 */
	public static void encouragePurchase(Persistance persistance) {
		if (!QLicenser.isPermissioned(QLicenser.Section.UI_NICETIES)) {
			JOptionPane.showConfirmDialog(appFrame, Theme.getHtmlText(QLicenser.PRO_LIC_MESSAGE), "Please Purchase qStudio Pro", JOptionPane.PLAIN_MESSAGE);
		}
	}


	public static void showJava17LicenseErrorToUser() {
		String JAVA_17_LICENSE_ERR = "You are running a newer version of java >16. \r\nOn newer java versions you must register using the new key format.";
		JOptionPane.showMessageDialog(null,Theme.getHtmlText(JAVA_17_LICENSE_ERR));
	}
	
	/**
	 * Force a user to register on the qstudio website and enter a license key to use qStudio
	 * @return true if and only if licensed.
	 */
	public static boolean askUserToRegister(Persistance persistance) {

		String message = "Please enter your email or <a href='" + TimeStored.Page.QSTUDIO_REGISTER.url() + "'>register</a> online:";
		String res = JOptionPane.showInputDialog(appFrame, Theme.getHtmlText(message), "Register qStudio", JOptionPane.PLAIN_MESSAGE);
		if(res != null) {
			String url = TimeStored.Page.QSTUDIO_REGISTER.url() + "&email=" + res; // TODO encode
			try {
				java.awt.Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				JOptionPane.showConfirmDialog(appFrame, Theme.getHtmlText("Please register at: " + url), "Please Register qStudio", JOptionPane.PLAIN_MESSAGE);
				return false;
			}
			try { Thread.sleep(5_000); } catch (InterruptedException e) { }
		}
		
		// accept key
		String signedLic = Theme.getTextFromDialog(appFrame, "Please Register", "", Msg.get(Key.PLEASE_ENTER_LICENSE_KEY));
		if (signedLic != null && signedLic.length() > 0) {
			String conMess = Msg.get(Key.SORRY_INVALID_LICENSE);
			try {
				if (QLicenser.setSignedLicense(signedLic)) {
					persistance.put(Persistance.Key.SIGNED_LICENSE, signedLic);
					conMess = Msg.get(Key.CONGRATS_VALID_LICENSE) + "\r\n" + QLicenser.getLicenseText();
					return QLicenser.isPermissioned(); // If and only if signed ok, return true.
				}
				JOptionPane.showMessageDialog(appFrame, conMess);
        	} catch(IllegalAccessError iae) {
        		QStudioLauncher.showJava17LicenseErrorToUser();
        	}
		}
		
		return false;
	}
	
	/** only to be used for testing! */
	public static QStudioFrame getAppFrame() {
		return appFrame;
	}
	
}
