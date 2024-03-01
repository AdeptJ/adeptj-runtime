package com.adeptj.runtime.kernel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.adeptj.runtime.kernel.Constants.SHA_256;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PasswordEncoder {

    public static byte[] encodePassword(byte[] password) {
        return Base64.getEncoder().encode(sha256(password));
    }

    public static byte[] encodePassword(String password) {
        return Base64.getEncoder().encode(sha256(password.getBytes(UTF_8)));
    }

    private static byte[] sha256(byte[] inputPwdBytes) {
        try {
            return MessageDigest.getInstance(SHA_256).digest(inputPwdBytes);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private PasswordEncoder() {
    }
}
