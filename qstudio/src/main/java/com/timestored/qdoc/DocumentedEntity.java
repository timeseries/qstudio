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

import javax.swing.ImageIcon;

/**
 * A documented entity has a name, documentation and a related image.
 */
public interface DocumentedEntity {
	
	/** Where this entity is known from */
	public enum SourceType {
		SOURCE, SERVER, BUILTIN, MIXED
	}
	
	/**
	 * @return Name including namespace plus if a function arguments are
	 * included in brackets.
	 */
	public String getDocName();
	
	/**
	 * @return Documentation for this element that would be most useful to a programmer.
	 * This can either be valid html with &lt;html&gt;&lt;body&gt; tags or an empty string if there is no documentation.
	 * @param shortFormat If true try not to include name and try to return 2-3 lines of html max.
	 */
	public String getHtmlDoc(boolean shortFormat);
	
	/** @return Icon for this entity or null if none set. */
	public ImageIcon getIcon();
	
	/**
	 * @return Where this entity is known from. If exists ons server or parsed from
	 * 	file or a builtin function etc.
	 */
	public SourceType getSourceType();

	/**
	 * @return full name including namespace that when typed would return this entities value,
	 * 				exludes function arguments.
	 */
	public abstract String getFullName();
	
	/** @return Descriptive text of exact source, e.g. for parsed from file: filename. */
	public String getSource();
}
