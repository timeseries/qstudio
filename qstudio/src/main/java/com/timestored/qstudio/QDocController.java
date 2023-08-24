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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.google.common.base.Preconditions;
import com.timestored.command.Command;
import com.timestored.command.CommandDialog;
import com.timestored.docs.Document;
import com.timestored.docs.OpenDocumentsModel;
import com.timestored.qdoc.AutoCompleteDialog;
import com.timestored.qdoc.DocumentationDialog;
import com.timestored.qdoc.DocumentedEntity;
import com.timestored.qdoc.DocumentedMatcher;
import com.timestored.qdoc.GotoDefinitionCommandProvider;
import com.timestored.qdoc.ParsedQEntity;
import com.timestored.qstudio.DocEditorPane.TooltipProvider;
import com.timestored.qstudio.QLicenser.Section;
import com.timestored.theme.Theme;
import com.timestored.tscore.persistance.PersistanceInterface;


/**
 * Controller for providing actions related to combining documents and
 * documentation and jumping definitions. 
 */
public class QDocController {
	
	private static final int shortModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	// this must be control as command+space on mac is system wide shortcut for finder
	private static final KeyStroke SHOW_DOC_KS = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
	private static final KeyStroke GOTO_DEFINITION_KS = KeyStroke.getKeyStroke(KeyEvent.VK_D, shortModifier);
	private static final KeyStroke OUTLINE_DOC_KS = KeyStroke.getKeyStroke(KeyEvent.VK_I, shortModifier);
	private static final KeyStroke OUTLINE_ALL_DOC_KS = KeyStroke.getKeyStroke(KeyEvent.VK_U, shortModifier);
	

	private final OpenDocumentsModel openDocumentsModel;
	private final DocumentedMatcher documentedMatcher;
	private final PersistanceInterface persistance;
	
	
	public QDocController(final OpenDocumentsModel openDocumentsModel, 
			final DocumentedMatcher documentedMatcher,
			final PersistanceInterface persistance) {

		this.openDocumentsModel = Preconditions.checkNotNull(openDocumentsModel);
		this.documentedMatcher = Preconditions.checkNotNull(documentedMatcher);
		this.persistance = Preconditions.checkNotNull(persistance);
	}
	
	

	Action getAutoCompleteAction(final DocEditorPane docEditorPane, final JFrame parentFrame) {

		// auto complete keys
		Action autoCompleteAction = new AbstractAction("autodoc") {
			@Override public void actionPerformed(ActionEvent e) {
				JDialog autoDialog = new AutoCompleteDialog(docEditorPane, parentFrame, documentedMatcher);
				showDocPopup(autoDialog, persistance, docEditorPane);
				// MUST be requestFocus, previously requestFocusinWindow was broken
				// did not allow typing more letters for autocompletion.
				docEditorPane.requestFocus();
			}

		};
		configureAction(autoCompleteAction, CommonActions.AUTO_COMPLETE_KS, "Show Autocomplete Suggestions");
		return autoCompleteAction;
	}
	
	
	
	
	Action getGotoDefinitionAction(final Document document) {
		// goto definition
		final Action gotoDefinitionAction = new AbstractAction("goto Definition", Theme.CIcon.INFO.get16()) {
			@Override public void actionPerformed(ActionEvent e) {
				if(QLicenser.requestPermission(Section.UI_NICETIES)) {
					int cp = document.getCaratPosition();
					List<DocumentedEntity> docEs = documentedMatcher.findDocs(document.getContent(), cp);
					for (DocumentedEntity de : docEs) {
						if (de instanceof ParsedQEntity) {
							try {
								((ParsedQEntity) de).gotoDefinition(openDocumentsModel);
							} catch (IOException ioe) {
								JOptionPane.showMessageDialog(null, "Error Opening Source File:\r\n" + ioe.toString(), 
										"Error Opening File", JOptionPane.ERROR_MESSAGE);
							}
							break;
						}
					}
				}
			}
		};
		configureAction(gotoDefinitionAction, GOTO_DEFINITION_KS, 
				"Try to goto the definition for the element under the carat.");
		return gotoDefinitionAction;
	}
	
