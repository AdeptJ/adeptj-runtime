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

package com.adeptj.runtime.server;

import io.undertow.security.idm.Credential;
import io.undertow.security.idm.PasswordCredential;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVStore;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static com.adeptj.runtime.common.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.common.Constants.MV_CREDENTIALS_STORE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * CredentialMatcher - utility creates an SHA-256 digest of the supplied password and matches with the one stored
 * in MVStore.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class CredentialMatcher {

    private static final String SHA_256 = "SHA-256";

    private CredentialMatcher() {
    }

    static boolean match(String username, Credential credential) {
        char[] inputPwd = ((PasswordCredential) credential).getPassword();
        if (StringUtils.isEmpty(username) || ArrayUtils.isEmpty(inputPwd)) {
            return false;
        }
        byte[] inputPwdBytes = null;
        byte[] digest = null;
        byte[] storedPwdBytes = null;
        try (MVStore store = MVStore.open(MV_CREDENTIALS_STORE)) {
            String storedPwd = (String) store.openMap(H2_MAP_ADMIN_CREDENTIALS).get(username);
            if (StringUtils.isEmpty(storedPwd)) {
                return false;
            }
            ByteBuffer buffer = UTF_8.encode(CharBuffer.wrap(inputPwd));
            inputPwdBytes = new byte[buffer.limit()];
            buffer.get(inputPwdBytes);
            digest = Base64.getEncoder().encode(sha256(inputPwdBytes));
            storedPwdBytes = storedPwd.getBytes(UTF_8);
            return MessageDigest.isEqual(digest, storedPwdBytes);
        } finally {
            nullSafeWipe(inputPwdBytes, digest, storedPwdBytes);
        }
    }

    private static byte[] sha256(byte[] inputPwdBytes) {
        try {
            return MessageDigest.getInstance(SHA_256).digest(inputPwdBytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static void nullSafeWipe(byte[]... arrays) {
        if (arrays != null) {
            for (byte[] array : arrays) {
                Arrays.fill(array, (byte) 0);
            }
        }
    }
}
