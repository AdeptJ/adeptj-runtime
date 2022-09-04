package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.UserManager;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;

public class SecurityConfigurer {

    public void configure(ServletContextHandler context, UserManager userManager) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);
        constraint.setRoles(new String[]{"OSGiAdmin"});
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/system/console/*");

        ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler) context.getSecurityHandler();
        securityHandler.addConstraintMapping(constraintMapping);

        securityHandler.setLoginService(new MVStoreLoginService(userManager));

        FormAuthenticator authenticator = new FormAuthenticator("/admin/login", "/admin/login", true);
        securityHandler.setAuthenticator(authenticator);
    }
}
