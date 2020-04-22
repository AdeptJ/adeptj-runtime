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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVStore;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Base64;

import static com.adeptj.runtime.common.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.common.Constants.MV_CREDENTIALS_STORE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * CredentialMatcher, Logic for creating password hash and comparing submitted credential is same as implemented
 * in [org.apache.felix.webconsole.internal.servlet.Password]
 * <p>
 * Because, we want to be consistent with the hashing mechanism used by OSGi Web Console configuration management,
 * but supporting classes available there are package private and therefore can't be accessible to outside world.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class CredentialMatcher {

    private CredentialMatcher() {
    }

    static boolean match(String username, Credential credential) {
        char[] inputPwd = ((PasswordCredential) credential).getPassword();
        if (StringUtils.isEmpty(username) || ArrayUtils.isEmpty(inputPwd)) {
            return false;
        }
        try (MVStore store = MVStore.open(MV_CREDENTIALS_STORE)) {
            String storedPwd = (String) store.openMap(H2_MAP_ADMIN_CREDENTIALS).get(username);
            if (StringUtils.isEmpty(storedPwd)) {
                return false;
            }
            ByteBuffer buffer = UTF_8.encode(CharBuffer.wrap(inputPwd));
            byte[] hash = DigestUtils.sha256(Arrays.copyOf(buffer.array(), buffer.limit()));
            byte[] inputPwdBytes = Base64.getEncoder().encode(hash);
            byte[] storedPwdBytes = storedPwd.getBytes(UTF_8);
            boolean match = Arrays.equals(inputPwdBytes, storedPwdBytes);
            Arrays.fill(hash, (byte) 0);
            Arrays.fill(inputPwdBytes, (byte) 0);
            Arrays.fill(storedPwdBytes, (byte) 0);
            return match;
        }
    }
}
