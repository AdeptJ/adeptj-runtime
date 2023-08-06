package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.UserManager;
import com.typesafe.config.Config;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;

import java.util.List;

public class SecurityConfigurer {

    public void configure(ServletContextHandler context, UserManager userManager, Config appConfig) {
        Config commonCfg = appConfig.getConfig("main.common");
        SecurityHandler securityHandler = context.getSecurityHandler();
        securityHandler.setLoginService(new MVStoreLoginService(userManager));
        this.addConstraintMapping(securityHandler, commonCfg);
        this.addAuthenticator(securityHandler, commonCfg);
        // Session settings
        SessionHandler sessionHandler = context.getSessionHandler();
        sessionHandler.setHttpOnly(commonCfg.getBoolean("session-cookie-httpOnly"));
        sessionHandler.setMaxInactiveInterval(commonCfg.getInt("session-timeout"));
    }

    private void addAuthenticator(SecurityHandler securityHandler, Config commonCfg) {
        Config formAuthCfg = commonCfg.getConfig("form-auth");
        String loginUrl = formAuthCfg.getString("login-url");
        String errorUlr = formAuthCfg.getString("error-url");
        FormAuthenticator authenticator = new FormAuthenticator(loginUrl, errorUlr, true);
        securityHandler.setAuthenticator(authenticator);
    }

    private void addConstraintMapping(SecurityHandler securityHandler, Config commonCfg) {
        Constraint constraint = this.getConstraint(commonCfg);
        List<String> protectedPaths = commonCfg.getStringList("protected-paths");
        for (String protectedPath : protectedPaths) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec(protectedPath);
            ((ConstraintSecurityHandler) securityHandler).addConstraintMapping(constraintMapping);
        }
    }

    private Constraint getConstraint(Config commonCfg) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);
        List<String> authRoles = commonCfg.getStringList("auth-roles");
        constraint.setRoles(authRoles.toArray(new String[0]));
        constraint.setAuthenticate(true);
        return constraint;
    }
}
