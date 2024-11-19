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
package com.timestored.connections;

import java.io.IOException;

import com.timestored.StringUtils;
import com.timestored.misc.Base64;
import com.timestored.misc.Encryptor;

/**
 * Helper that handles backwards compatible transforms of qStudio saved data
 */
public class PreferenceHelper {

	/*
	 * Originally we added encryption to hide connection strings,
	 * But once we added compression, encryption wasn't needed for obfuscation
	 * Still here for backwards compatibility purposes
	 */
	
	private static final String ENCRYPTED_PREFIX = "XXX_I_AM_ENCRYPTED_XXX";
	private static final String ENCODED_PREFIX = "XXX_ENCODED_XXX";
	private static final String DES_KEY = "ov0EXlLxDUY=";

	public static String decode(String txt) throws IOException {
		if(txt == null) {
			return null;
		}
		if (txt.startsWith(ENCRYPTED_PREFIX)) {
			String t = txt.substring(ENCRYPTED_PREFIX.length());
			txt = Encryptor.decrypt(t, DES_KEY);
		} else if (txt.startsWith(ENCODED_PREFIX)) {
			String t = txt.substring(ENCODED_PREFIX.length());
			byte[] b = Base64.decode(t);
			txt = StringUtils.decompress(b);
		}
		return txt;
	}

	public static String encode(String txt) {
		if(txt == null) {
			return null;
		}
		byte[] t = StringUtils.compress(txt);
//			t = Encryptor.encrypt(t, DES_KEY);
		return ENCODED_PREFIX + Base64.encodeBytes(t);
	}
}
