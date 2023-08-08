package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.UserManager;
import com.typesafe.config.Config;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.util.security.Constraint;

public class SecurityConfigurer {

    public void configure(ConstraintSecurityHandler securityHandler, UserManager userManager, Config commonCfg) {
        // Security settings
        securityHandler.setLoginService(new MVStoreLoginService(userManager));
        securityHandler.setAuthenticator(this.getFormAuthenticator(commonCfg));
        this.addConstraintMapping(securityHandler, commonCfg);
    }

    private FormAuthenticator getFormAuthenticator(Config commonCfg) {
        Config formAuthCfg = commonCfg.getConfig("form-auth");
        String loginUrl = formAuthCfg.getString("login-url");
        String errorUlr = formAuthCfg.getString("error-url");
        return new FormAuthenticator(loginUrl, errorUlr, true);
    }

    private void addConstraintMapping(ConstraintSecurityHandler securityHandler, Config commonCfg) {
        Constraint constraint = this.getConstraint(commonCfg);
        for (String protectedPath : commonCfg.getStringList("protected-paths")) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec(protectedPath);
            securityHandler.addConstraintMapping(constraintMapping);
        }
    }

    private Constraint getConstraint(Config commonCfg) {
        Constraint constraint = new Constraint();
        constraint.setName("AdeptJ Security Constraint");
        constraint.setRoles(commonCfg.getStringList("auth-roles").toArray(new String[0]));
        constraint.setAuthenticate(true);
        return constraint;
    }
}
