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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.timestored.command.Command;
import com.timestored.command.CommandDialog;
import com.timestored.command.CommandManager;
import com.timestored.command.CommandProvider;
import com.timestored.docs.Document;
import com.timestored.messages.Msg;
import com.timestored.messages.Msg.Key;
import com.timestored.qdoc.AutoCompleteDialog;
import com.timestored.qdoc.DocumentationDialog;
import com.timestored.qdoc.DocumentedEntity;
import com.timestored.qdoc.DocumentedMatcher;
import com.timestored.qdoc.EmptyDocSource;
import com.timestored.qdoc.GotoDefinitionCommandProvider;
import com.timestored.qdoc.ParsedQEntity;
import com.timestored.qstudio.DocEditorPane.TooltipProvider;
import com.timestored.qstudio.qdoc.ContextualDocCompleter;
import com.timestored.swingxx.AAction;
import com.timestored.theme.Theme;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * Controller for providing actions related to combining documents and
 * documentation and jumping definitions. 
 */ @RequiredArgsConstructor
public class QDocController {
	
	private static final int shortModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	// this must be control as command+space on mac is system wide shortcut for finder
	private static final KeyStroke SHOW_DOC_KS = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
	private static final KeyStroke GOTO_DEFINITION_KS = KeyStroke.getKeyStroke(KeyEvent.VK_D, shortModifier);
	private static final KeyStroke OUTLINE_DOC_KS = KeyStroke.getKeyStroke(KeyEvent.VK_I, shortModifier);
	private static final KeyStroke OUTLINE_ALL_DOC_KS = KeyStroke.getKeyStroke(KeyEvent.VK_U, shortModifier);

	private final @NonNull QStudioModel qStudioModel;
	
	private final static @NonNull List<Supplier<DocumentedMatcher>> SUPPLIERS = new ArrayList<>();
	private final static @NonNull List<DocumentedMatcher> docMatchers = new ArrayList<>();
	private final @NonNull  ContextualDocCompleter contextualDocCompleter;
	private static final DocumentedMatcher EMPTY_DOC_MATCHER = new DocumentedMatcher(Language.OTHER, EmptyDocSource.INSTANCE);
		
	public static void registerDocMatcherSupplier(Supplier<DocumentedMatcher> dms) {
		SUPPLIERS.add(dms);
	}
	
	public QDocController(QStudioModel qStudioModel) {
		this.qStudioModel = qStudioModel;

		contextualDocCompleter = new ContextualDocCompleter(qStudioModel);
		for(Supplier<DocumentedMatcher> supplier : SUPPLIERS) {
			docMatchers.add(supplier.get());
		}
		for(DocumentedMatcher dm : docMatchers) {
			dm.setContextDocCompleter(contextualDocCompleter);
		}
	}

	DocumentedMatcher getDocumentedMatcher() {
		for(DocumentedMatcher dm : docMatchers) {
			dm.setContextDocCompleter(contextualDocCompleter);
			if(qStudioModel.getCurrentSqlLanguage().equals(dm.getLanguage())) {
				return dm;
			}
		}
		return EMPTY_DOC_MATCHER;
	}

