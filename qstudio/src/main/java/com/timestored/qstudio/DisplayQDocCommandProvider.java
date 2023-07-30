package com.timestored.qstudio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.timestored.TimeStored;
import com.timestored.command.Command;
import com.timestored.command.CommandProvider;
import com.timestored.misc.HtmlUtils;
import com.timestored.qdoc.DocumentationDialog;
import com.timestored.qdoc.DocumentedEntity;
import com.timestored.qdoc.DocumentedEntityDocCommand;
import com.timestored.qdoc.DocumentedMatcher;
import com.timestored.qstudio.kdb.BuiltinDocumentedEntities;
import com.timestored.theme.Theme;

/**
 * Takes a documentation provider and wraps it to provide commands 
 * that allow looking at the javadoc. For builtin kdb commands
 * this takes you to the website, otherwise it shows the qDoc.
 */
class DisplayQDocCommandProvider implements CommandProvider {

	private final KDBResultPanel kdbResultPanel;
	private final DocumentedMatcher documentedMatcher;

	DisplayQDocCommandProvider(DocumentedMatcher documentedMatcher, 
			KDBResultPanel kdbResultPanel) {

		this.documentedMatcher = documentedMatcher;
		this.kdbResultPanel = kdbResultPanel;
	}
	
	@Override public Collection<Command> getCommands() {

		List<? extends DocumentedEntity> docs = documentedMatcher.getDocs();
		List<Command> r = new ArrayList<Command>();
		
		// option to display documentation
		for (final DocumentedEntity de : docs) {
			if(!de.getHtmlDoc(false).trim().isEmpty()) {
				DocumentedEntityDocCommand docCommand;
				docCommand = new DocumentedEntityDocCommand(de, true, "Display qDoc: ") {
					@Override public void perform() {
						if(de instanceof BuiltinDocumentedEntities && HtmlUtils.isBrowseSupported()) {
							String url = ((BuiltinDocumentedEntities)de).getLink();
							HtmlUtils.browse(TimeStored.getRedirectPage(url, "qdoc"));
						} else {
							JPanel p = DocumentationDialog.getDocPanel(de);
							kdbResultPanel.clearAndSetContent(p);
						}
					}
					
					@Override public ImageIcon getIcon() {
						return Theme.CIcon.TEXT_HTML.get16();
					}
				};
				r.add(docCommand);
			}
		}
		return r;
	}

}
