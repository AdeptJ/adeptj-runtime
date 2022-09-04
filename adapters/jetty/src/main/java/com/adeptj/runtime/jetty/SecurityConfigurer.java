package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.UserManager;
import com.typesafe.config.Config;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;

import java.util.List;

public class SecurityConfigurer {

    public void configure(ServletContextHandler context, UserManager userManager) {
        Config commonCfg = ConfigProvider.getInstance().getMainConfig().getConfig("common");
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);
        List<String> authRoles = commonCfg.getStringList("auth-roles");
        constraint.setRoles(authRoles.toArray(new String[0]));
        constraint.setAuthenticate(true);
        ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler) context.getSecurityHandler();
        List<String> protectedPaths = commonCfg.getStringList("protected-paths");
        for (String protectedPath : protectedPaths) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec(protectedPath);
            securityHandler.addConstraintMapping(constraintMapping);
        }
        securityHandler.setLoginService(new MVStoreLoginService(userManager));
        Config formAuthCfg = commonCfg.getConfig("form-auth");
        String loginUrl = formAuthCfg.getString("login-url");
        String errorUlr = formAuthCfg.getString("error-url");
        FormAuthenticator authenticator = new FormAuthenticator(loginUrl, errorUlr, true);
        securityHandler.setAuthenticator(authenticator);
    }
}
