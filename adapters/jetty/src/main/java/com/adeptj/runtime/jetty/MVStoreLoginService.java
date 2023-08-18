package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.UserManager;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.util.StringUtil;

import java.util.List;
import java.util.UUID;
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
        if (StringUtil.isEmpty(password)) {
            // This is just called to waste a bit of time as not to reveal that the user does not exist.
            this.userManager.encodePassword(UUID.randomUUID().toString());
            return null;
        }
        return new UserPrincipal(username, new Sha256Base64EncodedPassword(password));
    }
}
