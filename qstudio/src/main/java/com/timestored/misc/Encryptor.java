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
package com.timestored.misc;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

/** Allows string encrypion/decryption easily **/
public class Encryptor {

	/** @param keyString base64 encoded string of a byte[] DES key  */
	public static byte[] encrypt(byte[] utf8, String keyString) throws IOException {

		try {
			Cipher ecipher = Cipher.getInstance("DES");
			ecipher.init(Cipher.ENCRYPT_MODE, getKey(keyString));
			byte[] enc = ecipher.doFinal(utf8);
			return enc;
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}
	}

	/** @param keyString base64 encoded string of a byte[] DES key  */
	public static String encrypt(String str, String keyString) throws IOException {
		return Base64.encodeBytes(encrypt(str.getBytes("UTF8"), keyString));
	}

	private static Key getKey(String keyString) throws IOException {
		byte[] p = Base64.decode(keyString);
		Key key = new SecretKeySpec(p, 0, p.length, "DES");
		return key;
	}

	/** @param keyString base64 encoded string of a byte[] DES key  */
	public static byte[] decrypt(byte[] dec, String keyString) throws IOException {
		try {
			Cipher dcipher = Cipher.getInstance("DES");
			dcipher.init(Cipher.DECRYPT_MODE, getKey(keyString));
			return dcipher.doFinal(dec);
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		} catch (GeneralSecurityException e) {
			throw new IOException(e);
		}
	}
	
	/** @param keyString base64 encoded string of a byte[] DES key  */
	public static String decrypt(String str, String keyString) throws IOException {
		return new String(decrypt(Base64.decode(str.getBytes()), keyString), "UTF8");
	}

}