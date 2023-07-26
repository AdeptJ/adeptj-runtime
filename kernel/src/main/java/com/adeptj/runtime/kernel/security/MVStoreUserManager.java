package com.adeptj.runtime.kernel.security;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.UserManager;
import com.adeptj.runtime.kernel.util.MVStoreUtil;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import static com.adeptj.runtime.kernel.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.kernel.Constants.MV_CREDENTIALS_STORE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MVStoreUserManager implements UserManager {

    @Override
    public String getPassword(String username) {
        return MVStoreUtil.getValue(MV_CREDENTIALS_STORE, H2_MAP_ADMIN_CREDENTIALS, username);
    }

    @Override
    public boolean matchPassword(String inputPassword, String storedPassword) {
        if (StringUtils.isEmpty(inputPassword) || StringUtils.isEmpty(storedPassword)) {
            return false;
        }
        byte[] inputPwdBytes = inputPassword.getBytes(UTF_8);
        byte[] storedPwdBytes = storedPassword.getBytes(UTF_8);
        byte[] digest = PasswordEncoder.encodePassword(inputPwdBytes);
        try {
            return MessageDigest.isEqual(digest, storedPwdBytes);
        } finally {
            this.nullSafeWipe(inputPwdBytes, digest, storedPwdBytes);
        }
    }

    @Override
    public boolean matchPassword(char[] inputPassword, char[] storedPassword) {
        if (ArrayUtils.isEmpty(inputPassword) || ArrayUtils.isEmpty(storedPassword)) {
            return false;
        }
        byte[] inputPwdBytes = this.toByteArray(inputPassword);
        byte[] storedPwdBytes = this.toByteArray(storedPassword);
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
        return ConfigProvider.getInstance()
                .getMainConfig()
                .getStringList("common.user-roles-mapping." + username);
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
