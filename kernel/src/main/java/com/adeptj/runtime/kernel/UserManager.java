package com.adeptj.runtime.kernel;

import java.util.List;

public interface UserManager {

    String getPassword(String username);

    boolean matchPassword(String inputPassword, String storedPassword);

    boolean match(char[] inputPassword, char[] storedPassword);

    String encodePassword(String password);

    List<String> getRoles(String username);
}
