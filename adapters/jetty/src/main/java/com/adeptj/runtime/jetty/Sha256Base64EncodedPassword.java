package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.eclipse.jetty.util.security.Password;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Sha256Base64EncodedPassword extends Password {

    public Sha256Base64EncodedPassword(String password) {
        super(password);
    }

    @Override
    public boolean check(Object credentials) {
        return super.check(new String(PasswordEncoder.encodePassword((String) credentials), UTF_8));
    }
}
