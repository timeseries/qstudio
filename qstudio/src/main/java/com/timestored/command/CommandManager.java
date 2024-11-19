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
package com.timestored.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

/**
 * Allows grouping {@link CommandProvider} and treating them as one. 
 */
public class CommandManager implements CommandProvider {

	private List<CommandProvider> providers = new ArrayList<CommandProvider>();
	private Map<String,List<CommandProvider>> languageSpecificProviders = new HashMap<>();
	
	public CommandManager() {}

	public Collection<Command> getCommands(String language) {
		List<Command> r = new ArrayList<Command>();
		for(CommandProvider cp : providers) {
			r.addAll(cp.getCommands());
		}
		if(language != null) {
			List<CommandProvider> ls = languageSpecificProviders.get(language);
			if(ls != null) { 
				for(CommandProvider cp : ls) {
					r.addAll(cp.getCommands());
				}
			}
		}
		return r;
	}
	
	@Override public Collection<Command> getCommands() { return getCommands(null); }
	
	public void registerProvider(String language, CommandProvider commandProvider) {
		List<CommandProvider> l = languageSpecificProviders.computeIfAbsent(language, s -> new ArrayList<>());
		l.add(commandProvider);
	}

	public void registerProvider(CommandProvider commandProvider) {
		providers.add(commandProvider);
	}

	public void removeProvider(CommandProvider commandProvider) {
		providers.remove(commandProvider);
	}

	public static Collection<Command> toCommands(List<Action> actions) {
		List<Command> cs = new ArrayList<Command>();
		for(Action a : actions) {
			cs.add(new ActionCommand(a));
		}
		return cs;
	}

	public static Command toCommand(Action action) {
		return new ActionCommand(action);
	}
}
