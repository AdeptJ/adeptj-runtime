/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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

package com.adeptj.runtime.common;

import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.exception.SystemException;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static com.adeptj.runtime.common.Constants.UTF8;

/**
 * Passwords, utility for password generation and matching.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Passwords {

    INSTANCE;

    private Config config = Configs.DEFAULT.common();

    private static final Logger LOGGER = LoggerFactory.getLogger(Passwords.class);

    private static final SecureRandom DEFAULT_SECURE_RANDOM;

    static {
        DEFAULT_SECURE_RANDOM = new SecureRandom();
        DEFAULT_SECURE_RANDOM.setSeed(new byte[64]);
    }

    /**
     * Generates the random salt for hashing using SHA1PRNG.
     *
     * @return UTF-8 Base64 encoded hash.
     */
    public String generateSalt() {
        byte[] saltBytes = new byte[this.config.getInt("salt-size")];
        try {
            DEFAULT_SECURE_RANDOM.nextBytes(saltBytes);
            return new String(Base64.getEncoder().encode(saltBytes), UTF8);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Exception while generating salt!!", ex);
            throw new SystemException(ex.getMessage(), ex);
        }
    }

    /**
     * Generates UTF-8 Base64 encoded hashed password using PBKDF2WithHmacSHA256.
     *
     * @param pwd  the password to hash
     * @param salt the additive for more secure hashing
     * @return Hashed password
     */
    public String getHashedPassword(String pwd, String salt) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(this.config.getString("secret-key-algo"));
            PBEKeySpec keySpec = new PBEKeySpec(pwd.toCharArray(), salt.getBytes(UTF8),
                    this.config.getInt("iteration-count"),
                    this.config.getInt("derived-key-size"));
            return new String(Base64.getEncoder().encode(secretKeyFactory.generateSecret(keySpec).getEncoded()), UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeySpecException ex) {
            LOGGER.error("Exception while generating hashed text!!", ex);
            throw new SystemException(ex.getMessage(), ex);
        }
    }
}
