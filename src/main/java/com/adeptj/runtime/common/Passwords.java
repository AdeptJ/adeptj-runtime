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

/**
 * Passwords, utility for password generation and matching.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum Passwords {

    INSTANCE;

    /**
     * Generates the salt for hashing.
     *
     * @return UTF-8 Base64 encoded hash.
     */
    public String generateSalt() {
        String salt = null;
        Config config = Configs.DEFAULT.common();
        byte[] saltBytes = new byte[config.getInt("salt-size")];
        try {
            SecureRandom.getInstance(config.getString("secure-random-algo")).nextBytes(saltBytes);
            salt = new String(Base64.getEncoder().encode(saltBytes), Constants.UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new SystemException(ex.getMessage(), ex);
        }
        return salt;
    }

    /**
     * Generates UTF-8 Base64 encoded hashed password
     *
     * @param pwd  the password to hash
     * @param salt the additive for more secure hashing
     * @return Hashed password
     */
    public String hashPwd(String pwd, String salt) {
        String hashedStr = null;
        try {
            Config config = Configs.DEFAULT.common();
            hashedStr = new String(
                    Base64.getEncoder()
                            .encode(SecretKeyFactory.getInstance(config.getString("secret-key-algo"))
                                    .generateSecret(new PBEKeySpec(pwd.toCharArray(), salt.getBytes(Constants.UTF8),
                                            config.getInt("iteration-count"), config.getInt("derived-key-size")))
                                    .getEncoded()), Constants.UTF8);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeySpecException ex) {
            throw new SystemException(ex.getMessage(), ex);
        }
        return hashedStr;
    }
}
