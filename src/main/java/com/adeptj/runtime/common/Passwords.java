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

    /**
     * Generates the random salt for hashing using SHA1PRNG.
     *
     * @return UTF-8 Base64 encoded hash.
     */
    public String generateSalt() {
        Config config = Configs.DEFAULT.common();
        byte[] saltBytes = new byte[config.getInt("salt-size")];
        try {
            SecureRandom.getInstance(config.getString("secure-random-algo"))
                    .nextBytes(saltBytes);
            return new String(Base64.getEncoder().encode(saltBytes), UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
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
            Config config = Configs.DEFAULT.common();
            return new String(Base64.getEncoder()
                    .encode(SecretKeyFactory.getInstance(config.getString("secret-key-algo"))
                            .generateSecret(new PBEKeySpec(pwd.toCharArray(), salt.getBytes(UTF8),
                                    config.getInt("iteration-count"),
                                    config.getInt("derived-key-size")))
                            .getEncoded()), UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeySpecException ex) {
            throw new SystemException(ex.getMessage(), ex);
        }
    }
}
