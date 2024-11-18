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

package com.timestored.qdoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.timestored.command.Command;
import com.timestored.command.CommandProvider;
import com.timestored.docs.OpenDocumentsModel;

/**
 * Provides {@link Command} for jumping to the source definition of q entities. 
 */
public class GotoDefinitionCommandProvider implements CommandProvider {

	private final DocumentedMatcher documentedMatcher;
	private final OpenDocumentsModel openDocumentsModel;
	private final boolean showSource;
	private final String commandPrefix;

	public GotoDefinitionCommandProvider(DocumentedMatcher documentedMatcher, OpenDocumentsModel openDocumentsModel,
			boolean showSource, String commandPrefix) {
		this.documentedMatcher = Preconditions.checkNotNull(documentedMatcher);
		this.openDocumentsModel = Preconditions.checkNotNull(openDocumentsModel);
		this.showSource = showSource;
		this.commandPrefix = commandPrefix==null ? "" : commandPrefix;
	}

	/**
	 * Convert a list of ParsedQEntity to commands, that allow jumping to the
	 * file/location where they are defined.
	 */
	@Override public Collection<Command> getCommands() {
		return convert(documentedMatcher.findSourceDocs(), openDocumentsModel,  showSource, commandPrefix);
	}
	
	/**
	 * Convert a list of ParsedQEntity to commands, that allow jumping to the
	 * file/location where they are defined.
	 */
	public static List<Command> convert(List<ParsedQEntity> docs, 
			final OpenDocumentsModel openDocumentsModel,
			boolean showSource, String commandPrefix) {
		
		Collections.sort(docs, new Comparator<ParsedQEntity>() {
			@Override public int compare(ParsedQEntity pq1, ParsedQEntity pq2) {
				int diff = pq1.getSource().compareTo(pq2.getSource());
				return (diff != 0 ? diff : pq1.getOffset() - pq2.getOffset());
			}
		});

		List<Command> commands = new ArrayList<Command>();
		for (final ParsedQEntity parsedQEntity : docs) {
			DocumentedEntityDocCommand docCommand;
			docCommand = new DocumentedEntityDocCommand(parsedQEntity, showSource, commandPrefix) {
				@Override public void perform() {
					try {
						parsedQEntity.gotoDefinition(openDocumentsModel);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Error Opening Source File:\r\n" + e.toString(), 
								"Error Opening File", JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			commands.add(docCommand);
		}

		return commands;
	}
}
