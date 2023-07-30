package com.timestored.qstudio;

import static com.timestored.theme.Theme.getFormRow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.formdev.flatlaf.fonts.jetbrains_mono.FlatJetBrainsMonoFont;
import com.timestored.misc.AppLaunchHelper;
import com.timestored.swingxx.ColorChooserPanel;
import com.timestored.theme.Theme;

/**
 * GUI Panel that allows configuring {@link MyPreferences} related to editor/result appearance. 
 */
class AppearancePreferencesPanel extends PreferencesPanel {

	private static final long serialVersionUID = 1L;
	private final JSpinner maxFractionDigitsSpinner;
	private final JFormattedTextField rowLimitField;
	private final JFormattedTextField consoleLimitField;
	private final JSpinner codeFontSpinner;
	private final JComboBox<String> codeFontComboBox;
	private final JComboBox<String> editorConfigComboBox;

	private final JTextField criticalColorField;
	private final ColorChooserPanel colorChooserPanel;
	private boolean themeChanged;
	

	public AppearancePreferencesPanel(final MyPreferences myPreferences, final Component container) {
		super(myPreferences, container);
		
		Box panel = Box.createVerticalBox();
		panel.setBorder(Theme.getCentreBorder());

		List<String> editorConfigs = AppLaunchHelper.getLafNamesWithSpacerStrings();
		editorConfigComboBox = new JComboBox<>(editorConfigs.toArray(new String[] {}));
		JLabel ecb = new JLabel(Theme.CIcon.WARNING.get16());
		String tooltipText = "<html>You must restart qStudio for this to take full effect.</html>";
		ecb.setToolTipText(tooltipText);
		panel.add(getFormRow(editorConfigComboBox, "Color Theme:", tooltipText, ecb));
		
		/*
		 * Allow selecting code editor font and size.
		 */
		codeFontComboBox = new JComboBox<>(getFontList().toArray(new String[] {}));
		tooltipText = "<html>Font used in Code Editor. <br/>Recommended default is Monospaced";
		JLabel b = new JLabel(Theme.CIcon.INFO.get());
		b.setToolTipText(tooltipText);
		panel.add(getFormRow(codeFontComboBox, "Code Font:", tooltipText, b));
		
		codeFontSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		panel.add(getFormRow(codeFontSpinner, "Font Size:", "Font Size of Code Editor"));

		Box tipp = Box.createHorizontalBox();
		tipp.add(new JLabel("When theme=Light, Font Size ONLY sets code size."));
		panel.add(tipp);
		
		
		panel.add(Box.createVerticalStrut(20));
		

		
		maxFractionDigitsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
		panel.add(getFormRow(maxFractionDigitsSpinner, "Maximum Decimal Places Displayed:",
				"In results tables and lists, floating point values will have " +
				"their trailing decimal places trimmed to this precision."));


		// query table row limit
		rowLimitField = new JFormattedTextField(Integer.valueOf(0));
		panel.add(getFormRow(rowLimitField, "Maximum Table Rows Displayed:",
				"The IDE will only show this many rows at most. 0=unlimited"));
		
		// query table row limit
		consoleLimitField = new JFormattedTextField(Integer.valueOf(0));
		panel.add(getFormRow(consoleLimitField, "Maximum console characters:",
				"The console will only show up to this many characters."));
				
		
		JPanel ccPanel = new JPanel(new BorderLayout());
		ccPanel.setBorder(BorderFactory.createTitledBorder("Critical Instance Color:"));
		String ccTooltip = "Any servers containing one of these keywords will display this color.";
		colorChooserPanel = new ColorChooserPanel(container);
		criticalColorField = new JTextField(40);
		ccPanel.add(getFormRow(criticalColorField, "Keywords (Comma Separated):", ccTooltip), BorderLayout.NORTH);
		ccPanel.add(getFormRow(colorChooserPanel, "Color:", ccTooltip), BorderLayout.SOUTH);
		panel.add(ccPanel);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		
		refresh();


		// add listeners to update appearance on changes
		codeFontComboBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				String font = (String) codeFontComboBox.getSelectedItem();
				if(!font.startsWith(FONT_SPACER)) {
					myPreferences.setCodeFont(font);
					notifyAndUpdate();
				}
			}
		});
		
		editorConfigComboBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				String lfname = (String) editorConfigComboBox.getSelectedItem();
				if(!lfname.startsWith(AppLaunchHelper.SPACER_PREFIX)) {
					myPreferences.setCodeTheme(lfname);
					notifyAndUpdate();

					if(!themeChanged) {
						String message = "Theme changes require a restart to work fully.";
						JOptionPane.showMessageDialog(container, message);
						themeChanged = true;	
					}
				}
			}
		});
		
		codeFontSpinner.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent e) {
				int sz = (Integer) codeFontSpinner.getValue();
				myPreferences.setCodeFontSize(sz);
				notifyAndUpdate();
			}
		});
	}
	
	private void notifyAndUpdate() {
		myPreferences.notifyListeners();
		SwingUtilities.updateComponentTreeUI(container);
	}
	private static final String FONT_SPACER = " ----- ";
	
	private List<String> getFontList() {
		Font fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	    Set<String> monoFonts = new TreeSet<>();
	    Set<String> otherFonts = new TreeSet<>();
	    FontRenderContext frc = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
	    for (Font font : fonts) {
	        Rectangle2D iBounds = font.getStringBounds("i", frc);
	        Rectangle2D mBounds = font.getStringBounds("m", frc);
	        if (iBounds.getWidth() == mBounds.getWidth()) {
	            monoFonts.add(font.getFamily());
	        } else {
	        	otherFonts.add(font.getFamily());
	        }
	    }
		List<String> r = new ArrayList<>();
		r.add(FONT_SPACER + " Mono Fonts " + FONT_SPACER);
		if(!monoFonts.contains(FlatJetBrainsMonoFont.FAMILY)) {
			r.add(FlatJetBrainsMonoFont.FAMILY);
		}
		r.addAll(monoFonts);
		r.add(FONT_SPACER + " Non-Mono Fonts " + FONT_SPACER);
		r.addAll(otherFonts);
		return r;
	}



	/** Take the persisted values and show them values in the GUI */
	@Override void refresh() {
		int rows = myPreferences.getMaxRowsShown();
		rowLimitField.setValue(Integer.valueOf(rows));
		int maxDigits = myPreferences.getMaximumFractionDigits();
		maxFractionDigitsSpinner.setValue(Integer.valueOf(maxDigits));
		codeFontComboBox.setSelectedItem(myPreferences.getCodeFont());
		editorConfigComboBox.setSelectedItem(myPreferences.getCodeTheme());
		codeFontSpinner.setValue(Integer.valueOf(myPreferences.getCodeFontSize()));
		int consoleLength = myPreferences.getMaxConsoleLength();
		consoleLimitField.setValue(Integer.valueOf(consoleLength));

		criticalColorField.setText(myPreferences.getCriticalServerKeywords());
		colorChooserPanel.setColor(myPreferences.getCriticalServerColor());
	}
	

	/** undo any live settings changes that were made **/
	@Override  void cancel() {
		myPreferences.setCodeFont((String) codeFontComboBox.getSelectedItem());
		myPreferences.setMaxConsoleLength((Integer) consoleLimitField.getValue());
	}
	

	/** Take all changes the user made and persist them.  */
	@Override void saveSettings() {		
		myPreferences.setMaxRowsShown((Integer) rowLimitField.getValue());
		myPreferences.setMaximumFractionDigits((Integer)maxFractionDigitsSpinner.getValue());
		myPreferences.setCodeFontSize((Integer) codeFontSpinner.getValue());
		myPreferences.setCodeFont((String) codeFontComboBox.getSelectedItem());
		myPreferences.setCodeTheme((String) editorConfigComboBox.getSelectedItem());
		myPreferences.setMaxConsoleLength((Integer) consoleLimitField.getValue());
		myPreferences.setCriticalServerKeywords(criticalColorField.getText());
		myPreferences.setCriticalServerColor(colorChooserPanel.getColor());
		
		
	}
}
