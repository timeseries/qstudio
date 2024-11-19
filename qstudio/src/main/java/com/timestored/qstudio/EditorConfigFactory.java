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

import javax.swing.UIManager;

import jsyntaxpane.util.Configuration;

/**
 *  A configuration that when applied to a jsyntaxpane styles it.
 */
public class EditorConfigFactory {

	private static final EditorConfig LIGHT = new LightEditorConfig();
	private static final EditorConfig DARK = new DarkEditorConfig();
	
	private static final String[] NAMES = new String[] { "Light", "Dark" };
	
	public static interface EditorConfig {
		void apply(Configuration configuration);
	}

	
	public static EditorConfig get() { return LIGHT; }
	
	public static enum TCOLOR { LIGHT, DARK }
	public static EditorConfig get(TCOLOR name) {
		if(name != null && name.equals(TCOLOR.DARK)) {
			return DARK;
		}
		return LIGHT;
	}
	
	public static String[] getNames() { return NAMES; }
	
	private static class DarkEditorConfig implements EditorConfig {

		final String FG = "ABB2BF";
		
		@Override public void apply(Configuration c) {
//			# These are the various Attributes for each TokenType.
//			# The keys of this map are the TokenType Strings, and the values are:
//			# color (hex, or integer), Font.Style attribute
//			# Style is one of: 0 = plain, 1=bold, 2=italic, 3=bold/italic
			c.put("Style.COMMENT","0x777777, 2");
			c.put("Style.COMMENT2","0x339933, 3");
			c.put("Style.TYPE","0x56B6C2, 2");  // Boolean / byte / short
			c.put("Style.NUMBER","0x999933, 0");  // qSQL
			c.put("Style.REGEX","0xE5A07B, 0");  // ()[]
			c.put("Style.OPERATOR","0xCCCCCC, 0");  // <= > + - 
			c.put("Style.STRING","0x98C379, 0");  // qSQL - strings "blah" - green
			c.put("Style.STRING2","0x689349, 0");
			c.put("Style.DELIMITER","0xC678DD, 0"); // {} - purple
			c.put("Style.TYPE3","0xE5A07B, 0");
			c.put("Style.ERROR","0xCC0000, 3");
			c.put("Style.DEFAULT","0xFFFF00, 0");
			c.put("Style.KEYWORD","0x61AFEF, 0");  // xlog while sums
			c.put("Style.KEYWORD2","0x9F73A0, 0"); // .z.* .Q.*
			c.put("Style.TYPE2","0xC07F54, 0"); // Standard SQL keywords
			c.put("Style.WARNING","0xcc0000, 0"); 
			c.put("Style.IDENTIFIER","0x"+FG+", 0");   // qSQL - lots of text e.g. symbols and colNa


			c.put("SelectionColor","0x99ccff");
			c.put("CaretColor", getColor("TextArea.caretForeground", "0xeeffcc")); // 
			c.put("PairMarker.Color","0xAA0000");
			c.put("TokenMarker.Color","0x214FAF");
			c.put("LineNumbers.CurrentBack","0x222211");
			c.put("LineNumbers.Foreground","0xAAAADD");
			c.put("LineNumbers.Background","0x333333");

			c.put("SelectionColor","0x1659BB");
			c.put("SingleColorSelect", "false"); // true = turns off syntax highlighting when selected
			c.put("RightMarginColumn", "0");
			c.put("RightMarginColor", "0x222222");
		}

		private static String getColor(String name, String defColor) {
			Color c = UIManager.getColor(name);
			return c != null ? ("0x"+Integer.toHexString(c.getRGB()).substring(2)) : defColor;
		}
	}
	
	private static class LightEditorConfig implements EditorConfig {

//		# Style is one of: 0 = plain, 1=bold, 2=italic, 3=bold/italic
		@Override public void apply(Configuration c) {
			c.put("Style.COMMENT","0x33AA33, 2");
			c.put("Style.COMMENT2","0x33AA33, 3");
			c.put("Style.TYPE","0x56B6C2, 2");  // Boolean / byte / short
			c.put("Style.NUMBER","0x999933, 0");  // qSQL
			c.put("Style.REGEX","0x85502B, 0");  // ()[]
			c.put("Style.OPERATOR","0x222244, 0");  // <= > + - 
			c.put("Style.STRING","0xCC6600, 0");  // qSQL - strings "blah" - green
			c.put("Style.STRING2","0x663300, 0"); 
			c.put("Style.DELIMITER","0xA658BD, 0"); // {} - purple
			c.put("Style.TYPE3","0xE5A07B, 0");
			c.put("Style.ERROR","0xCC0000, 3");
			c.put("Style.DEFAULT","0x000000, 0");
			c.put("Style.KEYWORD","0x3333ee, 0");  // xlog while sums
			c.put("Style.KEYWORD2","0x2222CC, 0"); // .z.* .Q.*
			c.put("Style.TYPE2","0xC07F54, 0"); // Standard SQL keywords
			c.put("Style.WARNING","0xCC0000, 0"); 
			c.put("Style.IDENTIFIER","0x000000, 0");   // qSQL - lots of text e.g. symbols and colNa

			c.put("SelectionColor","0x99ccff");
			c.put("CaretColor","0x000000");
			c.put("PairMarker.Color","0xFF5555");
			c.put("TokenMarker.Color","0xEEAAFF");
			c.put("LineNumbers.CurrentBack","0xcccccc");
			c.put("LineNumbers.Foreground","0x333333");
			c.put("LineNumbers.Background","0xe6e6e6");

			c.put("SingleColorSelect", "false"); // true = turns off syntax highlighting when selected
			c.put("RightMarginColumn", "80");
			c.put("RightMarginColor", "0xEEEEEE");
		}
	}
	
}
