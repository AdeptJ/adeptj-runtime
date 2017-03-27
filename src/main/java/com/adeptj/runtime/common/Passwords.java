/*
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
package com.adeptj.runtime.common;

import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
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
        try {
            Config config = Configs.DEFAULT.common();
            byte[] salt = new byte[config.getInt("salt-size")];
            SecureRandom.getInstance(config.getString("secure-random-algo")).nextBytes(salt);
            return new String(Base64.getEncoder().encode(salt), Constants.UTF8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates UTF-8 Base64 encoded hashed password
     *
     * @param pwd  the password to hash
     * @param salt the additive for more secure hashing
     * @return Hashed password
     */
    public String hashPwd(String pwd, String salt) {
        try {
            Config config = Configs.DEFAULT.common();
            return new String(
                    Base64.getEncoder()
                            .encode(SecretKeyFactory.getInstance(config.getString("secret-key-algo"))
                                    .generateSecret(new PBEKeySpec(pwd.toCharArray(), salt.getBytes(Constants.UTF8),
                                            config.getInt("iteration-count"), config.getInt("derived-key-size")))
                                    .getEncoded()), Constants.UTF8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
