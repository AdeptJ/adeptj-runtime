package com.adeptj.runtime.kernel.security;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.UserManager;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

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
        try (MVStore store = MVStore.open(MV_CREDENTIALS_STORE)) {
            return store.openMap(H2_MAP_ADMIN_CREDENTIALS, new MVMap.Builder<String, String>()).get(username);
        }
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
    public boolean match(char[] inputPassword, char[] storedPassword) {
        if (ArrayUtils.isEmpty(inputPassword) || ArrayUtils.isEmpty(storedPassword)) {
            return false;
        }
        byte[] inputPwdBytes = toByteArray(inputPassword);
        byte[] storedPwdBytes = toByteArray(storedPassword);
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
        return ConfigProvider.getInstance().getMainConfig().getStringList("common.user-roles-mapping." + username);
    }

    private static byte[] toByteArray(char[] chars) {
        ByteBuffer buffer = UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
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
