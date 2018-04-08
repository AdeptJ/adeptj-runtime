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

package io.adeptj.runtime.common;

import com.typesafe.config.Config;
import io.adeptj.runtime.config.Configs;
import io.adeptj.runtime.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static io.adeptj.runtime.common.Constants.UTF8;

/**
 * CryptoSupport, utility for salt and hashed text generation and matching.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class CryptoSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoSupport.class);

    private static final SecureRandom DEFAULT_SECURE_RANDOM = new SecureRandom();

    private CryptoSupport() {
    }

    /**
     * Generates the random salt for hashing using SHA1PRNG.
     *
     * @return UTF-8 Base64 encoded hash.
     */
    public static String saltBase64() {
        byte[] saltBytes = new byte[Configs.DEFAULT.common().getInt("salt-size")];
        try {
            DEFAULT_SECURE_RANDOM.nextBytes(saltBytes);
            return new String(Base64.getEncoder().encode(saltBytes), UTF8);
        } catch (UnsupportedEncodingException | SystemException ex) {
            LOGGER.error("Exception while generating salt!!", ex);
            throw new SystemException(ex.getMessage(), ex);
        }
    }

    /**
     * Generates UTF-8 Base64 encoded hashed text using PBKDF2WithHmacSHA256.
     *
     * @param plainText the text to hash
     * @param salt      the additive for more secure hashing
     * @return Hashed text
     */
    public static String hashBase64(String plainText, String salt) {
        Config config = Configs.DEFAULT.common();
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(config.getString("secret-key-algo"));
            PBEKeySpec keySpec = new PBEKeySpec(plainText.toCharArray(), salt.getBytes(UTF8),
                    config.getInt("iteration-count"),
                    config.getInt("derived-key-size"));
            return new String(Base64.getEncoder().encode(secretKeyFactory.generateSecret(keySpec).getEncoded()), UTF8);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOGGER.error("Exception while generating hashed text!!", ex);
            throw new SystemException(ex.getMessage(), ex);
        }
    }
}
