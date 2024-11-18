package com.timestored.qstudio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.timestored.TimeStored;
import com.timestored.TimeStored.Page;
import com.timestored.babeldb.Curler;
import com.timestored.connections.ConnectionManager;
import com.timestored.connections.JdbcTypes;
import com.timestored.connections.ServerConfig;
import com.timestored.docs.Document;
import com.timestored.docs.OpenDocumentsModel;
import com.timestored.jgrowl.Growler;
import com.timestored.jgrowl.GrowlerFactory;
import com.timestored.misc.AppLaunchHelper;
import com.timestored.misc.HtmlUtils;
import com.timestored.theme.Theme;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import lombok.Setter;

public class UpdateHelper {

	private static final Logger LOG = Logger.getLogger(UpdateHelper.class.getName());
	private static final Random R = new Random();
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final Map<String,AtomicInteger> eventCount = new ConcurrentHashMap<>();	 
	@Setter private static QStudioModel qStudioModel;
	
	static {
		Runnable runnable = () -> {
			try {
				postInfo();
			} catch (IOException e) {
				LOG.warning(e.toString());
			}
		};
		int secondsBetweenSaves = 60*30;  //30*60 =  30 minutes
		scheduler.scheduleAtFixedRate(runnable, secondsBetweenSaves, secondsBetweenSaves, TimeUnit.SECONDS);
	}
	private static String encode(String k, String v) throws IOException {
		return URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8");
	}
	
	private static void postInfo() throws IOException {
		if(!MyPreferences.INSTANCE.isSendTelemetry()) {
			return;
		}
		
        StringBuilder postData = new StringBuilder();
		if(qStudioModel != null) {
			postData.append(getUrlParams(qStudioModel, 0));
		}
		for (Map.Entry<String, AtomicInteger> entry : eventCount.entrySet()) {
			if (postData.length() != 0) {
                postData.append('&');
            }
		   postData.append(encode(entry.getKey(), ""+entry.getValue()));
		}
		
		String st = postData.toString();
        byte[] postDataBytes = st.getBytes("UTF-8");
		String reqType = "application/x-www-form-urlencoded";
		String U = TimeStored.URL + "/qstudio/addon/u";
		Curler.POST(U, null, reqType, postDataBytes);
		eventCount.clear(); // Between iterate and clear, some data could be lost but that's negligible.
	}
	
	public static void registerEvent(String event) {
		eventCount.putIfAbsent(event, new AtomicInteger(0));
		eventCount.get(event).incrementAndGet();
	}
	
	static {
		
		eventCount.clear();
	}
	
	private static final int GAP = 4;
	public static JPanel getUpdateGrowler(String newVersion) {
		JPanel p = new JPanel(new BorderLayout(GAP,GAP));
		JPanel topRow = new JPanel(new BorderLayout(GAP,GAP));
		JLabel l = GrowlerFactory.getLabelWithFixedWidth("There's an update available: QStudio " + newVersion, 0);
		topRow.add(l, BorderLayout.CENTER);
		topRow.add(new JLabel("X"), BorderLayout.EAST);
		p.add(topRow, BorderLayout.NORTH);
		
		JButton updateButton = new JButton("Install Update");
		updateButton.addActionListener(al -> {
			HtmlUtils.browse(TimeStored.Page.QSTUDIO_DOWNLOAD.url());
			try {Thread.sleep(2000);} catch (InterruptedException e) {}
			System.exit(0);
		});
		updateButton.setBackground(Theme.HIGHLIGHT_BUTTON_COLOR);
		updateButton.setForeground(Color.WHITE);
		updateButton.setOpaque(true);

		JButton changesButton = new JButton("Release Notes");
		changesButton.addActionListener(al -> {
			HtmlUtils.browse(TimeStored.Page.QSTUDIO_CHANGES.url());
		});
		
		JPanel butPanel = new JPanel(new GridLayout(1, 0, GAP, GAP));
		butPanel.add(new JLabel("   "));
		butPanel.add(updateButton);
		butPanel.add(changesButton);
		p.add(butPanel, BorderLayout.SOUTH);
		return p;
	}
	


