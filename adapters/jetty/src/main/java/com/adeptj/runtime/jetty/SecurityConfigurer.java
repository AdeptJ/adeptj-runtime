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

import static org.eclipse.jetty.util.security.Constraint.__FORM_AUTH;

public class SecurityConfigurer {

    public void configure(ServletContextHandler context, UserManager userManager, Config appConfig) {
        Config commonCfg = appConfig.getConfig("main.common");
        // Security settings
        SecurityHandler securityHandler = context.getSecurityHandler();
        securityHandler.setLoginService(new MVStoreLoginService(userManager));
        securityHandler.setAuthenticator(this.getFormAuthenticator(commonCfg));
        this.addConstraintMapping(securityHandler, commonCfg);
        // Session settings
        SessionHandler sessionHandler = context.getSessionHandler();
        sessionHandler.setHttpOnly(commonCfg.getBoolean("session-cookie-httpOnly"));
        sessionHandler.setMaxInactiveInterval(commonCfg.getInt("session-timeout"));
    }

    private FormAuthenticator getFormAuthenticator(Config commonCfg) {
        Config formAuthCfg = commonCfg.getConfig("form-auth");
        String loginUrl = formAuthCfg.getString("login-url");
        String errorUlr = formAuthCfg.getString("error-url");
        return new FormAuthenticator(loginUrl, errorUlr, true);
    }

    private void addConstraintMapping(SecurityHandler securityHandler, Config commonCfg) {
        Constraint constraint = this.getConstraint(commonCfg);
        for (String protectedPath : commonCfg.getStringList("protected-paths")) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec(protectedPath);
            ((ConstraintSecurityHandler) securityHandler).addConstraintMapping(constraintMapping);
        }
    }

    private Constraint getConstraint(Config commonCfg) {
        Constraint constraint = new Constraint();
        constraint.setName(__FORM_AUTH);
        constraint.setRoles(commonCfg.getStringList("auth-roles").toArray(new String[0]));
        constraint.setAuthenticate(true);
        return constraint;
    }
}