	Action getOutlineAllFilesAction(final DocEditorPane docEditorPane) {

		Action outlineAllFilesAction = new AbstractAction("outlineAllFiles") {
			@Override public void actionPerformed(ActionEvent e) {
				showCommandDialog(documentedMatcher.findSourceDocs(), true, docEditorPane);
			}

		};
		return configureAction(outlineAllFilesAction, OUTLINE_ALL_DOC_KS, "Show Code Outline for all files.");
	}
	
	Action getOutlineFileAction(final DocEditorPane docEditorPane) {

		Action outlineFileAction = new AbstractAction("outlineFile") {
			@Override public void actionPerformed(ActionEvent e) {
				String fname = openDocumentsModel.getSelectedDocument().getTitle();
				final List<ParsedQEntity> docs = documentedMatcher.findSourceDocs(fname);
				showCommandDialog(docs, false, docEditorPane);
			}

		};
		return configureAction(outlineFileAction, OUTLINE_DOC_KS, "Show Code Outline");
	}
	
	
	Action getDocLookupAction(final DocEditorPane docEditorPane) {
		Action docLookupAction = new AbstractAction("Lookup Doc", Theme.CIcon.INFO.get16()) {
			@Override public void actionPerformed(ActionEvent e) {
				Document d = openDocumentsModel.getSelectedDocument();
				int cp = d.getCaratPosition();
				DocumentedEntity doc = documentedMatcher.findDoc(d.getContent(), cp);
				showDocPopup(new DocumentationDialog(doc), persistance, docEditorPane);
			}
		};
		return configureAction(docLookupAction, SHOW_DOC_KS,  "Try to lookup documentation for the text under the carat.");
	}

	private void showCommandDialog(final List<ParsedQEntity> docs, boolean showSource, final DocEditorPane docEditorPane) {
		
		CommandDialog coD;
		List<Command> commands = GotoDefinitionCommandProvider.convert(docs, openDocumentsModel, showSource, "");
		coD= new CommandDialog("Jump to Definition", commands, BackgroundExecutor.EXECUTOR);
		coD.setLocation(docEditorPane.getPopupPoint());
		coD.setVisible(true);
		final Document d = openDocumentsModel.getSelectedDocument();
		for (int i = 0; i < docs.size(); i++) {
			ParsedQEntity pqe = docs.get(i);
			if(pqe.getSource().equals(d.getTitle()) 
					&& pqe.getOffset() >= d.getCaratPosition()) {
				coD.setSelectedCommand(commands.get(i));
				break;
			}
		}
	}

	/** wrap dialog to persist its size, then show at location near carat in DocEditorPanel */
	private void showDocPopup(JDialog dialog, final PersistanceInterface p, DocEditorPane docEditorPane) {

		WindowSizePersister wsp = new WindowSizePersister(dialog, 
				p, Persistance.Key.QDOC_WINDOW_SIZE);
		dialog.setSize(wsp.getDimension(DocumentationDialog.PREF_DIMENSION));
		dialog.setLocation(docEditorPane.getPopupPoint());
		dialog.setVisible(true);
	}	
	
	private Action configureAction(Action action, KeyStroke keyStroke, String shortDescription) {
		action.putValue(Action.SHORT_DESCRIPTION, shortDescription);
		action.putValue(Action.ACCELERATOR_KEY, keyStroke);
		return action;
	}
	

	public TooltipProvider getTooltipProvider(final Document document) {
		return new TooltipProvider() {
			@Override public String getToolTipText(MouseEvent event, int pos) {
				String txt = document.getContent();
				if(pos<txt.length() && pos>=0) {
					// check if hovered over space
					if(!" \r\n".contains(""+txt.charAt(pos))) {
						DocumentedEntity de = documentedMatcher.findDoc(txt, pos);
						if(de != null) {
							return DocumentationDialog.getTooltip(de);	
						}
					}
				}
				return null;
			}
		};
	}
}
