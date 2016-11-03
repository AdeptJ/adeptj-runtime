/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/
package com.adeptj.modularweb.runtime.common;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Passwords, utility for password generation and matching.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Passwords {

	INSTANCE;

	public static final int RANDOM_BYTES = 16;

	public static final int KEY_LENGTH = 32 * 8;

	public static final String ALGO_SHA1PRNG = "SHA1PRNG";

	public static final String ALGO_PBKD = "PBKDF2WithHmacSHA1";

	public static final int ITERATIONS = 1000;

	public static final String UTF8 = "UTF-8";

	public String generateSalt() {
		try {
			byte[] salt = new byte[RANDOM_BYTES];
			SecureRandom.getInstance(ALGO_SHA1PRNG).nextBytes(salt);
			return new String(salt, UTF8);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String hashPwd(String pwd, String salt) {
		try {
			return Base64.getEncoder()
					.encodeToString(SecretKeyFactory.getInstance(ALGO_PBKD)
							.generateSecret(
									new PBEKeySpec(pwd.toCharArray(), salt.getBytes(UTF8), ITERATIONS, KEY_LENGTH))
							.getEncoded());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
