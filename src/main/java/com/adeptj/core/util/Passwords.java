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
package com.adeptj.core.util;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.adeptj.core.config.Configs;
import com.typesafe.config.Config;

/**
 * Passwords, utility for password generation and matching.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Passwords {

	INSTANCE;

	public static final String UTF8 = "UTF-8";

	/**
	 * Generates the salt for hashing.
	 * 
	 * @return UTF-8 Base64 encoded hash.
	 */
	public String generateSalt() {
		try {
			Config config = Configs.INSTANCE.common();
			byte[] salt = new byte[config.getInt("salt-size")];
			SecureRandom.getInstance(config.getString("secure-random-algo")).nextBytes(salt);
			return new String(Base64.getEncoder().encode(salt), UTF8);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Generates UTF-8 Base64 encoded hashed password
	 * 
	 * @param pwd
	 * @param salt
	 * @return Hashed password
	 */
	public String hashPwd(String pwd, String salt) {
		try {
			Config config = Configs.INSTANCE.common();
			return new String(
					Base64.getEncoder()
							.encode(SecretKeyFactory.getInstance(config.getString("secret-key-algo"))
									.generateSecret(new PBEKeySpec(pwd.toCharArray(), salt.getBytes(UTF8),
											config.getInt("iteration-count"), config.getInt("derived-key-size")))
									.getEncoded()), UTF8);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
