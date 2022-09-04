package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.UserManager;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;

import java.security.Principal;

public class MVStoreRealm extends RealmBase {

    private final UserManager userManager;

    public MVStoreRealm(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected String getPassword(String username) {
        return this.userManager.getPassword(username);
    }

    @Override
    protected Principal getPrincipal(String username) {
        return new GenericPrincipal(username, this.getPassword(username), this.userManager.getRoles(username));
    }
}
