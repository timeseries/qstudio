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
package com.timestored.qstudio.qtheme;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

import com.timestored.theme.Icon;
import com.timestored.theme.IconHelper;

/**
 * Theme specifics to qStudio.
 */
public class QTheme {

	public static final DefaultListCellRenderer LIST_RENDERER = new MyListRenderer();
	
	private static class MyListRenderer extends DefaultListCellRenderer {

		@Override public Component getListCellRendererComponent(JList list, Object value, 
				int index, boolean isSelected, boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, 
					isSelected, cellHasFocus);

			if (c instanceof JLabel) {
				((JLabel) c).setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			}
			return c;
		}
	}
	
	public static enum QIcon implements Icon {

		USER_ADD("user_add.png"),
		USER_COMMENT("user_comment.png"),
		USER_DELETE("user_delete.png"),
		USER_EDIT("user_edit.png"),
		USER_ORANGE("user_orange.png"),
		USER_RED("user_red.png"),
		USER_SUIT("user_suit.png"),
		USER("user.png"),
		ATTRIB_N("setattrn.png"),
		ATTRIB_U("setattru.png"),
		ATTRIB_P("setattrp.png"),
		ATTRIB_S("setattrs.png"),
		ATTRIB_G("setattrg.png");
		
		private final ImageIcon imageIcon;
		private final ImageIcon imageIcon16;
		public final ImageIcon imageIcon32;


		/** @return Default sized imageIcon */
		public ImageIcon get() { return imageIcon; }
		
		/** @return Size 16*16 imageIcon */
		public ImageIcon get16() { return imageIcon16; }
		
		/** @return Size 32*32 imageIcon */
		public ImageIcon get32() { return imageIcon32; }
		
		
		public BufferedImage getBufferedImage() {
			return IconHelper.getBufferedImage(imageIcon);
		}
		
		QIcon(String loc) {
			ImageIcon[] icons = IconHelper.getDiffSizesOfIcon(QIcon.class.getResource(loc));
			imageIcon = icons[0];
			imageIcon16 = icons[1];
			imageIcon32 = icons[2];
		}
	}
}