	static String getUrlParams(QStudioModel qsm, int queryCount) {
		StringBuilder sb = new StringBuilder();
		sb.append("?v=" + QStudioFrame.VERSION);

        MyPreferences myPreferences = MyPreferences.INSTANCE;
		if(myPreferences != null && !myPreferences.isSendTelemetry()) {
			return sb.toString();
		}
		sb.append("&q=" + queryCount);

		try {
			String ctypes = "";
			Stream<ServerConfig> conns = qsm.getConnectionManager().getServerConnections().stream();
			Map<String, Long> ctypesMap = conns.map(sc -> sc.getJdbcType().name().toLowerCase().substring(0, 2)).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			for(Entry<String,Long> enn : ctypesMap.entrySet()) {
				ctypes += (""+enn.getKey() + enn.getValue()); // some people have hundreds of servers
			}
			sb.append("&c=" + ctypes);
		} catch(Exception e) {}
		try {
			String ftypes = "";
			Stream<Document> docs = qsm.getOpenDocumentsModel().getDocuments().stream();
			Map<String, Long> ftypesMap = docs.map(d -> d.getFileEnding()).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			for(Entry<String,Long> enn : ftypesMap.entrySet()) {
				ftypes += ("-"+enn.getKey() + "." + enn.getValue()); // some people have hundreds of servers
			}
			sb.append("&f=" + ftypes);
		} catch(Exception e) {}
		
		try {
            long firstEverRun = qsm.getPersistance().getLong(Persistance.Key.FERDB, -1);
			sb.append("&d=" + qsm.getOpenDocumentsModel().getDocuments().size());
			sb.append("&date=" + firstEverRun);
			sb.append("&os=" + getPropAsParam("os.name"));
			sb.append("&ctry=" + getPropAsParam("user.country"));
			sb.append("&lang=" + getPropAsParam("user.language"));
			sb.append("&vmn=" + getPropAsParam("java.vm.name"));
			sb.append("&vmv=" + getPropAsParam("java.vm.vendor"));
			sb.append("&jv=" + getPropAsParam("java.version"));
			sb.append("&tz=" + URLEncoder.encode(TimeZone.getDefault().getID()));
			sb.append("&"+encode("font", MyPreferences.INSTANCE.getCodeFont()));
			sb.append("&"+encode("fontsize", ""+MyPreferences.INSTANCE.getCodeFontSize()));
			sb.append("&"+encode("theme", MyPreferences.INSTANCE.getCodeTheme()));
		} catch(Exception e) {}
		return sb.toString();
	}
	
	public static void main(String... args) {
		QStudioModel qsm = new QStudioModel(ConnectionManager.newInstance(), Persistance.INSTANCE, OpenDocumentsModel.newInstance());
		
		JFrame frame = new JFrame();
		frame.setSize(new Dimension(800, 800));
		
		JTabbedPane tabb = new JTabbedPane();

		Growler growler = GrowlerFactory.getGrowler(frame);
		JButton checkBut = new JButton("Check Version");
		checkBut.addActionListener(e -> {
			checkVersion(qsm, 0, GrowlerFactory.getGrowler(frame));
		});
		JButton but = new JButton("Force");
		but.addActionListener(e -> {
			growler.show(Level.INFO, getUpdateGrowler("VERSIONMARKER"), null, true);	
		});
		JPanel p = new JPanel();
		p.add(checkBut);
		p.add(but);
		
		tabb.add(p, "Update Example");
		int i = 1;
		for(String html : TimeStored.fetchOnlineNews(true)) {
			tabb.add(getNewsPanel(null, html), "Online Dark " + i++);
		}
		for(String html : TimeStored.fetchOnlineNews(false)) {
			tabb.add(getNewsPanel(null, html), "Online Light " + i++);
		}
		i = 1;
		for(String html : TimeStored.NEWS) {
			tabb.add(getNewsPanel(null, html), "Hardcoded " + i++);
		}
		
		frame.add(tabb);
		frame.setVisible(true);
	}
	
	static void checkVersion(QStudioModel qsm, int queryCount, Growler growler) {
		boolean newVersionAvailable = true;
		String vs = "?";
		String params = getUrlParams(qsm, queryCount);
		try(Scanner sc = new java.util.Scanner(new URL("https://www.timestored.com/qstudio/version3.txt" + params).openStream(), "UTF-8")) {
			try {
				String[] versionTxt = sc.useDelimiter("\\A").next().split(",");
				vs = versionTxt[0];
				newVersionAvailable = !QStudioFrame.VERSION.equals(vs);
				try {
					double vers = Double.parseDouble(vs);
					double cur = Double.parseDouble(QStudioFrame.VERSION);
					newVersionAvailable = cur < vers; // Allows releasing newer version then later changing .txt to recommend updating old
				} catch(NumberFormatException e) {
					LOG.warning("Error parsing versions");
				}
			} catch(RuntimeException e) {
				// any problems then showMsg but dont force
			}
		} catch (IOException e) {}
		if(newVersionAvailable && qsm.getQueryManager().hasAnyServers()) { // Don't ask to update before they add servers
			growler.show(Level.INFO, getUpdateGrowler(vs), null, true);
		}
	}
	


