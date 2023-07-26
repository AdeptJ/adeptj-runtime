package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.UserManager;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;

import java.util.List;
import java.util.stream.Collectors;

public class MVStoreLoginService extends AbstractLoginService {

    private final UserManager userManager;

    public MVStoreLoginService(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected List<RolePrincipal> loadRoleInfo(UserPrincipal user) {
        return this.userManager.getRoles(user.getName())
                .stream()
                .map(RolePrincipal::new)
                .collect(Collectors.toList());
    }

    @Override
    protected UserPrincipal loadUserInfo(String username) {
        String password = this.userManager.getPassword(username);
        return new UserPrincipal(username, new Sha256Base64EncodedPassword(password));
    }
}