	Action getAutoCompleteAction(final DocEditorPane docEditorPane, final JFrame parentFrame) {
		Action autoCompleteAction = new AAction("autodoc", e -> {
				JDialog autoDialog = new AutoCompleteDialog(docEditorPane, parentFrame, getDocumentedMatcher());
				showDocPopup(autoDialog, docEditorPane);
				// MUST be requestFocus, previously requestFocusinWindow was broken
				// did not allow typing more letters for autocompletion.
				docEditorPane.requestFocus();
			});
		configureAction(autoCompleteAction, CommonActions.AUTO_COMPLETE_KS, "Show Autocomplete Suggestions");
		return autoCompleteAction;
	}
	
	
	
	
	Action getGotoDefinitionAction(final Document document) {
		// goto definition
		final Action gotoDefinitionAction = new AbstractAction("goto Definition", Theme.CIcon.INFO.get16()) {
			@Override public void actionPerformed(ActionEvent e) {
				int cp = document.getCaratPosition();
				List<DocumentedEntity> docEs = getDocumentedMatcher().findDocs(document.getContent(), cp);
				for (DocumentedEntity de : docEs) {
					if (de instanceof ParsedQEntity) {
						try {
							((ParsedQEntity) de).gotoDefinition(qStudioModel.getOpenDocumentsModel());
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, "Error Opening Source File:\r\n" + ioe.toString(), 
									"Error Opening File", JOptionPane.ERROR_MESSAGE);
						}
						break;
					}
				}
			}
		};
		configureAction(gotoDefinitionAction, GOTO_DEFINITION_KS, "Try to goto the definition for the element under the carat.");
		return gotoDefinitionAction;
	}
	
	Action getOutlineAllFilesAction(final DocEditorPane docEditorPane) {
		Action outlineAllFilesAction = new AAction("outlineAllFiles", e -> {
				showCommandDialog(getDocumentedMatcher().findSourceDocs(), true, docEditorPane);
			});
		return configureAction(outlineAllFilesAction, OUTLINE_ALL_DOC_KS, "Show Code Outline for all files.");
	}
	
	Action getOutlineFileAction(final DocEditorPane docEditorPane) {
		Action outlineFileAction = new AAction("outlineFile", e -> {
				String fname = qStudioModel.getOpenDocumentsModel().getSelectedDocument().getTitle();
				final List<ParsedQEntity> docs = getDocumentedMatcher().findSourceDocs(fname);
				showCommandDialog(docs, false, docEditorPane);
			});
		return configureAction(outlineFileAction, OUTLINE_DOC_KS, "Show Code Outline");
	}
	
	
	Action getDocLookupAction(final DocEditorPane docEditorPane) {
		Action docLookupAction = new AAction("Lookup Doc", Theme.CIcon.INFO.get16(), e -> {
				Document d = qStudioModel.getOpenDocumentsModel().getSelectedDocument();
				int cp = d.getCaratPosition();
				DocumentedEntity doc = getDocumentedMatcher().findDoc(d.getContent(), cp);
				showDocPopup(new DocumentationDialog(doc), docEditorPane);
			});
		return configureAction(docLookupAction, SHOW_DOC_KS,  "Try to lookup documentation for the text under the carat.");
	}

	private void showCommandDialog(final List<ParsedQEntity> docs, boolean showSource, final DocEditorPane docEditorPane) {
		List<Command> commands = GotoDefinitionCommandProvider.convert(docs, qStudioModel.getOpenDocumentsModel(), showSource, "");
		CommandDialog coD = new CommandDialog("Jump to Definition", commands, BackgroundExecutor.EXECUTOR);
		coD.setLocation(docEditorPane.getPopupPoint());
		coD.setVisible(true);
		final Document d = qStudioModel.getOpenDocumentsModel().getSelectedDocument();
		for (int i = 0; i < docs.size(); i++) {
			ParsedQEntity pqe = docs.get(i);
			if(pqe.getSource().equals(d.getTitle()) && pqe.getOffset() >= d.getCaratPosition()) {
				coD.setSelectedCommand(commands.get(i));
				break;
			}
		}
	}

	/** wrap dialog to persist its size, then show at location near carat in DocEditorPanel */
	private void showDocPopup(JDialog dialog, DocEditorPane docEditorPane) {

		WindowSizePersister wsp = new WindowSizePersister(dialog, 
				qStudioModel.getPersistance(), Persistance.Key.QDOC_WINDOW_SIZE);
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
						DocumentedEntity de = getDocumentedMatcher().findDoc(txt, pos);
						if(de != null) {
							return DocumentationDialog.getTooltip(de);	
						}
					}
				}
				return null;
			}
		};
	}

	void registerCommandProviders(CommandManager commandManager, KDBResultPanel kdbResultPanel) {
		for(DocumentedMatcher dm : docMatchers) {
			CommandProvider cp = new GotoDefinitionCommandProvider(dm, qStudioModel.getOpenDocumentsModel(), true, Msg.get(Key.GOTO_DEFINITION));
			commandManager.registerProvider(dm.getLanguage().name(), cp);
			commandManager.registerProvider(dm.getLanguage().name(), new DisplayQDocCommandProvider(dm, kdbResultPanel));	
		}
	}

	public void setIgnoredFoldersRegex(Pattern pat) {
		// TODO Auto-generated method stub
		
	}

	public void folderSelected(File selectedFolder) {
		// TODO Auto-generated method stub
		
	}

	public Object getOpenDocumentsDocSource() {
		// TODO Auto-generated method stub
		return null;
	}


}