	private static String getPropAsParam(String name) {
		String s = System.getProperty(name);
		return s == null ? "" : URLEncoder.encode(s);
	}

	public static JScrollPane getNewsPanel(JdbcTypes jdbcType) {
		boolean isDarkTheme = AppLaunchHelper.isLafDark(MyPreferences.INSTANCE.getCodeTheme());
		return getNewsPanel(jdbcType, TimeStored.getRandomLongNewsHtml(isDarkTheme));
	}
	
	
	private static JScrollPane getNewsPanel(JdbcTypes jdbcType, String newsHTML) {
		// advert panel initially
		JPanel p = new JPanel(new BorderLayout());
		try {
			if(!TimeStored.isOnlineNewsAvailable()) { // Use builtin images
				p.add(getKdbImage());	
			} else {
				JPanel cp = new JPanel();
				JEditorPane tp = Theme.getHtmlText(newsHTML);
				cp.add(tp);
				p.add(cp, BorderLayout.CENTER);
			}
		} catch(Exception e) {
			// OK to show nothing if error.
		}
		
		return new JScrollPane(p);
	}


	public static JLabel getClickableImageLabel(String imgName, Page page) { 
		ImageIcon imgI = new ImageIcon(TimeStored.class.getResource(imgName));
		JLabel lbl = new JLabel(imgI);
		lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lbl.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
        		HtmlUtils.browse(page.url());
			}
		});
		return lbl;
	}

	
	private static final String[] IMG_NAMES = new String[] { "kdb-cover.jpg", "kdb-tutorials.jpg", "crypto-dark-med.png",
			"excel-export.png", "fxdash-dark-med.png", "price-grid-dark-med.png", "sqlnotebook-start-menu.png", "trade-blotter-dark-med.png" };
	
	
	public static JLabel getKdbImage() { 
		return getClickableImageLabel(IMG_NAMES[R.nextInt(IMG_NAMES.length)], Page.PULSE_TUTORIALS_KDB);
		
	}

	public static boolean possiblyShowTimeStoredWebsite(Persistance persistance) {
		// do a bit of advertising once a month
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY 
				&& cal.get(Calendar.DAY_OF_MONTH)<=4) {

			// calc int to represent month and year
	        final int lastOpen = persistance.getInt(Persistance.Key.LAST_AD, 0);
	        int yearOffset = Math.max((cal.get(Calendar.YEAR)-1900), 0)*12;
	        int thisOpen = yearOffset + cal.get(Calendar.MONTH);
	        boolean advertSeenThisMonth = thisOpen <= lastOpen;
	        
	        if(!advertSeenThisMonth) {
	        	// record as seen and open web browser
	        	persistance.putInt(Persistance.Key.LAST_AD, thisOpen);
				HtmlUtils.browse(Page.NEWSPAGE.url());
				return true;
	        }
		}
		return false;
	}

	

	/**
	 * TELemetry check which actions are popular with users. Only record minimum essential to know what is used over entire QStudio users
	 * AVOID anything that would be too specific!!!!  
	 */
	public static JButton tel(JButton button) {
		button.addActionListener(e -> {
			String txt = button.getName();
			txt = txt != null ? txt : button.getText() != null ? button.getText() : button.getToolTipText();
			UpdateHelper.registerEvent("button_" + (txt == null ? "" : txt));
		});
		return button;
	}
	public JMenuItem tel(JMenuItem jMenuItem) {
		jMenuItem.addActionListener(e -> {
			String txt = jMenuItem.getName();
			txt = txt != null ? txt : jMenuItem.getText() != null ? jMenuItem.getText() : jMenuItem.getToolTipText();
			UpdateHelper.registerEvent("menu_" + (txt == null ? "" : txt));
		});
		return jMenuItem;
	}
//	public static AAction tel(AAction a) {
//		return new AAction(a, e -> UpdateHelper.registerEvent("aaction_"+a.getName()));
//	}
	
	public static SimpleButtonAction tel(SimpleButtonAction button) {
		String txt = button.getText();
		UpdateHelper.registerEvent("buttonaction_"+txt != null ? txt : button.getTooltip());
		return button;
	}
	public JTextField tel(JTextField txtField) {
		txtField.addActionListener(e -> UpdateHelper.registerEvent("textfield_" + txtField.getName()));
		return txtField;
	}
}
