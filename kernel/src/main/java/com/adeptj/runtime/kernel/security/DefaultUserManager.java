/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.kernel.security;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.UserManager;
import com.adeptj.runtime.kernel.util.Configs;
import com.adeptj.runtime.kernel.util.MVStoreUtil;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default UserManager implementation for handling user authentication on AdeptJ Runtime.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class DefaultUserManager implements UserManager {

    @Override
    public String getPassword(String username) {
        return MVStoreUtil.getPassword(username);
    }

    @Override
    public boolean matchPassword(String inputPassword, String storedPassword) {
        if (StringUtils.isEmpty(inputPassword) || StringUtils.isEmpty(storedPassword)) {
            return false;
        }
        byte[] inputPwdBytes = inputPassword.getBytes(UTF_8);
        byte[] storedPwdBytes = storedPassword.getBytes(UTF_8);
        return this.doMatchPassword(inputPwdBytes, storedPwdBytes);
    }

    @Override
    public boolean matchPassword(char[] inputPassword, char[] storedPassword) {
        if (ArrayUtils.isEmpty(inputPassword) || ArrayUtils.isEmpty(storedPassword)) {
            return false;
        }
        byte[] inputPwdBytes = this.toByteArray(inputPassword);
        byte[] storedPwdBytes = this.toByteArray(storedPassword);
        return this.doMatchPassword(inputPwdBytes, storedPwdBytes);
    }

    private boolean doMatchPassword(byte[] inputPwdBytes, byte[] storedPwdBytes) {
        byte[] inputPwdDigest = PasswordEncoder.encodePassword(inputPwdBytes);
        try {
            return MessageDigest.isEqual(inputPwdDigest, storedPwdBytes);
        } finally {
            nullSafeWipe(inputPwdBytes, inputPwdDigest, storedPwdBytes);
        }
    }

    @Override
    public String encodePassword(String password) {
        return new String(PasswordEncoder.encodePassword(password), UTF_8);
    }

    @Override
    public List<String> getRoles(String username) {
        String path = "common.user-roles-mapping." + username;
        return Configs.getStringList(ConfigProvider.getInstance().getMainConfig(), path);
    }

    private byte[] toByteArray(char[] chars) {
        ByteBuffer buffer = UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[buffer.limit()];
        System.arraycopy(buffer.array(), 0, bytes, 0, buffer.limit());
        return bytes;
    }

    private void nullSafeWipe(byte[]... arrays) {
        for (byte[] array : arrays) {
            if (array != null) {
                Arrays.fill(array, (byte) 0);
            }
        }
    }
}
