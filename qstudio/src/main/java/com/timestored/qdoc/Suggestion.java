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


import lombok.Data;


/**
 * Suggested completion textInsert and where carat should be in 
 * relation to that text.
 */
@Data 
class Suggestion {

	private final DocumentedEntity documentedEntity;
	private final String textInsert;
	private final int selectionStart;
	private final int selectionEnd;

	/** Offset from start of textInsert that selectionStart should move to */
	public int getSelectionStart() { return selectionStart; }
	
	/** Offset from start of textInsert that selectionEnd should move to */
	public int getSelectionEnd() { return selectionEnd; }
		
	/** Get the text within the insertText (if any) that will be selected */
	public String getSelectedText() { return textInsert.substring(selectionStart, selectionEnd); }
	
	
}